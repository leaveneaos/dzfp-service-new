package com.rjxx.taxease.ws;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

/**
 * Created by Administrator on 2016/12/15.
 */
public class WebServiceTestCall {

   
    public static void testCallQuery() throws Exception {
    	 String QueryData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "<Request>\n" +
                 "  <!--以下查询条件可任选-->\n" +
                 "  <SerialNumber>YJ-2016-00084849</SerialNumber>\n" +
                 "  <!--SerialNumber可选，交易流水号String20-->\n" +
                 "  <OrderNumber></OrderNumber>\n" +
                 "  <!--OrderNumber可选，来源系统订单号，String20-->\n" +
                 "  <BuyerName></BuyerName>\n" +
                 "  <!--BuyerName可选，购买方名称String100-->\n" +
                 "  <BuyerTel></BuyerTel>\n" +
                 "  <!--BuyerTel可选，购买方电话String20-->\n" +
                 "</Request>\n";
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient(WS_URL);
        String methodName = "CallQuery";
        String AppKey = "RJ9b458d60149c";
        String key ="85a7764f0372dd7b04067e02985830d7";
        String Secret = getSign(QueryData,key);
        String InvoiceData = QueryData;
        Object[] objects = client.invoke(methodName, AppKey, Secret, InvoiceData);
        //输出调用结果
        System.out.println(objects[0].toString());
    }
 
    public static void testUploadOrder() throws Exception {
    	String OrderData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Request>\n" +
        		"<Row> \n"+
                "  <OrderNo>ME24156113</OrderNo>\n" +
                "  <!--OrderNo必选，订单号，String20-->\n" +
                "  <OrderTime>20161110093914</OrderTime>\n" +
                "  <!--OrderTime必选，订单时间，String14-->\n" +
                "  <Price>23</Price>\n" +
                "  <!--Price必选，金额，Double(10,2) -->\n" +
                "  <Sign>b0c87cce86a4dfebedc05d83e7f76790</Sign>\n" +
                "  <!--Sign必选，签名串String32-->\n" +
                "  <StoreNo>8001</StoreNo>\n" +
                "  <!--StoreNo可选，门店编号String20-->\n" +
                "  </Row>\n" +
                "<Row> \n"+
                "  <OrderNo>ME24156112</OrderNo>\n" +
                "  <!--OrderNo必选，订单号，String20-->\n" +
                "  <OrderTime>20161110093916</OrderTime>\n" +
                "  <!--OrderTime必选，订单时间，String14-->\n" +
                "  <Price>22</Price>\n" +
                "  <!--Price必选，金额，Double(10,2) -->\n" +
                "  <Sign>b0c87cce86a4dfebedc05d83e7aaaaaa</Sign>\n" +
                "  <!--Sign必选，签名串String32-->\n" +
                "  <StoreNo>8001</StoreNo>\n" +
                "  <!--StoreNo可选，门店编号String20-->\n" +
                "  </Row>\n" +
                "</Request>\n";
       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "UploadOrder";
       String AppKey = "RJ5689ea2d0482";
       String key ="0f2aa080911da0adcfc5f630e9d20e1a";
       String Secret = getSign(OrderData,key);
       String InvoiceData = OrderData;
       Object[] objects = client.invoke(methodName, AppKey, Secret, InvoiceData);
       //输出调用结果
       System.out.println(objects[0].toString());
   }
    
    public static void testCallService() throws Exception {
    	String lzData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Request>\n" +
                "\t<SerialNumber>2016082412444500077</SerialNumber>\n" +
                "\t<InvType>12</InvType>\n" +
                "\t<InvoiceSplit>1</InvoiceSplit>\n" +
                "\t<ServiceType>0</ServiceType>\n" +
                "\t<Email>zhangbing@datarj.com</Email>\n" +
                "\t<IsSend>1</IsSend>\n" +
                "\t<ExtractedCode>a2127a</ExtractedCode>\n" +
                "\t<Recipient>张三</Recipient>\n" +
                "\t<Zip>200000</Zip>\n" +
                "\t<LetterAddress>上海市漕宝路</LetterAddress>\n" +
                "\t<Drawer>开票人</Drawer>\n" +
                "\t<Payee>收款人</Payee>\n" +
                "\t<Reviewer>复核人</Reviewer>\n" +
                "\t<CancelUser>作废人</CancelUser>\n" +
                "\t<CNNoticeNo></CNNoticeNo>\n" +
                "\t<CNDNCode></CNDNCode>\n" +
                "\t<CNDNNo></CNDNNo>\n" +
                "\t<InvoiceMain>\n" +
                "\t\t<OrderNumber>ME24156071</OrderNumber>\n" +
                "\t\t<OrderDate>2016-06-22 23:59:59</OrderDate>\n" +
                "\t\t<TotalAmount>5410.00</TotalAmount>\n" +
                "\t\t<TaxMark>0</TaxMark>\n" +
                "\t\t<Remark>这是备注</Remark>\n" +
                "\t\t<Seller>\n" +
                "\t\t\t<Identifier>91310101MA1FW0008P</Identifier>\n" +
                "\t\t\t<Name>上海百旺测试盘</Name>\n" +
                "\t\t\t<Address>某某路10号1203室</Address>\n" +
                "\t\t\t<TelephoneNo>021-55555555</TelephoneNo>\n" +
                "\t\t\t<Bank>中国建设银行打浦桥支行</Bank>\n" +
                "\t\t\t<BankAcc>123456789-0</BankAcc>\n" +
                "\t\t</Seller>\n" +
                "\t\t<Buyer>\n" +
                "\t\t\t<Identifier></Identifier>\n" +
                "\t\t\t<Name>购方名称</Name>\n" +
                "\t\t\t<Address>某某路20号203室</Address>\n" +
                "\t\t\t<TelephoneNo>13912345678</TelephoneNo>\n" +
                "\t\t\t<Bank>中国建设银行打浦桥支行</Bank>\n" +
                "\t\t\t<BankAcc>123456789-0</BankAcc>\n" +
                "\t\t</Buyer>\n" +
                "\t</InvoiceMain>\n" +
                "\t<InvoiceDetails count=\"2\">\n" +
                "\t\t<ProductItem>\n" +
                "\t\t\t<ProductCode>1000000000000000000</ProductCode>\n" +
                "\t\t\t<ProductName>商品1</ProductName>\n" +
                "\t\t\t<RowType>0</RowType>\n" +
                "\t\t\t<Spec>规格型号1</Spec>\n" +
                "\t\t\t<Unit>单位1</Unit>\n" +
                "\t\t\t<Quantity>1</Quantity>\n" +
                "\t\t\t<UnitPrice>1000.00</UnitPrice>\n" +
                "\t\t\t<Amount>1000.00</Amount>\n" +
                "\t\t\t<TaxRate>0.17</TaxRate>\n" +
                "\t\t\t<TaxAmount>170.00</TaxAmount>\n" +
                "\t\t</ProductItem>\n" +
                "\t\t<ProductItem>\n" +
                "\t\t\t<ProductCode>1000000000000000000</ProductCode>\n" +
                "\t\t\t<ProductName>商品2</ProductName>\n" +
                "\t\t\t<RowType>0</RowType>\n" +
                "\t\t\t<Spec>规格型号2</Spec>\n" +
                "\t\t\t<Unit>单位2</Unit>\n" +
                "\t\t\t<Quantity>2</Quantity>\n" +
                "\t\t\t<UnitPrice>2000.00</UnitPrice>\n" +
                "\t\t\t<Amount>4000.00</Amount>\n" +
                "\t\t\t<TaxRate>0.06</TaxRate>\n" +
                "\t\t\t<TaxAmount>240.00</TaxAmount>\n" +
                "\t\t</ProductItem>\n" +
                "\t</InvoiceDetails>\n" +
                "</Request>\n";
    	String qbhcData ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    			"<Request>"
    			+ "<SerialNumber>2016072412444500022</SerialNumber>"
    			+ "<InvType>12</InvType>"
    			+ "<ServiceType>1</ServiceType>"
    			+ "<CNNoticeNo>专用发票红票通知单号</CNNoticeNo>"
    			+ "<CNDNCode>131001570055</CNDNCode>"
    			+ "<CNDNNo>09103536</CNDNNo>"
    			+ "<TotalAmount>-5410.00</TotalAmount>"
    			+ "</Request>";
    	
    	String bfhcData ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<Request>"
    			+ "<SerialNumber>2016062412444500001</SerialNumber>"
    			+ "<InvType>12</InvType>"
    			+ "<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
    			+ "<ServiceType>1</ServiceType>"
    			+ "<CNNoticeNo>专用发票红票通知单号</CNNoticeNo>"
    			+ "<!--CNNoticeNo可选，专用发票红票通知单号String20，电子发票不需要填写-->"
    			+ "<CNDNCode>050003523456</CNDNCode>"
    			+ "<!--CNDNCode，作废或红字发票必须，作废时对应的原始发票代码或红票对应蓝字发票代码String20-->"
    			+ "<CNDNNo>19111320</CNDNNo>"
    			+ "<!--CNDNNo，作废或红字发票必须，作废时对应的原始发票号码或红票对应蓝字发票号码String20-->"
    			+ "<TaxMark>0</TaxMark>"
    			+ "<!--TaxMark必须，明细的金额是否含税？（1、含税；0、不含税）-->"
    			+ "<InvoiceDetails count=\"1\">"
    					+ "<!--size收费明细商品或服务行数，必须与ProductItem数量一致-->"
    					+ "<ProductItem>"
    					+ "<ProductCode>3070401000000000000</ProductCode>"
    					+ "<!--ProductCode必选，商品代码String19 -->"
    					+ "<ProductName>餐费</ProductName>"
    					+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
    					+ "<RowType>0</RowType>"
    					+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
    					+ "<Spec>规格型号1</Spec>"
    					+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
    					+ "<Unit>单位1</Unit>"
    					+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
    					+ "<Quantity>-1</Quantity>"
    					+ "<!--Quantity可选，商品数量，负数，该栏目打印在发票上-->"
    					+ "<UnitPrice>70</UnitPrice>"
    					+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
    					+ "<Amount>-70</Amount>"
    					+ "<!--Amount必须，商品金额，负数，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
    					+ "<TaxRate>0.17</TaxRate>"
    					+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
    					+ "<TaxAmount>-11.9</TaxAmount>"
    					+ "<!--TaxAmount，商品税额，负数，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
    					+ "</ProductItem>"
    					+ "</InvoiceDetails>"
    					+ "</Request>";
       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "CallService";
       String AppKey = "RJ9b458d60149c";
       String key ="85a7764f0372dd7b04067e02985830d7";
       String Secret = getSign(bfhcData,key);
       String InvoiceData1 = bfhcData;
       Object[] objects = client.invoke(methodName, AppKey, Secret, InvoiceData1);
       //输出调用结果
       System.out.println(objects[0].toString());
   }
    
    private static String getSign(String QueryData,String key){
    	String signSourceData = "data=" + QueryData + "&key=" + key;
        String newSign =  DigestUtils.md5Hex(signSourceData);
        return newSign;
    }
    
    public static String WS_URL = "http://localhost:8080/dzfp-service-new/Service.asmx?wsdl";

    public static void main(String[] args) throws Exception {
    	//testCallQuery();
    	//testUploadOrder();
    	testCallService();

    }

}
