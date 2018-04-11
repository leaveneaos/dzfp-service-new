package com.rjxx.taxeasey.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasey.service.*;
import com.rjxx.taxeasey.service.dealorder.DealCommData;
import com.rjxx.taxeasey.utils.ResponeseUtils;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.yjapi.ResultUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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
	@ResponseBody
	public String commDataUpload(@RequestBody String str){

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
}
