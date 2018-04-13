package com.rjxx.taxeasey.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpInitServiceUtils {

	public void sendMessage() throws Exception {
		System.out.println("调用servlet开始=================");
		String data ="{\n" +
				"  \"appId\":\"RJf046355349b8\",\n" +
				"  \"sign\":\"fjfjfjfjfjfjfjfjf\",\n" +
				"  \"seller\": {\n" +
				"    \"identifier\": \"310101123456792\",\n" +
				"    \"name\": \"发票开具方名称\",\n" +
				"    \"address\": \"某某路10号1203室\",\n" +
				"    \"telephoneNo\": \"021-55555555\",\n" +
				"    \"bank\": \"中国建设银行打浦桥支行\",\n" +
				"    \"bankAcc\": \"123456789-0\",\n" +
				"    \"ybnsrqssj\": \"201803\",\n" +
				"    \"ybnsrlx\": \"2\",\n" +
				"    \"drawer\": \"开票人\",\n" +
				"    \"payee\": \"收款人\",\n" +
				"    \"reviewer\": \"复核人\",\n" +
				"    \"issueType\": \"01\",\n" +
				"    \"eticketLim\": \"9999.99\",\n" +
				"    \"specialticketLim\": \"9999.99\",\n" +
				"    \"ordinaryticketLim\": \"9999.99\",\n" +
				"    \"client\": [{\n" +
				"      \"clientNO\": \"KP002\",\n" +
				"      \"name\": \"陆家嘴1店\",\n" +
				"      \"brandCode\": \"pp06\",\n" +
				"      \"brandName\": \"火狐01\",\n" +
				"      \"taxEquip\": \"1\",\n" +
				"      \"equipNum\": \"499000135091\",\n" +
				"      \"taxDiskPass\": \"税控盘密码\",\n" +
				"      \"certiCipher\": \"证书密码\"\n" +
				"    }]\n" +
				"  }\n" +
				"}\n";

		String seller ="{\"appId\":\"RJ9015cfe90f14\",\"seller\":{\"identifier\":\"fdrtgfghty01531\",\"name\":\"2112\",\"address\":\"12\",\"telephoneNo\":\"15021233087\",\"bank\":\"1212\",\"bankAcc\":\"322323\",\"yidentifier\":\"887776776556689\"},\"sign\":\"144b60669c79e1218eaef4ab2390e5c7\"}";

		String client ="{\n" +
				"  \"appId\":\"RJf046355349b8\",\n" +
				"  \"sign\":\"fjfjfjfjfjfjfjfjf\",\n" +
				"   \"client\": {\n" +
				"\"identifier\": \"310101123456790\",\n" +
				"\"type\": \"02\",\n" +
				"      \"clientNO\": \"KP002\",\n" +
				"      \"name\": \"陆家嘴3店\",\n" +
				"      \"brandCode\": \"pp01\",\n" +
				"      \"taxEquip\": \"1\",\n" +
				"      \"equipNum\": \"499000135091\",\n" +
				"      \"taxDiskPass\": \"税控盘密码\",\n" +
				"      \"certiCipher\": \"证书密码\"\n" +
				"    }\n" +
				"}\n";
		HashMap<String, JSONObject> jsonObject = JSON.parseObject(seller,LinkedHashMap.class, Feature.OrderedField);
		JSONObject Seller =  jsonObject.get("seller");
		 //Seller = jsonObject.getJSONObject("seller");
		String Sign = getSign( Seller.toString(),"b3f6e805c65f39a2c25e604eb1b08740");
		JSONObject jsonObject1 = JSON.parseObject(seller);
		jsonObject1.put("sign", Sign);
		String s = JSON.toJSONString(jsonObject);
		Map param = new HashMap();
		ObjectMapper mapper = new ObjectMapper();
		//CommonData commonData=mapper.readValue(data, CommonData.class);
		//commonData.setSign(Sign);
		//String jsonString = JSONUtils.toJSONString(commonData);

		BufferedReader reader = null;

		try {
			String strMessage = "";
			StringBuffer buffer = new StringBuffer();

			// 接报文的地址
			URL uploadServlet = new URL(
					"http://localhost:8080/initService/sellerDataUpdate");
			/*URL uploadServlet = new URL(
					"http://test.datarj.com/webService/initService/sellerDataUpdate");*/
			HttpURLConnection servletConnection = (HttpURLConnection) uploadServlet
					.openConnection();
			// 设置连接参数
			servletConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			servletConnection.setRequestMethod("POST");
			servletConnection.setDoOutput(true);
			servletConnection.setDoInput(true);
			servletConnection.setAllowUserInteraction(true);
			// 开启流，写入XML数据
			OutputStream output = servletConnection.getOutputStream();
			System.out.println("发送的报文：");
			System.out.println(s.toString());

			output.write(s.toString().getBytes());
			output.flush();
			output.close();

			// 获取返回的数据
			InputStream inputStream = servletConnection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			while ((strMessage = reader.readLine()) != null) {
				buffer.append(strMessage);
			}

			System.out.println("接收返回值:" + buffer.toString().replaceAll("\\\\",""));

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
		HttpInitServiceUtils tt = new HttpInitServiceUtils();
		tt.sendMessage() ;
		String signSourceData = "data={\"identifier\":\"fdrtgfghty01531\",\"name\":\"2112\",\"address\":\"12\",\"telephoneNo\":\"15021233087\",\"bank\":\"1212\",\"bankAcc\":\"322323\",\"yidentifier\":\"887776776556689\"}&key=b3f6e805c65f39a2c25e604eb1b08740";
		String newSign =  DigestUtils.md5Hex(signSourceData);

		System.out.println(newSign);
		System.out.println(getSign("{\"identifier\":\"fdrtgfghty01531\",\"name\":\"2112\",\"address\":\"12\",\"telephoneNo\":\"15021233087\",\"bank\":\"1212\",\"bankAcc\":\"322323\",\"yidentifier\":\"887776776556689\"}","b3f6e805c65f39a2c25e604eb1b08740"));
	}
}