package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.service.result.Result07;
import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.axiom.om.OMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xlm on 2017/5/25.
 */
@Service
public class DealOrder07 implements IDealOrder{

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

    @Override
    public String execute(String gsdm, String orderData, String Operation) {
            Result07 result07 = new Result07();
        try {
            Map inputMap = dealOperation07(gsdm, orderData);
            String clientNO = String.valueOf(inputMap.get("ClientNO"));//开票点编号
            String SerialNumber = String.valueOf(inputMap.get("SerialNumber"));//交易流水号
            String InvType = String.valueOf(inputMap.get("InvType"));//      CNDNCode  CNDNNo
            String CancelInvType = String.valueOf(inputMap.get("CancelInvType"));//作废类型
            String VenderTaxNo = String.valueOf(inputMap.get("VenderTaxNo"));//销货单位识别号
            Double SumTotalAmount = Double.parseDouble(String.valueOf(inputMap.get("SumTotalAmount")));//合计金额
            String CancelUser = String.valueOf(inputMap.get("CancelUser"));//作废人
            String CNDNCode = String.valueOf(inputMap.get("CNDNCode"));//作废时对应的原始发票代码
            String CNDNNo = String.valueOf(inputMap.get("CNDNNo"));//作废时对应的原始发票号码
            String OrderNumber=String.valueOf(inputMap.get("OrderNumber"));//订单号


            if (InvType.equals("12")) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("不能作废电子发票!");
                return XmlJaxbUtils.toXml(result07);
            }
            if (SerialNumber.equals("")) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("交易流水号不能为空！");
                return XmlJaxbUtils.toXml(result07);
            }
            if (CNDNCode.equals("") || CNDNNo.equals("")) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("原发票代码、发票号码不允许为空！");
                return XmlJaxbUtils.toXml(result07);
            }
            Kpls kplsstr = new Kpls();
            kplsstr.setGsdm(gsdm);
            kplsstr.setJylsh(SerialNumber);
            Kpls kpls3 = kplsService.findByhzfphm(kplsstr);
            if (kpls3 != null) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("交易流水号必须唯一！");
                return XmlJaxbUtils.toXml(result07);
            }
            Kpls parms = new Kpls();
            parms.setFpdm(CNDNCode);
            parms.setFphm(CNDNNo);
            parms.setGsdm(gsdm);
            Kpls kpls = kplsService.findByfphm(parms);//查询原开票流水
            if (kpls == null) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("没有该笔数据！");
                return XmlJaxbUtils.toXml(result07);
            }
            BigDecimal data1 = new BigDecimal(SumTotalAmount);
            BigDecimal data2 = new BigDecimal(kpls.getJshj());
            if (data1.compareTo(data2)!=0) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("价税合计与原开票价税合计不符！");
                return XmlJaxbUtils.toXml(result07);
            } else if (kpls.getFpztdm().equals("08")) {
                result07.setReturnCode("9999");
                result07.setReturnMessage("该笔发票已作废！不能重复作废！");
                return XmlJaxbUtils.toXml(result07);
            }
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            savejyxxsq(kpls.getKplsh(),SerialNumber,OrderNumber);
            kpls.setSerialorder(SerialNumber+OrderNumber);
            kpls.setFpczlxdm("14");
            kpls.setFpztdm("04");
            kplsService.save(kpls);
            result07.setReturnCode("0000");
            result07.setReturnMessage("作废请求已接受！");
            return XmlJaxbUtils.toXml(result07);
        }catch (Exception e){
            e.printStackTrace();
            result07.setReturnCode("9999");
            result07.setReturnMessage(e.getMessage());
            return XmlJaxbUtils.toXml(result07);
        }
    }

    private void savejyxxsq(Integer kplsh,String SerialNumber,String OrderNumber) {
        Kpls kpls=kplsService.findOne(kplsh);
        Integer djh = kpls.getDjh();
        Map param4 = new HashMap<>();
        param4.put("djh", djh);
        Jyls jyls = jylsService.findJylsByDjh(param4);
        String ddh = jyls.getDdh(); // 查询原交易流水得ddh
        Jyxxsq jyxxsq=new Jyxxsq();
        jyxxsq.setJylsh(SerialNumber);
        jyxxsq.setDdh(ddh);
        jyxxsq.setGflxr(kpls.getGflxr());
        jyxxsq.setGfyb(kpls.getGfyb());
        jyxxsq.setBz(kpls.getBz());
        jyxxsq.setClztdm("00");
        jyxxsq.setDdrq(new Date());
        jyxxsq.setFpczlxdm("14");
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
        jyxxsq.setJshj(kpls.getJshj());
        jyxxsq.setHztzdh(kpls.getHztzdh());
        jyxxsq.setKpddm(kpls.getKpddm());
        jyxxsq.setZtbz("3");
        jyxxsq.setXgsj(kpls.getXgsj());
        jyxxsq.setSkr(kpls.getSkr());
        jyxxsq.setSkpid(kpls.getSkpid());
        jyxxsq.setKpr(kpls.getKpr());
        jyxxsq.setYfpdm(kpls.getFpdm());
        jyxxsq.setYfphm(kpls.getHzyfphm());
        jyxxsq.setLrry(kpls.getLrry());
        jyxxsq.setSfdyqd(kpls.getSfdyqd());
        jyxxsqService.save(jyxxsq);
        Map parms=new HashMap();
        parms.put("kplsh",kpls.getKplsh());
        List<Kpspmx> kpspmxList=kpspmxService.findMxList(parms);
        List<Jymxsq> jymxsqList=new ArrayList<>();
        for(int i=0;i<kpspmxList.size();i++){
            Kpspmx kpspmx=kpspmxList.get(i);
           Jymxsq jymxsq=new Jymxsq();
           jymxsq.setLrry(kpspmx.getLrry());
           jymxsq.setFphxz(kpspmx.getFphxz());
           jymxsq.setGsdm(kpspmx.getGsdm());
           jymxsq.setJshj(kpspmx.getSpje()+kpspmx.getSpse());
           jymxsq.setKkjje(kpspmx.getSpje()+kpspmx.getSpse());
           jymxsq.setKce(kpspmx.getKce());
           jymxsq.setKpddm(kpspmx.getKpddm());
           jymxsq.setLslbz(kpspmx.getLslbz());
           jymxsq.setSps(kpspmx.getSps());
           jymxsq.setSpdj(kpspmx.getSpdj());
           jymxsq.setSpdm(kpspmx.getSpdm());
           jymxsq.setSpse(kpspmx.getSpse());
           jymxsq.setSpdw(kpspmx.getSpdw());
           jymxsq.setSpje(kpspmx.getSpje());
           jymxsq.setSpggxh(kpspmx.getSpggxh());
           jymxsq.setSpmc(kpspmx.getSpmc());
           jymxsq.setSpmxxh(kpspmx.getSpmxxh());
           jymxsq.setYxbz("1");
           jymxsq.setYhzcbs(kpspmx.getYhzcbs());
           jymxsq.setYhzcmc(kpspmx.getYhzcmc());
           jymxsq.setSqlsh(jyxxsq.getSqlsh());
           jymxsq.setJshj(kpspmx.getSpje()+kpspmx.getSpse());
           jymxsqList.add(jymxsq);
        }
        jymxsqService.save(jymxsqList);
    }

    /**
     *
     *
     * @param gsdm,OrderData
     * @return Map
     */
    private Map dealOperation07(String gsdm, String OrderData) {
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
