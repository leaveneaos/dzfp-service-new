package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.service.result.Result08;
import com.rjxx.taxease.utils.CallDllWebServiceUtil;
import com.rjxx.taxease.utils.ResponseUtil;
import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.axiom.om.OMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取发票代码号码
 * Created by Administrator on 2017-05-18.
 */
@Service("dealOrder08")
public class DealOrder08 implements IDealOrder {

    @Autowired
    private SkService skService;

    @Autowired
    private SkpService skpService;

    @Autowired
    private CszbService cszbService;

    @Autowired
    private ResponseUtil responseUtil;

    public String execute(String gsdm, String orderData, String Operation) {
        String result = "";
        // 08代表当前发票号码
        Map inputMap = dealOperation08(gsdm, orderData);
        String clientNO = String.valueOf(inputMap.get("clientNO"));
        String fpzldm = String.valueOf(inputMap.get("fpzldm"));
        Result08 result08 = new Result08();
        result08.setClientNO(clientNO);
        result08.setFplxdm(fpzldm);
        if (StringUtils.isBlank(clientNO) || StringUtils.isBlank(fpzldm)) {
            result08.setOperateFlag("1");
            result08.setReturnmsg("ClientNO或Fplxdm不能为空！");
            return XmlJaxbUtils.toXml(result08);
        }
        Map params = new HashMap();
        params.put("kpddm", clientNO);
        params.put("gsdm", gsdm);
        Skp skp = skpService.findOneByParams(params);
        if (skp == null) {
            result08.setOperateFlag("1");
            result08.setReturnmsg("开票点：" + clientNO + "不存在！");
            return XmlJaxbUtils.toXml(result08);
        }
        int xfid = skp.getXfid();
        int kpdid = skp.getId();
        Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "kpfs");
        String kpfs = cszb.getCsz();
        if ("01".equals(kpfs)) {
            //文本
            try {
                InvoiceResponse response = skService.getCodeAndNo(kpdid, fpzldm);
                if ("0000".equals(response.getReturnCode())) {
                    result08.setOperateFlag("0");
                    result08.setReturnmsg("");
                } else {
                    result08.setOperateFlag("1");
                    result08.setReturnmsg(response.getReturnMessage());
                }
                result08.setDqfpdm(response.getFpdm());
                result08.setDqfphm(response.getFphm());
                return XmlJaxbUtils.toXml(result08);
            } catch (Exception e) {
                result08.setOperateFlag("1");
                result08.setReturnmsg(e.getMessage());
                return XmlJaxbUtils.toXml(result08);
            }
        } else if ("02".equals(kpfs)) {
            if (fpzldm.equals("01")) {
                fpzldm = "0";
            } else if (fpzldm.equals("02")) {
                fpzldm = "1";
            }
            Map map = new HashMap();
            map.put("clientNO", clientNO);
            map.put("fpzldm", fpzldm);
            map.put("Operation", Operation);
            CallDllWebServiceUtil utils = new CallDllWebServiceUtil();
            result = utils.callDllWebSevice(gsdm, map);
            result = responseUtil.response08(result);
            return result;
        } else {
            result08.setOperateFlag("1");
            result08.setReturnmsg("开票点：" + clientNO + "的开票方式不支持该接口！");
            return XmlJaxbUtils.toXml(result08);
        }
    }

    /**
     * 处理查询当前发票代码、号码
     *
     * @param gsdm,OrderData
     * @return Map
     */
    private Map dealOperation08(String gsdm, String OrderData) {
        OMElement root = null;
        Map inputMap = new HashMap();
        try {
            root = XmlMapUtils.xml2OMElement(OrderData);
            Map rootMap = XmlMapUtils.xml2Map(root, "");
            String clientNO = (String) rootMap.get("ClientNO");
            String fpzldm = (String) rootMap.get("Fplxdm");
            inputMap.put("clientNO", clientNO);
            inputMap.put("fpzldm", fpzldm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return inputMap;
    }

}
