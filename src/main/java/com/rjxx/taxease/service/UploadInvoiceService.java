package com.rjxx.taxease.service;

import com.rjxx.taxease.service.dealorder.DealOrder01;
import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.utils.ResponseUtils;
import com.rjxx.utils.TimeUtil;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/8/18.
 */
@Service
public class UploadInvoiceService {

    @Autowired
    private GsxxService gsxxservice;

    @Autowired
    private YhService yhservice;

    @Autowired
    private SkpService skpservice;

    @Autowired
    private JylsService jylsservice;

    @Autowired
    private KplsService kplsservice;

    @Autowired
    private XfService xfservice;

    @Autowired
    private JyspmxService jyspmxservice;

    @Autowired
    private KpspmxService kpspmxservice;

    @Autowired
    private DealOrder01 dealOrder01;
    @Autowired
    private SpvoService spvoService;

    /**
     * 解析参数
     *
     * @param invoiceData
     * @return
     */
    @Transactional
    public String callService(String AppId, String Sign, String invoiceData) {
        final Map resultMap = new HashMap();
        try {
            Map tempMap = new HashMap();
            tempMap.put("appkey", AppId);
            Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
            if (gsxxBean == null) {
                return ResponseUtils.printFailure("9060:" + AppId + "," + Sign);
            }
            //校验数据是否被篡改过
            String key = gsxxBean.getSecretKey();
            String signSourceData = "data=" + invoiceData + "&key=" + key;
            String newSign = DigestUtils.md5Hex(signSourceData);
            if (!Sign.equals(newSign)) {
                return "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9060:签名不通过</ReturnMessage> \n</Responese>";
                //return result;
            }
            String result = parseResult(invoiceData, gsxxBean);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            String result = ResponseUtils.printFailure("9099:" + e.getMessage());
            throw new RuntimeException(result);
        }

    }

    /**
     * 解析结果
     *
     * @param invoiceData
     * @param
     * @return
     * @throws Exception
     */
    public String parseResult(String invoiceData, Gsxx gsxxBean) {

        String gsdm = gsxxBean.getGsdm();
        Map params1 = new HashMap();
        params1.put("gsdm", gsdm);
        Yh yh = yhservice.findOneByParams(params1);
        int lrry = yh.getId();

        Map params2 = new HashMap();
        params2.put("gsdm", gsdm);
        Skp skp = skpservice.findOneByParams(params2);
        int xfid = skp.getXfid();
        int skpid = skp.getId();
        OMElement root = null;
        try {
            root = xml2OMElement(invoiceData);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map rootMap = xml2Map(root, "InvoiceDetails");
        // 交易流水号
        String serialNumber = (String) rootMap.get("SerialNumber");
        if (root == null || rootMap == null || serialNumber == null) {
            return ResponseUtils.printFailure("9032");
        }
        // 交易流水号判断重复
        // JylsBean old = JylsBean.dao.findFirst("select * from t_jyls where
        // jylsh = '" + serialNumber + "' and gsdm = '" + gsdm + "'");
        Map params3 = new HashMap();
        params3.put("gsdm", gsdm);
        List jylshList = new ArrayList();
        String jylsh = serialNumber;
        jylshList.add(jylsh);
        params3.put("jylshList", jylshList);
        List<Jyls> jylsList = jylsservice.findByMapParams(params3);
        if (jylsList != null && !jylsList.isEmpty()) {
            return ResponseUtils.printFailure("9063:" + serialNumber);
        }
        // 发票类型
        String fpzldm = (String) rootMap.get("InvType");
        if (!"12".equals(fpzldm)) {
            return ResponseUtils.printFailure("9061:不支持" + fpzldm + ",目前只支持12");
        }

        // 发票业务类型
        String serviceType = (String) rootMap.get("ServiceType");
        if (!("0".equals(serviceType) || "1".equals(serviceType) || "4".equals(serviceType))) {
            // 电子发票支持0-蓝票，1-红票，4-换开
            return ResponseUtils.printFailure("9062:不支持" + serviceType + ",目前只支持0,1,4");
        }

        // 红冲或换开，必须要有原发票代码发票号码
        String yfpdm = (String) rootMap.get("CNDNCode");
        if (yfpdm != null && yfpdm.length() != 12) {
            return ResponseUtils.printFailure("原发票代码只能等于12个字符");
        }
        String yfphm = (String) rootMap.get("CNDNNo");
        if (yfphm != null && yfphm.length() != 8) {
            return ResponseUtils.printFailure("原发票号码只能等于8个字符");
        }
        if ("1".equals(serviceType) || "4".equals(serviceType)) {
            if (StringUtils.isBlank(yfpdm) || StringUtils.isBlank(yfphm)) {
                return ResponseUtils.printFailure("9065");
            }
        }
        int djh = 0;
        // 判断是不是红冲或者是换开或蓝字 1表示红冲
        if (!"1".equals(serviceType)) {
            Map InvoiceMain = (Map) rootMap.get("InvoiceMain");
            String TotalAmount = (String) InvoiceMain.get("TotalAmount");
            // 发票类型
            String InvoiceSplit = (String) rootMap.get("InvoiceSplit");
            if (!"0".equals(InvoiceSplit) && !"1".equals(InvoiceSplit)) {
                return ResponseUtils.printFailure("9114:不支持" + InvoiceSplit + ",目前只支持0,1");
            }
            // 提取码
            String tqm = (String) rootMap.get("ExtractedCode");
            if (StringUtils.isNotBlank(tqm)) {
                Map params4 = new HashMap();
                params4.put("gsdm", gsdm);
                List tqmList = new ArrayList();
                tqmList.add(tqm);
                params4.put("tqmList", tqmList);
                List<Jyls> jylsList2 = jylsservice.findByMapParams(params4);
                /*
				 * JylsBean oldJylsBean = JylsBean.dao.findFirst(
				 * "select * from t_jyls where gsdm = ? and tqm = ?", new
				 * Object[]{gsdm, tqm});
				 */
                if (jylsList2 != null && !jylsList2.isEmpty()) {
                    return ResponseUtils.printFailure("9064:" + tqm);
                }
            }
            // 开票人
            String Drawer = (String) rootMap.get("Drawer");
            if (Drawer == null) {
                return ResponseUtils.printFailure("9103：开票人为空");
            } else if (Drawer.length() > 10) {
                return ResponseUtils.printFailure("9103：开票人不能超过10个字符");
            }
            // email
            String Email = (String) rootMap.get("Email");
            if (Email != null && !Email
                    .matches("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$")) {
                return ResponseUtils.printFailure("9013:请求参数<Email>格式有误;");
            }
            // 是否发送邮件
            String IsSend = (String) rootMap.get("IsSend");
            if (IsSend != null && !"".equals("")) {
                if (!("0".equals(IsSend) || "1".equals(IsSend))) {
                    return ResponseUtils.printFailure("是否发送电子邮件只能填写0，1或者不填写");
                }
            }
            // 收件人
            String Recipient = (String) rootMap.get("Recipient");
            if (Recipient != null && Recipient.length() > 10) {
                return ResponseUtils.printFailure("收件人不能超过10个字符");
            }
            // 收件人邮编
            String Zip = (String) rootMap.get("Zip");
            if (Zip != null && Zip.length() > 10) {
                return ResponseUtils.printFailure("收件人邮编不能超过10个字符");
            }
            // 收件人地址
            String LetterAddress = (String) rootMap.get("LetterAddress");
            // 收款人
            String Payee = (String) rootMap.get("Payee");
            if (Payee != null && Payee.length() > 10) {
                return ResponseUtils.printFailure("收款人不能超过10个字符");
            }
            // 复核人
            String Reviewer = (String) rootMap.get("Reviewer");
            if (Reviewer != null && Reviewer.length() > 10) {
                return ResponseUtils.printFailure("复核人不能超过10个字符");
            }
            // 作废人
            String CancelUser = (String) rootMap.get("CancelUser");

            // 订单号
            String OrderNumber = (String) InvoiceMain.get("OrderNumber");
            if (OrderNumber == null) {
                return ResponseUtils.printFailure("9104：订单号为空");
            } else if (OrderNumber.length() > 20) {
                return ResponseUtils.printFailure("9015：订单号不能超过20个字符");
            }
            BigDecimal zero = new BigDecimal(0);
            // 价税合计
            if (TotalAmount == null) {
                return ResponseUtils.printFailure("9105：价税合计为空");
            } else if (!TotalAmount.matches("^\\-?[1-9]{1}?[0-9]{0,15}+(.[0-9]{0,2})?$")) {
                return ResponseUtils.printFailure("价税合计格式不正确");
            }
            if ("1".equals(serviceType) || "4".equals(serviceType)) {
                BigDecimal jshj = new BigDecimal(TotalAmount);
                if (jshj.compareTo(zero) >= 0) {
                    return ResponseUtils.printFailure("红冲或换开操作时TotalAmount必须为负数");
                } else {
                    // 判断红冲金额对不对
                    // KplsBean kplsBean = KplsBean.dao.findFirst("select * from
                    // t_kpls where fpdm = ? and fphm = ?", yfpdm, yfphm);
                    Map params5 = new HashMap();
                    params5.put("fpdm", yfpdm);
                    params5.put("fphm", yfphm);
                    Kpls kpls = (Kpls) kplsservice.findFpExist(params5).get(0);
                    // Double yjshj = kplsBean.get("jshj");
                    Double yjshj = kpls.getJshj();
                    if (new BigDecimal("-" + yjshj.toString()).compareTo(new BigDecimal(TotalAmount)) != 0) {
                        return ResponseUtils.printFailure("红冲或换开操作的负数金额必须原发票的价税合计相等");
                    }
                }
            }
            // 含税标志
            String TaxMark = (String) InvoiceMain.get("TaxMark");
            if (TaxMark == null) {
                return ResponseUtils.printFailure("9106：含税标志为空");
            } else if (!(TaxMark.equals("0") || TaxMark.equals("1"))) {
                return ResponseUtils.printFailure("9113：含税标志格式不符");
            }
            // 订单时间
            String OrderDate = (String) InvoiceMain.get("OrderDate");
            Pattern p = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s((([0-1][0-9])|(2?[0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
            if (OrderDate != null && !p.matcher(OrderDate).matches()) {
                return ResponseUtils.printFailure("订单时间格式不正确");
            }
            // 备注
            String Remark = (String) InvoiceMain.get("Remark");
            if (Remark != null && Remark.length() > 150) {
                return ResponseUtils.printFailure("备注信息太长");
            }
            Map seller = (Map) InvoiceMain.get("Seller");
            // 销方税号
            String Identifier = (String) seller.get("Identifier");
            if (Identifier == null) {
                return ResponseUtils.printFailure("9002");
            }
            // 销方名称
            String Name = (String) seller.get("Name");
            if (Name == null) {
                return ResponseUtils.printFailure("9004");
            }
			/*
			 * XfBean xfbean = XfBean.dao.findFirst(
			 * "select * from t_xf where xfsh = ? and xfmc = ? and gsdm = ?",
			 * new Object[]{Identifier, Name, gsdm});
			 */
            Xf params6 = new Xf();
            params6.setXfsh(Identifier);
            params6.setXfmc(Name);
            params6.setGsdm(gsdm);
            Xf xfbean = xfservice.findOneByParams(params6);
            if (xfbean == null) {
                return ResponseUtils.printFailure("9100:公司对应的销方税号或名称不存在");
            }
            // 销方地址
            String Address = (String) seller.get("Address");
            if (Address == null) {
                return ResponseUtils.printFailure("9006");
            } else if (Address.length() > 100) {
                return ResponseUtils.printFailure("销方地址太长");
            }
            // 销方电话
            String TelephoneNo = (String) seller.get("TelephoneNo");
            if (TelephoneNo == null) {
                return ResponseUtils.printFailure("9008");
            } else if (TelephoneNo.length() > 20) {
                return ResponseUtils.printFailure("销方电话超过20个字符");
            }
            // 销方银行
            String Bank = (String) seller.get("Bank");
            if (Bank == null) {
                return ResponseUtils.printFailure("9101：销方银行为空");
            } else if (Bank.length() > 25) {
                return ResponseUtils.printFailure("销方银行超过25个字符");
            }
            // 销方银行账号
            String BankAcc = (String) seller.get("BankAcc");
            if (BankAcc == null) {
                return ResponseUtils.printFailure("9102：销方银行账号为空");
            } else if (BankAcc.length() > 30) {
                return ResponseUtils.printFailure("销方银行账号超过30个字符");
            }
            Map buyer = (Map) InvoiceMain.get("Buyer");
            // 购方名称
            String buyerName = (String) buyer.get("Name");
            if (buyerName == null) {
                return ResponseUtils.printFailure("9014");
            } else if (buyerName.length() > 100) {
                return ResponseUtils.printFailure("购方名称太长");
            }
            // 购方税号
            String buyerIdentifier = (String) buyer.get("Identifier");
            if (buyerIdentifier != null) {
                if (!(buyerIdentifier.length() == 15 || buyerIdentifier.length() == 18)) {
                    return ResponseUtils.printFailure("购方税号只能是15或者18位");
                }
            }
            // 购方地址
            String buyerAddress = (String) buyer.get("Address");
            if (buyerAddress != null && buyerAddress.length() > 100) {
                return ResponseUtils.printFailure("购方地址太长");
            }
            // 购方电话
            String buyerTelephoneNo = (String) buyer.get("TelephoneNo");
            if (buyerTelephoneNo != null && buyerTelephoneNo.length() > 20) {
                return ResponseUtils.printFailure("购方电话超过20个字符");
            }
            // 购方银行
            String buyerBank = (String) buyer.get("Bank");
            if (buyerBank != null && buyerBank.length() > 50) {
                return ResponseUtils.printFailure("购方银行超过50个字符");
            }
            // 购方银行账号
            String buyerBankAcc = (String) buyer.get("BankAcc");
            if (buyerBankAcc != null && buyerBankAcc.length() > 50) {
                return ResponseUtils.printFailure("购方银行账号超过50个字符");
            }
            // 商品明细
            List Details = (List) rootMap.get("InvoiceDetails");
            if (Details == null || Details.size() < 1) {
                return ResponseUtils.printFailure("9107:订单号为" + OrderNumber + "的订单没有商品明细");
            }
            Map detail;
            String msg = "";
            boolean flag = true;
            BigDecimal ajshj;
            BigDecimal jshj = new BigDecimal("0");
            for (int i = 0; i < Details.size(); i++) {
                detail = (Map) Details.get(i);
                // 商品编码
                String ProductCode = (String) detail.get("ProductCode");
                if (ProductCode == null) {
                    msg += "9108：订单号为" + OrderNumber + "的订单第" + i + "条商品ProductCode为空";
                    flag = false;
                } else if (ProductCode.length() != 19) {
                    msg += "9108：订单号为" + OrderNumber + "的订单第" + i + "条商品ProductCode不等于19位";
                    flag = false;
                }
                // 商品名称
                String ProductName = (String) detail.get("ProductName");
                if (ProductName == null) {
                    msg += "9016：订单号为" + OrderNumber + "的订单第" + i + "条商品ProductName为空！";
                    flag = false;
                } else if (ProductName.length() > 50) {
                    msg += "9016：订单号为" + OrderNumber + "的订单第" + i + "条商品ProductName太长！";
                    flag = false;
                }
                // 发票行性质
                String RowType = (String) detail.get("RowType");
                if (RowType == null) {
                    msg += "9109：订单号为" + OrderNumber + "的订单第" + i + "条商品RowType为空|";
                    flag = false;
                } else if (!("0".equals(RowType) || "1".equals(RowType) || "2".equals(RowType))) {
                    msg += "9109：订单号为" + OrderNumber + "的订单第" + i + "条商品RowType只能填写0，1或2";
                    flag = false;
                }
                // 商品数
                String Quantity = (String) detail.get("Quantity");
                if (Quantity != null && ("1".equals(serviceType) || "4".equals(serviceType))
                        && Double.valueOf(Quantity) >= 0) {
                    msg += "9023：订单号为" + OrderNumber + "的订单第" + i + "条商品Quantity在红冲或换开时必须为负数！";
                    flag = false;
                }

                // 商品金额
                String Amount = (String) detail.get("Amount");
                if (Amount == null) {
                    msg += "9023：订单号为" + OrderNumber + "的订单第" + i + "条商品Amount为空|";
                    flag = false;
                } else if (!Amount.matches("^\\-?[1-9]{1}?[0-9]{0,15}+(.[0-9]{0,2})?$")) {
                    msg += "9023：订单号为" + OrderNumber + "的订单第" + i + "条商品Amount格式不正确！";
                    flag = false;
                }
                if (("1".equals(serviceType) || "4".equals(serviceType)) && !"-".equals(Amount.substring(0, 1))) {
                    msg += "9023：订单号为" + OrderNumber + "的订单第" + i + "条商品在红冲或换开操作时Amount必须为负数！";
                    flag = false;
                }

                // 商品税率
                String TaxRate = (String) detail.get("TaxRate");
                if (TaxRate == null) {
                    msg = "9018：订单号为" + OrderNumber + "的订单第" + i + "条商品TaxRate为空|";
                    flag = false;
                }
                if (!(TaxRate.equals("0") || TaxRate.equals("0.03") || TaxRate.equals("0.04") || TaxRate.equals("0.06")
                        || TaxRate.equals("0.11") || TaxRate.equals("0.13") || TaxRate.equals("0.17"))) {
                    msg += "9019：订单号为" + OrderNumber + "的订单第" + i + "条商品TaxRate格式有误|";
                    flag = false;
                }
                // 商品税额
                String TaxAmount = (String) detail.get("TaxAmount");
                if (TaxAmount != null && TaxAmount.equals("^\\-?[1-9]{1}?[0-9]{0,15}+(.[0-9]{0,2})?$")) {
                    msg += "订单号为" + OrderNumber + "的订单第" + i + "条商品TaxAmount格式不正确！";
                    flag = false;
                }
                if (("1".equals(serviceType) || "4".equals(serviceType)) && TaxAmount != null
                        && !"-".equals(TaxAmount.substring(0, 1))) {
                    msg += "9023：订单号为" + OrderNumber + "的订单第" + i + "条商品在红冲或换开操作时TaxAmount必须为负数！";
                    flag = false;
                }
                if (flag) {
                    double je = Double.valueOf(Amount);
                    double se = 0;
                    if (TaxAmount != null && !"".equals(TaxAmount)) {
                        se = Double.valueOf(TaxAmount);
                    }
                    double sl = Double.valueOf(TaxRate);
                    if (TaxMark.equals("0") && je * sl - se >= 0.0625) {
                        msg += "9112：订单号为" + OrderNumber + "的订单第" + i + "条商品的(Amount，TaxRate，TaxAmount)之间的校验不通过";
                        flag = false;
                    }
                    BigDecimal bd = new BigDecimal(je);
                    BigDecimal bd1 = new BigDecimal(se);
                    ajshj = bd.add(bd1);
                    jshj = jshj.add(ajshj);
                }
            }
            if (flag) {
                BigDecimal bd = new BigDecimal(TotalAmount);
                if (bd.subtract(jshj.setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue() != 0) {
                    msg += "订单号为" + OrderNumber + "的订单TotalAmount，Amount，TaxAmount计算校验不通过";
                    flag = false;
                }
            }
            if (!flag) {
                return ResponseUtils.printFailure(msg);
            }
            String fpczlxdm = "11";
            if ("1".equals(serviceType)) {
                fpczlxdm = "12";
            } else if ("4".equals(serviceType)) {
                fpczlxdm = "13";
            }

            SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Jyls iurb = new Jyls(); // 主表
            iurb.setJylsh(serialNumber);// 交易流水号 包括所有需要开票和不需要开票的全部交易流水
            try {
                iurb.setJylssj(OrderDate == null ? new Date() : sim.parse(OrderDate));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }// 交易流水时间
            iurb.setFpzldm(fpzldm); // 发票种类代码
            iurb.setFpczlxdm(fpczlxdm);// 发票操作类型代码
            iurb.setDdh(OrderNumber);// 订单号
            iurb.setXfmc(Name);// 销方名称
            iurb.setXfsh(Identifier);// 销方税号 非空
            iurb.setXfyh(Bank);// 销方银行
            iurb.setXfyhzh(BankAcc);// 销方银行账号
            iurb.setXfdz(Address);// 销方地址
            iurb.setXfdh(TelephoneNo);// 销方电话
            iurb.setSffsyj(IsSend);// 0纸质开具 仅作存库 1发送邮件
            iurb.setGfmc(buyerName);// 购方名称
            iurb.setGfsh(buyerIdentifier);// 购方税号
            iurb.setGfyh(buyerBank);// 购方银行
            iurb.setGfyhzh(buyerBankAcc);// 购方银行账号
            iurb.setGfdz(buyerAddress);// 购方地址
            iurb.setGfdh(buyerTelephoneNo);// 购方单号
            iurb.setGfyb(Zip);// 购方邮编
            iurb.setGfemail(Email);// 邮箱地址
            iurb.setClztdm("01");// 电子发票处理状态代码
            iurb.setBz(Remark);// 备注
            iurb.setKpr(Drawer);// 开票人
            iurb.setSkr(Payee);// 收款人
            iurb.setFhr(Reviewer);// 复核人
            iurb.setYfpdm(yfpdm);// 原发票代码
            iurb.setYfphm(yfphm);// 原发票号码
            iurb.setJshj(Double.valueOf(TotalAmount));// 加税合计
            iurb.setYkpjshj(Double.valueOf("0.00"));// 已开发票价税合计
            iurb.setSsyf(TimeUtil.getSysDateString());// 所属月份 6位
            iurb.setHsbz(TaxMark);// 含税标志
            iurb.setYxbz("1");// 有效标志
            iurb.setLrsj(new Date());// 录入日期
            iurb.setLrry(lrry);// 录入人员
            iurb.setXgsj(new Date());// 修改日期
            iurb.setXgry(lrry);// 修改人员
            iurb.setGfsjr(Recipient);// 购方收件人
            iurb.setGsdm(gsdm); // 公司代码
            iurb.setTqm(tqm); // 提取码
            iurb.setXfid(xfid);
            iurb.setSkpid(skpid);
            jylsservice.save(iurb);

            //iurb = iurb.find("select * from t_jyls where jylsh ='" + serialNumber + "' and gsdm='" + gsdm + "'").get(0);
			/*Map params4 = new HashMap();
			params4.put("gsdm", gsdm);
			List jylshList2 = new ArrayList();
			jylshList2.add(serialNumber);
			params4.put("jylshList", jylshList2);
			iurb = jylsservice.findByMapParams(params4).get(0);*/

            djh = iurb.getDjh();
            String ProductCode;
            String ProductName;
            String RowType;
            String Amount;
            String TaxRate;
            String TaxAmount;
            String Spec;
            String Unit;
            String Quantity;
            String UnitPrice;
            Jyspmx iurdb;// 明细表

            for (int i = 0; i < Details.size(); i++) {
                detail = (Map) Details.get(i);
                ProductCode = (String) detail.get("ProductCode");
                ProductName = (String) detail.get("ProductName");
                RowType = (String) detail.get("RowType");
                Amount = (String) detail.get("Amount");
                TaxRate = (String) detail.get("TaxRate");
                TaxAmount = (String) detail.get("TaxAmount");
                Spec = (String) detail.get("Spec");
                Unit = (String) detail.get("Unit");
                Quantity = (String) detail.get("Quantity");
                UnitPrice = (String) detail.get("UnitPrice");

                iurdb = new Jyspmx();
                iurdb.setDjh(iurb.getDjh());// 流水单据号
                iurdb.setSpmxxh(i + 1);// 明细序号
                iurdb.setFphxz(RowType);// 发票行性质
                iurdb.setSpdm(ProductCode);// 商品编码
                iurdb.setSpmc(ProductName);// 商品名称
                iurdb.setSpggxh(Spec);// 商品规格型号
                iurdb.setSpdw(Unit);// 商品单位
                iurdb.setSps((null == Quantity || Quantity.equals("") || Quantity.equals("null")) ? null : Double.valueOf(Quantity));// 商品数量
                iurdb.setSpdj((null == UnitPrice || UnitPrice.equals("") || UnitPrice.equals("null")) ? null : Double.valueOf(UnitPrice));// 商品
                iurdb.setSpje((null == Amount || Amount.equals("") || Amount.equals("null")) ? null : Double.valueOf(Amount));// 商品金额
                iurdb.setSpsl((TaxRate == Amount || TaxRate.equals("") || TaxRate.equals("null")) ? null : Double.valueOf(TaxRate));// 商品税率
                iurdb.setSpse(TaxMark.equals("0") ? Double.valueOf(TaxAmount)
                        : Double.valueOf(Amount) * Double.valueOf(TaxRate));// 商品税额
                iurdb.setJshj(TaxMark.equals("0") ? (Double.valueOf(TaxAmount) + Double.valueOf(Amount))
                        : Double.valueOf(Amount));// 价税合计
                iurdb.setYkphj(Double.valueOf("0.00"));
                iurdb.setLrsj(new Date());// 录入时间
                iurdb.setLrry(lrry);// 录入人员
                iurdb.setXgsj(new Date());// 修改时间
                iurdb.setXgry(lrry);// 修改人员
                iurdb.setGsdm(gsdm);// 公司代码
                iurdb.setXfid(xfid);
                iurdb.setSkpid(skpid);
                jyspmxservice.save(iurdb);
            }
        } else {// 红冲操作：分为全部红冲和部分红冲
            // 先判断商品明细是否为空
            List Details = (List) rootMap.get("InvoiceDetails");
            // 通过发票号码，发票代码查询库里的开票流水信息
            Map params5 = new HashMap();
            params5.put("fpdm", yfpdm);
            params5.put("fphm", yfphm);
            Kpls kplsBean = (Kpls) kplsservice.findFpExist(params5).get(0);
            String kplsh = "";
            String jylsh2 = "";
            String djh2 = "";
            if (null == kplsBean || kplsBean.equals("")) {
                return ResponseUtils.printFailure("红冲操作发票号码或发票代码不存在");
            } else {
                kplsh = String.valueOf(kplsBean.getKplsh());
                djh = kplsBean.getDjh();
                djh2 = String.valueOf(kplsBean.getDjh());
                jylsh2 = kplsBean.getJylsh();
            }
            // 红冲通知单号
            String CNNoticeNo = (String) rootMap.get("CNNoticeNo");
            if (CNNoticeNo != null && CNNoticeNo.length() > 20) {
                return ResponseUtils.printFailure("红冲通知单号不能超过20个字符");
            }

            Jyls jylsin = new Jyls();
            jylsin.setJylsh(jylsh2);
            jylsin.setDjh(djh);
            Jyls jylsBean = jylsservice.findOneByParams(jylsin);
            if (Details == null || Details.size() < 1) { // 全部红冲

                // 校验其之前是否部分红冲，若进行过部分红冲，则不允许其再做全部红冲操作
                if (kplsBean.getFpztdm().equals("02")) {
                    return ResponseUtils.printFailure("此发票已红冲过!");
                }

                String TotalAmount = (String) rootMap.get("TotalAmount");
                if (TotalAmount == null) {
                    return ResponseUtils.printFailure("9105：价税合计为空");
                } else if (!TotalAmount.matches("^\\-?[1-9]{1}?[0-9]{0,15}+(.[0-9]{0,2})?$")) {
                    return ResponseUtils.printFailure("价税合计格式不正确");
                }
                // 通过开票流水信息查询开票商品明细数据
                Map paramst = new HashMap();
                paramst.put("kplsh", kplsh);
                List<Kpspmx> kpspmxList = kpspmxservice.findMxNewList(paramst);
                Double yjshj = 0.0;
                if (null == kpspmxList || kpspmxList.size() < 1) {
                    return ResponseUtils.printFailure("红冲操作未查到要红冲的商品明细");
                } else {
                    for (int i = 0; i < kpspmxList.size(); i++) {
                        Kpspmx kpspmxBean = (Kpspmx) kpspmxList.get(i);
                        yjshj = yjshj
                                + Double.valueOf((null == kpspmxBean.getKhcje() || kpspmxBean.getKhcje().equals(""))
                                ? "0" : kpspmxBean.getKhcje().toString());
                    }
                }
                // 判断红冲金额对不对
                // Double yjshj = kplsBean.get("jshj");
                if (new BigDecimal("-" + yjshj.toString()).compareTo(new BigDecimal(TotalAmount)) != 0) {
                    return ResponseUtils.printFailure("红冲或换开操作的负数金额必须原发票的价税合计相等");
                } else {
                    Map map = new HashMap();
                    map.put("xfid", xfid);
                    map.put("skpid", skpid);
                    map.put("hztzdh", CNNoticeNo);
                    map.put("serialNumber", serialNumber);
                    map.put("sfqhcbz", "1"); // 表示是否是全部红冲 1表示是，0 表示否
                    jylsBean = saveJyls(kplsBean, jylsBean, map);
                    saveJyspmx(kpspmxList, jylsBean, map);
                }
            } else { // 部分红冲
                //Map InvoiceMain = (Map) rootMap.get("InvoiceMain");
                // 含税标志
                String TaxMark = (String) rootMap.get("TaxMark");
                if (TaxMark == null) {
                    return ResponseUtils.printFailure("9106：含税标志为空");
                } else if (!(TaxMark.equals("0") || TaxMark.equals("1"))) {
                    return ResponseUtils.printFailure("9113：含税标志格式不符");
                }
                // 校验数据是否符合条件
                Map detail2 = null;
                String msg = "";
                boolean flag = true;
                for (int i = 0; i < Details.size(); i++) {
                    detail2 = (Map) Details.get(i);
                    // 商品编码
                    String ProductCode = (String) detail2.get("ProductCode");
                    if (ProductCode == null) {
                        msg += "9108：第" + i + "条商品ProductCode为空";
                        flag = false;
                    } else if (ProductCode.length() != 19) {
                        msg += "9108：第" + i + "条商品ProductCode不等于19位";
                        flag = false;
                    }
                    // 商品名称
                    String ProductName = (String) detail2.get("ProductName");
                    if (ProductName == null) {
                        msg += "9016：第" + i + "条商品ProductName为空！";
                        flag = false;
                    } else if (ProductName.length() > 50) {
                        msg += "9016：第" + i + "条商品ProductName太长！";
                        flag = false;
                    }
                    // 发票行性质
                    String RowType = (String) detail2.get("RowType");
                    if (RowType == null) {
                        msg += "9109：第" + i + "条商品RowType为空|";
                        flag = false;
                    } else if (!("0".equals(RowType) || "1".equals(RowType) || "2".equals(RowType))) {
                        msg += "9109：第" + i + "条商品RowType只能填写0，1或2";
                        flag = false;
                    }
                    // 商品数
                    String Quantity = (String) detail2.get("Quantity");
                    if (Quantity != null && ("1".equals(serviceType) || "4".equals(serviceType))
                            && Double.valueOf(Quantity) >= 0) {
                        msg += "9023：第" + i + "条商品Quantity在红冲或换开时必须为负数！";
                        flag = false;
                    }

                    // 商品金额
                    String Amount = (String) detail2.get("Amount");
                    if (Amount == null) {
                        msg += "9023：第" + i + "条商品Amount为空|";
                        flag = false;
                    } else if (!Amount.matches("^\\-?[1-9]{1}?[0-9]{0,15}+(.[0-9]{0,2})?$")) {
                        msg += "9023：第" + i + "条商品Amount格式不正确！";
                        flag = false;
                    }
                    if (("1".equals(serviceType) || "4".equals(serviceType)) && !"-".equals(Amount.substring(0, 1))) {
                        msg += "9023：第" + i + "条商品在红冲或换开操作时Amount必须为负数！";
                        flag = false;
                    }

                    // 商品税率
                    String TaxRate = (String) detail2.get("TaxRate");
                    if (TaxRate == null) {
                        msg = "9018：第" + i + "条商品TaxRate为空|";
                        flag = false;
                    }
                    if (!(TaxRate.equals("0") || TaxRate.equals("0.03") || TaxRate.equals("0.04")
                            || TaxRate.equals("0.06") || TaxRate.equals("0.11") || TaxRate.equals("0.13")
                            || TaxRate.equals("0.17"))) {
                        msg += "9019：第" + i + "条商品TaxRate格式有误|";
                        flag = false;
                    }
                    // 商品税额
                    String TaxAmount = (String) detail2.get("TaxAmount");
                    if (TaxAmount != null && TaxAmount.equals("^\\-?[1-9]{1}?[0-9]{0,15}+(.[0-9]{0,2})?$")) {
                        msg += "第" + i + "条商品TaxAmount格式不正确！";
                        flag = false;
                    }
                    if (("1".equals(serviceType) || "4".equals(serviceType)) && TaxAmount != null
                            && !"-".equals(TaxAmount.substring(0, 1))) {
                        msg += "9023：第" + i + "条商品在红冲或换开操作时TaxAmount必须为负数！";
                        flag = false;
                    }
                    if (flag) {
                        double je = Double.valueOf(Amount);
                        double se = 0;
                        if (TaxAmount != null && !"".equals(TaxAmount)) {
                            se = Double.valueOf(TaxAmount);
                        }
                        double sl = Double.valueOf(TaxRate);
                        if (TaxMark.equals("0") && je * sl - se >= 0.0625) {
                            msg += "9112：第" + i + "条商品的(Amount，TaxRate，TaxAmount)之间的校验不通过";
                            flag = false;
                        }
						/*
						 * BigDecimal bd = new BigDecimal(je); BigDecimal bd1 =
						 * new BigDecimal(se); ajshj = bd.add(bd1); jshj =
						 * jshj.add(ajshj);
						 */
                    }
                }
                if (!flag) {
                    return ResponseUtils.printFailure(msg);
                }

                List detailsNew = dealList(Details);// 将相同的明细数据合并 上传数据list
                // 已经通过商品代码和商品名称合并后的开票商品明细数据，用来和detailesNew中数据进行校验，看其是否符合红冲条件
			/*	List<Kpspmx> kpspmxList = KpspmxBean.dao
						.find("select t.kplsh,t.djh,t.spmxxh,t.fphxz,t.spdm,t.spmc,SUM(t.sps) sps,SUM(t.spje) spje,SUM(t.spse) spse,SUM(t.khcje) khcje,sum(t.yhcje) yhcje from t_kpspmx t where  t.kplsh='"
								+ kplsh + "' GROUP BY t.spdm,spmc");*/
                Map tt = new HashMap();
                tt.put("kplsh", kplsh);
                List<Kpspmx> kpspmxList = kpspmxservice.findMxNewByParams(tt);
                // 比较上传明细和开票流水明细数据，校验是否符合部分红冲条件 true表示符合红冲校验，false表示不符合红冲校验
                boolean khcbz = compareList(detailsNew, kpspmxList);
                if (khcbz == true) {
					/*List<Kpspmx> kpspmxList2 = KpspmxBean.dao
							.find("select * from t_kpspmx t where  t.kplsh='" + kplsh + "'");*/
                    List<Kpspmx> kpspmxList2 = kpspmxservice.findMxNewList(tt);
                    Map map = new HashMap();
                    map.put("xfid", xfid);
                    map.put("skpid", skpid);
                    map.put("hztzdh", CNNoticeNo);
                    map.put("TaxMark", TaxMark);
                    map.put("serialNumber", serialNumber);
                    map.put("sfqhcbz", "0"); // 表示是否是全部红冲 1表示是，0 表示否
                    Double hcjshj = 0.0;
                    for (int i = 0; i < detailsNew.size(); i++) {
                        Map tmpMap = (Map) detailsNew.get(i);
                        hcjshj = hcjshj + Double.valueOf(tmpMap.get("TaxAmount").toString())
                                + Double.valueOf(tmpMap.get("Amount").toString());
                    }
                    map.put("hcjshj", hcjshj);

                    String result = "";

                    jylsBean = saveJyls(kplsBean, jylsBean, map);// 保存交易流水表，并更新fpztdm为02已红冲
                    result = updateKhcAndYhc(kpspmxList2, detailsNew, jylsBean, map);

                    if (!result.equals("0")) {
                        //数据不匹配必须手动回滚
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return ResponseUtils.printFailure("第" + result + "明细数据红冲金额大于系统可红冲金额！");
                    } else {
                        saveJyspmx2(Details, jylsBean, map);
                    }
                    // System.out.println(khcbz);
                } else {
                    return ResponseUtils.printFailure("明细数据可红冲金额大于系统可红冲金额！");
                }
            }
            djh = jylsBean.getDjh();
        }
        return ResponseUtils.printSuccess(djh);
    }

    // 部分红冲保存交易商品明细
    @Transactional
    public String updateKhcAndYhc(List kpspmxList, List detailsNew, Jyls jylsBean, Map map) {

        String ProductCode;
        String ProductName;
        String RowType;
        String Amount;
        String TaxRate;
        String TaxAmount;
        String Spec;
        String Unit;
        String Quantity;
        String UnitPrice;
        String KHCJE;
        String YHCJE;
        // String TaxMark =jylsBean.get("hsbz");
        String TaxMark = map.get("TaxMark").toString();
        Jyspmx iurdb;// 交易商品明细表
        Kpspmx kpspmxbean;// 开票商品明细表
        //Map detail = null;

        for (int i = 0; i < kpspmxList.size(); i++) {
            kpspmxbean = (Kpspmx) kpspmxList.get(i);
            ProductCode = (String) kpspmxbean.getSpdm();
            ProductName = (String) kpspmxbean.getSpmc();
            RowType = (String) kpspmxbean.getFphxz();
            Amount = String.valueOf(kpspmxbean.getSpje());
            TaxRate = String.valueOf(kpspmxbean.getSpsl());
            TaxAmount = String.valueOf(kpspmxbean.getSpse());
            Spec = (String) kpspmxbean.getSpggxh();
            Unit = (String) kpspmxbean.getSpdw();
            Quantity = String.valueOf(kpspmxbean.getSps());
            UnitPrice = String.valueOf(kpspmxbean.getSpdj());
            KHCJE = String.valueOf(kpspmxbean.getKhcje());
            YHCJE = String.valueOf(kpspmxbean.getYhcje());
            for (int j = 0; j < detailsNew.size(); j++) {
                Map tmpMap = (Map) detailsNew.get(j);
                if (tmpMap.get("ProductCode").equals(ProductCode) && tmpMap.get("ProductName").equals(ProductName)) {
                    Double yhcje;
                    if ((Double
                            .valueOf(KHCJE)) < (Double.valueOf(tmpMap.get("TaxAmount").toString().replaceFirst("-", ""))
                            + Double.valueOf(tmpMap.get("Amount").toString().replaceFirst("-", "")))) {
                        if (kpspmxList.size() > detailsNew.size()) {// 如果开票商品明细数据条数>合并后的明细list，则进行后续操作
                            yhcje = Double.valueOf(KHCJE);
                            Double TaxAmount2 = Double.valueOf(tmpMap.get("TaxAmount").toString().replaceFirst("-", ""))
                                    - (Double.valueOf(KHCJE) / (Double.valueOf(TaxRate) + Double.valueOf(1)));
                            Double Amount2 = Double.valueOf(tmpMap.get("Amount").toString().replaceFirst("-", ""))
                                    - (Double.valueOf(KHCJE)
                                    - (Double.valueOf(KHCJE) / (Double.valueOf(TaxRate) + Double.valueOf(1))));
                            tmpMap.put("TaxAmount", "-" + TaxAmount2);
                            tmpMap.put("Amount", "-" + Amount2);

                            Map params = new HashMap();
                            params.put("khcje", Double.valueOf("0.000000"));
                            params.put("yhcje", yhcje);
                            params.put("kplsh", kpspmxbean.getKplsh());
                            params.put("xh", kpspmxbean.getSpmxxh());
                            kpspmxservice.update(params);//更新原发票明细的可红冲金额和已红冲金额
                            //kpspmxbean.set("khcje", Double.valueOf("0.000000")).set("yhcje", yhcje).update();
                        } else { // 可红冲金额小于上传金额+税额，且开票商品明细数据条数==合并后的明细list说明不能进行红冲操作
                            return "" + (i + 1);
                        }
                    } else {
                        Double khcje;
                        yhcje = Double.valueOf(tmpMap.get("TaxAmount").toString().replaceFirst("-", ""))
                                + Double.valueOf(tmpMap.get("Amount").toString().replaceFirst("-", ""));
                        khcje = Double.valueOf(KHCJE)
                                - Double.valueOf(tmpMap.get("TaxAmount").toString().replaceFirst("-", ""))
                                - Double.valueOf(tmpMap.get("Amount").toString().replaceFirst("-", ""));
                        Double TaxAmount2 = Double.valueOf("0");
                        Double Amount2 = Double.valueOf("0");
                        tmpMap.put("TaxAmount", "-" + TaxAmount2);
                        tmpMap.put("Amount", "-" + Amount2);
                        //kpspmxbean.set("khcje", khcje).set("yhcje", yhcje).update();

                        Map params = new HashMap();
                        params.put("khcje", khcje);
                        params.put("yhcje", yhcje);
                        params.put("kplsh", kpspmxbean.getKplsh());
                        params.put("xh", kpspmxbean.getSpmxxh());
                        kpspmxservice.update(params);//更新原发票明细的可红冲金额和已红冲金额
                    }
                }
            }
        }
        return "0";
    }

    public void saveJyspmx2(List Details, Jyls jylsBean, Map map) {
        Map detail = null;
        String ProductCode;
        String ProductName;
        String RowType;
        String Amount;
        String TaxRate;
        String TaxAmount;
        String Spec;
        String Unit;
        String Quantity;
        String UnitPrice;
        String KHCJE;
        String YHCJE;
        Jyspmx iurdb;
        String TaxMark = map.get("TaxMark").toString();
        for (int k = 0; k < Details.size(); k++) {
            detail = (Map) Details.get(k);
            ProductCode = (String) detail.get("ProductCode");
            ProductName = (String) detail.get("ProductName");
            RowType = (String) detail.get("RowType");
            Amount = String.valueOf(detail.get("Amount"));
            TaxRate = (String) detail.get("TaxRate");
            TaxAmount = (String) detail.get("TaxAmount");
            Spec = (String) detail.get("Spec");
            Unit = (String) detail.get("Unit");
            Quantity = (String) detail.get("Quantity");
            UnitPrice = (String) detail.get("UnitPrice");

            iurdb = new Jyspmx();
            iurdb.setDjh(jylsBean.getDjh());// 流水单据号
            iurdb.setSpmxxh(k + 1);// 明细序号
            iurdb.setFphxz(RowType);// 发票行性质
            iurdb.setSpdm(ProductCode);// 商品编码
            iurdb.setSpmc(ProductName);// 商品名称
            iurdb.setSpggxh(Spec);// 商品规格型号
            iurdb.setSpdw(Unit);// 商品单位
            iurdb.setSps((null == Quantity || Quantity.equals("") || Quantity.equals("null")) ? null : Double.valueOf(Quantity));// 商品数量
            iurdb.setSpdj((null == UnitPrice || UnitPrice.equals("") || UnitPrice.equals("null")) ? null : Double.valueOf(UnitPrice));// 商品单价
            iurdb.setSpje(Double.valueOf(Amount));// 商品金额
            iurdb.setSpsl(Double.valueOf(TaxRate));// 商品税率
            iurdb.setSpse(TaxMark.equals("0") ? Double.valueOf(TaxAmount)
                    : Double.valueOf(Amount) * Double.valueOf(TaxRate));// 商品税额
            iurdb.setJshj(TaxMark.equals("0") ? (Double.valueOf(TaxAmount) + Double.valueOf(Amount))
                    : Double.valueOf(Amount));// 价税合计
            iurdb.setYkphj(Double.valueOf("0.00"));
            iurdb.setLrsj(new Date());// 录入时间
            iurdb.setLrry(jylsBean.getLrry());// 录入人员
            iurdb.setXgsj(new Date());// 修改时间
            iurdb.setXgry(jylsBean.getXgry());// 修改人员
            iurdb.setGsdm(jylsBean.getGsdm());// 公司代码
            iurdb.setXfid(Integer.valueOf(map.get("xfid").toString()));
            iurdb.setSkpid(Integer.valueOf(map.get("skpid").toString()));
            jyspmxservice.save(iurdb);
        }
    }

    // 比较上传明细和开票流水明细数据，校验是否符合部分红冲条件
    // scList用户发来的数据明细数据，cqList数据库抽取的明细数据
    private boolean compareList(List scList, List cqList) {
        // boolean flag = true;
        for (int i = 0; i < scList.size(); i++) {
            Map tmpMap = (Map) scList.get(i);
            Double jshj = Double.valueOf(tmpMap.get("Amount").toString())
                    + Double.valueOf(tmpMap.get("TaxAmount").toString());
            for (int j = 0; j < cqList.size(); j++) {
                Kpspmx tmpBean = (Kpspmx) cqList.get(j);
                if (tmpMap.get("ProductCode").equals(tmpBean.getSpdm())
                        && tmpMap.get("ProductName").equals(tmpBean.getSpmc())) {
                    if (jshj > Double.valueOf(tmpBean.getKhcje().toString())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 处理传入的商品明细(将商品代码和商品名称相同的数据合并)
    private List dealList(List list1) {
        List list2 = new ArrayList(); // 新创建的list，用来存放处理过的list，最终返回。
        for (int i = 0; i < list1.size(); i++) {
            Map tmp = new HashMap();
            Map map1 = (Map) list1.get(i);
            if (list2.size() == 0) {
                // 第一次list2为空，就将list1的第一个map加进list2

                tmp.putAll(map1);
                list2.add(tmp);
                continue; // 终止下面的执行
            }
            int flag = 1;
            for (int j = 0; j < list2.size(); j++) {
                flag = 1; // 每次进来都先将flag赋为1
                Map map2 = (Map) list2.get(j);
                if (map2.get("ProductCode").equals(map1.get("ProductCode"))
                        && map2.get("ProductName").equals(map1.get("ProductName"))) {
                    Double nValue = Double.valueOf(map2.get("Amount").toString()); // 取出金额所对应的值
                    nValue += Double.valueOf(map1.get("Amount").toString()); // 将id相同的金额合并
                    map2.put("Amount", nValue); // 将合并后的name重新放进list2，此时会覆盖掉原来key相等的map

                    Double nValue2 = Double.valueOf(map2.get("TaxAmount").toString()); // 取出税额所对应的值
                    nValue2 += Double.valueOf(map1.get("TaxAmount").toString()); // 将id相同的税额合并
                    map2.put("TaxAmount", nValue2); // 将合并后的name重新放进list2，此时会覆盖掉原来key相等的map

                    Double nValue3 = Double.valueOf(map2.get("Quantity").toString()); // 取出税额所对应的值
                    nValue3 += Double.valueOf(map1.get("Quantity").toString()); // 将id相同的税额合并
                    map2.put("Quantity", nValue3); // 将合并后的name重新放进list2，此时会覆盖掉原来key相等的map
                    flag = 0; // 将flag赋值为0，flag用来判断是否将得到的list1中的map放进list2
                    break;
                }
            }
            if (flag == 1) {
                tmp.putAll(map1);
                list2.add(tmp);
            }
        }
        return list2;
    }

    // 全部红冲时保存jyls信息
    @Transactional
    public Jyls saveJyls(Kpls kplsBean, Jyls jylsBean, Map map) {
        String jshj;
        String hsbz = jylsBean.getHsbz();
        if (map.get("sfqhcbz").equals("1")) {// 表示全部红冲
            jshj = "-" + kplsBean.getJshj();
            hsbz = "0";
        } else {
            jshj = map.get("hcjshj").toString();
        }
        Jyls iurb = new Jyls(); // 主表
        iurb.setJylsh(String.valueOf(map.get("serialNumber")));// 交易流水号 包括所有需要开票和不需要开票的全部交易流水
        iurb.setJylssj(kplsBean.getJylssj());    // 交易流水时间
        iurb.setFpzldm(kplsBean.getFpzldm()); // 发票种类代码
        iurb.setFpczlxdm("12");// 发票操作类型代码 12表示红冲
        iurb.setDdh(jylsBean.getDdh()); // 订单号
        iurb.setJylssj(kplsBean.getJylssj());// 交易流水时间
        iurb.setXfmc(kplsBean.getXfmc());// 销方名称
        iurb.setXfsh(kplsBean.getXfsh());// 销方税号 非空
        iurb.setXfyh(kplsBean.getXfyh());// 销方银行
        iurb.setXfyhzh(kplsBean.getXfyhzh());// 销方银行账号
        iurb.setXfdz(kplsBean.getXfdz());// 销方地址
        iurb.setXfdh(kplsBean.getXfdh());// 销方电话
        iurb.setSffsyj(jylsBean.getSffsyj());// 0纸质开具 仅作存库 1发送邮件
        iurb.setGfmc(kplsBean.getGfmc());// 购方名称
        iurb.setGfsh(kplsBean.getGfsh());// 购方税号
        iurb.setGfyh(kplsBean.getGfyh());// 购方银行
        iurb.setGfyhzh(kplsBean.getGfyhzh());// 购方银行账号
        iurb.setGfdz(kplsBean.getGfdz());// 购方地址
        iurb.setGfdh(kplsBean.getGfdh());// 购方单号
        iurb.setGfyb(kplsBean.getGfyb()); // 购方邮编
        iurb.setGfemail(kplsBean.getGfemail());// 邮箱地址
        iurb.setClztdm("01"); // 电子发票处理状态代码
        iurb.setBz(kplsBean.getBz()); // 备注
        iurb.setKpr(kplsBean.getKpr());// 开票人
        iurb.setSkr(kplsBean.getSkr());// 收款人
        iurb.setFhr(kplsBean.getFhr()); // 复核人
        iurb.setHztzdh(String.valueOf(map.get("hztzdh")));// 红冲通知单号
        iurb.setYfpdm(kplsBean.getFpdm()); // 原发票代码
        iurb.setYfphm(kplsBean.getFphm()); // 原发票号码
        iurb.setJshj(Double.valueOf(jshj));// 价税合计
        iurb.setYkpjshj(Double.valueOf("0.00"));// 已开发票价税合计
        iurb.setSsyf(TimeUtil.getSysDateString());// 所属月份 6位
        iurb.setHsbz(hsbz);// 含税标志
        iurb.setYxbz("1");// 有效标志
        iurb.setLrsj(new Date());// 录入日期
        iurb.setLrry(kplsBean.getLrry());// 录入人员
        iurb.setXgsj(new Date());// 修改日期
        iurb.setXgry(kplsBean.getXgry());// 修改人员
        iurb.setGfsjr(jylsBean.getGfsjr());     // 购方收件人
        iurb.setGsdm(kplsBean.getGsdm()); // 公司代码
        iurb.setTqm(""); // 提取码
        iurb.setXfid(Integer.valueOf(map.get("xfid").toString()));
        iurb.setSkpid(Integer.valueOf(map.get("skpid").toString()));
        jylsservice.save(iurb);

        //kplsBean.set("fpztdm", "12").update();
        Map params = new HashMap();
        params.put("fpztdm", "02");
        params.put("kplsh", kplsBean.getKplsh());
        kplsservice.updateFpczlx(params);//更新原发票状态为02，已红冲
        return iurb;
    }

    // 全部红冲时保存jyspmx数据信息
    @Transactional
    public void saveJyspmx(List kpspmxList, Jyls jylsBean, Map map) {

        String ProductCode;
        String ProductName;
        String RowType;
        String Amount;
        String TaxRate;
        String TaxAmount;
        String Spec;
        String Unit;
        String Quantity;
        String UnitPrice;
        String TaxMark = jylsBean.getHsbz();
        Jyspmx iurdb;// 交易商品明细表
        Kpspmx kpspmxbean;// 开票商品明细表
        for (int i = 0; i < kpspmxList.size(); i++) {
            kpspmxbean = (Kpspmx) kpspmxList.get(i);
            ProductCode = (String) kpspmxbean.getSpdm();
            ProductName = (String) kpspmxbean.getSpmc();
            RowType = (String) kpspmxbean.getFphxz();
            Amount = String.valueOf(kpspmxbean.getSpje());
            TaxRate = String.valueOf(kpspmxbean.getSpsl());
            TaxAmount = String.valueOf(kpspmxbean.getSpse());
            Spec = (String) kpspmxbean.getSpggxh();
            Unit = (String) kpspmxbean.getSpdw();
            Quantity = String.valueOf(kpspmxbean.getSps());
            UnitPrice = String.valueOf(kpspmxbean.getSpdj());

            iurdb = new Jyspmx();
            iurdb.setDjh(jylsBean.getDjh());// 流水单据号
            iurdb.setSpmxxh(i + 1);// 明细序号
            iurdb.setFphxz(RowType);// 发票行性质
            iurdb.setSpdm(ProductCode);// 商品编码
            iurdb.setSpmc(ProductName);// 商品名称
            iurdb.setSpggxh(Spec);// 商品规格型号
            iurdb.setSpdw(Unit);// 商品单位
            iurdb.setSps((null == Quantity || Quantity.equals("") || Quantity.equals("null")) ? null : Double.valueOf("-" + Quantity));// 商品数量
            iurdb.setSpdj((null == UnitPrice || UnitPrice.equals("") || UnitPrice.equals("null")) ? null : Double.valueOf(UnitPrice));// 商品单价
            iurdb.setSpje(Double.valueOf("-" + Amount));// 商品金额
            iurdb.setSpsl(Double.valueOf(TaxRate));// 商品税率
            iurdb.setSpse(TaxMark.equals("0") ? Double.valueOf("-" + TaxAmount)
                    : Double.valueOf("-" + Amount) * Double.valueOf(TaxRate));// 商品税额
            iurdb.setJshj(TaxMark.equals("0") ? (Double.valueOf("-" + TaxAmount) + Double.valueOf("-" + Amount))
                    : Double.valueOf("-" + Amount));// 价税合计
            iurdb.setYkphj(Double.valueOf("0.00"));
            iurdb.setLrsj(new Date());// 录入时间
            iurdb.setLrry(jylsBean.getLrry());// 录入人员
            iurdb.setXgsj(new Date());// 修改时间
            iurdb.setXgry(jylsBean.getXgry());// 修改人员
            iurdb.setGsdm(jylsBean.getGsdm());// 公司代码
            iurdb.setXfid(Integer.valueOf(map.get("xfid").toString()));
            iurdb.setSkpid(Integer.valueOf(map.get("skpid").toString()));
            jyspmxservice.save(iurdb);

            Map params = new HashMap();
            params.put("khcje", Double.valueOf("0.000000"));
            params.put("yhcje", Double.valueOf(TaxAmount) + Double.valueOf(Amount));
            params.put("kplsh", kpspmxbean.getKplsh());
            params.put("xh", kpspmxbean.getSpmxxh());
            kpspmxservice.update(params);//更新原发票明细的可红冲金额和已红冲金额
        }
    }

    private OMElement xml2OMElement(String xml) throws XMLStreamException, UnsupportedEncodingException {
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
        StAXBuilder builder = new StAXOMBuilder(xmlStream);
        OMElement documentElement = builder.getDocumentElement();
        return documentElement;
    }

    private Map xml2Map(OMElement doc, String listTagNames) {
        Map docMap = new HashMap();
        Iterator<OMElement> iter = doc.getChildElements();
        OMElement node;
        String tagName, tagText;
        while (iter.hasNext()) {
            node = iter.next();
            tagName = str2Trim(node.getLocalName());
            if (listTagNames.indexOf(tagName) > -1) {
                docMap.put(tagName, xml2List(node, listTagNames));
            } else if (node.getChildElements().hasNext()) {
                docMap.put(tagName, xml2Map(node, listTagNames));
            } else {
                tagText = str2Trim(node.getText());
                docMap.put(tagName, tagText);
            }
        }
        return docMap;
    }

    private List xml2List(OMElement doc, String listTagNames) {
        List list = new ArrayList();
        Iterator<OMElement> iter = doc.getChildElements();
        OMElement node;
        String tagText;
        while (iter.hasNext()) {
            node = iter.next();
            if (node.getChildElements().hasNext()) {
                list.add(xml2Map(node, listTagNames));
            } else {
                tagText = str2Trim(node.getText());
                list.add(tagText);
            }
        }
        return list;
    }

    private String str2Trim(String str) {
        return "".equals(str) ? null : str.trim();
    }

    /**
     * 保存接受及发送的报文信息
     *
     * @param djh
     * @param clztdm
     * @param cljgdm
     * @param ffcs
     * @param ycms
     * @param lrry
     * @param xfsh
     * @param jylsh
     */
    private void saveLog(int djh, String clztdm, String cljgdm, String ffcs, String ycms, int lrry, String xfsh,
                         String jylsh) {
        Map bean = new HashMap();
        if (djh > 0) {
            bean.put("djh", djh);// 单据号
        }
        bean.put("clztdm", clztdm);// 电子发票处理状态代码
        bean.put("cljgdm", cljgdm);// 电子发票处理结果代码
        bean.put("ffcs", ffcs);// 调用方法和处理参数 调用方法名及传入参数
        bean.put("ycms", ycms); // 异常描述
        bean.put("lrsj", new Date());// 录入时间 系统时间
        bean.put("lrry", lrry);// 录入人员
        bean.put("xfsh", xfsh);
        bean.put("jylsh", jylsh);
        //new DzfplogBean().setAttrs(bean).save();
    }

    public String callService2(String OrderData) {
        Map params1 = new HashMap();
        String gsdm="afb";
        params1.put("gsdm", gsdm);
        Yh yh = yhservice.findOneByParams(params1);
        int lrry = yh.getId();
        OMElement root = null;
        List<Jymxsq> jymxsqList = new ArrayList();
        List<Jyzfmx> jyzfmxList = new ArrayList<Jyzfmx>();
        Map rsMap = new HashMap();
        Document xmlDoc = null;
        try {
            xmlDoc = DocumentHelper.parseText(OrderData);
            root = XmlMapUtils.xml2OMElement(OrderData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map rootMap = XmlMapUtils.xml2Map(root, "fpxx");
        // 购方名称
        String FPClientName = (String) rootMap.get("FPClientName");

        // 购方税号
        String FPClientTaxCode = (String) rootMap.get("FPClientTaxCode");

        // 交易流水号
        String SerialNo = (String) rootMap.get("SerialNo");

        // 订单号
        String OrderNo = (String) rootMap.get("OrderNo");

        // 订单时间
        String OrderDate = (String) rootMap.get("OrderDate");

        // 发票种类代码
        String invType = "12";

        // 购方开户行及账号
        String FPClientBankAccount = (String) rootMap.get("FPClientBankAccount");

        // 购方地址电话
        String FPClientAddressTel = (String) rootMap.get("FPClientAddressTel");

        // 销方开户行及账号
        String FPSellerBankAccount = (String) rootMap.get("FPSellerBankAccount");

        //销方税号
        String FPSellerTaxCode = String.valueOf(rootMap.get("FPSellerTaxCode"));

        // 销方名称
        String FPSellerName = (String) rootMap.get("FPSellerName");

        //销方地址电话
        String FPSellerAddressTel = String.valueOf(rootMap.get("FPSellerAddressTel"));

        //备注
        String FPNotes =  String.valueOf(rootMap.get("FPNotes"));

        //开票人
        String FPInvoicer =  String.valueOf(rootMap.get("FPInvoicer"));

        //复核人
        String FPChecker =  String.valueOf(rootMap.get("FPChecker"));

        //收款人
        String FPCashier =  String.valueOf(rootMap.get("FPCashier"));

        //价税合计
        String TotalAmount=String.valueOf(rootMap.get("TotalAmount"));

        //含税标志
        String PriceKind=String.valueOf(rootMap.get("PriceKind"));

        //购方邮箱
        String Email=String.valueOf(rootMap.get("Email"));

        //是否打印销货清单
        String FPListName =  String.valueOf(rootMap.get("FPListName"));

        //是否打印销货清单
        String FPState =  String.valueOf(rootMap.get("FPState"));

        Jyxxsq jyxxsq=new Jyxxsq();
        // 保存主表信息
        jyxxsq.setKpddm("");
        jyxxsq.setJylsh(SerialNo);
        jyxxsq.setFpzldm("12");
        jyxxsq.setKpr(FPInvoicer);
        jyxxsq.setSkr(FPCashier);
        jyxxsq.setFhr(FPChecker);
        jyxxsq.setOpenid("");
        jyxxsq.setXfsh(FPSellerTaxCode);
        jyxxsq.setXfmc(FPSellerName);
        jyxxsq.setXfdz(FPSellerAddressTel);
        //jyxxsq.setXfdh(telephoneNo);
        jyxxsq.setXfyh(FPSellerBankAccount);
        //jyxxsq.setXfyhzh(bankAcc);
        jyxxsq.setDdh(OrderNo);
        jyxxsq.setSfdyqd("1");
        jyxxsq.setSfcp("1");
        jyxxsq.setSfdy("0");
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            jyxxsq.setDdrq(OrderDate == null ? new Date() : sim.parse(OrderDate));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        jyxxsq.setZsfs("0");
        jyxxsq.setJshj(Double.valueOf(TotalAmount));
        jyxxsq.setHsbz(PriceKind);
        jyxxsq.setBz(FPNotes);
        jyxxsq.setGflx("0");
        jyxxsq.setGfsh(FPClientTaxCode.replaceAll(" ", ""));
        jyxxsq.setGfmc(FPClientName.replaceAll(" ", ""));
        jyxxsq.setGfdz(FPClientAddressTel);
        //jyxxsq.setGfdh(buyerTelephoneNo);
        jyxxsq.setGfyh(FPClientBankAccount);
        //jyxxsq.setGfyhzh(buyerBankAcc);
        jyxxsq.setGfemail(Email);//邮箱
        //jyxxsq.setGfsjh(MobilephoneNo);//手机号
        //jyxxsq.setKhh(khh);//客户号
        if(Email!=null){
            jyxxsq.setSffsyj("1");//是否发送邮件
        }else{
            jyxxsq.setSffsyj("0");//是否发送邮件
        }
        //为了照顾亚朵，途家两家老版本的发票开具xml样例
                /*if (null != ExtractedCode && !ExtractedCode.equals("")) {
                    jyxxsq.setTqm(ExtractedCode);
                } else if (null != buyerExtractedCode && !buyerExtractedCode.equals("")) {
                    jyxxsq.setTqm(buyerExtractedCode);
                }*/
        jyxxsq.setGfsjr("");
        jyxxsq.setGfsjrdz("");
        jyxxsq.setGfyb("");
        jyxxsq.setYkpjshj(Double.valueOf("0.00"));
        jyxxsq.setYxbz("1");
        jyxxsq.setLrsj(new Date());
        jyxxsq.setLrry(lrry);
        jyxxsq.setXgry(lrry);
        jyxxsq.setFpczlxdm("11");
        jyxxsq.setXgsj(new Date());
        jyxxsq.setGsdm(gsdm);
        jyxxsq.setSjly("1");
        jyxxsq.setClztdm("00");

        List<Element> xnList = xmlDoc.selectNodes("root/fpxx");
        if (null != xnList && xnList.size() > 0) {
            for (Element xn : xnList) {
                Element OrderDetails = (Element) xn.selectSingleNode("fpmx");
                List<Element> orderDetailsList = (List<Element>) OrderDetails.elements("group");
                if (null != orderDetailsList && orderDetailsList.size() > 0) {
                    int spmxxh = 1;
                    for (Element orderDetails : orderDetailsList) {
                        Jymxsq jymxsq = new Jymxsq();

                        // 商品名称
                        String MXGoodsName = "";
                        if (null != orderDetails.selectSingleNode("MXGoodsName")
                                && !orderDetails.selectSingleNode("MXGoodsName").equals("")) {
                            MXGoodsName = orderDetails.selectSingleNode("MXGoodsName").getText();
                        }

                        jymxsq.setSpmc(MXGoodsName);
                        jymxsq.setDdh(jyxxsq.getDdh());
                        jymxsq.setHsbz(jyxxsq.getHsbz());
                        jymxsq.setFphxz("0");
                        // 商品规格型号
                        String MXStandard = "";
                        if (null != orderDetails.selectSingleNode("MXStandard")
                                && !orderDetails.selectSingleNode("MXStandard").equals("")) {
                            MXStandard = orderDetails.selectSingleNode("MXStandard").getText();
                        }
                        jymxsq.setSpggxh(MXStandard);
                        // 商品单位
                        String MXUnit = "";
                        if (null != orderDetails.selectSingleNode("MXUnit")
                                && !orderDetails.selectSingleNode("MXUnit").equals("")) {
                            MXUnit = orderDetails.selectSingleNode("MXUnit").getText();
                        }
                        jymxsq.setSpdw(MXUnit);
                        // 商品数量
                        String MXNumber = "";
                        if (null != orderDetails.selectSingleNode("MXNumber")
                                && !orderDetails.selectSingleNode("MXNumber").equals("")) {
                            MXNumber = orderDetails.selectSingleNode("MXNumber").getText();
                            try {
                                jymxsq.setSps(Double.valueOf(MXNumber));
                            } catch (Exception e) {
                                jymxsq.setSps(null);
                            }
                        }
                        // 商品单价
                        String MXPrice = "";
                        if (null != orderDetails.selectSingleNode("MXPrice")
                                && !orderDetails.selectSingleNode("MXPrice").equals("")) {
                            MXPrice = orderDetails.selectSingleNode("MXPrice").getText();
                            try {
                                jymxsq.setSpdj(Double.valueOf(MXPrice));
                            } catch (Exception e) {
                                jymxsq.setSpdj(null);
                            }
                        }
                        // 商品金额
                        String MXAmount = "";
                        if (null != orderDetails.selectSingleNode("MXAmount")
                                && !orderDetails.selectSingleNode("MXAmount").equals("")) {
                            MXAmount = orderDetails.selectSingleNode("MXAmount").getText();
                            jymxsq.setSpje(Double.valueOf(MXAmount));
                        }
                       /* // 扣除金额
                        String DeductAmount = "";
                        if (null != orderDetails.selectSingleNode("DeductAmount")
                                && !orderDetails.selectSingleNode("DeductAmount").equals("")) {
                            DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
                            jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
                                    : Double.valueOf(DeductAmount));
                        }*/
                        //税率
                        String MXTaxRate = "";
                        if (null != orderDetails.selectSingleNode("MXTaxRate")
                                && !orderDetails.selectSingleNode("MXTaxRate").equals("")) {
                            MXTaxRate = orderDetails.selectSingleNode("MXTaxRate").getText();
                            jymxsq.setSpsl(Double.valueOf(MXTaxRate));
                        }
                        //商品税额
                        String MXTaxAmount = "";
                        if (null != orderDetails.selectSingleNode("MXTaxAmount")
                                && !orderDetails.selectSingleNode("MXTaxAmount").equals("")) {
                            MXTaxAmount = orderDetails.selectSingleNode("MXTaxAmount").getText();
                            try {
                                jymxsq.setSpse(Double.valueOf(MXTaxAmount));
                            } catch (Exception e) {
                                jymxsq.setSpse(null);
                            }
                        }
                        /*//含税标志
                        String MXPriceKind = "";
                        if (null != orderDetails.selectSingleNode("MXPriceKind")
                                && !orderDetails.selectSingleNode("MXPriceKind").equals("")) {
                            MXPriceKind = orderDetails.selectSingleNode("MXPriceKind").getText();
                            //
                        }*/

                        jymxsq.setSpmxxh(spmxxh);
                        spmxxh++;
                        if(jyxxsq.getHsbz().equals("1")){
                            jymxsq.setKkjje(Double.valueOf(MXAmount));
                            jymxsq.setJshj(Double.valueOf(MXAmount));
                        }else {
                            jymxsq.setKkjje(Double.valueOf(MXAmount)+Double.valueOf(MXTaxAmount));
                            jymxsq.setJshj(Double.valueOf(MXAmount)+Double.valueOf(MXTaxAmount));
                        }
                        jymxsq.setYkjje(0d);
                        /*String PolicyMark = "";
                        if (null != orderDetails.selectSingleNode("PolicyMark")
                                && !orderDetails.selectSingleNode("PolicyMark").equals("")) {
                            PolicyMark = orderDetails.selectSingleNode("PolicyMark").getText();
                        }
                        jymxsq.setYhzcbs(PolicyMark);

                        String TaxRateMark = "";
                        if (null != orderDetails.selectSingleNode("TaxRateMark")
                                && !orderDetails.selectSingleNode("TaxRateMark").equals("")) {
                            TaxRateMark = orderDetails.selectSingleNode("TaxRateMark").getText();
                        }
                        jymxsq.setLslbz(TaxRateMark);

                        String PolicyName = "";
                        if (null != orderDetails.selectSingleNode("PolicyName")
                                && !orderDetails.selectSingleNode("PolicyName").equals("")) {
                            PolicyName = orderDetails.selectSingleNode("PolicyName").getText();
                        }
                        jymxsq.setYhzcmc(PolicyName);*/
                        jymxsq.setGsdm(gsdm);
                        jymxsq.setLrry(lrry);
                        jymxsq.setLrsj(new Date());
                        jymxsq.setXgry(lrry);
                        jymxsq.setXgsj(new Date());
                        jymxsq.setYxbz("1");
                        jymxsqList.add(jymxsq);
                    }
                }
            }
        }
            for(int i = 0; i < jymxsqList.size(); i++){
                Jymxsq jymxsq = jymxsqList.get(i);
                Map map = new HashMap();
                map.put("gsdm",jymxsq.getGsdm());
                map.put("spmc",jymxsq.getSpmc());
                Spvo spvo = spvoService.findOneSpvo(map);
                if(null==spvo){
                }else {
                    jymxsq.setSpdm(spvo.getSpbm());
                    jymxsq.setYhzcbs(spvo.getYhzcbs());
                    jymxsq.setLslbz(spvo.getLslbz());
                    jymxsq.setYhzcmc(spvo.getYhzcmc());
                }
            }

        String xml= GetXmlUtil.getFpkjXml(jyxxsq,jymxsqList,jyzfmxList);
        String result=dealOrder01.execute(gsdm,xml,"11");
        return result;
    }
}
