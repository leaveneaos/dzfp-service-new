package com.rjxx.taxeasey.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.jkpz.JkpzService;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.yjapi.Result;
import com.rjxx.utils.yjapi.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/3/26
 */
@RequestMapping("/kptService")
@RestController
public class AdapterPostController {
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private JkpzService jkpzService;

    @RequestMapping(method = RequestMethod.POST)
    public Result post(@RequestBody String str) {
        JSONObject jsonObject = JSON.parseObject(str);
        String sign = jsonObject.getString("sign");
        String appId = jsonObject.getString("appId");
        JSONObject data = jsonObject.getJSONObject("data");
        Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
        String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
        if ("0".equals(check)) {
            return ResultUtil.error("验签失败");
        }
        return jkpzService.jkpzInvoice(str);
    }
}
