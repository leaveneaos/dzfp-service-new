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
				"\t\"seller\": {\n" +
				"\t\t\"address\": \"浙江省杭州市江干区兴庆路32号新罗商务酒店1层\",\n" +
				"\t\t\"bank\": \"鞍山农村商业银行股份有限公司对炉分理处\",\n" +
				"\t\t\"bankAcc\": \"3534534\",\n" +
				"\t\t\"client\": [{\n" +
				"\t\t\t\"certiCipher\": \"证书\",\n" +
				"\t\t\t\"clientNO\": \"开票1\",\n" +
				"\t\t\t\"equipNum\": \"145245456454\",\n" +
				"\t\t\t\"name\": \"测试\",\n" +
				"\t\t\t\"taxDiskPass\": \"密码\",\n" +
				"\t\t\t\"taxEquip\": \"1\"\n" +
				"\t\t}],\n" +
				"\t\t\"drawer\": \"子默\",\n" +
				"\t\t\"identifier\": \"123451524524515\",\n" +
				"\t\t\"issueType\": \"01\",\n" +
				"\t\t\"name\": \"测试\",\n" +
				"\t\t\"payee\": \"收款人\",\n" +
				"\t\t\"reviewer\": \"复核人\",\n" +
				"\t\t\"telephoneNo\": \"18657134440\"\n" +
				"\t},\n" +
				"\t\"sign\": \"b2e5bae7a5f636ed0fa7a866db7bd3d6\"\n" +
				"}";

		String seller ="{\"appId\":\"RJf5d8acdbbbcf\",\"seller\":{\"identifier\":\"876478787232989\",\"type\":\"01\",\"name\":\"的方式当3\",\"address\":\"fand得到dd\",\"telephoneNo\":\"18738727124\",\"bank\":\"f韩国国会\",\"bankAcc\":\"韩国国会\",\"issueType\":\"01\",\"drawer\":\"的方式当\",\"yidentifier\":\"876478787232988\"},\"sign\":\"0e731036a3bb3dbd1071e9867f77f712\"}";

		String client ="{\n" +
				"  \"appId\":\"RJf5d8acdbbbcf\",\n" +
				"  \"sign\":\"fjfjfjfjfjfjfjfjf\",\n" +
				"   \"client\": {\n" +
				"\"identifier\": \"310101123456790\",\n" +
				"\"type\": \"02\",\n" +
				"      \"clientNO\": \"KP002\",\n" +
				"      \"name\": \"陆家嘴3店\",\n" +
				//"      \"brandCode\": \"pp01\",\n" +
				"      \"taxEquip\": \"1\",\n" +
				"      \"equipNum\": \"499000135091\",\n" +
				"      \"taxDiskPass\": \"税控盘密码\",\n" +
				"      \"certiCipher\": \"证书密码\"\n" +
				"    }\n" +
				"}\n";
		HashMap<String, Object> jsonObject = JSON.parseObject(client,LinkedHashMap.class, Feature.OrderedField);
		JSONObject Seller =  (JSONObject)jsonObject.get("client");
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
					"http://localhost:8080/initService/initialClientData");
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