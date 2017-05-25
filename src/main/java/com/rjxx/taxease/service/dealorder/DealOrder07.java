package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.utils.XmlMapUtils;
import org.apache.axiom.om.OMElement;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xlm on 2017/5/25.
 */
@Service
public class DealOrder07 implements IDealOrder{
    @Override
    public String execute(String gsdm, String orderData, String Operation) {
        Map inputMap = dealOperation07(gsdm, orderData);
        return null;
    }
    /**
     *
     *
     * @param gsdm,OrderData
     * @return Map
     */
    private Map dealOperation07(String gsdm, String OrderData) {
        OMElement root = null;
        Map inputMap = new HashMap();
        try {
            root = XmlMapUtils.xml2OMElement(OrderData);
            inputMap = XmlMapUtils.xml2Map(root, "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return inputMap;
    }
}
