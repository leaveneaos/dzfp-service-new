package com.rjxx.taxeasey.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-23.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class CommData {

    @XmlElement
    private String Identifier;

    @XmlElement
    private String Name;

    @XmlElement
    private String LoginName;

    @XmlElement
    private String PassWord;

    @XmlElement
    private String equipNum;

    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String identifier) {
        Identifier = identifier;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLoginName() {
        return LoginName;
    }

    public void setLoginName(String loginName) {
        LoginName = loginName;
    }

    public String getPassWord() {
        return PassWord;
    }

    public void setPassWord(String passWord) {
        PassWord = passWord;
    }

    public String getEquipNum() {
        return equipNum;
    }

    public void setEquipNum(String equipNum) {
        equipNum = equipNum;
    }
}
