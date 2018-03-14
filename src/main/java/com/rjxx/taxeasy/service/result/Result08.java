package com.rjxx.taxeasy.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-18.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Responese")
public class Result08 {

    @XmlElement(name = "CLIENTNO")
    private String ClientNO;

    @XmlElement
    private String Fplxdm;

    @XmlElement
    private String Dqfpdm;

    @XmlElement
    private String Dqfphm;

    @XmlElement
    private String OperateFlag;

    @XmlElement
    private String Returnmsg;

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

    public String getOperateFlag() {
        return OperateFlag;
    }

    public void setOperateFlag(String operateFlag) {
        OperateFlag = operateFlag;
    }

    public String getReturnmsg() {
        return Returnmsg;
    }

    public void setReturnmsg(String returnmsg) {
        Returnmsg = returnmsg;
    }
}
