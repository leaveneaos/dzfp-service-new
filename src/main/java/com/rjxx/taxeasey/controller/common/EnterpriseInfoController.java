package com.rjxx.taxeasey.controller.common;

import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.utils.shouqianba.PayUtil;
import com.rjxx.utils.weixin.HttpClientUtil;
import com.rjxx.utils.yjapi.QCCUtils;
import com.rjxx.utils.yjapi.Result;
import com.rjxx.utils.yjapi.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    private Logger logger = LoggerFactory.getLogger(EnterpriseInfoController.class);

    @RequestMapping(value = "/{gsdm}/find", method = RequestMethod.POST)
    public Result getMsg(@PathVariable String gsdm, @RequestParam String name,@RequestHeader String Sign){
        boolean validation = validation(gsdm, name,Sign);
        if(!validation){
            return ResultUtil.error("验签失败");
        }
        String taxno = qccUtils.getTycById(null, name);
        Map map = new HashMap<>();
        map.put("taxNo", taxno);
        return ResultUtil.success(map);
    }

    @RequestMapping(value = "/{gsdm}/list", method = RequestMethod.POST)
    public Result getName(@PathVariable String gsdm,@RequestParam String name,@RequestHeader String Sign){
        boolean validation = validation(gsdm, name,Sign);
        if(!validation){
            return ResultUtil.error("验签失败");
        }
        String result = qccUtils.getTycByName(name);
        Map map = new HashMap();
        map.put("companyList", result);
        return ResultUtil.success(map);
    }

    private boolean validation(String gsdm,String name,String authorization){
        logger.info("【key word】={}",name);
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

    public static void main(String[] args) {
        String url = "http://test.datarj.com/webService/enterpriseInfo/two/list";
        Map map = new HashMap();
        map.put("name", "容津");
        String signContent = "name=" + "容津" + "&key=" + "two";
        String sign = PayUtil.getSign(signContent);
        String s = HttpClientUtil.doPostSign(url, map, sign);
        System.out.println(s);
    }
}
