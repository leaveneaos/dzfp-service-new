package com.rjxx.taxease.service;

import com.rjxx.taxease.service.dealorder.DealCommData;
import com.rjxx.taxease.utils.ResponeseUtils;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.GsxxService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("uploadCommonDataService")
public class UploadCommonDataService {

    @Autowired
    private GsxxService gsxxservice;
    @Autowired
    private DealCommData dealCommData;
    /**
     * 公共信息接口上传service
     *
     * @param AppId
     * @param Sign
     * @param OrderData
     * @return
     */
    public String UploadCommonData(final String AppId, final String Sign,final String OrderData) {
        String result = "";
        try {
            Map tempMap = new HashMap();
            tempMap.put("appkey", AppId);
            Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
            //查询公司是否存在。不存在则提示校验不通过
            if (gsxxBean == null) {
                return ResponeseUtils.printCommDataResult("9999","9060:" + AppId + "," + Sign+"公司信息未维护","","","","");
            }else{
                // 校验数据是否被篡改过
                String key = gsxxBean.getSecretKey();
                String signSourceData = "data=" + OrderData + "&key=" + key;
                String newSign = DigestUtils.md5Hex(signSourceData);
                //签名没有通过
                if (!Sign.equals(newSign)) {
                    return ResponeseUtils.printCommDataResult("9999","9060:" + AppId + "," + Sign+"签名不通过","","","","");
                }else{
                    //调用公共初始化接口数据方法
                    result = dealCommData.execute(gsxxBean,OrderData);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result = ResponeseUtils.printFailure("9999:" + e.getMessage());
            throw new RuntimeException(result);
        }
    }




}
