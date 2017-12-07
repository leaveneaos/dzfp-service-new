package com.rjxx.taxease.service.dealorder;


import com.rjxx.taxease.utils.ResponeseUtils;
import com.rjxx.taxease.utils.XmlMapUtils;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.time.TimeUtil;
import com.rjxx.utils.PasswordUtils;
import org.apache.axiom.om.OMElement;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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

    public String execute(Gsxx gsxx, String OrderData) {
        Map resultMap = new HashMap();
        String result = "";//处理返回后的结果信息
        //调用解析xml的公共方法。
        resultMap = dealCommonData(gsxx,OrderData);
        Xf xf = (Xf)resultMap.get("Xf");
        List<Skp> skpList = (List)resultMap.get("skpList");
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

    public Map saveXfAndSkp(Xf xf, List<Skp> skpList, String issueType){
        Map resultMap = new HashMap();
        Roles roles = rolesService.findDefaultOneByParams(null);
        if(null == roles || roles.equals("")){
            return resultMap;
        }else{
            Yh yh = new Yh();
            String dlyhid = xf.getGsdm()+"_"+xf.getXfsh().substring(xf.getXfsh().length()-5,xf.getXfsh().length()).toLowerCase();
            yh.setDlyhid(dlyhid);
            yh.setSjhm(null);
            yh.setYx(null);
            yh.setXb("1");
            yh.setYhmc("开票用户");
            yh.setGsdm(xf.getGsdm());
            yh.setYhmm(PasswordUtils.encrypt("12345678"));
            yh.setRoleids(roles.getId().toString());
            yh.setSjhm(null);
            yh.setYx(null);
            yh.setYxbz("1");
            yh.setLrry(1);
            yh.setXgry(1);
            yh.setLrsj(TimeUtil.getSysDate());
            yh.setXgsj(TimeUtil.getSysDate());
            try {
                yhService.save(yh);
                xf.setLrry(yh.getId());
                xf.setXgry(yh.getId());
                xfService.saveNew(xf);
                for(int i =0;i<skpList.size();i++){
                    Skp skp = skpList.get(i);
                    skp.setXfid(xf.getId());
                    skp.setLrry(yh.getId());
                    skp.setXgry(yh.getId());
                    skp.setLrsj(TimeUtil.getSysDate());
                    skp.setXgsj(TimeUtil.getSysDate());
                    skp.setGsdm(xf.getGsdm());
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
        List<Skp> skpList = new ArrayList<Skp>();
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
                Skp skp = new Skp();
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
                //skp.setXfid(xfid);保存时需要更新进去
                skp.setKpddm(clientNO);
                skp.setKpdmc(clientName);
                skp.setSkph(equipNum);
                skp.setSbcs(taxEquip);
                skp.setLxdz(sellerAddress);
                skp.setSkpmm(taxDiskPass);
                skp.setZsmm(certiCipher);
                skp.setLxdh(sellerTel);
                skp.setKhyh(sellerBank);
                skp.setYhzh(sellerBankAcc);
                skp.setSkr(payee);
                skp.setFhr(reviewer);
                skp.setKpr(drawer);
                skp.setDpmax(eticketLim.equals("")?null:Double.valueOf(eticketLim));
                skp.setFpfz(eticketLim.equals("")?null:Double.valueOf(eticketLim));
                skp.setZpmax(specialticketLim.equals("")?null:Double.valueOf(specialticketLim));
                skp.setZpfz(specialticketLim.equals("")?null:Double.valueOf(specialticketLim));
                skp.setPpmax(ordinaryticketLim.equals("")?null:Double.valueOf(ordinaryticketLim));
                skp.setPpfz(ordinaryticketLim.equals("")?null:Double.valueOf(ordinaryticketLim));
                skp.setLrry(1);
                skp.setLrsj(new Date());
                skp.setXgry(1);
                skp.setXgsj(new Date());
                skp.setYxbz("1");
                String fplx = "";
                if(null !=skp.getDpmax()&& !skp.getDpmax().equals("")){
                    fplx = "12";
                }
                if(null !=skp.getZpmax()&& !skp.getZpmax().equals("")) {
                    fplx = fplx.equals("")?"01":",01";
                }
                if(null !=skp.getPpmax()&& !skp.getPpmax().equals("")) {
                    fplx = fplx.equals("")?"02":",02";
                }
                skp.setKplx(fplx.equals("")?null:fplx);
                skp.setWrzs("1");//无人值守 ：默认1
                skpList.add(skp);
            }
        }
        resultMap.put("Xf",xfBo);
        resultMap.put("skpList",skpList);
        resultMap.put("issueType",issueType);
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
    private String checkCommData(Xf xf,List<Skp> list,String issueType){

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
            result +=  "IssueType开票方式不能为空";
        }else if(!issueType.equals("01") && !issueType.equals("03")){
            result +=  "IssueType开票方式必须为01或03;";
        }
        if(null == list || list.isEmpty()){
            result +=  xf.getXfsh()+"Clients开票点节点不能为空;";
        }else{
            Skp skp = list.get(0);
            String kplx = skp.getKplx();
            if(null == kplx ||kplx.equals("")){
                result +=  "开票限额必须填写一个;";
            }
            for(int i=0;i<list.size();i++){
                Skp skp2 = list.get(i);
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
            }
        }

        return result;

    }
}
