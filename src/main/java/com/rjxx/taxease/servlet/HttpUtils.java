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
		String InvoiceData="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Seller>\n" +
				"\t<Identifier>310101123456780</Identifier>\n" +
				"\t<!--Identifier必须，发票开具方税号String20，15、18或20位-->\n" +
				"\t<Name>发票开具方名称2</Name>\n" +
				"\t<!--Name必须，发票开具方名称String100-->\n" +
				"\t<Address>某某路10号1203室</Address>\n" +
				"\t<!--Address必须，发票开具方地址String100-->\n" +
				"\t<TelephoneNo>021-55555555</TelephoneNo>\n" +
				"\t<!--TelephoneNo必须，发票开具方电话String20-->\n" +
				"\t<Bank>中国建设银行打浦桥支行</Bank>\n" +
				"\t<!--Bank必须，发票开具方银行String100-->\n" +
				"\t<BankAcc>123456789-0</BankAcc>\n" +
				"\t<!--BankAcc必须，发票开具方银行账号String30-->\n" +
				"\t<Drawer>开票人</Drawer>\n" +
				"\t<!--Drawer必须，开票人String20-->\n" +
				"\t<Payee>收款人</Payee>\n" +
				"\t<!--Payee可选，收款人String20-->\n" +
				"\t<Reviewer>复核人</Reviewer>\n" +
				"\t<!--Reviewer可选，复核人String20-->\n" +
				"\t<IssueType>01</IssueType>\n" +
				"\t<!--IssueType必选，开票方式（01税控盘或金税盘开票，03税控盘组或税控服务器开票）String2-->\n" +
				"\t<EticketLim>9999.99</EticketLim>\n" +
				"\t<!--EticketLim可选，电子票开票限额，开具电票必须，即税控盘或金税盘的最大开票限额double（18,2）-->\n" +
				"\t<SpecialticketLim>9999.99</SpecialticketLim>\n" +
				"\t<!--SpecialticketLim可选，专用发票开票限额，开具专票必须，即税控盘或金税盘的最大开票限额double（18,2）-->\n" +
				"\t<OrdinaryticketLim>9999.99</OrdinaryticketLim>\n" +
				"\t<!--RdinaryticketLim可选，普通发票开票限额，开具普票必须，即税控盘或金税盘的最大开票限额double（18,2）-->\n" +
				"\t<Clients size=\"1\">\n" +
				"\t\t<Client>\n" +
				"\t\t\t<ClientNO>gvc_04</ClientNO>\n" +
				"\t\t\t<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002，String（40）-->\n" +
				"\t\t\t<Name>陆家嘴1店</Name>\n" +
				"\t\t\t<!--必须,开票点名称String（40）-->\n" +
				"\t\t\t<TaxEquip>1</TaxEquip>\n" +
				"\t\t\t<!--必须，税控设备厂商,1表示百旺厂商设备，2表示航信厂商设备String（1）-->\n" +
				"\t\t\t<EquipNum>499000135091</EquipNum>\n" +
				"\t\t\t<!--必须，税控设备号，如果是盘则是盘号，服务器则是核心板版号String（12）-->\n" +
				"\t\t\t<TaxDiskPass>税控盘密码</TaxDiskPass>\n" +
				"\t\t\t<!--必须，TaxDiskPass税控盘密码（百旺一般为88888888，航信12345678.准确的需要插盘登录客户端获取）-->\n" +
				"\t\t\t<CertiCipher>证书密码</CertiCipher>\n" +
				"\t\t\t<!--当TaxEquip为1时必须，反之可选，CertiCipher证书密码（百旺一般为12345678.准确的需要插盘登录客户端获取）-->\n" +
				"\t\t</Client>\n" +
				"\t</Clients>\n" +
				"</Seller>\n";


		String xml01 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<Request>\n" +
				"\t<ClientNO>gvc</ClientNO>\n" +
				"\t<SerialNumber>JY21d72d1g261</SerialNumber>\n" +
				"\t<InvType>12</InvType>\n" +
				"\t<Spbmbbh>13.0</Spbmbbh>\n" +
				"\t<Drawer>刘先生</Drawer>\n" +
				"\t<Payee>王五</Payee>\n" +
				"\t<Reviewer>李四</Reviewer>\n" +
				"    <DataSource>1</DataSource>\n" +
				"    <OpenId></OpenId>\n" +
				"\t<Seller>\n" +
				"\t\t<Identifier>500102010003643</Identifier>\n" +
				"\t\t<Name>上海百旺测试3643</Name>\n" +
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
				"\t\t\t\t<Remark>真的好开心呀</Remark>\n" +
				"\t\t\t\t<ExtractedCode></ExtractedCode>\n" +
				"\t\t\t\t<Buyer>\n" +
				"\t\t\t\t\t<CustomerType>0</CustomerType>\n" +
				"\t\t\t\t\t<Identifier>91370600050948561M</Identifier>\n" +
				"\t\t\t\t\t<Name>个人</Name>\n" +
				"\t\t\t\t\t<Address>徐家汇</Address>\n" +
				"\t\t\t\t\t<TelephoneNo>200123455</TelephoneNo>\n" +
				"\t\t\t\t\t<Bank>中国银行</Bank>\n" +
				"\t\t\t\t\t<BankAcc>40023154555</BankAcc>\n" +
				"\t\t\t\t\t<Email>ch-unjie_.p-an@boge-rubb_er-plastics.com</Email>\n" +
				"\t\t\t\t\t<IsSend>1</IsSend>\n" +
				"\t\t\t\t\t<Recipient></Recipient>\n" +
				"\t\t\t\t\t<ReciAddress></ReciAddress>\n" +
				"\t\t\t\t\t<Zip></Zip>\n" +
				"\t\t\t\t</Buyer>\n" +
				"\t\t\t</OrderMain>\n" +
				"\t\t\t<OrderDetails count=\"9\">\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t\t<ProductCode>1010303020100000000</ProductCode>\n" +
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
				"\t\t\t\t\t<ProductCode>1040107020000000000</ProductCode>\n" +
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
				"\t\t\t\t\t<ProductCode>1040107020000000000</ProductCode>\n" +
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
		String Secret = getSign(xml01,"08d39fb70c08eef4b4e92a5ec94fee90");
		Map param = new HashMap();
		param.put("methodName", "UploadOrderData");
		param.put("AppKey","RJe766d3c9293b");
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