package com.rjxx.taxease.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

import com.rjxx.taxeasy.vo.KplsVO4;

public class CallDllWebServiceUtil {
	
	/**
	 * 调用组件接口webservice
	 *
	 * @param String,Map
	 * @return String
	 */
	public String callDllWebSevice(String xml, Map map) {
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		String dllwspath = readFile("DllWebServicePath");
		Client client = dcf.createClient(dllwspath);
		String Operation = (String) map.get("Operation");
		String result = "";
		try {
			if (Operation.equals("01")) {
				KplsVO4 kplsvo4 = (KplsVO4) map.get("kplsvo4");
				String CLIENTNO = kplsvo4.getKpddm();
				String TaxMachineIP = "";
				String SysInvNo = String.valueOf(kplsvo4.getKplsh());
				String InvoiceList = kplsvo4.getSfdyqd();
				String InvoiceSplit = "1";
				String InvoiceConsolidate = "0";
				String methodName = "CallService";
				Object[] objects = client.invoke(methodName, CLIENTNO, TaxMachineIP, SysInvNo, InvoiceList,
						InvoiceSplit, InvoiceConsolidate, xml);
				result = objects[0].toString();
			} else if (Operation.equals("08")) {
				String methodName = "GetCodeAndNo";
				String CLIENTNO = String.valueOf(map.get("clientNO"));
				String fplxdm = String.valueOf(map.get("fpzldm"));
				Object[] objects = client.invoke(methodName, CLIENTNO, null, fplxdm);
				result = objects[0].toString();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	// 读取af的默认配置
	private String readFile(String str) {
		Properties properties = new Properties();
		InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
		BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
		try {
			properties.load(bf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties.getProperty(str);
	}
}