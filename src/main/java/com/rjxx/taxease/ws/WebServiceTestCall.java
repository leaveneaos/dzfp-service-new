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
                 "  <SerialNumber>20161228191816565029</SerialNumber>\n" +
                 "  <!--SerialNumber可选，交易流水号String20-->\n" +
                 "  <OrderNumber></OrderNumber>\n" +
                 "  <!--OrderNumber可选，来源系统订单号，String20-->\n" +
                 "  <ExtractCode></ExtractCode>\n" +
                 "  <!--ExtractCode可选，提取码String20-->\n" +
                 "</Request>\n";
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient(WS_URL);
        String methodName = "CallQuery";
        String AppKey = "RJc68ad2517ad3";
        String key ="105c29bc32a1319b945170e25a19f1ea";
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
                "  <OrderNo>ME241561114</OrderNo>\n" +
                "  <!--OrderNo必选，订单号，String20-->\n" +
                "  <OrderTime>20161220103914</OrderTime>\n" +
                "  <!--OrderTime必选，订单时间，String14-->\n" +
                "  <Price>438.33</Price>\n" +
                "  <!--Price必选，金额，Double(10,2) -->\n" +
                "  <Sign>b0c87cce86a4dfebedc05d83e7f76790</Sign>\n" +
                "  <!--Sign必选，签名串String32-->\n" +
                "  <StoreNo>8001</StoreNo>\n" +
                "  <!--StoreNo可选，门店编号String20-->\n" +
                "  </Row>\n" +
                "<Row> \n"+
                "  <OrderNo>ME24156115</OrderNo>\n" +
                "  <!--OrderNo必选，订单号，String20-->\n" +
                "  <OrderTime>20161220112216</OrderTime>\n" +
                "  <!--OrderTime必选，订单时间，String14-->\n" +
                "  <Price>438.99</Price>\n" +
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
                "\t<SerialNumber>2016082512444500078</SerialNumber>\n" +
                "\t<InvType>12</InvType>\n" +
                "\t<InvoiceSplit>1</InvoiceSplit>\n" +
                "\t<ServiceType>0</ServiceType>\n" +
                "\t<Email>zhangbing@datarj.com</Email>\n" +
                "\t<IsSend>1</IsSend>\n" +
                "\t<ExtractedCode>d2127a</ExtractedCode>\n" +
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
    			+ "<SerialNumber>2016083412444500022</SerialNumber>"
    			+ "<InvType>12</InvType>"
    			+ "<ServiceType>1</ServiceType>"
    			+ "<CNNoticeNo>专用发票红票通知单号</CNNoticeNo>"
    			+ "<CNDNCode>131001570055</CNDNCode>"
    			+ "<CNDNNo>09103961</CNDNNo>"
    			+ "<TotalAmount>-5410.00</TotalAmount>"
    			+ "</Request>";
    	
    	String bfhcData ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<Request>"
    			+ "<SerialNumber>2016062412444500005</SerialNumber>"
    			+ "<InvType>12</InvType>"
    			+ "<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
    			+ "<ServiceType>1</ServiceType>"
    			+ "<CNNoticeNo>专用发票红票通知单号</CNNoticeNo>"
    			+ "<!--CNNoticeNo可选，专用发票红票通知单号String20，电子发票不需要填写-->"
    			+ "<CNDNCode>131001570055</CNDNCode>"
    			+ "<!--CNDNCode，作废或红字发票必须，作废时对应的原始发票代码或红票对应蓝字发票代码String20-->"
    			+ "<CNDNNo>09103958</CNDNNo>"
    			+ "<!--CNDNNo，作废或红字发票必须，作废时对应的原始发票号码或红票对应蓝字发票号码String20-->"
    			+ "<TaxMark>0</TaxMark>"
    			+ "<!--TaxMark必须，明细的金额是否含税？（1、含税；0、不含税）-->"
    			+ "\t<InvoiceDetails count=\"2\">\n" +
                 "\t\t<ProductItem>\n" +
                 "\t\t\t<ProductCode>1000000000000000000</ProductCode>\n" +
                 "\t\t\t<ProductName>商品1</ProductName>\n" +
                 "\t\t\t<RowType>0</RowType>\n" +
                 "\t\t\t<Spec>规格型号1</Spec>\n" +
                 "\t\t\t<Unit>单位1</Unit>\n" +
                 "\t\t\t<Quantity>-1</Quantity>\n" +
                 "\t\t\t<UnitPrice>500.00</UnitPrice>\n" +
                 "\t\t\t<Amount>-500</Amount>\n" +
                 "\t\t\t<TaxRate>0.17</TaxRate>\n" +
                 "\t\t\t<TaxAmount>-85.00</TaxAmount>\n" +
                 "\t\t</ProductItem>\n" +
                 "\t\t<ProductItem>\n" +
                 "\t\t\t<ProductCode>1000000000000000000</ProductCode>\n" +
                 "\t\t\t<ProductName>商品2</ProductName>\n" +
                 "\t\t\t<RowType>0</RowType>\n" +
                 "\t\t\t<Spec>规格型号2</Spec>\n" +
                 "\t\t\t<Unit>单位2</Unit>\n" +
                 "\t\t\t<Quantity>-2</Quantity>\n" +
                 "\t\t\t<UnitPrice>1000.00</UnitPrice>\n" +
                 "\t\t\t<Amount>-2000.00</Amount>\n" +
                 "\t\t\t<TaxRate>0.06</TaxRate>\n" +
                 "\t\t\t<TaxAmount>-120.00</TaxAmount>\n" +
                   "\t\t</ProductItem>\n" 
    					+ "</InvoiceDetails>"
    					+ "</Request>";
       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "CallService";
       String AppKey = "RJ9b458d60149c";
       String key ="f97e385c9bfeec4d72fbecb8deb3ed5d";
       String Secret = getSign(qbhcData,key);
       String InvoiceData1 = qbhcData;
       Object[] objects = client.invoke(methodName, AppKey, Secret, InvoiceData1);
       //输出调用结果
       System.out.println(objects[0].toString());
   }
    
    public static void testinvoiceUpload() throws Exception {
    	final String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Request>\n" +
    			"    <TotalAmount>1</TotalAmount>\n" +
    			"    <SerialNumber>7000138809059495305</SerialNumber>\n" +
    			"    <Main>\n" +
    			"        <OrderNumber>70001388990</OrderNumber>\n" +
    			"        <OrderDate>2016-07-11</OrderDate>\n" +
    			"        <OrderTime>06:08:41</OrderTime>\n" +
    			"        <Seller>\n" +
    			"            <Identifier>91310101MA1FW0008P</Identifier>\n" +
    			"            <Name>上海百旺测试盘</Name>\n" +
    			"            <Address>Shanghai JingAn District West Nanjing Road No.15, JingAn JiaLi Center Room 607</Address>\n" +
    			"            <TelephoneNumber>021-xxxxxxxx</TelephoneNumber>\n" +
    			"        </Seller>\n" +
    			"        <Buyer>\n" +
    			"            <Identifier/>\n" +
    			"            <Name>王五</Name>\n" +
    			"            <IsSend>1</IsSend>\n" +
    			"            <Payment>ALIPAY-FASTPAY</Payment>\n" +
    			"            <Address>&#x6c5f;&#x82cf;&#x7701;,&#x5357;&#x4eac;&#x5e02; CN &#x8f6f;&#x4ef6;&#x5927;&#x9053;108&#x53f7;&#x84dd;&#x7b79;&#x8c37;2&#x680b;1&#x5355;&#x5143;401 ;;;;210012;;;; &#x9648;&#x4e16;&#x6770;</Address>\n" +
    			"            <TelephoneNumber>18811049101</TelephoneNumber>\n" +
    			"            <Email>zhangbing@datarj.com</Email>\n" +
    			"        </Buyer>\n" +
    			"    </Main>\n" +
    			"    <Details size=\"2\">\n" +
    			"        <ProductItem>\n" +
    			"            <Description>Abercrombie and Fitch Apparel</Description>\n" +
    			"            <Unit>EACH</Unit>\n" +
    			"            <Quantity>1</Quantity>\n" +
    			"            <UnitPrice>1000</UnitPrice>\n" +
    			"            <Amount>1000.00</Amount>\n" +
    			"        </ProductItem>\n" +
    			"        <ProductItem>\n" +
    			"            <Description>Abercrombie and Fitch Apparel</Description>\n" +
    			"            <Unit>EACH</Unit>\n" +
    			"            <Quantity>1</Quantity>\n" +
    			"            <UnitPrice>1000</UnitPrice>\n" +
    			"            <Amount>1000.00</Amount>\n" +
    			"        </ProductItem>\n" +
    			"    </Details>\n" +
    			"</Request>\n";
       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "invoiceUpload";
     
       Object[] objects = client.invoke(methodName,xml);
       //输出调用结果
       System.out.println(objects[0].toString());
    }
    
    public static void testuploadOrderData() throws Exception {
    	final String xml03="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<Request>"
    			+ "<Row>"
    			+ "<ClientNO>kp005</ClientNO>"
    			+ "<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
    			+ "<OrderNo>ME24156075</OrderNo>"
    			+ "<!--OrderNo必选,每笔订单号必须唯一，订单号，String20-->"
    			+ "<OrderTime>2016-06-22 23:59:59</OrderTime>"
    			+ "<!--OrderTime必选，订单时间，String14-->"
    			+ "<TaxMark>1</TaxMark>"
				+ "<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->"
    			+ "<TotalAmount>5410.00</TotalAmount>"
    			+ "<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->"
    			+ "<InvType>1</InvType>"
    			+ "<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
    			+ "<Identifier>310105987654321</Identifier>"
    			+ "<!--Identifier可选，购买方税号String20，15、18或20位，该栏目打印在发票上，专用发票必须-->"
    			+ "<Name>购买方名称</Name>"
    			+ "<!--Name必须，购买方名称String100，该栏目打印在发票上-->"
    			+ "<Address>某某路20号203室</Address>"
    			+ "<!--Address可选，购买方地址String100，该栏目打印在发票上，专用发票必须-->"
    			+ "<TelephoneNo>13912345678</TelephoneNo>"
    			+ "<!--TelephoneNo可选，购买方电话String20，该栏目打印在发票上，专用发票必须-->"
    			+ "<Bank>中国建设银行打浦桥支行</Bank>"
    			+ "<!--Bank可选，购买方银行String100，该栏目打印在发票上，专用发票必须-->"
    			+ "<BankAcc>123456789-0</BankAcc>"
    			+ "<!--BankAcc可选，购买方银行账号String30，该栏目打印在发票上，专用发票必须-->"
    			+ "<Email>abc@163.com</Email>"
    			+ "<!--Email可选，客户接收电子发票的电子邮箱地址String50-->"
    			+ "<IsSend>1</IsSend>"
    			+ "<!--IsSend电子发票生成后是否发送电子邮件（1、是；0、否）-->"
    			+ "<Recipient>张三</Recipient>"
    			+ "<!--Recipient可选，纸质发票收件人姓名String50-->"
    			+ "<ReciAddress>收件人地址</ReciAddress>"
    			+ "<!--Address可选，纸质发票收件人地址String200-->"
    			+ "<Zip>200000</Zip>"
    			+ "<!--zip可选，纸质发票收件人邮编String10-->"
    			+ "</Row>"
    			+ "<Row>"
    			+ "<ClientNO>kp005</ClientNO>"
    			+ "<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
    			+ "<OrderNo>ME24156072</OrderNo>"
    			+ "<!--OrderNo必选，每笔订单号必须唯一，订单号，String20-->"
    			+ "<OrderTime>2016-06-22 23:59:59</OrderTime>"
    			+ "<!--OrderTime必选，订单时间，String14-->"
    			+ "<TaxMark>1</TaxMark>"
				+ "<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->"
    			+ "<TotalAmount>5410.00</TotalAmount>"
    			+ "<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->"
    			+ "<InvType>1</InvType>"
    			+ "<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
    			+ "<Identifier>310105987654321</Identifier>"
    			+ "<!--Identifier可选，购买方税号String20，15、18或20位，该栏目打印在发票上，专用发票必须-->"
    			+ "<Name>购买方名称</Name>"
    			+ "<!--Name必须，购买方名称String100，该栏目打印在发票上-->"
    			+ "<Address>某某路20号203室</Address>"
    			+ "<!--Address可选，购买方地址String100，该栏目打印在发票上，专用发票必须-->"
    			+ "<TelephoneNo>13912345678</TelephoneNo>"
    			+ "<!--TelephoneNo可选，购买方电话String20，该栏目打印在发票上，专用发票必须-->"
    			+ "<Bank>中国建设银行打浦桥支行</Bank>"
    			+ "<!--Bank可选，购买方银行String100，该栏目打印在发票上，专用发票必须-->"
    			+ "<BankAcc>123456789-0</BankAcc>"
    			+ "<!--BankAcc可选，购买方银行账号String30，该栏目打印在发票上，专用发票必须-->"
    			+ "<Email>abc@163.com</Email>"
    			+ "<!--Email可选，客户接收电子发票的电子邮箱地址String50-->"
    			+ "<IsSend>1</IsSend>"
    			+ "<!--IsSend电子发票生成后是否发送电子邮件（1、是；0、否）-->"
    			+ "<Recipient>张三</Recipient>"
    			+ "<!--Recipient可选，纸质发票收件人姓名String50-->"
    			+ "<ReciAddress>收件人地址</ReciAddress>"
    			+ "<!--Address可选，纸质发票收件人地址String200-->"
    			+ "<Zip>200000</Zip>"
    			+ "<!--zip可选，纸质发票收件人邮编String10-->"
    			+ "</Row>"
    			+ "</Request>";
    	
    	final String xml01 ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<Request>"
    			+ "<ClientNO>kp005</ClientNO>"
    			+ "<!--纸质票必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
    			+ "<SerialNumber>2017010913444500001</SerialNumber>"
    			+ "<!--SerialNumber必须，交易流水号String20，每次请求唯一值，不可重复，用于返回接口中与来源系统进行数据匹配-->"
    			+ "<InvType>01</InvType>"
    			+ "<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
    			+ "<ServiceType>0</ServiceType>"
    			+ "<!--ServiceType必须，发票业务类型（0、蓝字发票；1、红字发票；2、作废已开发票；3、作废空白发票；4、纸质发票换开 ），电子发票使用0、1、4-->"
    			+ "<Drawer>开票人</Drawer>"
    			+ "<!--Drawer必须，开票人String20，该栏目打印在发票上-->"
    			+ "<Payee>收款人</Payee>"
    			+ "<!--Payee可选，收款人String20，该栏目打印在发票上-->"
    			+ "<Reviewer>复核人</Reviewer>"
    			+ "<!--Reviewer可选，复核人String20，该栏目打印在发票上-->"
    			+ "<Seller>"
    			+ "<Identifier>500102010003698</Identifier>"
    			+ "<!--Identifier必须，发票开具方税号String20，15、18或20位，该栏目打印在发票上-->"
    			+ "<Name>升级版测试用户3698</Name>"
    			+ "<!--Name必须，发票开具方名称String100，该栏目打印在发票上-->"
    			+ "<Address>某某路10号1203室</Address>"
    			+ "<!--Address必须，发票开具方地址String100，该栏目打印在发票上-->"
    			+ "<TelephoneNo>021-55555555</TelephoneNo>"
    			+ "<!--TelephoneNo必须，发票开具方电话String20，该栏目打印在发票上-->"
    			+ "<Bank>中国建设银行打浦桥支行</Bank>"
    			+ "<!--Bank必须，发票开具方银行String100，该栏目打印在发票上-->"
    			+ "<BankAcc>123456789-0</BankAcc>"
    			+ "<!--BankAcc必须，发票开具方银行账号String30，该栏目打印在发票上-->"
    			+ "</Seller>"
    			+ "<OrderSize count=\"2\">"
    			+ "<Order>"
    			+ "<OrderMain>"
    			+ "<OrderNo>MK44156075</OrderNo>"
    			+ "<!-- OrderNo必须, 每笔订单号必须唯一，来源系统订单号，String20-->"
    			+ "<InvoiceList>0</InvoiceList>"
    			+ "<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->"
    			+ "<InvoiceSplit>0</InvoiceSplit>"
    			+ "<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->"
    			+ "<OrderDate>2016-06-22 23:59:59</OrderDate>"
    			+ "<!--OrderDate可选，来源系统订单时间，\"YYYY-MM-DD HH24:MI:SS\"格式-->"
    					+ "<ChargeTaxWay>0</ChargeTaxWay>"
    					+ "<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->"
    					+ "<TotalAmount>5410.00</TotalAmount>"
    					+ "<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->"
    					+ "<TaxMark>0</TaxMark>"
    					+ "<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->"
    					+ "<Remark>该栏目打印在发票上的备注</Remark>"
    					+ "<!--Remark可选，该栏目打印在发票上的备注，String200，该栏目打印在发票上-->"
    					+ "<Buyer>"
    					+ "<Identifier>310105987654321</Identifier>"
    					+ "<!--Identifier可选，购买方税号String20，15、18或20位，该栏目打印在发票上，专用发票必须-->"
    					+ "<Name>购买方名称</Name>"
    					+ "<!--Name必须，购买方名称String100，该栏目打印在发票上-->"
    					+ "<Address>某某路20号203室</Address>"
    					+ "<!--Address可选，购买方地址String100，该栏目打印在发票上，专用发票必须-->"
    					+ "<TelephoneNo>13912345678</TelephoneNo>"
    					+ "<!--TelephoneNo可选，购买方电话String20，该栏目打印在发票上，专用发票必须-->"
    					+ "<Bank>中国建设银行打浦桥支行</Bank>"
    					+ "<!--Bank可选，购买方银行String100，该栏目打印在发票上，专用发票必须-->"
    					+ "<BankAcc>123456789-0</BankAcc>"
    					+ "<!--BankAcc可选，购买方银行账号String30，该栏目打印在发票上，专用发票必须-->"
    					+ "<Email>abc@163.com</Email>"
    					+ "<!--Email可选，客户接收电子发票的电子邮箱地址String50-->"
    					+ "<IsSend>1</IsSend>"
    					+ "<!--IsSend电子发票生成后是否发送电子邮件（1、是；0、否）-->"
    					+ "<ExtractedCode>3A2B3C4D5F</ExtractedCode>"
    					+ "<!--ExtractedCode，可选，提取码，客户可以在网站上输入后提取码下载电子发票String100-->"
    					+ "<Recipient>张三</Recipient>"
    					+ "<!--Recipient可选，纸质发票收件人姓名String50-->"
    					+ "<ReciAddress>收件人地址</ReciAddress>"
    					+ "<!--Address可选，纸质发票收件人地址String200-->"
    					+ "<Zip>200000</Zip>"
    					+ "<!--zip可选，纸质发票收件人邮编String10-->"
    					+ "</Buyer>"
    					+ "</OrderMain>"
    					+ "<OrderDetails count=\"2\">"
    							+ "<!--size收费明细商品或服务行数，必须与ProductItem数量一致-->"
    							+ "<ProductItem>"
    							+ "<ProductCode>5010103000000000000</ProductCode>"
    							+ "<!--ProductCode必选，商品代码String19 -->"
    							+ "<ProductName>商品1</ProductName>"
    							+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
    							+ "<RowType>0</RowType>"
    							+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
    							+ "<Spec>规格型号1</Spec>"
    							+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
    							+ "<Unit>单位1</Unit>"
    							+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
    							+ "<Quantity>1</Quantity>"
    							+ "<!--Quantity可选，商品数量，该栏目打印在发票上-->"
    							+ "<UnitPrice>1000.00</UnitPrice>"
    							+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
    							+ "<Amount>1000.00</Amount>"
    							+ "<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
    							+ "<DeductAmount></DeductAmount>"
    							+ "<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
    							+ "<TaxRate>0.17</TaxRate>"
    							+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
    							+ "<TaxAmount>170.00</TaxAmount>"
    							+ "<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
    							+ "<MxTotalAmount>1170.00</MxTotalAmount>"
    							+ "<!--MxTotalAmount，商品明细价税合计，必须-->"
    							+ "<VenderOwnCode></VenderOwnCode>"
    							+ "<!--可空，ERP系统中商品或收费项目的自行编码-->"
    							+ "<PolicyMark>0</PolicyMark>"
    							+ "<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
    							+ "<TaxRateMark></TaxRateMark>"
    							+ "<!--必须,空或0是正常税率,1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税,3普通零税率-->"
    							+ "<PolicyName></PolicyName>"
    							+ "<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
    							+ "</ProductItem>"
    							+ "<ProductItem>"
    							+ "<ProductCode>5010103000000000000</ProductCode>"
    							+ "<!--ProductCode可选，商品代码String20 -->"
    							+ "<ProductName>商品2</ProductName>"
    							+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
    							+ "<RowType>0</RowType>"
    							+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
    							+ "<Spec>规格型号2</Spec>"
    							+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
    							+ "<Unit>单位2</Unit>"
    							+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
    							+ "<Quantity>2</Quantity>"
    							+ "<!--Quantity可选，商品数量，该栏目打印在发票上-->"
    							+ "<UnitPrice>2000.00</UnitPrice>"
    							+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
    							+ "<Amount>4000.00</Amount>"
    							+ "<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
    							+ "<DeductAmount></DeductAmount>"
    							+ "<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
    							+ "<TaxRate>0.06</TaxRate>"
    							+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
    							+ "<TaxAmount>240.00</TaxAmount>"
    							+ "<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
    							+ "<MxTotalAmount>4240.00</MxTotalAmount>"
    							+ "<!-- MxTotalAmount，商品明细价税合计，必须-->"
    							+ "<VenderOwnCode>商品自行编码</VenderOwnCode>"
    							+ "<!--可空，ERP系统中商品或收费项目的自行编码-->"
    							+ "<PolicyMark>0</PolicyMark>"
    							+ "<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
    							+ "<TaxRateMark></TaxRateMark>"
    							+ "<!--必须空或0，是正常税率1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税3普通零税率-->"
    							+ "<PolicyName></PolicyName>"
    							+ "<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
    							+ "</ProductItem>"
    							+ "</OrderDetails>"
    							+ "</Order>"
    							+ "<Order>"
    							+ "<OrderMain>"
    							+ "<OrderNo>MK35156077</OrderNo>"
    							+ "<!-- OrderNo必须, 每笔订单号必须唯一，来源系统订单号，String20-->"
    							+ "<InvoiceList>0</InvoiceList>"
    							+ "<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->"
    							+ "<InvoiceSplit>0</InvoiceSplit>"
    							+ "<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->"
    							+ "<OrderDate>2016-06-22 23:59:59</OrderDate>"
    							+ "<!--OrderDate可选，来源系统订单时间，\"YYYY-MM-DD HH24:MI:SS\"格式-->"
    									+ "<ChargeTaxWay>0</ChargeTaxWay>"
    									+ "<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->"
    									+ "<TotalAmount>5410.00</TotalAmount>"
    									+ "<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->"
    									+ "<TaxMark>0</TaxMark>"
    									+ "<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->"
				+ "<Remark>该栏目打印在发票上的备注</Remark>"
				+ "<!--Remark可选，该栏目打印在发票上的备注，String200，该栏目打印在发票上-->"
				+"<Buyer>"
				+"<Identifier>310105987654321</Identifier>"
					+"<!--Identifier可选，购买方税号String20，15、18或20位，该栏目打印在发票上，专用发票必须-->"
					+"<Name>购买方名称</Name>"
							+"<!--Name必须，购买方名称String100，该栏目打印在发票上-->"
					+"<Address>某某路20号203室</Address>"
							+"<!--Address可选，购买方地址String100，该栏目打印在发票上，专用发票必须-->"
					+"<TelephoneNo>13912345678</TelephoneNo>"
							+"<!--TelephoneNo可选，购买方电话String20，该栏目打印在发票上，专用发票必须-->"
					+"<Bank>中国建设银行打浦桥支行</Bank>"
							+"<!--Bank可选，购买方银行String100，该栏目打印在发票上，专用发票必须-->"
					+"<BankAcc>123456789-0</BankAcc>"
					+"<!--BankAcc可选，购买方银行账号String30，该栏目打印在发票上，专用发票必须-->"
					+"<Email>abc@163.com</Email>"
							+"<!--Email可选，客户接收电子发票的电子邮箱地址String50-->"
					+"<IsSend>1</IsSend>"
							+"<!--IsSend电子发票生成后是否发送电子邮件（1、是；0、否）-->"
					+"<ExtractedCode>2A2B3C4D6D</ExtractedCode>"
							+"<!--ExtractedCode，可选，提取码，客户可以在网站上输入后提取码下载电子发票String100-->"
					+"<Recipient>张三</Recipient>"
							+"<!--Recipient可选，纸质发票收件人姓名String50-->"
					+"<ReciAddress>收件人地址</ReciAddress>"
							+"<!--Address可选，纸质发票收件人地址String200-->"
					+"	<Zip>200000</Zip>"
							+"<!--zip可选，纸质发票收件人邮编String10-->"
					+"</Buyer>"
						+"</OrderMain>"
			+"<OrderDetails count=\"2\">"
					+"<!--size收费明细商品或服务行数，必须与ProductItem数量一致-->"
				+"<ProductItem>"
						+"<ProductCode>5010103000000000000</ProductCode>"
					+"<!--ProductCode必选，商品代码String19 -->"
					+"<ProductName>商品1</ProductName>"
							+"	<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
					+"<RowType>0</RowType>"
					+"<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
					+"<Spec>规格型号1</Spec>"
							+"<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
					+"<Unit>单位1</Unit>"
							+"<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
					+"<Quantity>1</Quantity>"
							+"<!--Quantity可选，商品数量，该栏目打印在发票上-->"
					+"<UnitPrice>1000.00</UnitPrice>"
							+"<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
							+"<Amount>1000.00</Amount>"
							+"<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
							+"<DeductAmount></DeductAmount>"
							+"<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
							+"<TaxRate>0.17</TaxRate>"
							+"<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
					+"<TaxAmount>170.00</TaxAmount>"
							+"<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
							+"<MxTotalAmount>1170.00</MxTotalAmount>"
							+"<!-- MxTotalAmount，商品明细价税合计，必须 -->"
					+"<VenderOwnCode></VenderOwnCode>"
							+"<!--可空，ERP系统中商品或收费项目的自行编码-->"
					+"<PolicyMark>0</PolicyMark>"
							+"<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
					+"<TaxRateMark></TaxRateMark>"
							+"<!--必须,空或0是正常税率,1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税,3普通零税率-->"
							+"<PolicyName></PolicyName>"
							+"<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
							+"</ProductItem>"
						+"<ProductItem>"
				+"<ProductCode>5010103000000000000</ProductCode>"
					+"<!--ProductCode可选，商品代码String20 -->"
					+"<ProductName>商品2</ProductName>"
							+"<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
					+"<RowType>0</RowType>"
							+"<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
					+"<Spec>规格型号2</Spec>"
					+"<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
					+"<Unit>单位2</Unit>"
							+"<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
					+"<Quantity>2</Quantity>"
							+"<!--Quantity可选，商品数量，该栏目打印在发票上-->"
					+"<UnitPrice>2000.00</UnitPrice>"
							+"<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
							+"<Amount>4000.00</Amount>"
							+"<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
							+"<DeductAmount></DeductAmount>"
							+"<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
							+"<TaxRate>0.06</TaxRate>"
							+"<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
					+"<TaxAmount>240.00</TaxAmount>"
							+"<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
							+"<MxTotalAmount>4240.00</MxTotalAmount>"
							+"<!-- MxTotalAmount，商品明细价税合计，必须 -->"
					+"<VenderOwnCode></VenderOwnCode>"
							+"<!--可空，ERP系统中商品或收费项目的自行编码-->"
					+"<PolicyMark>0</PolicyMark>"
							+"<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
					+"<TaxRateMark></TaxRateMark>"
					+"<!--必须空或0，是正常税率1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税3普通零税率-->"
					+"<PolicyName></PolicyName>"
							+"<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
							+"</ProductItem>"
						+"</OrderDetails>"
			+"</Order>"
		+"</OrderSize>"
        +"</Request>";
    	
    	final String xml02 ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<Request>"
    			+ "<Row>"
    			+ "<ClientNO>kp005</ClientNO>"
    			+ "<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
    			+ "<OrderNo>ME24156075</OrderNo>"
    			+ "<!--OrderNo必选，每笔订单号必须唯一，订单号，String20-->"
    			+ "<ChargeTaxWay>0</ChargeTaxWay>"
    			+ "<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->"
    			+ "<InvoiceList>0</InvoiceList>"
    			+ "<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->"
    			+ "<InvoiceSplit>0</InvoiceSplit>"
    			+ "<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->"
    			+ "<InvType>1</InvType>"
    			+ "<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
    			+ "<TotalAmount>5410.00</TotalAmount>"
    			+ "<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->"
    			+ "<TaxMark>0</TaxMark>"
    			+ "<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->"
    			+ "<InvoiceDetails count=\"2\">"
    			+ "<!--size收费明细商品或服务行数，必须与ProductItem数量一致-->"
    			+ "<ProductItem>"
    			+ "<ProductCode>5010103000000000000</ProductCode>"
    			+ "<!--ProductCode必选，商品代码String19 -->"
    			+ "<ProductName>商品1</ProductName>"
    			+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
    			+ "<RowType>0</RowType>"
    			+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
    			+ "<Spec>规格型号1</Spec>"
    			+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
    			+ "<Unit>单位1</Unit>"
    			+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
    			+ "<Quantity>1</Quantity>"
    			+ "<!--Quantity可选，商品数量，该栏目打印在发票上-->"
    			+ "<UnitPrice>1000.00</UnitPrice>"
    			+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
    			+ "<Amount>1000.00</Amount>"
    			+ "<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
    			+ "<DeductAmount></DeductAmount>"
    			+ "<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
    			+ "<TaxRate>0.17</TaxRate>"
    			+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
    			+ "<TaxAmount>170.00</TaxAmount>"
    			+ "<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
    			+ "<MxTotalAmount>1170.00</MxTotalAmount >"
    			+ "<!-- MxTotalAmount，商品明细价税合计，必须 -->"
    			+ "<VenderOwnCode>商品自行编码</VenderOwnCode>"
    			+ "<!--可空，ERP系统中商品或收费项目的自行编码-->"
    			+ "<PolicyMark>0</PolicyMark>"
    			+ "<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
    			+ "<TaxRateMark></TaxRateMark>"
    			+ "<!--必须,空或0是正常税率,1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税,3普通零税率-->"
				+ "<PolicyName></PolicyName>"
				+ "<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
			+ "</ProductItem>"
			+ "<ProductItem>"
				+ "<ProductCode>5010103000000000000</ProductCode>"
				+ "<!--ProductCode可选，商品代码String20 -->"
				+ "<ProductName>商品2</ProductName>"
				+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
				+ "<RowType>0</RowType>"
				+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
				+ "<Spec>规格型号2</Spec>"
				+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
				+ "<Unit>单位2</Unit>"
				+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
				+ "<Quantity>2</Quantity>"
				+ "<!--Quantity可选，商品数量，该栏目打印在发票上-->"
				+ "<UnitPrice>2000.00</UnitPrice>"
				+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
				+ "<Amount>4000.00</Amount>"
				+ "<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
				+ "<DeductAmount></DeductAmount>"
				+ "<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
				+ "<TaxRate>0.06</TaxRate>"
				+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
				+ "<TaxAmount>240.00</TaxAmount>"
				+ "<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
             + "<MxTotalAmount>4240.00</MxTotalAmount >"
				+ "<!-- MxTotalAmount，商品明细价税合计，必须 -->"
				+ "<VenderOwnCode>商品自行编码</VenderOwnCode>"
				+ "<!--可空，ERP系统中商品或收费项目的自行编码-->"
				+ "<PolicyMark>0</PolicyMark>"
				+ "<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
				+ "<TaxRateMark></TaxRateMark>"
				+ "<!--必须空或0，是正常税率1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税3普通零税率-->"
				+ "<PolicyName></PolicyName>"
				+ "<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
			+ "</ProductItem>"
		+ "</InvoiceDetails>"
	+ "</Row>"
	+ "<Row>"
		+ "<ClientNO>kp005</ClientNO>"
		+ "<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
		+ "<OrderNo>ME24156072</OrderNo>"
		+ "<!--OrderNo必选，每笔订单号必须唯一，订单号，String20-->"
		+ "<ChargeTaxWay>0</ChargeTaxWay>"
		+ "<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->"
		+ "<InvoiceList>0</InvoiceList>"
		+ "<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->"
		+ "<InvoiceSplit>0</InvoiceSplit>"
		+ "<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->"
		+ "<InvType>01</InvType>"
		+ "<!--InvType必须，发票种类（00、专用发票(纸质)；01、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->"
		+ "<TotalAmount>5410.00</TotalAmount>"
		+ "<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->"
		+ "<TaxMark>0</TaxMark>"
		+ "<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->"
		+ "<InvoiceDetails count=\"2\">"
		+ "<!--size收费明细商品或服务行数，必须与ProductItem数量一致-->"
			+ "<ProductItem>"
				+ "<ProductCode>5010103000000000000</ProductCode>"
				+ "<!--ProductCode必选，商品代码String19 -->"
				+ "<ProductName>商品1</ProductName>"
				+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
				+ "<RowType>0</RowType>"
				+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
				+ "<Spec>规格型号1</Spec>"
				+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
				+ "<Unit>单位1</Unit>"
				+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
				+ "<Quantity>1</Quantity>"
				+ "<!--Quantity可选，商品数量，该栏目打印在发票上-->"
				+ "<UnitPrice>1000.00</UnitPrice>"
				+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
				+ "<Amount>1000.00</Amount>"
				+ "<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
				+ "<DeductAmount></DeductAmount>"
				+ "<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
				+ "<TaxRate>0.17</TaxRate>"
				+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
				+ "<TaxAmount>170.00</TaxAmount>"
				+ "<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
				+ "<MxTotalAmount>1170.00</MxTotalAmount >"
				+ "<!-- MxTotalAmount，商品明细价税合计，必须 -->"
				+ "<VenderOwnCode>商品自行编码</VenderOwnCode>"
				+ "<!--可空，ERP系统中商品或收费项目的自行编码-->"
				+ "<PolicyMark>0</PolicyMark>"
				+ "<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
				+ "<TaxRateMark></TaxRateMark>"
				+ "<!--必须,空或0是正常税率,1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税,3普通零税率-->"
				+ "<PolicyName></PolicyName>"
				+ "<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
			+ "</ProductItem>"
			+ "<ProductItem>"
				+ "<ProductCode>5010103000000000000</ProductCode>"
				+ "<!--ProductCode可选，商品代码String20 -->"
				+ "<ProductName>商品2</ProductName>"
				+ "<!--ProductName必须，商品名称String30，该栏目打印在发票上-->"
				+ "<RowType>0</RowType>"
				+ "<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->"
				+ "<Spec>规格型号2</Spec>"
				+ "<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->"
				+ "<Unit>单位2</Unit>"
				+ "<!--Unit可选，商品单位String20，该栏目打印在发票上-->"
				+ "<Quantity>2</Quantity>"
				+ "<!--Quantity可选，商品数量，该栏目打印在发票上-->"
				+ "<UnitPrice>2000.00</UnitPrice>"
				+ "<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->"
				+ "<Amount>4000.00</Amount>"
				+ "<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->"
				+ "<DeductAmount></DeductAmount>"
				+ "<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->"
				+ "<TaxRate>0.06</TaxRate>"
				+ "<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->"
				+ "<TaxAmount>240.00</TaxAmount>"
				+ "<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->"
				+ "<MxTotalAmount>4240.00</MxTotalAmount >"
				+ "<!-- MxTotalAmount，商品明细价税合计，必须 -->"
				+ "<VenderOwnCode></VenderOwnCode>"
				+ "<!--可空，ERP系统中商品或收费项目的自行编码-->"
				+ "<PolicyMark>0</PolicyMark>"
				+ "<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->"
				+ "<TaxRateMark></TaxRateMark>"
				+ "<!--必须空或0，是正常税率1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税3普通零税率-->"
    			+ "<PolicyName></PolicyName>"
				+ "<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->"
			+ "</ProductItem>"
		+ "</InvoiceDetails>"
	+ "</Row>"
+ "</Request>";
       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "UploadOrderData";
       String AppKey = "RJ5689ea2d0482";
       String key ="0f2aa080911da0adcfc5f630e9d20e1a";
       String Secret = getSign(xml01,key);
       Object[] objects = client.invoke(methodName, AppKey, Secret,"01",xml01);
       //输出调用结果
       System.out.println(objects[0].toString());
    }
    
    private static String getSign(String QueryData,String key){
    	String signSourceData = "data=" + QueryData + "&key=" + key;
        String newSign =  DigestUtils.md5Hex(signSourceData);
        return newSign;
    }
    
    //public static String WS_URL = "http://test.datarj.com/webService/services/invoiceService?wsdl";
    public static String WS_URL = "http://localhost:8080/dzfp-service-new/services/invoiceService?wsdl";

    public static void main(String[] args) throws Exception {
    	//testCallQuery();
    	//testUploadOrder();
    	//testCallService();
    	//testinvoiceUpload();
    	testuploadOrderData();
    }

}
