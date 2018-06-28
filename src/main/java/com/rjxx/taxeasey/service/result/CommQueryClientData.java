package com.rjxx.taxeasey.service.result;

/**
 * @author: zsq
 * @date: 2018/6/26 10:15
 * @describe:
 */
public class CommQueryClientData {
    private String clientNO;
    private String name;
    private String deviceSN;
    private String devicePSWD;
    private String deviceKEY;
    private String equipNum;
    private String taxDiskPass;
    private String certiCipher;

    public String getClientNO() {
        return clientNO;
    }

    public void setClientNO(String clientNO) {
        this.clientNO = clientNO;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getDevicePSWD() {
        return devicePSWD;
    }

    public void setDevicePSWD(String devicePSWD) {
        this.devicePSWD = devicePSWD;
    }

    public String getDeviceKEY() {
        return deviceKEY;
    }

    public void setDeviceKEY(String deviceKEY) {
        this.deviceKEY = deviceKEY;
    }

    public String getEquipNum() {
        return equipNum;
    }

    public void setEquipNum(String equipNum) {
        this.equipNum = equipNum;
    }

    public String getTaxDiskPass() {
        return taxDiskPass;
    }

    public void setTaxDiskPass(String taxDiskPass) {
        this.taxDiskPass = taxDiskPass;
    }

    public String getCertiCipher() {
        return certiCipher;
    }

    public void setCertiCipher(String certiCipher) {
        this.certiCipher = certiCipher;
    }
}
