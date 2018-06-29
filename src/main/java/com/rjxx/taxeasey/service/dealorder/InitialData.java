package com.rjxx.taxeasey.service.dealorder;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.rjxx.taxeasey.service.result.CommQueryClientData;
import com.rjxx.taxeasey.service.result.CommQueryData;
import com.rjxx.taxeasey.utils.ResponeseUtils;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.XfService;
import com.rjxx.utils.RJCheckUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: zsq
 * @date: 2018/6/29 15:34
 * @describe:
 */
@Service
public class InitialData {

    @Autowired
    protected GsxxJpaDao gsxxJpaDao ;
    @Autowired
    private XfService xfService;
    private static Logger logger= LoggerFactory.getLogger(InitialData.class);

    /**
     * post Json 销售方及门店信息查询接口  供销售渠道使用
     * @param OrderData
     * @return
     */
    public String InitialDataQuery(String OrderData) {
        Map resultMap = new HashMap();
        HashMap<String, Object> jsonObject = null;
        try {
            jsonObject = JSON.parseObject(OrderData,LinkedHashMap.class, Feature.OrderedField);

        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！","","","","");
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject data = (JSONObject)jsonObject.get("data");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护","","","","");
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过","","","","");
            }else{
                //校验数据是否符合规则
                if(data.getString("identifier")==null || "".equals(data.getString("identifier"))){
                    return ResponeseUtils.printResultToJson("9999","数据格式不正确，销货方税号identifier：不能为空","","","","");
                }
                if(data.getString("identifier").length()>20){
                    return ResponeseUtils.printResultToJson("9999","数据格式不正确，销货方税号identifier长度不能大于20位","","","","");
                }
                String xfsh = data.getString("identifier");
                String kpddm = data.getString("clientNO");
                Map params = new HashMap();
                params.put("gsdm",gsxx.getGsdm());
                params.put("kpddm",kpddm);
                params.put("xfsh",xfsh);
//                params.put("csz","04");
                List<Map> list1 = xfService.findByxfshAndSkph(params);
                if(list1.isEmpty()){
                    return ResponeseUtils.printResultToJson("9999","根据销货方税号identifier"+xfsh+"未查询到数据","","","","");
                }
                CommQueryData commQueryData = new CommQueryData();
                commQueryData.setIdentifier(list1.get(0).get("xfsh")==null?"":String.valueOf(list1.get(0).get("xfsh")));
                commQueryData.setName(list1.get(0).get("xfmc")==null?"":String.valueOf(list1.get(0).get("xfmc")));
                commQueryData.setAddress(list1.get(0).get("xfdz")==null?"":String.valueOf(list1.get(0).get("xfdz")));
                commQueryData.setTelephoneNo(list1.get(0).get("xfdh")==null?"":String.valueOf(list1.get(0).get("xfdh")));
                commQueryData.setBank(list1.get(0).get("xfyh")==null?"":String.valueOf(list1.get(0).get("xfyh")));
                commQueryData.setBankAcc(list1.get(0).get("xfyhzh")==null?"":String.valueOf(list1.get(0).get("xfyhzh")));
                List list = new ArrayList();
                for(int i =0;i<list1.size();i++){
                    Map map = list1.get(i);
                    CommQueryClientData commQueryClientData = new CommQueryClientData();
                    commQueryClientData.setClientNO(map.get("kpddm") == null ?"":String.valueOf(map.get("kpddm")));
                    commQueryClientData.setName(map.get("name")==null?"":String.valueOf(map.get("kpdmc")));
//                    commQueryClientData.setDeviceSN(map.get("devicesn")==null?"":String.valueOf(map.get("devicesn")));//凯盈开票终端sn
//                    commQueryClientData.setDevicePSWD(map.get("devicepassword")==null?"":String.valueOf(map.get("devicepassword")));
//                    commQueryClientData.setDeviceKEY(map.get("devicekey")==null?"":String.valueOf(map.get("devicekey")));
                    commQueryClientData.setEquipNum(map.get("skph")==null?"":String.valueOf(map.get("skph")));//税控盘号
                    commQueryClientData.setTaxDiskPass(map.get("skpmm")==null?"":String.valueOf(map.get("skpmm")));//税控盘密码
                    commQueryClientData.setCertiCipher(map.get("zsmm")==null?"":String.valueOf(map.get("zsmm")));//证书密码
                    list.add(commQueryClientData);
                }
                commQueryData.setClient(list);
                resultMap.put("returnCode","0000");
                resultMap.put("returnMessage","销售方及门店信息查询成功");
                resultMap.put("commData",commQueryData);
                return JSON.toJSONString(resultMap);
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","销售方及门店信息查询失败！","","","","");
        }
    }
}
