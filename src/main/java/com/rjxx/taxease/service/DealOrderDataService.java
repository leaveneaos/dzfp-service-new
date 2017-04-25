package com.rjxx.taxease.service;

import com.rjxx.taxease.utils.CallDllWebServiceUtil;
import com.rjxx.taxease.utils.ResponseUtil;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
import com.rjxx.taxeasy.bizcomm.utils.SaveOrderData;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Jymxsq;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.domains.Yh;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JyxxsqService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.YhService;
import com.rjxx.taxeasy.vo.KplsVO4;
import com.rjxx.utils.CheckOrderUtil;
import com.rjxx.utils.ResponseUtils;
import com.rjxx.utils.TemplateUtils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayInputStream;
import java.io.File;
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
	private SkpService skpservice;

	@Autowired
	private YhService yhservice;

	@Autowired
	private CszbService cszbservice;

	@Autowired
	private FpclService fpclservice;
	
	@Autowired
	private KpspmxService kpspmxService;
	
	@Autowired
	private ResponseUtil responseUtil;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 交易数据上传service
	 *
	 * @param uploadOrder
	 * @return
	 */
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

	/**
	 * 处理上传的交易信息 appkey == AppId secret == key
	 *
	 * @param String,String,String,String
	 * @return String
	 */
	public String dealOrder(String AppId, String Sign, String Operation, String OrderData) {
		String result = "";
		//ResponseUtil responseUtil = new ResponseUtil();
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
				String tmp = checkorderutil.checkBuyer(jyxxsqList, gsdm, Operation);
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
				// 处理一些必须由平台抽取的数据
				//Map moreDate = new HashMap();
				//moreDate = this.addMoreDate(jyxxsqList, gsdm);
				jyxxsqList = (List<Jyxxsq>) this.addMoreDate(jyxxsqList, gsdm);
				//String fpje = (String) moreDate.get("fpje");
				// 取出xfid、skpid等用来插叙该开票点是否直连开票
				String xfid = String.valueOf(jyxxsqList.get(0).getXfid());
				String skpid = String.valueOf(jyxxsqList.get(0).getSkpid());
				String csm = "sfzlkp";
				Map cszbMap = new HashMap();
				cszbMap.put("xfid", xfid);
				cszbMap.put("kpdid", skpid);
				cszbMap.put("csm", csm);
				cszbMap.put("gsdm", gsdm);
				List cszbList = cszbservice.findAllByParams(cszbMap);
				Cszb cszb = null;
				if (null != cszbList && cszbList.size() > 0){
					cszb = (Cszb) cszbList.get(0);

				// List<Jyxxsq> reList = new ArrayList<Jyxxsq>();
				// t_jyxxsq表中sfzlkp设置为对应的值，1直连开，0非直连开
				for (int i = 0; i < jyxxsqList.size(); i++) {
					Jyxxsq jyxxsq = jyxxsqList.get(i);
					if (null != cszb && cszb.getCsz().equals("是")) {
						jyxxsq.setSfzlkp("1");
						jyxxsq.setZtbz("6");
					}
					// reList.add(jyxxsq);
				  }
				}else{
					for (int i = 0; i < jyxxsqList.size(); i++) {
						Jyxxsq jyxxsq = jyxxsqList.get(i);
							jyxxsq.setSfzlkp("0");
							jyxxsq.setZtbz("6");
						// reList.add(jyxxsq);
					}
				}
				// jyxxsqList = reList;

				List<Jymxsq> jymxsqList = (List) map.get("jymxsqList");
				// List<Jymxsq> tmpList = null;
				Jyxxsq jyxxsq = new Jyxxsq();
				Jymxsq jymxsq = new Jymxsq();

				String tmp = checkorderutil.checkAll(jyxxsqList, jymxsqList, gsdm, Operation);
				// 校验通过，进行后续保存，以及开票功能
				if (null == tmp || tmp.equals("")) {
					String tmp2 = saveorderdata.saveAllData(jyxxsqList, jymxsqList);
					// 保存操作成功与否
					if (null != tmp2 && !tmp2.equals("")) {
						result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + tmp2
								+ "</ReturnMessage> \n</Responese>";

					} else {
						if (null != cszb && cszb.getCsz().equals("是")) {
							// 需考虑是否直连开票，若不是直连，不需要实时接口，考虑用参数配置,分为组件接口，录屏方式01录屏，02组件，03其他
							Map cszbMap2 = new HashMap();
							cszbMap2.put("xfid", xfid);
							cszbMap2.put("kpdid", skpid);
							cszbMap2.put("csm", "kpfs");
							cszbMap2.put("gsdm", gsdm);
							List cszbList2 = cszbservice.findAllByParams(cszbMap2);
							Cszb cszb2 = null;
							if (null != cszbList2 && cszbList2.size() > 0)
								cszb2 = (Cszb) cszbList2.get(0);
							// 录屏方式
							if (cszb2.getCsz().equals("01")) {
								List resultList = new ArrayList();
								try {
									resultList = (List)fpclservice.zjkp(jyxxsqList,"01");//录屏

								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								result = responseUtil.lpResponse(resultList);
							} else if (cszb2.getCsz().equals("02")) {
								// 组件方式
								List fpclList = new ArrayList(); 
								try {
									fpclList = (List)fpclservice.zjkp(jyxxsqList,"02");//组件
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								List resultList = new ArrayList();
								if(null != fpclList){
									KplsVO4 zjKplsvo4 =  new KplsVO4();
									for (int i = 0; i < fpclList.size(); i++) {
										double hjje = 0.00;
										double hjse = 0.00;
										List<Kpspmx> tmpList = new ArrayList<Kpspmx>();
										
										zjKplsvo4 = (KplsVO4)fpclList.get(i);
										//获取对应开票商品明细信息
										Map params = new HashMap();
										params.put("kplsh", zjKplsvo4.getKplsh());
										tmpList = kpspmxService.findMxList(params);
										
										//只为获取zsfs,hsbz
										//Integer sqlsh = zjKplsvo4.getSqlsh();
										//jyxxsq = jyxxsqService.findOne(sqlsh);
										
										Kpspmx kpspmx = new Kpspmx();
										for (int j = 0; j < tmpList.size(); j++) {
											kpspmx = tmpList.get(j);
											hjje = hjje + kpspmx.getSpje();
											hjse = hjse + kpspmx.getSpse();
											
										}
										String path = this.getClass().getClassLoader().getResource("DllFpkjModel.xml")
												.getPath();
										try {
											Map params2 = new HashMap();
											String fpzldm = zjKplsvo4.getFpzldm();
											if(fpzldm.equals("01")){
												zjKplsvo4.setFpzldm("0");
											}else if(fpzldm.equals("02")){
												zjKplsvo4.setFpzldm("1");
											}else{
												zjKplsvo4.setFpzldm("12");
											}
											params2.put("kplsvo4", zjKplsvo4);
											params2.put("tmpList", tmpList);
											params2.put("count", tmpList.size());
											params2.put("hjje", hjje);
											params2.put("hjse", hjse);
											//params2.put("jyxxsq", jyxxsq);
											params2.put("Operation", Operation);
											path = URLDecoder.decode(path, "UTF-8");
											File templateFile = new File(path);
											String result2 = TemplateUtils.generateContent(templateFile, params2, "gbk");
											System.out.println(result2);
											logger.debug("封装传开票通的报文" + result2);
											CallDllWebServiceUtil utils = new CallDllWebServiceUtil();
											result = utils.callDllWebSevice(result2, params2);
											resultList.add(result);
											System.out.println(result);

										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										
									}
								/*for (int i = 0; i < jyxxsqList.size(); i++) {
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
									String path = this.getClass().getClassLoader().getResource("DllFpkjModel.xml")
											.getPath();
									try {
										Map params = new HashMap();
										if (jyxxsq.getFpzldm().equals("01")) {
											jyxxsq.setFpzldm("0");
										} else if (jyxxsq.getFpzldm().equals("02")) {
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
										logger.debug("封装传开票通的报文" + result2);
										result = callDllWebSevice(result2, params);
										resultList.add(result);
										System.out.println(result);

									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								  }*/
								}
								result = responseUtil.response(resultList);
							} else {

							}

						} else {
							// 不是直连开票
							result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>开票数据已接收！</ReturnMessage> \n</Responese>";
						}

					}
				} else {
					result = ResponseUtils.printFailure(tmp);
					logger.debug("封装校验不通过信息" + result);
				}

			} else if (Operation.equals("02")) {
				// dealOperation01
				Map map = (Map) dealOperation02(gsdm, OrderData);
				List<Jyxxsq> jyxxsqList = (List) map.get("jyxxsqList");
				List<Jymxsq> jymxsqList = (List) map.get("jymxsqList");
				// List<Jymxsq> tmpList = null;
				Jyxxsq jyxxsq = new Jyxxsq();
				Jymxsq jymxsq = new Jymxsq();
				String tmp = checkorderutil.checkAll(jyxxsqList, jymxsqList, gsdm, Operation);
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
			} else if (Operation.equals("08")) {
				// 08代表当前发票号码
				Map inputMap = dealOperation08(gsdm, OrderData);
				String clientNO = String.valueOf(inputMap.get("clientNO"));
				String fpzldm = String.valueOf(inputMap.get("fpzldm"));
				if (null == clientNO || clientNO.equals("") || null == fpzldm || fpzldm.equals("")) {
					result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>" + "ClientNO或Fplxdm不能为空！"
							+ "</ReturnMessage> \n</Responese>";
				} else {
					if (fpzldm.equals("01")) {
						fpzldm = "0";
					} else if (fpzldm.equals("02")) {
						fpzldm = "1";
					}
					Map map = new HashMap();
					map.put("clientNO", clientNO);
					map.put("fpzldm", fpzldm);
					map.put("Operation", Operation);
					CallDllWebServiceUtil utils = new CallDllWebServiceUtil();
					result = utils.callDllWebSevice(gsdm, map);
					result = responseUtil.response08(result);
				}

			}

		}
		return result;
	}

	/**
	 * 当客户未上传已初始化信息时，查询所平台配置的信息
	 *
	 * @param List,String
	 * @return Map
	 */
	private List<Jyxxsq> addMoreDate(List<Jyxxsq> jyxxsqList, String gsdm) {
		//Map resultMap = new HashMap();
		List<Jyxxsq> resultList = new ArrayList<Jyxxsq>();
		String kpddm = "";
		String fpje = "";
		String fpzldm = "";
		for (int i = 0; i < jyxxsqList.size(); i++) {
			Jyxxsq jyxxsq = jyxxsqList.get(i);
			kpddm = jyxxsq.getKpddm();
			Map params = new HashMap();
			params.put("kpddm", kpddm);
			params.put("gsdm", gsdm);
			Skp skp = skpservice.findOneByParams(params);
			if (null != skp && !skp.equals("")) {
				jyxxsq.setXfid(skp.getXfid());
				jyxxsq.setSkpid(skp.getId());
			}
			/*fpzldm = jyxxsq.getFpzldm();
			if (fpzldm.equals("0")) { // 专票
				fpje = String.valueOf(skp.getZpmax());
			} else if (fpzldm.equals("1")) { // 普票
				fpje = String.valueOf(skp.getPpmax());
			} else {
				fpje = String.valueOf(skp.getDpmax());
			}*/

			resultList.add(jyxxsq);
		}
		//resultMap.put("fpje", fpje);
		//resultMap.put("resultList", resultList);
		return resultList;
	}

	/**
	 * 处理查询当前发票代码、号码
	 *
	 * @param gsdm,OrderData
	 * @return Map
	 */
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

	/**
	 * 处理全部交易信息
	 *
	 * @param String,String
	 * @return Map
	 */
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
		if (invType.equals("01")) {
			invType = "0";
		} else if (invType.equals("02")) {
			invType = "1";
		}
		// 发票业务类型
		String serviceType = (String) rootMap.get("ServiceType");

		// 开票人
		String drawer = (String) rootMap.get("Drawer");
		if (null == drawer) {
			drawer = "";
		}
		// 收款人
		String payee = (String) rootMap.get("Payee");
		if (null == payee) {
			payee = "";
		}
		// 复核人
		String reviewer = (String) rootMap.get("Reviewer");
		if (null == reviewer) {
			reviewer = "";
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
				String orderNo = "";
				if (null != orderMainMap.selectSingleNode("OrderNo")
						&& !orderMainMap.selectSingleNode("OrderNo").equals("")) {
					orderNo = orderMainMap.selectSingleNode("OrderNo").getText();
				}
				// 是否打印清单 1 打印清单 0 不打印清单
				String invoiceList = "0";
				if (null != orderMainMap.selectSingleNode("InvoiceList")
						&& !orderMainMap.selectSingleNode("InvoiceList").equals("")) {
					invoiceList = orderMainMap.selectSingleNode("InvoiceList").getText();
				}

				// 是否自动拆分（1、拆分；0、不拆分）
				String invoiceSplit = "1";
				if (null != orderMainMap.selectSingleNode("InvoiceSplit")
						&& !orderMainMap.selectSingleNode("InvoiceSplit").equals("")) {
					invoiceSplit = orderMainMap.selectSingleNode("InvoiceSplit").getText();
				}
				//是否打印1打印，0不打印
				String InvoiceSfdy = "1";
				if (null != orderMainMap.selectSingleNode("InvoiceSfdy")
						&& !orderMainMap.selectSingleNode("InvoiceSfdy").equals("")) {
					InvoiceSfdy = orderMainMap.selectSingleNode("InvoiceSfdy").getText();
				}
				// 订单日期
				String orderDate = "";
				if (null != orderMainMap.selectSingleNode("OrderDate")
						&& !orderMainMap.selectSingleNode("OrderDate").equals("")) {
					orderDate = orderMainMap.selectSingleNode("OrderDate").getText();
				}

				// 征税方式
				String chargeTaxWay = "";
				if (null != orderMainMap.selectSingleNode("ChargeTaxWay")
						&& !orderMainMap.selectSingleNode("ChargeTaxWay").equals("")) {
					chargeTaxWay = orderMainMap.selectSingleNode("ChargeTaxWay").getText();
				}

				// 价税合计
				String totalAmount = "";
				if (null != orderMainMap.selectSingleNode("TotalAmount")
						&& !orderMainMap.selectSingleNode("TotalAmount").equals("")) {
					totalAmount = orderMainMap.selectSingleNode("TotalAmount").getText();
				}

				// 含税标志
				String taxMark = "";
				if (null != orderMainMap.selectSingleNode("TaxMark")
						&& !orderMainMap.selectSingleNode("TaxMark").equals("")) {
					taxMark = orderMainMap.selectSingleNode("TaxMark").getText();
				}

				// 备注
				String remark = "";
				if (null != orderMainMap.selectSingleNode("Remark")
						&& !orderMainMap.selectSingleNode("Remark").equals("")) {
					remark = orderMainMap.selectSingleNode("Remark").getText();
				}

				Element buyerMap = (Element) orderMainMap.selectSingleNode("Buyer");

				String buyerIdentifier = "";
				if (null != buyerMap.selectSingleNode("Identifier")
						&& !buyerMap.selectSingleNode("Identifier").equals("")) {
					buyerIdentifier = buyerMap.selectSingleNode("Identifier").getText();
				}

				String buyerName = "";
				if (null != buyerMap.selectSingleNode("Name") && !buyerMap.selectSingleNode("Name").equals("")) {
					buyerName = buyerMap.selectSingleNode("Name").getText();
				}

				String buyerAddress = "";
				if (null != buyerMap.selectSingleNode("Address") && !buyerMap.selectSingleNode("Address").equals("")) {
					buyerAddress = buyerMap.selectSingleNode("Address").getText();
				}

				String buyerTelephoneNo = "";
				if (null != buyerMap.selectSingleNode("TelephoneNo")
						&& !buyerMap.selectSingleNode("TelephoneNo").equals("")) {
					buyerTelephoneNo = buyerMap.selectSingleNode("TelephoneNo").getText();
				}

				String buyerBank = "";
				if (null != buyerMap.selectSingleNode("Bank") && !buyerMap.selectSingleNode("Bank").equals("")) {
					buyerBank = buyerMap.selectSingleNode("Bank").getText();
				}

				String buyerBankAcc = "";
				if (null != buyerMap.selectSingleNode("BankAcc") && !buyerMap.selectSingleNode("BankAcc").equals("")) {
					buyerBankAcc = buyerMap.selectSingleNode("BankAcc").getText();
				}

				String buyerEmail = "";
				if (null != buyerMap.selectSingleNode("Email") && !buyerMap.selectSingleNode("Email").equals("")) {
					buyerEmail = buyerMap.selectSingleNode("Email").getText();
				}

				String buyerIsSend = "";
				if (null != buyerMap.selectSingleNode("IsSend") && !buyerMap.selectSingleNode("IsSend").equals("")) {
					buyerIsSend = buyerMap.selectSingleNode("IsSend").getText();
				}

				String buyerExtractedCode = "";
				if (null != buyerMap.selectSingleNode("ExtractedCode")
						&& !buyerMap.selectSingleNode("ExtractedCode").equals("")) {
					buyerExtractedCode = buyerMap.selectSingleNode("ExtractedCode").getText();
				}

				String buyerRecipient = "";
				if (null != buyerMap.selectSingleNode("Recipient")
						&& !buyerMap.selectSingleNode("Recipient").equals("")) {
					buyerRecipient = buyerMap.selectSingleNode("Recipient").getText();
				}

				String buyerReciAddress = "";
				if (null != buyerMap.selectSingleNode("ReciAddress")
						&& !buyerMap.selectSingleNode("ReciAddress").equals("")) {
					buyerReciAddress = buyerMap.selectSingleNode("ReciAddress").getText();
				}

				String buyerZip = "";
				if (null != buyerMap.selectSingleNode("Zip") && !buyerMap.selectSingleNode("Zip").equals("")) {
					buyerZip = buyerMap.selectSingleNode("Zip").getText();
				}

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
				jyxxsq.setSfdy(InvoiceSfdy);
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
				jyxxsq.setFpczlxdm("11");
				jyxxsq.setXgsj(new Date());
				jyxxsq.setGsdm(gsdm);
				jyxxsq.setSjly("1");
				jyxxsq.setClztdm("00");
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
						String ProductCode = "";
						if (null != orderDetails.selectSingleNode("ProductCode")
								&& !orderDetails.selectSingleNode("ProductCode").equals("")) {
							ProductCode = orderDetails.selectSingleNode("ProductCode").getText();
						}

						jymxsq.setSpdm(ProductCode);
						// 商品名称
						String ProductName = "";
						if (null != orderDetails.selectSingleNode("ProductName")
								&& !orderDetails.selectSingleNode("ProductName").equals("")) {
							ProductName = orderDetails.selectSingleNode("ProductName").getText();
						}

						jymxsq.setSpmc(ProductName);
						jymxsq.setDdh(jyxxsq.getDdh());
						jymxsq.setHsbz(jyxxsq.getHsbz());
						// 发票行性质
						String RowType = "";
						if (null != orderDetails.selectSingleNode("RowType")
								&& !orderDetails.selectSingleNode("RowType").equals("")) {
							RowType = orderDetails.selectSingleNode("RowType").getText();
						}

						jymxsq.setFphxz(RowType);
						// 商品规格型号
						String Spec = "";
						if (null != orderDetails.selectSingleNode("Spec")
								&& !orderDetails.selectSingleNode("Spec").equals("")) {
							Spec = orderDetails.selectSingleNode("Spec").getText();
						}

						jymxsq.setSpggxh(Spec);
						// 商品单位
						String Unit = "";
						if (null != orderDetails.selectSingleNode("Unit")
								&& !orderDetails.selectSingleNode("Unit").equals("")) {
							Unit = orderDetails.selectSingleNode("Unit").getText();
						}

						jymxsq.setSpdw(Unit);
						// 商品数量
						String Quantity = "";
						if (null != orderDetails.selectSingleNode("Quantity")
								&& !orderDetails.selectSingleNode("Quantity").equals("")) {
							Quantity = orderDetails.selectSingleNode("Quantity").getText();
							jymxsq.setSps(Double.valueOf(Quantity));
						}

						// 商品单价
						String UnitPrice = "";
						if (null != orderDetails.selectSingleNode("UnitPrice")
								&& !orderDetails.selectSingleNode("UnitPrice").equals("")) {
							UnitPrice = orderDetails.selectSingleNode("UnitPrice").getText();
							jymxsq.setSpdj(Double.valueOf(UnitPrice));
						}

						// 商品金额
						String Amount = "";
						if (null != orderDetails.selectSingleNode("Amount")
								&& !orderDetails.selectSingleNode("Amount").equals("")) {
							Amount = orderDetails.selectSingleNode("Amount").getText();
							jymxsq.setSpje(Double.valueOf(Amount));
						}

						// 扣除金额
						String DeductAmount = "";
						if (null != orderDetails.selectSingleNode("DeductAmount")
								&& !orderDetails.selectSingleNode("DeductAmount").equals("")) {
							DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
							jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
									: Double.valueOf(DeductAmount));
						}

						String TaxRate = "";
						if (null != orderDetails.selectSingleNode("TaxRate")
								&& !orderDetails.selectSingleNode("TaxRate").equals("")) {
							TaxRate = orderDetails.selectSingleNode("TaxRate").getText();
							jymxsq.setSpsl(Double.valueOf(TaxRate));
						}

						String TaxAmount = "";
						if (null != orderDetails.selectSingleNode("TaxAmount")
								&& !orderDetails.selectSingleNode("TaxAmount").equals("")) {
							TaxAmount = orderDetails.selectSingleNode("TaxAmount").getText();
							jymxsq.setSpse(Double.valueOf(TaxAmount));
						}

						String MxTotalAmount = "";
						if (null != orderDetails.selectSingleNode("MxTotalAmount")
								&& !orderDetails.selectSingleNode("MxTotalAmount").equals("")) {
							MxTotalAmount = orderDetails.selectSingleNode("MxTotalAmount").getText();
							jymxsq.setJshj(Double.valueOf(MxTotalAmount));
						}

						jymxsq.setSpmxxh(spmxxh);
						jymxsq.setKkjje(Double.valueOf(MxTotalAmount));
						jymxsq.setYkjje(0d);
						String VenderOwnCode = "";
						if (null != orderDetails.selectSingleNode("VenderOwnCode")
								&& !orderDetails.selectSingleNode("VenderOwnCode").equals("")) {
							VenderOwnCode = orderDetails.selectSingleNode("VenderOwnCode").getText();
						}
						jymxsq.setSpzxbm(VenderOwnCode);

						String PolicyMark = "";
						if (null != orderDetails.selectSingleNode("PolicyMark")
								&& !orderDetails.selectSingleNode("PolicyMark").equals("")) {
							PolicyMark = orderDetails.selectSingleNode("PolicyMark").getText();
						}
						jymxsq.setYhzcbs(PolicyMark);

						String TaxRateMark = "";
						if (null != orderDetails.selectSingleNode("TaxRateMark")
								&& !orderDetails.selectSingleNode("TaxRateMark").equals("")) {
							TaxRateMark = orderDetails.selectSingleNode("TaxRateMark").getText();
						}
						jymxsq.setLslbz(TaxRateMark);

						String PolicyName = "";
						if (null != orderDetails.selectSingleNode("PolicyName")
								&& !orderDetails.selectSingleNode("PolicyName").equals("")) {
							PolicyName = orderDetails.selectSingleNode("PolicyName").getText();
						}
						jymxsq.setYhzcmc(PolicyName);

						jymxsq.setGsdm(gsdm);
						jymxsq.setLrry(lrry);
						jymxsq.setLrsj(new Date());
						jymxsq.setXgry(lrry);
						jymxsq.setXgsj(new Date());
						jymxsq.setYxbz("1");
						jymxsqList.add(jymxsq);

					}

				}

			}
		}

		rsMap.put("jyxxsqList", jyxxsqList);
		rsMap.put("jymxsqList", jymxsqList);
		return rsMap;
	}

	/**
	 * 处理购方信息xml
	 *
	 * @param String,String
	 * @return List
	 */
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
				if (invType.equals("01")) {
					invType = "0";
				} else if (invType.equals("02")) {
					invType = "1";
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

	/**
	 * 处理交易信息xml
	 *
	 * @param String,String
	 * @return Map
	 */
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
			String InvoiceSfdy = "";// 是否打印
			SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			for (Element row : xntList) {
				// 分装对应的bean
				if (null != row.selectSingleNode("ClientNO") && !row.selectSingleNode("ClientNO").equals("")) {
					clientNO = row.selectSingleNode("ClientNO").getText();// 开票点代码
				}

				Map tt = new HashMap();
				tt.put("kpddm", clientNO);
				tt.put("gsdm", gsdm);
				Xf xf = jyxxsqService.findXfExistByKpd(tt);
				if (null != row.selectSingleNode("OrderNo") && !row.selectSingleNode("OrderNo").equals("")) {
					orderNo = row.selectSingleNode("OrderNo").getText();// 订单号
																		// 必选
				}
				// orderTime = (String) jyxxsqMap.get("OrderTime");// 订单日期 必选
				if (null != row.selectSingleNode("TotalAmount") && !row.selectSingleNode("TotalAmount").equals("")) {
					totalAmount = String.valueOf(row.selectSingleNode("TotalAmount").getText());// 计税合计
				}
				// 发票种类01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）
				if (null != row.selectSingleNode("InvType") && !row.selectSingleNode("InvType").equals("")) {
					invType = row.selectSingleNode("InvType").getText();
				}
				if (invType.equals("01")) {
					invType = "0";
				} else if (invType.equals("02")) {
					invType = "1";
				}

				if (null != row.selectSingleNode("TaxMark") && !row.selectSingleNode("TaxMark").equals("")) {
					taxMark = String.valueOf(row.selectSingleNode("TaxMark").getText());
				}

				if (null != row.selectSingleNode("InvoiceList") && !row.selectSingleNode("InvoiceList").equals("")) {
					invoiceList = row.selectSingleNode("InvoiceList").getText();
				}
				// 是否自动拆分
				if (null != row.selectSingleNode("InvoiceSplit") && !row.selectSingleNode("InvoiceSplit").equals("")) {
					invoiceSplit = row.selectSingleNode("InvoiceSplit").getText();
				}
				
				if (null != row.selectSingleNode("InvoiceSfdy") && !row.selectSingleNode("InvoiceSfdy").equals("")) {
					InvoiceSfdy = row.selectSingleNode("InvoiceSfdy").getText();
				}
				// 订单日期
				// orderDate = row.selectSingleNode("OrderDate").getText();
				// 征税方式
				if (null != row.selectSingleNode("ChargeTaxWay") && !row.selectSingleNode("ChargeTaxWay").equals("")) {
					chargeTaxWay = row.selectSingleNode("ChargeTaxWay").getText();
				}
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
				jyxxsq.setSfdy(InvoiceSfdy);
				jyxxsq.setSjly("1");
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
						String ProductCode = "";
						if (null != orderDetails.selectSingleNode("ProductCode")
								&& !orderDetails.selectSingleNode("ProductCode").equals("")) {
							ProductCode = orderDetails.selectSingleNode("ProductCode").getText();
						}
						jymxsq.setSpdm(ProductCode);

						// 商品名称
						String ProductName = "";
						if (null != orderDetails.selectSingleNode("ProductName")
								&& !orderDetails.selectSingleNode("ProductName").equals("")) {
							ProductName = orderDetails.selectSingleNode("ProductName").getText();
						}
						jymxsq.setSpmc(ProductName);
						jymxsq.setDdh(jyxxsq.getDdh());
						jymxsq.setHsbz(jyxxsq.getHsbz());
						// 发票行性质
						String RowType = "";
						if (null != orderDetails.selectSingleNode("RowType")
								&& !orderDetails.selectSingleNode("RowType").equals("")) {
							RowType = orderDetails.selectSingleNode("RowType").getText();
						}
						jymxsq.setFphxz(RowType);
						// 商品规格型号
						String Spec = "";
						if (null != orderDetails.selectSingleNode("Spec")
								&& !orderDetails.selectSingleNode("Spec").equals("")) {
							Spec = orderDetails.selectSingleNode("Spec").getText();
						}
						jymxsq.setSpggxh(Spec);
						// 商品单位
						String Unit = "";
						if (null != orderDetails.selectSingleNode("Unit")
								&& !orderDetails.selectSingleNode("Unit").equals("")) {
							Unit = orderDetails.selectSingleNode("Unit").getText();
						}
						jymxsq.setSpdw(Unit);
						// 商品数量
						String Quantity = "";
						if (null != orderDetails.selectSingleNode("Quantity")
								&& !orderDetails.selectSingleNode("Quantity").equals("")) {
							Quantity = orderDetails.selectSingleNode("Quantity").getText();
							jymxsq.setSps(Double.valueOf(Quantity));
						}

						// 商品单价
						String UnitPrice = "";
						if (null != orderDetails.selectSingleNode("UnitPrice")
								&& !orderDetails.selectSingleNode("UnitPrice").equals("")) {
							UnitPrice = orderDetails.selectSingleNode("UnitPrice").getText();
							jymxsq.setSpdj(Double.valueOf(UnitPrice));
						}

						// 商品金额
						String Amount = "";
						if (null != orderDetails.selectSingleNode("Amount")
								&& !orderDetails.selectSingleNode("Amount").equals("")) {
							Amount = orderDetails.selectSingleNode("Amount").getText();
							jymxsq.setSpje(Double.valueOf(Amount));
						}

						// 扣除金额
						String DeductAmount = "";
						if (null != orderDetails.selectSingleNode("DeductAmount")
								&& !orderDetails.selectSingleNode("DeductAmount").equals("")) {
							DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
							jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
									: Double.valueOf(DeductAmount));
						}

						String TaxRate = "";
						if (null != orderDetails.selectSingleNode("TaxRate")
								&& !orderDetails.selectSingleNode("TaxRate").equals("")) {
							TaxRate = orderDetails.selectSingleNode("TaxRate").getText();
							jymxsq.setSpsl(Double.valueOf(TaxRate));
						}

						String TaxAmount = "";
						if (null != orderDetails.selectSingleNode("TaxAmount")
								&& !orderDetails.selectSingleNode("TaxAmount").equals("")) {
							TaxAmount = orderDetails.selectSingleNode("TaxAmount").getText();
							jymxsq.setSpse(Double.valueOf(TaxAmount));
						}

						String MxTotalAmount = "";
						if (null != orderDetails.selectSingleNode("MxTotalAmount")
								&& !orderDetails.selectSingleNode("MxTotalAmount").equals("")) {
							MxTotalAmount = orderDetails.selectSingleNode("MxTotalAmount").getText();
							jymxsq.setJshj(Double.valueOf(MxTotalAmount));
						}

						jymxsq.setSpmxxh(spmxxh);

						String VenderOwnCode = "";
						if (null != orderDetails.selectSingleNode("VenderOwnCode")
								&& !orderDetails.selectSingleNode("VenderOwnCode").equals("")) {
							VenderOwnCode = orderDetails.selectSingleNode("VenderOwnCode").getText();
							jymxsq.setSpzxbm(VenderOwnCode);
						}

						String PolicyMark = "";
						if (null != orderDetails.selectSingleNode("PolicyMark")
								&& !orderDetails.selectSingleNode("PolicyMark").equals("")) {
							PolicyMark = orderDetails.selectSingleNode("PolicyMark").getText();
							jymxsq.setYhzcbs(PolicyMark);
						}

						String TaxRateMark = "";
						if (null != orderDetails.selectSingleNode("TaxRateMark")
								&& !orderDetails.selectSingleNode("TaxRateMark").equals("")) {
							TaxRateMark = orderDetails.selectSingleNode("TaxRateMark").getText();
							jymxsq.setLslbz(TaxRateMark);
						}

						String PolicyName = "";
						if (null != orderDetails.selectSingleNode("PolicyName")
								&& !orderDetails.selectSingleNode("PolicyName").equals("")) {
							PolicyName = orderDetails.selectSingleNode("PolicyName").getText();
							jymxsq.setYhzcmc(PolicyName);
						}

						jymxsq.setGsdm(gsdm);
						jymxsq.setLrry(lrry);
						jymxsq.setLrsj(new Date());
						jymxsq.setXgry(lrry);
						jymxsq.setXgsj(new Date());
						jymxsq.setYxbz("1");
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
		// return jyxxsqList;
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

	

}
