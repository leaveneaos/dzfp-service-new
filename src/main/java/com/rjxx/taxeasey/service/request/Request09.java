package com.rjxx.taxeasey.service.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-19.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Request")
public class Request09 {

    @XmlElement
    private String ClientNO;

    @XmlElement
    private String TaxMachineIP;

    @XmlElement
    private String SerialNumber;

    @XmlElement
    private String InvType;

    @XmlElement
    private String InvCode;

    @XmlElement
    private String InvNo;

    @XmlElement
    private String PrintType;

    public String getClientNO() {
        return ClientNO;
    }

    public void setClientNO(String clientNO) {
        ClientNO = clientNO;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public String getTaxMachineIP() {
        return TaxMachineIP;
    }

    public void setTaxMachineIP(String taxMachineIP) {
        TaxMachineIP = taxMachineIP;
    }

    public String getInvType() {
        return InvType;
    }

    public void setInvType(String invType) {
        InvType = invType;
    }

    public String getInvCode() {
        return InvCode;
    }

    public void setInvCode(String invCode) {
        InvCode = invCode;
    }

    public String getInvNo() {
        return InvNo;
    }

    public void setInvNo(String invNo) {
        InvNo = invNo;
    }

    public String getPrintType() {
        return PrintType;
    }

    public void setPrintType(String printType) {
        PrintType = printType;
    }
}
