package com.rjxx.taxease.service;

import com.rjxx.taxease.vo.*;
import com.rjxx.taxeasy.bizcomm.utils.URLUtils;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.utils.ResponseUtils;
import com.rjxx.utils.SignUtils;
import com.rjxx.utils.XmlJaxbUtils;
import com.rjxx.utils.XmlUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HanderJysjxxService {

    @Autowired
    private GsxxService gsxxservice;
    @Autowired
    private JyxxService jyxxservice;
    @Autowired
    private JyxxsqService jyxxsqService;
    @Autowired
    private SkpService skpService;
    @Autowired
    private XfService xfService;
    @Autowired
    private CszbService cszbService;
    @Autowired
    private SpvoService spvoService;

    /**
     * 处理交易数据上传
     */
    @Transactional
    public String uploadOrder(final String AppId, final String Sign, final String OrderData) {
        final Map resultMap = new HashMap();
        try {
            String result = handerMessage(AppId, Sign, OrderData);
            return result;
            //resultMap.put("result", result);
        } catch (Exception e) {
            e.printStackTrace();
            String result = ResponseUtils.printFailure("9999:" + e.getMessage());
            //resultMap.put("result", result);
            throw new RuntimeException(result);
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        //String result = (String) resultMap.get("result");
        //return result;
    }

    // 处理上传的交易信息 appkey == AppId secret == key
    @Transactional
    public String handerMessage(String AppId, String Sign, String OrderData) {
        String result = "";
        Map tempMap = new HashMap();
        tempMap.put("appkey", AppId);
        Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
        if (gsxxBean == null) {
            return ResponseUtils.printFailure1("9060:" + AppId + "," + Sign);
        } else {
            // int xfid = gsxxBean.get("yhjg");
            // XfBean xfBean = XfBean.dao.findFirst("select * from t_xf where
            // xfid = " + xfid);
            // 校验数据是否被篡改过
            String key = gsxxBean.getSecretKey();
            String signSourceData = "data=" + OrderData + "&key=" + key;
            String newSign = DigestUtils.md5Hex(signSourceData);
            if (!Sign.equalsIgnoreCase(newSign)) {
                result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9060:签名不通过</ReturnMessage> \n</Responese>";
                return result;
            }
            String gsdm = gsxxBean.getGsdm();
            OMElement root = null;
            try {
                root = xml2OMElement(OrderData);
            } catch (Exception e) {
                e.printStackTrace();
                result = e.getMessage();
                return result;
            }
            List rootList = xml2List(root, "Row");// 获取xml中的row标签下的数据
            Map jyxxMap = null;
            for (int i = 0; i < rootList.size(); i++) {
                String orderNo = "";
                String orderTime = "";
                Double price = -9999.0; // price = Double.valueOf("0");
                String storeNo = "";
                jyxxMap = (Map) rootList.get(i);
                // 分装对应的bean
                orderNo = (String) jyxxMap.get("OrderNo");// 订单号 必选
                orderTime = (String) jyxxMap.get("OrderTime");// 订单日期 必选
                storeNo = (String) jyxxMap.get("StoreNo"); // 门店编号 必选
                if ((String) jyxxMap.get("Price") == null || jyxxMap.get("Price").equals("")) {

                } else {
                    price = Double.valueOf((String) jyxxMap.get("Price")); // 金额
                    // 必选
                }
                if (orderNo == null || orderNo.trim().equals("")) {
                    result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9104:第" + (i + 1)
                            + "行订单号不能为空</ReturnMessage> \n</Responese>";
                    return result;
                } else if ((orderTime == null || orderTime.trim().equals(""))) {
                    result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9115:第" + (i + 1)
                            + "行订单时间不能为空</ReturnMessage> \n</Responese>";
                    return result;
                } else if (price == -9999.0) {
                    result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9105:第" + (i + 1)
                            + "行金额不能为空</ReturnMessage> \n</Responese>";
                    return result;
                } else if (storeNo == null || storeNo.equals("")) {
                    result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9117:第" + (i + 1)
                            + "行门店号不能为空</ReturnMessage> \n</Responese>";
                    return result;
                }
            }
            for (int i = 0; i < rootList.size(); i++) {
                jyxxMap = (Map) rootList.get(i);
                // 分装对应的bean
                String orderNo = (String) jyxxMap.get("OrderNo");// 订单号 必选
                String orderTime = (String) jyxxMap.get("OrderTime");// 订单日期 必选
                String storeNo = (String) jyxxMap.get("StoreNo"); // 门店编号 必选
                Double price = Double.valueOf((String) jyxxMap.get("Price")); // 金额
                try {
                    Map skpMap = new HashMap();
                    skpMap.put("gsdm", gsdm);
                    skpMap.put("kpddm", storeNo);
                    Skp skp = skpService.findOneByParams(skpMap);

                    String kpr = skp.getKpr();
                    String fhr = skp.getFhr();
                    String skr = skp.getSkr();
                    String lxdh = skp.getLxdh();
                    String lxdz = skp.getLxdz();
                    String yhzh = skp.getYhzh();
                    String khyh = skp.getKhyh();

                    Integer xfid = skp.getXfid();
                    Xf xf = xfService.findOne(xfid);

                    String xfmc = xf.getXfmc();
                    String xfsh = xf.getXfsh();

                    InvoiceRequest request = new InvoiceRequest();
                    request.setClientNO(storeNo);
                    request.setDrawer(kpr);
                    request.setInvType("12");
                    request.setPayee(skr);
                    request.setReviewer(fhr);
                    request.setSerialNumber(orderNo);

                    Seller seller = new Seller();
                    seller.setIdentifier(xfsh);
                    seller.setName(xfmc);
                    seller.setTelephoneNo(lxdh);
                    seller.setAddress(lxdz);
                    seller.setBank(khyh);
                    seller.setBankAcc(yhzh);
                    request.setSeller(seller);

                    OrderMain orderMain = new OrderMain();
                    orderMain.setOrderNo(orderNo);
                    orderMain.setOrderDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime)));
                    orderMain.setTotalAmount(price.toString());
                    orderMain.setTaxMark("1");
                    orderMain.setExtractedCode(orderNo);

                    Order order = new Order();
                    order.setOrderMain(orderMain);

                    Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, skp.getId(), "dyspbmb");
                    Map spvoMap = new HashMap();
                    spvoMap.put("gsdm", gsdm);
                    spvoMap.put("spdm", cszb.getCsz());
                    Spvo spvo = spvoService.findOneSpvo(spvoMap);

                    List<ProductItem> productItemList = new ArrayList<>();
                    ProductItem productItem = new ProductItem();
                    productItem.setAmount(price.toString());
                    productItem.setProductName(spvo.getSpmc());
                    productItem.setProductCode(spvo.getSpbm());
                    productItem.setPolicyMark(spvo.getYhzcbs());
                    productItem.setTaxRateMark(spvo.getLslbz());
                    productItem.setPolicyName(spvo.getYhzcmc());
                    productItem.setTaxRate(String.valueOf(spvo.getSl()));
                    productItem.setMxTotalAmount(price.toString());
                    productItem.setSpec("");
                    productItem.setTaxAmount("0");
                    productItemList.add(productItem);

                    order.setProductItem(productItemList);
                    List<Order> orderList = new ArrayList<>();
                    orderList.add(order);
                    request.setOrder(orderList);

                    String xml = XmlJaxbUtils.toXml(request);
                    String resultxml = HttpUrlPost(xml, AppId, key, "02");
                    Map<String, Object> resultMap = null;
                    try {
                        resultMap = XmlUtil.xml2Map(resultxml);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String returnMsg = resultMap.get("ReturnMessage").toString();
                    String returnCode = resultMap.get("ReturnCode").toString();
                    if ("9999".equals(returnCode)) {
                        result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + returnMsg + "</ReturnMessage> \n</Responese>";
                        return result;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = "交易数据上传失败：" + e;
                    //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    //return result;
                    throw new RuntimeException(result);
                }
            }
            result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>交易数据上传成功</ReturnMessage> \n</Responese>";
        }
        return result;
    }

    private static OMElement xml2OMElement(String xml) throws XMLStreamException, UnsupportedEncodingException {
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
        StAXBuilder builder = new StAXOMBuilder(xmlStream);
        OMElement documentElement = builder.getDocumentElement();
        return documentElement;
    }

    private static Map xml2Map(OMElement doc, String listTagNames) {
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

    private static List xml2List(OMElement doc, String listTagNames) {
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

    private static String str2Trim(String str) {
        return "".equals(str) ? null : str.trim();
    }

    public static String printFailure(String errorMessage) {
        return "<Responese>\n  <ReturnCode>9999</ReturnCode>\n" + "  <ReturnMessage>" + errorMessage
                + "</ReturnMessage>\n</Responese>";
    }

    public static String HttpUrlPost(String QueryData, String AppId, String key, String type) {
        String result = "";
        try {
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            Client client = dcf.createClient(URLUtils.WS_URL);
            String methodName = "UploadOrderData";
            String sign = SignUtils.getSign(QueryData, key);
            Object[] objects = client.invoke(methodName, AppId, sign, type, QueryData);
            //输出调用结果
            result = objects[0].toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            String OrderData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<Request>\n" +
                    "<Row> \n" +
                    "  <OrderNo>20171019baxi"+new Random().nextInt(9)+new Random().nextInt(9)+"</OrderNo>\n" +
                    "  <OrderTime>20161110093912</OrderTime>\n" +
                    "  <Price>100</Price>\n" +
                    "  <StoreNo>baxi</StoreNo>\n" +
                    "  </Row>\n" +
                    "<Row> \n" +
                    "  <OrderNo>20171019yidu"+new Random().nextInt(9)+new Random().nextInt(9)+"</OrderNo>\n" +
                    "  <OrderTime>20161110093916</OrderTime>\n" +
                    "  <Price>10</Price>\n" +
                    "  <StoreNo>yidu</StoreNo>\n" +
                    "  </Row>\n" +
                    "</Request>\n";
            String wsdlurl = "http://test.datarj.com/webService/services/invoiceService?wsdl";
            String key = "0f2aa080911da0adcfc5f630e9d20e1a";
            String methodname = "UploadOrder";
            String appid = "RJ5689ea2d0482";
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            Client client = dcf.createClient(wsdlurl);
            String sign = SignUtils.getSign(OrderData, key);
            System.out.println(sign);
            Object[] objects = client.invoke(methodname, appid, sign, OrderData);
            //输出调用结果
            String result = objects[0].toString();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
