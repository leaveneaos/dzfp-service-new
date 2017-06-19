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
		String InvoiceData="<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<Request>"
				+ "<ClientNO>kp011</ClientNO>"
				+ "<SerialNumber>19223214</SerialNumber>"
				+ "<InvType>02</InvType>"
				+ "<ServiceType>0</ServiceType>"
				+ "<Drawer>qian</Drawer>"
				+ "<Payee/>"
				+ "<Reviewer/>"
				+ "<Seller>"
				+ "<Identifier>500102010003697</Identifier>"
				+ "<Name>升级版测试用户3697</Name>"
				+ "<Address>测试南京湖南路亚朵酒店</Address>"
				+ "<TelephoneNo>020-123456768</TelephoneNo>"
				+ "<Bank>中国农业银行</Bank>"
				+ "<BankAcc>03399500040009801</BankAcc>"
				+ "</Seller>"
				+ "<OrderSize count=\"1\">"
				+ "<Order>"
				+ "<OrderMain>"
				+ "<OrderNo>128_932393</OrderNo>"
				+ "<InvoiceList>0</InvoiceList>"
				+ "<InvoiceSplit>0</InvoiceSplit>"
				+ "<OrderDate>2017-03-09 15:33:00</OrderDate>"
				+ "<ChargeTaxWay>0</ChargeTaxWay>"
				+ "<TotalAmount>30780.08</TotalAmount>"
				+ "<TaxMark>0</TaxMark>"
				+ "<Remark/>"
				+ "<ExtractedCode></ExtractedCode>"
				+ "<Buyer>"
				+ "<Identifier>123456789013234568</Identifier>"
				+ "<CustomerType>0</CustomerType>"
				+ "<Name>商超功能测试用户</Name>"
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
				+ "<Recipient>0</Recipient>"
				+ "<ReciAddress/>"
				+ "<Zip/>"
				+ "</Buyer>"
				+ "</OrderMain>"
				+ "<OrderDetails count=\"4\">"
				+ "<ProductItem>"
				+ "<ProductCode>3070402000000000000</ProductCode>"
				+ "<ProductName>住宿服务费</ProductName>"
				+ "<RowType>0</RowType>"
				+ "<Spec/>"
				+ "<Unit>次</Unit>"
				+ "<Quantity>1</Quantity>"
				+ "<UnitPrice>10000</UnitPrice>"
				+ "<Amount>10000</Amount>"
				+ "<DeductAmount>0</DeductAmount>"
				+ "<TaxRate>0.06</TaxRate>"
				+ "<TaxAmount>600</TaxAmount>"
				+ "<MxTotalAmount>10600</MxTotalAmount>"
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
				+ "<ProductItem>"
				+ "<ProductCode>3070401000000000000</ProductCode>"
				+ "<ProductName>餐饮住宿服务费</ProductName>"
				+ "<RowType>2</RowType>"
				+ "<Spec/>"
				+ "<Unit>次</Unit>"
				+ "<Quantity>1</Quantity>"
				+ "<UnitPrice>18868</UnitPrice>"
				+ "<Amount>18868</Amount>"
				+ "<DeductAmount>0</DeductAmount>"
				+ "<TaxRate>0.06</TaxRate>"
				+ "<TaxAmount>1132.08</TaxAmount>"
				+ "<MxTotalAmount>20000.08</MxTotalAmount>"
				+ "<VenderOwnCode/>"
				+ "<PolicyMark>0</PolicyMark>"
				+ "<TaxRateMark/>"
				+ "<PolicyName/>"
				+ "</ProductItem>"
				+ "<ProductItem>"
				+ "<ProductCode>3070401000000000000</ProductCode>"
				+ "<ProductName>餐饮住宿服务费</ProductName>"
				+ "<RowType>1</RowType>"
				+ "<Spec/>"
				+ "<Unit>次</Unit>"
				+ "<Quantity>-1</Quantity>"
				+ "<UnitPrice>18.87</UnitPrice>"
				+ "<Amount>-18.87</Amount>"
				+ "<DeductAmount>0</DeductAmount>"
				+ "<TaxRate>0.06</TaxRate>"
				+ "<TaxAmount>-1.13</TaxAmount>"
				+ "<MxTotalAmount>-20.00</MxTotalAmount>"
				+ "<VenderOwnCode/>"
				+ "<PolicyMark>0</PolicyMark>"
				+ "<TaxRateMark/>"
				+ "<PolicyName/>"
				+ "</ProductItem>"
				+ "</OrderDetails>"
   			/*+ "<Payments>"
   			    + "<PaymentItem>"
 			       + "<PayPrice>29800.08</PayPrice>"
 			        + "<!--支付金额-->"
 			       + "<PayCode>01</PayCode>"
 			       + "<!--支付方式，01现金支付、02支付宝支付、03积分支付、04储蓄卡支付、05银联卡支付-->"
 			   + "</PaymentItem>"
 			   + "<PaymentItem>"
 			       + "<PayPrice>980.00</PayPrice>"
 			        + "<!--支付金额-->"
 			       + "<PayCode>03</PayCode>"
 			       + "<!--支付方式，01现金支付、02支付宝支付、03积分支付、04储蓄卡支付、05银联卡支付-->"
 			   + "</PaymentItem>"
 			+ "</Payments>"*/
				+ "</Order>"
				+ "</OrderSize>"
				+ "</Request>";


		String xml01 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<Request>"
				+ "<ClientNO>tujia_01</ClientNO>"
				+ "<SerialNumber>310</SerialNumber>"
				+ "<InvType>02</InvType>"
				+ "<ServiceType>0</ServiceType>"
				+ "<Drawer>qian</Drawer>"
				+ "<Payee/>"
				+ "<Reviewer/>"
				+ "<Seller>"
				+ "<Identifier>500102010003643</Identifier>"
				+ "<Name>上海百旺测试3643</Name>"
				+ "<Address>测试南京湖南路亚朵酒店</Address>"
				+ "<TelephoneNo>020-123456768</TelephoneNo>"
				+ "<Bank>中国农业银行</Bank>"
				+ "<BankAcc>03399500040009801</BankAcc>"
				+ "</Seller>"
				+ "<OrderSize count=\"1\">"
				+ "<Order>"
				+ "<OrderMain>"
				+ "<OrderNo>14_2082</OrderNo>"
				+ "<InvoiceList>0</InvoiceList>"
				+ "<InvoiceSplit>0</InvoiceSplit>"
				+ "<OrderDate>2017-01-10 15:33:00</OrderDate>"
				+ "<ChargeTaxWay>0</ChargeTaxWay>"
				+ "<TotalAmount>21.00</TotalAmount>"
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
				+ "<UnitPrice>18.87</UnitPrice>"
				+ "<Amount>18.87</Amount>"
				+ "<DeductAmount>0</DeductAmount>"
				+ "<TaxRate>0.06</TaxRate>"
				+ "<TaxAmount>1.13</TaxAmount>"
				+ "<MxTotalAmount>20.00</MxTotalAmount>"
				+ "<VenderOwnCode/>"
				+ "<PolicyMark>0</PolicyMark>"
				+ "<TaxRateMark/>"
				+ "<PolicyName/>"
				+ "</ProductItem>"
				+ "</OrderDetails>"
				+ "</Order>"
				+ "</OrderSize>"
				+ "</Request>";
		String Secret = getSign(xml01,"8e37be80cd6dcd8051d589d32f4d0ff2");
		Map param = new HashMap();
		param.put("methodName", "UploadOrderData");
		param.put("AppKey","RJ874afd58e67b");
		param.put("Secret", Secret);
		param.put("Operation","01");
		param.put("InvoiceData",xml01);
		String jsonString = mapper.writeValueAsString(param);

		BufferedReader reader = null;

		try {
			String strMessage = "";
			StringBuffer buffer = new StringBuffer();

			// 接报文的地址
			URL uploadServlet = new URL(
					"http://localhost:8080/dzfp-service-new/service");

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