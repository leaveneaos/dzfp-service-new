package com.rjxx.taxeasey.service.result;

import java.util.List;

/**
 * @author: zsq
 * @date: 2018/6/26 10:12
 * @describe:
 */
public class CommQueryData {
    private String identifier;//销方税号
    private String name;//销方名称
    private String address;//销方地址
    private String telephoneNo;//销方电话
    private String bank;//销方银行
    private String bankAcc;//销方银行账号
    private List<CommQueryClientData> client;//开票点

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephoneNo() {
        return telephoneNo;
    }

    public void setTelephoneNo(String telephoneNo) {
        this.telephoneNo = telephoneNo;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBankAcc() {
        return bankAcc;
    }

    public void setBankAcc(String bankAcc) {
        this.bankAcc = bankAcc;
    }

    public List getClient() {
        return client;
    }

    public void setClient(List client) {
        this.client = client;
    }
}
