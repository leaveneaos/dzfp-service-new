package com.rjxx.taxease.ws;

import javax.jws.WebMethod;
/**
 * Created by kzx on 2016/12/15.
 */
public interface WebService {

    @WebMethod
    public String CallQuery(String AppId, String Sign, String QueryData);//发票查询

    @WebMethod
    public String UploadOrder(String AppId, String Sign, String OrderData);//交易信息上传
    
    @WebMethod
    public String CallService(String AppId, String Sign, String invoiceData);//发票上传，全部红冲，部分红冲

    @WebMethod
    public String CallService2(String invoiceData);
    
    @WebMethod
    public String invoiceUpload(String xml);//af 发票上传
    
    @WebMethod
    public String UploadOrderData(String AppId, String Sign, String Operation,String invoiceData);
    
}
