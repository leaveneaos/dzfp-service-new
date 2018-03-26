package com.rjxx.taxeasey.ws;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasey.service.*;
import com.rjxx.taxeasy.domains.Fwqxx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.jws.WebParam;

/**
 * Created by Administrator on 2016/10/17.
 */
@javax.jws.WebService(targetNamespace = "http://service.rj.com", name = "Service", serviceName = "Service")
public class WebServiceImpl implements WebService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PARAMS_SEPERATE = "####";

    @Value("${IsCheckSysInvNo:true}")
    private boolean IsCheckSysInvNo;

    double param = 0.06;    //参数：金额和税额校验时允许的误差
    
    @Autowired
    private GetYkfpService getYkfpService;
  
    @Autowired
    private HanderJysjxxService handerJysjxxService;
    
    @Autowired
    private UploadInvoiceService uploadInvoiceService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private DealOrderDataService dealOrderDataService;

    @Autowired
	private UploadCommonDataService uploadCommonDataService;

    public WebServiceImpl() {
    }

	@Override
	public String CallQuery(@WebParam(name = "AppId") String AppId,@WebParam(name = "Sign") String Sign,@WebParam(name = "queryData") String QueryData) {
		// TODO Auto-generated method stub
        logger.debug(AppId + "," + Sign + "," + QueryData);
        String result = getYkfpService.CallQuery(AppId, Sign, QueryData);
		return result;
	}

	@Override
	public String UploadOrder(@WebParam(name = "AppId") String AppId, @WebParam(name = "Sign") String Sign,
			@WebParam(name = "OrderData") String OrderData) {
		// TODO Auto-generated method stub
		logger.debug(AppId + "," + Sign + "," + OrderData);
		String result = "";
		try {
			result = handerJysjxxService.uploadOrder(AppId, Sign, OrderData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}

	@Override
	public String CallService(@WebParam(name = "AppId") String AppId,@WebParam(name = "Sign") String Sign,@WebParam(name = "invoiceData") String invoiceData) {
		// TODO Auto-generated method stub
		logger.debug(AppId + "," + Sign + "," + invoiceData);
		String result = "";
		try {
			result = uploadInvoiceService.callService(AppId, Sign, invoiceData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}
	@Override
	public String CallService2(@WebParam(name = "invoiceData") String invoiceData) {
		// TODO Auto-generated method stub
		logger.debug("-------------"+invoiceData);
		String result = "";
		try {
			result = uploadInvoiceService.callService2(invoiceData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}

	@Override
	public String Cb(Fwqxx fwqxx) {
    	logger.debug("-----服务器信息-------"+JSON.toJSONString(fwqxx));
    	String result="";
    	try{
    		result=invoiceService.cb(fwqxx);
		}catch (Exception e){
          result=e.getMessage();
		}
		return result;
	}
	@Override
	public String Fx(Fwqxx fwqxx) {
		logger.debug("-----服务器信息-------"+JSON.toJSONString(fwqxx));
		String result="";
		try{
			result=invoiceService.fx(fwqxx);
		}catch (Exception e){
			result=e.getMessage();
		}
		return result;
	}
	@Override
	public String invoiceUpload(@WebParam(name = "xml") String xml) {
		// TODO Auto-generated method stub
		logger.debug(xml);
		String result = "";
		try {
			result = invoiceService.invoiceUpload("invoiceUpload", xml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}

	@Override
	public String UploadOrderData(@WebParam(name = "AppId") String AppId,@WebParam(name = "Sign") String Sign,@WebParam(name = "Operation") String Operation,@WebParam(name = "InvoiceData") String InvoiceData) {
		// TODO Auto-generated method stub
		logger.debug(AppId + "," + Sign + ","+Operation +","+ InvoiceData);
		String result = "";
		try {
			result = dealOrderDataService.uploadOrderData(AppId, Sign, Operation, InvoiceData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}

	@Override
	public String UploadCommonData(@WebParam(name = "AppId") String AppId,@WebParam(name = "Sign") String Sign,@WebParam(name = "InvoiceData") String InvoiceData) {
		// TODO Auto-generated method stub
		logger.debug("UploadCommonData:"+AppId + "," + Sign + ","+ InvoiceData);
		String result = "";
		try {
			result = uploadCommonDataService.UploadCommonData(AppId, Sign, InvoiceData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}
}
