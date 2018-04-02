package com.rjxx.taxeasey.servlet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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


		String xml01 = "\n" +
				"\t\t\t\t<Request>\n" +
				"\t\t\t\t<ClientNO>alipay_02</ClientNO>\n" +
				"\t\t\t\t<SerialNumber>JY21dddd2243</SerialNumber>\n" +
				"\t\t\t\t<InvType>12</InvType>\n" +
				"\t\t\t\t<Spbmbbh>13.0</Spbmbbh>\n" +
				"\t\t\t\t<Drawer>刘先生</Drawer>\n" +
				"\t\t\t\t<Payee>王五</Payee>\n" +
				"\t\t\t\t<Reviewer>李四</Reviewer>\n" +
				"\t\t\t\t    <DataSource>1</DataSource>\n" +
				"\t\t\t\t    <OpenId></OpenId>\n" +
				"\t\t\t\t<Seller>\n" +
				"\t\t\t\t<Identifier>500102010003643</Identifier>\n" +
				"\t\t\t\t<Name>上海百旺测试3643</Name>\n" +
				"\t\t\t\t<Address>漕宝路</Address>\n" +
				"\t\t\t\t<TelephoneNo>021-23443453</TelephoneNo>\n" +
				"\t\t\t\t<Bank>建设银行</Bank>\n" +
				"\t\t\t\t<BankAcc>34567865435</BankAcc>\n" +
				"\t\t\t\t</Seller>\n" +
				"\t\t\t\t<OrderSize count=\"1\">\n" +
				"\t\t\t\t<Order>\n" +
				"\t\t\t\t<OrderMain>\n" +
				"\t\t\t\t<OrderNo>A001724293</OrderNo>\n" +
				"\t\t\t\t<InvoiceList>0</InvoiceList>\n" +
				"\t\t\t\t<InvoiceSplit>1</InvoiceSplit>\n" +
				"\t\t\t\t<InvoiceSfdy>0</InvoiceSfdy>\n" +
				"\t\t\t\t<OrderDate>2017-08-15 16:58:21</OrderDate>\n" +
				"\t\t\t\t<ChargeTaxWay>2</ChargeTaxWay>\n" +
				"\t\t\t\t<TotalAmount>1840.1</TotalAmount>\n" +
				"\t\t\t\t<TaxMark>0</TaxMark>\n" +
				"\t\t\t\t<Remark>真的好开心呀</Remark>\n" +
				"\t\t\t\t<ExtractedCode></ExtractedCode>\n" +
				"\t\t\t\t<Buyer>\n" +
				"\t\t\t\t<CustomerType>0</CustomerType>\n" +
				"\t\t\t\t<Identifier>91370600050948561M</Identifier>\n" +
				"\t\t\t\t<Name>个人</Name>\n" +
				"\t\t\t\t<Address>徐家汇</Address>\n" +
				"\t\t\t\t<TelephoneNo>200123455</TelephoneNo>\n" +
				"\t\t\t\t<Bank>中国银行</Bank>\n" +
				"\t\t\t\t<BankAcc>40023154555</BankAcc>\n" +
				"\t\t\t\t<Email>ch-unjie_.p-an@boge-rubb_er-plastics.com</Email>\n" +
				"\t\t\t\t<IsSend>1</IsSend>\n" +
				"\t\t\t\t<Recipient></Recipient>\n" +
				"\t\t\t\t<ReciAddress></ReciAddress>\n" +
				"\t\t\t\t<Zip></Zip>\n" +
				"\t\t\t\t</Buyer>\n" +
				"\t\t\t\t</OrderMain>\n" +
				"\t\t\t\t<OrderDetails count=\"2\">\n" +
				"\t\t\t\t<ProductItem>\n" +
				"\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				"\t\t\t\t<ProductCode>1030306000000000000</ProductCode>\n" +
				"\t\t\t\t<ProductName>Creamel凯利太妃味甜酒200ml</ProductName>\n" +
				"\t\t\t\t<RowType>0</RowType>\n" +
				"\t\t\t\t<Spec></Spec>\n" +
				"\t\t\t\t<Unit></Unit>\n" +
				"\t\t\t\t<Quantity>\n" +
				"\t\t\t\t1\n" +
				"\t\t\t\t</Quantity>\n" +
				"\t\t\t\t                    <UnitPrice>\n" +
				"\t\t\t\t1580\n" +
				"\t\t\t\t</UnitPrice>\n" +
				"\t\t\t\t<Amount>1580</Amount>\n" +
				"\t\t\t\t<DeductAmount>50</DeductAmount>\n" +
				"\t\t\t\t<TaxRate>0.17</TaxRate>\n" +
				"\t\t\t\t<TaxAmount>260.10</TaxAmount>\n" +
				"\t\t\t\t<MxTotalAmount>1840.1</MxTotalAmount>\n" +
				"\t\t\t\t<PolicyMark></PolicyMark>\n" +
				"\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				"\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t</OrderDetails>\n" +

				"\t\t\t\t</Order>\n" +
				"\t\t\t\t</OrderSize>\n" +
				"\t\t\t\t</Request>";
		String Secret = getSign(xml01,"a4bc50406ca43cad291be7818364bf10");
		Map param = new HashMap();
		param.put("methodName", "UploadOrderData");
		param.put("AppKey","RJf046355349b8");
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