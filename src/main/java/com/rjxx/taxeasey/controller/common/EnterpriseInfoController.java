package com.rjxx.taxeasey.controller.common;

import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.utils.shouqianba.PayUtil;
import com.rjxx.utils.yjapi.QCCUtils;
import com.rjxx.utils.yjapi.Result;
import com.rjxx.utils.yjapi.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/7/17
 */
@RestController
@RequestMapping("/enterpriseInfo")
public class EnterpriseInfoController {
    @Autowired
    private QCCUtils qccUtils;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private HttpServletRequest request;

    private Logger logger = LoggerFactory.getLogger(EnterpriseInfoController.class);

    @RequestMapping(value = "/{gsdm}/find", method = RequestMethod.POST)
    public Result getMsg(@PathVariable String gsdm, @RequestParam String name){
        boolean validation = validation(gsdm, name);
        if(!validation){
            return ResultUtil.error("验签失败");
        }
        String taxno = qccUtils.getTycById(null, name);
        Map map = new HashMap<>();
        map.put("taxNo", taxno);
        return ResultUtil.success(map);
    }

    @RequestMapping(value = "/{gsdm}/list", method = RequestMethod.POST)
    public Result getName(@PathVariable String gsdm,@RequestParam String name){
        boolean validation = validation(gsdm, name);
        if(!validation){
            return ResultUtil.error("验签失败");
        }
        String result = qccUtils.getTycByName(name);
        Map map = new HashMap();
        map.put("companyList", result);
        return ResultUtil.success(map);
    }

    private boolean validation(String gsdm,String name){
        logger.info("【key word】={}",name);
        String authorization = request.getHeader("Sign");
        logger.info("【in sign】={}",authorization);
        Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
        String secretKey = oneByGsdm.getSecretKey();
        String signContent = "name=" + name + "&key=" + secretKey;
        String sign = PayUtil.getSign(signContent);
        logger.info("【out sign】={}",sign);
        if(!sign.equalsIgnoreCase(authorization)){
            return false;
        }
        return true;
    }

//    public static void main(String[] args) {
//        String url = "http://localhost:8080/enterpriseInfo/two/list";
//        Map map = new HashMap();
//        map.put("name", "容津");
//        String signContent = "name=" + "容津" + "&key=" + "two";
//        String sign = PayUtil.getSign(signContent);
//        String s = HttpClientUtil.doPostSign(url, map, sign);
//        JSONObject jsonObject = JSON.parseObject(s);
//        JSONObject data = jsonObject.getJSONObject("data");
//        String companyList = data.getString("companyList");
//        JSONArray objects = JSON.parseArray(companyList);
//        System.out.println(JSON.toJSONString(objects));
//    }
}
