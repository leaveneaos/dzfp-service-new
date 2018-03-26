package com.rjxx.taxeasey.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by xlm on 2017/5/23.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Responese")
public class Result04 {
    @XmlElement
    private String ReturnCode;

    @XmlElement
    private String ReturnMessage;

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
}
