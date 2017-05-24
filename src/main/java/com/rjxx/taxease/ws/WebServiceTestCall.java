package com.rjxx.taxease.ws;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

/**
 * Created by Administrator on 2016/12/15.
 */
public class WebServiceTestCall {

   
    public static void testCallQuery() throws Exception {
    	 String QueryData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                 "<Request>" +
                 "  <!--以下查询条件可任选-->" +
                 "  <SerialNumber>20170122115605432522</SerialNumber>" +
                 "  <!--SerialNumber可选，交易流水号String20-->" +
                 "  <OrderNumber></OrderNumber>" +
                 "  <!--OrderNumber可选，来源系统订单号，String20-->" +
                 "  <ExtractCode></ExtractCode>" +
                 "  <!--ExtractCode可选，提取码String20-->" +
                 "</Request>";
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient(WS_URL);
        String methodName = "CallQuery";
        String AppKey = "RJ874afd58e67b";
        String key ="8e37be80cd6dcd8051d589d32f4d0ff2";
        String Secret = getSign(QueryData,key);
        String InvoiceData = QueryData;
        Object[] objects = client.invoke(methodName, AppKey, Secret, InvoiceData);
        //输出调用结果
        System.out.println(objects[0].toString());
    }
 
    public static void testUploadOrder() throws Exception {
    	String OrderData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Request>" +
        		"<Row> "+
                "  <OrderNo>ME241561114</OrderNo>" +
                "  <!--OrderNo必选，订单号，String20-->" +
                "  <OrderTime>20161220103914</OrderTime>" +
                "  <!--OrderTime必选，订单时间，String14-->" +
                "  <Price>438.33</Price>" +
                "  <!--Price必选，金额，Double(10,2) -->" +
                "  <Sign>b0c87cce86a4dfebedc05d83e7f76790</Sign>" +
                "  <!--Sign必选，签名串String32-->" +
                "  <StoreNo>8001</StoreNo>" +
                "  <!--StoreNo可选，门店编号String20-->" +
                "  </Row>" +
                "<Row> "+
                "  <OrderNo>ME24156115</OrderNo>" +
                "  <!--OrderNo必选，订单号，String20-->" +
                "  <OrderTime>20161220112216</OrderTime>" +
                "  <!--OrderTime必选，订单时间，String14-->" +
                "  <Price>438.99</Price>" +
                "  <!--Price必选，金额，Double(10,2) -->" +
                "  <Sign>b0c87cce86a4dfebedc05d83e7aaaaaa</Sign>" +
                "  <!--Sign必选，签名串String32-->" +
                "  <StoreNo>8001</StoreNo>" +
                "  <!--StoreNo可选，门店编号String20-->" +
                "  </Row>" +
                "</Request>";
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
    	String lzData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Request>" +
                "<SerialNumber>2016082512444500078</SerialNumber>" +
                "<InvType>12</InvType>" +
                "<InvoiceSplit>1</InvoiceSplit>" +
                "<ServiceType>0</ServiceType>" +
                "<Email>zhangbing@datarj.com</Email>" +
                "<IsSend>1</IsSend>" +
                "<ExtractedCode>d2127a</ExtractedCode>" +
                "<Recipient>张三</Recipient>" +
                "<Zip>200000</Zip>" +
                "<LetterAddress>上海市漕宝路</LetterAddress>" +
                "<Drawer>开票人</Drawer>" +
                "<Payee>收款人</Payee>" +
                "<Reviewer>复核人</Reviewer>" +
                "<CancelUser>作废人</CancelUser>" +
                "<CNNoticeNo></CNNoticeNo>" +
                "<CNDNCode></CNDNCode>" +
                "<CNDNNo></CNDNNo>" +
                "<InvoiceMain>" +
                "<OrderNumber>ME24156071</OrderNumber>" +
                "<OrderDate>2016-06-22 23:59:59</OrderDate>" +
                "<TotalAmount>5410.00</TotalAmount>" +
                "<TaxMark>0</TaxMark>" +
                "<Remark>这是备注</Remark>" +
                "<Seller>" +
                "<Identifier>91310101MA1FW0008P</Identifier>" +
                "<Name>上海百旺测试盘</Name>" +
                "<Address>某某路10号1203室</Address>" +
                "<TelephoneNo>021-55555555</TelephoneNo>" +
                "<Bank>中国建设银行打浦桥支行</Bank>" +
                "<BankAcc>123456789-0</BankAcc>" +
                "</Seller>" +
                "<Buyer>" +
                "<Identifier></Identifier>" +
                "<Name>购方名称</Name>" +
                "<Address>某某路20号203室</Address>" +
                "<TelephoneNo>13912345678</TelephoneNo>" +
                "<Bank>中国建设银行打浦桥支行</Bank>" +
                "<BankAcc>123456789-0</BankAcc>" +
                "</Buyer>" +
                "</InvoiceMain>" +
                "<InvoiceDetails count=\"2\">" +
                "<ProductItem>" +
                "<ProductCode>1000000000000000000</ProductCode>" +
                "<ProductName>商品1</ProductName>" +
                "<RowType>0</RowType>" +
                "<Spec>规格型号1</Spec>" +
                "<Unit>单位1</Unit>" +
                "<Quantity>1</Quantity>" +
                "<UnitPrice>1000.00</UnitPrice>" +
                "<Amount>1000.00</Amount>" +
                "<TaxRate>0.17</TaxRate>" +
                "<TaxAmount>170.00</TaxAmount>" +
                "</ProductItem>" +
                "<ProductItem>" +
                "<ProductCode>1000000000000000000</ProductCode>" +
                "<ProductName>商品2</ProductName>" +
                "<RowType>0</RowType>" +
                "<Spec>规格型号2</Spec>" +
                "<Unit>单位2</Unit>" +
                "<Quantity>2</Quantity>" +
                "<UnitPrice>2000.00</UnitPrice>" +
                "<Amount>4000.00</Amount>" +
                "<TaxRate>0.06</TaxRate>" +
                "<TaxAmount>240.00</TaxAmount>" +
                "</ProductItem>" +
                "</InvoiceDetails>" +
                "</Request>";
    	String qbhcData ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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
    			+ "<InvoiceDetails count=\"2\">" +
                 "<ProductItem>" +
                 "<ProductCode>1000000000000000000</ProductCode>" +
                 "<ProductName>商品1</ProductName>" +
                 "<RowType>0</RowType>" +
                 "<Spec>规格型号1</Spec>" +
                 "<Unit>单位1</Unit>" +
                 "<Quantity>-1</Quantity>" +
                 "<UnitPrice>500.00</UnitPrice>" +
                 "<Amount>-500</Amount>" +
                 "<TaxRate>0.17</TaxRate>" +
                 "<TaxAmount>-85.00</TaxAmount>" +
                 "</ProductItem>" +
                 "<ProductItem>" +
                 "<ProductCode>1000000000000000000</ProductCode>" +
                 "<ProductName>商品2</ProductName>" +
                 "<RowType>0</RowType>" +
                 "<Spec>规格型号2</Spec>" +
                 "<Unit>单位2</Unit>" +
                 "<Quantity>-2</Quantity>" +
                 "<UnitPrice>1000.00</UnitPrice>" +
                 "<Amount>-2000.00</Amount>" +
                 "<TaxRate>0.06</TaxRate>" +
                 "<TaxAmount>-120.00</TaxAmount>" +
                   "</ProductItem>" 
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
    	final String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?><Request>" +
    			"    <TotalAmount>1</TotalAmount>" +
    			"    <SerialNumber>7000138809059495305</SerialNumber>" +
    			"    <Main>" +
    			"        <OrderNumber>70001388990</OrderNumber>" +
    			"        <OrderDate>2016-07-11</OrderDate>" +
    			"        <OrderTime>06:08:41</OrderTime>" +
    			"        <Seller>" +
    			"            <Identifier>91310101MA1FW0008P</Identifier>" +
    			"            <Name>上海百旺测试盘</Name>" +
    			"            <Address>Shanghai JingAn District West Nanjing Road No.15, JingAn JiaLi Center Room 607</Address>" +
    			"            <TelephoneNumber>021-xxxxxxxx</TelephoneNumber>" +
    			"        </Seller>" +
    			"        <Buyer>" +
    			"            <Identifier/>" +
    			"            <Name>王五</Name>" +
    			"            <IsSend>1</IsSend>" +
    			"            <Payment>ALIPAY-FASTPAY</Payment>" +
    			"            <Address>&#x6c5f;&#x82cf;&#x7701;,&#x5357;&#x4eac;&#x5e02; CN &#x8f6f;&#x4ef6;&#x5927;&#x9053;108&#x53f7;&#x84dd;&#x7b79;&#x8c37;2&#x680b;1&#x5355;&#x5143;401 ;;;;210012;;;; &#x9648;&#x4e16;&#x6770;</Address>" +
    			"            <TelephoneNumber>18811049101</TelephoneNumber>" +
    			"            <Email>zhangbing@datarj.com</Email>" +
    			"        </Buyer>" +
    			"    </Main>" +
    			"    <Details size=\"2\">" +
    			"        <ProductItem>" +
    			"            <Description>Abercrombie and Fitch Apparel</Description>" +
    			"            <Unit>EACH</Unit>" +
    			"            <Quantity>1</Quantity>" +
    			"            <UnitPrice>1000</UnitPrice>" +
    			"            <Amount>1000.00</Amount>" +
    			"        </ProductItem>" +
    			"        <ProductItem>" +
    			"            <Description>Abercrombie and Fitch Apparel</Description>" +
    			"            <Unit>EACH</Unit>" +
    			"            <Quantity>1</Quantity>" +
    			"            <UnitPrice>1000</UnitPrice>" +
    			"            <Amount>1000.00</Amount>" +
    			"        </ProductItem>" +
    			"    </Details>" +
    			"</Request>";
       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "invoiceUpload";
     
       Object[] objects = client.invoke(methodName,xml);
       //输出调用结果
       System.out.println(objects[0].toString());
    }
    
    public static void testuploadOrderData() throws Exception {
    	final String xml011="<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<Request>" +
				"  <ClientNO>tujia_01</ClientNO>" +
				"  <SerialNumber>2016062412444500014</SerialNumber>" +
				"  <InvType>12</InvType>" +
				"  <ServiceType>0</ServiceType>" +
				"  <Spbmbbh>12.0</Spbmbbh>" +
				"  <Drawer>张三</Drawer>" +
				"  <Seller>" +
				"    <Identifier>500102010003643</Identifier>" +
				"    <Name>上海百旺测试3643</Name>" +
				"    <Address>某某路10号1203室</Address>" +
				"    <TelephoneNo>021-55555555</TelephoneNo>" +
				"    <Bank>中国建设银行打浦桥支行</Bank>" +
				"    <BankAcc>123456789-0</BankAcc>" +
				"  </Seller>" +
				"   <OrderSize count=\"1\">" +
				"    <Order>" +
				"      <OrderMain>" +
				"        <OrderNo>ME24156071</OrderNo>" +
				"        <InvoiceList>0</InvoiceList>" +
				"        <InvoiceSplit>1</InvoiceSplit>" +
				"        <InvoiceSfdy>1</InvoiceSfdy>" +
				"        <OrderDate>2017-05-16 17:39:57</OrderDate>" +
				"        <ChargeTaxWay>0</ChargeTaxWay>" +
				"        <TotalAmount>1000</TotalAmount>" +
				"        <TaxMark>1</TaxMark>" +
				"        <Remark>tesr</Remark>" +
				"        <Buyer>" +
				"          <Name>上海途逸公寓管理有限公司</Name>" +
				"			 <Email>179637014@qq.com</Email>" +
				"			 <IsSend>1</IsSend>" +
				"        </Buyer>" +
				"      </OrderMain>" +
				"      <OrderDetails count=\"1\">" +
				"        <ProductItem>" +
				"          <ProductCode>1000000000000000000</ProductCode>" +
				"          <ProductName>房费</ProductName>" +
				"          <RowType>0</RowType>" +
				"          <Quantity>0</Quantity>" +
				"          <UnitPrice>0</UnitPrice>" +
				"          <Amount>1000</Amount>" +
				"          <DeductAmount>0</DeductAmount>" +
				"          <TaxRate>0.17</TaxRate>" +
				"          <TaxAmount>170</TaxAmount>" +
				"          <MxTotalAmount>1170</MxTotalAmount>" +
				"          <PolicyMark>0</PolicyMark>" +
				"        </ProductItem>" +
				"      </OrderDetails>" +
				"    </Order>" +
				"  </OrderSize>" +
				"</Request>";
    	
    	final String xml01 ="<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    			+ "<Request>"
    			+ "<ClientNO>kp006</ClientNO>"
    			+ "<SerialNumber>3922121312221</SerialNumber>"
    			+ "<InvType>02</InvType>"
    			+ "<ServiceType>0</ServiceType>"
    			+ "<Drawer>qian</Drawer>"
	+ "<Payee/>"
	+ "<Reviewer/>"
	+ "<Seller>"
		+ "<Identifier>500102010003699</Identifier>"
		+ "<Name>升级版测试用户3699</Name>"
    	+ "<Address>测试南京湖南路亚朵酒店</Address>"
		+ "<TelephoneNo>020-123456768</TelephoneNo>"
		+ "<Bank>中国农业银行</Bank>"
		+ "<BankAcc>03399500040009801</BankAcc>"
	+ "</Seller>"
	+ "<OrderSize count=\"1\">"
	+ "<Order>"
			+ "<OrderMain>"
				+ "<OrderNo>396124_1991312</OrderNo>"
				+ "<InvoiceList>0</InvoiceList>"
				+ "<InvoiceSplit>0</InvoiceSplit>"
				+ "<OrderDate>2017-01-10 15:33:00</OrderDate>"
				+ "<ChargeTaxWay>0</ChargeTaxWay>"
				+ "<TotalAmount>201.00</TotalAmount>"
				+ "<TaxMark>0</TaxMark>"
				+ "<Remark/>"
    	+ "<Buyer>"
					+ "<Identifier>123456789013234568</Identifier>"
					+ "<Name>购买方名称</Name>"
	    			+ "<!--Name必须，购买方名称String100，该栏目打印在发票上-->"
	    			+ "<Address>某某路20号203室</Address>"
	    			+ "<!--Address可选，购买方地址String100，该栏目打印在发票上，专用发票必须-->"
	    			+ "<TelephoneNo>13912345678</TelephoneNo>"
	    			+ "<!--TelephoneNo可选，购买方电话String20，该栏目打印在发票上，专用发票必须-->"
	    			+ "<Bank>中国建设银行打浦桥支行</Bank>"
	    			+ "<!--Bank可选，购买方银行String100，该栏目打印在发票上，专用发票必须-->"
	    			+ "<BankAcc>123456789-0</BankAcc>"
					+ "<Email/>"
					+ "<IsSend>0</IsSend>"
    	+ "<ExtractedCode></ExtractedCode>"
					+ "<Recipient>0</Recipient>"
					+ "<ReciAddress/>"
					+ "<Zip/>"
				+ "</Buyer>"
			+ "</OrderMain>"
			+ "<OrderDetails count=\"2\">"
			+ "<ProductItem>"
					+ "<ProductCode>3070402000000000000</ProductCode>"
					+ "<ProductName>住宿服务费</ProductName>"
					+ "<RowType>0</RowType>"
					+ "<Spec/>"
					+ "<Unit>次</Unit>"
					+ "<Quantity>1</Quantity>"
					+ "<UnitPrice>0.94</UnitPrice>"
					+ "<Amount>0.94</Amount>"
					+ "<DeductAmount>0</DeductAmount>"
					+ "<TaxRate>0.06</TaxRate>"
					+ "<TaxAmount>0.06</TaxAmount>"
					+ "<MxTotalAmount>1.00</MxTotalAmount>"
					+ "<VenderOwnCode/>"
					+ "<PolicyMark>0</PolicyMark>"
					+ "<TaxRateMark/>"
					+ "<PolicyName/>"
				+ "</ProductItem>"
				+ "<ProductItem>"
					+ "<ProductCode>3070401000000000000</ProductCode>"
					+ "<ProductName>餐饮住宿服务费</ProductName>"
					+ "<RowType>0</RowType>"
					+ "<Spec/>"
					+ "<Unit>次</Unit>"
					+ "<Quantity>1</Quantity>"
					+ "<UnitPrice>188.68</UnitPrice>"
					+ "<Amount>188.68</Amount>"
					+ "<DeductAmount>0</DeductAmount>"
					+ "<TaxRate>0.06</TaxRate>"
					+ "<TaxAmount>11.32</TaxAmount>"
    	+ "<MxTotalAmount>200.00</MxTotalAmount>"
					+ "<VenderOwnCode/>"
					+ "<PolicyMark>0</PolicyMark>"
					+ "<TaxRateMark/>"
					+ "<PolicyName/>"
				+ "</ProductItem>"
			+ "</OrderDetails>"
		+ "</Order>"
	+ "</OrderSize>"
+ "</Request>";
    	
    	final String xml02 ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<Request>"
    			+ "<Row>"
    			+ "<ClientNO>kp006</ClientNO>"
    			+ "<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
    			+ "<OrderNo>ME24256075</OrderNo>"
    			+ "<!--OrderNo必选，每笔订单号必须唯一，订单号，String20-->"
    			+ "<ChargeTaxWay>0</ChargeTaxWay>"
    			+ "<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->"
    			+ "<InvoiceList>0</InvoiceList>"
    			+ "<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->"
    			+ "<InvoiceSplit>0</InvoiceSplit>"
    			+ "<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->"
    			+ "<InvType>02</InvType>"
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
		+ "<OrderNo>ME24256072</OrderNo>"
		+ "<!--OrderNo必选，每笔订单号必须唯一，订单号，String20-->"
		+ "<ChargeTaxWay>0</ChargeTaxWay>"
		+ "<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->"
		+ "<InvoiceList>0</InvoiceList>"
		+ "<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->"
		+ "<InvoiceSplit>0</InvoiceSplit>"
		+ "<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->"
		+ "<InvType>02</InvType>"
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
    	
    	String xml08 = "<Request>"
    			+ "<ClientNO>tujia_01</ClientNO>"
    			+ "<!--纸质票必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002-->"
    			+ "<Fplxdm>02</Fplxdm>"
    			+ "<!--必须，发票类型代码，01增值税专用发票，02增值税普通发票；-->"
    			+ "</Request>";

		String xml11 = "<Request>" +
				"<ClientNO></ClientNO>" +
				"<Fplxdm></Fplxdm>" +
				"<SerialNumber></SerialNumber>" +
				"<OrderNumber>2608771</OrderNumber>" +
				"<ExtractCode></ExtractCode>" +
				"</Request>";

		String xml04="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Request>\n" +
				"\t<ClientNO>tujia_01</ClientNO>\n" +
				"\t<SerialNumber>2016062412444500013</SerialNumber>\n" +
				"\t<InvType>12</InvType>\n" +
				"\t<ServiceType>1</ServiceType>\n" +
				"\t<ChargeTaxWay>0</ChargeTaxWay>\n" +
				"\t<TotalAmount>-1170.000000</TotalAmount>\n" +
				"\t\n" +
				"\t<CNNoticeNo></CNNoticeNo>\n" +
				"\t<CNDNCode>150007899501</CNDNCode>\n" +
				"\t<CNDNNo>21321349</CNDNNo>\n" +
				"</Request>\n";

       JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
       Client client = dcf.createClient(WS_URL);
       String methodName = "UploadOrderData";
       String AppKey = "RJ874afd58e67b";
       String key ="8e37be80cd6dcd8051d589d32f4d0ff2";
       String Secret = getSign(xml04,key);
       Object[] objects = client.invoke(methodName, AppKey, Secret,"04",xml04);
       //输出调用结果
       System.out.println(objects[0].toString());
    }
    
    private static String getSign(String QueryData,String key){
    	String signSourceData = "data=" + QueryData + "&key=" + key;
        String newSign =  DigestUtils.md5Hex(signSourceData);
        return newSign;
    }
    
   //public static String WS_URL = "http://open.datarj.com/webService/services/invoiceService?wsdl";
   //public static String WS_URL = "http://test.datarj.com/webService/services/invoiceService?wsdl";
   public static String WS_URL = "http://localhost:8080/services/invoiceService?wsdl";

    public static void main(String[] args) throws Exception {
        //testCallQuery();
    	//testUploadOrder();
    	//testCallService();
    	//testinvoiceUpload();
    	testuploadOrderData();
    }

}
