package com.rjxx.taxease.ws;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

/**
 * Created by Administrator on 2016/12/15.
 */
public class WebServiceTestCall {

   
    public static void testCallQuery() throws Exception {
    	 String QueryData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "<Request>\n" +
                 "  <!--以下查询条件可任选-->\n" +
                 "  <SerialNumber>YJ-2016-00084849</SerialNumber>\n" +
                 "  <!--SerialNumber可选，交易流水号String20-->\n" +
                 "  <OrderNumber></OrderNumber>\n" +
                 "  <!--OrderNumber可选，来源系统订单号，String20-->\n" +
                 "  <BuyerName></BuyerName>\n" +
                 "  <!--BuyerName可选，购买方名称String100-->\n" +
                 "  <BuyerTel></BuyerTel>\n" +
                 "  <!--BuyerTel可选，购买方电话String20-->\n" +
                 "</Request>\n";
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient(WS_URL);
        String methodName = "CallQuery";
        String AppKey = "RJ9b458d60149c";
        String key ="85a7764f0372dd7b04067e02985830d7";
        String Secret = getSign(QueryData,key);
        String InvoiceData = QueryData;
        Object[] objects = client.invoke(methodName, AppKey, Secret, InvoiceData);
        //输出调用结果
        System.out.println(objects[0].toString());
    }

    private static String getSign(String QueryData,String key){
    	String signSourceData = "data=" + QueryData + "&key=" + key;
        String newSign =  DigestUtils.md5Hex(signSourceData);
        return newSign;
    }
    
    public static String WS_URL = "http://localhost:8080/dzfp-service-new/Service.asmx?wsdl";

    public static void main(String[] args) throws Exception {
    	testCallQuery();
    }

}
