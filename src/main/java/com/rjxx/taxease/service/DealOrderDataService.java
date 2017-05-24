package com.rjxx.taxease.service;

import com.rjxx.taxease.service.dealorder.*;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.utils.ResponseUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DealOrderDataService {

    @Autowired
    private GsxxService gsxxservice;

    @Autowired
    private DealOrder01 dealOrder01;

    @Autowired
    private DealOrder02 dealOrder02;

    @Autowired
    private DealOrder03 dealOrder03;

    @Autowired
    private DealOrder08 dealOrder08;

    @Autowired
    private DealOrder09 dealOrder09;

    @Autowired
    private DealOrder13 dealOrder13;

    @Autowired
    private DealOrder11 dealOrder11;

    @Autowired
    private DealOrder04 dealOrder04;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 交易数据上传service
     *
     * @param AppId
     * @param Sign
     * @param Operation
     * @param OrderData
     * @return
     */
    public String uploadOrderData(final String AppId, final String Sign, final String Operation,
                                  final String OrderData) {
        final Map resultMap = new HashMap();
        try {
            String result = dealOrder(AppId, Sign, Operation, OrderData);
            return result;
            // resultMap.put("result", result);
        } catch (Exception e) {
            e.printStackTrace();
            String result = ResponseUtils.printFailure("9999:" + e.getMessage());
            throw new RuntimeException(result);
        }
    }

    /**
     * 处理上传的交易信息
     *
     * @param AppId
     * @param Sign
     * @param Operation
     * @param OrderData
     * @return
     */
    public String dealOrder(String AppId, String Sign, String Operation, String OrderData) {
        String result = "";
        Map tempMap = new HashMap();
        tempMap.put("appkey", AppId);
        Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
        if (gsxxBean == null) {
            return ResponseUtils.printFailure1("9060:" + AppId + "," + Sign);
        }
        // 校验数据是否被篡改过
        String key = gsxxBean.getSecretKey();
        String signSourceData = "data=" + OrderData + "&key=" + key;
        String newSign = DigestUtils.md5Hex(signSourceData);
        if (!Sign.equals(newSign)) {
            result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9060:签名不通过</ReturnMessage> \n</Responese>";
            return result;
        }
        String gsdm = gsxxBean.getGsdm();
        if (Operation.equals("03")) {
            return dealOrder03.execute(gsdm, OrderData, Operation);
        } else if (Operation.equals("01")) {
            return dealOrder01.execute(gsdm, OrderData, Operation);
        } else if (Operation.equals("02")) {
            return dealOrder02.execute(gsdm, OrderData, Operation);
        } else if (Operation.equals("08")) {
            return dealOrder08.execute(gsdm, OrderData, Operation);
        } else if (Operation.equals("09")) {
            return dealOrder09.execute(gsdm, OrderData, Operation);
        } else if (Operation.equals("11")) {
            return dealOrder11.execute(gsdm, OrderData, Operation);
        } else if (Operation.equals("13")) {
            return dealOrder13.execute(gsdm, OrderData, Operation);
        }else if(Operation.equals("04")){
            return dealOrder04.execute(gsdm, OrderData, Operation);
        }
        return result;
    }
}
