package com.rjxx.taxeasey.service.dealorder;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasey.utils.ResponeseUtils;
import com.rjxx.taxeasey.utils.XmlMapUtils;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.PpJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
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
    private PpJpaDao ppJpaDao;

    @Autowired
    protected GsxxJpaDao gsxxJpaDao ;

    public String execute(Gsxx gsxx, String OrderData) {
        Map resultMap = new HashMap();
        String result = "";//处理返回后的结果信息
        //调用解析xml的公共方法。
        resultMap = dealCommonData(gsxx,OrderData);
        Xf xf = (Xf)resultMap.get("Xf");
        List<SkpVo> skpList = (List)resultMap.get("skpList");
        String issueType = (String)resultMap.get("issueType");
        //调用校验数据是否符合规则方法。
        result = checkCommData(xf,skpList,issueType);
        if(null != result && !result.equals("")){
            return ResponeseUtils.printCommDataResult("9999",result,"","","","");
        }else{
            //保存待处理数据。
            resultMap = saveXfAndSkp(xf,skpList,issueType);
            //保存失败
            if(null == resultMap || resultMap.isEmpty()){
                return ResponeseUtils.printCommDataResult("9999","保存信息出错","","","","");
            }else{
                String dlyhid = (String)resultMap.get("dlyhid");
                String yhmm = (String)resultMap.get("yhmm");
                String xfsh = (String)resultMap.get("xfsh");
                String xfmc = (String)resultMap.get("xfmc");
                return ResponeseUtils.printCommDataResult("0000","成功",xfsh,xfmc,dlyhid,yhmm);
            }
        }
        //return result;
    }

    /**
     *
     * @param OrderData
     * @return
     */
    public String execute2(String OrderData) {
        Map resultMap = new HashMap();
        String result = "";//处理返回后的结果信息
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(OrderData);
        }catch (Exception e){
            return ResponeseUtils.printResultToJson("9999","JSON数据格式有误！","","","","");
        }
        try {
            String sign = jsonObject.getString("sign");
            String appId = jsonObject.getString("appId");
            JSONObject data = jsonObject.getJSONObject("seller");
            Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
            if(null == gsxx){
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + ",公司信息没有维护","","","","");
            }
            String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
            if ("0".equals(check)) {
                return ResponeseUtils.printResultToJson("9999","9060:" + appId + "," + sign+"签名不通过","","","","");
            }else{
                //调用解析Json的公共方法。
                ObjectMapper mapper = new ObjectMapper();
                CommonData commonData=mapper.readValue(jsonObject.toJSONString(), CommonData.class);
                resultMap = dealCommonData2(gsxx, commonData);
                Xf xf = (Xf)resultMap.get("Xf");
                List<SkpVo> skpList = (List)resultMap.get("skpList");
                String issueType = (String)resultMap.get("issueType");
                //调用校验数据是否符合规则方法。
                result = checkCommData(xf,skpList,issueType);
                if(null != result && !result.equals("")){
                    return ResponeseUtils.printResultToJson("9999",result,"","","","");
                }else{
                    //保存待处理数据。
                    resultMap = saveXfAndSkp(xf,skpList,issueType);
                    //保存失败
                    if(null == resultMap || resultMap.isEmpty()){
                        return ResponeseUtils.printResultToJson("9999","保存信息出错","","","","");
                    }else{
                        String dlyhid = (String)resultMap.get("dlyhid");
                        String yhmm = (String)resultMap.get("yhmm");
                        String xfsh = (String)resultMap.get("xfsh");
                        String xfmc = (String)resultMap.get("xfmc");
                        return ResponeseUtils.printResultToJson("0000","成功",xfsh,xfmc,dlyhid,yhmm);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponeseUtils.printResultToJson("9999","公共信息初始化失败！","","","","");
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
    public Map saveXfAndSkp(Xf xf, List<SkpVo> skpVoList, String issueType){
        Map resultMap = new HashMap();
        Roles roles = rolesService.findDefaultOneByParams(null);
        Date lrsj = TimeUtil.getSysDate();
        if(null == roles || roles.equals("")){
            return resultMap;
        }else{
            Yh yh = new Yh();
            String dlyhid = xf.getGsdm()+"_"+xf.getXfsh().substring(xf.getXfsh().length()-5,xf.getXfsh().length()).toLowerCase();
            yh.setDlyhid(dlyhid);
            yh.setSjhm(null);
            yh.setYx(null);
            yh.setXb("1");
            yh.setYhmc("开票用户"+xf.getXfsh().substring(xf.getXfsh().length()-5,xf.getXfsh().length()).toLowerCase());
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
            try {
                yhService.save(yh);
                xf.setLrry(yh.getId());
                xf.setXgry(yh.getId());
                xfService.saveNew(xf);
                List<Skp> skpList = new ArrayList<>();
                for(int i =0;i<skpVoList.size();i++){
                    SkpVo skpvo = skpVoList.get(i);
                    int pid = 0;
                    if(null!=skpvo.getPpdm() && !skpvo.getPpdm().equals("")){
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

                skpJpaDao.save(skpList);
                if(issueType.equals("03")){
                    Cszb cszb = new Cszb();
                    Cszb tmp = cszbService.getSpbmbbh(xf.getGsdm(),null,null,"kpfs");
                    cszb.setCsid(tmp.getCsid());
                    cszb.setGsdm(xf.getGsdm());
                    cszb.setXfid(xf.getId());
                    cszb.setYxbz("1");
                    cszb.setLrry(yh.getId());
                    cszb.setXgry(yh.getId());
                    cszb.setLrsj(TimeUtil.getSysDate());
                    cszb.setXgsj(TimeUtil.getSysDate());
                    cszb.setCsz("03");
                    cszbService.save(cszb);
                }
                resultMap.put("dlyhid",yh.getDlyhid());
                resultMap.put("yhmm","12345678");
                resultMap.put("xfsh",xf.getXfsh());
                resultMap.put("xfmc",xf.getXfmc());
            }catch (Exception e){
                e.printStackTrace();
            }

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
    private Map dealCommonData2(Gsxx gsxx, CommonData commonData) {

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
     * 校验待保存数据是否全部符合规则。
     *
     * @param xf
     * @param list
     * @param issueType
     * @return
     */
    private String checkCommData(Xf xf,List<SkpVo> list,String issueType){

        List<Xf> xfList = xfService.findAllByParams(xf);
        String result = "";
        if(null == xf.getXfsh() || xf.getXfsh().equals("")){
            result +=  "Identifier销方税号不能为空;";
        }else if(!(xf.getXfsh().length() == 15 || xf.getXfsh().length() == 18 || xf.getXfsh().length() == 20 )){
            result +=  "Identifier销方税号"+xf.getXfsh()+"只能为15,18,20位;";
        }
        if(null == xf.getXfmc() || xf.getXfmc().equals("")){
            result +=  "Name销方名称不能为空;";
        }else if (xf.getXfmc().length() > 100) {
            result += "Name销方名称"+xf.getXfmc()+"过长;";
        }
        if(null == xf.getXfdz() || xf.getXfdz().equals("")){
            result +=  "Address销方地址不能为空;";
        }else if (xf.getXfdz().length() > 100) {
            result += "Address销方地址"+xf.getXfdz()+"过长;";
        }
        if(null == xf.getXfdh() || xf.getXfdh().equals("")){
            result +=  "TelephoneNo销方电话不能为空;";
        }else if (xf.getXfdh().length() > 20) {
            result += "TelephoneNo销方电话"+xf.getXfdh()+"过长;";
        }
        if(null == xf.getXfyh() || xf.getXfyh().equals("")){
            result +=  "Bank销方银行不能为空;";
        }else if (xf.getXfyh().length() > 100) {
            result += "Bank销方银行"+xf.getXfyh()+"过长;";
        }
        if(null == xf.getXfyhzh() || xf.getXfyhzh().equals("")){
            result +=  "BankAcc销方银行账号不能为空;";
        }else if (xf.getXfyhzh().length() > 30) {
            result += "BankAcc销方银行账号"+xf.getXfyhzh()+"过长;";
        }
        if(null == xf.getKpr() || xf.getKpr().equals("")){
            result +=  "Drawer开票人不能为空;";
        }else if (xf.getKpr().length() > 4) {
            result += "Drawer开票人"+xf.getKpr()+"过长;";
        }
        if(null != xfList && !xfList.isEmpty()){
            result +=  xf.getXfsh()+"销方已存在;";
        }
        if(null == issueType || issueType.equals("") ){
            result +=  "IssueType开票方式不能为空;";
        }else if(!issueType.equals("01") && !issueType.equals("03") && !issueType.equals("04")){
            result +=  "IssueType开票方式必须为01,03或04;";
        }

        if(null !=xf.getYbnsrqssj() && !xf.getYbnsrqssj().equals("")){
            if(xf.getYbnsrqssj().length()>6){
                result +=  "Ybnsrqssj一般纳税人起始时间必须为YYYYMM;";
            }
        }
        if(null !=xf.getYbnsrjyzs() && (!xf.getYbnsrjyzs().equals("2") && !xf.getYbnsrjyzs().equals("3") && !xf.getYbnsrjyzs().equals("4"))){
            result +=  "Ybnsrlx一般纳税人类型只能为（2，3，4）的一种;";
        }
        if(null == list || list.isEmpty()){
            result +=  xf.getXfsh()+"Clients开票点节点不能为空;";
        }else{
            SkpVo skp = list.get(0);
            String kplx = skp.getKplx();
            if(null == kplx ||kplx.equals("")){
                result +=  "开票限额必须填写一个;";
            }
            for(int i=0;i<list.size();i++){
                SkpVo skp2 = list.get(i);
                Map params = new HashMap();
                params.put("gsdm",skp2.getGsdm());
                params.put("kpddm",skp2.getKpddm());
                Skp skptmp = skpService.findOneByParams(params);
                if(null != skptmp && !skptmp.equals("")){
                    result +=  "ClientNO开票点代码"+skp2.getKpddm()+"已存在;";
                }
                if(null ==  skp2.getKpddm() || skp2.getKpddm().equals("")){
                    result +=  "ClientNO开票点代码不能为空;";
                }else if(skp2.getKpddm().length() >40){
                    result +=  "ClientNO开票点代码"+skp2.getKpddm()+"过长;";
                }
                if(null ==  skp2.getKpdmc() || skp2.getKpdmc().equals("")){
                    result +=  "Name开票点名称不能为空;";
                }else if(skp2.getKpdmc().length() >40){
                    result +=  "Name开票点名称"+skp2.getKpdmc()+"过长;";
                }
                if(null ==  skp2.getSbcs() || skp2.getSbcs().equals("")){
                    result +=  "TaxEquip税控设备厂商不能为空;";
                }else if(!(skp2.getSbcs().equals("1") || skp2.getSbcs().equals("2"))){
                    result +=  "TaxEquip税控设备厂商只能为1或2;";
                }
                if(null ==  skp2.getSkph() || skp2.getSkph().equals("")){
                    result +=  "EquipNum税控设备号不能为空;";
                }else if(skp2.getSkph().length() !=12){
                    result +=  "EquipNum税控设备号"+skp2.getSkph()+"只能为12位;";
                }
                if(null ==  skp2.getSkpmm() || skp2.getSkpmm().equals("")){
                    result +=  "TaxDiskPass税控盘密码不能为空;";
                }
                if(null !=skp2.getSbcs() && !skp2.getSbcs().equals("") && skp2.getSbcs().equals("1")
                        && (null == skp2.getZsmm() || skp2.getZsmm().equals(""))){
                    result +=  "当TaxEquip为1时,CertiCipher证书密码不能为空;";
                }
                if(null != skp2.getPpdm() && !skp2.getPpdm().equals("")){
                    List<Pp> pplist = ppJpaDao.findAllByPpdm(skp2.getPpdm(),skp2.getGsdm());
                    if(!pplist.isEmpty()){
                        result +=  "BrandCode品牌代码"+skp2.getPpdm()+"已存在";
                    }
                }
            }
        }

        return result;

    }
}
