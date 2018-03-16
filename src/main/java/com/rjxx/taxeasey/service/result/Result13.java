package com.rjxx.taxeasey.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-18.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Responese")
public class Result13 {

    @XmlElement
    private String ReturnCode;

    @XmlElement
    private String ReturnMessage;

    @XmlElement
    private String ClientNO;

    @XmlElement
    private String Fplxdm;

    @XmlElement
    private String Dqfpdm;

    @XmlElement
    private String Dqfphm;

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

    public String getClientNO() {
        return ClientNO;
    }

    public void setClientNO(String clientNO) {
        ClientNO = clientNO;
    }

    public String getFplxdm() {
        return Fplxdm;
    }

    public void setFplxdm(String fplxdm) {
        Fplxdm = fplxdm;
    }

    public String getDqfpdm() {
        return Dqfpdm;
    }

    public void setDqfpdm(String dqfpdm) {
        Dqfpdm = dqfpdm;
    }

    public String getDqfphm() {
        return Dqfphm;
    }

    public void setDqfphm(String dqfphm) {
        Dqfphm = dqfphm;
    }
}
