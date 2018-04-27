package com.rjxx.taxeasey.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.jkpz.JkpzService;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.yjapi.Result;
import com.rjxx.utils.yjapi.ResultUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/3/26
 */
@RequestMapping("/kptService")
@RestController
public class AdapterPostController {
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private JkpzService jkpzService;

    @ApiOperation(value = "交易数据上传/开票/红冲")
    @RequestMapping(method = RequestMethod.POST)
    public Result post(@RequestBody String str) {
        HashMap<String, Object> jsonObject = null;
        try {
            jsonObject = JSON.parseObject(str,LinkedHashMap.class, Feature.OrderedField);
        }catch (Exception e){
            return ResultUtil.error("数据格式错误");
        }
        String sign = (String) jsonObject.get("sign");
        String appId = (String) jsonObject.get("appId");
        JSONObject data = (JSONObject) jsonObject.get("data");
        Gsxx gsxx = gsxxJpaDao.findOneByAppid(appId);
        if(gsxx==null){
            return ResultUtil.error("公司信息获取失败");
        }
        String check = RJCheckUtil.decodeXml(gsxx.getSecretKey(), JSON.toJSONString(data), sign);
        if ("0".equals(check)) {
            return ResultUtil.error("验签失败");
        }
        return jkpzService.jkpzInvoice(str);
    }


//    public static void main(String[] args) {
//        AdapterPost post = new AdapterPost();
//        AdapterData data = new AdapterData();
//        AdapterDataOrder order = new AdapterDataOrder();
//        AdapterDataSeller seller = new AdapterDataSeller();
//        AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
//        List<AdapterDataOrderDetails> details = new ArrayList<>();
//        List<AdapterDataOrderPayments> payments = new ArrayList<>();
//
//        //数据
//        data.setDrawer("王亚辉");
//        data.setVersion("19");
//        data.setInvType("12");
//        data.setSerialNumber("20180323103125X");
//        data.setOrder(order);
//        data.setSeller(seller);
//
//        //销方
//        seller.setName("上海百旺测试3643");
//        seller.setIdentifier("500102010003643");
//        seller.setAddress("销方地址");
//        seller.setTelephoneNo("110");
//        seller.setBank("销方银行");
//        seller.setBankAcc("123");
//
//        //订单
//        order.setBuyer(buyer);
//        order.setPayments(payments);
//        order.setOrderDetails(details);
//        order.setOrderNo(System.currentTimeMillis()+"");
//        order.setOrderDate(new Date());
//        order.setTotalAmount(10d);
//        order.setChargeTaxWay("0");//普通征收
//        order.setInvoiceList("0");//不打印清单
//        order.setInvoiceSplit("1");//拆票
//        order.setInvoiceSfdy("0");//不立即打印
//        order.setTaxMark("1");//金额含税
//        order.setRemark("这是备注");
//
//        //购方
//        buyer.setName("法国ankama信息技术有限公司");
//        buyer.setIdentifier("500102010003643");
//        buyer.setAddress("购方地址");
//        buyer.setTelephoneNo("120");
//        buyer.setBank("购方银行");
//        buyer.setBankAcc("321");
//        buyer.setCustomerType("1");
//        buyer.setEmail("243409312@qq.com");
//        buyer.setIsSend("1");
//
//        //明细
//        for (int i=2;i>0;i--){
//            AdapterDataOrderDetails detail = new AdapterDataOrderDetails();
//            detail.setAmount(5d);
//            detail.setMxTotalAmount(5d);
//            detail.setPolicyMark("0");
//            detail.setProductCode("3070401000000000000");
//            detail.setProductName("餐饮服务");
//            detail.setQuantity(1d);
//            detail.setUnitPrice(5d);
//            detail.setUtil("次");
//            detail.setRowType("0");
//            detail.setTaxRate(0.06);
//            details.add(detail);
//        }
//
////        //支付
////        AdapterDataOrderPayments payment = new AdapterDataOrderPayments();
////        payment.setPayCode("02");
////        payment.setPayPrice(5d);
////        payments.add(payment);
////
////        AdapterDataOrderPayments payment2 = new AdapterDataOrderPayments();
////        payment2.setPayCode("04");
////        payment2.setPayPrice(5d);
////        payments.add(payment2);
//
//        //请求
//        post.setAppId("RJ02b52a84dbf2");
//        post.setTaxNo("500102010003643");
//        String dataJson = JSON.toJSONString(data);
//        System.out.println("data="+dataJson);
//        String key = "7e7a0dea6654b77a13a9cf7af3e4caa9";
//        String sign = DigestUtils.md5Hex("data=" + dataJson + "&key=" + key);
//        System.out.println("sign="+sign);
//        post.setSign(sign);
//        post.setData(data);
//        post.setClientNo("KP001");
//        String postJson=JSON.toJSONString(post);
//        System.out.println("request="+postJson);
//
//        String url = "http://localhost:8080/kptService";
//        String result = HttpClientUtil.doPostJson(url, postJson);
//        System.out.println(result);
//    }
}
