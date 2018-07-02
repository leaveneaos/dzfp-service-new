package com.rjxx.taxeasey.utils;

import com.rjxx.taxeasy.dao.PpJpaDao;
import com.rjxx.taxeasy.domains.Pp;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.XfService;
import com.rjxx.taxeasy.vo.SkpVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InitialCheckUtil {


    @Autowired
    private XfService xfService;

    @Autowired
    private PpJpaDao ppJpaDao;

    @Autowired
    private SkpService skpService;
    /**
     * 校验待保存数据是否全部符合规则。
     *
     * @param xf
     * @param list
     * @param issueType
     * @return
     */
    public String checkCommData(Xf xf, List<SkpVo> list, String issueType,boolean isCrestv){

        String result ="";
        result=checkSeller(xf,issueType,false,isCrestv);
        if(null == list || list.isEmpty()){
            //result +=  xf.getXfsh()+"Clients开票点节点不能为空;"; //允许只做销方的初始化。
        }else{
            SkpVo skp = list.get(0);
            String kplx = skp.getKplx();
            if(null == kplx ||kplx.equals("")){
                result +=  "开票限额必须填写一个;";
            }
            if(list.size()>1){
                result +=  "目前一次请求只允许传入一个门店信息;";
            }
            for(int i=0;i<list.size();i++){
                SkpVo skp2 = list.get(i);
                result += checkClient(skp2,"01",isCrestv);
            }
        }

        return result;

    }

    /**
     * 校验门店信息
     * @param skp2
     * @param type
     * @return
     */
    public String checkClient(SkpVo skp2,String type,boolean isCrestv){
        String result = "";
        Map params = new HashMap();
        params.put("gsdm",skp2.getGsdm());
        if(isCrestv){
            params.put("skph",skp2.getSkph());
            params.put("xfid",skp2.getXfid());
        }else{
            params.put("kpddm",skp2.getKpddm());
        }
        Skp skptmp = skpService.findOneByParams(params);
        if(type.equals("02") && null == skptmp){
            result +=  "需要修改的税控盘号"+skp2.getSkph()+"对应的门店不存在！";
        }
        if(null != skptmp && !skptmp.equals("") && type.equals("01")){
            result +=  "ClientNO开票点代码"+skp2.getKpddm()+"已存在;";
        }
        if(null ==  skp2.getKpddm() || skp2.getKpddm().equals("")){
            result +=  "ClientNO开票点代码不能为空;";
        }else if(skp2.getKpddm().length() >40){
            result +=  "ClientNO开票点代码"+skp2.getKpddm()+"过长;";
        }
        if(null ==  skp2.getKpdmc() || skp2.getKpdmc().equals("")&& type.equals("01")){
            result +=  "Name开票点名称不能为空;";
        }else if(skp2.getKpdmc().length() >40){
            result +=  "Name开票点名称"+skp2.getKpdmc()+"过长;";
        }
        if((null ==  skp2.getSbcs() || skp2.getSbcs().equals(""))&& type.equals("01")){
            result +=  "TaxEquip税控设备厂商不能为空;";
        }else if(!(skp2.getSbcs().equals("1") || skp2.getSbcs().equals("2"))&& type.equals("01")){
            result +=  "TaxEquip税控设备厂商只能为1或2;";
        }
        if((null ==  skp2.getSkph() || skp2.getSkph().equals(""))){
            result +=  "EquipNum税控设备号不能为空;";
        }else if(skp2.getSkph().length() !=12 && type.equals("01")){
            result +=  "EquipNum税控设备号"+skp2.getSkph()+"只能为12位;";
        }
        if((null ==  skp2.getSkpmm() || skp2.getSkpmm().equals(""))&& type.equals("01")){
            result +=  "TaxDiskPass税控盘密码不能为空;";
        }
        if((null !=skp2.getSbcs() && !skp2.getSbcs().equals("") && skp2.getSbcs().equals("1")
                && (null == skp2.getZsmm() || skp2.getZsmm().equals("")))&& type.equals("01")){
            result +=  "当TaxEquip为1时,CertiCipher证书密码不能为空;";
        }
        if(null != skp2.getPpdm() && !skp2.getPpdm().equals("")&& type.equals("01")){
            List<Pp> pplist = ppJpaDao.findAllByPpdm(skp2.getPpdm(),skp2.getGsdm());
            if(!pplist.isEmpty()){
                result +=  "BrandCode品牌代码"+skp2.getPpdm()+"已存在";
            }
        }
        if(null != skp2.getPpdm() && !skp2.getPpdm().equals("")&& type.equals("02")){
            List<Pp> pplist = ppJpaDao.findAllByPpdm(skp2.getPpdm(),skp2.getGsdm());
            if(pplist.isEmpty()){
                result +=  "该BrandCode品牌代码"+skp2.getPpdm()+"不存在，无法修改";
            }else{
                skp2.setPid(pplist.get(0).getId());
            }
        }
        return result;
    }

    /**
     * 销方信息校验方法
     * @param xf
     * @param issueType
     * @param isupdate 是否更新销方
     * @param isCrestv
     * @return
     */
    public String checkSeller(Xf xf,String issueType,boolean isupdate,boolean isCrestv){

        String result = "";
        if(null == xf.getXfsh() || xf.getXfsh().equals("")){
            result +=  "Identifier销方税号不能为空;";
        }else if(!(xf.getXfsh().length() == 15 || xf.getXfsh().length() == 17 || xf.getXfsh().length() == 18 || xf.getXfsh().length() == 20 )){
            result +=  "Identifier销方税号"+xf.getXfsh()+"只能为15,17,18,20位;";
        }
        if(null == xf.getXfmc() || xf.getXfmc().equals("")){
            result +=  "Name销方名称不能为空;";
        }else if (xf.getXfmc().length() > 100) {
            result += "Name销方名称"+xf.getXfmc()+"过长;";
        }
        if((null == xf.getXfdz() || xf.getXfdz().equals(""))&& !isupdate){
            result +=  "Address销方地址不能为空;";
        }else if (xf.getXfdz().length() > 100) {
            result += "Address销方地址"+xf.getXfdz()+"过长;";
        }
        if((null == xf.getXfdh() || xf.getXfdh().equals("")) && !isupdate){
            result +=  "TelephoneNo销方电话不能为空;";
        }else if (xf.getXfdh().length() > 20) {
            result += "TelephoneNo销方电话"+xf.getXfdh()+"过长;";
        }
        if((null == xf.getXfyh() || xf.getXfyh().equals("")) && !isupdate){
            result +=  "Bank销方银行不能为空;";
        }else if (xf.getXfyh().length() > 100) {
            result += "Bank销方银行"+xf.getXfyh()+"过长;";
        }
        if((null == xf.getXfyhzh() || xf.getXfyhzh().equals("")) && !isupdate){
            result +=  "BankAcc销方银行账号不能为空;";
        }else if (xf.getXfyhzh().length() > 30) {
            result += "BankAcc销方银行账号"+xf.getXfyhzh()+"过长;";
        }
        if((null == xf.getKpr() || xf.getKpr().equals("")) && !isupdate ){
            result +=  "Drawer开票人不能为空;";
        }else if (xf.getKpr().length() > 4) {
            result += "Drawer开票人"+xf.getKpr()+"过长;";
        }
        if(!isupdate){
            if(isCrestv){
                Xf xf1  = xfService.findOneByParams(xf);
                if(null != xf1){
                    result +=  xf.getXfsh()+"销方已存在;";
                }
            }else{
                Xf xfParam = new Xf();
                xfParam.setXfsh(xf.getXfsh());
                Xf xf1  = xfService.findOneByParams(xfParam);
                if(null != xf1){
                    result +=  xf.getXfsh()+"销方已存在;";
                }
            }


        }
        if((null == issueType || issueType.equals("")) && !isupdate){
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
        return result;
    }
}
