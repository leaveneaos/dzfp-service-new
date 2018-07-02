package com.rjxx.taxeasey.controller.common;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasey.service.dealorder.DealCommData;
import com.rjxx.taxeasey.service.dealorder.InitialData;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@Autowired
	protected InitialData initialData;

	private static Logger logger = LoggerFactory.getLogger(CommDataController.class);


	/**
	 * 销货方，开票点信息查询接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/commDataQuery",method = RequestMethod.POST)
	@ApiOperation(value ="销货方，开票点信息查询接口" )
	@ResponseBody
	public String commDataQuery(@RequestBody String str){
		logger.info("销货方，开票点--查询接口传入报文："+str);
		String result = dealCommData.execute5(str);

		// 设置返回报文的格式
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.println(JSON.parseObject(result));
		out.flush();
		out.close();

		return null;

	}


	/**
	 * 销货方，开票点信息新增接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/commDataUpload",method = RequestMethod.POST)
	@ApiOperation(value ="销货方，开票点信息新增接口" )
	@ResponseBody
	public String commDataUpload(@RequestBody String str){
		logger.info("初始化信息接口传入报文："+str);
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
		out.println(JSON.parseObject(result));
		out.flush();
		out.close();

		return null;

	}


	/**
	 * 销货方信息新增更新接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/sellerDataUpdate",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value ="销货方信息更新接口" )
	public String sellerDataUpdate(@RequestBody String str){
		logger.info("销货方更新接口传入报文："+str);
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
		out.println(JSON.parseObject(result));
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
	@ApiOperation(value = "门店信息新增或更新接口,凯盈使用")
	public String clientDataUpdate(@RequestBody String str){
		logger.info("门店信息更新接口传入报文："+str);
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
		out.println(JSON.parseObject(result));
		out.flush();
		out.close();

		return null;

	}


	/**
	 * 门店信息新增或更新接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/initialClientData",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "门店信息新增或更新接口")
	public String initialClient(@RequestBody String str){
		logger.info("门店信息更新接口传入报文："+str);
		String result = dealCommData.initialClient(str);

		// 设置返回报文的格式
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.println(JSON.parseObject(result));
		out.flush();
		out.close();

		return null;

	}

	/**
	 * 门店信息新增或更新接口
	 * @param str
	 * @return String
	 */
	@RequestMapping(value ="/initialDataQuery",method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "销售方及门店信息查询接口,供渠道使用")
	public String initialDataQuery(@RequestBody String str){
		logger.info("销售方及门店信息查询接口,供渠道使用："+str);
		String result = initialData.InitialDataQuery(str);

		// 设置返回报文的格式
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.println(JSON.parseObject(result));
		out.flush();
		out.close();

		return null;

	}

}
