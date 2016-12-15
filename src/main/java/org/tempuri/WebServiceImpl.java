package org.tempuri;

import com.rjxx.comm.utils.StringUtils;
import com.rjxx.comm.utils.XmlUtils;
import com.rjxx.kpt.domains.*;
import com.rjxx.kpt.service.*;
import com.rjxx.kpt.socket.ServerHandler;
import com.rjxx.kpt.vo.*;
import com.rjxx.kpt.ws.WebServiceHelper;
import com.rjxx.kpt.xmlbo.business.BusinessGroup;
import com.rjxx.kpt.xmlbo.business.BusinessInput;
import com.rjxx.kpt.xmlbo.invoicedata.InvoiceDataInput;
import com.rjxx.kpt.xmlbo.invoicedata.InvoiceDataRoot;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.jws.WebParam;
import java.util.*;

/**
 * Created by Administrator on 2016/10/17.
 */
@javax.jws.WebService(targetNamespace = "http://tempuri.org/", name = "Service", serviceName = "Service")
public class WebServiceImpl implements WebService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PARAMS_SEPERATE = "####";

    @Value("${IsCheckSysInvNo:true}")
    private boolean IsCheckSysInvNo;

    double param = 0.06;    //参数：金额和税额校验时允许的误差

    @Autowired
    private InvoicedetaildataService invoicedetaildataService;

    public WebServiceImpl() {
    }

    public String CallService(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "SysInvNo") String SysInvNo, @WebParam(name = "InvoiceList") String InvoiceList, @WebParam(name = "InvoiceSplit") String InvoiceSplit, @WebParam(name = "InvoiceConsolidate") String InvoiceConsolidate, @WebParam(name = "InvoiceData") String InvoiceData) throws Exception {
        logger.debug(CLIENTNO + "," + TaxMachineIP + "," + SysInvNo + "," + InvoiceList + "," + InvoiceSplit + "," + InvoiceConsolidate + "," + InvoiceData);
        String returnMessage = "";
        String swiftNumber = "";
        String skpkl;
        String keypwd;
        String taxCardNO;
        int opCode;
        String fplxdm;
        String kplx;
        String zflx;
        String kpjh = "";
        List<String> returnMessageList = new ArrayList<String>();
        InvoiceDataRoot invoiceDataObject = null;
        InvoiceDataInput input = null;
        try {
            invoiceDataObject = XmlUtils.convertXmlStrToObject(InvoiceDataRoot.class, InvoiceData);
            input = invoiceDataObject.getInputList().get(0);
        } catch (Exception e) {
            logger.error("", e);
            returnMessage = "(服务端)解析Xml出错：" + e.getMessage();
            //封装成输出形式的字符串
            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, "", SysInvNo, returnMessage);
            return returnMessage;
        }
        try {
            if (IsCheckSysInvNo) {
                try {
                    String creditNoteInv = "0";
                    String cancelInvType = "0";

                    if (StringUtils.isNotBlank(input.getCreditNoteInv())) {
                        creditNoteInv = input.getCreditNoteInv();
                    }
                    if (StringUtils.isNotBlank(input.getCancelInvType())) {
                        cancelInvType = input.getCancelInvType();
                    }
                    if (creditNoteInv.equals("0") && cancelInvType.equals("0")) {
                        //开票
                        Invoicedetaildata checkSysInvNo = CheckSysInvNo2(SysInvNo);
                        if (checkSysInvNo != null) {
                            returnMessage = "(服务端)单据号SysInvNo无效：该单据号已成功开具过发票！";
                            //封装成输出形式的字符串
                            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, "", SysInvNo, returnMessage);
                            return returnMessage;
                        }
                    }
                } catch (Exception ex) {
                    logger.error("", ex);
                    returnMessage = "(服务端)读数据库信息校验单据号SysInvNo" + ex.toString();
                    //封装成输出形式的字符串
                    returnMessage = WebServiceHelper.OutputXml(CLIENTNO, "", SysInvNo, returnMessage);
                    return returnMessage;
                }
            }
            InvClienttaxcard getClientTaxCardInfo = null;

            //读数据库信息获得skpkl
            try {
                ///读数据库信息获得skpkl keypwd
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                kpjh = getClientTaxCardInfo.getKpjh();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                logger.debug("GetClientTaxCardInfo Exception " + ex.getMessage());
                returnMessage = "(服务端)读取数据库失败！" + ex.toString();
                //封装成输出形式的字符串
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, "", SysInvNo, returnMessage);
                return returnMessage;
            }
//            if (InvoiceData != null) {
//                returnMessage = WebServiceHelper.LoadXMLDoc(InvoiceData);
//            }
//            if (!returnMessage.equals(""))
//            {
//                //如果校验有误直接返回
//                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
//                returnMessageList.add(returnMessage);
//            }
            try {
                if (StringUtils.isBlank(input.getSwiftNumber())) {
                    swiftNumber = UUID.randomUUID().toString();
                    input.setSwiftNumber(swiftNumber);
                } else {
                    swiftNumber = input.getSwiftNumber();
                }
                WriteInputInfoToDB(taxCardNO, TaxMachineIP, CLIENTNO, SysInvNo, InvoiceList, InvoiceSplit, InvoiceConsolidate, InvoiceData);
            } catch (Exception ex) {
                logger.error("WriteInputInfoToDB Exception:", ex);
                returnMessage = "(服务端)业务数据写入数据库失败！" + ex.toString();
                //封装成输出形式的字符串
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                return returnMessage;
            }
            ///校验输入参数
            returnMessage = WebServiceHelper.CheckInput(CLIENTNO, TaxMachineIP, SysInvNo, InvoiceList, InvoiceSplit, InvoiceConsolidate, InvoiceData);
            ///校验xml字符串
            returnMessage += WebServiceHelper.CheckXml(InvoiceList, InvoiceSplit, input, param);
            if (!returnMessage.equals(""))//如果校验有误直接返回
            {
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                return returnMessage;
            }
            BusinessInput businessInput = WebServiceHelper.convertInvoiceDataInputToBusinessInput(input);
            GetPara(businessInput);//针对组件接口升级，调整开票Xml
            //logger.debug("333333333333" + InvoiceData);
            List<BusinessInput> InvoiceDataList = WebServiceHelper.ModifyXml(InvoiceList, InvoiceSplit, businessInput, skpkl, keypwd, taxCardNO, TaxMachineIP, kpjh);
            for (int i = 0; i < InvoiceDataList.size(); i++) {
                String temp_InvoiceData = InvoiceDataList.get(i);
                Map map = WebServiceHelper.GetPara(temp_InvoiceData);
                opCode = (Integer) map.get("opCode");
                fplxdm = (String) map.get("fplxdm");
                try {
                    ///税控盘校验服务
                    returnMessage = WebServiceHelper.CheckXmlFromSKP(InvoiceSplit, temp_InvoiceData, skpkl, fplxdm, opCode, taxCardNO, param, keypwd);
                    if (!returnMessage.equals("")) {
                        //如果校验有误直接返回
                        //logger.debug("789" + returnMessage);
                        returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                        break;
                    } else {
                        ///更新InvoiceDetailData表中的信息
                        try {
                            Document xmlDoc = DocumentHelper.parseText(temp_InvoiceData);
                            Element node = (Element) xmlDoc.selectSingleNode("business/body/input");
                            kplx = node.selectSingleNode("kplx").getText();
                            zflx = node.selectSingleNode("zflx").getText();
                            if (kplx.equals("0") && zflx.equals("-1") || kplx.equals("1") && zflx.equals("-1"))//开票和红冲时
                            {
                                WriteInvoiceDetailDataToDB(swiftNumber, SysInvNo, temp_InvoiceData);
                            }
                        } catch (Exception ex) {
                            logger.debug("WriteInvoiceDetailDataToDB Exception " + ex.getMessage());
                            returnMessage = "(服务端)InvoiceDetailData数据写入数据库失败！" + ex.toString();
                            //封装成输出形式的字符串
                            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                            returnMessageList.add(returnMessage);
                            break;
                        }
                        String temp_InvoiceData1 = WebServiceHelper.ModifyXmlNew(temp_InvoiceData);
                        logger.debug("5555555555555555" + temp_InvoiceData1);
                        ///调本地开票服务
                        String params = CLIENTNO + PARAMS_SEPERATE + swiftNumber + PARAMS_SEPERATE + SysInvNo + PARAMS_SEPERATE + temp_InvoiceData1 + PARAMS_SEPERATE + opCode + PARAMS_SEPERATE + fplxdm + PARAMS_SEPERATE + skpkl + PARAMS_SEPERATE + InvoiceList + PARAMS_SEPERATE + keypwd;
                        returnMessage = ServerHandler.sendMessage(taxCardNO, "SKPRead", params);

                        ///返回本地服务返回的结果
                        try {
//                            Document xmlDoc = DocumentHelper.parseText(returnMessage);
                            ///更新InvoiceDetailData表中的信息
                            UpdateInvoiceDetailData_KP(temp_InvoiceData, returnMessage, swiftNumber);


                            returnMessageList.add(returnMessage);
                        } catch (Exception ex) {
                            logger.debug(ex.getMessage());
                            //封装成输出形式的字符串
                            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                            returnMessageList.add(returnMessage);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    logger.error("", ex);
                    returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                    returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                    returnMessageList.add(returnMessage);
                }
            }
            if (returnMessageList.size() > 0) {
                StringBuilder str = new StringBuilder();
                str.append("<?xml version='1.0' encoding='gbk'?>")
                        .append("<InvoiceData>")
                        .append("<body>")
                        .append("<output>")
                        .append("<CLIENTNO></CLIENTNO>")
                        .append("<SwiftNumber></SwiftNumber>")
                        .append("<SysInvNo></SysInvNo>")
                        .append("<InvCode></InvCode>")
                        .append("<InvNo></InvNo>")
                        .append("<InvDate></InvDate>")
                        .append("<CancelDate/>")
                        .append("<OperateFlag></OperateFlag>")
                        .append("<PrintFlag></PrintFlag>")
                        .append("<returnmsg></returnmsg>")
                        .append("</output>")
                        .append("</body>")
                        .append("</InvoiceData>");
                Document main_doc = DocumentHelper.parseText(str.toString());
                List<String> SwiftNumber = new ArrayList<String>();
                List<String> InvCode = new ArrayList<String>();
                List<String> InvNo = new ArrayList<String>();
                List<String> InvDate = new ArrayList<String>();
                List<String> CancelDate = new ArrayList<String>();
                List<String> OperateFlag = new ArrayList<String>();
                List<String> PrintFlag = new ArrayList<String>();
                List<String> returnmsg = new ArrayList<String>();
                for (String x : returnMessageList) {
                    Document doc = DocumentHelper.parseText(x);
                    SwiftNumber.add(doc.selectSingleNode("//SwiftNumber").getText());
                    Element InvCodeNode = (Element) doc.selectSingleNode("//InvCode");
                    InvCode.add(InvCodeNode == null ? "" : InvCodeNode.getText());
                    Element InvNoNode = (Element) doc.selectSingleNode("//InvNo");
                    InvNo.add(InvNoNode == null ? "" : InvNoNode.getText());

                    Element InvDateNode = (Element) doc.selectSingleNode("//InvDate");
                    if (InvDateNode == null || InvDateNode.getText().equals("")) {
                        InvDate.add(DateFormatUtils.format(new Date(), "yyyyMMdd"));
                    } else {
                        InvDate.add(InvDateNode.getText());
                    }

                    //InvDate.Add(doc.selectSingleNode("//InvDate").InnerText);
                    if (doc.selectSingleNode("//CancelDate") != null && !doc.selectSingleNode("//CancelDate").getText().equals("")) {
                        CancelDate.add(doc.selectSingleNode("//CancelDate").getText());
                    }
                    if (doc.selectSingleNode("//OperateFlag").getText().equals("00000000")) {
                        OperateFlag.add("0");
                    } else {
                        OperateFlag.add(doc.selectSingleNode("//OperateFlag").getText());
                    }
                    if (doc.selectSingleNode("//PrintFlag").getText().equals("00000000")) {
                        PrintFlag.add("0");
                    } else {
                        PrintFlag.add(doc.selectSingleNode("//PrintFlag").getText());
                    }
                    returnmsg.add(doc.selectSingleNode("//returnmsg").getText());
                }
                main_doc.selectSingleNode("//CLIENTNO").setText(CLIENTNO);
                main_doc.selectSingleNode("//SysInvNo").setText(SysInvNo);
                main_doc.selectSingleNode("//InvCode").setText(StringUtils.join(InvCode, ","));
                main_doc.selectSingleNode("//InvNo").setText(StringUtils.join(InvNo, ","));
                main_doc.selectSingleNode("//InvDate").setText(StringUtils.join(InvDate, ","));

                main_doc.selectSingleNode("//CancelDate").setText(StringUtils.join(CancelDate, ","));

                main_doc.selectSingleNode("//OperateFlag").setText(StringUtils.join(OperateFlag, ","));
                main_doc.selectSingleNode("//PrintFlag").setText(StringUtils.join(PrintFlag, ","));
                main_doc.selectSingleNode("//returnmsg").setText(StringUtils.join(returnmsg, ","));
                main_doc.selectSingleNode("//SwiftNumber").setText(StringUtils.join(SwiftNumber, ","));
                returnMessage = main_doc.asXML();
            }
            return returnMessage;
        } catch (Exception ex) {
            //第一个try的catch
            logger.error("", ex);
            returnMessage = "(服务端)" + ex.toString();
            try {
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                return returnMessage;
            } catch (Exception e) {
                logger.error("", e);
            }
        } finally {
            //操作结果写入数据库
            try {
                WriteLogToDB(TaxMachineIP, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }

    private void UpdateInvoiceDetailData_KP(String InvoiceData, String returnMessage, String swiftNumber) throws Exception {
        logger.debug(returnMessage);
        Document rmDoc = createDocument(returnMessage);
        Node rmNode = rmDoc.getRootElement().selectSingleNode("body/output");
        Document xmlDoc = createDocument(InvoiceData);
        Element node = (Element) xmlDoc.selectSingleNode("business/body/input");
        String kplx = node.selectSingleNode("kplx").getText();
        String zflx = node.selectSingleNode("zflx").getText();
        if (kplx.equals("0") && zflx.equals("-1") || kplx.equals("1") && zflx.equals("-1"))//开票 红冲
        {
            String fpdm = selectNodeText(rmNode, "InvCode");
            String fphm = selectNodeText(rmNode, "InvNo");
            String kprq = selectNodeText(rmNode, "InvDate");
            if (kprq == null || kprq.equals("")) {
                kprq = DateFormatUtils.format(new Date(), "yyyyMMdd");
            }
            String dyrq = "";
            if ("0".equals(selectNodeText(rmNode, "PrintFlag"))) {
                dyrq = kprq;
            }

            List<Element> oldxhList = xmlDoc.selectNodes("//group[@oldxh]");
            List<String> paList = new ArrayList<String>();
            if (oldxhList.size() != 0) {
                for (Element node_oldxh : oldxhList) {
                    paList.add(node_oldxh.attributeValue("oldxh"));
                }
            } else {
                oldxhList = xmlDoc.selectNodes("//group[@xh]");
                for (Element node_oldxh : oldxhList) {
                    paList.add(node_oldxh.attributeValue("xh"));
                }
            }
            Map params = new HashMap();
            params.put("swiftnumber", swiftNumber);
            if (!paList.isEmpty()) {
                params.put("noList", paList);
            }
            params.put("invcode", "");
            params.put("invno", "");
            List<Invoicedetaildata> invoicedetaildataList = invoicedetaildataService.findAllByParams(params);
            for (Invoicedetaildata invoicedetaildata : invoicedetaildataList) {
                invoicedetaildata.setInvcode(fpdm);
                invoicedetaildata.setInvno(fphm);
                invoicedetaildata.setInvdate(kprq);
                invoicedetaildata.setPrintdate(dyrq);
            }
            invoicedetaildataService.save(invoicedetaildataList);


            if (kplx.equals("1") && zflx.equals("-1")) {
                //红冲
                String hcrq = selectNodeText(rmNode, "InvDate");
                fpdm = node.selectSingleNode("yfpdm").getText();
                fphm = node.selectSingleNode("yfphm").getText();
                params.clear();
                params.put("invcode", fpdm);
                params.put("invno", fphm);
                invoicedetaildataList = invoicedetaildataService.findAllByParams(params);
                for (Invoicedetaildata invoicedetaildata : invoicedetaildataList) {
                    invoicedetaildata.setReddasheddate(hcrq);
                }
                invoicedetaildataService.save(invoicedetaildataList);
            }
        } else if (kplx.equals("0") && zflx.equals("1")) {
            //已开发票作废
            String zfrq = selectNodeText(rmNode, "CancelDate");
            String fpdm = selectNodeText(rmNode, "InvCode");
            String fphm = selectNodeText(rmNode, "InvNo");
            Map params = new HashMap();
            params.put("invcode", fpdm);
            params.put("invno", fphm);
            String operateFlag = selectNodeText(rmNode, "OperateFlag");
            if (operateFlag.equals("0") || operateFlag.equals("00000000")) {
                List<Invoicedetaildata> invoicedetaildataList = invoicedetaildataService.findAllByParams(params);
                for (Invoicedetaildata invoicedetaildata : invoicedetaildataList) {
                    invoicedetaildata.setCanceldate(zfrq);
                }
                invoicedetaildataService.save(invoicedetaildataList);
            }
        }

    }

    public String KPCallService(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "SysInvNo") String SysInvNo, @WebParam(name = "InvoiceList") String InvoiceList, @WebParam(name = "InvoiceSplit") String InvoiceSplit, @WebParam(name = "InvoiceConsolidate") String InvoiceConsolidate, @WebParam(name = "InvoiceData") String InvoiceData) {
        logger.debug(CLIENTNO + "," + TaxMachineIP + "," + SysInvNo + "," + InvoiceList + "," + InvoiceSplit + "," + InvoiceConsolidate + "," + InvoiceData);
        String returnMessage = "";
        String swiftNumber = "";
        String kpjh = "";
        String skpkl;
        String keypwd;
        String taxCardNO;
        int opCode;
        String fplxdm;
        String kplx;
        String zflx;
        List<String> returnMessageList = new ArrayList<String>();
        try {
            if (IsCheckSysInvNo) {
                //读数据库信息校验单据号SysInvNo
                try {
                    Document xmlDoc = DocumentHelper.parseText(InvoiceData);
                    Element node = (Element) xmlDoc.selectSingleNode("InvoiceData/body/input");
                    Element creditNoteInv_node = (Element) node.selectSingleNode("CreditNoteInv");
                    Element cancelInvType_node = (Element) node.selectSingleNode("CancelInvType");

                    String creditNoteInv = "0";
                    String cancelInvType = "0";

                    if (creditNoteInv_node != null) {
                        creditNoteInv = creditNoteInv_node.getText();
                    }
                    if (cancelInvType_node != null) {
                        cancelInvType = cancelInvType_node.getText();
                    }
                    if (creditNoteInv.equals("0") && cancelInvType.equals("0"))//开票
                    {
                        Invoicedetaildata checkSysInvNo = CheckSysInvNo2(SysInvNo);
                        if (checkSysInvNo != null) {
                            returnMessage = "(服务端)单据号SysInvNo无效：该单据号已成功开具过发票！";
                            //封装成输出形式的字符串
                            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                            return returnMessage;
                        }
                    }
                } catch (Exception ex) {
                    returnMessage = "(服务端)读数据库信息校验单据号SysInvNo" + ex.toString();
                    //封装成输出形式的字符串
                    returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                    return returnMessage;
                }
            }
            InvClienttaxcard getClientTaxCardInfo = null;
            //读数据库信息获得skpkl
            try {
                ///读数据库信息获得skpkl keypwd
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                kpjh = getClientTaxCardInfo.getKpjh();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                logger.debug("GetClientTaxCardInfo Exception " + ex.getMessage());
                returnMessage = "(服务端)读取数据库失败！" + ex.toString();
                //封装成输出形式的字符串
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;

            if (InvoiceData != null) {
                returnMessage = WebServiceHelper.LoadXMLDoc(InvoiceData);
            }
            if (!returnMessage.equals(""))//如果校验有误直接返回
            {
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                returnMessageList.add(returnMessage);
            }


            // 将输入参数写入数据库
            try {
                Map<String, String> map = genSwiftNumber(InvoiceData);
                swiftNumber = map.get("swiftNumber");
                InvoiceData = map.get("invoiceData");
                WriteInputInfoToDB(taxCardNO, TaxMachineIP, CLIENTNO, SysInvNo, InvoiceList, InvoiceSplit, InvoiceConsolidate, InvoiceData);
            } catch (Exception ex) {
                logger.error("WriteInputInfoToDB Exception " + ex.getMessage(), ex);
                returnMessage = "(服务端)业务数据写入数据库失败！" + ex.toString();
                //封装成输出形式的字符串
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                return returnMessage;
            }
            ///校验输入参数
            returnMessage = WebServiceHelper.CheckInput(CLIENTNO, TaxMachineIP, SysInvNo, InvoiceList, InvoiceSplit, InvoiceConsolidate, InvoiceData);
            ///校验xml字符串
            returnMessage += WebServiceHelper.CheckXml(InvoiceList, InvoiceSplit, InvoiceData, param);
            if (!returnMessage.equals(""))//如果校验有误直接返回
            {
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                return returnMessage;
            }
            ///形成需要格式的Xml
            InvoiceData = WebServiceHelper.ConvertXml(InvoiceData);
            InvoiceData = GetPara(InvoiceData);//针对组件接口升级，调整开票Xml
            ///TODO

            List<String> InvoiceDataList = WebServiceHelper.ModifyXml(InvoiceList, InvoiceSplit, InvoiceData, skpkl, keypwd, taxCardNO, TaxMachineIP, kpjh);

            for (int i = 0; i < InvoiceDataList.size(); i++) {
                String temp_InvoiceData = InvoiceDataList.get(i);
                //logger.debug("22222222222222222222222222222" + temp_InvoiceData);

                returnMessage = WebServiceHelper.CheckNewXml(temp_InvoiceData);
                if (!returnMessage.equals("")) {
                    returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                    break;
                }
                ///得到opCode（1.开票 2.作废） fplxdm的值
                Map map = WebServiceHelper.GetPara(temp_InvoiceData);
                opCode = (Integer) map.get("opCode");
                fplxdm = (String) map.get("fplxdm");
                try {
                    ///税控盘校验服务
                    returnMessage = WebServiceHelper.CheckXmlFromSKP(InvoiceSplit, temp_InvoiceData, skpkl, fplxdm, opCode, taxCardNO, param, keypwd);
                    logger.debug("22222" + returnMessage);
                    if (!returnMessage.equals(""))//如果校验有误直接返回
                    {
                        returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                        break;
                    } else {
                        ///更新InvoiceDetailData表中的信息
                        try {
                            Document xmlDoc = DocumentHelper.parseText(temp_InvoiceData);
                            Element node = (Element) xmlDoc.selectSingleNode("business/body/input");
                            kplx = node.selectSingleNode("kplx").getText();
                            zflx = node.selectSingleNode("zflx").getText();
                            if (kplx.equals("0") && zflx.equals("-1") || kplx.equals("1") && zflx.equals("-1"))//开票和红冲时
                            {
                                WriteInvoiceDetailDataToDB(swiftNumber, SysInvNo, temp_InvoiceData);
                            }
                        } catch (Exception ex) {
                            logger.error("WriteInvoiceDetailDataToDB Exception " + ex.getMessage(), ex);
                            returnMessage = "(服务端)InvoiceDetailData数据写入数据库失败！" + ex.toString();
                            //封装成输出形式的字符串
                            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                            returnMessageList.add(returnMessage);
                            break;
                        }

                        String temp_InvoiceData1 = WebServiceHelper.ModifyXmlNew(temp_InvoiceData);
                        ///调本地开票服务
                        String params = CLIENTNO + PARAMS_SEPERATE + swiftNumber + PARAMS_SEPERATE
                                + SysInvNo + PARAMS_SEPERATE + temp_InvoiceData1 + PARAMS_SEPERATE
                                + opCode + PARAMS_SEPERATE + fplxdm + PARAMS_SEPERATE + skpkl + PARAMS_SEPERATE
                                + InvoiceList + PARAMS_SEPERATE + keypwd;
                        returnMessage = ServerHandler.sendMessage(taxCardNO, "KPRead", params);

                        ///返回本地服务返回的结果
                        try {
                            Document xmlDoc = DocumentHelper.parseText(returnMessage);
                            ///更新InvoiceDetailData表中的信息
                            UpdateInvoiceDetailData_OnlyKP(temp_InvoiceData, returnMessage, swiftNumber);
                            returnMessageList.add(returnMessage);
                        } catch (Exception ex) {
                            logger.debug(ex.getMessage());
                            //封装成输出形式的字符串
                            returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                            returnMessageList.add(returnMessage);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                    returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
                    returnMessageList.add(returnMessage);
                }
            }
            if (returnMessageList.size() > 0) {
                StringBuilder str = new StringBuilder();
                str.append("<?xml version='1.0' encoding='gbk'?>")
                        .append("<InvoiceData>")
                        .append("<body>")
                        .append("<output>")
                        .append("<CLIENTNO></CLIENTNO>")
                        .append("<SwiftNumber></SwiftNumber>")
                        .append("<SysInvNo></SysInvNo>")
                        .append("<InvCode></InvCode>")
                        .append("<InvNo></InvNo>")
                        .append("<InvDate></InvDate>")
                        .append("<CancelDate/>")
                        .append("<OperateFlag></OperateFlag>")
                        .append("<PrintFlag></PrintFlag>")
                        .append("<returnmsg></returnmsg>")
                        .append("</output>")
                        .append("</body>")
                        .append("</InvoiceData>");
                Document main_doc = DocumentHelper.parseText(str.toString());
                List<String> SwiftNumber = new ArrayList<String>();
                List<String> InvCode = new ArrayList<String>();
                List<String> InvNo = new ArrayList<String>();
                List<String> InvDate = new ArrayList<String>();
                List<String> CancelDate = new ArrayList<String>();
                List<String> OperateFlag = new ArrayList<String>();
                List<String> returnmsg = new ArrayList<String>();
                for (String x : returnMessageList) {
                    Document doc = DocumentHelper.parseText(x);
                    SwiftNumber.add(doc.selectSingleNode("//SwiftNumber").getText());
                    Element InvCodeNode = (Element) doc.selectSingleNode("//InvCode");
                    InvCode.add(InvCodeNode == null ? "" : InvCodeNode.getText());

                    Element InvNoNode = (Element) doc.selectSingleNode("//InvNo");
                    InvNo.add(InvNoNode == null ? "" : InvNoNode.getText());

                    Element InvDateNode = (Element) doc.selectSingleNode("//InvDate");
                    if (InvDateNode == null || InvDateNode.getText().equals("")) {
                        InvDate.add(DateFormatUtils.format(new Date(), "yyyyMMdd"));
                    } else {
                        InvDate.add(InvDateNode.getText());
                    }

                    //InvDate.Add(doc.selectSingleNode("//InvDate").InnerText);
                    if (doc.selectSingleNode("//CancelDate") != null && !doc.selectSingleNode("//CancelDate").getText().equals("")) {
                        CancelDate.add(doc.selectSingleNode("//CancelDate").getText());
                    }
                    if (doc.selectSingleNode("//OperateFlag").getText().equals("00000000")) {
                        OperateFlag.add("0");
                    } else {
                        OperateFlag.add(doc.selectSingleNode("//OperateFlag").getText());
                    }
                    returnmsg.add(doc.selectSingleNode("//returnmsg").getText());
                }
                main_doc.selectSingleNode("//CLIENTNO").setText(CLIENTNO);
                main_doc.selectSingleNode("//SysInvNo").setText(SysInvNo);
                main_doc.selectSingleNode("//InvCode").setText(StringUtils.join(InvCode, ","));
                main_doc.selectSingleNode("//InvNo").setText(StringUtils.join(InvNo, ","));
                main_doc.selectSingleNode("//InvDate").setText(StringUtils.join(InvDate, ","));
                main_doc.selectSingleNode("//CancelDate").setText(StringUtils.join(CancelDate, ","));
                main_doc.selectSingleNode("//OperateFlag").setText(StringUtils.join(OperateFlag, ","));
                main_doc.selectSingleNode("//returnmsg").setText(StringUtils.join(returnmsg, ","));
                main_doc.selectSingleNode("//SwiftNumber").setText(StringUtils.join(SwiftNumber, ","));
                returnMessage = main_doc.asXML();
            }
            return returnMessage;
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        } catch (Exception ex)//第一个try的catch
        {
            logger.error(ex.getMessage(), ex);
            returnMessage = "(服务端)" + ex.toString();
            try {
                returnMessage = WebServiceHelper.OutputXml(CLIENTNO, swiftNumber, SysInvNo, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }

            return returnMessage;
        } finally {
            //操作结果写入数据库
            try {
                WriteLogToDB(TaxMachineIP, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    public String RePrint(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "InvoiceData") String InvoiceData) {
        //String UserHostAddress = TaxMachineIP;
        //object[] attrs = { new UrlAttribute("tcp://" + UserHostAddress + ":15000/KPlocalService") };
        String returnMessage = "";
        String swiftNumber = "";
        String fpdm = "";
        String fphm = "";
        String skpkl;
        String keypwd;
        String taxCardNO;
        try {
            Document doc = DocumentHelper.parseText(InvoiceData);
            swiftNumber = doc.selectSingleNode("InvoiceData/body/input/SwiftNumber").getText();
            fpdm = doc.selectSingleNode("InvoiceData/body/input/InvCode").getText();
            fphm = doc.selectSingleNode("InvoiceData/body/input/InvNo").getText();
            if (StringUtils.isBlank(swiftNumber)) {
                swiftNumber = UUID.randomUUID().toString();
                doc.selectSingleNode("InvoiceData/body/input/SwiftNumber").setText(swiftNumber);
                InvoiceData = doc.asXML();
            }
            //校验输入参数
            returnMessage = WebServiceHelper.CheckInput_RePrint(CLIENTNO, TaxMachineIP, InvoiceData);
            //校验xml
            returnMessage += WebServiceHelper.CheckXml_RePrint(InvoiceData, swiftNumber, fpdm, fphm);
            if (!returnMessage.equals(""))//如果校验有误直接返回
            {
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, "", "", "1", returnMessage);
                return returnMessage;
            }


            InvClienttaxcard getClientTaxCardInfo = null;
            try {
                ///读数据库信息
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.toString();
                //封装成输出形式的字符串
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, "", "", "1", returnMessage);
                return returnMessage;
            }


            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }

            ///形成需要格式的Xml
            try {
                InvoiceData = WebServiceHelper.ConvertXml_RePrint(InvoiceData);
                InvoiceData = WebServiceHelper.ModifyXml_RePrint(InvoiceData, skpkl, taxCardNO, taxCardNO, keypwd);
            } catch (Exception ex) {
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, "", "", "1", returnMessage);
                return returnMessage;
            }
            ///调本地开票服务
            try {
                returnMessage = ServerHandler.sendMessage(taxCardNO, "RePrint", InvoiceData);
            } catch (Exception ex) {
                logger.error("", ex);
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, "", "", "1", returnMessage);
                return returnMessage;
            }
            ///返回本地服务返回的结果
            try {
                Document xmlDoc = DocumentHelper.parseText(returnMessage);
                Element node = (Element) xmlDoc.selectSingleNode("business/body/output");
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, fpdm, fphm, node.selectSingleNode("returncode").getText(), node.selectSingleNode("returnmsg").getText());
                ///更新InvoiceDetailData表中的信息
                UpdateInvoiceDetailData_RePrint(returnMessage);
                return returnMessage;
            } catch (Exception e) {
                //封装成输出形式的字符串
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, "", "", "1", returnMessage);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.toString();
            //封装成输出形式的字符串
            try {
                returnMessage = WebServiceHelper.OutputXml_RePrint(CLIENTNO, swiftNumber, "", "", "1", returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
            return returnMessage;
        } finally {
            //操作结果写入数据库
            try {
                WriteLog_RePrintToDB(TaxMachineIP, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    public String GetCodeAndNo(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm) {
        String returnMessage = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        String skpkl = "";
        String taxCardNO = "";
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.toString();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            ///本地查询当前发票代码和当前发票号码
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);
                String params = skpkl + PARAMS_SEPERATE + fplxdm;
                String gpxxOutput = ServerHandler.sendMessage(taxCardNO, "GetGPXXCX", params);
                Document document = DocumentHelper.parseText(gpxxOutput);
                Node output = document.getRootElement().selectSingleNode("body/output");
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, selectNodeText(output, "dqfpdm"), selectNodeText(output, "dqfphm"), CLIENTNO, "", document);
                return returnMessage;
            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                logger.error(returnMessage, ex);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.toString();
            //封装成输出形式的字符串
            try {
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            } catch (Exception e) {
                logger.error("", e);
            }
            return returnMessage;
        } finally {
            //操作结果写入数据库
            try {
                WriteLog_GetCodeAndNoToDB(TaxMachineIP, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    public String GetFPStock(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm) {
        String returnMessage = "";
        String skpkl = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        String taxCardNO = "";
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.toString();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            ///本地查询当前发票代码和当前发票号码
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);

                String params = skpkl + PARAMS_SEPERATE + fplxdm;
                String gpxxOutput = ServerHandler.sendMessage(taxCardNO, "GetGPXXCX", params);
                returnMessage = WebServiceHelper.ModifyOutputXml_GetFPStock(fplxdm, TaxMachineIP, CLIENTNO, "", gpxxOutput);
                logger.debug(returnMessage);
                return returnMessage;

            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.toString();
            //封装成输出形式的字符串
            try {
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            } catch (Exception e) {
                logger.error("", e);
            }
            return returnMessage;
        } finally {
            //操作结果写入数据库
            try {
                WriteLog_GetCodeAndNoToDB(TaxMachineIP, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    public String GetFp(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm, @WebParam(name = "cxfs") String cxfs, @WebParam(name = "cxtj") String cxtj, @WebParam(name = "cxlx") String cxlx) {
        String returnMessage = "";
        String skpkl = "";
        String taxCardNO = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.toString();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            ///本地查询当前发票代码和当前发票号码
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);
                String params = skpkl + PARAMS_SEPERATE + fplxdm + PARAMS_SEPERATE + cxfs + PARAMS_SEPERATE + cxtj + PARAMS_SEPERATE + cxlx;
                String fpcxOutput = ServerHandler.sendMessage(taxCardNO, "GetFPXX", params);
                // returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, gpxxOutput.body.output.Dqfpdm, gpxxOutput.body.output.Dqfphm, CLIENTNO, "", gpxxOutput);
                returnMessage = WebServiceHelper.Modify_FPXX(fpcxOutput, fplxdm, TaxMachineIP, CLIENTNO);
                return returnMessage;
            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                //returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                logger.error(returnMessage, ex);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.toString();
            //封装成输出形式的字符串
            //returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            return returnMessage;
        } finally {
            //操作结果写入数据库
            try {
                // WriteLog_GetCodeAndNoToDB(TaxMachineIP, returnMessage);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    public String InvoiceUpload(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm) {

        String returnMessage = "";
        String skpkl = "";
        String keypwd = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        String taxCardNO = "";
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.getMessage();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            ///本地查询当前发票代码和当前发票号码
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);
                returnMessage = ServerHandler.sendMessage(taxCardNO, "SJCB", fplxdm);
                returnMessage = WebServiceHelper.Modify_SJCBandFX(fplxdm, TaxMachineIP, CLIENTNO, returnMessage);
                return returnMessage;
            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.getMessage();
            //封装成输出形式的字符串
            //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            return returnMessage;
        }
    }

    public String InvoiceWrieBack(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm) {
        String returnMessage = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        String skpkl = "";
        String keypwd = "";
        String taxCardNO = "";
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.getMessage();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);
                returnMessage = ServerHandler.sendMessage(taxCardNO, "InvoiceWriteback", fplxdm);
                returnMessage = WebServiceHelper.Modify_SJCBandFX(fplxdm, TaxMachineIP, CLIENTNO, returnMessage);
                return returnMessage;
            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.getMessage();
            //封装成输出形式的字符串
            //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            return returnMessage;
        }
    }

    public String InvoiceUnUploadCount(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm) {
        String returnMessage = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        String taxCardNO = "";
        String skpkl = "";
        String keypwd = "";
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.getMessage();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            ///本地查询当前发票代码和当前发票号码
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);
                String wscfpOutput = ServerHandler.sendMessage(taxCardNO, "GetWSCFP", fplxdm + "####" + skpkl + "####" + keypwd);
                returnMessage = WebServiceHelper.Modify_Wcfp(fplxdm, CLIENTNO, wscfpOutput);
                return returnMessage;
            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.getMessage();
            //封装成输出形式的字符串
            //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            return returnMessage;
        }
    }

    public String FpUpload(@WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "fplxdm") String fplxdm) {
        String returnMessage = "";
        InvClienttaxcard getClientTaxCardInfo = null;
        String taxCardNO = null;
        String skpkl = null;
        String keypwd = null;
        try {
            ///读数据库信息
            try {
                getClientTaxCardInfo = GetClientTaxCardInfo2(CLIENTNO);
                taxCardNO = getClientTaxCardInfo.getTaxcardno();
                Inv_TaxCardInfo getTaxCardInfo = GetTaxCardInfo2(taxCardNO);
                skpkl = getTaxCardInfo.getTaxcardPwd();
                keypwd = getTaxCardInfo.getKeyPwd();
            } catch (Exception ex) {
                returnMessage = "(服务端)读取数据库失败！" + ex.getMessage();
                returnMessage = WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
            if (TaxMachineIP == null || TaxMachineIP.length() == 0) {
                TaxMachineIP = getClientTaxCardInfo.getClientip();
            }
            String UserHostAddress = TaxMachineIP;
            try {
                ///0 - 专票  1 - 普票
                fplxdm = WebServiceHelper.GetFplx(fplxdm);
                String wscfpOutput = ServerHandler.sendMessage(taxCardNO, "GetWSCFP", fplxdm + "####" + skpkl + "####" + keypwd);
                returnMessage = WebServiceHelper.Modify_Wcfp(fplxdm, CLIENTNO, wscfpOutput);
                return returnMessage;
            } catch (Exception ex) {
                returnMessage = "(服务端)" + ex.getMessage();
                returnMessage += "\r\n(服务端)请在发票监控窗口选择你要打印的发票类型并启动监听或与系统管理员联系";
                //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
                return returnMessage;
            }
        } catch (Exception ex) {
            returnMessage = "(服务端)" + ex.getMessage();
            //封装成输出形式的字符串
            //returnMessage = WebServiceHelper.WebServiceHelper.ModifyOutputXml_GetCodeAndNo(fplxdm, "", "", CLIENTNO, returnMessage, null);
            return returnMessage;
        }
    }

    public String CheckSysInvNo(@WebParam(name = "_SysInvNo") String _SysInvNo) {
        Map params = new HashMap();
        params.put("sysinvno", _SysInvNo);
        Invoicedetaildata invoicedetaildata = invoicedetaildataService.findOneByParams(params);
        String result = XmlUtils.toXml(invoicedetaildata);
        return result;
    }

    private Invoicedetaildata CheckSysInvNo2(String _SysInvNo) {
        Map params = new HashMap();
        params.put("sysinvno", _SysInvNo);
        Invoicedetaildata invoicedetaildata = invoicedetaildataService.findOneByParams(params);
        return invoicedetaildata;
    }

    @Autowired
    private InvClienttaxcardService invClienttaxcardService;

    public String GetClientTaxCardInfo(@WebParam(name = "_CLIENTNO") String _CLIENTNO) {
        Map params = new HashMap();
        params.put("clientno", _CLIENTNO);
        InvClienttaxcard invClienttaxcard = invClienttaxcardService.findOneByParams(params);
        String result = XmlUtils.toXml(invClienttaxcard);
        return result;
    }

    private InvClienttaxcard GetClientTaxCardInfo2(String _CLIENTNO) {
        Map params = new HashMap();
        params.put("clientno", _CLIENTNO);
        return invClienttaxcardService.findOneByParams(params);
    }

    @Autowired
    private InvTaxcardinfoService invTaxcardinfoService;

    public String GetTaxCardInfo(@WebParam(name = "_TAXCARDNO") String _TAXCARDNO) {
        Map params = new HashMap();
        params.put("taxcardno", _TAXCARDNO);
        Inv_TaxCardInfo inv_taxCardInfo = invTaxcardinfoService.findOneByParams(params);
        String result = XmlUtils.toXml(inv_taxCardInfo);
        return result;
    }

    private Inv_TaxCardInfo GetTaxCardInfo2(String _TAXCARDNO) {
        Map params = new HashMap();
        params.put("taxcardno", _TAXCARDNO);
        return invTaxcardinfoService.findOneByParams(params);
    }

    public String GetTaxCardAndSfdm(@WebParam(name = "_TAXCARDNO") String _TAXCARDNO, @WebParam(name = "_NSRSBH") String _NSRSBH) {
        Map params = new HashMap();
        params.put("taxcardno", _TAXCARDNO);
        params.put("nsrsbh", _NSRSBH);
        Inv_TaxCardAndDfdm inv_taxCardAndDfdm = invTaxcardinfoService.getTaxCardAndSfdm(params);
        String result = XmlUtils.toXml(inv_taxCardAndDfdm);
        return result;
    }

    private Inv_TaxCardAndDfdm GetTaxCardAndSfdm2(String _TAXCARDNO, String _NSRSBH) {
        Map params = new HashMap();
        params.put("taxcardno", _TAXCARDNO);
        params.put("nsrsbh", _NSRSBH);
        return invTaxcardinfoService.getTaxCardAndSfdm(params);
    }

    public boolean SetIP(@WebParam(name = "TAXCARDNO") String TAXCARDNO, @WebParam(name = "CLIENTIP") String CLIENTIP, @WebParam(name = "Kpjh") String Kpjh) {
        Map params = new HashMap();
        params.put("taxcardno", TAXCARDNO);
        params.put("kpjh", Kpjh);
        InvClienttaxcard invClienttaxcard = invClienttaxcardService.findOneByParams(params);
        if (invClienttaxcard == null) {
            return false;
        }
        invClienttaxcard.setKpjh(Kpjh);
        invClienttaxcard.setClientip(CLIENTIP);
        invClienttaxcardService.save(invClienttaxcard);
        return true;
    }

    @Autowired
    private XfService xfService;

    public boolean UpdateT_XF_KPXE(@WebParam(name = "xfsh") String xfsh, @WebParam(name = "zpmaxje") String zpmaxje, @WebParam(name = "ppmaxje") String ppmaxje) {
        Map params = new HashMap();
        params.put("xfsh", xfsh);
        params.put("zpmaxje", zpmaxje);
        params.put("ppmaxje", ppmaxje);
        try {
            xfService.updateXfKpxe(params);
            return true;
        } catch (Exception e) {
//            logger.error("", e);
            return false;
        }
    }

    public String SelectReadTime() {
        Parameters parameters = parametersService.findOne("ReadTime");
        String value = parameters.getParavalue();
        SelectReadTime selectReadTime = new SelectReadTime();
        selectReadTime.setParaValue(value);
        String result = XmlUtils.toXml(selectReadTime);
        return result;
    }

    public boolean UpdateReadTime(@WebParam(name = "readtime") String readtime) {
        String key = "ReadTime";
        Parameters parameters = parametersService.findOne(key);
        if (parameters == null) {
            return false;
        }
        parameters.setParavalue(readtime);
        parametersService.save(parameters);
        return true;
    }

    public boolean WriteFpxx(@WebParam(name = "ClientNo") String ClientNo, @WebParam(name = "Fplxdm") String Fplxdm, @WebParam(name = "Pch") String Pch, @WebParam(name = "strFpxxOutput") String strFpxxOutput) throws Exception {
        boolean isSucess = false;
//        FPCX_Output FpxxOutput = DeserializeFromXml<FPCX_Output>(strFpxxOutput);
//        string fpcount = FpxxOutput.body.output.fpxx.Count;
//        for (int i = 0; i < fpcount.TryToInt();i++ )
//        {
//            string SwiftNumber = Guid.NewGuid().ToString();
//            DataTable dt = new DataTable();
//            Group1_FPCX fpxxgroup = FpxxOutput.body.output.fpxx.group[i];
//            FPXXMainData FPXX_MainData = new FPXXMainData(Pch, SwiftNumber, ClientNo, Fplxdm, fpxxgroup);
//            string strSQL = @"SELECT Fpdm from kpxx_maindata where Fpdm = '"+ fpxxgroup.Fpdm.ToString() +"' and Fphm ='" + fpxxgroup.Fphm.ToString() + "'";
//            //KPInterfaceWS.LogHelper.WriteSQLLog(strSQL);
//            var query = CPQuery.New() + strSQL;
//            //int IsHave = DbHelper.ExecuteNonQuery(query);
//            dt = DbHelper.FillDataTable(query);
//            //KPInterfaceWS.LogHelper.WriteSQLLog("ishave" + dt.Rows.Count.ToString());
//            if (dt .Rows.Count<= 0)
//            {
//                DbHelper.ExecuteNonQuery("InsertFpxxMainData", FPXX_MainData);//插入fpmaindta
//                if (fpxxgroup.Qdbz == "0")//无清单
//                {
//                    string fyxmcount = fpxxgroup.fyxm.Count;
//                    for (int j = 0; j < fyxmcount.TryToInt(); j++)
//                    {
//                        Group2_FPCX fyxmgroup = fpxxgroup.fyxm.group[j];
//                        FPXXFyxmData FPXX_FyxmData = new FPXXFyxmData(Pch,SwiftNumber, fpxxgroup.Fpdm, fpxxgroup.Fphm, fpxxgroup.Qdbz, fyxmgroup);
//                        DbHelper.ExecuteNonQuery("InsertFpxxDetailData", FPXX_FyxmData);
//                        isSucess = true;
//                    }
//                }
//                else
//                {
//                    string qdxmcount = fpxxgroup.qdxm.Count;
//                    for (int j = 0; j < qdxmcount.TryToInt(); j++)
//                    {
//                        Group3_FPCX qdxmgroup = fpxxgroup.qdxm.group[j];
//                        FPXXFyxmData FPXX_FyxmData = new FPXXFyxmData(Pch,SwiftNumber,fpxxgroup.Fpdm, fpxxgroup.Fphm, fpxxgroup.Qdbz, qdxmgroup);
//                        DbHelper.ExecuteNonQuery("InsertFpxxDetailData", FPXX_FyxmData);
//                        isSucess = true;
//                    }
//                }
//            }
//        }
        return isSucess;
    }

    public boolean SetKL(@WebParam(name = "TAXCARDNO") String TAXCARDNO, @WebParam(name = "skpkl") String skpkl) {
        Map params = new HashMap();
        params.put("taxcardno", TAXCARDNO);
        Inv_TaxCardInfo invTaxcardinfo = invTaxcardinfoService.findOneByParams(params);
        if (invTaxcardinfo == null) {
            return false;
        }
        invTaxcardinfo.setTaxcardPwd(skpkl);
        invTaxcardinfoService.save(invTaxcardinfo);
        return true;
    }

    public boolean SetKeyPwd(@WebParam(name = "TAXCARDNO") String TAXCARDNO, @WebParam(name = "KeyPwd") String KeyPwd) {
        Map params = new HashMap();
        params.put("taxcardno", TAXCARDNO);
        Inv_TaxCardInfo invTaxcardinfo = invTaxcardinfoService.findOneByParams(params);
        if (invTaxcardinfo == null) {
            return false;
        }
        invTaxcardinfo.setKeyPwd(KeyPwd);
        invTaxcardinfoService.save(invTaxcardinfo);
        return true;
    }

    public boolean SetAutoPrint(@WebParam(name = "TAXCARDNO") String TAXCARDNO, @WebParam(name = "AutoPrint") String AutoPrint) {
        Map params = new HashMap();
        params.put("taxcardno", TAXCARDNO);
        InvClienttaxcard invClienttaxcard = invClienttaxcardService.findOneByParams(params);
        if (invClienttaxcard == null) {
            return false;
        }
        invClienttaxcard.setAutoprint(AutoPrint);
        invClienttaxcardService.save(invClienttaxcard);
        return true;
    }

    @Autowired
    private MaindataService maindataService;

    private Maindata GetMainData(String _SysInvNo) {
        Map params = new HashMap();
        params.put("sysinvno", _SysInvNo);
        Maindata maindata = maindataService.findOneByParams(params);
        return maindata;
    }

    @Autowired
    private LogService logService;

    private Log GetLog(String _SysInvNo) {
        Map params = new HashMap();
        params.put("sysinvno", _SysInvNo);
        return logService.getLog(params);
    }

    public String GetData(@WebParam(name = "sysInvNo") String sysInvNo, @WebParam(name = "kpStart") String kpStart, @WebParam(name = "kpEnd") String kpEnd, @WebParam(name = "zfStart") String zfStart, @WebParam(name = "zfEnd") String zfEnd, @WebParam(name = "operateFlag") String operateFlag, @WebParam(name = "printFlag") String printFlag) {
        Map params = new HashMap();
        if (StringUtils.isNotBlank(sysInvNo)) {
            params.put("sysInvNo", sysInvNo);
        }
        if (StringUtils.isNotBlank(kpStart)) {
            params.put("kpStart", kpStart + "000000");
        }
        if (StringUtils.isNotBlank(kpEnd)) {
            params.put("kpEnd", kpEnd + "235959");
        }
        if (StringUtils.isNotBlank(zfStart)) {
            params.put("zfStart", zfStart);
        }
        if (StringUtils.isNotBlank(zfEnd)) {
            params.put("zfEnd", zfEnd);
        }
        if (StringUtils.isNotBlank(operateFlag)) {
            params.put("operateFlag", operateFlag);
        }
        if (StringUtils.isNotBlank(printFlag)) {
            params.put("printFlag", printFlag);
        }
        List<Log> logList = logService.findAllByParams(params);
        for (Log log : logList) {
            if ("0".equals(log.getOperateflag())) {
                log.setOperateflag("成功");
            } else {
                log.setOperateflag("失败");
            }
            if ("0".equals(log.getPrintflag())) {
                log.setPrintflag("成功");
            } else {
                log.setPrintflag("失败");
            }
        }
        LogList logList1 = new LogList();
        logList1.setLostList(logList);
        String result = XmlUtils.toXml(logList1);
        return result;
    }

    public boolean CheckWebService() {
        return true;
    }

    @Autowired
    private DhcpService dhcpService;

    public String CheckDHCPIP() {
        List<Dhcp> list = dhcpService.findAllByParams(new HashMap());
        DHCPIPList dhcpipList = new DHCPIPList();
        dhcpipList.setDhcpList(list);
        String result = XmlUtils.toXml(dhcpipList);
        return result;
    }

    public String Test(@WebParam(name = "x") String x) {
        return "{'HELLO WORD' :" + x + "}";
    }

    /**
     * 操作结果写入数据库(开票)
     *
     * @param log
     */
    private void WriteResultToDB(Log log) {
        logService.save(log);
    }

    @Autowired
    private ErrorlogService errorlogService;

    public boolean InsertErrorLog(@WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "TaxCardNo") String TaxCardNo, @WebParam(name = "OperateType") String OperateType, @WebParam(name = "ErrorMsg") String ErrorMsg, @WebParam(name = "CR_XML") String CR_XML, @WebParam(name = "CC_XML") String CC_XML) {
        Errorlog errorlog = new Errorlog();
        errorlog.setIp(TaxMachineIP);
        errorlog.setOperatetime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        errorlog.setTaxcardno(TaxCardNo);
        errorlog.setOperatetype(OperateType);
        errorlog.setErrormessage(ErrorMsg);
        errorlog.setCrXml(CR_XML);
        errorlog.setCcXml(CC_XML);
        try {
            errorlogService.save(errorlog);
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
        return true;
    }

    /**
     * 操作数据(MainData)写入数据库
     *
     * @param md
     */
    private void WriteMainDataToDB(Maindata md) {
        maindataService.save(md);
    }

    @Autowired
    private DetaildataService detaildataService;

    /**
     * 操作数据(DetailData)写入数据库
     *
     * @param dd
     */
    private void WriteDetailDataToDB(Detaildata dd) {
        detaildataService.save(dd);
    }

    /**
     * 将输入参数写入数据库
     *
     * @param taxCardNO
     * @param TaxMachineIP
     * @param CLIENTNO
     * @param SysInvNo
     * @param InvoiceList
     * @param InvoiceSplit
     * @param InvoiceConsolidate
     * @param InvoiceData
     */
    public void WriteInputInfoToDB(@WebParam(name = "taxCardNO") String taxCardNO, @WebParam(name = "TaxMachineIP") String TaxMachineIP, @WebParam(name = "CLIENTNO") String CLIENTNO, @WebParam(name = "SysInvNo") String SysInvNo, @WebParam(name = "InvoiceList") String InvoiceList, @WebParam(name = "InvoiceSplit") String InvoiceSplit, @WebParam(name = "InvoiceConsolidate") String InvoiceConsolidate, @WebParam(name = "InvoiceData") String InvoiceData) {
        Document xmlDoc = createDocument(InvoiceData);
        Node node = xmlDoc.getRootElement().selectSingleNode("body/input");
        String swiftNumber = "";
        int detailCount = 0;
        if (StringUtils.isNotBlank(InvoiceData)) {
            swiftNumber = selectNodeText(node, "SwiftNumber");
            if (StringUtils.isBlank(swiftNumber)) {
                swiftNumber = UUID.randomUUID().toString();
                node.selectSingleNode("SwiftNumber").setText(swiftNumber);
            }
            Node detailNode = node.selectSingleNode("InvoiceDetail");
            if (detailNode != null) {
                List<Element> groupList = detailNode.selectNodes("group");
                detailCount = groupList.size();
                for (Element group : groupList) {
                    Detaildata detaildata = new Detaildata();
                    detaildata.setTaxmachineip(TaxMachineIP);
                    detaildata.setOperatetime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
                    detaildata.setSwiftnumber(swiftNumber);
                    detaildata.setSysinvno(SysInvNo);
                    detaildata.setNo(group.attributeValue("xh"));
                    detaildata.setProductname(selectNodeText(group, "ProductName"));
                    detaildata.setProductsize(selectNodeText(group, "ProductSize"));
                    detaildata.setProductunit(selectNodeText(group, "ProductUnit"));
                    detaildata.setProductamount(selectNodeText(group, "ProductAmount"));
                    detaildata.setUnitprice(selectNodeText(group, "UnitPrice"));
                    detaildata.setTotalamount(selectNodeText(group, "TotalAmount"));
                    detaildata.setTaxrate(selectNodeText(group, "TaxRate"));
                    detaildata.setTaxamount(selectNodeText(group, "TaxAmount"));
                    detaildataService.save(detaildata);
                }
            }
        }
        Maindata md = new Maindata();
        md.setTaxcardno(taxCardNO);
        md.setTaxmachineip(TaxMachineIP);
        md.setOperatetime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        md.setClientno(CLIENTNO);
        md.setSysinvno(SysInvNo);
        md.setInvoicelist(InvoiceList);
        md.setInvoicesplit(InvoiceSplit);
        md.setInvoiceconsolidate(InvoiceConsolidate);
        md.setSwiftnumber(selectNodeText(node, "SwiftNumber"));
        md.setInvtype(selectNodeText(node, "InvType"));
        md.setCreditnoteinv(selectNodeText(node, "CreditNoteInv"));
        md.setCancelinvtype(selectNodeText(node, "CancelInvType"));
        md.setVendertaxno(selectNodeText(node, "VenderTaxNo"));
        md.setVendername(selectNodeText(node, "VenderName"));
        md.setVenderaddresstel(selectNodeText(node, "VenderAddressTel"));
        md.setVenderbanknameno(selectNodeText(node, "VenderBankNameNo"));
        md.setCustomertaxno(selectNodeText(node, "CustomerTaxNo"));
        md.setCustomername(selectNodeText(node, "CustomerName"));
        md.setCustomeraddresstel(selectNodeText(node, "CustomerAddressTel"));
        md.setCustomerbanknameno(selectNodeText(node, "CustomerBankNameNo"));
        md.setInvoicedetailcount(detailCount + "");
        md.setSumtotalamount(selectNodeText(node, "SumTotalAmount"));
        md.setSumtaxamount(selectNodeText(node, "SumTaxAmount"));
        md.setTotal(selectNodeText(node, "Total"));
        md.setRemark(selectNodeText(node, "Remark"));
        md.setReceiver(selectNodeText(node, "Receiver"));
        md.setChecker(selectNodeText(node, "Checker"));
        md.setIssuer(selectNodeText(node, "Issuer"));
        md.setCanceluser(selectNodeText(node, "CancelUser"));
        md.setMonth(selectNodeText(node, "Month"));
        md.setCnnoticeno(selectNodeText(node, "CNNoticeNo"));
        md.setCndncode(selectNodeText(node, "CNDNCode"));
        md.setCndnno(selectNodeText(node, "CNDNNo"));
        maindataService.save(md);
    }

    /**
     * 将发票明细写入InvoiceDetail表中
     *
     * @param swiftNumber
     * @param SysInvNo
     * @param InvoiceData
     */
    private void WriteInvoiceDetailDataToDB(String swiftNumber, String SysInvNo, String InvoiceData) throws Exception {
        if (InvoiceData != null) {
            Document xmlDoc = createDocument(InvoiceData);
            List<Element> qdxmList = xmlDoc.selectNodes("//group");
            if (qdxmList != null && qdxmList.size() > 0) {
                for (Element group : qdxmList) {
                    Invoicedetaildata invoicedetaildata = new Invoicedetaildata();
                    invoicedetaildata.setSwiftnumber(swiftNumber);
                    invoicedetaildata.setSysinvno(SysInvNo);
                    invoicedetaildata.setInvcode("");
                    invoicedetaildata.setInvno("");
                    invoicedetaildata.setInvdate("");
                    invoicedetaildata.setReddasheddate("");
                    invoicedetaildata.setCanceldate("");
                    invoicedetaildata.setPrintdate("");
                    String oldxh = group.attributeValue("oldxh");
                    if (StringUtils.isBlank(oldxh)) {
                        invoicedetaildata.setNo(group.attributeValue("xh"));
                    } else {
                        invoicedetaildata.setNo(oldxh);
                    }
                    invoicedetaildata.setProductname(selectNodeText(group, "spmc"));
                    invoicedetaildata.setProductsize(selectNodeText(group, "ggxh"));
                    invoicedetaildata.setProductunit(selectNodeText(group, "dw"));
                    invoicedetaildata.setProductamount(selectNodeText(group, "spsl"));
                    invoicedetaildata.setUnitprice(selectNodeText(group, "dj"));
                    invoicedetaildata.setTotalamount(selectNodeText(group, "je"));
                    invoicedetaildata.setTaxrate(selectNodeText(group, "sl"));
                    invoicedetaildata.setTaxamount(selectNodeText(group, "se"));
                    invoicedetaildataService.save(invoicedetaildata);
                }
            }
        }
    }

    /**
     * 操作结果写入数据库(开票)
     *
     * @param TaxMachineIP
     * @param returnMessage
     */

    private void WriteLogToDB(String TaxMachineIP, String returnMessage) throws Exception {
        Document xmlDoc = DocumentHelper.parseText(returnMessage);
        Element root = xmlDoc.getRootElement();
        String operateFlag = selectNodeText(root, "body/output/OperateFlag");
        if (!"0".equals(operateFlag)) {
            operateFlag = "1";
        }
        String printFlag = selectNodeText(root, "body/output/PrintFlag");
        if (!"0".equals(printFlag)) {
            printFlag = "1";
        }
        Log log = new Log();
        log.setTaxmachineip(TaxMachineIP);
        log.setOperatetime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        log.setSwiftnumber(selectNodeText(root, "body/output/SwiftNumber"));
        log.setSysinvno(selectNodeText(root, "body/output/SysInvNo"));
        log.setInvcode(selectNodeText(root, "body/output/InvCode"));
        log.setInvno(selectNodeText(root, "body/output/InvNo"));
        log.setInvdate(selectNodeText(root, "body/output/InvDate"));
        log.setCanceldate(selectNodeText(root, "body/output/CancelDate"));
        log.setOperateflag(operateFlag);
        log.setPrintflag(printFlag);
        log.setReturnmsg(selectNodeText(root, "body/output/returnmsg"));
        logService.save(log);
    }

    /**
     * 获取节点文本
     *
     * @param node
     * @param path
     * @return
     */
    private String selectNodeText(Node node, String path) {
        Node target = node.selectSingleNode(path);
        if (target == null) {
            return null;
        }
        return target.getText();
    }

    private Document createDocument(String xml) {
        try {
            return DocumentHelper.parseText(xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 操作结果写入数据库(查询发票代码发票号码)
     *
     * @param TaxMachineIP
     * @param returnMessage
     */
    private void WriteLog_GetCodeAndNoToDB(String TaxMachineIP, String returnMessage) throws Exception {
        Document xmlDoc = createDocument(returnMessage);
        Element node = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
        Log log = new Log();
        log.setTaxmachineip(TaxMachineIP);
        log.setOperatetime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        log.setSwiftnumber("");
        log.setSysinvno("");
        log.setInvcode(selectNodeText(node, "dqfpdm"));
        log.setInvno(selectNodeText(node, "dqfphm"));
        log.setInvdate("");
        log.setCanceldate("");
        log.setOperateflag(selectNodeText(node, "OperateFlag"));
        log.setPrintflag("");
        log.setReturnmsg(selectNodeText(node, "returnmsg"));
        logService.save(log);
    }

    /**
     * 操作结果写入数据库(重新打印)
     *
     * @param TaxMachineIP
     * @param returnMessage
     * @throws Exception
     */
    private void WriteLog_RePrintToDB(String TaxMachineIP, String returnMessage) throws Exception {
        Document xmlDoc = createDocument(returnMessage);
        Element node = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
        Log log = new Log();
        log.setTaxmachineip(TaxMachineIP);
        log.setOperatetime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        log.setSwiftnumber("");
        log.setSysinvno("");
        log.setInvcode(selectNodeText(node, "InvCode"));
        log.setInvno(selectNodeText(node, "InvNo"));
        log.setInvdate("");
        log.setCanceldate("");
        log.setOperateflag(selectNodeText(node, "OperateFlag"));
        log.setPrintflag("");
        log.setReturnmsg(selectNodeText(node, "returnmsg"));
        logService.save(log);
    }

    /**
     * 根据操作结果更新InvoiceDetail表(重新打印)
     *
     * @param returnMessage
     */
    private void UpdateInvoiceDetailData_RePrint(String returnMessage) throws Exception {
        Document xmlDoc = createDocument(returnMessage);
        Element node = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
        String fpdm = node.selectSingleNode("InvCode").getText();
        String fphm = node.selectSingleNode("InvNo").getText();
        String dyrq = "";
        if (node.selectSingleNode("OperateFlag").getText().equals("0")) {
            dyrq = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        }

        Map params = new HashMap();
        params.put("invcode", fpdm);
        params.put("invno", fphm);
        Invoicedetaildata invoicedetaildata = invoicedetaildataService.findOneByParams(params);
        invoicedetaildata.setPrintdate(dyrq);
        invoicedetaildataService.save(invoicedetaildata);
    }

    /**
     * 根据操作结果更新InvoiceDetail表(开票)
     *
     * @param InvoiceData
     * @param returnMessage
     */
    private void UpdateInvoiceDetailData_OnlyKP(String InvoiceData, String returnMessage, String swiftNumber) throws Exception {
        logger.debug(returnMessage);
        Document xmlDoc = createDocument(InvoiceData);
        Document rmDoc = createDocument(returnMessage);
        Element rmNode = (Element) rmDoc.getRootElement().selectSingleNode("body/output");
        Element node = (Element) xmlDoc.selectSingleNode("business/body/input");
        String kplx = node.selectSingleNode("kplx").getText();
        String zflx = node.selectSingleNode("zflx").getText();
        if (kplx.equals("0") && zflx.equals("-1") || kplx.equals("1") && zflx.equals("-1")) {
            //开票 红冲
            String fpdm = selectNodeText(rmNode, "InvCode");
            String fphm = selectNodeText(rmNode, "InvNo");
            String kprq = selectNodeText(rmNode, "InvDate");
            if (StringUtils.isBlank(kprq)) {
                kprq = DateFormatUtils.format(new Date(), "yyyyMMdd");
            }
            String dyrq = "";

            List<Element> oldxhList = xmlDoc.selectNodes("//group[@oldxh]");
            List<String> paList = new ArrayList<String>();
            if (oldxhList != null && !oldxhList.isEmpty()) {
                for (Element node_oldxh : oldxhList) {
                    paList.add(node_oldxh.attributeValue("oldxh"));
                }
            } else {
                oldxhList = xmlDoc.selectNodes("//group[@xh]");
                for (Element node_oldxh : oldxhList) {
                    paList.add(node_oldxh.attributeValue("xh"));
                }
            }
            Map params = new HashMap();
            params.put("swiftnumber", swiftNumber);
            if (!paList.isEmpty()) {
                params.put("noList", paList);
            }
            params.put("invcode", "");
            params.put("invno", "");
            List<Invoicedetaildata> invoicedetaildataList = invoicedetaildataService.findAllByParams(params);
            for (Invoicedetaildata invoicedetaildata : invoicedetaildataList) {
                invoicedetaildata.setInvcode(fpdm);
                invoicedetaildata.setInvno(fphm);
                invoicedetaildata.setInvdate(kprq);
                invoicedetaildata.setPrintdate(dyrq);
            }
            invoicedetaildataService.save(invoicedetaildataList);

            if (kplx.equals("1") && zflx.equals("-1")) {
                //红冲
                String hcrq = selectNodeText(rmNode, "InvDate");
                fpdm = node.selectSingleNode("yfpdm").getText();
                fphm = node.selectSingleNode("yfphm").getText();
                params = new HashMap();
                params.put("invcode", fpdm);
                params.put("invno", fphm);
                invoicedetaildataList = invoicedetaildataService.findAllByParams(params);
                for (Invoicedetaildata invoicedetaildata : invoicedetaildataList) {
                    invoicedetaildata.setReddasheddate(hcrq);
                }
                invoicedetaildataService.save(invoicedetaildataList);
            }
        } else if (kplx.equals("0") && zflx.equals("1")) {
            //已开发票作废
            String zfrq = selectNodeText(rmNode, "CancelDate");
            String fpdm = selectNodeText(rmNode, "InvCode");
            String fphm = selectNodeText(rmNode, "InvNo");
            String operateFlag = selectNodeText(rmNode, "OperateFlag");
            if ("0".equals(operateFlag) || "00000000".equals(operateFlag)) {
                Map params = new HashMap();
                params.put("invcode", fpdm);
                params.put("invno", fphm);
                List<Invoicedetaildata> invoicedetaildataList = invoicedetaildataService.findAllByParams(params);
                for (Invoicedetaildata invoicedetaildata : invoicedetaildataList) {
                    invoicedetaildata.setCanceldate(zfrq);
                }
                invoicedetaildataService.save(invoicedetaildataList);
            }
        }
    }

    @Autowired
    private SpbmService spbmService;

    /**
     * 查询是否存在该商品代码
     *
     * @param _SPBM
     * @return
     */
    public String CheckSPBM(@WebParam(name = "_SPBM") String _SPBM) {
        Map params = new HashMap();
        params.put("spbm", _SPBM);
        params.put("zzsslNotEqual", "");
        Spbm spbm = spbmService.findOneByParams(params);
        CheckSPBM checkSPBM = new CheckSPBM();
        if (spbm == null) {
            return XmlUtils.toXml(checkSPBM);
        }
        checkSPBM.setSPBM(spbm.getSpbm());
        return XmlUtils.toXml(checkSPBM);
    }

    @Autowired
    private SpdzService spdzService;

    /**
     * 插入客户商品信息
     *
     * @param SPMC
     * @param SPBM
     * @param BBH
     * @param MHBZ
     * @return
     */
    public boolean InsertSPDZ(@WebParam(name = "SPMC") String SPMC, @WebParam(name = "SPBM") String SPBM, @WebParam(name = "BBH") String BBH, @WebParam(name = "MHBZ") String MHBZ) {
        Spdz spdz = new Spdz();
        spdz.setSpmc(SPMC);
        spdz.setSpbm(SPBM);
        spdz.setBbh(BBH);
        spdz.setMhbz(MHBZ);
        try {
            spdzService.save(spdz);
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
        return true;
    }

    public String CheckData(@WebParam(name = "spbm") String spbm, @WebParam(name = "spmc") String spmc) {
        Map params = new HashMap();
        if (StringUtils.isNotBlank(spbm)) {
            params.put("likeSpbm", "%" + spbm + "%");
        }
        if (StringUtils.isNotBlank(spmc)) {
            params.put("likeSpmc", "%" + spmc + "%");
        }
        List<Spdz> spdzList = spdzService.findAllByParams(params);
        for (Spdz spdz : spdzList) {
            if ("True".equals(spdz.getMhbz())) {
                spdz.setMhbz("是");
            } else {
                spdz.setMhbz("否");
            }
        }
        SpdzList spdzList1 = new SpdzList();
        spdzList1.setSpdzList(spdzList);
        String result = XmlUtils.toXml(spdzList1);
        return result;
    }

    @Autowired
    private ParametersService parametersService;

    public String CheckBBH() {
        Map params = new HashMap();
        params.put("parakey", "DEFAULT SPBM VERSION");
        Parameters parameters = parametersService.findOneByParams(params);
        if (parameters == null) {
            return "";
        }
        return parameters.getParavalue();
    }

    /**
     * 删除商品对照表记录
     *
     * @param SPMC
     * @param SPBM
     * @param BBH
     * @param MHBZ
     * @return
     */
    public boolean isDeleteSPDZ(@WebParam(name = "SPMC") String SPMC, @WebParam(name = "SPBM") String SPBM, @WebParam(name = "BBH") String BBH, @WebParam(name = "MHBZ") String MHBZ) {
        Map params = new HashMap();
        params.put("spmc", SPMC);
        params.put("spbm", SPBM);
        params.put("bbh", BBH);
        params.put("mhbz", MHBZ);
        List<Spdz> list = spdzService.findAllByParams(params);
        if (list == null || list.isEmpty()) {
            return false;
        }
        spdzService.deleteAll(list);
        return true;
    }

    public int SelectSPMC(@WebParam(name = "SPMC") String SPMC, @WebParam(name = "BBH") String BBH) {
        Map params = new HashMap();
        params.put("spmc", SPMC);
        params.put("bbh", BBH);
        List list = spdzService.findAllByParams(params);
        return list.size();
    }

    public int SelectID(@WebParam(name = "ID") int ID) {
        Spdz spdz = spdzService.findOne(ID);
        if (spdz == null) {
            return 0;
        }
        return 1;
    }

    public String SelectFromID(@WebParam(name = "ID") int ID) {
        Spdz spdz = spdzService.findOne(ID);
        SpdzList spdzList = new SpdzList();
        if (spdz != null) {
            List<Spdz> list = new ArrayList<>();
            list.add(spdz);
            spdzList.setSpdzList(list);
        }
        String result = XmlUtils.toXml(spdzList);
        return result;
    }

    public String SelectDefSPBM() {
        Map params = new HashMap();
        params.put("parakey", "USER DEFAULT SPBM");
        List<Parameters> list = parametersService.findAllByParams(params);
        ParametersList parametersList = new ParametersList();
        parametersList.setList(list);
        String result = XmlUtils.toXml(parametersList);
        return result;
    }

    public boolean UpdateSPDZ(@WebParam(name = "ID") int ID, @WebParam(name = "SPMC") String SPMC, @WebParam(name = "SPBM") String SPBM, @WebParam(name = "BBH") String BBH, @WebParam(name = "MHBZ") String MHBZ) {
        Spdz spdz = spdzService.findOne(ID);
        if (spdz == null) {
            return false;
        }
        spdz.setSpmc(SPMC);
        spdz.setSpbm(SPBM);
        spdz.setBbh(BBH);
        spdz.setMhbz(MHBZ);
        spdzService.save(spdz);
        return true;
    }

    public boolean UpdateParameterSpbm(@WebParam(name = "SPBM") String SPBM, @WebParam(name = "YXBZ") String YXBZ) {
        int intYxbz;
        if (YXBZ.equals("False")) {
            intYxbz = 0;
        } else {
            intYxbz = 1;
        }
        Map params = new HashMap();
        params.put("parakey", "USER DEFAULT SPBM");
        Parameters parameters = parametersService.findOneByParams(params);
        if (parameters == null) {
            return false;
        }
        parameters.setEffectivemark(intYxbz);
        parameters.setParavalue(SPBM);
        parametersService.save(parameters);
        return true;
    }

    public int spdzdatatable(@WebParam(name = "SPMC") String SPMC, @WebParam(name = "SPBM") String SPBM, @WebParam(name = "BBH") String BBH, @WebParam(name = "MHBZ") String MHBZ) {
        Map params = new HashMap();
        params.put("spbm", SPBM);
        params.put("spmc", SPMC);
        params.put("bbh", BBH);
        params.put("mhbz", MHBZ);
        List list = spdzService.findAllByParams(params);
        return list.size();
    }

    private void addFirstElement(List<Node> nodeList, Node newNode) {
        nodeList.add(0, newNode);
    }

    private void addBeforeElement(List<Node> nodeList, Node newNode, Node beforeNode) {
        if (beforeNode == null) {
            nodeList.add(newNode);
        }
        int index = 0;
        for (Node node : nodeList) {
            if (node == beforeNode) {
                break;
            }
            index++;
        }
        nodeList.add(index, newNode);
    }

    private void addAfterElement(List<Node> nodeList, Node newNode, Node afterNode) {
        if (afterNode == null) {
            nodeList.add(newNode);
        }
        int index = 1;
        for (Node node : nodeList) {
            if (node == afterNode) {
                break;
            }
            index++;
        }
        nodeList.add(index, newNode);
    }

    private void GetPara(BusinessInput businessInput) throws Exception {
        ///1 空白发票作废 2 已开发票作废
        String zflx = businessInput.getZflx();
        ///判断是否有<bmbbbh>编码表版本号</bmbbbh>、<hsslbs>含税税率标识</hsslbs>
        ///没有则增加
        ///
        ///只在正常开票的时候增加以下节点
        String bbh = businessInput.getBmbbbh();//编码表版本号
        if (!zflx.equals("1") && !zflx.equals("2")) {
            if (StringUtils.isBlank(bbh)) {
                bbh = CheckDefBBH();
                businessInput.setBmbbbh(bbh);
            }
            String hsslbs = businessInput.getHsslbs();//含税税率标识
            if (StringUtils.isBlank(hsslbs)) {
                hsslbs = CheckDefHsslbs();
                businessInput.setHsslbs(hsslbs);
            }
        }

        if (zflx.equals("0")) {
            //正常开票
            List<BusinessGroup> groupList = businessInput.getGroupList();
            for (BusinessGroup businessGroup : groupList) {
                String hsslbs = businessInput.getHsslbs();
                //差额征收
                if (hsslbs.equals("2") && StringUtils.isBlank(businessGroup.getKcje())) {
                    businessGroup.setKcje("0");
                }

                String spbm = businessGroup.getSpbm();
                if (StringUtils.isBlank(spbm)) {
                    //商品编码节点不存在
                    List<Parameters> parametersList = CheckDefSpbmYxbz();
                    if (parametersList != null && !parametersList.isEmpty()) {
                        if ("1".equals(parametersList.get(0).getEffectivemark().toString())) {
                            //设置了默认商品编码有效
                            businessGroup.setSpbm(parametersList.get(0).getParavalue());
                        } else {
                            spbm = GetSPBM(businessGroup.getSpmc(), businessInput.getBmbbbh());
                            businessGroup.setSpbm(spbm);
                        }
                    }
                } else {
                    businessGroup.setSpbm(getZZSSL(spbm));
                }

                if (StringUtils.isBlank(businessGroup.getYhzcbs())) {
                    //优惠政策标识
                    businessGroup.setYhzcbs(CheckDefYhzcbs());
                }
                if (StringUtils.isBlank("slbs")) {
                    //税率标识
                    businessGroup.setSlbs(CheckDefSlbs());
                }
                if (StringUtils.isBlank("zzstsgl")) {
                    //增值税特殊管理/优惠政策名称
                    if ("1".equals(businessGroup.getYhzcbs())) {
                        //使用优惠政策的时候
                        businessGroup.setZzstsgl(getyhzcmc(businessGroup.getSpbm(), businessInput.getBmbbbh()));
                    }
                }
            }
        }
    }


    private String getyhzcmc(String ProductCode, String BBH) {
        Map params = new HashMap();
        params.put("spbm", ProductCode);
        params.put("bbh", BBH);
        Spbm spbm = spbmService.findOneByParams(params);
        if (spbm == null) {
            return "0";
        }
        if (StringUtils.isBlank(spbm.getZzstsgl())) {
            return "1";
        }
        return spbm.getZzstsgl();
    }


    private String getZZSSL(String ProductCode) {
        Map params = new HashMap();
        params.put("spbm", ProductCode);
        Spbm spbm = spbmService.findOneByParams(params);
        if (spbm == null) {
            return "0";
        }
        if (StringUtils.isBlank(spbm.getZzssl())) {
            return "1";
        }
        return spbm.getSpbm();
    }

    private String CheckDefBBH() {
        Map params = new HashMap();
        params.put("parakey", "DEFAULT SPBM VERSION");
        Parameters parameters = parametersService.findOneByParams(params);
        if (parameters == null) {
            return "";
        }
        return parameters.getParavalue();
    }

    private String CheckDefHsslbs() {
        Map params = new HashMap();
        params.put("parakey", "DEFAULT TAXATION MODE");
        Parameters parameters = parametersService.findOneByParams(params);
        if (parameters == null) {
            return "";
        }
        return parameters.getParavalue();
    }

    private List<Parameters> CheckDefSpbmYxbz() {
        Map params = new HashMap();
        params.put("parakey", "USER DEFAULT SPBM");
        List<Parameters> list = parametersService.findAllByParams(params);
        return list;
    }

    private String CheckDefYhzcbs() {
        Map params = new HashMap();
        params.put("parakey", "DEFAULT TAX INCENTIVES MARKING");
        Parameters parameters = parametersService.findOneByParams(params);
        if (parameters == null) {
            return "";
        }
        return parameters.getParavalue();
    }


    private String CheckDefSlbs() {
        Map params = new HashMap();
        params.put("parakey", "DEFAULT TAXMARK");
        Parameters parameters = parametersService.findOneByParams(params);
        if (parameters == null) {
            return "";
        }
        return parameters.getParavalue();
    }


    private String GetSPBM(String SPMC, String BBH) {
        Map params = new HashMap();
        params.put("spmc", SPMC);
        params.put("bbh", BBH);
        params.put("spmcList", "%" + SPMC + "%");
        List<Spdz> list = spdzService.getSpbm(params);
        if (list != null && !list.isEmpty()) {
            return list.get(0).getSpbm();
        }
        return "";
    }

    public String InvFromSysInvNo(@WebParam(name = "_SysInvNo") String _SysInvNo) {
        StringBuilder outputXml = new StringBuilder();
        outputXml.append("<?xml version=\"1.0\" encoding=\"gbk\"?>")
                .append("<InvoiceData>")
                .append("<body>")
                .append("<output>")
                .append("<SysInvNo></SysInvNo>")
                .append("<SwiftNumber></SwiftNumber>")
                .append("<InvCode></InvCode>")
                .append("<InvNo></InvNo>")
                .append("</output>")
                .append("</body>")
                .append("</InvoiceData>");
        Map params = new HashMap();
        params.put("sysinvno", _SysInvNo);
        params.put("invdateNotEqual", "");
        params.put("reddasheddate", "");
        params.put("canceldate", "");
        Invoicedetaildata invoicedetaildata = invoicedetaildataService.findOneByParams(params);
        try {
            Document xmlDoc = createDocument(outputXml.toString());
            if (invoicedetaildata != null) {
                //修改节点的值
                Element n = (Element) xmlDoc.selectSingleNode("InvoiceData");
                n = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output/SysInvNo");
                n.setText(_SysInvNo);
                n = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output/SwiftNumber");
                n.setText(invoicedetaildata.getSwiftnumber());
                n = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output/InvCode");
                n.setText(invoicedetaildata.getInvcode());
                n = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output/InvNo");
                n.setText(invoicedetaildata.getInvno());
            }
            return xmlDoc.asXML();
        } catch (Exception ex) {
            return ex.getMessage().toString();
        }

    }

    public String getParaMeter(@WebParam(name = "_ParaKey") String _ParaKey) {
        Parameters parameters = parametersService.findOne(_ParaKey);
        if (parameters == null) {
            parameters = new Parameters();
        }
        String result = XmlUtils.toXml(parameters);
        return result;
    }

}
