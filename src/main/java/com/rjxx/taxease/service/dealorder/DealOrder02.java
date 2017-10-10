package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.service.result.DefaultResult;
import com.rjxx.taxease.service.result.Result04;
import com.rjxx.taxease.utils.CallDllWebServiceUtil;
import com.rjxx.taxease.utils.ResponseUtil;
import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.bizcomm.utils.DiscountDealUtil;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
import com.rjxx.taxeasy.bizcomm.utils.RemarkProcessingUtil;
import com.rjxx.taxeasy.bizcomm.utils.SaveOrderData;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.KplsVO4;
import com.rjxx.utils.CheckOrderUtil;
import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.axiom.om.OMElement;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017-05-18.
 */
@Service
public class DealOrder02 implements IDealOrder {

    @Autowired
    private CheckOrderUtil checkorderutil;

    @Autowired
    private SaveOrderData saveorderdata;

    @Autowired
    private YhService yhservice;

    @Autowired
    private JyxxsqService jyxxsqService;

    @Autowired
    private SkpService skpservice;

    @Autowired
    private CszbService cszbservice;

    @Autowired
    private DiscountDealUtil discountDealUtil;

    @Autowired
    private RemarkProcessingUtil remarkUtil;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private FpclService fpclservice;

    @Autowired
    private KpspmxService kpspmxService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(String gsdm, String orderData, String Operation) {
        String result = "";
        Map map = (Map) dealOperation02(gsdm, orderData);
        List<Jyxxsq> jyxxsqList = (List) map.get("jyxxsqList");
        //交易明细
        List<Jymxsq> jymxsqList = (List) map.get("jymxsqList");
        DefaultResult defaultResult = new DefaultResult();
        jyxxsqList = (List<Jyxxsq>) this.addMoreDate(jyxxsqList, gsdm);
        // 取出xfid、skpid等用来插叙该开票点是否直连开票
        int xfid = jyxxsqList.get(0).getXfid();
        int skpid = jyxxsqList.get(0).getSkpid();
        Cszb cszb = cszbservice.getSpbmbbh(gsdm, xfid, skpid, "sfzlkp");
        // List<Jyxxsq> reList = new ArrayList<Jyxxsq>();
        // t_jyxxsq表中sfzlkp设置为对应的值，1直连开，0非直连开
        for (int i = 0; i < jyxxsqList.size(); i++) {
            Jyxxsq jyxxsq = jyxxsqList.get(i);
            if (null != cszb && cszb.getCsz().equals("是")) {
                jyxxsq.setSfzlkp("1");
                jyxxsq.setZtbz("6");
            } else {
                jyxxsq.setSfzlkp("0");
                jyxxsq.setZtbz("6");
            }
        }
        //解决后续操作对list对象值改变造成原始数据的丢失
        List<Jymxsq> tmp1List = new ArrayList<Jymxsq>((List) map.get("jymxsqList"));
        List<Jyzfmx> jyzfmxList = (List) map.get("jyzfmxList");
        List<Jymxsq> jymxsq2List = new ArrayList<Jymxsq>(tmp1List);
        Jyxxsq jyxxsq = new Jyxxsq();
        Jymxsq jymxsq = new Jymxsq();
        List<JymxsqCl> jymxsqClList = new ArrayList<JymxsqCl>();

        String tmp = checkorderutil.checkAll(jyxxsqList, jymxsqList, jyzfmxList, gsdm, Operation);
        // 校验通过，进行后续保存，以及开票功能
        if (null == tmp || tmp.equals("")) {
            //处理折扣行数据
            jymxsqClList = discountDealUtil.dealDiscount(jyxxsqList, jymxsq2List, jyzfmxList, gsdm);
            //备注处理方式
            jyxxsqList = remarkUtil.dealRemark(jyxxsqList, jyzfmxList, gsdm);
            //保存申请及明细数据
            String tmp2 = saveorderdata.saveAllData(jyxxsqList, jymxsqList, jyzfmxList, jymxsqClList);
            // 保存操作成功与否
            if (null != tmp2 && !tmp2.equals("")) {
                defaultResult.setReturnCode("9999");
                defaultResult.setReturnMessage(tmp2);
                result = XmlJaxbUtils.toXml(defaultResult);

            } else {
                defaultResult.setReturnCode("0000");
                defaultResult.setReturnMessage("交易信息保存成功");
                result = XmlJaxbUtils.toXml(defaultResult);
            }
        } else {
            defaultResult.setReturnCode("9999");
            defaultResult.setReturnMessage(tmp);
            result = XmlJaxbUtils.toXml(defaultResult);
            logger.debug("封装校验不通过信息" + result);
        }
        return result;
    }

    /**
     * 处理交易信息xml
     *
     * @param gsdm
     * @param OrderData
     * @return
     */
    private Map dealOperation02(String gsdm, String OrderData) {
        Document xmlDoc = null;
        List<Jyxxsq> jyxxsqList = new ArrayList();
        List<Jymxsq> jymxsqList = new ArrayList();
        Map params1 = new HashMap();
        params1.put("gsdm", gsdm);
        Map rsMap = new HashMap();
        Yh yh = yhservice.findOneByParams(params1);
        int lrry = yh.getId();
        OMElement root = null;
        List<Jyzfmx> jyzfmxList = new ArrayList<Jyzfmx>();
        try {
            xmlDoc = DocumentHelper.parseText(OrderData);
            root = XmlMapUtils.xml2OMElement(OrderData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map rootMap = XmlMapUtils.xml2Map(root, "Order");
        // 开票点代码
        String clientNO = (String) rootMap.get("ClientNO");

        // 交易流水号
        String serialNumber = (String) rootMap.get("SerialNumber");

        // 发票种类代码
        String invType = (String) rootMap.get("InvType");
        if (invType.equals("01")) {
            invType = "0";
        } else if (invType.equals("02")) {
            invType = "1";
        }
        // 发票业务类型
        String serviceType = (String) rootMap.get("ServiceType");

        // 开票人
        String drawer = (String) rootMap.get("Drawer");
        if (null == drawer) {
            drawer = "";
        }
        // 收款人
        String payee = (String) rootMap.get("Payee");
        if (null == payee) {
            payee = "";
        }
        // 复核人
        String reviewer = (String) rootMap.get("Reviewer");
        if (null == reviewer) {
            reviewer = "";
        }

        String sjly = String.valueOf(rootMap.get("DataSource"));
        if (null == sjly || sjly.equals("") || sjly.equals("null")) {
            sjly = "";
        }

        String openid = String.valueOf(rootMap.get("OpenId"));
        if (null == openid || openid.equals("") || openid.equals("null")) {
            openid = "";
        }

        // 销方信息
        Map sellerMap = (Map) rootMap.get("Seller");
        String identifier = (String) sellerMap.get("Identifier");
        String name = (String) sellerMap.get("Name");
        String address = (String) sellerMap.get("Address");
        String telephoneNo = (String) sellerMap.get("TelephoneNo");
        String bank = (String) sellerMap.get("Bank");
        String bankAcc = (String) sellerMap.get("BankAcc");
        // 明细信息

        List<Element> xnList = xmlDoc.selectNodes("Request/OrderSize/Order");
        if (null != xnList && xnList.size() > 0) {
            for (Element xn : xnList) {
                Jyxxsq jyxxsq = new Jyxxsq();
                Element orderMainMap = (Element) xn.selectSingleNode("OrderMain");
                // 订单号
                String orderNo = "";
                if (null != orderMainMap.selectSingleNode("OrderNo")
                        && !orderMainMap.selectSingleNode("OrderNo").equals("")) {
                    orderNo = orderMainMap.selectSingleNode("OrderNo").getText();
                }
                // 是否打印清单 1 打印清单 0 不打印清单
                String invoiceList = "0";
                if (null != orderMainMap.selectSingleNode("InvoiceList")
                        && !orderMainMap.selectSingleNode("InvoiceList").equals("")) {
                    invoiceList = orderMainMap.selectSingleNode("InvoiceList").getText();
                }

                // 是否自动拆分（1、拆分；0、不拆分）
                String invoiceSplit = "1";
                if (null != orderMainMap.selectSingleNode("InvoiceSplit")
                        && !orderMainMap.selectSingleNode("InvoiceSplit").equals("")) {
                    invoiceSplit = orderMainMap.selectSingleNode("InvoiceSplit").getText();
                }
                //是否打印1打印，0不打印
                String InvoiceSfdy = "1";
                if (null != orderMainMap.selectSingleNode("InvoiceSfdy")
                        && !orderMainMap.selectSingleNode("InvoiceSfdy").equals("")) {
                    InvoiceSfdy = orderMainMap.selectSingleNode("InvoiceSfdy").getText();
                }
                // 订单日期
                String orderDate = "";
                if (null != orderMainMap.selectSingleNode("OrderDate")
                        && !orderMainMap.selectSingleNode("OrderDate").equals("")) {
                    orderDate = orderMainMap.selectSingleNode("OrderDate").getText();
                }

                // 征税方式
                String chargeTaxWay = "";
                if (null != orderMainMap.selectSingleNode("ChargeTaxWay")
                        && !orderMainMap.selectSingleNode("ChargeTaxWay").equals("")) {
                    chargeTaxWay = orderMainMap.selectSingleNode("ChargeTaxWay").getText();
                }

                // 价税合计
                String totalAmount = "";
                if (null != orderMainMap.selectSingleNode("TotalAmount")
                        && !orderMainMap.selectSingleNode("TotalAmount").equals("")) {
                    totalAmount = orderMainMap.selectSingleNode("TotalAmount").getText();
                }

                // 含税标志
                String taxMark = "";
                if (null != orderMainMap.selectSingleNode("TaxMark")
                        && !orderMainMap.selectSingleNode("TaxMark").equals("")) {
                    taxMark = orderMainMap.selectSingleNode("TaxMark").getText();
                }

                // 备注
                String remark = "";
                if (null != orderMainMap.selectSingleNode("Remark")
                        && !orderMainMap.selectSingleNode("Remark").equals("")) {
                    remark = orderMainMap.selectSingleNode("Remark").getText();
                }

                String ExtractedCode = "";
                if (null != orderMainMap.selectSingleNode("ExtractedCode")
                        && !orderMainMap.selectSingleNode("ExtractedCode").equals("")) {
                    ExtractedCode = orderMainMap.selectSingleNode("ExtractedCode").getText();
                }
                String khh = "";
                // 保存主表信息
                jyxxsq.setKpddm(clientNO);
                jyxxsq.setJylsh(serialNumber);
                jyxxsq.setFpzldm(invType);
                jyxxsq.setKpr(drawer);
                jyxxsq.setSkr(payee);
                jyxxsq.setFhr(reviewer);
                jyxxsq.setOpenid(openid);
                jyxxsq.setXfsh(identifier);
                jyxxsq.setXfmc(name);
                jyxxsq.setXfdz(address);
                jyxxsq.setXfdh(telephoneNo);
                jyxxsq.setXfyh(bank);
                jyxxsq.setXfyhzh(bankAcc);
                jyxxsq.setDdh(orderNo);
                jyxxsq.setSfdyqd(invoiceList);
                jyxxsq.setSfcp(invoiceSplit);
                jyxxsq.setSfdy(InvoiceSfdy);
                SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    jyxxsq.setDdrq(orderDate == null ? new Date() : sim.parse(orderDate));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                jyxxsq.setZsfs(chargeTaxWay);
                jyxxsq.setJshj(Double.valueOf(totalAmount));
                jyxxsq.setHsbz(taxMark);
                jyxxsq.setBz(remark);

                jyxxsq.setKhh(khh);
                jyxxsq.setYkpjshj(Double.valueOf("0.00"));
                jyxxsq.setYxbz("1");
                jyxxsq.setLrsj(new Date());
                jyxxsq.setLrry(lrry);
                jyxxsq.setXgry(lrry);
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setXgsj(new Date());
                jyxxsq.setGsdm(gsdm);
                if (sjly.equals("") || null == sjly) {
                    jyxxsq.setSjly("1");
                } else {
                    jyxxsq.setSjly(sjly);
                }

                jyxxsq.setClztdm("00");
                if(null != ExtractedCode && !ExtractedCode.equals("")){
                    jyxxsq.setTqm(ExtractedCode);
                }
                jyxxsqList.add(jyxxsq);
                // List orderDetailsList = (List)
                // orderMainMap.get("OrderDetails");
                Element OrderDetails = (Element) xn.selectSingleNode("OrderDetails");
                List<Element> orderDetailsList = (List<Element>) OrderDetails.elements("ProductItem");
                if (null != orderDetailsList && orderDetailsList.size() > 0) {
                    int spmxxh = 1;
                    for (Element orderDetails : orderDetailsList) {
                        Jymxsq jymxsq = new Jymxsq();
                        // Map ProductItem = (Map) orderDetailsList.get(j);
                        //spmxxh++;
                        // 商品代码
                        String ProductCode = "";
                        if (null != orderDetails.selectSingleNode("ProductCode")
                                && !orderDetails.selectSingleNode("ProductCode").equals("")) {
                            ProductCode = orderDetails.selectSingleNode("ProductCode").getText();
                        }

                        jymxsq.setSpdm(ProductCode);
                        // 商品名称
                        String ProductName = "";
                        if (null != orderDetails.selectSingleNode("ProductName")
                                && !orderDetails.selectSingleNode("ProductName").equals("")) {
                            ProductName = orderDetails.selectSingleNode("ProductName").getText();
                        }

                        jymxsq.setSpmc(ProductName);
                        jymxsq.setDdh(jyxxsq.getDdh());
                        jymxsq.setHsbz(jyxxsq.getHsbz());
                        // 发票行性质
                        String RowType = "";
                        if (null != orderDetails.selectSingleNode("RowType")
                                && !orderDetails.selectSingleNode("RowType").equals("")) {
                            RowType = orderDetails.selectSingleNode("RowType").getText();
                        }

                        jymxsq.setFphxz(RowType);
                        // 商品规格型号
                        String Spec = "";
                        if (null != orderDetails.selectSingleNode("Spec")
                                && !orderDetails.selectSingleNode("Spec").equals("")) {
                            Spec = orderDetails.selectSingleNode("Spec").getText();
                        }

                        jymxsq.setSpggxh(Spec);
                        // 商品单位
                        String Unit = "";
                        if (null != orderDetails.selectSingleNode("Unit")
                                && !orderDetails.selectSingleNode("Unit").equals("")) {
                            Unit = orderDetails.selectSingleNode("Unit").getText();
                        }

                        jymxsq.setSpdw(Unit);
                        // 商品数量
                        String Quantity = "";
                        if (null != orderDetails.selectSingleNode("Quantity")
                                && !orderDetails.selectSingleNode("Quantity").equals("")) {
                            Quantity = orderDetails.selectSingleNode("Quantity").getText();
                            try {
                                jymxsq.setSps(Double.valueOf(Quantity));
                            } catch (Exception e) {
                                jymxsq.setSps(null);
                            }

                        }

                        // 商品单价
                        String UnitPrice = "";
                        if (null != orderDetails.selectSingleNode("UnitPrice")
                                && !orderDetails.selectSingleNode("UnitPrice").equals("")) {
                            UnitPrice = orderDetails.selectSingleNode("UnitPrice").getText();
                            try {
                                jymxsq.setSpdj(Double.valueOf(UnitPrice));
                            } catch (Exception e) {
                                jymxsq.setSpdj(null);
                            }
                        }

                        // 商品金额
                        String Amount = "";
                        if (null != orderDetails.selectSingleNode("Amount")
                                && !orderDetails.selectSingleNode("Amount").equals("")) {
                            Amount = orderDetails.selectSingleNode("Amount").getText();
                            jymxsq.setSpje(Double.valueOf(Amount));
                        }

                        // 扣除金额
                        String DeductAmount = "";
                        if (null != orderDetails.selectSingleNode("DeductAmount")
                                && !orderDetails.selectSingleNode("DeductAmount").equals("")) {
                            DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
                            jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
                                    : Double.valueOf(DeductAmount));
                        }

                        String TaxRate = "";
                        if (null != orderDetails.selectSingleNode("TaxRate")
                                && !orderDetails.selectSingleNode("TaxRate").equals("")) {
                            TaxRate = orderDetails.selectSingleNode("TaxRate").getText();
                            jymxsq.setSpsl(Double.valueOf(TaxRate));
                        }

                        String TaxAmount = "";
                        if (null != orderDetails.selectSingleNode("TaxAmount")
                                && !orderDetails.selectSingleNode("TaxAmount").equals("")) {
                            TaxAmount = orderDetails.selectSingleNode("TaxAmount").getText();
                            try {
                                jymxsq.setSpse(Double.valueOf(TaxAmount));
                            } catch (Exception e) {
                                jymxsq.setSpse(null);
                            }

                        }

                        String MxTotalAmount = "";
                        if (null != orderDetails.selectSingleNode("MxTotalAmount")
                                && !orderDetails.selectSingleNode("MxTotalAmount").equals("")) {
                            MxTotalAmount = orderDetails.selectSingleNode("MxTotalAmount").getText();
                            jymxsq.setJshj(Double.valueOf(MxTotalAmount));
                        }

                        jymxsq.setSpmxxh(spmxxh);
                        if (RowType.equals("2")) {//如果为被折扣行，则明细序号不变，反之明细序号加1

                        } else {
                            spmxxh++;
                        }
                        jymxsq.setKkjje(Double.valueOf(MxTotalAmount));
                        jymxsq.setYkjje(0d);
                        String VenderOwnCode = "";
                        if (null != orderDetails.selectSingleNode("VenderOwnCode")
                                && !orderDetails.selectSingleNode("VenderOwnCode").equals("")) {
                            VenderOwnCode = orderDetails.selectSingleNode("VenderOwnCode").getText();
                        }
                        jymxsq.setSpzxbm(VenderOwnCode);

                        String PolicyMark = "";
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
                        jymxsq.setYhzcmc(PolicyName);

                        jymxsq.setGsdm(gsdm);
                        jymxsq.setLrry(lrry);
                        jymxsq.setLrsj(new Date());
                        jymxsq.setXgry(lrry);
                        jymxsq.setXgsj(new Date());
                        jymxsq.setYxbz("1");
                        jymxsqList.add(jymxsq);

                    }

                }
                // 获取参数中对应的支付信息
                Element payments = (Element) xn.selectSingleNode("Payments");
                if (null != payments && !payments.equals("")) {
                    List<Element> paymentItemList = (List<Element>) payments.elements("PaymentItem");

                    if (null != paymentItemList && paymentItemList.size() > 0) {
                        for (Element PaymentItem : paymentItemList) {
                            Jyzfmx jyzfmx = new Jyzfmx();
                            String zffsDm = "";
                            if (null != PaymentItem.selectSingleNode("PayCode")
                                    && !PaymentItem.selectSingleNode("PayCode").equals("")) {
                                zffsDm = PaymentItem.selectSingleNode("PayCode").getText();
                                jyzfmx.setZffsDm(zffsDm);
                            }
                            String zfje = "";
                            if (null != PaymentItem.selectSingleNode("PayPrice")
                                    && !PaymentItem.selectSingleNode("PayPrice").equals("")) {
                                zfje = PaymentItem.selectSingleNode("PayPrice").getText();
                                jyzfmx.setZfje(Double.valueOf(zfje));
                            }
                            jyzfmx.setGsdm(gsdm);
                            jyzfmx.setDdh(jyxxsq.getDdh());
                            jyzfmx.setLrry(lrry);
                            jyzfmx.setLrsj(new Date());
                            jyzfmx.setXgry(lrry);
                            jyzfmx.setXgsj(new Date());
                            jyzfmxList.add(jyzfmx);
                        }

                    }
                }

            }
        }
        rsMap.put("jyxxsqList", jyxxsqList);
        rsMap.put("jymxsqList", jymxsqList);
        rsMap.put("jyzfmxList", jyzfmxList);
        return rsMap;
    }

    /**
     * 当客户未上传已初始化信息时，查询所平台配置的信息
     *
     * @param jyxxsqList
     * @param gsdm
     * @return
     */
    private List<Jyxxsq> addMoreDate(List<Jyxxsq> jyxxsqList, String gsdm) {
        //Map resultMap = new HashMap();
        List<Jyxxsq> resultList = new ArrayList<Jyxxsq>();
        String kpddm = "";
        String fpje = "";
        String fpzldm = "";
        for (int i = 0; i < jyxxsqList.size(); i++) {
            Jyxxsq jyxxsq = jyxxsqList.get(i);
            kpddm = jyxxsq.getKpddm();
            Map params = new HashMap();
            params.put("kpddm", kpddm);
            params.put("gsdm", gsdm);
            Skp skp = skpservice.findOneByParams(params);
            if (null != skp && !skp.equals("")) {
                jyxxsq.setXfid(skp.getXfid());
                jyxxsq.setSkpid(skp.getId());
            }
            resultList.add(jyxxsq);
        }
        return resultList;
    }
}
