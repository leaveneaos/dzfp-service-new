package com.rjxx.taxease.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.rjxx.taxease.service.DealOrderDataService;
import com.rjxx.taxease.service.GetYkfpService;
import com.rjxx.taxease.service.HanderJysjxxService;
import com.rjxx.taxease.service.InvoiceService;
import com.rjxx.taxease.service.UploadInvoiceService;

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
    private GetYkfpService getykfpservice;
  
    @Autowired
    private HanderJysjxxService handerjysjxxservice;
    
    @Autowired
    private UploadInvoiceService uploadinvoiceservice;
    
    @Autowired
    private InvoiceService invoiceservice;
    
    @Autowired
    private DealOrderDataService dealorderdataservice;

    public WebServiceImpl() {
    }

	@Override
	public String CallQuery(@WebParam(name = "AppId") String AppId,@WebParam(name = "Sign") String Sign,@WebParam(name = "queryData") String QueryData) {
		// TODO Auto-generated method stub
        logger.debug(AppId + "," + Sign + "," + QueryData);
        String result = getykfpservice.CallQuery(AppId, Sign, QueryData);
		return result;
	}

	@Override
	public String UploadOrder(@WebParam(name = "AppId") String AppId, @WebParam(name = "Sign") String Sign,
			@WebParam(name = "OrderData") String OrderData) {
		// TODO Auto-generated method stub
		logger.debug(AppId + "," + Sign + "," + OrderData);
		String result = "";
		try {
			result = handerjysjxxservice.uploadOrder(AppId, Sign, OrderData);
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
			result = uploadinvoiceservice.callService(AppId, Sign, invoiceData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}

	@Override
	public String invoiceUpload(@WebParam(name = "xml") String xml) {
		// TODO Auto-generated method stub
		logger.debug(xml);
		String result = "";
		try {
			result = invoiceservice.invoiceUpload("invoiceUpload", xml);
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
			result = dealorderdataservice.uploadOrderData(AppId, Sign, Operation, InvoiceData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage();
		}
		return result;
	}

	
}
