package com.rjxx.taxease.service;

import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Jyxx;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JyxxService;
import com.rjxx.utils.ResponseUtils;
import com.rjxx.utils.weixin.HttpClientUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class HanderJysjxxService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private  GsxxService gsxxservice;

	@Autowired
	private  JyxxService jyxxservice;

	/**
	 * 处理交易数据上传
	 */
	@Transactional
	public  String uploadOrder(final String AppId, final String Sign, final String OrderData){
		logger.debug("appid={}",AppId);
		logger.debug("sign={}",Sign);
		logger.debug("orderdata={}",OrderData);
		final Map resultMap = new HashMap();
		try {
			String result = handerMessage(AppId, Sign, OrderData);
			return result;
			//resultMap.put("result", result);
		} catch (Exception e) {
			e.printStackTrace();
			String result = ResponseUtils.printFailure("9999:" + e.getMessage());
			//resultMap.put("result", result);
			throw new RuntimeException(result);
			//TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		//String result = (String) resultMap.get("result");
		//return result;
	}

	// 处理上传的交易信息 appkey == AppId secret == key
	@Transactional
	public  String handerMessage(String AppId, String Sign, String OrderData){
		String result = "";
		Map tempMap = new HashMap();
		tempMap.put("appkey", AppId);
		Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
		if (gsxxBean == null) {
			return ResponseUtils.printFailure1("9060:" + AppId + "," + Sign);
		} else {
			// int xfid = gsxxBean.get("yhjg");
			// XfBean xfBean = XfBean.dao.findFirst("select * from t_xf where
			// xfid = " + xfid);
			// 校验数据是否被篡改过
			String key = gsxxBean.getSecretKey();
			String signSourceData = "data=" + OrderData + "&key=" + key;
			String newSign = DigestUtils.md5Hex(signSourceData);
//			if (!Sign.equals(newSign)) {
//				result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9060:签名不通过</ReturnMessage> \n</Responese>";
//				return result;
//			}
			String gsdm = gsxxBean.getGsdm();
			OMElement root = null;
			try {
				root = xml2OMElement(OrderData);
			} catch (Exception e) {
				e.printStackTrace();
				result = e.getMessage();
				return result;
			}
			List rootList = xml2List(root, "Row");// 获取xml中的row标签下的数据
			Map jyxxMap = null;
			for (int i = 0; i < rootList.size(); i++) {
				String orderNo = "";
				String orderTime = "";
				Double price = -9999.0; // price = Double.valueOf("0");
				String sign = "";
				String storeNo = "";
				jyxxMap = (Map) rootList.get(i);
				// 分装对应的bean
				orderNo = (String) jyxxMap.get("OrderNo");// 订单号 必选
				orderTime = (String) jyxxMap.get("OrderTime");// 订单日期 必选
				storeNo = (String) jyxxMap.get("StoreNo"); // 门店编号 必选
				if ((String) jyxxMap.get("Price") == null || jyxxMap.get("Price").equals("")) {

				} else {
					price = Double.valueOf((String) jyxxMap.get("Price")); // 金额
																			// 必选
				}
				sign = (String) jyxxMap.get("Sign");// 签名串 必选
				if (orderNo == null || orderNo.trim().equals("")) {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9104:第" + (i + 1)
							+ "行订单号不能为空</ReturnMessage> \n</Responese>";
					return result;
				} else if ((orderTime == null || orderTime.trim().equals(""))) {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9115:第" + (i + 1)
							+ "行订单时间不能为空</ReturnMessage> \n</Responese>";
					return result;
				} else if (price == -9999.0) {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9105:第" + (i + 1)
							+ "行金额不能为空</ReturnMessage> \n</Responese>";
					return result;
				} else if (sign == null || sign.equals("")) {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9116:第" + (i + 1)
							+ "行签名串不能为空</ReturnMessage> \n</Responese>";
					return result;
				} else if (storeNo == null || storeNo.equals("")) {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9117:第" + (i + 1)
							+ "行门店号不能为空</ReturnMessage> \n</Responese>";
					return result;
				}
			}
			for (int i = 0; i < rootList.size(); i++) {
				String orderNo = "";
				String orderTime = "";
				Double price = -9999.0; // price = Double.valueOf("0");
				String sign = "";
				String storeNo = "";
				jyxxMap = (Map) rootList.get(i);
				// 分装对应的bean
				orderNo = (String) jyxxMap.get("OrderNo");// 订单号 必选
				orderTime = (String) jyxxMap.get("OrderTime");// 订单日期 必选
				storeNo = (String) jyxxMap.get("StoreNo"); // 门店编号 必选
				if ((String) jyxxMap.get("Price") == null || jyxxMap.get("Price").equals("")) {

				} else {
					price = Double.valueOf((String) jyxxMap.get("Price")); // 金额
																			// 必选
				}
				sign = (String) jyxxMap.get("Sign");
				try {
					Jyxx jyxx = new Jyxx(); // 主表
					Map params = new HashMap();
					params.put("gsdm", gsdm);
					params.put("tqm", orderNo);
					jyxx = jyxxservice.findOneByParams(params);
					if (null != jyxx) {
						Map params2 = new HashMap();
						params2.put("gsdm", gsdm);
						params2.put("order_no", orderNo);
						params2.put("store_no", storeNo);
						params2.put("order_time", orderTime);
						params2.put("price", price);
						params2.put("sign", sign);
						params2.put("xgsj", new Date());
						jyxxservice.update(params2);
					} else {
						jyxx = new Jyxx(); // 主表
						jyxx.setOrderNo(orderNo);// 订单号
						jyxx.setOrderTime(orderTime);// 订单时间
						jyxx.setPrice(price);// 金额
						jyxx.setSign(sign);// 签名串
						jyxx.setGsdm(gsdm);// 公司代码
						jyxx.setLrry(gsdm);// 录入人员
						jyxx.setLrsj(new Date());// 录入时间
						jyxx.setXgry(gsdm);// 修改人员
						jyxx.setXgsj(new Date());// 修改
						jyxx.setYxbz("1");// 有效标志 1有效 0 无效
						jyxx.setStoreNo(storeNo);
						jyxxservice.save(jyxx);
					}
				} catch (Exception e) {
					e.printStackTrace();
					result = "交易数据上传失败："+e.getMessage();
					//TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
					//return result;
					throw new RuntimeException(result);
					
				}
			}
			result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>交易数据上传成功</ReturnMessage> \n</Responese>";
		}
		return result;
	}

	private static OMElement xml2OMElement(String xml) throws XMLStreamException, UnsupportedEncodingException {
		ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
		StAXBuilder builder = new StAXOMBuilder(xmlStream);
		OMElement documentElement = builder.getDocumentElement();
		return documentElement;
	}

	private static Map xml2Map(OMElement doc, String listTagNames) {
		Map docMap = new HashMap();
		Iterator<OMElement> iter = doc.getChildElements();
		OMElement node;
		String tagName, tagText;
		while (iter.hasNext()) {
			node = iter.next();
			tagName = str2Trim(node.getLocalName());
			if (listTagNames.indexOf(tagName) > -1) {
				docMap.put(tagName, xml2List(node, listTagNames));
			} else if (node.getChildElements().hasNext()) {
				docMap.put(tagName, xml2Map(node, listTagNames));
			} else {
				tagText = str2Trim(node.getText());
				docMap.put(tagName, tagText);
			}
		}
		return docMap;
	}

	private static List xml2List(OMElement doc, String listTagNames) {
		List list = new ArrayList();
		Iterator<OMElement> iter = doc.getChildElements();
		OMElement node;
		String tagText;
		while (iter.hasNext()) {
			node = iter.next();
			if (node.getChildElements().hasNext()) {
				list.add(xml2Map(node, listTagNames));
			} else {
				tagText = str2Trim(node.getText());
				list.add(tagText);
			}
		}
		return list;
	}

	private static String str2Trim(String str) {
		return "".equals(str) ? null : str.trim();
	}

	public static String printFailure(String errorMessage) {
		return "<Responese>\n  <ReturnCode>9999</ReturnCode>\n" + "  <ReturnMessage>" + errorMessage
				+ "</ReturnMessage>\n</Responese>";
	}

	public static void main(String[] args) {
		String OrderData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Request>\n" +
				"<Row> \n"+
				"  <OrderNo>20171025yidu"+new Random().nextInt(999)+"</OrderNo>\n" +
				"  <!--OrderNo必选，订单号，String20-->\n" +
				"  <OrderTime>20161110093912</OrderTime>\n" +
				"  <!--OrderTime必选，订单时间，String14-->\n" +
				"  <Price>"+new Random().nextInt(800)+"</Price>\n" +
				"  <!--Price必选，金额，Double(10,2) -->\n" +
				"  <Sign>b0c87cce86a4dfebedc05d83e7f76790</Sign>\n" +
				"  <!--Sign必选，签名串String32-->\n" +
				"  <StoreNo>yidu</StoreNo>\n" +
				"  <!--StoreNo可选，门店编号String20-->\n" +
				"  </Row>\n" +
				"<Row> \n"+
				"  <OrderNo>20171025baxi"+new Random().nextInt(999)+"</OrderNo>\n" +
				"  <!--OrderNo必选，订单号，String20-->\n" +
				"  <OrderTime>20161110093916</OrderTime>\n" +
				"  <!--OrderTime必选，订单时间，String14-->\n" +
				"  <Price>"+new Random().nextInt(800)+"</Price>\n" +
				"  <!--Price必选，金额，Double(10,2) -->\n" +
				"  <Sign>b0c87cce86a4dfebedc05d83e7aaaaaa</Sign>\n" +
				"  <!--Sign必选，签名串String32-->\n" +
				"  <StoreNo>baxi</StoreNo>\n" +
				"  <!--StoreNo可选，门店编号String20-->\n" +
				"  </Row>\n" +
				"</Request>\n";
		//测试
//		String appId = "RJ5689ea2d0482";
//		String key = "0f2aa080911da0adcfc5f630e9d20e1a";

		//正式
		String appId = "RJ7dc91c19acd6";
		String key = "3a0fcda27307672fe09033b2690f32b3";
		System.out.println(HttpClientUtil.wsUploadOrder("http://invoice.datarj.com/webService/services/invoiceService?wsdl",OrderData,appId,key ));
	}

}
