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
				"\t\"appId\": \"RJf5d8acdbbbcf\",\n" +
				"\t\"sign\": \"7c2aaad98fea78e182e789abfb75f6b9\",\n" +
				"\t\"seller\": {\n" +
				"\t\t\"identifier\": \"500102000000385\",\n" +
				"\t\t\"name\": \"测试企业用户385\",\n" +
				"\t\t\"address\": \"福建福州\",\n" +
				"\t\t\"telephoneNo\": \"0591-83512152\",\n" +
				"\t\t\"bank\": \"中国银行\",\n" +
				"\t\t\"bankAcc\": \"66666666\",\n" +
				"\t\t\"ybnsrqssj\": \"201805\",\n" +
				"\t\t\"ybnsrlx\": \"2\",\n" +
				"\t\t\"drawer\": \"开票人\",\n" +
				"\t\t\"payee\": \"收款人\",\n" +
				"\t\t\"reviewer\": \"复核人\",\n" +
				"\t\t\"issueType\": \"04\",\n" +
				"\t\t\"eticketLim\": null,\n" +
				"\t\t\"specialticketLim\": \"999999.99\",\n" +
				"\t\t\"ordinaryticketLim\": \"999999.99\",\n" +
				"\t\t\"rollticketLim\": \"999999.99\",\n" +
				"\t\t\"client\": [{\n" +
				"\t\t\t\"name\": \"开票通\",\n" +
				"\t\t\t\"brandCode\": null,\n" +
				"\t\t\t\"brandName\": null,\n" +
				"\t\t\t\"deviceSN\": \"0302011170100002\",\n" +
				"\t\t\t\"devicePSWD\": \"123456\",\n" +
				"\t\t\t\"deviceKEY\": \"02F0D811C577DAC21AC85E2571DB2C9E33644A41C3A925347A51A5E36A1EF69C\",\n" +
				"\t\t\t\"taxEquip\": \"1\",\n" +
				"\t\t\t\"equipNum\": \"499000105369\",\n" +
				"\t\t\t\"taxDiskPass\": \"88888888\",\n" +
				"\t\t\t\"certiCipher\": \"00000000\"\n" +
				"\t\t}]\n" +
				"\t}\n" +
				"}";

		String seller ="{\"appId\":\"RJf5d8acdbbbcf\",\"seller\":{\"identifier\":\"876478787232989\",\"type\":\"01\",\"name\":\"的方式当3\",\"address\":\"fand得到dd\",\"telephoneNo\":\"18738727124\",\"bank\":\"f韩国国会\",\"bankAcc\":\"韩国国会\",\"issueType\":\"01\",\"drawer\":\"的方式当\",\"yidentifier\":\"876478787232988\"},\"sign\":\"0e731036a3bb3dbd1071e9867f77f712\"}";

		String client ="{\"appId\":\"RJ5be0d41cb531\",\"sign\":\"5fa1199eda5102a7f16637071e72701b\",\"client\":{\"identifier\":\"201704140000000017\",\"type\":\"01\",\"clientNO\":\"11\",\"name\":\"3dd的的\",\"taxEquip\":\"1\",\"equipNum\":\"499000135091\",\"taxDiskPass\":\"534533\",\"certiCipher\":\"34235\"}}\n";
		HashMap<String, Object> jsonObject = JSON.parseObject(data,LinkedHashMap.class, Feature.OrderedField);
		JSONObject Seller =  (JSONObject)jsonObject.get("seller");
		 //Seller = jsonObject.getJSONObject("seller");
		String Sign = getSign( Seller.toString(),"5aab6270b7042aef2098b8fbb005097e");
		//JSONObject jsonObject1 = JSON.parseObject(seller);
		jsonObject.put("sign", Sign);
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
					"http://localhost:8080/initService/initialData");
			/*URL uploadServlet = new URL(
					"http://test.datarj.com/webService/kptService");*/
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

		//System.out.println(newSign);
		System.out.println(getSign("{\"identifier\":\"fdrtgfghty01531\",\"name\":\"2112\",\"address\":\"12\",\"telephoneNo\":\"15021233087\",\"bank\":\"1212\",\"bankAcc\":\"322323\",\"yidentifier\":\"887776776556689\"}","b3f6e805c65f39a2c25e604eb1b08740"));
	}
}