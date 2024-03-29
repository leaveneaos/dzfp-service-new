package com.rjxx.taxeasey.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rjxx.taxeasey.service.*;
import com.rjxx.taxeasy.service.GsxxService;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/service")
public class WebServiceController {
	@Autowired
	protected HttpServletRequest request;

	@Autowired
	protected HttpServletResponse response;

	@Autowired
	protected GetYkfpService getYkfpService;
	
	@Autowired
	protected HanderJysjxxService handerJysjxxService;
	
	@Autowired
	protected UploadInvoiceService uploadInvoiceService;
	
	@Autowired
	protected InvoiceService invoiceService;
	
	@Autowired
	protected DealOrderDataService dealOrderDataService;

	@Autowired
	protected UploadCommonDataService uploadCommonDataService;

	@Autowired
	protected GsxxService   gsxxService ;
	@RequestMapping
	@ResponseBody
	public String index() {

		// 获取收到的报文
		BufferedReader reader;
		String line = "";
		StringBuffer inputString = new StringBuffer();
		ObjectMapper mapper = new ObjectMapper();
		Map map = new HashMap();
		String result = "";
		try {
			// 接收请求数据，包括appkey、secret、method、xml
			reader = request.getReader();

			while ((line = reader.readLine()) != null) {
				inputString.append(line);
			}
			// 将请求的ObjectMapper格式数据转为map数据
			map = mapper.readValue(inputString.toString(), Map.class);
			// 根据对应的method方法，调用对应的service
			result = this.dealResult(map);

			// 设置返回报文的格式
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			PrintWriter out = response.getWriter();
			out.println(result);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(map.get("name"));
		return null;
	}

	/**
	 * 将请求的数据进行处理并根据对应的method调用对应service， 
	 * return String
	 */
	private String dealResult(Map map) {
		String methodName = String.valueOf(map.get("methodName"));
		String AppKey = String.valueOf(map.get("AppKey"));
		String Secret = String.valueOf(map.get("Secret"));
		String InvoiceData = String.valueOf(map.get("InvoiceData"));
		String Operation = String.valueOf(map.get("Operation"));
		String result = "";
		if (methodName.equals("CallQuery")) {
			result = getYkfpService.CallQuery(AppKey, Secret, InvoiceData);
		}else if(methodName.equals("UploadOrder")){
			result = handerJysjxxService.uploadOrder(AppKey, Secret, InvoiceData);
		}else if(methodName.equals("CallService")){
			result = uploadInvoiceService.callService(AppKey, Secret, InvoiceData);
		}else if(methodName.equals("CallService2")){
			result = uploadInvoiceService.callService2(InvoiceData);
		}else if(methodName.equals("invoiceUpload")){
			result = invoiceService.invoiceUpload(methodName, InvoiceData);
		}else if(methodName.equals("UploadOrderData")){
			result = dealOrderDataService.dealOrder(AppKey, Secret, Operation, InvoiceData);
		}else if(methodName.equals("UploadCommonData")){
			result = uploadCommonDataService.UploadCommonData(AppKey,Secret,InvoiceData);
		}
		

		return result;
	}

}
