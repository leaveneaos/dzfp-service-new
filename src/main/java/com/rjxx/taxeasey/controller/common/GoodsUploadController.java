package com.rjxx.taxeasey.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.rjxx.taxeasey.utils.ResponeseUtils;
import com.rjxx.taxeasy.bizcomm.utils.DealGoodsUtil;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.utils.RJCheckUtil;
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
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author: zsq
 * @date: 2018/6/22 15:20
 * @describe:商品上传接口
 */

@Controller
@RequestMapping("/goodsService")
public class GoodsUploadController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected HttpServletResponse response;
    @Autowired
    private DealGoodsUtil dealGoodsUtil;

    private static Logger logger = LoggerFactory.getLogger(GoodsUploadController.class);


    @RequestMapping(value ="/dataUpload",method = RequestMethod.POST)
    @ApiOperation(value ="商品信息上传接口" )
    @ResponseBody
    public String dataUpload(@RequestBody String str){
        logger.info("商品数据上传接口传入报文："+str);
        String result = dealGoodsUtil.dealGoods(str);

        // 设置返回报文的格式
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println(result);
        out.flush();
        out.close();
        return null;
    }
}
