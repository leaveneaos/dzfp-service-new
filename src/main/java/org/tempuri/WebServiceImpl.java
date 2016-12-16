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
  

    public WebServiceImpl() {
    }

	@Override
	public String CallQuery(String AppKey, String Secret, String QueryData) {
		// TODO Auto-generated method stub
        logger.debug(AppKey + "," + Secret + "," + QueryData);
        //getykfpservice = 
        String result = getykfpservice.CallQuery(AppKey, Secret, QueryData);
		return result;
	}

	
}
