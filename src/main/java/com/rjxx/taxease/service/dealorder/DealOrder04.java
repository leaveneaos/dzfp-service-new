package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.service.result.Result04;

import com.rjxx.taxease.utils.XmlMapUtils;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;

import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.time.TimeUtil;

import com.rjxx.utils.XmlJaxbUtils;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xlm on 2017/5/23.
 */

@Service
public class DealOrder04 implements IDealOrder{



    @Autowired
    private SkpService skpservice;



    @Autowired
    private CszbService cszbservice;



    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private JylsService jylsService;

    @Autowired
    private JyspmxService jyspmxService;

    @Autowired
    private SkService skService;



    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public String execute(String gsdm, String orderData, String Operation) {

            Map inputMap = dealOperation04(gsdm, orderData);
            String clientNO = String.valueOf(inputMap.get("ClientNO"));
            Double TotalAmount = Double.parseDouble(String.valueOf(inputMap.get("TotalAmount")));//价税合计
            String CNDNCode = String.valueOf(inputMap.get("CNDNCode"));//原发票代码
            String CNDNNo = String.valueOf(inputMap.get("CNDNNo"));//原发票号码
            String InvType = String.valueOf(inputMap.get("InvType"));
            String ServiceType = String.valueOf(inputMap.get("ServiceType"));

            Result04 result04 = new Result04();
        try {
            Map params = new HashMap();
            params.put("kpddm", clientNO);
            params.put("gsdm", gsdm);
            Skp skp = skpservice.findOneByParams(params);
            int xfid = skp.getXfid();
            int kpdid = skp.getId();
            Cszb cszb = cszbservice.getSpbmbbh(gsdm, xfid, kpdid, "sftbkp");
            String sftbkp = cszb.getCsz();
            if (!InvType.equals("12")) {
                result04.setReturnCode("9999");
                result04.setReturnMessage("该接口目前只支持电子发票红冲！");
                return XmlJaxbUtils.toXml(result04);
            }
            if (CNDNCode.equals("") || CNDNNo.equals("")) {
                result04.setReturnCode("9999");
                result04.setReturnMessage("原发票代码、发票号码不允许为空！");
                return XmlJaxbUtils.toXml(result04);
            }
            if(!ServiceType.equals("1")){
                result04.setReturnCode("9999");
                result04.setReturnMessage("该接口的发票业务类型ServiceType必须为红子发票1");
                return XmlJaxbUtils.toXml(result04);
            }
            Kpls parms = new Kpls();
            parms.setFpdm(CNDNCode);
            parms.setFphm(CNDNNo);
            Kpls kpls = kplsService.findByfphm(parms);//查询原开票流水
            if (TotalAmount != -kpls.getJshj()) {
                result04.setReturnCode("9999");
                result04.setReturnMessage("价税合计与原开票价税合计不符！");
                return XmlJaxbUtils.toXml(result04);
            }
            Map map = new HashMap();
            map.put("kplsh", kpls.getKplsh());
            List<Kpspmx> kpspmxList = kpspmxService.findMxList(map);
            Integer djh = kpls.getDjh();
            Map param4 = new HashMap<>();
            param4.put("djh", djh);
            Jyls jyls = jylsService.findJylsByDjh(param4);
            String ddh = jyls.getDdh(); // 查询原交易流水得ddh
            Kpls kpls2 = save(ddh,kpls, kpspmxList, sftbkp);
            if (sftbkp.equals("是")) {   //是否同步开票
                kpls2.setFpztdm("14");
                kplsService.save(kpls2);
                InvoiceResponse response = skService.callService( kpls2.getKplsh().intValue());
                if ("0000".equals(response.getReturnCode())) {
                    result04.setReturnCode("0000");
                    result04.setReturnMessage("红冲成功！");
                } else {
                    result04.setReturnCode("9999");
                    result04.setReturnMessage(response.getReturnMessage());
                }
            } else {
                kpls2.setFpztdm("04");
                kplsService.save(kpls2);
                result04.setReturnCode("0000");
                result04.setReturnMessage("红冲成功！请再开票软件中查看！");
            }
            return XmlJaxbUtils.toXml(result04);
        }catch (Exception e){
            e.printStackTrace();
            result04.setReturnCode("9999");
            result04.setReturnMessage(e.getMessage());
            return XmlJaxbUtils.toXml(result04);
        }
    }

    public Kpls save(String ddh,Kpls kpls,List<Kpspmx> kpspmxList,String sftbkp){
        //保存交易流水
        Jyls jyls1 = new Jyls();
        jyls1.setDdh(ddh);
        String jylsh = "JY" + new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        jyls1.setJylsh(jylsh);
        jyls1.setJylssj(TimeUtil.getNowDate());
        jyls1.setFpzldm(kpls.getFpzldm());
        jyls1.setFpczlxdm("12");
        jyls1.setClztdm("02");
        jyls1.setXfid(kpls.getXfid());
        jyls1.setXfsh(kpls.getXfsh());
        jyls1.setXfmc(kpls.getXfmc());
        jyls1.setXfyh(kpls.getXfyh());
        jyls1.setXfyhzh(kpls.getXfyhzh());
        jyls1.setXflxr(kpls.getXflxr());
        jyls1.setXfdh(kpls.getXfdh());
        jyls1.setXfdz(kpls.getXfdz());
        jyls1.setGfid(kpls.getGfid());
        jyls1.setGfsh(kpls.getGfsh());
        jyls1.setGfmc(kpls.getGfmc());
        jyls1.setGfyh(kpls.getGfyh());
        jyls1.setGfyhzh(kpls.getGfyhzh());
        jyls1.setGflxr(kpls.getGflxr());
        jyls1.setGfdh(kpls.getGfdh());
        jyls1.setGfdz(kpls.getGfdz());
        jyls1.setGfyb(kpls.getGfyb());
        jyls1.setGfemail(kpls.getGfemail());
        //jyls1.setClztdm("01");
        jyls1.setBz(kpls.getBz());
        jyls1.setSkr(kpls.getSkr());
        jyls1.setKpr(kpls.getKpr());
        jyls1.setFhr(kpls.getFhr());
        jyls1.setSsyf(kpls.getSsyf());
        jyls1.setYfpdm(kpls.getFpdm());
        jyls1.setYfphm(kpls.getFphm());
        jyls1.setHsbz("0");
        jyls1.setJshj(-kpls.getJshj());
        jyls1.setYkpjshj(-kpls.getJshj());
        jyls1.setYxbz("1");
        jyls1.setGsdm(kpls.getGsdm());
        jyls1.setLrry(kpls.getLrry());
        jyls1.setLrsj(TimeUtil.getNowDate());
        jyls1.setXgry(kpls.getLrry());
        jyls1.setXgsj(TimeUtil.getNowDate());
        jyls1.setSkpid(kpls.getSkpid());

        jylsService.save(jyls1);
        //保存开票流水
        Kpls kpls2 = new Kpls();
        kpls2.setDjh(jyls1.getDjh());
        kpls2.setJylsh(jylsh);
        kpls2.setJylssj(jyls1.getJylssj());
        kpls2.setFpzldm(jyls1.getFpzldm());
        kpls2.setFpczlxdm(jyls1.getFpczlxdm());
        kpls2.setXfid(jyls1.getXfid());
        kpls2.setXfsh(jyls1.getXfsh());
        kpls2.setXfmc(jyls1.getXfmc());
        kpls2.setXfyh(jyls1.getXfyh());
        kpls2.setXfyhzh(jyls1.getXfyhzh());
        kpls2.setXflxr(jyls1.getXflxr());
        kpls2.setXfdh(jyls1.getXfdh());
        kpls2.setXfdz(jyls1.getXfdz());
        kpls2.setGfid(jyls1.getGfid());
        kpls2.setGfsh(jyls1.getGfsh());
        kpls2.setGfmc(jyls1.getGfmc());
        kpls2.setGfyh(jyls1.getGfyh());
        kpls2.setGfyhzh(jyls1.getGfyhzh());
        kpls2.setGflxr(jyls1.getGflxr());
        kpls2.setGfdh(jyls1.getGfdh());
        kpls2.setGfdz(jyls1.getGfdz());
        kpls2.setGfyb(jyls1.getGfyb());
        kpls2.setGfemail(jyls1.getGfemail());
        kpls2.setBz(jyls1.getBz());
        kpls2.setSkr(jyls1.getSkr());
        kpls2.setKpr(jyls1.getKpr());
        kpls2.setFhr(jyls1.getFhr());
        kpls2.setHztzdh(jyls1.getHztzdh());
        kpls2.setHkFpdm(jyls1.getYfpdm());
        kpls2.setHkFphm(jyls1.getYfphm());
        kpls2.setHzyfpdm(jyls1.getYfpdm());
        kpls2.setHzyfphm(jyls1.getYfphm());
        kpls2.setJshj(jyls1.getJshj());
        kpls2.setHjje(-kpls.getHjje());
        kpls2.setHjse(-kpls.getHjse());
        kpls2.setGsdm(jyls1.getGsdm());
        kpls2.setYxbz("1");
        kpls2.setLrsj(jyls1.getLrsj());
        kpls2.setXgsj(jyls1.getXgsj());
        kpls2.setSkpid(jyls1.getSkpid());
        kpls2.setLrry(jyls1.getLrry());
        kpls2.setXgry(jyls1.getLrry());
        kplsService.save(kpls2);
        for(Kpspmx kpspmx:kpspmxList){
            Jyspmx jyspmx = new Jyspmx();
            jyspmx.setDjh(jyls1.getDjh());
            jyspmx.setSpmxxh(kpspmx.getSpmxxh());
            jyspmx.setSpdm(kpspmx.getSpdm());
            jyspmx.setSpmc(kpspmx.getSpmc());
            jyspmx.setSpggxh(kpspmx.getSpggxh());
            jyspmx.setSpdw(kpspmx.getSpdw());
            jyspmx.setSps(-kpspmx.getSps());
            jyspmx.setSpdj(-(kpspmx.getSpdj() == null ? null : -kpspmx.getSpdj()));
            jyspmx.setSpje(-kpspmx.getSpje());
            jyspmx.setSpsl(kpspmx.getSpsl());
            jyspmx.setSpse(-kpspmx.getSpse());
            jyspmx.setJshj(-(kpspmx.getSpje()+kpspmx.getSpse()));
            jyspmx.setYkphj(-(kpspmx.getSpje()+kpspmx.getSpse()));
            jyspmx.setGsdm(kpspmx.getGsdm());
            jyspmx.setFphxz(kpspmx.getFphxz());
            jyspmx.setKce(kpspmx.getKce());
            jyspmx.setLrry(kpspmx.getLrry());
            jyspmx.setLrsj(TimeUtil.getNowDate());
            jyspmx.setXgsj(TimeUtil.getNowDate());
            jyspmx.setLslbz(kpspmx.getLslbz());
            jyspmx.setSkpid(jyls1.getSkpid());
            jyspmxService.save(jyspmx);
            Kpspmx kpspmx1=new Kpspmx();
            kpspmx1.setKplsh(kpls2.getKplsh());
            kpspmx1.setDjh(jyspmx.getDjh());
            kpspmx1.setSpmxxh(jyspmx.getSpmxxh());
            kpspmx1.setFphxz(jyspmx.getFphxz());
            kpspmx1.setSpdm(jyspmx.getSpdm());
            kpspmx1.setSpmc(jyspmx.getSpmc());
            kpspmx1.setSpggxh(jyspmx.getSpggxh());
            kpspmx1.setSpdw(jyspmx.getSpdw());
            if (jyspmx.getSpdj() != null) {
                kpspmx1.setSpdj(jyspmx.getSpdj().doubleValue());
            }
            kpspmx1.setSpdw(jyspmx.getSpdw());
            if (jyspmx.getSps() != null) {
                kpspmx1.setSps(jyspmx.getSps().doubleValue());
            }
            kpspmx1.setSpje(jyspmx.getSpje().doubleValue());
            kpspmx1.setSpsl(jyspmx.getSpsl().doubleValue());
            kpspmx1.setSpse(jyspmx.getSpse().doubleValue());
            kpspmx1.setHcrq(TimeUtil.getNowDate());
            kpspmx1.setLrsj(jyspmx.getLrsj());
            kpspmx1.setLrry(jyspmx.getLrry());
            kpspmx1.setXgsj(jyspmx.getXgsj());
            kpspmx1.setXgry(jyspmx.getXgry());
            kpspmx1.setKhcje(0d);
            kpspmx1.setYhcje(-jyspmx.getJshj().doubleValue());
            kpspmxService.save(kpspmx1);
            kpspmx.setKhcje(0d);
            kpspmx.setYhcje(kpspmx.getSpje()+kpspmx.getSpse());
            kpspmxService.save(kpspmx);
        }
        return kpls2;
    }
    /**
     *
     *
     * @param gsdm,OrderData
     * @return Map
     */
    private Map dealOperation04(String gsdm, String OrderData) {
        OMElement root = null;
        Map inputMap = new HashMap();
        try {
            root = XmlMapUtils.xml2OMElement(OrderData);
            inputMap = XmlMapUtils.xml2Map(root, "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return inputMap;
    }
}
