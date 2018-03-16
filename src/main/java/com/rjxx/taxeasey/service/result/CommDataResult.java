package com.rjxx.taxeasey.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-23.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Response")
public class CommDataResult {

    @XmlElement
    private String ReturnCode;

    @XmlElement
    private String ReturnMessage;

    @XmlElement
    private CommData CommData;

    public String getReturnCode() {
        return ReturnCode;
    }

    public void setReturnCode(String returnCode) {
        ReturnCode = returnCode;
    }

    public String getReturnMessage() {
        return ReturnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        ReturnMessage = returnMessage;
    }

    public com.rjxx.taxeasey.service.result.CommData getCommData() {
        return CommData;
    }

    public void setCommData(com.rjxx.taxeasey.service.result.CommData commData) {
        CommData = commData;
    }
}
