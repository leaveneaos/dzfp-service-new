package com.rjxx.taxease.utils;

import com.rjxx.taxease.service.result.DefaultResult;
import com.rjxx.utils.XmlJaxbUtils;

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
        return XmlJaxbUtils.toXml(message);
    }

    public static String printFailure1(String errorMessage) {
        DefaultResult defaultResult = new DefaultResult();
        defaultResult.setReturnCode("0000");
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

}
