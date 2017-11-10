package com.rjxx.taxease.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpUtils {

	public void sendMessage() throws Exception {
		System.out.println("调用servlet开始=================");
		ObjectMapper mapper = new ObjectMapper();
		String InvoiceData="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<Request>\n" +
				"\t<ClientNO>gvc_01</ClientNO>\n" +
				"\t<SerialNumber>JY32B2d24drfaf</SerialNumber>\n" +
				"\t<InvType>12</InvType>\n" +
				"\t<Spbmbbh>13.0</Spbmbbh>\n" +
				"\t<Drawer>测试</Drawer>\n" +
				"\t<Payee/>\n" +
				"\t<Reviewer/>\n" +
				"\t<DataSource>1</DataSource>\n" +
				"\t<OpenId/>\n" +
				"\t<Seller>\n" +
				"\t\t<Identifier>500102010003643</Identifier>\n" +
				"\t\t<Name>上海百旺测试3643</Name>\n" +
				"\t\t<Address>德意志</Address>\n" +
				"\t\t<TelephoneNo>021-59895352</TelephoneNo>\n" +
				"\t\t<Bank>美利坚大银行</Bank>\n" +
				"\t\t<BankAcc>128906323710203</BankAcc>\n" +
				"\t</Seller>\n" +
				"\t<OrderSize count=\"1\">\n" +
				"\t\t<Order>\n" +
				"\t\t\t<OrderMain>\n" +
				"\t\t\t\t<OrderNo>5d33ddadf3</OrderNo>\n" +
				"\t\t\t\t<InvoiceList>0</InvoiceList>\n" +
				"\t\t\t\t<InvoiceSplit>1</InvoiceSplit>\n" +
				"\t\t\t\t<InvoiceSfdy>0</InvoiceSfdy>\n" +
				"\t\t\t\t<OrderDate>2017-08-09 15:26:23</OrderDate>\n" +
				"\t\t\t\t<ChargeTaxWay>0</ChargeTaxWay>\n" +
				"\t\t\t\t<TotalAmount>1590</TotalAmount>\n" +
				"\t\t\t\t<TotalDiscount>300</TotalDiscount>\n" +
				"\t\t\t\t<TaxMark>1</TaxMark>\n" +
				"\t\t\t\t<Remark/>\n" +
				"\t\t\t\t<ExtractedCode/>\n" +
				"\t\t\t\t<Buyer>\n" +
				"\t\t\t\t\t<CustomerType>0</CustomerType>\n" +
				"\t\t\t\t\t<Identifier>91370600050948561K</Identifier>\n" +
				"\t\t\t\t\t<Name>康中徐</Name>\n" +
				"\t\t\t\t\t<Address>漕宝路</Address>\n" +
				"\t\t\t\t\t<TelephoneNo>123456</TelephoneNo>\n" +
				"\t\t\t\t\t<Bank>中国银行</Bank>\n" +
				"\t\t\t\t\t<BankAcc>123456778</BankAcc>\n" +
				"\t\t\t\t\t<Email>kangzhongxu@datarj.com</Email>\n" +
				"\t\t\t\t\t<IsSend>1</IsSend>\n" +
				"\t\t\t\t\t<Recipient/>\n" +
				"\t\t\t\t\t<ReciAddress/>\n" +
				"\t\t\t\t\t<Zip/>\n" +
				"\t\t\t\t</Buyer>\n" +
				"\t\t\t</OrderMain>\n" +
				"\t\t\t<OrderDetails count=\"2\">\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode/>\n" +
				"\t\t\t\t\t<ProductCode>1010115010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>法拉利(YD)</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec/>\n" +
				"\t\t\t\t\t<Unit/>\n" +
				"\t\t\t\t\t<Quantity>1</Quantity>\n" +
				"\t\t\t\t\t<UnitPrice>200</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>200</Amount>\n" +
				"\t\t\t\t\t<DeductAmount/>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>19.82</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>200</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark/>\n" +
				"\t\t\t\t\t<TaxRateMark/>\n" +
				"\t\t\t\t\t<PolicyName/>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode/>\n" +
				"\t\t\t\t\t<ProductCode>1010115000000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>MH370大型客机(YD)</ProductName>\n" +
				"\t\t\t\t\t<RowType>2</RowType>\n" +
				"\t\t\t\t\t<Spec/>\n" +
				"\t\t\t\t\t<Unit/>\n" +
				"\t\t\t\t\t<Quantity>1</Quantity>\n" +
				"\t\t\t\t\t<UnitPrice>790</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>790</Amount>\n" +
				"\t\t\t\t\t<DeductAmount/>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>78.29</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>790</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark/>\n" +
				"\t\t\t\t\t<TaxRateMark/>\n" +
				"\t\t\t\t\t<PolicyName/>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode/>\n" +
				"\t\t\t\t\t<ProductCode>1010115000000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>MH370大型客机(YD)</ProductName>\n" +
				"\t\t\t\t\t<RowType>1</RowType>\n" +
				"\t\t\t\t\t<Spec/>\n" +
				"\t\t\t\t\t<Unit/>\n" +
				"\t\t\t\t\t<Quantity/>\n" +
				"\t\t\t\t\t<UnitPrice/>\n" +
				"\t\t\t\t\t<Amount>-200</Amount>\n" +
				"\t\t\t\t\t<DeductAmount/>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>-19.82</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>-200</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark/>\n" +
				"\t\t\t\t\t<TaxRateMark/>\n" +
				"\t\t\t\t\t<PolicyName/>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode/>\n" +
				"\t\t\t\t\t<ProductCode>1010115010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>东方明珠办公楼(GG)</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec/>\n" +
				"\t\t\t\t\t<Unit/>\n" +
				"\t\t\t\t\t<Quantity>2</Quantity>\n" +
				"\t\t\t\t\t<UnitPrice>400</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>800</Amount>\n" +
				"\t\t\t\t\t<DeductAmount/>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>79.28</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>800</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark/>\n" +
				"\t\t\t\t\t<TaxRateMark/>\n" +
				"\t\t\t\t\t<PolicyName/>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode/>\n" +
				"\t\t\t\t\t<ProductCode>1010115010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>火星星球(FG)</ProductName>\n" +
				"\t\t\t\t\t<RowType>2</RowType>\n" +
				"\t\t\t\t\t<Spec/>\n" +
				"\t\t\t\t\t<Unit/>\n" +
				"\t\t\t\t\t<Quantity>1</Quantity>\n" +
				"\t\t\t\t\t<UnitPrice>100</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>100</Amount>\n" +
				"\t\t\t\t\t<DeductAmount/>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>9.91</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>100</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark/>\n" +
				"\t\t\t\t\t<TaxRateMark/>\n" +
				"\t\t\t\t\t<PolicyName/>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode/>\n" +
				"\t\t\t\t\t<ProductCode>1010115010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>火星星球(FG)</ProductName>\n" +
				"\t\t\t\t\t<RowType>1</RowType>\n" +
				"\t\t\t\t\t<Spec/>\n" +
				"\t\t\t\t\t<Unit/>\n" +
				"\t\t\t\t\t<Quantity/>\n" +
				"\t\t\t\t\t<UnitPrice/>\n" +
				"\t\t\t\t\t<Amount>-100</Amount>\n" +
				"\t\t\t\t\t<DeductAmount/>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>-9.91</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>-100</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark/>\n" +
				"\t\t\t\t\t<TaxRateMark/>\n" +
				"\t\t\t\t\t<PolicyName/>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t</OrderDetails>\n" +
				"\t\t\t<Payments>\n" +
				"\t\t\t\t<PaymentItem>\n" +
				"\t\t\t\t\t<PayCode>PT_05</PayCode>\n" +
				"\t\t\t\t\t<PayPrice>500</PayPrice>\n" +
				"\t\t\t\t</PaymentItem>\n" +
				"\t\t\t\t<PaymentItem>\n" +
				"\t\t\t\t\t<PayCode>PT_01</PayCode>\n" +
				"\t\t\t\t\t<PayPrice>790</PayPrice>\n" +
				"\t\t\t\t</PaymentItem>\n" +
				"\n" +
				"\t\t\t</Payments>\n" +
				"\t\t</Order>\n" +
				"\t</OrderSize>\n" +
				"</Request>\n";


		String xml01 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<Request>\n" +
				"\t<ClientNO>gvc_01</ClientNO>\n" +
				"\t<SerialNumber>JY21d7d081516d8219</SerialNumber>\n" +
				"\t<InvType>12</InvType>\n" +
				"\t<Spbmbbh>13.0</Spbmbbh>\n" +
				"\t<Drawer>刘先生</Drawer>\n" +
				"\t<Payee>王五</Payee>\n" +
				"\t<Reviewer>李四</Reviewer>\n" +
				"    <DataSource>1</DataSource>\n" +
				"    <OpenId></OpenId>\n" +
				"\t<Seller>\n" +
				"\t\t<Identifier>500102010003697</Identifier>\n" +
				"\t\t<Name>升级版测试用户3697</Name>\n" +
				"\t\t<Address>漕宝路</Address>\n" +
				"\t\t<TelephoneNo>021-23443453</TelephoneNo>\n" +
				"\t\t<Bank>建设银行</Bank>\n" +
				"\t\t<BankAcc>34567865435</BankAcc>\n" +
				"\t</Seller>\n" +
				"\t<OrderSize count=\"1\">\n" +
				"\t\t<Order>\n" +
				"\t\t\t<OrderMain>\n" +
				"\t\t\t\t<OrderNo>A001724293</OrderNo>\n" +
				"\t\t\t\t<InvoiceList>0</InvoiceList>\n" +
				"\t\t\t\t<InvoiceSplit>1</InvoiceSplit>\n" +
				"\t\t\t\t<InvoiceSfdy>0</InvoiceSfdy>\n" +
				"\t\t\t\t<OrderDate>2017-08-15 16:58:21</OrderDate>\n" +
				"\t\t\t\t<ChargeTaxWay>0</ChargeTaxWay>\n" +
				"\t\t\t\t<TotalAmount>201.9</TotalAmount>\n" +
				"\t\t\t\t<TaxMark>1</TaxMark>\n" +
				"\t\t\t\t<Remark></Remark>\n" +
				"\t\t\t\t<ExtractedCode>00117d140d0d04068</ExtractedCode>\n" +
				"\t\t\t\t<Buyer>\n" +
				"\t\t\t\t\t<CustomerType>0</CustomerType>\n" +
				"\t\t\t\t\t<Identifier>91370600050948561M</Identifier>\n" +
				"\t\t\t\t\t<Name>个人</Name>\n" +
				"\t\t\t\t\t<Address>徐家汇</Address>\n" +
				"\t\t\t\t\t<TelephoneNo>200123455</TelephoneNo>\n" +
				"\t\t\t\t\t<Bank>中国银行</Bank>\n" +
				"\t\t\t\t\t<BankAcc>40023154555</BankAcc>\n" +
				"\t\t\t\t\t<Email>kangzhongxu@datarj.com</Email>\n" +
				"\t\t\t\t\t<IsSend>1</IsSend>\n" +
				"\t\t\t\t\t<Recipient></Recipient>\n" +
				"\t\t\t\t\t<ReciAddress></ReciAddress>\n" +
				"\t\t\t\t\t<Zip></Zip>\n" +
				"\t\t\t\t</Buyer>\n" +
				"\t\t\t</OrderMain>\n" +
				"\t\t\t<OrderDetails count=\"9\">\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1030303000000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>白熊啤酒330ml</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t2\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t16.9\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>33.8</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>4.91111113</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>33.8</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1010303020100000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>兰妃鸡蛋10枚</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t1\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t20.8\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>20.8</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>0</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>20.8</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1030307990000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>可尔必思原味乳酸饮料335ml</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t2\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t10.9\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>21.8</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>3.16752143</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>21.8</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1040107020000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>G-Super大号袋</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t1\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t0.5\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>0.5</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>0.0726495</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>0.5</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1070223010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>惠润柔净洗发露（绿野芳香）600ml</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t1\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t62\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>62</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>9.00854701</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>62</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1010115010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>菲律宾香蕉（YD)</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t1.06\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t13.58490566\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>14.4</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>1.42702703</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>14.4</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1030306000000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>Creamel凯利草莓味甜酒200ml</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t1\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t15.8\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>15.8</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>2.29572658</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>15.8</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1030306000000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>Creamel凯利太妃味甜酒200ml</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t1\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t15.8\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>15.8</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>2.29572658</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>15.8</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1010115010000000000</ProductCode>\n" +
				"\t\t\t\t\t<ProductName>201飞机盒包装（YD)</ProductName>\n" +
				"\t\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t\t<Quantity>\n" +
				"\t\t\t\t\t\t17\n" +
				"\t\t\t\t\t</Quantity>\n" +
				"                    <UnitPrice>\n" +
				"\t\t\t\t\t\t1\n" +
				"\t\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t\t<Amount>17</Amount>\n" +
				"\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				"\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
				"\t\t\t\t\t<TaxAmount>1.68468465</TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>17</MxTotalAmount>\n" +
				"\t\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t</OrderDetails>\n" +
				"            <Payments>\n" +
				"                <PaymentItem>\n" +
				"                    <PayCode>PT_20</PayCode>\n" +
				"                    <PayPrice>201.9</PayPrice>\n" +
				"                </PaymentItem>\n" +
				"            </Payments>\n" +
				"\t\t</Order>\n" +
				"\t</OrderSize>\n" +
				"</Request>";
		String Secret = getSign(InvoiceData,"08d39fb70c08eef4b4e92a5ec94fee90");
		Map param = new HashMap();
		param.put("methodName", "UploadOrderData");
		param.put("AppKey","RJe766d3c9293b");
		param.put("Secret", Secret);
		param.put("Operation","01");
		param.put("InvoiceData",InvoiceData);
		String jsonString = mapper.writeValueAsString(param);

		BufferedReader reader = null;

		try {
			String strMessage = "";
			StringBuffer buffer = new StringBuffer();

			// 接报文的地址
			URL uploadServlet = new URL(
					"http://localhost:8080/service");
			/*URL uploadServlet = new URL(
					"http://test.datarj.com/webService/service");*/
			HttpURLConnection servletConnection = (HttpURLConnection) uploadServlet
					.openConnection();
			// 设置连接参数
			servletConnection.setRequestProperty("Content-Type", "plain/text; charset=UTF-8");
			servletConnection.setRequestMethod("POST");
			servletConnection.setDoOutput(true);
			servletConnection.setDoInput(true);
			servletConnection.setAllowUserInteraction(true);
			// 开启流，写入XML数据
			OutputStream output = servletConnection.getOutputStream();
			System.out.println("发送的报文：");
			System.out.println(jsonString.toString());

			output.write(jsonString.toString().getBytes());
			output.flush();
			output.close();

			// 获取返回的数据
			InputStream inputStream = servletConnection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			while ((strMessage = reader.readLine()) != null) {
				buffer.append(strMessage);
			}

			System.out.println("接收返回值:" + buffer);

		} catch (java.net.ConnectException e) {
			throw new Exception();
		} finally {
			if (reader != null) {
				reader.close();
			}

		}
	}

	private static String getSign(String QueryData,String key){
		String signSourceData = "data=" + QueryData + "&key=" + key;
		String newSign =  DigestUtils.md5Hex(signSourceData);
		return newSign;
	}
	public static void main(String args[]) throws Exception{
		HttpUtils  tt = new HttpUtils();
		tt.sendMessage() ;
	}
}