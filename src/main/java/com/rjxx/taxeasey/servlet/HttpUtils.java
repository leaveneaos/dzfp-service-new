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
				"\t<Identifier>310101123456783</Identifier>\n" +
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
				"\t\t\t<ClientNO>gvc_11</ClientNO>\n" +
				"\t\t\t<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002，String（40）-->\n" +
				"\t\t\t<Name>陆家嘴1店</Name>\n" +
				"\t\t\t<!--必须,开票点名称String（40）-->\n" +
				"<BrandCode>pp03</BrandCode>\n" +
				"\t\t\t<!--非必须,品牌代码，保证公司下唯一String（50）-->\n" +
				"\t\t\t<BrandName>火狐01</BrandName>\n" +
				"\t\t\t<!--非必须,品牌名称， String（50）-->\n"+
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


		String xml01 = "<Request>\n" +
				" \t<ClientNO>ceshi5074</ClientNO>\n" +
				" \t<SerialNumber>062523500801797110552</SerialNumber>\n" +
				" \t<InvType>02</InvType>\n" +
				" \t<Spbmbbh>13.0</Spbmbbh>\n" +
				" \t<Drawer>谭凯</Drawer>\n" +
				" \t<Payee>张冲</Payee>\n" +
				" \t<Reviewer>周磊</Reviewer>\n" +
				"     <DataSource>5</DataSource>\n" +
				"     <OpenId>2088002500367799</OpenId>\n" +
				" \t<Seller>\n" +
				" \t\t<Identifier>500102010003697</Identifier>\n" +
				" \t\t<Name>升级版测试用户3697</Name>\n" +
				" \t\t<Address>苏州工业园区金鸡湖大道1355号国际科技园四期A1202</Address>\n" +
				" \t\t<TelephoneNo>0512-69171090</TelephoneNo>\n" +
				" \t\t<Bank>中国民生银行苏州分行</Bank>\n" +
				" \t\t<BankAcc>2601014180001564</BankAcc>\n" +
				" \t</Seller>\n" +
				" \t<OrderSize count=\"1\">\n" +
				" \t\t<Order>\n" +
				" \t\t\t<OrderMain>\n" +
				" \t\t\t\t<OrderNo>062523500801797110552</OrderNo>\n" +
				" \t\t\t\t<InvoiceList>0</InvoiceList>\n" +
				" \t\t\t\t<InvoiceSplit>1</InvoiceSplit>\n" +
				" \t\t\t\t<InvoiceSfdy>0</InvoiceSfdy>\n" +
				" \t\t\t\t<OrderDate>2018-06-25 18:11:38</OrderDate>\n" +
				" \t\t\t\t<ChargeTaxWay>0</ChargeTaxWay>\n" +
				" \t\t\t\t<TotalAmount>0.01</TotalAmount>\n" +
				"                 <TotalDiscount>0</TotalDiscount>\n" +
				"                 <TaxMark>1</TaxMark>\n" +
				" \t\t\t\t<Remark>交易小票号:79711054;商品折扣金额:-4.70;支付折扣:30.19;店名:科技园二店</Remark>\n" +
				" \t\t\t\t<ExtractedCode></ExtractedCode>\n" +
				" \t\t\t\t<Buyer>\n" +
				" \t\t\t\t\t<CustomerType>0</CustomerType>\n" +
				" \t\t\t\t\t<Identifier></Identifier>\n" +
				" \t\t\t\t\t<Name><![CDATA[石焱]]></Name>\n" +
				" \t\t\t\t\t<Address></Address>\n" +
				" \t\t\t\t\t<TelephoneNo></TelephoneNo>\n" +
				" \t\t\t\t\t<Bank></Bank>\n" +
				" \t\t\t\t\t<BankAcc></BankAcc>\n" +
				" \t\t\t\t\t<Email></Email>\n" +
				" \t\t\t\t\t<IsSend></IsSend>\n" +
				" \t\t\t\t\t<Recipient></Recipient>\n" +
				" \t\t\t\t\t<ReciAddress></ReciAddress>\n" +
				" \t\t\t\t\t<Zip></Zip>\n" +
				"                     <MobilephoneNo></MobilephoneNo>\n" +
				" \t\t\t\t</Buyer>\n" +
				" \t\t\t</OrderMain>\n" +
				" \t\t\t<OrderDetails count=\"8\">\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030307010000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[可口可乐中Ｐ]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>2</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>1</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>4.2</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>4.2</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount>\n" +
				" \t\t\t\t\t</TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>4.2</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030307010000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[可口可乐中Ｐ]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>1</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>\n" +
				" \t\t\t\t\t</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>\n" +
				" \t\t\t\t\t</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>-4.2</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount></TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>-4.2</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030201020000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[椰丝餐包]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>2</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>1</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>8</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>8</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount></TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>8</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030201020000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[椰丝餐包]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>1</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>\n" +
				" \t\t\t\t\t</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>\n" +
				" \t\t\t\t\t</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>-8</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount></TaxAmount>\n" +
				"\t\t\t\t\t<MxTotalAmount>-8</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				"\t\t\t\t</ProductItem>\n" +
				"\t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030201030000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[丽芝士威化饼干]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>2</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>\n" +
				" \t\t\t\t\t</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>\n" +
				" \t\t\t\t\t</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>14.70</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount></TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>14.70</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030201030000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[丽芝士威化饼干]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>1</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>\n" +
				" \t\t\t\t\t</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>\n" +
				" \t\t\t\t\t</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>-14.69</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount></TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>-14.69</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030201020000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[火腿芝士厚烧吐司]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>2</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>1</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>8</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>8</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount>\n" +
				" \t\t\t\t\t</TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>8</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t\t<ProductItem>\n" +
				" \t\t\t\t\t<VenderOwnCode></VenderOwnCode>\n" +
				" \t\t\t\t\t<ProductCode>1030201020000000000</ProductCode>\n" +
				" \t\t\t\t\t<ProductName><![CDATA[火腿芝士厚烧吐司]]></ProductName>\n" +
				" \t\t\t\t\t<RowType>1</RowType>\n" +
				" \t\t\t\t\t<Spec></Spec>\n" +
				" \t\t\t\t\t<Unit></Unit>\n" +
				" \t\t\t\t\t<Quantity>\n" +
				" \t\t\t\t\t</Quantity>\n" +
				" \t\t\t\t\t<UnitPrice>\n" +
				" \t\t\t\t\t</UnitPrice>\n" +
				" \t\t\t\t\t<Amount>-8</Amount>\n" +
				" \t\t\t\t\t<DeductAmount></DeductAmount>\n" +
				" \t\t\t\t\t<TaxRate>0.16</TaxRate>\n" +
				" \t\t\t\t\t<TaxAmount></TaxAmount>\n" +
				" \t\t\t\t\t<MxTotalAmount>-8</MxTotalAmount>\n" +
				" \t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
				" \t\t\t\t\t<TaxRateMark></TaxRateMark>\n" +
				" \t\t\t\t\t<PolicyName></PolicyName>\n" +
				" \t\t\t\t</ProductItem>\n" +
				" \t\t\t</OrderDetails>\n" +
				"             <Payments></Payments>\n" +
				" \t\t</Order>\n" +
				" \t</OrderSize>\n" +
				" </Request>";
		String Secret = getSign(xml01,"5aab6270b7042aef2098b8fbb005097e");
		Map param = new HashMap();
		param.put("methodName", "UploadOrderData");
		param.put("AppKey","RJf5d8acdbbbcf");
		param.put("Secret", Secret);
		param.put("Operation","01");
		param.put("InvoiceData",xml01);
		String jsonString = mapper.writeValueAsString(param);

		BufferedReader reader = null;

		try {
			String strMessage = "";
			StringBuffer buffer = new StringBuffer();

			// 接报文的地址
			/*URL uploadServlet = new URL(
					"http://localhost:8080/service");*/
			URL uploadServlet = new URL(
					"http://test.datarj.com/webService/service");
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