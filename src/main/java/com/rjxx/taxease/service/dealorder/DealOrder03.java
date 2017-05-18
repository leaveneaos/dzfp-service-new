package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.bizcomm.utils.SaveOrderData;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.service.JyxxsqService;
import com.rjxx.utils.CheckOrderUtil;
import org.apache.axiom.om.OMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017-05-18.
 */
@Service
public class DealOrder03 implements IDealOrder {

    @Autowired
    private CheckOrderUtil checkorderutil;

    @Autowired
    private SaveOrderData saveorderdata;

    @Autowired
    private JyxxsqService jyxxsqService;

    @Override
    public String execute(String gsdm, String orderData, String Operation) {
        String result = "";
        List<Jyxxsq> jyxxsqList = dealOperation03(gsdm, orderData);
        String tmp = checkorderutil.checkBuyer(jyxxsqList, gsdm, Operation);
        if (null == tmp || tmp.equals("")) {
            String tmp1 = saveorderdata.saveBuyerData(jyxxsqList);
            if (null != tmp1 && !tmp1.equals("")) {
                result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp1
                        + "</ReturnMessage> \n</Responese>";
            } else {
                result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>" + "代开票数据上传成功"
                        + "</ReturnMessage> \n</Responese>";
            }
        } else {
            result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp
                    + "</ReturnMessage> \n</Responese>";
        }
        return result;
    }

    /**
     * 处理购方信息xml
     *
     * @param gsdm
     * @param OrderData
     * @return
     */
    private List dealOperation03(String gsdm, String OrderData) {
        OMElement root = null;
        List<Jyxxsq> jyxxsqList = new ArrayList();
        try {
            root = XmlMapUtils.xml2OMElement(OrderData);
            List rootList = XmlMapUtils.xml2List(root, "Row");// 获取xml中的row标签下的数据
            Map jyxxsqMap = null;
            String clientNO = "";// 开票点代码
            String orderNo = "";// 订单号
            String orderTime = "";// 订单日期
            String totalAmount = "";// 计税合计
            String invType = "";// 发票种类
            String identifier = "";// 购方税号
            String name = "";// 购方名称
            String address = "";// 购方地址
            String telephoneNo = "";// 购方电话
            String bank = "";// 购方银行
            String bankAcc = "";// 购方银行账号
            String email = "";// 购方邮箱
            String isSend = "";// 是否发送邮件
            String extractedCode = "";// 提取码
            String recipient = "";// 购方收件人
            String reciAddress = "";// 购方收件人地址
            String zip = "";// 购方邮编
            String remark = "";// 备注
            String taxMark = "";// 含税标志
            SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            for (int i = 0; i < rootList.size(); i++) {
                jyxxsqMap = (Map) rootList.get(i);
                // 分装对应的bean
                clientNO = (String) jyxxsqMap.get("ClientNO");// 开票点代码
                Map tt = new HashMap();
                tt.put("kpddm", clientNO);
                tt.put("gsdm", gsdm);
                Xf xf = jyxxsqService.findXfExistByKpd(tt);
                orderNo = (String) jyxxsqMap.get("OrderNo");// 订单号 必选
                orderTime = (String) jyxxsqMap.get("OrderTime");// 订单日期 必选
                totalAmount = String.valueOf(jyxxsqMap.get("TotalAmount"));// 计税合计
                invType = (String) jyxxsqMap.get("InvType");// 发票种类01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）
                if (invType.equals("01")) {
                    invType = "0";
                } else if (invType.equals("02")) {
                    invType = "1";
                }
                identifier = (String) jyxxsqMap.get("Identifier");// 购方税号
                name = (String) jyxxsqMap.get("Name");// 购方名称
                address = String.valueOf(jyxxsqMap.get("Address"));// 购方地址
                telephoneNo = String.valueOf(jyxxsqMap.get("TelephoneNo"));// 购方电话
                bank = String.valueOf(jyxxsqMap.get("Bank"));// 购方银行
                bankAcc = String.valueOf(jyxxsqMap.get("BankAcc"));// 购方银行账号
                email = String.valueOf(jyxxsqMap.get("Email"));// 购方邮箱
                isSend = String.valueOf(jyxxsqMap.get("IsSend"));// 是否发送邮件
                extractedCode = String.valueOf(jyxxsqMap.get("ExtractedCode"));// 提取码
                recipient = String.valueOf(jyxxsqMap.get("Recipient"));// 购方收件人地址
                reciAddress = String.valueOf(jyxxsqMap.get("ReciAddress"));// 购方收件人地址
                zip = String.valueOf(jyxxsqMap.get("Zip"));// 购方邮编
                remark = String.valueOf(jyxxsqMap.get("Remark"));
                taxMark = String.valueOf(jyxxsqMap.get("TaxMark"));
                Jyxxsq jyxxsq = new Jyxxsq(); // 主表
                jyxxsq.setKpddm(clientNO);
                jyxxsq.setDdh(orderNo);
                jyxxsq.setDdrq(orderTime == null ? new Date() : sim.parse(orderTime));
                jyxxsq.setJshj(Double.valueOf(totalAmount));
                jyxxsq.setFpzldm(invType);
                jyxxsq.setGfsh(identifier);
                jyxxsq.setGfmc(name);
                jyxxsq.setGfdz(address);
                jyxxsq.setGfdh(telephoneNo);
                jyxxsq.setGfyh(bank);
                jyxxsq.setGfyhzh(bankAcc);
                jyxxsq.setGfemail(email);
                jyxxsq.setSffsyj(isSend);
                // jyxxsq.setSsyf(isSend);
                jyxxsq.setTqm(extractedCode);
                jyxxsq.setGfsjr(recipient);
                jyxxsq.setGfsjrdz(reciAddress);
                jyxxsq.setGfyb(zip);
                jyxxsq.setBz(remark == null ? "" : remark);
                jyxxsq.setXfid(xf.getId());
                jyxxsq.setXfsh(xf.getXfsh());
                jyxxsq.setXfmc(xf.getXfmc());
                jyxxsq.setXfdz(xf.getXfdz());
                jyxxsq.setXfdh(xf.getXfdh());
                jyxxsq.setXflxr(xf.getXflxr());
                jyxxsq.setXfyh(xf.getXfyh());
                jyxxsq.setXfyhzh(xf.getXfyhzh());
                jyxxsq.setXfyb(xf.getXfyb());
                jyxxsq.setKpr(xf.getKpr());
                jyxxsq.setSkr(xf.getSkr());
                jyxxsq.setFhr(xf.getFhr());
                jyxxsq.setHsbz(taxMark);
                jyxxsq.setLrsj(new Date());
                jyxxsq.setLrry(xf.getId());
                jyxxsq.setXgry(xf.getId());
                jyxxsq.setXgsj(new Date());
                jyxxsq.setYkpjshj(Double.valueOf("0.00"));
                jyxxsq.setYxbz("1");
                jyxxsq.setGsdm(gsdm);
                jyxxsq.setTqm("");
                jyxxsq.setJylsh("YD" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
                jyxxsqList.add(jyxxsq);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String result = e.getMessage();
            throw new RuntimeException(result);
        }
        return jyxxsqList;
    }

}
