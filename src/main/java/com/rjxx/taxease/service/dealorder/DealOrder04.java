package com.rjxx.taxease.service.dealorder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxease.service.result.Result04;

import com.rjxx.taxease.utils.XmlMapUtils;

import com.rjxx.taxeasy.bizcomm.utils.DiscountDealUtil;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;

import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.KplsVO5;
import com.rjxx.taxeasy.vo.Kpspmxvo;
import com.rjxx.time.TimeUtil;

import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    @Autowired
    private JyxxsqService jyxxsqService;
    @Autowired
    private JymxsqService jymxsqService;
    @Autowired
    private FpclService fpclService;
    @Autowired
    private GsxxService gsxxService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public String execute(String gsdm, String orderData, String Operation) {

            Map inputMap = dealOperation04(gsdm, orderData);
            logger.info("++++++++数据++++++++++"+orderData);
            Double TotalAmount = Double.parseDouble(String.valueOf(inputMap.get("TotalAmount")));//价税合计
            String CNDNCode = String.valueOf(inputMap.get("CNDNCode"));//原发票代码
            String CNDNNo = String.valueOf(inputMap.get("CNDNNo"));//原发票号码
            String InvType = String.valueOf(inputMap.get("InvType"));//发票种类
            String SerialNumber=null;
            String ServiceType=null;
            String OrderNumber=null;
            String CNNoticeNo=null;
            if(gsdm.equals("Family")){
                String ExtractCode = String.valueOf(inputMap.get("ExtractCode"));//发票种类
                ServiceType="1";//全家写死是1
                SerialNumber = ExtractCode;//全家tqm作为交易流水号
                OrderNumber=ExtractCode;
            }else{
                String clientNO = String.valueOf(inputMap.get("ClientNO"));//开票点编号
                ServiceType = String.valueOf(inputMap.get("ServiceType"));//发票业务类型
                SerialNumber = String.valueOf(inputMap.get("SerialNumber"));//序列号
                OrderNumber=String.valueOf(inputMap.get("OrderNumber"));//订单号
                CNNoticeNo = String.valueOf(inputMap.get("CNNoticeNo"));//专票红字通知单号
            }
            Result04 result04 = new Result04();
        try {
            Kpls parms = new Kpls();
            parms.setFpdm(CNDNCode);
            parms.setFphm(CNDNNo);
            parms.setGsdm(gsdm);
            Kpls kpls = kplsService.findByfphm(parms);//查询原开票流水
            Map params = new HashMap();
            params.put("kpdid", kpls.getSkpid());
            params.put("gsdm", gsdm);
            Skp skp = skpservice.findOneByParams(params);
            int xfid = skp.getXfid();
            int kpdid = skp.getId();
            Cszb cszb = cszbservice.getSpbmbbh(gsdm, xfid, kpdid, "sftbkp");
            String sftbkp = cszb.getCsz();
            if(SerialNumber.equals("")){
                result04.setReturnCode("9999");
                result04.setReturnMessage("交易流水号不能为空！");
                return XmlJaxbUtils.toXml(result04);
            }
            Kpls kplsstr=new Kpls();
            kplsstr.setGsdm(gsdm);
            kplsstr.setJylsh(SerialNumber);
            kplsstr.setHzyfpdm(CNDNCode);
            kplsstr.setHzyfphm(CNDNNo);
            Kpls kpls3=kplsService.findByhzfphm(kplsstr);
            if(kpls3!=null){
                    result04.setReturnCode("9999");
                    result04.setReturnMessage("交易流水号、发票代码、发票号码必须唯一！");
                    return XmlJaxbUtils.toXml(result04);
            }
            /*if (!InvType.equals("12")) {
                result04.setReturnCode("9999");
                result04.setReturnMessage("该接口目前只支持电子发票红冲！");
                return XmlJaxbUtils.toXml(result04);
            }*/
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
            if(kpls==null){
                result04.setReturnCode("9999");
                result04.setReturnMessage("没有该笔数据！");
                return XmlJaxbUtils.toXml(result04);
            }
            if (TotalAmount != -kpls.getJshj()) {
                result04.setReturnCode("9999");
                result04.setReturnMessage("价税合计与原开票价税合计不符！");
                return XmlJaxbUtils.toXml(result04);
            }else if(kpls.getFpztdm().equals("02")){
                result04.setReturnCode("9999");
                result04.setReturnMessage("该笔发票已红冲！不能重复红冲！");
                return XmlJaxbUtils.toXml(result04);
            }
            Map map = new HashMap();
            map.put("kplsh", kpls.getKplsh());
            List<Kpspmx> kpspmxList = kpspmxService.findMxList(map);
            Integer djh = kpls.getDjh();
            Map param4 = new HashMap<>();
            param4.put("djh", djh);
            Jyls jyls = jylsService.findJylsByDjh(param4);
            Map resultMap=this.Savejyxxsq(kpls.getKplsh(),SerialNumber,jyls);
            Jyxxsq jyxxsq=(Jyxxsq) resultMap.get("jyxxsq");
            String ddh = jyls.getDdh(); // 查询原交易流水得ddh
            Map kplsMap = save(ddh,kpls, kpspmxList, sftbkp,SerialNumber,CNNoticeNo,OrderNumber,jyxxsq);
            Kpls kpls2=(Kpls)kplsMap.get("kpls2");
            List<Kpspmx> kpspmxList2=(List)kplsMap.get("kpspmxList2");
            Cszb cszb2 = cszbservice.getSpbmbbh(gsdm, Integer.valueOf(xfid), kpdid, "kpfs");
            String kpfs=cszb2.getCsz();
            if (sftbkp.equals("是")) {   //是否同步开票
                if(kpfs.equals("03")){
                    skService.SkServerKP(kpls2.getKplsh().intValue());
                    result04.setReturnCode("0000");
                    result04.setReturnMessage("红冲成功！");
                }else if(kpfs.equals("1")){
                    kpls2.setFpztdm("14");
                    kplsService.save(kpls2);
                    InvoiceResponse response = skService.callService(kpls2.getKplsh().intValue());
                    if ("0000".equals(response.getReturnCode())) {
                        result04.setReturnCode("0000");
                        result04.setReturnMessage("红冲成功！");
                    } else {
                        result04.setReturnCode("9999");
                        result04.setReturnMessage(response.getReturnMessage());
                    }
                }
            } else {
                kpls2.setFpztdm("14");
                kplsService.save(kpls2);
                skService.callService(kpls2.getKplsh());
                result04.setReturnCode("0000");
                result04.setReturnMessage("红冲请求已接受！");
            }
            return XmlJaxbUtils.toXml(result04);
        }catch (Exception e){
            e.printStackTrace();
            result04.setReturnCode("9999");
            result04.setReturnMessage(e.getMessage());
            return XmlJaxbUtils.toXml(result04);
        }
    }
    private Map Savejyxxsq(Integer kplsh,String tqm,Jyls jyls) throws Exception {
        Map result = new HashMap();
        Kpls kpls = kplsService.findOne(kplsh);
        Jyxxsq jyxxsq = new Jyxxsq();
        String jylsh = "JY" + new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        jyxxsq.setJylsh(tqm);
        jyxxsq.setDdh(tqm);
        jyxxsq.setGflxr(kpls.getGflxr());
        jyxxsq.setGfyb(kpls.getGfyb());
        jyxxsq.setBz(kpls.getBz());
        jyxxsq.setClztdm("00");
        jyxxsq.setHsbz(jyls.getHsbz());
        jyxxsq.setDdrq(new Date());
        jyxxsq.setFpczlxdm("12");
        jyxxsq.setFpzldm(kpls.getFpzldm());
        jyxxsq.setFhr(kpls.getFhr());
        jyxxsq.setGfdh(kpls.getGfdh());
        jyxxsq.setGfdz(kpls.getGfdz());
        jyxxsq.setGfemail(kpls.getGfemail());
        jyxxsq.setGfid(kpls.getGfid());
        jyxxsq.setGfmc(kpls.getGfmc());
        jyxxsq.setGfsh(kpls.getGfsh());
        jyxxsq.setGsdm(kpls.getGsdm());
        jyxxsq.setGfyh(kpls.getGfyh());
        jyxxsq.setGfyhzh(kpls.getGfyhzh());
        jyxxsq.setXfdh(kpls.getXfdh());
        jyxxsq.setXfdz(kpls.getXfdz());
        jyxxsq.setXfid(kpls.getXfid());
        jyxxsq.setXflxr(kpls.getXflxr());
        jyxxsq.setXgsj(new Date());
        jyxxsq.setXfmc(kpls.getXfmc());
        jyxxsq.setXfsh(kpls.getXfsh());
        jyxxsq.setXfyb(kpls.getXfyb());
        jyxxsq.setXfyh(kpls.getXfyh());
        jyxxsq.setXfyhzh(kpls.getXfyhzh());
        jyxxsq.setYxbz("1");
        jyxxsq.setYkpjshj(0d);
        jyxxsq.setLrsj(new Date());
        jyxxsq.setJshj(-kpls.getJshj());
        jyxxsq.setHztzdh(kpls.getHztzdh());
        jyxxsq.setKpddm(kpls.getKpddm());
        jyxxsq.setZtbz("3");
        jyxxsq.setXgsj(kpls.getXgsj());
        jyxxsq.setSkr(kpls.getSkr());
        jyxxsq.setSkpid(kpls.getSkpid());
        jyxxsq.setKpr(kpls.getKpr());
        jyxxsq.setYfpdm(kpls.getFpdm());
        jyxxsq.setYfphm(kpls.getFphm());
        jyxxsq.setLrry(kpls.getLrry());
        jyxxsq.setSfdyqd(kpls.getSfdyqd());
        jyxxsq.setHsbz("1");
        jyxxsq.setTqm(tqm);
        jyxxsqService.save(jyxxsq);
        result.put("jyxxsq", jyxxsq);
        Map parms = new HashMap();
        parms.put("kplsh", kpls.getKplsh());
        List<Kpspmx> kpspmxList = kpspmxService.findMxList(parms);
        List<Jymxsq> jymxsqList = new ArrayList<>();
        for (int i = 0; i < kpspmxList.size(); i++) {
            Kpspmx kpspmx = kpspmxList.get(i);
            Jymxsq jymxsq = new Jymxsq();
            jymxsq.setDdh(jyls.getDdh());
            jymxsq.setLrry(kpspmx.getLrry());
            jymxsq.setFphxz(kpspmx.getFphxz());
            jymxsq.setGsdm(kpspmx.getGsdm());
            jymxsq.setJshj(-(kpspmx.getSpje() + kpspmx.getSpse()));
            jymxsq.setKkjje(-(kpspmx.getSpje() + kpspmx.getSpse()));
            jymxsq.setKce(kpspmx.getKce());
            jymxsq.setKpddm(kpspmx.getKpddm());
            jymxsq.setLslbz(kpspmx.getLslbz());
            try {
                jymxsq.setSps(-kpspmx.getSps());
            }catch (Exception e){
                jymxsq.setSps(null);
            }
            try {
                jymxsq.setSpdj(kpspmx.getSpdj());
            }catch (Exception e){
                jymxsq.setSpdj(null);
            }
            jymxsq.setSpdm(kpspmx.getSpdm());
            jymxsq.setSpse(-kpspmx.getSpse());
            jymxsq.setSpdw(kpspmx.getSpdw());
            jymxsq.setSpje(-kpspmx.getSpje());
            jymxsq.setSpsl(kpspmx.getSpsl());
            jymxsq.setSpggxh(kpspmx.getSpggxh());
            jymxsq.setSpmc(kpspmx.getSpmc());
            jymxsq.setSpmxxh(kpspmx.getSpmxxh());
            jymxsq.setYxbz("1");
            jymxsq.setHsbz(jyls.getHsbz());
            jymxsq.setYhzcbs(kpspmx.getYhzcbs());
            jymxsq.setYhzcmc(kpspmx.getYhzcmc());
            jymxsq.setSqlsh(jyxxsq.getSqlsh());
            jymxsq.setJshj(-(kpspmx.getSpje() + kpspmx.getSpse()));
            jymxsq.setXgsj(new Date());
            jymxsq.setLrsj(new Date());
            jymxsqList.add(jymxsq);
        }
        jymxsqService.save(jymxsqList);
        result.put("jymxsqList",jymxsqList);
        return result;
    }
    public Map save(String ddh,Kpls kpls,List<Kpspmx> kpspmxList,String sftbkp,String SerialNumber,String CNNoticeNo,String OrderNumber,Jyxxsq jyxxsq)throws Exception{
        //保存交易流水
        Jyls jyls1 = new Jyls();
        jyls1.setDdh(ddh);
        String jylsh = SerialNumber;
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
        jyls1.setHztzdh(CNNoticeNo);
        //jyls1.setClztdm("01");
        jyls1.setBz(kpls.getBz());
        jyls1.setSkr(kpls.getSkr());
        jyls1.setKpr(kpls.getKpr());
        jyls1.setFhr(kpls.getFhr());
        jyls1.setSsyf(kpls.getSsyf());
        jyls1.setYfpdm(kpls.getFpdm());
        jyls1.setYfphm(kpls.getFphm());
        jyls1.setHsbz(jyxxsq.getHsbz());
        jyls1.setJshj(-kpls.getJshj());
        jyls1.setYkpjshj(-kpls.getJshj());
        jyls1.setYxbz("1");
        jyls1.setGsdm(kpls.getGsdm());
        jyls1.setLrry(kpls.getLrry());
        jyls1.setLrsj(TimeUtil.getNowDate());
        jyls1.setXgry(kpls.getLrry());
        jyls1.setXgsj(TimeUtil.getNowDate());
        jyls1.setSkpid(kpls.getSkpid());
        jyls1.setSqlsh(jyxxsq.getSqlsh());
        if(jyls1.getGsdm().equals("Family")){
            jyls1.setTqm(jyls1.getJylsh());
        }else{
            //jyls1.setTqm(ddh);
        }
        jylsService.save(jyls1);
        //保存开票流水
        Kpls kpls2 = new Kpls();
        kpls2.setKpddm(kpls.getKpddm());
        kpls2.setDjh(jyls1.getDjh());
        kpls2.setJylsh(jyls1.getJylsh());
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
        /*kpls2.setHkFpdm(jyls1.getYfpdm());
        kpls2.setHkFphm(jyls1.getYfphm());*/
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
        kpls2.setSerialorder(SerialNumber+OrderNumber);
        kplsService.save(kpls2);
        List<Kpspmx> kpspmxList2=new ArrayList<>();
        for(Kpspmx kpspmx:kpspmxList){
            Jyspmx jyspmx = new Jyspmx();
            jyspmx.setDjh(jyls1.getDjh());
            jyspmx.setSpmxxh(kpspmx.getSpmxxh());
            jyspmx.setSpdm(kpspmx.getSpdm());
            jyspmx.setSpmc(kpspmx.getSpmc());
            jyspmx.setSpggxh(kpspmx.getSpggxh());
            jyspmx.setSpdw(kpspmx.getSpdw());
            if(kpspmx.getSps()!=null){
                jyspmx.setSps(-kpspmx.getSps());
            }
            if(kpspmx.getSpdj()!=null){
                jyspmx.setSpdj((kpspmx.getSpdj() == null ? null : kpspmx.getSpdj()));
            }
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
                kpspmx1.setSpdj(jyspmx.getSpdj());
            }
            kpspmx1.setSpdw(jyspmx.getSpdw());
            if (jyspmx.getSps() != null) {
                kpspmx1.setSps(jyspmx.getSps());
            }
            kpspmx1.setSpje(jyspmx.getSpje());
            kpspmx1.setSpsl(jyspmx.getSpsl());
            kpspmx1.setSpse(jyspmx.getSpse());
            kpspmx1.setHcrq(TimeUtil.getNowDate());
            kpspmx1.setLrsj(jyspmx.getLrsj());
            kpspmx1.setLrry(jyspmx.getLrry());
            kpspmx1.setXgsj(jyspmx.getXgsj());
            kpspmx1.setXgry(jyspmx.getXgry());
            kpspmx1.setKhcje(0d);
            kpspmx1.setYhcje(-jyspmx.getJshj());
            kpspmx1.setGsdm(kpls.getGsdm());
            kpspmx1.setYhzcbs(kpspmx.getYhzcbs());
            kpspmx1.setYhzcmc(kpspmx.getYhzcmc());
            kpspmx1.setLslbz(kpspmx.getLslbz());
            kpspmx1.setKpddm(kpls.getKpddm());
            //kpspmxService.save(kpspmx1);
            kpspmxList2.add(kpspmx1);
            kpspmx.setKhcje(0d);
            kpspmx.setYhcje(kpspmx.getSpje()+kpspmx.getSpse());
            kpspmxService.save(kpspmx);
        }
        kpspmxList2= DiscountDealUtil.discountMergeLinesKpspmx(kpspmxList2);
        logger.info("------------"+ JSON.toJSONString(kpspmxList2));
        kpspmxService.save(kpspmxList2);
        Map resultMap=new HashMap();
        resultMap.put("kpls2",kpls2);
        resultMap.put("kpspmxList2",kpspmxList2);
        return resultMap;
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
