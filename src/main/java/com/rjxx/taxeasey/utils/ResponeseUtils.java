package com.rjxx.taxeasey.utils;

import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasey.service.result.CommData;
import com.rjxx.taxeasey.service.result.CommDataResult;
import com.rjxx.taxeasey.service.result.DefaultResult;
import com.rjxx.utils.XmlJaxbUtils;

import java.util.Map;

/**
 * Created by Administrator on 2017-06-02.
 */
public class ResponeseUtils {

    /**
     * 返回失败消息
     *
     * @param message
     * @return
     */
    public static String printFailure(String message) {
        DefaultResult defaultResult = new DefaultResult();
        defaultResult.setReturnCode("9999");
        defaultResult.setReturnMessage(message);
        return XmlJaxbUtils.toXml(defaultResult);
    }

    public static String printFailure1(String errorMessage) {
        DefaultResult defaultResult = new DefaultResult();
        defaultResult.setReturnCode("9999");
        defaultResult.setReturnMessage(errorMessage);
        return XmlJaxbUtils.toXml(defaultResult);
    }

    /**
     * 返回正确结果
     *
     * @param djh
     * @return
     */
    public static String printSuccess(int djh) {
        DefaultResult defaultResult = new DefaultResult();
        defaultResult.setReturnCode("0000");
        defaultResult.setReturnMessage("待开票数据保存成功");
        return XmlJaxbUtils.toXml(defaultResult);
    }

    /**
     * 结果返回信息
     * @param returnCode
     * @param returnMes
     * @param resultMap
     * @return
     */
    public static String printCommDataResult(String returnCode,String returnMes,Map resultMap) {
        CommDataResult commDataResult = new CommDataResult();
        CommData CommData = new CommData();
        if(returnCode.equals("0000")){
            CommData.setIdentifier(String.valueOf(resultMap.get("xfsh")));
            CommData.setName(String.valueOf(resultMap.get("xfmc")));
            CommData.setLoginName(String.valueOf(resultMap.get("dlyhid")));
            CommData.setEquipNum(String.valueOf(resultMap.get("skph")));
            CommData.setPassWord(String.valueOf(resultMap.get("yhmm")));
            commDataResult.setCommData(CommData);
        }
        commDataResult.setReturnCode(returnCode);
        commDataResult.setReturnMessage(returnMes);

        return XmlJaxbUtils.toXml(commDataResult);
    }

    /**
     * 公共数据初始化返回信息
     * @param returnCode
     * @param returnMes
     * @param resultMap
     * @return
     */
    public static String printResultToJson(String returnCode, String returnMes, Map resultMap) {
        CommDataResult commDataResult = new CommDataResult();
        CommData commData = new CommData();
        if(returnCode.equals("0000") && null!= resultMap.get("yhmm") && !resultMap.get("yhmm").equals("")){
            commData.setIdentifier(String.valueOf(resultMap.get("xfsh")));
            commData.setName(String.valueOf(resultMap.get("xfmc")));
            commData.setLoginName(String.valueOf(resultMap.get("dlyhid")));
            commData.setPassWord(String.valueOf(resultMap.get("yhmm")));
            commData.setEquipNum(String.valueOf(resultMap.get("skph")));
            commDataResult.setCommData(commData);
        }
        commDataResult.setReturnCode(returnCode);
        commDataResult.setReturnMessage(returnMes);

        return JSONObject.toJSONString(commDataResult);
    }

}
