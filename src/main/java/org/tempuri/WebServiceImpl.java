package org.tempuri;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.rjxx.taxease.ws.GetYkfpService;
import com.rjxx.taxease.ws.HanderJysjxxService;
import com.rjxx.taxease.ws.UploadInvoiceService;

import javax.jws.WebParam;
import java.util.*;

/**
 * Created by Administrator on 2016/10/17.
 */
@javax.jws.WebService(targetNamespace = "http://tempuri.org/", name = "Service", serviceName = "Service")
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

    public WebServiceImpl() {
    }

	@Override
	public String CallQuery(@WebParam(name = "AppId") String AppId,@WebParam(name = "Sign") String Sign,@WebParam(name = "QueryData") String QueryData) {
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
	public String CallService(String AppId, String Sign, String invoiceData) {
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

	
}
