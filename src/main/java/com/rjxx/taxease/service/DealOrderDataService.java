package com.rjxx.taxease.service;

import com.rjxx.taxeasy.bizcomm.utils.SaveOrderData;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Jymxsq;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.domains.Yh;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JyxxsqService;
import com.rjxx.taxeasy.service.YhService;
import com.rjxx.utils.CheckOrderUtil;
import com.rjxx.utils.ResponseUtils;
import com.rjxx.utils.TemplateUtils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.xml.stream.XMLStreamException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DealOrderDataService {

	@Autowired
	private GsxxService gsxxservice;

	@Autowired
	private CheckOrderUtil checkorderutil;

	@Autowired
	private JyxxsqService jyxxsqService;

	@Autowired
	private SaveOrderData saveorderdata;

	@Autowired
	private YhService yhservice;

	/**
	 * 处理交易数据上传
	 *
	 * @param uploadOrder
	 * @return
	 */
	@Transactional
	public String uploadOrderData(final String AppId, final String Sign, final String Operation,
			final String OrderData) {
		final Map resultMap = new HashMap();
		try {
			String result = dealOrder(AppId, Sign, Operation, OrderData);
			return result;
			// resultMap.put("result", result);
		} catch (Exception e) {
			e.printStackTrace();
			String result = ResponseUtils.printFailure("9999:" + e.getMessage());
			throw new RuntimeException(result);
		}
	}

	// 处理上传的交易信息 appkey == AppId secret == key
	@Transactional
	public String dealOrder(String AppId, String Sign, String Operation, String OrderData) {
		String result = "";
		Map tempMap = new HashMap();
		tempMap.put("appkey", AppId);
		Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
		if (gsxxBean == null) {
			return ResponseUtils.printFailure1("9060:" + AppId + "," + Sign);
		} else {
			// 校验数据是否被篡改过
			String key = gsxxBean.getSecretKey();
			String signSourceData = "data=" + OrderData + "&key=" + key;
			String newSign = DigestUtils.md5Hex(signSourceData);
			if (!Sign.equals(newSign)) {
				result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9060:签名不通过</ReturnMessage> \n</Responese>";
				return result;
			}
			String gsdm = gsxxBean.getGsdm();
			if (Operation.equals("03")) {
				List<Jyxxsq> jyxxsqList = dealOperation03(gsdm, OrderData);
				String tmp = checkorderutil.checkBuyer(jyxxsqList, gsdm,Operation);
				if (null == tmp || tmp.equals("")) {
					String tmp1 = saveorderdata.saveBuyerData(jyxxsqList);
					if (null != tmp1 && !tmp1.equals("")) {
						result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp1
								+ "</ReturnMessage> \n</Responese>";
					} else {
						result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>" + "代开票数据上传成功"
								+ "</ReturnMessage> \n</Responese>";
					}
				} else {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp
							+ "</ReturnMessage> \n</Responese>";
				}
			} else if (Operation.equals("01")) {
				Map map = dealOperation01(gsdm, OrderData);
				List<Jyxxsq> jyxxsqList = (List) map.get("jyxxsqList");
				List<Jymxsq> jymxsqList = (List) map.get("jymxsqList");
				// List<Jymxsq> tmpList = null;
				Jyxxsq jyxxsq = new Jyxxsq();
				Jymxsq jymxsq = new Jymxsq();

				String tmp = checkorderutil.checkAll(jyxxsqList, jymxsqList, gsdm,Operation);
				if (null == tmp || tmp.equals("")) {
					String tmp2 = saveorderdata.saveAllData(jyxxsqList, jymxsqList);
					if (null != tmp2 && !tmp2.equals("")) {
						result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp2
								+ "</ReturnMessage> \n</Responese>";

					} else {
						List resultList = new ArrayList();
						for (int i = 0; i < jyxxsqList.size(); i++) {
							double hjje = 0.00;
							double hjse = 0.00;
							List<Jymxsq> tmpList = new ArrayList<Jymxsq>();
							jyxxsq = jyxxsqList.get(i);
							for (int j = 0; j < jymxsqList.size(); j++) {
								jymxsq = jymxsqList.get(j);
								if (jyxxsq.getDdh().equals(jymxsq.getDdh())) {
									tmpList.add(jymxsq);
									hjje = hjje + jymxsq.getSpje();
									hjse = hjse + jymxsq.getSpse();
								}
							}
							String path = this.getClass().getClassLoader().getResource("DllFpkjModel.xml").getPath();
							try {
								Map params = new HashMap();
								if(jyxxsq.getFpzldm().equals("01")){
									jyxxsq.setFpzldm("0");
								}else if(jyxxsq.getFpzldm().equals("02")){
									jyxxsq.setFpzldm("1");
								}
								params.put("jyxxsq", jyxxsq);
								params.put("tmpList", tmpList);
								params.put("count", tmpList.size());
								params.put("hjje", hjje);
								params.put("hjse", hjse);
								params.put("Operation", Operation);
								path = URLDecoder.decode(path, "UTF-8");
								File templateFile = new File(path);
								String result2 = TemplateUtils.generateContent(templateFile, params, "gbk");
								System.out.println(result2);
								result = callDllWebSevice(result2, params);
								resultList.add(result);
								System.out.println(result);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						result = response(resultList);
					}
				} else {
					result = ResponseUtils.printFailure(tmp);
				}

			} else if (Operation.equals("02")) {
				// dealOperation01
				Map map = (Map) dealOperation02(gsdm, OrderData);
				List<Jyxxsq> jyxxsqList = (List) map.get("jyxxsqList");
				List<Jymxsq> jymxsqList = (List) map.get("jymxsqList");
				// List<Jymxsq> tmpList = null;
				Jyxxsq jyxxsq = new Jyxxsq();
				Jymxsq jymxsq = new Jymxsq();
				String tmp = checkorderutil.checkAll(jyxxsqList, jymxsqList, gsdm,Operation);
				if (null == tmp || tmp.equals("")) {
					String tmp3 = saveorderdata.saveAllData(jyxxsqList, jymxsqList);
					if (null != tmp3 && !tmp3.equals("")) {
						result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp3
								+ "</ReturnMessage> \n</Responese>";

					} else {
						result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>" + "代开票数据上传成功"
								+ "</ReturnMessage> \n</Responese>";

					}

				} else {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp
							+ "</ReturnMessage> \n</Responese>";
				}
			}else if (Operation.equals("08")){
				//08代表当前发票号码
				Map inputMap = dealOperation08(gsdm,OrderData);
				String clientNO = String.valueOf(inputMap.get("clientNO"));
				String fpzldm = String.valueOf(inputMap.get("fpzldm"));
				if(null ==clientNO ||clientNO.equals("") ||null ==fpzldm ||fpzldm.equals("")){
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + "ClientNO或Fplxdm不能为空！"
							+ "</ReturnMessage> \n</Responese>";
				}else{
					if(fpzldm.equals("01")){
						fpzldm="0";
					}else if(fpzldm.equals("02")){
						fpzldm="1";
					}
					Map map = new HashMap();
					map.put("clientNO", clientNO);
					map.put("fpzldm", fpzldm);
					map.put("Operation",Operation);
					result =callDllWebSevice(gsdm,map);
					result =response08(result);
				}
				
			}

		}
		return result;
	}

	private String callDllWebSevice(String xml, Map map) {
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		String dllwspath = readFile("DllWebServicePath");
		Client client = dcf.createClient(dllwspath);
		String Operation = (String) map.get("Operation");
		String result = "";
		try {
			if (Operation.equals("01")) {
				Jyxxsq jyxxsq = (Jyxxsq) map.get("jyxxsq");
				String CLIENTNO = jyxxsq.getKpddm();
				String TaxMachineIP = "";
				String SysInvNo = jyxxsq.getDdh();
				String InvoiceList = jyxxsq.getSfdyqd();
				String InvoiceSplit = jyxxsq.getSfcp();
				String InvoiceConsolidate = "0";
				String methodName = "CallService";
				Object[] objects = client.invoke(methodName, CLIENTNO, TaxMachineIP, SysInvNo, InvoiceList,
						InvoiceSplit, InvoiceConsolidate, xml);
				result = objects[0].toString();
			}else if(Operation.equals("08")){
				String methodName = "GetCodeAndNo";
				String CLIENTNO = String.valueOf(map.get("clientNO"));
				String fplxdm = String.valueOf(map.get("fpzldm"));
				Object[] objects =client.invoke(methodName, CLIENTNO, null, fplxdm);
				result = objects[0].toString();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private Map dealOperation08(String gsdm, String OrderData) {
		OMElement root = null;
		Map inputMap = new HashMap();
		try {
			root = xml2OMElement(OrderData);
			Map rootMap = xml2Map(root, "");
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
	private Map dealOperation01(String gsdm, String OrderData) {
		Map params1 = new HashMap();
		params1.put("gsdm", gsdm);
		Yh yh = yhservice.findOneByParams(params1);
		int lrry = yh.getId();
		OMElement root = null;
		List<Jyxxsq> jyxxsqList = new ArrayList();
		List<Jymxsq> jymxsqList = new ArrayList();
		Map rsMap = new HashMap();
		Document xmlDoc = null;
		try {
			xmlDoc = DocumentHelper.parseText(OrderData);
			root = xml2OMElement(OrderData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map rootMap = xml2Map(root, "Order");
		// 开票点代码
		String clientNO = (String) rootMap.get("ClientNO");

		// 交易流水号
		String serialNumber = (String) rootMap.get("SerialNumber");

		// 发票种类代码
		String invType = (String) rootMap.get("InvType");
		if(invType.equals("01")){
			invType="0";
		}else if(invType.equals("02")){
			invType="1";
		}
		// 发票业务类型
		String serviceType = (String) rootMap.get("ServiceType");

		// 开票人
		String drawer = (String) rootMap.get("Drawer");
		if(null == drawer){
			drawer="";
		}
		// 收款人
		String payee = (String) rootMap.get("Payee");
		if(null == payee){
			payee="";
		}
		// 复核人
		String reviewer = (String) rootMap.get("Reviewer");
		if(null == reviewer){
			reviewer ="";
		}
		// 销方信息
		Map sellerMap = (Map) rootMap.get("Seller");
		String identifier = (String) sellerMap.get("Identifier");
		String name = (String) sellerMap.get("Name");
		String address = (String) sellerMap.get("Address");
		String telephoneNo = (String) sellerMap.get("TelephoneNo");
		String bank = (String) sellerMap.get("Bank");
		String bankAcc = (String) sellerMap.get("BankAcc");
		// 明细信息

		List<Element> xnList = xmlDoc.selectNodes("Request/OrderSize/Order");
		if (null != xnList && xnList.size() > 0) {
			for (Element xn : xnList) {
				Jyxxsq jyxxsq = new Jyxxsq();
				Element orderMainMap = (Element) xn.selectSingleNode("OrderMain");
				// 订单号
				String orderNo = orderMainMap.selectSingleNode("OrderNo").getText();
				// 是否打印清单 1 打印清单 0 不打印清单
				String invoiceList = orderMainMap.selectSingleNode("InvoiceList").getText();
				// 是否自动拆分
				String invoiceSplit = orderMainMap.selectSingleNode("InvoiceSplit").getText();
				// 订单日期
				String orderDate = orderMainMap.selectSingleNode("OrderDate").getText();
				// 征税方式
				String chargeTaxWay = orderMainMap.selectSingleNode("ChargeTaxWay").getText();
				// 价税合计
				String totalAmount = orderMainMap.selectSingleNode("TotalAmount").getText();
				// 含税标志
				String taxMark = orderMainMap.selectSingleNode("TaxMark").getText();
				// 备注
				String remark = orderMainMap.selectSingleNode("Remark").getText();

				Element buyerMap = (Element) orderMainMap.selectSingleNode("Buyer");
				String buyerIdentifier = buyerMap.selectSingleNode("Identifier").getText();
				String buyerName = buyerMap.selectSingleNode("Name").getText();
				String buyerAddress = buyerMap.selectSingleNode("Address").getText();
				String buyerTelephoneNo = buyerMap.selectSingleNode("TelephoneNo").getText();
				String buyerBank = buyerMap.selectSingleNode("Bank").getText();
				String buyerBankAcc = buyerMap.selectSingleNode("BankAcc").getText();
				String buyerEmail = buyerMap.selectSingleNode("Email").getText();
				String buyerIsSend = buyerMap.selectSingleNode("IsSend").getText();
				String buyerExtractedCode = buyerMap.selectSingleNode("ExtractedCode").getText();
				String buyerRecipient = buyerMap.selectSingleNode("Recipient").getText();
				String buyerReciAddress = buyerMap.selectSingleNode("ReciAddress").getText();
				String buyerZip = buyerMap.selectSingleNode("Zip").getText();
				// 保存主表信息
				jyxxsq.setKpddm(clientNO);
				jyxxsq.setJylsh(serialNumber);
				jyxxsq.setFpzldm(invType);
				jyxxsq.setKpr(drawer);
				jyxxsq.setSkr(payee);
				jyxxsq.setFhr(reviewer);
				jyxxsq.setXfsh(identifier);
				jyxxsq.setXfmc(name);
				jyxxsq.setXfdz(address);
				jyxxsq.setXfdh(telephoneNo);
				jyxxsq.setXfyh(bank);
				jyxxsq.setXfyhzh(bankAcc);
				jyxxsq.setDdh(orderNo);
				jyxxsq.setSfdyqd(invoiceList);
				jyxxsq.setSfcp(invoiceSplit);
				SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try {
					jyxxsq.setDdrq(orderDate == null ? new Date() : sim.parse(orderDate));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jyxxsq.setZsfs(chargeTaxWay);
				jyxxsq.setJshj(Double.valueOf(totalAmount));
				jyxxsq.setHsbz(taxMark);
				jyxxsq.setBz(remark);
				jyxxsq.setGfsh(buyerIdentifier);
				jyxxsq.setGfmc(buyerName);
				jyxxsq.setGfdz(buyerAddress);
				jyxxsq.setGfdh(buyerTelephoneNo);
				jyxxsq.setGfyh(buyerBank);
				jyxxsq.setGfyhzh(buyerBankAcc);
				jyxxsq.setGfemail(buyerEmail);
				jyxxsq.setSffsyj(buyerIsSend);
				jyxxsq.setTqm(buyerExtractedCode);
				jyxxsq.setGfsjr(buyerRecipient);
				jyxxsq.setGfsjrdz(buyerReciAddress);
				jyxxsq.setGfyb(buyerZip);
				jyxxsq.setYkpjshj(Double.valueOf("0.00"));
				jyxxsq.setYxbz("1");
				jyxxsq.setLrsj(new Date());
				jyxxsq.setLrry(lrry);
				jyxxsq.setXgry(lrry);
				jyxxsq.setXgsj(new Date());
				jyxxsq.setGsdm(gsdm);
				jyxxsqList.add(jyxxsq);
				// List orderDetailsList = (List)
				// orderMainMap.get("OrderDetails");
				Element OrderDetails = (Element) xn.selectSingleNode("OrderDetails");
				List<Element> orderDetailsList = (List<Element>) OrderDetails.elements("ProductItem");
				if (null != orderDetailsList && orderDetailsList.size() > 0) {
					int spmxxh = 0;
					for (Element orderDetails : orderDetailsList) {
						Jymxsq jymxsq = new Jymxsq();
						// Map ProductItem = (Map) orderDetailsList.get(j);
						spmxxh++;
						// 商品代码
						String ProductCode = orderDetails.selectSingleNode("ProductCode").getText();
						jymxsq.setSpdm(ProductCode);
						// 商品名称
						String ProductName = orderDetails.selectSingleNode("ProductName").getText();
						jymxsq.setSpmc(ProductName);
						jymxsq.setDdh(jyxxsq.getDdh());
						jymxsq.setHsbz(jyxxsq.getHsbz());
						// 发票行性质
						String RowType = orderDetails.selectSingleNode("RowType").getText();
						jymxsq.setFphxz(RowType);
						// 商品规格型号
						String Spec = orderDetails.selectSingleNode("Spec").getText();
						jymxsq.setSpggxh(Spec);
						// 商品单位
						String Unit = orderDetails.selectSingleNode("Unit").getText();
						jymxsq.setSpdw(Unit);
						// 商品数量
						String Quantity = orderDetails.selectSingleNode("Quantity").getText();
						if (null != Quantity && !Quantity.equals(""))
							jymxsq.setSps(Double.valueOf(Quantity));
						// 商品单价
						String UnitPrice = orderDetails.selectSingleNode("UnitPrice").getText();
						if (null != UnitPrice && !UnitPrice.equals(""))
							jymxsq.setSpdj(Double.valueOf(UnitPrice));
						// 商品金额
						String Amount = orderDetails.selectSingleNode("Amount").getText();
						jymxsq.setSpje(Double.valueOf(Amount));
						// 扣除金额
						String DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
						jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
								: Double.valueOf(DeductAmount));
						String TaxRate = orderDetails.selectSingleNode("TaxRate").getText();
						jymxsq.setSpsl(Double.valueOf(TaxRate));
						String TaxAmount = orderDetails.selectSingleNode("TaxAmount").getText();
						jymxsq.setSpse(Double.valueOf(TaxAmount));
						String MxTotalAmount = orderDetails.selectSingleNode("MxTotalAmount").getText();
						jymxsq.setJshj(Double.valueOf(MxTotalAmount));
						jymxsq.setSpmxxh(spmxxh);
						String VenderOwnCode = orderDetails.selectSingleNode("VenderOwnCode").getText();
						jymxsq.setSpzxbm(VenderOwnCode);
						String PolicyMark = orderDetails.selectSingleNode("PolicyMark").getText();
						jymxsq.setYhzcbs(PolicyMark);
						String TaxRateMark = orderDetails.selectSingleNode("TaxRateMark").getText();
						jymxsq.setLslbz(TaxRateMark);
						String PolicyName = orderDetails.selectSingleNode("PolicyName").getText();
						jymxsq.setYhzcmc(PolicyName);
						jymxsq.setGsdm(gsdm);
						jymxsq.setLrry(lrry);
						jymxsq.setLrsj(new Date());
						jymxsq.setXgry(lrry);
						jymxsq.setXgsj(new Date());
						jymxsqList.add(jymxsq);

					}

				}

			}
		}

		rsMap.put("jyxxsqList", jyxxsqList);
		rsMap.put("jymxsqList", jymxsqList);
		return rsMap;
	}

	// 处理购方信息xml
	private List dealOperation03(String gsdm, String OrderData) {
		OMElement root = null;
		List<Jyxxsq> jyxxsqList = new ArrayList();
		try {
			root = xml2OMElement(OrderData);
			List rootList = xml2List(root, "Row");// 获取xml中的row标签下的数据
			Map jyxxsqMap = null;
			String clientNO = "";// 开票点代码
			String orderNo = "";// 订单号
			String orderTime = "";// 订单日期
			String totalAmount = "";// 计税合计
			String invType = "";// 发票种类
			String identifier = "";// 购方税号
			String name = "";// 购方名称
			String address = "";// 购方地址
			String telephoneNo = "";// 购方电话
			String bank = "";// 购方银行
			String bankAcc = "";// 购方银行账号
			String email = "";// 购方邮箱
			String isSend = "";// 是否发送邮件
			String extractedCode = "";// 提取码
			String recipient = "";// 购方收件人
			String reciAddress = "";// 购方收件人地址
			String zip = "";// 购方邮编
			String remark = "";// 备注
			String taxMark = "";// 含税标志
			SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			for (int i = 0; i < rootList.size(); i++) {
				jyxxsqMap = (Map) rootList.get(i);
				// 分装对应的bean
				clientNO = (String) jyxxsqMap.get("ClientNO");// 开票点代码
				Map tt = new HashMap();
				tt.put("kpddm", clientNO);
				tt.put("gsdm", gsdm);
				Xf xf = jyxxsqService.findXfExistByKpd(tt);
				orderNo = (String) jyxxsqMap.get("OrderNo");// 订单号 必选
				orderTime = (String) jyxxsqMap.get("OrderTime");// 订单日期 必选
				totalAmount = String.valueOf(jyxxsqMap.get("TotalAmount"));// 计税合计
				invType = (String) jyxxsqMap.get("InvType");// 发票种类01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）
				if(invType.equals("01")){
					invType="0";
				}else if(invType.equals("02")){
					invType="1";
				}
				identifier = (String) jyxxsqMap.get("Identifier");// 购方税号
				name = (String) jyxxsqMap.get("Name");// 购方名称
				address = String.valueOf(jyxxsqMap.get("Address"));// 购方地址
				telephoneNo = String.valueOf(jyxxsqMap.get("TelephoneNo"));// 购方电话
				bank = String.valueOf(jyxxsqMap.get("Bank"));// 购方银行
				bankAcc = String.valueOf(jyxxsqMap.get("BankAcc"));// 购方银行账号
				email = String.valueOf(jyxxsqMap.get("Email"));// 购方邮箱
				isSend = String.valueOf(jyxxsqMap.get("IsSend"));// 是否发送邮件
				extractedCode = String.valueOf(jyxxsqMap.get("ExtractedCode"));// 提取码
				recipient = String.valueOf(jyxxsqMap.get("Recipient"));// 购方收件人地址
				reciAddress = String.valueOf(jyxxsqMap.get("ReciAddress"));// 购方收件人地址
				zip = String.valueOf(jyxxsqMap.get("Zip"));// 购方邮编
				remark = String.valueOf(jyxxsqMap.get("Remark"));
				taxMark = String.valueOf(jyxxsqMap.get("TaxMark"));
				Jyxxsq jyxxsq = new Jyxxsq(); // 主表
				jyxxsq.setKpddm(clientNO);
				jyxxsq.setDdh(orderNo);
				jyxxsq.setDdrq(orderTime == null ? new Date() : sim.parse(orderTime));
				jyxxsq.setJshj(Double.valueOf(totalAmount));
				jyxxsq.setFpzldm(invType);
				jyxxsq.setGfsh(identifier);
				jyxxsq.setGfmc(name);
				jyxxsq.setGfdz(address);
				jyxxsq.setGfdh(telephoneNo);
				jyxxsq.setGfyh(bank);
				jyxxsq.setGfyhzh(bankAcc);
				jyxxsq.setGfemail(email);
				jyxxsq.setSffsyj(isSend);
				// jyxxsq.setSsyf(isSend);
				jyxxsq.setTqm(extractedCode);
				jyxxsq.setGfsjr(recipient);
				jyxxsq.setGfsjrdz(reciAddress);
				jyxxsq.setGfyb(zip);
				jyxxsq.setBz(remark == null ? "" : remark);
				jyxxsq.setXfid(xf.getId());
				jyxxsq.setXfsh(xf.getXfsh());
				jyxxsq.setXfmc(xf.getXfmc());
				jyxxsq.setXfdz(xf.getXfdz());
				jyxxsq.setXfdh(xf.getXfdh());
				jyxxsq.setXflxr(xf.getXflxr());
				jyxxsq.setXfyh(xf.getXfyh());
				jyxxsq.setXfyhzh(xf.getXfyhzh());
				jyxxsq.setXfyb(xf.getXfyb());
				jyxxsq.setKpr(xf.getKpr());
				jyxxsq.setSkr(xf.getSkr());
				jyxxsq.setFhr(xf.getFhr());
				jyxxsq.setHsbz(taxMark);
				jyxxsq.setLrsj(new Date());
				jyxxsq.setLrry(xf.getId());
				jyxxsq.setXgry(xf.getId());
				jyxxsq.setXgsj(new Date());
				jyxxsq.setYkpjshj(Double.valueOf("0.00"));
				jyxxsq.setYxbz("1");
				jyxxsq.setGsdm(gsdm);
				jyxxsq.setTqm("");
				jyxxsq.setJylsh("YD" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
				jyxxsqList.add(jyxxsq);
			}
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage();
			throw new RuntimeException(result);
		}
		return jyxxsqList;
	}

	// 处理交易信息xml
	private Map dealOperation02(String gsdm, String OrderData) {
		Document xmlDoc = null;
		List<Jyxxsq> jyxxsqList = new ArrayList();
		List<Jymxsq> jymxsqList = new ArrayList();
		Map params1 = new HashMap();
		params1.put("gsdm", gsdm);
		Map rsMap = new HashMap();
		Yh yh = yhservice.findOneByParams(params1);
		int lrry = yh.getId();
		try {
			xmlDoc = DocumentHelper.parseText(OrderData);
			List<Element> xntList = (List) xmlDoc.selectNodes("Request/Row");
			// Map jyxxsqMap = null;
			String clientNO = "";// 开票点代码
			String orderNo = "";// 订单号
			String chargeTaxWay = "";// 征税方式
			String invoiceList = "";// 是否打印清单
			String invoiceSplit = "";// 是否分票
			String invType = "";// 发票种类
			String totalAmount = "";// 加税合计
			String taxMark = "";// 含税标志
			SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			for (Element row : xntList) {
				// 分装对应的bean
				clientNO = row.selectSingleNode("ClientNO").getText();// 开票点代码
				Map tt = new HashMap();
				tt.put("kpddm", clientNO);
				tt.put("gsdm", gsdm);
				Xf xf = jyxxsqService.findXfExistByKpd(tt);
				orderNo = row.selectSingleNode("OrderNo").getText();// 订单号 必选
				// orderTime = (String) jyxxsqMap.get("OrderTime");// 订单日期 必选
				totalAmount = String.valueOf(row.selectSingleNode("TotalAmount").getText());// 计税合计
				invType = row.selectSingleNode("InvType").getText();// 发票种类01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）
				if(invType.equals("01")){
					invType="0";
				}else if(invType.equals("02")){
					invType="1";
				}
				taxMark = String.valueOf(row.selectSingleNode("TaxMark").getText());
				
				 invoiceList = row.selectSingleNode("InvoiceList").getText();
				// 是否自动拆分
				 invoiceSplit = row.selectSingleNode("InvoiceSplit").getText();
				// 订单日期
				// orderDate = row.selectSingleNode("OrderDate").getText();
				// 征税方式
				 chargeTaxWay = row.selectSingleNode("ChargeTaxWay").getText();
				Jyxxsq jyxxsq = new Jyxxsq(); // 主表
				jyxxsq.setKpddm(clientNO);
				jyxxsq.setDdh(orderNo);
				// jyxxsq.setDdrq(orderTime == null ? new Date() :
				// sim.parse(orderTime));
				jyxxsq.setJshj(Double.valueOf(totalAmount));
				jyxxsq.setFpzldm(invType);
				jyxxsq.setSfdyqd(invoiceList);
				jyxxsq.setSfcp(invoiceSplit);
				jyxxsq.setZsfs(chargeTaxWay);
				jyxxsq.setXfid(xf.getId());
				jyxxsq.setXfsh(xf.getXfsh());
				jyxxsq.setXfmc(xf.getXfmc());
				jyxxsq.setXfdz(xf.getXfdz());
				jyxxsq.setXfdh(xf.getXfdh());
				jyxxsq.setXflxr(xf.getXflxr());
				jyxxsq.setXfyh(xf.getXfyh());
				jyxxsq.setXfyhzh(xf.getXfyhzh());
				jyxxsq.setXfyb(xf.getXfyb());
				jyxxsq.setKpr(xf.getKpr());
				jyxxsq.setSkr(xf.getSkr());
				jyxxsq.setFhr(xf.getFhr());
				jyxxsq.setHsbz(taxMark);
				jyxxsq.setLrsj(new Date());
				jyxxsq.setLrry(xf.getId());
				jyxxsq.setXgry(xf.getId());
				jyxxsq.setXgsj(new Date());
				jyxxsq.setYkpjshj(Double.valueOf("0.00"));
				jyxxsq.setYxbz("1");
				jyxxsq.setGsdm(gsdm);
				jyxxsq.setTqm("");
				jyxxsq.setJylsh("YD" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
				jyxxsqList.add(jyxxsq);
				Element InvoiceDetails = (Element) row.selectSingleNode("InvoiceDetails");
				List<Element> productList = (List<Element>) InvoiceDetails.elements("ProductItem");
				if (null != productList && productList.size() > 0) {
					int spmxxh = 0;
					for (Element orderDetails : productList) {
						Jymxsq jymxsq = new Jymxsq();
						// Map ProductItem = (Map) orderDetailsList.get(j);
						spmxxh++;
						// 商品代码
						String ProductCode = orderDetails.selectSingleNode("ProductCode").getText();
						jymxsq.setSpdm(ProductCode);
						// 商品名称
						String ProductName = orderDetails.selectSingleNode("ProductName").getText();
						jymxsq.setSpmc(ProductName);
						jymxsq.setDdh(jyxxsq.getDdh());
						jymxsq.setHsbz(jyxxsq.getHsbz());
						// 发票行性质
						String RowType = orderDetails.selectSingleNode("RowType").getText();
						jymxsq.setFphxz(RowType);
						// 商品规格型号
						String Spec = orderDetails.selectSingleNode("Spec").getText();
						jymxsq.setSpggxh(Spec);
						// 商品单位
						String Unit = orderDetails.selectSingleNode("Unit").getText();
						jymxsq.setSpdw(Unit);
						// 商品数量
						String Quantity = orderDetails.selectSingleNode("Quantity").getText();
						if (null != Quantity && !Quantity.equals(""))
							jymxsq.setSps(Double.valueOf(Quantity));
						// 商品单价
						String UnitPrice = orderDetails.selectSingleNode("UnitPrice").getText();
						if (null != UnitPrice && !UnitPrice.equals(""))
							jymxsq.setSpdj(Double.valueOf(UnitPrice));
						// 商品金额
						String Amount = orderDetails.selectSingleNode("Amount").getText();
						jymxsq.setSpje(Double.valueOf(Amount));
						// 扣除金额
						String DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
						jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
								: Double.valueOf(DeductAmount));
						String TaxRate = orderDetails.selectSingleNode("TaxRate").getText();
						jymxsq.setSpsl(Double.valueOf(TaxRate));
						String TaxAmount = orderDetails.selectSingleNode("TaxAmount").getText();
						jymxsq.setSpse(Double.valueOf(TaxAmount));
						String MxTotalAmount = orderDetails.selectSingleNode("MxTotalAmount").getText();
						jymxsq.setJshj(Double.valueOf(MxTotalAmount));
						jymxsq.setSpmxxh(spmxxh);
						String VenderOwnCode = orderDetails.selectSingleNode("VenderOwnCode").getText();
						jymxsq.setSpzxbm(VenderOwnCode);
						String PolicyMark = orderDetails.selectSingleNode("PolicyMark").getText();
						jymxsq.setYhzcbs(PolicyMark);
						String TaxRateMark = orderDetails.selectSingleNode("TaxRateMark").getText();
						jymxsq.setLslbz(TaxRateMark);
						String PolicyName = orderDetails.selectSingleNode("PolicyName").getText();
						jymxsq.setYhzcmc(PolicyName);
						jymxsq.setGsdm(gsdm);
						jymxsq.setLrry(lrry);
						jymxsq.setLrsj(new Date());
						jymxsq.setXgry(lrry);
						jymxsq.setXgsj(new Date());
						jymxsqList.add(jymxsq);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage();
			throw new RuntimeException(result);
		}
		rsMap.put("jyxxsqList", jyxxsqList);
		rsMap.put("jymxsqList", jymxsqList);
		return rsMap;
		//return jyxxsqList;
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

	private String response(List list) {
		Document xmlDoc = null;
		XMLWriter xw;
		Document doc = DocumentHelper.createDocument();
		StringWriter sendXml = new StringWriter();
		// 增加根节点
		Element Responese = doc.addElement("Responese");
		Element returnCode = Responese.addElement("ReturnCode");
		returnCode.setText("0000");
		Element returnMessage = Responese.addElement("ReturnMessage");
		returnMessage.setText("成功");
		if (null != list && !list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				String tmp = String.valueOf(list.get(i));
				try {
					xmlDoc = DocumentHelper.parseText(tmp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Element xnt = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
				// 为根节点增加元素body
				
				Element RCF = Responese.addElement("invoice");
				Element CLIENTNO = RCF.addElement("CLIENTNO");
				CLIENTNO.setText(xnt.selectSingleNode("CLIENTNO").getText() == null ? ""
						: xnt.selectSingleNode("CLIENTNO").getText());// 添加值
				Element SwiftNumber = RCF.addElement("SwiftNumber");
				SwiftNumber.setText(xnt.selectSingleNode("SwiftNumber") == null ? ""
						: xnt.selectSingleNode("SwiftNumber").getText());
				Element SysInvNo = RCF.addElement("SysInvNo");
				SysInvNo.setText(
						xnt.selectSingleNode("SysInvNo") == null ? "" : xnt.selectSingleNode("SysInvNo").getText());
				Element InvCode = RCF.addElement("InvCode");
				InvCode.setText(
						xnt.selectSingleNode("InvCode") == null ? "" : xnt.selectSingleNode("InvCode").getText());
				Element InvNo = RCF.addElement("InvNo");
				InvNo.setText(xnt.selectSingleNode("InvNo") == null ? "" : xnt.selectSingleNode("InvNo").getText());
				Element InvDate = RCF.addElement("InvDate");
				InvDate.setText(
						xnt.selectSingleNode("InvDate") == null ? "" : xnt.selectSingleNode("InvDate").getText());
				Element CancelDate = RCF.addElement("CancelDate");
				CancelDate.setText(
						xnt.selectSingleNode("CancelDate") == null ? "" : xnt.selectSingleNode("CancelDate").getText());
				Element OperateFlag = RCF.addElement("OperateFlag");
				OperateFlag.setText(xnt.selectSingleNode("OperateFlag") == null ? ""
						: xnt.selectSingleNode("OperateFlag").getText());
				Element PrintFlag = RCF.addElement("PrintFlag");
				PrintFlag.setText(
						xnt.selectSingleNode("PrintFlag") == null ? "" : xnt.selectSingleNode("PrintFlag").getText());
				Element returnmsg = RCF.addElement("returnmsg");
				returnmsg.setText(
						xnt.selectSingleNode("returnmsg") == null ? "" : xnt.selectSingleNode("returnmsg").getText());
			}
			// 规范格式
			OutputFormat format = OutputFormat.createPrettyPrint();
			// 设置输出编码
			format.setEncoding("gbk");
			xw = new XMLWriter(sendXml, format);
			try {
				xw.write(doc);
				xw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return sendXml.toString();
	}
    
	private String response08(String result) {
		Document xmlDoc = null;
		XMLWriter xw;
		Document doc = DocumentHelper.createDocument();
		StringWriter sendXml = new StringWriter();
		// 增加根节点
			Element Responese = doc.addElement("Responese");
			try {
				xmlDoc = DocumentHelper.parseText(result);
				Element xnt = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
				Element ClientNO = Responese.addElement("CLIENTNO");
				ClientNO.setText(xnt.selectSingleNode("CLIENTNO").getText() == null ? ""
						: xnt.selectSingleNode("CLIENTNO").getText());// 添加值
				Element Fplxdm = Responese.addElement("Fplxdm");
				String fplxdm = xnt.selectSingleNode("fplxdm").getText();
				if(fplxdm.equals("0")){
					fplxdm ="01";
				}else if(fplxdm.equals("1")){
					fplxdm ="02";
				}
				Fplxdm.setText(fplxdm);// 添加值
				Element Dqfpdm = Responese.addElement("Dqfpdm");
				Dqfpdm.setText(
						xnt.selectSingleNode("dqfpdm") == null ? "" : xnt.selectSingleNode("dqfpdm").getText());
				Element Dqfphm = Responese.addElement("Dqfphm");
				Dqfphm.setText(
						xnt.selectSingleNode("dqfphm") == null ? "" : xnt.selectSingleNode("dqfphm").getText());
				Element OperateFlag = Responese.addElement("OperateFlag");
				OperateFlag.setText(xnt.selectSingleNode("OperateFlag") == null ? ""
						: xnt.selectSingleNode("OperateFlag").getText());
				Element PrintFlag = Responese.addElement("PrintFlag");
				PrintFlag.setText(
						xnt.selectSingleNode("PrintFlag") == null ? "" : xnt.selectSingleNode("PrintFlag").getText());
				Element Returnmsg = Responese.addElement("Returnmsg");
				Returnmsg.setText(
						xnt.selectSingleNode("returnmsg") == null ? "" : xnt.selectSingleNode("returnmsg").getText());
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 规范格式
						OutputFormat format = OutputFormat.createPrettyPrint();
						// 设置输出编码
						format.setEncoding("gbk");
						xw = new XMLWriter(sendXml, format);
						try {
							xw.write(doc);
							xw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		return sendXml.toString();
	}
	private static String str2Trim(String str) {
		return "".equals(str) ? null : str.trim();
	}

	public static String printFailure(String errorMessage) {
		return "<Responese>\n  <ReturnCode>9999</ReturnCode>\n" + "  <ReturnMessage>" + errorMessage
				+ "</ReturnMessage>\n</Responese>";
	}

	   //读取af的默认配置
    private String readFile(String str){
    	    Properties properties = new Properties();  
	        InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");  
	        BufferedReader bf = new BufferedReader(new  InputStreamReader(inputStream));  
	        try {
				properties.load(bf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
	        return properties.getProperty(str);
    }
}
