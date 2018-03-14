package com.rjxx.taxeasy.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by xlm on 2017/5/26.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Responese")
public class Result07 {

    @XmlElement
    private String ReturnCode;//返回代码

    @XmlElement
    private String ReturnMessage;//返回错误信息

    @XmlElement
    private String ClientNO;//开票点编号

    @XmlElement
    private String SerialNumber;//交易流水号

    @XmlElement
    private String SysInvNo;//单据号

    @XmlElement
    private String InvCode;//发票代码

    @XmlElement
    private String InvNo;//发票号码

    @XmlElement
    private String InvDate;//开票日期

    @XmlElement
    private String CancelDate;//作废日期

    @XmlElement
    private String PrintFlag;//打印是否成功

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

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public String getSysInvNo() {
        return SysInvNo;
    }

    public void setSysInvNo(String sysInvNo) {
        SysInvNo = sysInvNo;
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

    public String getInvDate() {
        return InvDate;
    }

    public void setInvDate(String invDate) {
        InvDate = invDate;
    }

    public String getCancelDate() {
        return CancelDate;
    }

    public void setCancelDate(String cancelDate) {
        CancelDate = cancelDate;
    }

    public String getPrintFlag() {
        return PrintFlag;
    }

    public void setPrintFlag(String printFlag) {
        PrintFlag = printFlag;
    }
}
