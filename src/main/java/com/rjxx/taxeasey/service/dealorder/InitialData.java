package com.rjxx.taxeasey.service.dealorder;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasey.service.result.CommQueryClientData;
import com.rjxx.taxeasey.service.result.CommQueryData;
import com.rjxx.taxeasey.utils.InitialCheckUtil;
import com.rjxx.taxeasey.utils.ResponeseUtils;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.dto.ClientData;
import com.rjxx.taxeasy.dto.CommonData;
import com.rjxx.taxeasy.dto.SellerData;
import com.rjxx.taxeasy.service.RolesService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.XfService;
import com.rjxx.taxeasy.service.YhService;
import com.rjxx.taxeasy.vo.SkpVo;
import com.rjxx.utils.PasswordUtils;
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
    @Autowired
    private InitialCheckUtil initialCheckUtil;
    @Autowired
    protected RolesService rolesService;
    @Autowired
    protected SkpService skpService;
    @Autowired
    protected YhService yhService;
    @Autowired
    protected SkpJpaDao skpJpaDao;
    @Autowired
    protected DealCommData dealCommData;

    private static Logger logger= LoggerFactory.getLogger(InitialData.class);


    /**
     * 凯盈盒子最新初始化接口，首先判断凯盈每次传入的
     * 初始化信息是做销方和开票点的新增还是更新操作，
     * 判断好后做对应操作
     * 分为：1、新增销方开票点
     *      2、新增开票点
     *      3、新增销方
     *      4、更新销方
     *      5、更新开票点
     * @param commData
     * @return
     */
    public String initialData(String commData){

        HashMap<String, Object> jsonObject = null;
        try {
            jsonObject = JSON.parseObject(commData,LinkedHashMap.class, Feature.OrderedField);

        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！",new HashMap());
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject seller = (JSONObject)jsonObject.get("seller");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护",new HashMap());
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(seller), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过",new HashMap());
            }else{
                ObjectMapper mapper = new ObjectMapper();
                CommonData commonData=mapper.readValue(JSON.parseObject(commData).toJSONString(), CommonData.class);
                SellerData sellerData = commonData.getSeller();
                String xfsh = sellerData.getIdentifier();
                List<ClientData> clientDataList = sellerData.getClient();
                ClientData clientData = clientDataList.get(0);
                String skph = clientData.getEquipNum();
                String sn = clientData.getDeviceSN();
                //校验销方税号，税控盘号必须都不为空
                if(null ==xfsh || xfsh.equals("") || null == skph || skph.equals("")){
                    return ResponeseUtils.printResultToJson("9999","销方税号（identifier）、税控盘号（equipNum）必须全不为空！",new HashMap());
                }
                Map params = new HashMap();
                params.put("skph",skph);
                params.put("xfsh",xfsh);
                params.put("csz","04");
                List<Map> list = xfService.findByxfshAndSkph(params);
                if(list.isEmpty()){
                    //若未做过初始化销货方等信息，以sn+skph为开票点代码做初始化操作。
                    commonData.getSeller().getClient().get(0).setClientNO(sn+skph);
                    Map resultMap = dealCommData.dealCommonData2(gsxx, commonData);
                    Xf xf = (Xf)resultMap.get("Xf");
                    List<SkpVo> skpList = (List)resultMap.get("skpList");
                    String issueType = String.valueOf(resultMap.get("issueType"));
                    //调用校验数据是否符合规则方法。
                    boolean isCrestv = false;
                    //凯盈盒子不校验销方是否已存在。
                    if(gsxx.getGsdm().equals("crestv") || gsxx.getGsdm().equals("rjxx")){
                        isCrestv = true;
                    }
                    String result = initialCheckUtil.checkCommData(xf,skpList,issueType,isCrestv);
                    if(null != result && !result.equals("")){
                        return ResponeseUtils.printResultToJson("9999",result,new HashMap());
                    }else{
                        //保存待处理数据。
                        resultMap = dealCommData.saveXfAndSkp(xf,skpList,issueType,gsxx);
                        //保存失败
                        if(null == resultMap || resultMap.isEmpty()){
                            return ResponeseUtils.printResultToJson("9999","保存信息出错",new HashMap());
                        }else{
                            return ResponeseUtils.printResultToJson("0000","成功",resultMap);
                        }
                    }
                 }else{
                    //做过销方初始化，则调用开票点初始化接口
                    boolean isupdateClient = false; //是否更新开票点
                    for (int i=0;i<list.size();i++){
                        Map result = list.get(i);
                        if(result.get("skph").equals(skph)){
                            isupdateClient =true;
                            break;
                        }
                    }
                    //判断开票点的是否做过初始化
                    if(isupdateClient){
                        clientData.setType("02");
                    }else{
                        clientData.setType("01");
                    }
                    clientData.setIdentifier(xfsh);
                    clientData.setClientNO(sn+skph);
                    clientData.setKpyh(sellerData.getBank());
                    clientData.setKpyhzh(sellerData.getBankAcc());
                    clientData.setKpdh(sellerData.getTelephoneNo());
                    clientData.setKpdz(sellerData.getAddress());
                    clientData.setDrawer(sellerData.getDrawer());
                    clientData.setReviewer(sellerData.getReviewer());
                    clientData.setPayee(sellerData.getPayee());
                    Map resultMap =  dealCommData.updateclientData(gsxx,clientData);
                    return ResponeseUtils.printResultToJson("0000","销售方及开票点成功！",resultMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","销售方及开票点初始化失败！",new HashMap());
        }
    }

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
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！",new HashMap());
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject data = (JSONObject)jsonObject.get("data");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护",new HashMap());
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过",new HashMap());
            }else{
                //校验数据是否符合规则
                if(data.getString("identifier")==null || "".equals(data.getString("identifier"))){
                    return ResponeseUtils.printResultToJson("9999","数据格式不正确，销货方税号identifier：不能为空",new HashMap());
                }
                if(data.getString("identifier").length()>20){
                    return ResponeseUtils.printResultToJson("9999","数据格式不正确，销货方税号identifier长度不能大于20位",new HashMap());
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
                    return ResponeseUtils.printResultToJson("9999","根据销货方税号identifier"+xfsh+"未查询到数据",new HashMap());
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
            return ResponeseUtils.printResultToJson("9999","销售方及门店信息查询失败！",new HashMap());
        }
    }

    /**
     * 渠道用来初始化开票点处理方法
     * @param gsxx
     * @param clientData
     * @return
     */
    public Map initialClientData(Gsxx gsxx, ClientData clientData){

        String result = "";
        Map resultMap = new HashMap();
        try {

            String xfsh = clientData.getIdentifier();
            if(null == xfsh || xfsh.equals("")){
                resultMap.put("reCode","9999");
                resultMap.put("reMessage","销方信息identifier销方税号必传;");
                return resultMap;
            }else{
                Xf xfParm = new Xf();
                xfParm.setGsdm(gsxx.getGsdm());
                xfParm.setXfsh(xfsh);
                Xf xfBo = xfService.findOneByParams(xfParm);
                if(null ==xfBo){
                    resultMap.put("reCode","9999");
                    resultMap.put("reMessage","销方信息identifier"+xfsh+"不存在;");
                    return resultMap;
                }else{
                    SkpVo skpvo = new SkpVo();
                    //新增开票点。
                    if(null !=clientData.getType() && clientData.getType().equals("01")){
                        skpvo.setKpddm(clientData.getClientNO());
                        skpvo.setKpdmc(clientData.getName());
                        skpvo.setSkph(clientData.getEquipNum());
                        skpvo.setSbcs(clientData.getTaxEquip());
                        skpvo.setLxdz(clientData.getKpdz());
                        skpvo.setSkpmm(clientData.getTaxDiskPass());
                        skpvo.setZsmm(clientData.getCertiCipher());
                        skpvo.setDevicesn(clientData.getDeviceSN());
                        skpvo.setDevicepassword(clientData.getDevicePSWD());
                        skpvo.setDevicekey(clientData.getDeviceKEY());
                        skpvo.setLxdh(clientData.getKpdh());
                        skpvo.setKhyh(clientData.getKpyh());
                        skpvo.setYhzh(clientData.getKpyhzh());
                        skpvo.setSkr(clientData.getPayee());
                        skpvo.setFhr(clientData.getReviewer());
                        skpvo.setKpr(clientData.getDrawer());
                        skpvo.setDpmax(xfBo.getDzpzdje());
                        skpvo.setFpfz(xfBo.getDzpfpje());
                        skpvo.setZpmax(xfBo.getZpzdje());
                        skpvo.setZpfz(xfBo.getZpfpje());
                        skpvo.setPpmax(xfBo.getPpzdje());
                        skpvo.setPpfz(xfBo.getPpfpje());
                        skpvo.setLrry(xfParm.getLrry());
                        skpvo.setLrsj(new Date());
                        skpvo.setXgry(xfParm.getLrry());
                        skpvo.setXgsj(new Date());
                        skpvo.setYxbz("1");
                        skpvo.setPpdm(clientData.getBrandCode());
                        skpvo.setPpmc(clientData.getBrandName());
                        String fplx = "";
                        if(null !=skpvo.getDpmax()&& !skpvo.getDpmax().equals("")){
                            fplx = "12";
                        }
                        if(null !=skpvo.getZpmax()&& !skpvo.getZpmax().equals("")) {
                            fplx = fplx.equals("")?"01":fplx+",01";
                        }
                        if(null !=skpvo.getPpmax()&& !skpvo.getPpmax().equals("")) {
                            fplx = fplx.equals("")?"02":fplx+",02";
                        }
                        skpvo.setKplx(fplx.equals("")?null:fplx);
                        skpvo.setWrzs("1");//无人值守 ：默认1
                        skpvo.setGsdm(xfBo.getGsdm());
                        skpvo.setXfid(xfBo.getId());
                        result = initialCheckUtil.checkClient(skpvo,clientData.getType(),false);
                        if(!result.equals("")){
                            resultMap.put("reCode","9999");
                            resultMap.put("reMessage",result);
                            return resultMap;
                        }else{
                            Roles roles = rolesService.findDefaultOneByParams(null);
                            Yh yh = new Yh();
                            yh.setDlyhid(skpvo.getSkph());
                            yh.setSjhm(null);
                            yh.setYx(null);
                            yh.setXb("1");
                            yh.setYhmc("开票用户" + yh.getDlyhid());
                            yh.setGsdm(xfBo.getGsdm());
                            yh.setYhmm(PasswordUtils.encrypt("12345678"));
                            yh.setRoleids(roles.getId().toString());
                            yh.setSjhm(null);
                            yh.setYx(null);
                            yh.setYxbz("1");
                            yh.setLrry(1);
                            yh.setXgry(1);
                            yh.setLrsj(new Date());
                            yh.setXgsj(new Date());
                            yhService.save(yh);
                            skpvo.setLrry(yh.getId());
                            skpvo.setXgry(yh.getId());
                            skpService.saveNew(new Skp(skpvo));
                            resultMap.put("reCode","0000");
                            resultMap.put("reMessage","门店信息新增成功！");
                            resultMap.put("dlyhid", yh.getDlyhid());
                            resultMap.put("yhmm", "12345678");
                            resultMap.put("xfsh", xfBo.getXfsh());
                            resultMap.put("xfmc", xfBo.getXfmc());
                            return resultMap;
                        }
                    }else if(null !=clientData.getType() && clientData.getType().equals("02")){
                        skpvo.setKpddm(clientData.getClientNO());
                        skpvo.setKpdmc(clientData.getName());
                        skpvo.setSkph(clientData.getEquipNum());
                        skpvo.setSbcs(clientData.getTaxEquip());
                        skpvo.setLxdz(clientData.getKpdz());
                        skpvo.setDevicesn(clientData.getDeviceSN());
                        skpvo.setDevicepassword(clientData.getDevicePSWD());
                        skpvo.setDevicekey(clientData.getDeviceKEY());
                        skpvo.setSkpmm(clientData.getTaxDiskPass());
                        skpvo.setZsmm(clientData.getCertiCipher());
                        skpvo.setLxdh(clientData.getKpdh());
                        skpvo.setKhyh(clientData.getKpyh());
                        skpvo.setYhzh(clientData.getKpyhzh());
                        skpvo.setSkr(clientData.getPayee());
                        skpvo.setFhr(clientData.getReviewer());
                        skpvo.setKpr(clientData.getDrawer());
                        skpvo.setPpdm(clientData.getBrandCode());
                        skpvo.setPpmc(clientData.getBrandName());
                        skpvo.setGsdm(xfBo.getGsdm());
                        skpvo.setXfid(xfBo.getId());
                        result = initialCheckUtil.checkClient(skpvo,clientData.getType(),false);
                        if(!result.equals("")){
                            resultMap.put("reCode","9999");
                            resultMap.put("reMessage",result);
                            return resultMap;
                        }else{
                            //校验通过，进行修改处理
                            Skp params = new Skp();
                            params.setGsdm(skpvo.getGsdm());
                            params.setXfid(skpvo.getXfid());
                            params.setSkph(skpvo.getSkph());
                            List<Skp> skptmpList = skpService.findAllByParams(params);
                            for(int i=0;i<skptmpList.size();i++){
                                Skp skptmp = skptmpList.get(i);
                                if(null !=skpvo.getKpdmc() && !skpvo.getKpdmc().equals("") && skptmpList.size()==1){
                                    skptmp.setKpdmc(skpvo.getKpdmc());
                                }
                                if(null !=skpvo.getPpdm() && !skpvo.getPpdm().equals("")){
                                    skptmp.setPid(skpvo.getPid());
                                }
                                if(null !=skpvo.getSkr() && !skpvo.getSkr().equals("") && skptmpList.size()==1){
                                    skptmp.setSkr(skpvo.getSkr());
                                }
                                if(null !=skpvo.getFhr() && !skpvo.getFhr().equals("") && skptmpList.size()==1){
                                    skptmp.setFhr(skpvo.getFhr());
                                }
                                if(null !=skpvo.getKpr() && !skpvo.getKpr().equals("") && skptmpList.size()==1){
                                    skptmp.setKpr(skpvo.getKpr());
                                }
                                if(null !=skpvo.getLxdz() && !skpvo.getLxdz().equals("") && skptmpList.size()==1){
                                    skptmp.setLxdz(skpvo.getLxdz());
                                }
                                if(null !=skpvo.getLxdh() && !skpvo.getLxdh().equals("") && skptmpList.size()==1){
                                    skptmp.setLxdh(skpvo.getLxdh());
                                }
                                if(null !=skpvo.getKhyh() && !skpvo.getKhyh().equals("") && skptmpList.size()==1){
                                    skptmp.setKhyh(skpvo.getKhyh());
                                }
                                if(null !=skpvo.getYhzh() && !skpvo.getYhzh().equals("") && skptmpList.size()==1){
                                    skptmp.setYhzh(skpvo.getYhzh());
                                }
                                if(null !=skpvo.getSbcs() && !skpvo.getSbcs().equals("")){
                                    skptmp.setSbcs(skpvo.getSbcs());
                                }
                                if(null !=skpvo.getSkph() && !skpvo.getSkph().equals("")){
                                    skptmp.setSkph(skpvo.getSkph());
                                }
                                if(null !=skpvo.getSkpmm() && !skpvo.getSkpmm().equals("")){
                                    skptmp.setSkpmm(skpvo.getSkpmm());
                                }
                                if(null !=skpvo.getZsmm() && !skpvo.getZsmm().equals("")){
                                    skptmp.setZsmm(skpvo.getZsmm());
                                }
                                if(null !=skpvo.getDevicesn() && !skpvo.getDevicesn().equals("")){
                                    skptmp.setDevicesn(skpvo.getDevicesn());
                                }
                                if(null !=skpvo.getDevicepassword() && !skpvo.getDevicepassword().equals("")){
                                    skptmp.setDevicepassword(skpvo.getDevicepassword());
                                }
                                if(null !=skpvo.getDevicekey() && !skpvo.getDevicekey().equals("")){
                                    skptmp.setDevicekey(skpvo.getDevicekey());
                                }
                                skptmp.setXgsj(new Date());
                            }
                            skpJpaDao.save(skptmpList);
                            resultMap.put("reCode","0000");
                            resultMap.put("reMessage","门店信息更新成功！");
                            return resultMap;
                        }
                    }else {
                        resultMap.put("reCode","9999");
                        resultMap.put("reMessage","type参数必须传入;");
                        return resultMap;
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("reCode","9999");
            resultMap.put("reMessage","门店信息初始化失败;");
            return resultMap;
        }
    }
}
