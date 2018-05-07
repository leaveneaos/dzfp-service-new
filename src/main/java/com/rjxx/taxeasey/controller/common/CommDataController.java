package com.rjxx.taxeasey.controller.common;

import com.alibaba.druid.support.json.JSONUtils;
import com.rjxx.taxeasey.service.dealorder.DealCommData;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Controller
@RequestMapping("/initService")
public class CommDataController {
	@Autowired
	protected HttpServletRequest request;

	@Autowired
	protected HttpServletResponse response;

	@Autowired
	protected DealCommData dealCommData;



	/**
	 * 销货方，开票点信息新增接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/commDataUpload",method = RequestMethod.POST)
	@ApiOperation(value ="销货方，开票点信息新增接口" )
	@ResponseBody
	public String commDataUpload(@RequestBody String str){
		System.out.println("初始化信息接口传入报文："+str);
		String result = dealCommData.execute2(str);

		// 设置返回报文的格式
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.println(JSONUtils.toJSONString(result));
		out.flush();
		out.close();

		return null;

	}


	/**
	 * 销货方信息更新接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/sellerDataUpdate",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value ="销货方信息更新接口" )
	public String sellerDataUpdate(@RequestBody String str){
        System.out.println("销货方更新接口传入报文："+str);
		String result = dealCommData.execute3(str);

		// 设置返回报文的格式
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.println(JSONUtils.toJSONString(result));
		out.flush();
		out.close();

		return null;

	}

	/**
	 * 门店信息新增或更新接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/clientDataUpdate",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "门店信息新增或更新接口")
	public String clientDataUpdate(@RequestBody String str){
		System.out.println("门店信息更新接口传入报文："+str);
		String result = dealCommData.execute4(str);

		// 设置返回报文的格式
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.println(JSONUtils.toJSONString(result));
		out.flush();
		out.close();

		return null;

	}
}