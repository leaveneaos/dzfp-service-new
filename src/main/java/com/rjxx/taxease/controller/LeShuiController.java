package com.rjxx.taxease.controller;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.dao.leshui.JxdyjlJpaDao;
import com.rjxx.taxeasy.dao.leshui.JxfpxxJpaDao;
import com.rjxx.taxeasy.dao.leshui.JxhdjlJpaDao;
import com.rjxx.taxeasy.dao.leshui.JxywjlJpaDao;
import com.rjxx.taxeasy.domains.leshui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyahui on 2018/1/9 0009.
 */
@RestController
@RequestMapping("/leshui")
public class LeShuiController {

    private final static String INVOICE_INFO_SUCCESS = "00";
    private final static String INVOICE_QUERY_SUCCESS = "000";

    @Autowired
    private JxywjlJpaDao jxywjlJpaDao;
    @Autowired
    private JxdyjlJpaDao jxdyjlJpaDao;
    @Autowired
    private JxfpxxJpaDao jxfpxxJpaDao;
    @Autowired
    private JxhdjlJpaDao jxhdjlJpaDao;
    /**
     * 发票查询结果推送
     */
    @RequestMapping(value = "/svatBatchInvoices", method = RequestMethod.POST)
    public String svatBatchInvoices(@RequestBody LSSvatBatchInvoices lsSvatBatchInvoices) {
        try {
            SvatBody body = lsSvatBatchInvoices.getBody();
            SvatHead head = lsSvatBatchInvoices.getHead();
            SvatInvoices[] invoices = body.getInvoices();
            String taxCode = head.getTaxCode();
            String uniqueId = head.getUniqueId();
            Integer totalSum = head.getTotalSum();

            //创建业务记录对象
            Jxywjl jxywjl = new Jxywjl();
            jxywjl.setYwlx(4);//1.单张发票查询 2.批量查询 3.发票认证 4.发票查询回调 5.认证结果回调
            jxywjl.setZt("0000");
            Jxywjl saveJxywjl = jxywjlJpaDao.save(jxywjl);

            //创建调用记录对象
            Jxdyjl jxdyjl = new Jxdyjl();
            jxdyjl.setDyxh(1);
            jxdyjl.setYwid(saveJxywjl.getId());
            Jxdyjl saveJxdyjl = jxdyjlJpaDao.save(jxdyjl);

            //创建回调记录对象
            Jxhdjl jxhdjl = new Jxhdjl();
            jxhdjl.setTotalsum(totalSum);
            jxhdjl.setTaxcode(taxCode);
            jxhdjl.setUniqueid(uniqueId);
            jxhdjl.setDyid(saveJxdyjl.getId());

            for(SvatInvoices fp:invoices){
                String fpdm = fp.getInvoiceCode();
                String fphm = fp.getInvoiceNo();
                Date authTime = fp.getAuthTime();
                String authType = fp.getAuthType();
                String invoicesStatus = fp.getInvoicesStatus();
                String isAuth = fp.getIsAuth();
                Jxfpxx oldJxfpxx = jxfpxxJpaDao.findByFpdmAndFphm(fpdm, fphm);
                oldJxfpxx.setUniqueid(uniqueId);
                oldJxfpxx.setGfsh(taxCode);
                oldJxfpxx.setXgsj(new Date());
                oldJxfpxx.setRzsj(authTime);//*可能会变
                oldJxfpxx.setRzlx(authType);//*可能会变
                oldJxfpxx.setRzbz(isAuth);//*可能会变
                oldJxfpxx.setFpzt(invoicesStatus);//*可能会变
                jxhdjl.setRzsj(authTime);
                jxhdjl.setRzlx(authType);
                jxhdjl.setRzbz(isAuth);
                jxhdjl.setFpzt(invoicesStatus);
                jxhdjl.setFplsh(oldJxfpxx.getFplsh());
                jxfpxxJpaDao.save(oldJxfpxx);
                jxhdjlJpaDao.save(jxhdjl);
            }
            return success();
        } catch (Exception e) {
            return error("格式错误");
        }
    }


    /**
     * 发票认证结果反馈
     */
    @RequestMapping(value = "/authorizeSvatResultBatchInvoice", method = RequestMethod.POST)
    public String authorizeSvatResultBatchInvoice(@RequestBody LSAuthorizeSvatResultBatchInvoice lsAuthorizeSvatResultBatchInvoice) {
        try {
            AuthBody[] body = lsAuthorizeSvatResultBatchInvoice.getBody();
            AuthHead head = lsAuthorizeSvatResultBatchInvoice.getHead();
            String batchId = head.getBatchId();
            String taxCode = head.getTaxCode();
            String uniqueId = head.getUniqueId();

            //创建业务记录对象
            Jxywjl jxywjl = new Jxywjl();
            jxywjl.setYwlx(5);//1.单张发票查询 2.批量查询 3.发票认证 4.发票查询回调 5.认证结果回调
            jxywjl.setZt("0000");
            Jxywjl saveJxywjl = jxywjlJpaDao.save(jxywjl);

            //创建调用记录对象
            Jxdyjl jxdyjl = new Jxdyjl();
            jxdyjl.setDyxh(1);
            jxdyjl.setYwid(saveJxywjl.getId());
            Jxdyjl saveJxdyjl = jxdyjlJpaDao.save(jxdyjl);

            //创建回调记录对象
            Jxhdjl jxhdjl = new Jxhdjl();
            jxhdjl.setBatchid(batchId);
            jxhdjl.setTaxcode(taxCode);
            jxhdjl.setUniqueid(uniqueId);
            jxhdjl.setDyid(saveJxdyjl.getId());

            for(AuthBody authBody:body){
                String invoiceNo = authBody.getInvoiceNo();
                String invoiceCode = authBody.getInvoiceCode();
                Date authorizeTime = authBody.getAuthorizeTime();
                String message = authBody.getMessage();
                String status = authBody.getStatus();
                Jxfpxx oldJxfpxx = jxfpxxJpaDao.findByFpdmAndFphm(invoiceCode, invoiceNo);
                oldJxfpxx.setXgsj(new Date());
                oldJxfpxx.setBatchid(batchId);
                oldJxfpxx.setUniqueid(uniqueId);
                oldJxfpxx.setRzsj(authorizeTime);//*可能会变
                if("0".equals(status)){
                    oldJxfpxx.setRzbz("Y");
                    jxhdjl.setRzbz("Y");

                }else{
                    oldJxfpxx.setRzbz("N");
                    jxhdjl.setRzbz("N");
                }
                oldJxfpxx.setRzzt(status);//0成功 1失败 2已发送乐税
                jxhdjl.setFplsh(oldJxfpxx.getFplsh());
                jxhdjl.setRzsj(authorizeTime);
                jxhdjl.setRzmsg(message);
                jxfpxxJpaDao.save(oldJxfpxx);
                jxhdjlJpaDao.save(jxhdjl);
            }
            return success();
        } catch (Exception e) {
            return error("格式错误");
        }
    }

//    /**
//     * i1 发票查验
//     *
//     * @invoiceCode 发票代码（长度10位或者12位）
//     * @invoiceNumber 发票号码（长度8位）
//     * @billTime 开票时间（时间格式必须为：2017-05-11，不支持其他格式）
//     * @checkCode 校验码（检验码后六位，增值税专用发票，增值税机动车发票可以不传）
//     * @invoiceAmount 开具金额、不含税价（增值税普通发票，增值税电子发票可以不传）
//     */
//    @RequestMapping(value = "/invoiceInfo/{gsdm}", method = RequestMethod.POST)
//    public String invoiceInfo(@PathVariable String gsdm, @RequestParam String data) {
//        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
//        String key = gsxx.getSecretKey();
//        String decode;
//        try {
//            decode = DesUtils.DESDecrypt(data, key);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return error("秘钥错误");
//        }
//        JSONObject jsonObject = JSON.parseObject(decode);
//        String invoiceCode = jsonObject.getString("invoiceCode");
//        String invoiceNumber = jsonObject.getString("invoiceNumber");
//        String billTime = jsonObject.getString("billTime");
//        String checkCode = jsonObject.getString("checkCode");
//        String invoiceAmount = jsonObject.getString("invoiceAmount");
//        String result = LeShuiUtil.invoiceInfoForCom(invoiceCode, invoiceNumber, billTime, checkCode, invoiceAmount);
//        JSONObject resultJson = JSON.parseObject(result);
//        String rtnCode = resultJson.get("RtnCode ").toString();
//        if(INVOICE_INFO_SUCCESS.equals(rtnCode)){
//            String invoiceResult = resultJson.get("invoiceResult").toString();
//            if(invoiceResult!=null){
//                String encode;
//                try {
//                    encode = DesUtils.DESEncrypt(invoiceResult, key);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return error("发生未知错误，请联系管理员");
//                }
//                return success(encode);
//            }else{
//                return result;
//            }
//        }else{
//            return result;
//        }
//    }

//    /**
//     * i2 单张发票查询
//     *
//     * @uniqueId 唯一编码（20位），客户系统生成，规则："QBI"+"yyyyMMddhhmmss+001"
//     * @invoiceCode 发票代码
//     * @invoiceNo 发票号码
//     * @taxCode 纳税人识别号(一般为购方纳税人识别号，即客户系统公司纳税人识别号)
//     */
//    @RequestMapping(value = "/invoiceQuery/{gsdm}", method = RequestMethod.POST)
//    public String invoiceQuery(@PathVariable String gsdm, @RequestParam String data) {
//        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
//        String key = gsxx.getSecretKey();
//        String decode;
//        try {
//            decode = DesUtils.DESDecrypt(data, key);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return error("秘钥错误");
//        }
//        JSONObject jsonObject = JSON.parseObject(decode);
//        String uniqueId = jsonObject.getString("uniqueId");
//        String invoiceCode = jsonObject.getString("invoiceCode");
//        String invoiceNo = jsonObject.getString("invoiceNo");
//        String taxCode = jsonObject.getString("taxCode");
//        //获取结果
//        String result = LeShuiUtil.invoiceQuery(uniqueId, invoiceCode, invoiceNo, taxCode);
//        JSONObject resultJson = JSON.parseObject(result);
//        JSONObject head = resultJson.getJSONObject("head ");
//        String rtnCode = head.getString("rtnCode");
//        if(INVOICE_QUERY_SUCCESS.equals(rtnCode)){
//            String body=resultJson.get("body").toString();
//            String encode;
//            try {
//                encode = DesUtils.DESEncrypt(body, key);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return error("发生未知错误，请联系管理员");
//            }
//            return success(encode);
//        }else{
//            return result;
//        }
//    }
//
//    /**
//     * i3 发票信息批量查询
//     *
//     * @uniqueId 唯一编码（20位），客户系统生成，规则："QBI"+"yyyyMMddhhmmss+001"
//     * @startTime 开始时间
//     * @endTime 结束时间
//     * @taxCode 纳税人识别号(一般为购方纳税人识别号，即客户系统公司纳税人识别号)
//     * @pageNo 第几页
//     */
//    @RequestMapping(value = "/invoiceBatchQuery/{gsdm}", method = RequestMethod.POST)
//    public String invoiceBatchQuery(@PathVariable String gsdm, @RequestParam String data) {
//        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
//        String key = gsxx.getSecretKey();
//        String decode;
//        try {
//            decode = DesUtils.DESDecrypt(data, key);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return error("秘钥错误");
//        }
//        JSONObject jsonObject = JSON.parseObject(decode);
//        String uniqueId = jsonObject.getString("uniqueId");
//        String startTime = jsonObject.getString("startTime");
//        String endTime = jsonObject.getString("endTime");
//        String taxCode = jsonObject.getString("taxCode");
//        String pageNo = jsonObject.getString("pageNo");
//        String result = LeShuiUtil.invoiceBatchQuery(uniqueId, startTime, endTime, taxCode, pageNo);
//        JSONObject resultJson = JSON.parseObject(result);
//        JSONObject head = resultJson.getJSONObject("head ");
//        String rtnCode = head.getString("rtnCode");
//        if(INVOICE_QUERY_SUCCESS.equals(rtnCode)){
//            String body=resultJson.get("body").toString();
//            String encode;
//            try {
//                encode = DesUtils.DESEncrypt(body, key);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return error("发生未知错误，请联系管理员");
//            }
//            return success(encode);
//        }else{
//            return result;
//        }
//    }
//
//    /**
//     * i4 发票认证
//     *
//     * @batchId 唯一编码（20位），客户系统生成，规则："QBI"+"yyyyMMddhhmmss+001"
//     * @taxCode 纳税人识别号(一般为购方纳税人识别号，即客户系统公司纳税人识别号)
//     * @body 需要认证的发票信息 invoiceCode&invoiceNo
//     */
//    @RequestMapping(value = "/invoiceAuthorize/{gsdm}", method = RequestMethod.POST)
//    public String invoiceAuthorize(@PathVariable String gsdm, @RequestParam String data) {
//        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
//        String key = gsxx.getSecretKey();
//        String decode;
//        try {
//            decode = DesUtils.DESDecrypt(data, key);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return error("秘钥错误");
//        }
//        JSONObject jsonObject = JSON.parseObject(decode);
//        String batchId = jsonObject.getString("batchId");
//        String taxCode = jsonObject.getString("taxCode");
//        JSONArray body = jsonObject.getJSONArray("body");
//        String result = LeShuiUtil.invoiceAuthorize(batchId, taxCode, body);
//        JSONObject resultJson = JSON.parseObject(result);
//        JSONObject head = resultJson.getJSONObject("head ");
//        String rtnCode = head.getString("rtnCode");
//        if(INVOICE_QUERY_SUCCESS.equals(rtnCode)){
//            String resultbody=resultJson.get("body").toString();
//            String encode;
//            try {
//                encode = DesUtils.DESEncrypt(resultbody, key);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return error("发生未知错误，请联系管理员");
//            }
//            return success(encode);
//        }else{
//            return result;
//        }
//    }


    /**
     * 返回值方法
     */
    private String success() {
        return success(null);
    }

    private String success(String data) {
        Map map = new HashMap<>();
        map.put("rtnMsg", "本次请求成功");
        map.put("rtnCode", "000");
        map.put("rtnData", data);
        Map param = new HashMap();
        param.put("head", map);
        return JSON.toJSONString(param);
    }

    private String error(String rtnMsg) {
        Map map = new HashMap<>();
        map.put("rtnMsg", rtnMsg);
        map.put("rtnCode", "9999");
        Map param = new HashMap();
        param.put("head", map);
        return JSON.toJSONString(param);
    }
}
