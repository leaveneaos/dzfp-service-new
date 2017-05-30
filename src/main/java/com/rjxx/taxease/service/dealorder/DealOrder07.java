package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.service.result.Result07;
import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.axiom.om.OMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
                /*InvoiceResponse response = skService.voidInvoice(kpls.getKplsh().intValue());
                if ("0000".equals(response.getReturnCode())) {

                    result07.setInvDate(sdf.format(kpls.getKprq()));
                    result07.setCancelDate(sdf.format(new Date()));
                    result07.setInvCode(kpls.getFpdm());
                    result07.setInvNo(kpls.getFphm());
                    } else {
                        result07.setReturnCode("9999");
                        result07.setReturnMessage(response.getReturnMessage());
                    }*/
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
