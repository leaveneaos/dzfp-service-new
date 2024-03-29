package com.rjxx.taxeasey.service.dealorder;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasey.service.result.CommQueryClientData;
import com.rjxx.taxeasey.service.result.CommQueryData;
import com.rjxx.taxeasey.utils.InitialCheckUtil;
import com.rjxx.taxeasey.utils.ResponeseUtils;
import com.rjxx.taxeasey.utils.XmlMapUtils;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.PpJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.dto.ClientData;
import com.rjxx.taxeasy.dto.CommonData;
import com.rjxx.taxeasy.dto.SellerData;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.SkpVo;
import com.rjxx.time.TimeUtil;
import com.rjxx.utils.PasswordUtils;
import com.rjxx.utils.RJCheckUtil;
import org.apache.axiom.om.OMElement;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service("dealCommData")
public class DealCommData {

    @Autowired
    private XfService xfService;
    @Autowired
    private SkpService skpService;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private YhService yhService;
    @Autowired
    private CszbService cszbService;

    @Autowired
    private CsbService csbService;

    @Autowired
    private PpJpaDao ppJpaDao;

    @Autowired
    protected GsxxJpaDao gsxxJpaDao ;

    @Autowired
    protected XfJpaDao xfJpaDao;

    @Autowired
    private InitialCheckUtil initialCheckUtil;

    @Autowired
    protected InitialData initialData;

    public String execute(Gsxx gsxx, String OrderData) {
        Map resultMap = new HashMap();
        String result = "";//处理返回后的结果信息
        //调用解析xml的公共方法。
        resultMap = dealCommonData(gsxx,OrderData);
        Xf xf = (Xf)resultMap.get("Xf");
        List<SkpVo> skpList = (List)resultMap.get("skpList");
        String issueType = (String)resultMap.get("issueType");
        //调用校验数据是否符合规则方法。
        result = initialCheckUtil.checkCommData(xf,skpList,issueType,false);
        if(null != result && !result.equals("")){
            return ResponeseUtils.printCommDataResult("9999",result,new HashMap());
        }else{
            //保存待处理数据。
            resultMap = saveXfAndSkp(xf,skpList,issueType,gsxx);
            //保存失败
            if(null == resultMap || resultMap.isEmpty()){
                return ResponeseUtils.printCommDataResult("9999","保存信息出错",new HashMap());
            }else{
                /*String dlyhid = (String)resultMap.get("dlyhid");
                String yhmm = (String)resultMap.get("yhmm");
                String xfsh = (String)resultMap.get("xfsh");
                String xfmc = (String)resultMap.get("xfmc");*/
                return ResponeseUtils.printCommDataResult("0000","成功",resultMap);
            }
        }
        //return result;
    }

    /**
     * post Json 销货方门店信息新增接口
     * @param OrderData
     * @return
     */
    public String execute2(String OrderData) {
        Map resultMap = new HashMap();
        String result = "";//处理返回后的结果信息
        //JSONObject jsonObject = null;
        HashMap<String, Object> jsonObject = null;
        try {
            //jsonObject = JSON.parseObject(OrderData);
            jsonObject = JSON.parseObject(OrderData,LinkedHashMap.class, Feature.OrderedField);

        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！",new HashMap());
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject data = (JSONObject)jsonObject.get("seller");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护",new HashMap());
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过",new HashMap());
            }else{
                //调用解析Json的公共方法。
                ObjectMapper mapper = new ObjectMapper();
                CommonData commonData=mapper.readValue(JSON.parseObject(OrderData).toJSONString(), CommonData.class);
                resultMap = dealCommonData2(gsxx, commonData);
                Xf xf = (Xf)resultMap.get("Xf");
                List<SkpVo> skpList = (List)resultMap.get("skpList");
                String issueType = String.valueOf(resultMap.get("issueType"));
                //调用校验数据是否符合规则方法。
                boolean isCrestv = false;
                //凯盈盒子不校验销方是否已存在，只做盘的新增操作。
                if(gsxx.getGsdm().equals("crestv") || gsxx.getGsdm().equals("rjxx")){
                    isCrestv = true;
                }
                result = initialCheckUtil.checkCommData(xf,skpList,issueType,isCrestv);
                if(null != result && !result.equals("")){
                    return ResponeseUtils.printResultToJson("9999",result,new HashMap());
                }else{
                    //保存待处理数据。
                    resultMap = saveXfAndSkp(xf,skpList,issueType,gsxx);
                    //保存失败
                    if(null == resultMap || resultMap.isEmpty()){
                        return ResponeseUtils.printResultToJson("9999","保存信息出错",new HashMap());
                    }else{
                        /*String dlyhid = (String)resultMap.get("dlyhid");
                        String yhmm = (String)resultMap.get("yhmm");
                        String xfsh = (String)resultMap.get("xfsh");
                        String xfmc = (String)resultMap.get("xfmc");*/
                        return ResponeseUtils.printResultToJson("0000","成功",resultMap);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","公共信息初始化失败！",new HashMap());
        }

    }


    /**
     *  门店信息新增和更新接口方法（凯盈使用）
     * @param clientStr
     * @return
     */
    public String execute4(String clientStr) {
        Map resultMap = new HashMap();
        String reCode = "";//处理返回后的结果信息
        //JSONObject jsonObject = null;
        HashMap<String, Object> jsonObject = null;
        try {
            //jsonObject = JSON.parseObject(clientStr);
            jsonObject = JSON.parseObject(clientStr,LinkedHashMap.class, Feature.OrderedField);

        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！",new HashMap());
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject data = (JSONObject)jsonObject.get("client");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护",new HashMap());
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过",new HashMap());
            }else{
                //调用解析Json的公共方法。
                ObjectMapper mapper = new ObjectMapper();
                ClientData clientData=mapper.readValue(data.toJSONString(), ClientData.class);
                resultMap = updateclientData(gsxx, clientData);
                reCode = String.valueOf(resultMap.get("reCode"));
                if(null != reCode && !reCode.equals("0000")){
                    return ResponeseUtils.printResultToJson("9999",String.valueOf(resultMap.get("reMessage")),new HashMap());
                }else{
                    //需要调整
                    return ResponeseUtils.printResultToJson("0000",String.valueOf(resultMap.get("reMessage")),resultMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","门店信息更新失败！",new HashMap());
        }

    }


    /**
     *  门店信息新增和更新接口方法（渠道使用）
     * @param clientStr
     * @return
     */
    public String initialClient(String clientStr) {
        Map resultMap = new HashMap();
        String reCode = "";//处理返回后的结果信息
        //JSONObject jsonObject = null;
        HashMap<String, Object> jsonObject = null;
        try {
            //jsonObject = JSON.parseObject(clientStr);
            jsonObject = JSON.parseObject(clientStr,LinkedHashMap.class, Feature.OrderedField);

        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！",new HashMap());
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject data = (JSONObject)jsonObject.get("client");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护",new HashMap());
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过",new HashMap());
            }else{
                //调用解析Json的公共方法。
                ObjectMapper mapper = new ObjectMapper();
                ClientData clientData=mapper.readValue(data.toJSONString(), ClientData.class);
                resultMap = initialData.initialClientData(gsxx, clientData);
                reCode = String.valueOf(resultMap.get("reCode"));
                if(null != reCode && !reCode.equals("0000")){
                    return ResponeseUtils.printResultToJson("9999",String.valueOf(resultMap.get("reMessage")),new HashMap());
                }else{
                    return ResponeseUtils.printResultToJson("0000",String.valueOf(resultMap.get("reMessage")),resultMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","门店信息更新失败！",new HashMap());
        }

    }

    /**
     *  销货方信息更新接口方法
     * @param sellerStr
     * @return
     */
    public String execute3(String sellerStr) {
        Map resultMap = new HashMap();
        String reCode = "";//处理返回后的结果信息
        //JSONObject jsonObject = null;
        HashMap<String, Object> jsonObject = null;
        try {
            //jsonObject = JSON.parseObject(sellerStr);
            jsonObject = JSON.parseObject(sellerStr,LinkedHashMap.class, Feature.OrderedField);
        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！",new HashMap());
        }
        try {
            String sign = String.valueOf(jsonObject.get("sign"));
            String appId = String.valueOf(jsonObject.get("appId"));
            JSONObject data = (JSONObject)jsonObject.get("seller");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId.toString());
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护",new HashMap());
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过",new HashMap());
            }else{
                //调用解析Json的公共方法。
                ObjectMapper mapper = new ObjectMapper();
                SellerData sellerData=mapper.readValue(data.toJSONString(), SellerData.class);
                resultMap = updateSellerData(gsxx, sellerData);
                reCode = String.valueOf(resultMap.get("reCode"));
                if(null != reCode && !reCode.equals("0000")){
                    return ResponeseUtils.printResultToJson("9999",String.valueOf(resultMap.get("reMessage")),new HashMap());
                }else{
                    //需要调整
                    return ResponeseUtils.printResultToJson("0000",String.valueOf(resultMap.get("reMessage")),resultMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","销货方信息更新失败！",new HashMap());
        }

    }

    /**
     * post Json 销售方及门店信息  查询接口（供凯盈使用）
     * @param OrderData
     * @return
     */
    public String execute5(String OrderData) {
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
                String xfsh = data.getString("identifier");
                String skph = data.getString("equipNum");
                Map params = new HashMap();
                //params.put("gsdm",gsxx.getGsdm());
                params.put("skph",skph);
                params.put("xfsh",xfsh);
                params.put("csz","04");
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
                    commQueryClientData.setDeviceSN(map.get("devicesn")==null?"":String.valueOf(map.get("devicesn")));//凯盈开票终端sn
                    commQueryClientData.setDevicePSWD(map.get("devicepassword")==null?"":String.valueOf(map.get("devicepassword")));
                    commQueryClientData.setDeviceKEY(map.get("devicekey")==null?"":String.valueOf(map.get("devicekey")));
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
     * 保存销方，开票点等信息，同时写入用户表，且
     * 插入一条权限到销方的group权限数据，根据issueType
     * 为03时保存一条数据kpfs到cszb中。
     * @param xf,skpList,issueType
     * @return  Map
     */
    @Transactional
    public Map saveXfAndSkp(Xf xf, List<SkpVo> skpVoList, String issueType,Gsxx gsxx){
        Map resultMap = new HashMap();
        Roles roles = rolesService.findDefaultOneByParams(null);
        Date lrsj = TimeUtil.getSysDate();
        Integer skpid = 0;
        try {
            if(null == roles || roles.equals("")){
                return resultMap;
            }else {
                //渠道调用新增销方接口。
                if (null == skpVoList || skpVoList.isEmpty()) {
                    Yh yh = new Yh();
                    yh.setDlyhid(xf.getXfsh());
                    yh.setSjhm(null);
                    yh.setYx(null);
                    yh.setXb("1");
                    yh.setYhmc("开票用户" + xf.getXfsh());
                    yh.setGsdm(xf.getGsdm());
                    yh.setYhmm(PasswordUtils.encrypt("12345678"));
                    yh.setRoleids(roles.getId().toString());
                    yh.setSjhm(null);
                    yh.setYx(null);
                    yh.setYxbz("1");
                    yh.setLrry(1);
                    yh.setXgry(1);
                    yh.setLrsj(lrsj);
                    yh.setXgsj(lrsj);
                    yhService.save(yh);
                    xf.setLrry(yh.getId());
                    xf.setXgry(yh.getId());
                    xfService.saveNew(xf);
                    if (null !=issueType || !issueType.equals("")) {
                        Cszb cszb = new Cszb();
                        Map map = new HashMap();
                        map.put("csm","kpfs");
                        Csb tmp = csbService.findOneByParams(map);
                        cszb.setCsid(tmp.getId());
                        cszb.setGsdm(xf.getGsdm());
                        cszb.setXfid(xf.getId());
                        cszb.setYxbz("1");
                        cszb.setLrry(xf.getLrry());
                        cszb.setXgry(xf.getLrry());
                        cszb.setKpdid(skpid.compareTo(0)>0?skpid:null);
                        cszb.setLrsj(TimeUtil.getSysDate());
                        cszb.setXgsj(TimeUtil.getSysDate());
                        cszb.setCsz(issueType);
                        cszbService.save(cszb);
                    }
                    resultMap.put("dlyhid", yh.getDlyhid());
                    resultMap.put("yhmm", "12345678");
                    resultMap.put("xfsh", xf.getXfsh());
                    resultMap.put("xfmc", xf.getXfmc());
                    resultMap.put("skph",null);
                    //return resultMap;
                } else {
                    Yh yh = new Yh();
                    yh.setDlyhid(skpVoList.get(0).getSkph());
                    yh.setSjhm(null);
                    yh.setYx(null);
                    yh.setXb("1");
                    yh.setYhmc("开票用户" + yh.getDlyhid());
                    yh.setGsdm(xf.getGsdm());
                    yh.setYhmm(PasswordUtils.encrypt("12345678"));
                    yh.setRoleids(roles.getId().toString());
                    yh.setSjhm(null);
                    yh.setYx(null);
                    yh.setYxbz("1");
                    yh.setLrry(1);
                    yh.setXgry(1);
                    yh.setLrsj(lrsj);
                    yh.setXgsj(lrsj);
                    yhService.save(yh);
                    xf.setLrry(yh.getId());
                    xf.setXgry(yh.getId());
                    xfJpaDao.save(xf);
                    List<Skp> skpList = new ArrayList<>();
                    for (int i = 0; i < skpVoList.size(); i++) {
                        SkpVo skpvo = skpVoList.get(i);
                        int pid = 33;
                        if (null != skpvo.getPpdm() && !skpvo.getPpdm().equals("")) {
                            Pp pp = new Pp();
                            pp.setPpdm(skpvo.getPpdm());
                            pp.setPpmc(skpvo.getPpmc());
                            pp.setYxbz("1");
                            pp.setLrry(yh.getId());
                            pp.setXgry(yh.getId());
                            pp.setLrsj(lrsj);
                            pp.setXgsj(lrsj);
                            pp.setGsdm(skpvo.getGsdm());
                            pid = ppJpaDao.save(pp).getId();
                        }
                        skpvo.setXfid(xf.getId());
                        skpvo.setLrry(yh.getId());
                        skpvo.setXgry(yh.getId());
                        skpvo.setLrsj(lrsj);
                        skpvo.setXgsj(lrsj);
                        skpvo.setGsdm(xf.getGsdm());
                        skpvo.setPid(pid);
                        Skp skp = new Skp(skpvo);
                        skpList.add(skp);
                    }

                    skpService.save(skpList);
                    skpid = skpList.get(0).getId();
                    resultMap.put("dlyhid", yh.getDlyhid());
                    resultMap.put("yhmm", "12345678");
                    resultMap.put("xfsh", xf.getXfsh());
                    resultMap.put("xfmc", xf.getXfmc());
                    resultMap.put("skph",yh.getDlyhid());
                }
                if (issueType.equals("03") || issueType.equals("04")) {
                    Cszb cszb = new Cszb();
                    Map map = new HashMap();
                    map.put("csm","kpfs");
                    Csb tmp = csbService.findOneByParams(map);
                    cszb.setCsid(tmp.getId());
                    cszb.setGsdm(xf.getGsdm());
                    cszb.setXfid(xf.getId());
                    cszb.setYxbz("1");
                    cszb.setLrry(xf.getLrry());
                    cszb.setXgry(xf.getLrry());
                    cszb.setKpdid(skpid.compareTo(0)>0?skpid:null);
                    cszb.setLrsj(TimeUtil.getSysDate());
                    cszb.setXgsj(TimeUtil.getSysDate());
                    cszb.setCsz(issueType);
                    cszbService.save(cszb);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return resultMap;

    }

    public static void main(String args[]){
        String t ="1234567R43312";
        String tmp = t.substring(t.length()-5,t.length());
        System.out.println(tmp.toLowerCase());
    }

    /**
     * 解析xml数据方法。
     *
     * @param gsxx
     * @param OrderData
     * @return
     */
    private Map dealCommonData(Gsxx gsxx, String OrderData) {
        OMElement root = null;
        Document xmlDoc = null;
        Map resultMap = new HashMap();
        Xf xfBo = new Xf();
        List<SkpVo> skpList = new ArrayList<SkpVo>();
        try {
            xmlDoc = DocumentHelper.parseText(OrderData);
            root = XmlMapUtils.xml2OMElement(OrderData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map rootMap = XmlMapUtils.xml2Map(root, "Seller");
        String sellerIdentifier = (String) rootMap.get("Identifier");
        String sellerName = (String) rootMap.get("Name");
        String sellerAddress = (String) rootMap.get("Address");
        String sellerTel = (String) rootMap.get("TelephoneNo");
        String sellerBank = (String) rootMap.get("Bank");
        String sellerBankAcc = (String) rootMap.get("BankAcc");
        String ybnsrqssj = (String) rootMap.get("Ybnsrqssj");
        String ybnsrlx = (String) rootMap.get("Ybnsrlx");
        String drawer = (String) rootMap.get("Drawer");//开票人
        String payee = (String) rootMap.get("Payee");//收款人
        String reviewer = (String) rootMap.get("Reviewer");//复核人
        String issueType = (String) rootMap.get("IssueType");//开票方式（01税控盘或金税盘开票，03税控盘组或税控服务器开票）
        String eticketLim = (String) rootMap.get("EticketLim");//电子票开票限额
        String specialticketLim = (String) rootMap.get("SpecialticketLim");//专用发票开票限额
        String ordinaryticketLim = (String) rootMap.get("OrdinaryticketLim");//普通发票开票限额
        xfBo.setXfmc(sellerName);
        xfBo.setXfsh(sellerIdentifier);
        xfBo.setSjjgbm(null);
        xfBo.setXfdh(sellerTel);
        xfBo.setXfdz(sellerAddress);
        xfBo.setXfyh(sellerBank);
        xfBo.setXfyhzh(sellerBankAcc);
        xfBo.setYxbz("1");
        xfBo.setYbnsrqssj(ybnsrqssj);
        xfBo.setYbnsrjyzs(ybnsrlx);
        xfBo.setFhr(reviewer);
        xfBo.setGsdm(gsxx.getGsdm());
        xfBo.setKpr(drawer);
        xfBo.setSkr(payee);
        xfBo.setLrry(1);
        xfBo.setXgry(1);
        xfBo.setLrsj(new Date());
        xfBo.setXgsj(new Date());
        xfBo.setXflxr(null);
        xfBo.setXfyb(null);
        xfBo.setZfr(null);
        xfBo.setDzpzdje(eticketLim.equals("")?null:Double.valueOf(eticketLim));
        xfBo.setDzpfpje(eticketLim.equals("")?null:Double.valueOf(eticketLim));
        xfBo.setZpzdje(specialticketLim.equals("")?null:Double.valueOf(specialticketLim));
        xfBo.setZpfpje(specialticketLim.equals("")?null:Double.valueOf(specialticketLim));
        xfBo.setPpzdje(ordinaryticketLim.equals("")?null:Double.valueOf(ordinaryticketLim));
        xfBo.setPpfpje(ordinaryticketLim.equals("")?null:Double.valueOf(ordinaryticketLim));

        List<Element> xnList = xmlDoc.selectNodes("Seller/Clients");
        if (null != xnList && xnList.size() > 0) {
            for (Element xn : xnList) {
                SkpVo skpvo = new SkpVo();
                Element clientMap = (Element) xn.selectSingleNode("Client");
                // 开票点代码
                String clientNO = "";
                if (null != clientMap.selectSingleNode("ClientNO")
                        && !clientMap.selectSingleNode("ClientNO").equals("")) {
                    clientNO = clientMap.selectSingleNode("ClientNO").getText();
                }
                String clientName = "";//开票点名称
                if (null != clientMap.selectSingleNode("Name")
                        && !clientMap.selectSingleNode("Name").equals("")) {
                    clientName = clientMap.selectSingleNode("Name").getText();
                }
                String taxEquip = "";//税控设备厂商,1表示百旺厂商设备，2表示航信厂商设备
                if (null != clientMap.selectSingleNode("TaxEquip")
                        && !clientMap.selectSingleNode("TaxEquip").equals("")) {
                    taxEquip = clientMap.selectSingleNode("TaxEquip").getText();
                }
                String equipNum = "";//税控设备号
                if (null != clientMap.selectSingleNode("EquipNum")
                        && !clientMap.selectSingleNode("EquipNum").equals("")) {
                    equipNum = clientMap.selectSingleNode("EquipNum").getText();
                }
                String taxDiskPass = "";//TaxDiskPass税控盘密码
                if (null != clientMap.selectSingleNode("TaxDiskPass")
                        && !clientMap.selectSingleNode("TaxDiskPass").equals("")) {
                    taxDiskPass = clientMap.selectSingleNode("TaxDiskPass").getText();
                }
                String certiCipher = "";//当TaxEquip为1时必须，反之可选，CertiCipher证书密码
                if (null != clientMap.selectSingleNode("CertiCipher")
                        && !clientMap.selectSingleNode("CertiCipher").equals("")) {
                    certiCipher = clientMap.selectSingleNode("CertiCipher").getText();
                }

                String brandCode ="";//品牌代码
                if (null != clientMap.selectSingleNode("BrandCode")
                        && !clientMap.selectSingleNode("BrandCode").equals("")) {
                    brandCode = clientMap.selectSingleNode("BrandCode").getText();
                }

                String brandName ="";//品牌名称
                if (null != clientMap.selectSingleNode("BrandName")
                        && !clientMap.selectSingleNode("BrandName").equals("")) {
                    brandName = clientMap.selectSingleNode("BrandName").getText();
                }
                //skp.setXfid(xfid);保存时需要更新进去
                skpvo.setKpddm(clientNO);
                skpvo.setKpdmc(clientName);
                skpvo.setSkph(equipNum);
                skpvo.setSbcs(taxEquip);
                skpvo.setLxdz(sellerAddress);
                skpvo.setSkpmm(taxDiskPass);
                skpvo.setZsmm(certiCipher);
                skpvo.setLxdh(sellerTel);
                skpvo.setKhyh(sellerBank);
                skpvo.setYhzh(sellerBankAcc);
                skpvo.setSkr(payee);
                skpvo.setFhr(reviewer);
                skpvo.setKpr(drawer);
                skpvo.setDpmax(eticketLim.equals("")?null:Double.valueOf(eticketLim));
                skpvo.setFpfz(eticketLim.equals("")?null:Double.valueOf(eticketLim));
                skpvo.setZpmax(specialticketLim.equals("")?null:Double.valueOf(specialticketLim));
                skpvo.setZpfz(specialticketLim.equals("")?null:Double.valueOf(specialticketLim));
                skpvo.setPpmax(ordinaryticketLim.equals("")?null:Double.valueOf(ordinaryticketLim));
                skpvo.setPpfz(ordinaryticketLim.equals("")?null:Double.valueOf(ordinaryticketLim));
                skpvo.setLrry(1);
                skpvo.setLrsj(new Date());
                skpvo.setXgry(1);
                skpvo.setXgsj(new Date());
                skpvo.setYxbz("1");
                skpvo.setPpdm(brandCode);
                skpvo.setPpmc(brandName);
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
                skpvo.setGsdm(gsxx.getGsdm());
                skpList.add(skpvo);
            }
        }
        resultMap.put("Xf",xfBo);
        resultMap.put("skpList",skpList);
        resultMap.put("issueType",issueType);
        return resultMap;
    }


    /**
     * 解析Json转换成CommData数据方法。
     *
     * @param gsxx
     * @param commonData
     * @return
     */
     public Map dealCommonData2(Gsxx gsxx, CommonData commonData) {

        Map resultMap = new HashMap();
        Xf xfBo = new Xf();
        SellerData sellerData = commonData.getSeller();
        List<SkpVo> skpList = new ArrayList<SkpVo>();
        xfBo.setXfmc(sellerData.getName());
        xfBo.setXfsh(sellerData.getIdentifier());
        xfBo.setSjjgbm(null);
        xfBo.setXfdh(sellerData.getTelephoneNo());
        xfBo.setXfdz(sellerData.getAddress());
        xfBo.setXfyh(sellerData.getBank());
        xfBo.setXfyhzh(sellerData.getBankAcc());
        xfBo.setYxbz("1");
        xfBo.setYbnsrqssj(sellerData.getYbnsrqssj());
        xfBo.setYbnsrjyzs(sellerData.getYbnsrlx());
        xfBo.setFhr(sellerData.getReviewer());
        xfBo.setGsdm(gsxx.getGsdm());
        xfBo.setKpr(sellerData.getDrawer());
        xfBo.setSkr(sellerData.getPayee());
        xfBo.setLrry(1);
        xfBo.setXgry(1);
        xfBo.setLrsj(new Date());
        xfBo.setXgsj(new Date());
        xfBo.setXflxr(null);
        xfBo.setXfyb(null);
        xfBo.setZfr(null);
        xfBo.setDzpzdje(Double.valueOf(sellerData.getEticketLim()) ==null?null:sellerData.getEticketLim());
        xfBo.setDzpfpje(Double.valueOf(sellerData.getEticketLim()) ==null?null:sellerData.getEticketLim());
        xfBo.setZpzdje(Double.valueOf(sellerData.getSpecialticketLim()) ==null?null:sellerData.getSpecialticketLim());
        xfBo.setZpfpje(Double.valueOf(sellerData.getSpecialticketLim()) ==null?null:sellerData.getSpecialticketLim());
        xfBo.setPpzdje(Double.valueOf(sellerData.getOrdinaryticketLim()) ==null?null:sellerData.getOrdinaryticketLim());
        xfBo.setPpfpje(Double.valueOf(sellerData.getOrdinaryticketLim()) ==null?null:sellerData.getOrdinaryticketLim());
        xfBo.setJpzdje(Double.valueOf(sellerData.getRollticketLim()) ==null?null:sellerData.getRollticketLim());
        xfBo.setJpfpje(Double.valueOf(sellerData.getRollticketLim()) ==null?null:sellerData.getRollticketLim());
        List<ClientData> clientDataList = sellerData.getClient();
        for(int i=0;i<clientDataList.size();i++){
            ClientData clientData = clientDataList.get(i);
            SkpVo skpvo = new SkpVo();
            skpvo.setKpddm(clientData.getClientNO());
            skpvo.setKpdmc(clientData.getName());
            skpvo.setSkph(clientData.getEquipNum());
            skpvo.setSbcs(clientData.getTaxEquip());
            skpvo.setLxdz(xfBo.getXfdz());
            skpvo.setSkpmm(clientData.getTaxDiskPass());
            skpvo.setZsmm(clientData.getCertiCipher());
            skpvo.setDevicesn(clientData.getDeviceSN());
            skpvo.setDevicepassword(clientData.getDevicePSWD());
            skpvo.setDevicekey(clientData.getDeviceKEY());
            skpvo.setLxdh(xfBo.getXfdh());
            skpvo.setKhyh(xfBo.getXfyh());
            skpvo.setYhzh(xfBo.getXfyhzh());
            skpvo.setSkr(xfBo.getSkr());
            skpvo.setFhr(xfBo.getFhr());
            skpvo.setKpr(xfBo.getKpr());
            skpvo.setDpmax(xfBo.getDzpzdje());
            skpvo.setFpfz(xfBo.getDzpfpje());
            skpvo.setZpmax(xfBo.getZpzdje());
            skpvo.setZpfz(xfBo.getZpfpje());
            skpvo.setPpmax(xfBo.getPpzdje());
            skpvo.setPpfz(xfBo.getPpfpje());
            skpvo.setJpfz(xfBo.getJpfpje());
            skpvo.setJpmax(xfBo.getJpzdje());
            skpvo.setLrry(1);
            skpvo.setLrsj(new Date());
            skpvo.setXgry(1);
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
            skpvo.setGsdm(gsxx.getGsdm());
            skpList.add(skpvo);
        }


        resultMap.put("Xf",xfBo);
        resultMap.put("skpList",skpList);
        resultMap.put("issueType",sellerData.getIssueType());
        return resultMap;
    }

    /**
     * 校验销货方数据及更新数据方法。
     *
     * @param gsxx
     * @param sellerData
     * @return
     */
    private Map updateSellerData(Gsxx gsxx, SellerData sellerData) {
        Map resultMap = new HashMap();
        String yxfsh = sellerData.getYidentifier();
        String type = sellerData.getType();
        if((null == type || type.equals("")) || (!type.equals("01") && !type.equals("02"))){
            resultMap.put("reCode","9999");
            resultMap.put("reMessage","type更新类型不能为空且只能为01或02！");
            return resultMap;
        }else{
            boolean isupdate = false;//判断是否是更新销方，true表示是
            boolean isCrestv = false;//判断是否是凯盈初始化销货方，true表示是
            if(gsxx.getGsdm().equals("crestv") || gsxx.getGsdm().equals("rjxx")){
                isCrestv = true;
            }
            if(null !=yxfsh && !yxfsh.equals("")){
                try {
                    if(type.equals("01")){
                        Xf addXf = new Xf();
                        if(null != sellerData.getIdentifier() && !sellerData.getIdentifier().equals("")){
                            addXf.setXfsh(sellerData.getIdentifier());
                        }
                        if(null != sellerData.getName() && !sellerData.getName().equals("")){
                            addXf.setXfmc(sellerData.getName());
                        }
                        if(null != sellerData.getAddress() && !sellerData.getAddress().equals("")){
                            addXf.setXfdz(sellerData.getAddress());
                        }
                        if(null != sellerData.getTelephoneNo() && !sellerData.getTelephoneNo().equals("")){
                            addXf.setXfdh(sellerData.getTelephoneNo());
                        }
                        if(null != sellerData.getBank() && !sellerData.getBank().equals("")){
                            addXf.setXfyh(sellerData.getBank());
                        }
                        if(null != sellerData.getBankAcc() && !sellerData.getBankAcc().equals("")){
                            addXf.setXfyhzh(sellerData.getBankAcc());
                        }
                        if(null != sellerData.getYbnsrqssj() && !sellerData.getYbnsrqssj().equals("")){
                            addXf.setYbnsrqssj(sellerData.getYbnsrqssj());
                        }
                        if(null != sellerData.getYbnsrlx() && !sellerData.getYbnsrlx().equals("")){
                            addXf.setYbnsrjyzs(sellerData.getYbnsrlx());
                        }
                        if(null != sellerData.getDrawer() && !sellerData.getDrawer().equals("")){
                            addXf.setKpr(sellerData.getDrawer());
                        }
                        if(null != sellerData.getPayee() && !sellerData.getPayee().equals("")){
                            addXf.setSkr(sellerData.getPayee());
                        }
                        if(null != sellerData.getReviewer() && !sellerData.getReviewer().equals("")){
                            addXf.setFhr(sellerData.getReviewer());
                        }
                        if(sellerData.getEticketLim()>0d){
                            addXf.setDzpzdje(sellerData.getEticketLim());
                            addXf.setDzpfpje(sellerData.getEticketLim());
                        }
                        if(sellerData.getSpecialticketLim()>0d){
                            addXf.setZpzdje(sellerData.getSpecialticketLim());
                            addXf.setZpfpje(sellerData.getSpecialticketLim());
                        }
                        if(sellerData.getOrdinaryticketLim()>0d){
                            addXf.setPpzdje(sellerData.getOrdinaryticketLim());
                            addXf.setPpfpje(sellerData.getOrdinaryticketLim());
                        }
                        String issueType = "";
                        if(null != sellerData.getIssueType() && !sellerData.getIssueType().equals("")){
                            issueType = sellerData.getIssueType();
                        }
                        String res = initialCheckUtil.checkSeller(addXf,issueType,isupdate,isCrestv);
                        if(null != res && !res.equals("")){
                            resultMap.put("reCode","9999");
                            resultMap.put("reMessage",res);
                            return resultMap;
                        }else{
                            Roles roles = rolesService.findDefaultOneByParams(null);
                            Yh yh = new Yh();
                            yh.setDlyhid(addXf.getXfsh());
                            yh.setSjhm(null);
                            yh.setYx(null);
                            yh.setXb("1");
                            yh.setYhmc("开票用户" + yh.getDlyhid());
                            yh.setGsdm(gsxx.getGsdm());
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
                            addXf.setLrry(yh.getId());
                            addXf.setXgry(yh.getId());
                            addXf.setLrsj(new Date());
                            addXf.setXgsj(new Date());
                            addXf.setGsdm(gsxx.getGsdm());
                            addXf.setYxbz("1");
                            xfService.saveNew(addXf);
                            resultMap.put("reCode","0000");
                            resultMap.put("reMessage","销货方新增成功！");
                            resultMap.put("dlyhid", yh.getDlyhid());
                            resultMap.put("yhmm", "12345678");
                            resultMap.put("xfsh", addXf.getXfsh());
                            resultMap.put("xfmc", addXf.getXfmc());
                        }
                    }else{
                        //更新銷貨方信息
                        Xf xf =null;
                        isupdate =true;
                        if(isCrestv){
                            Xf xfParm = new Xf();
                            //xfParm.setGsdm(gsxx.getGsdm());
                            xfParm.setXfsh(yxfsh);
                            xf = xfService.findOneByParams(xfParm);
                        }else{
                            Xf xfParm = new Xf();
                            xfParm.setGsdm(gsxx.getGsdm());
                            xfParm.setXfsh(yxfsh);
                            xf = xfService.findOneByParams(xfParm);
                        }
                        if(null ==xf){
                            resultMap.put("reCode","9999");
                            resultMap.put("reMessage","原销货方税号"+yxfsh+"未查到需要修改的销货方！");
                            return resultMap;
                        }else{
                            if(null !=sellerData.getIdentifier() && !sellerData.getIdentifier().equals("")){
                                xf.setXfsh(sellerData.getIdentifier());
                                Xf xfParm2 = new Xf();
                                if(!isCrestv){
                                    xfParm2.setGsdm(gsxx.getGsdm());
                                }
                                xfParm2.setXfsh(sellerData.getIdentifier());
                                Xf xf2 = xfService.findOneByParams(xfParm2);
                                if(null !=xf2 && !sellerData.getYidentifier().equals(sellerData.getIdentifier())){
                                    resultMap.put("reCode","9999");
                                    resultMap.put("reMessage","新销货方税号"+sellerData.getIdentifier()+"对应销货方已存在！");
                                    return resultMap;
                                }
                            }
                            if(null != sellerData.getName() && !sellerData.getName().equals("")){
                                xf.setXfmc(sellerData.getName());
                            }
                            if(null != sellerData.getAddress() && !sellerData.getAddress().equals("")){
                                xf.setXfdz(sellerData.getAddress());
                            }
                            if(null != sellerData.getTelephoneNo() && !sellerData.getTelephoneNo().equals("")){
                                xf.setXfdh(sellerData.getTelephoneNo());
                            }
                            if(null != sellerData.getBank() && !sellerData.getBank().equals("")){
                                xf.setXfyh(sellerData.getBank());
                            }
                            if(null != sellerData.getBankAcc() && !sellerData.getBankAcc().equals("")){
                                xf.setXfyhzh(sellerData.getBankAcc());
                            }
                            if(null != sellerData.getYbnsrqssj() && !sellerData.getYbnsrqssj().equals("")){
                                xf.setYbnsrqssj(sellerData.getYbnsrqssj());
                            }
                            if(null != sellerData.getYbnsrlx() && !sellerData.getYbnsrlx().equals("")){
                                xf.setYbnsrjyzs(sellerData.getYbnsrlx());
                            }
                            if(null != sellerData.getDrawer() && !sellerData.getDrawer().equals("")){
                                xf.setKpr(sellerData.getDrawer());
                            }
                            if(null != sellerData.getPayee() && !sellerData.getPayee().equals("")){
                                xf.setSkr(sellerData.getPayee());
                            }
                            if(null != sellerData.getReviewer() && !sellerData.getReviewer().equals("")){
                                xf.setFhr(sellerData.getReviewer());
                            }
                            if(sellerData.getEticketLim()>0d){
                                xf.setDzpzdje(sellerData.getEticketLim());
                                xf.setDzpfpje(sellerData.getEticketLim());
                            }
                            if(sellerData.getSpecialticketLim()>0d){
                                xf.setZpzdje(sellerData.getSpecialticketLim());
                                xf.setZpfpje(sellerData.getSpecialticketLim());
                            }
                            if(sellerData.getOrdinaryticketLim()>0d){
                                xf.setPpzdje(sellerData.getOrdinaryticketLim());
                                xf.setPpfpje(sellerData.getOrdinaryticketLim());
                            }
                        }
                        String res = initialCheckUtil.checkSeller(xf,"04",isupdate,isCrestv);
                        if(null != res && !res.equals("")){
                            resultMap.put("reCode","9999");
                            resultMap.put("reMessage",res);
                            return resultMap;
                        }else{
                            xfService.update(xf);
                            resultMap.put("reCode","0000");
                            resultMap.put("reMessage","销货方更新成功！");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    resultMap.put("reCode","9999");
                    if(type.equals("01")){
                        resultMap.put("reMessage","销货方新增失败！");
                    }else{
                        resultMap.put("reMessage","销货方更新失败！");
                    }
                    return resultMap;
                }

            }else{
                resultMap.put("reCode","9999");
                resultMap.put("reMessage","原销货方税号(yidentifier)不能为空！");
                return resultMap;
            }
        }


        return resultMap;
    }


    /**
     * 校验门店信息新增或更新数据方法。（供凯盈使用）
     *
     * @param gsxx
     * @param clientData
     * @return
     */
    public Map updateclientData(Gsxx gsxx, ClientData clientData) {
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
                //xfParm.setGsdm(gsxx.getGsdm());
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
                        result = initialCheckUtil.checkClient(skpvo,clientData.getType(),true);
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
                            resultMap.put("skph",yh.getDlyhid());
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
                        result = initialCheckUtil.checkClient(skpvo,clientData.getType(),true);
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
