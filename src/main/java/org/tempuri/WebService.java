package org.tempuri;

import javax.jws.WebMethod;

import org.springframework.transaction.annotation.Transactional;

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
}
