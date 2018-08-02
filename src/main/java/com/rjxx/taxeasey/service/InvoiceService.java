package com.rjxx.taxeasey.service;


import com.rjxx.taxeasy.bizcomm.utils.SkOperationService;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.SpvoService;
import com.rjxx.taxeasy.service.XfService;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rjxx.taxeasey.service.result.DefaultResult;
import com.rjxx.taxeasy.bizcomm.utils.DataOperte;
import com.rjxx.taxeasy.bizcomm.utils.GetYjnr;
import com.rjxx.taxeasy.bizcomm.utils.SendalEmail;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.utils.CheckUtil;
import com.rjxx.utils.GlobsAttributes;
import com.rjxx.utils.TemplateUtils;
import com.rjxx.utils.TimeUtil;
import com.rjxx.utils.XmlJaxbUtils;
import sun.misc.BASE64Encoder;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * 发票开具
 *
 * @author Administrator
 */
@Service("invoiceService")
public class InvoiceService {
	 
	 public InvoiceService(){
		 
	 }
	 
	 @Autowired
	 private SkpService skpservice;
	 
	 @Autowired
	 private DataOperte dataoperte;
	 
	 @Autowired
	 private GsxxService gsxxService;
	 
	 @Autowired
	 private DealOrderDataService dealOrderDataService;
	 
	 @Autowired
     private SendalEmail sendalEmail;
	 
	 @Autowired
     private XfService xfService;
	 
	 @Autowired
     private SpvoService spvoService;

	 @Autowired
	 private SkOperationService skOperationService;
	 
	 private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 单张电子发票开具接口
     *
     * @param method 调用方法名：此处应为“invoiceUpload”，仅作区分
     * @param in0    调用方法值：请求的xml报文
     * @return
     */
	 @Transactional
    public  String invoiceUpload(String method, String in0) {

        //ActiveRecordHelper.start();
        /**************************************/
        // A&F 配置的默认信息 (获取销方信息，商品信息)    
		Xf param = new Xf();
		param.setGsdm("af");
		Xf xf = xfService.findOneByParams(param);
		
		Spvo spvo = (Spvo) ((List)spvoService.findAllByGsdm("af")).get(0);
		
        //PropKit.use("attr.properties");
        final String Seller_Identifier = xf.getXfsh();
        final String Seller_Name = xf.getXfmc();
        final String TelephoneNumberDEfault = xf.getXfdh();
        final String Drawer = xf.getKpr();
        final String Seller_BankAcc = xf.getXfyh();
        final String Payee = xf.getSkr();
        final String Reviewer = xf.getFhr();
        final String TaxRate = String.valueOf(spvo.getSl());
        final String description = spvo.getSpmc();
        final String unit = spvo.getSpdw();
        final String quantityStr = "1";
        final String hsbz = "1";
        final int Xgry = xf.getXgry();
        final int Lrry = xf.getLrry();
        final String productCode = spvo.getSpbm();
        final String fsyj = this.readFile("FSYJ");
        //final String ewm = this.readFile("ewm");
        final String tqlj = this.readFile("tqlj");
        /**************************************/
        OMElement root;
        final Map rootMap;
        final String SerialNumber;
        String SerialNumber2 = null;
        try {
            root = xml2OMElement(in0);
            rootMap = xml2Map(root, "DETAILS");
            SerialNumber = (String) rootMap.get("SERIALNUMBER");
            SerialNumber2 = SerialNumber;
            if (root == null || rootMap == null || SerialNumber == null) {
            	dataoperte.saveLog(0, "61", "1", "InvoiceService:xml2OMElement", "电子发票平台解析请求报文失败或报文有误", Lrry, Seller_Identifier, SerialNumber);
                return GlobsAttributes.ERROR_9032;
            }
            dataoperte.saveLog(0, "61", "0", "InvoiceService:xml2OMElement", null, Lrry, Seller_Identifier, SerialNumber);
        } catch (Exception e) {
        	dataoperte.saveLog(0, "61", "1", "InvoiceService:xml2OMElement", "电子发票平台解析请求报文失败或报文有误", Lrry, Seller_Identifier, SerialNumber2);
            return GlobsAttributes.ERROR_9032;
        }

        /****************/
        //保存请求xml     
        try {
        	dataoperte.saveXml(Seller_Identifier, SerialNumber, in0);
            dataoperte.saveLog(0, "60", "0", "InvoiceService:saveXml", null, Lrry, Seller_Identifier, SerialNumber);
        } catch (Exception e) {
        	dataoperte.saveLog(0, "60", "1", "InvoiceService:saveXml", "电子发票平台保存请求报文失败：" + e, Lrry, Seller_Identifier, SerialNumber);
            return GlobsAttributes.ERROR_9030;
        }
        final Map resultMap = new HashMap();
                try {
                    /****************/
                    String TotalAmount = (String) rootMap.get("TOTALAMOUNT");
                    Map Main = (Map) rootMap.get("MAIN");
                    // 订单信息
                    String OrderNumber = (String) Main.get("ORDERNUMBER");
                    String OrderDateStr = (String) Main.get("ORDERDATE");
                    Date OrderDate = TimeUtil.getSysDateInDate(OrderDateStr, "yyyy-mm-dd");
                    // 卖方信息
                    Map seller = (Map) Main.get("SELLER");
                    /*String Seller_Identifier = (String) seller.get("IDENTIFIER");
                    String Seller_Name = (String) seller.get("NAME");*/
                    String Seller_Address = (String) seller.get("ADDRESS");
                    String Seller_TelephoneNumber = (String) seller.get("TELEPHONENUMBER");

                    // 买方信息
                    Map Buyer = (Map) Main.get("BUYER");
                    String Buyer_Identifier = (String) Buyer.get("IDENTIFIER");
                    String Buyer_Name = (String) Buyer.get("NAME");
                    String Buyer_Address = (String) Buyer.get("ADDRESS");
                    String Buyer_TelephoneNumber = (String) Buyer.get("TELEPHONENUMBER");
                    String Buyer_BankAcc = (String) seller.get("YHZH");
                    // String Payment = (String) Buyer.get("PAYMENT");//支付方式
                    String IsSend = (String) Buyer.get("ISSEND");
                    String Email = (String) Buyer.get("EMAIL");

                    String buyerAddress = "";
                    String buyerCode = "";
                    String buyerPerson = "";

                    if (Buyer_Address != null && !Buyer_Address.trim().equals("")) {
                        String[] strs = Buyer_Address.split(";;;;");
                        if (strs.length >= 3) {
                            buyerAddress = strs[0];
                            buyerCode = strs[1];
                            buyerPerson = strs[2];
                        } else {
                            buyerAddress = Buyer_Address;
                        }
                    }

                    // 为A&F设置默认电话号码
                    if (null == Seller_TelephoneNumber || "".equals(Seller_TelephoneNumber)) {
                        Seller_TelephoneNumber = TelephoneNumberDEfault;
                    }
                    List Details = (List) rootMap.get("DETAILS");
                    /****************************/
                    //校验企业发送的报文数据
                    String check = CheckUtil.checkAll(SerialNumber, Main, seller, Buyer, Seller_Identifier, Seller_Name, Seller_Address, Seller_TelephoneNumber,
                            IsSend, Email, Buyer_Name, description, TaxRate, Details, quantityStr);
                    if (check != "" && !"".equals(check)) {
                    	dataoperte.saveLog(0, "01", "1", "CheckUtil:checkAll " + check, "报文数据格式有误", Lrry, Seller_Identifier, SerialNumber);
                        //resultMap.put("result", check);
                    	return check;
                        //return false;
                    }
                    /****************************/
                    //yi下为报文保存数据
                    Jyls iurb = new Jyls(); //主表
                    String clzt = "01";
                    String fpzl = "12";//全部改为电子票
                    /*if ("0".equals(IsSend) || IsSend == "0") {
                        clzt = "99";//纸质开具
                        fpzl = "02";//增普
                    } else if ("2".equals(IsSend) || IsSend == "2") {
                        clzt = "97";//纸质开具
                        fpzl = "02";//增普
                    } else if ("3".equals(IsSend) || IsSend == "3") {
                        clzt = "98";//纸质开具
                        fpzl = "02";//增普
                    }*/
                    //20161114 kzx  接口接入数据加入xfid和skpid的维护
                    Map params2 = new HashMap();
            		params2.put("gsdm", "af");
            		Skp skp = skpservice.findOneByParams(params2);
            		int xfid = skp.getXfid();
            		int skpid = skp.getId();
            		String kpddm = skp.getKpddm();
                    Jyspmx iurdb = new Jyspmx();//明细表
                    // 保存 发票表
                    Double total = 0.00;
                    String amountStr;
                    Map detailt1;
                    for (int i = 0, l = Details.size(); i < l; i++) {
                    	detailt1 = (Map) Details.get(i);
                    	amountStr = (String) detailt1.get("AMOUNT");
                    	total += Double.valueOf(amountStr);
                    	
                    }
                    iurb.setJylsh(SerialNumber);//交易流水号 包括所有需要开票和不需要开票的全部交易流水
                    iurb.setFpzldm(fpzl);  //发票种类代码
                    iurb.setFpczlxdm("11");  //发票操作类型代码
                    iurb.setDdh(OrderNumber); //订单号
                    iurb.setJylssj(OrderDate);//交易流水时间
                    iurb.setXfmc(Seller_Name); /*"爱芙趣商贸（上海）有限公司"*///销方名称
                    iurb.setXfsh(Seller_Identifier);/*"310106550096887"*///销方税号  非空
                    iurb.setXfyh("-");//销方银行
                    iurb.setXfyhzh("-");//销方银行账号
                            //.set("xflxr", "")//销方联系人
                    iurb.setXfdz("-");//销方地址
                    iurb.setXfdh("-");
                    iurb.setSffsyj(IsSend);//0纸质开具 仅作存库 1发送邮件
                    iurb.setGfmc(Buyer_Name);//购方名称
                    iurb.setGfsh(Buyer_Identifier);//购方税号
                    iurb.setGfyhzh(Buyer_BankAcc);//购方银行账号
                    iurb.setGfdz(buyerAddress);//购方地址
                    iurb.setGfdh(Buyer_TelephoneNumber);                            //.set("gfyb", "")//购方邮编
                    iurb.setGfemail(Email);//邮箱地址
                    iurb.setClztdm(clzt); //电子发票处理状态代码  等同之前的flag
                    iurb.setBz("订单号：" + OrderNumber);    //备注
                    iurb.setKpr(Drawer); // 开票人
                    iurb.setSkr(Payee); // 收款人
                    iurb.setFhr(Reviewer);
                    iurb.setJshj(total);
                    iurb.setYkpjshj(0.00);
                    iurb.setSsyf(TimeUtil.getSysDateString());// 所属月份 6位
                    iurb.setHsbz(hsbz);
                    iurb.setYxbz("1");
                    iurb.setLrsj(new Date());//录入日期
                    iurb.setLrry(Lrry);
                    iurb.setXgsj(new Date());
                    iurb.setXgry(Xgry);
                    iurb.setGfyb(buyerCode); //购方邮编
                    iurb.setGfsjr(buyerPerson); //购方收件人
                    iurb.setGsdm("af");
                    iurb.setXfid(xfid);
                    iurb.setSkpid(skpid);
                    //jylsservice.save(iurb);
                 /*   List<Jyls> iurbs = iurb.find("select * from t_jyls where jylsh ='" + SerialNumber + "'");
                    iurb = iurbs.get(iurbs.size() - 1);*/
                    // 保存发票明细信息表
                  
                    Map detail;
                    String unitPriceStr;
                    List<Jyspmx> iurdbList = new ArrayList<Jyspmx>();
                    for (int i = 0, l = Details.size(); i < l; i++) {
                        detail = (Map) Details.get(i);
                        unitPriceStr = (String) detail.get("UNITPRICE");
                        amountStr = (String) detail.get("AMOUNT");

                        iurdb = new Jyspmx();  
                        iurdb.setDjh(iurb.getDjh());
                        iurdb.setSpmxxh(i + 1); 
                        iurdb.setFphxz("0");
                        iurdb.setSpdm(productCode);
                        iurdb.setSpmc(description); //商品名称
                        iurdb.setSpdw(unit);//商品单位
                        iurdb.setSps(null);//商品数量
                        iurdb.setSpdj(null);//商品单价
                        iurdb.setSpje(Double.valueOf(amountStr));//商品
                        iurdb.setSpsl(Double.valueOf(TaxRate));//商品税率
                        iurdb.setSpse(0.00);//商品税额
                        iurdb.setJshj( Double.valueOf(amountStr) * Double.valueOf(quantityStr));
                        iurdb.setYkphj(0.00);
                        iurdb.setLrsj(new Date());//录入时间
                        iurdb.setLrry(Lrry);//录入人员
                        iurdb.setXgsj(new Date());//修改时间
                        iurdb.setXgry(Xgry);
                        iurdb.setGsdm("af");
                        iurdb.setXfid(xfid);
                        iurdb.setSkpid(skpid);
                        
                        iurdbList.add(iurdb);
                        //jyspmxservice.save(iurdb);
                        
                        //total += Double.valueOf(amountStr);
                    }
                    //iurb.set("jshj", total).update();
                    //保存报文数据结束
                   String result = callDealOrderDataService(iurb,iurdbList,kpddm);
       			   DefaultResult defaultResult = new DefaultResult();
       			   defaultResult = XmlJaxbUtils.convertXmlStrToObject(DefaultResult.class, result);
                    //如果fsyj为1表示发送邮件
                    //20180802注释af邮件通知。
                    /*if(fsyj.equals("1") && defaultResult.getReturnCode().equals("0000")){
                    	GetYjnr yjnrF = new GetYjnr();
                        *//*String path = this.getClass().getClassLoader().getResource("/af.jpg")
                                .getPath();*//*
                        String path =  InvoiceService.class.getClassLoader().getResource("/template/af.jpg").getPath();
                        String ewm ="data:image/jpeg;base64,"+getImageStr(URLDecoder.decode(path, "UTF-8"));
                    	String yjnr = yjnrF.getAfEmail(ewm, tqlj);
                    	sendalEmail.sendEmail("999999", iurb.getGsdm(), iurb.getGfemail(), "爱芙趣发票提取",
                    			"999999", yjnr, "电子发票","0");
                    }*/
                    /*************************************************/
                    //向爱芙趣返回存库情况
                    //String resXml = invoiceRtnXml(iurb);
                    //保存处理状态至日志里
                    //dataoperte.saveLog(iurb.getDjh(), clzt, "0", "invoiceUpload 接收电子发票待开票数据完成", null, Lrry, Seller_Identifier, SerialNumber);
                   /* resultMap.put("result", "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Responese>\n  <ReturnCode>0000</ReturnCode>\n" +
                            "  <Djh>" + iurb.getDjh() + "</Djh>\n  <ReturnMessage>待开票数据保存成功</ReturnMessage>\n</Responese>");*/
                    //return true;
                    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Responese>\n  <ReturnCode>0000</ReturnCode>\n" +
                    "  <Djh>" + iurb.getDjh() + "</Djh>\n  <ReturnMessage>待开票数据保存成功</ReturnMessage>\n</Responese>";
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Responese>\n  <ReturnCode>9999</ReturnCode>\n" +
                            "<ReturnMessage>9099:" + e.getMessage() + "</ReturnMessage>\n</Responese>");
      /*              resultMap.put("result", "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Responese>\n  <ReturnCode>9999</ReturnCode>\n" +
                            "  <Djh></Djh>\n  <ReturnMessage>9099:" + e.getMessage() + "</ReturnMessage>\n</Responese>");*/
                    //return false;
                }

        //String result = (String) resultMap.get("result");
        //return result;
    }
	 
	private static String getSign(String QueryData,String key){
			String signSourceData = "data=" + QueryData + "&key=" + key;
			String newSign =  DigestUtils.md5Hex(signSourceData);
			return newSign;
	}
	 
	 public String callDealOrderDataService(Jyls iurb,List<Jyspmx> mxList,String kpddm) {
		 logger.debug("爱芙趣kpddm:" + kpddm);
         String path = this.getClass().getClassLoader().getResource("AFToFpkj.xml")
                 .getPath();
         String result = "";
         try {
			path = URLDecoder.decode(path, "UTF-8");
			File templateFile = new File(path);
	         String jylssj = TimeUtil.formatDate(null == iurb.getJylssj()? new Date():iurb.getJylssj(), "yyyy-MM-dd HH:mm:ss");
	         Map params2 = new HashMap();
	         params2.put("iurb", iurb);
	         params2.put("kpddm", kpddm);
	         params2.put("jylssj", jylssj);
	         params2.put("mxList", mxList);
	         params2.put("count", mxList.size());
	         String result2 = TemplateUtils.generateContent(templateFile, params2, "utf-8");
	         //System.out.println(result2);
	         logger.debug("爱芙趣InvoiceService调用DealOrderDataService传入报文：" + result2);
	         Map params = new HashMap();
	 		 params.put("gsdm", "af");
	 		 Gsxx gsxx = gsxxService.findOneByGsdm(params);
	 		 String AppId = gsxx.getAppKey();
	 		 String key = gsxx.getSecretKey();
	 		 String Sign = getSign(result2,key);
	 		 result = dealOrderDataService.dealOrder(AppId, Sign, "01", result2);
	        
	         //输出调用结果
	         //System.out.println("爱芙趣InvoiceService调用DealOrderDataService返回结果：" + result);
	         logger.debug("爱芙趣InvoiceService调用DealOrderDataService返回结果：" + result);
	         return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DefaultResult defaultResult = new DefaultResult();
		    defaultResult.setReturnCode("9999");
		    defaultResult.setReturnMessage("调用接口异常");
		    return XmlJaxbUtils.toXml(defaultResult);	
		}
       
	 }
	 
    /**
     * 构造对接收到的爱芙趣待开票数据存库情况的返回报文
     *
     * @return
     * @throws XMLStreamException
     */
    /*public static String invoiceRtnXml(Jyls iurb) throws XMLStreamException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMDocument doc = factory.createOMDocument();
        doc.setCharsetEncoding("utf-8");

        OMElement Response = factory.createOMElement(new QName("Response"));
        OMElement ReturnCode = factory.createOMElement(new QName("ReturnCode"));
        OMElement ReturnMessage = factory.createOMElement(new QName("ReturnMessage"));

        if (iurb.getDjh() != null) {
            ReturnCode.setText("0000");
            ReturnMessage.setText("待开票数据保存成功");
        } else {
            ReturnCode.setText("9999");
            ReturnMessage.setText("待开票数据保存失败");
        }
        Response.addChild(ReturnCode);
        Response.addChild(ReturnMessage);

        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(sw);
        XMLStreamWriter encoder = new XOPEncodingStreamWriter(writer,
                ContentIDGenerator.DEFAULT, OptimizationPolicy.DEFAULT);
        Response.serialize(encoder);
        return sw.toString();
    }*/

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
            tagName = str2Trim(node.getLocalName()).toUpperCase();
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
        return str == "" ? null : str.trim();
    }
    
   /* public static void main(String args[]){
    	InvoiceService t  = new InvoiceService();
    	System.out.println(t.readFile("Reviewer"));
    }*/


    /**
     * @Description: 根据图片地址转换为base64编码字符串
     * @Author:
     * @CreateTime:
     * @return
     */
    public static String getImageStr(String imgFile) {
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 加密
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

   /* public  static void main(String args[]){
        InvoiceService t = new InvoiceService();
        //t.getImageStr(this.getClass().getClassLoader());
        System.out.println(InvoiceService.class.getResource("/").getPath()+"kzx");

    }*/

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

    public String cb(Fwqxx fwqxx) {
        String result=null;
        try{
            result=skOperationService.Cb(fwqxx);
        }catch (Exception e){
            result=e.getMessage();
        }
       return result;
    }

    public String fx(Fwqxx fwqxx) {
        String result=null;
        try{
            result=skOperationService.Fx(fwqxx);
        }catch (Exception e){
            result=e.getMessage();
        }
        return result;
    }
}
