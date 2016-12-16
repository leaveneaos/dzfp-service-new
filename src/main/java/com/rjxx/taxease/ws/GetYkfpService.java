package com.rjxx.taxease.ws;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.vo.KplsVO3;
import com.rjxx.utils.ResponseUtils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@Service
public class GetYkfpService {
      
	@Autowired
	private GsxxService gsxxservice;
	
	@Autowired
	private KplsService  kplsservice;
	
    public String CallQuery(String AppKey, String Secret, String QueryData) {
        String result = "";
       
        Map tempMap = new HashMap();
        tempMap.put("appkey", AppKey);
        Gsxx gsxxBean = gsxxservice.findOneByParams(tempMap);
        if (gsxxBean == null) {
            return ResponseUtils.printFailure1("9060:" + AppKey + "," + Secret);
        } else {
            //int xfid = yhBean.get("yhjg");
            //XfBean xfBean = XfBean.dao.findFirst("select * from t_xf where xfid = " + xfid);
        	 String key = gsxxBean.getSecretKey();
             String signSourceData = "data=" + QueryData + "&key=" + key;
             String newSign =  DigestUtils.md5Hex(signSourceData);
             if(!Secret.equals(newSign)){
 		        result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>9060:签名不通过</ReturnMessage> \n</Responese>";
                 return result;
             }
            String gsdm = gsxxBean.getGsdm();
            OMElement root = null;
            try {
                root = xml2OMElement(QueryData);
            } catch (Exception e) {
                e.printStackTrace();
                result = ResponseUtils.printFailure1("9099:" + e.getMessage());
                return result;
            }
            Map rootMap = xml2Map(root, "Request");
            String serialNumber = (String) rootMap.get("SerialNumber");    // 交易流水号
            String OrderNumber = (String) rootMap.get("OrderNumber");      //订单号
            /*String BuyerName = (String) rootMap.get("BuyerName");          //购方名称
            String BuyerTel = (String) rootMap.get("BuyerTel");            //购方电话
*/
            String ExtractCode = (String) rootMap.get("ExtractCode");      //提取码
            Pagination pagination = new Pagination();
            pagination.addParam("jylsh", serialNumber);
            pagination.addParam("ddh", OrderNumber);
            pagination.addParam("tqm", ExtractCode);
            //pagination.addParam("gfdh", BuyerTel);
            pagination.addParam("gsdm", gsdm);
            List<KplsVO3> kplsList = kplsservice.findList2ByPagination(pagination);
            SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat for2 = new SimpleDateFormat ("yyyyMMddHHmmss"); 
            if (kplsList != null && kplsList.size() != 0) {
                result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>发票查询成功</ReturnMessage>\n  <TotalCount>"+kplsList.size()+"</TotalCount>\n"
                		+ "  <Invoices count=\""+ kplsList.size() + "\">\n";
                for (KplsVO3 kplsvo3 : kplsList) {
                	String kprq = String.valueOf(kplsvo3.getKprq());
                	Date kprqt = new Date();
                	
					try {
						if(null ==kprq || kprq.equals("") || kprq.equals("null")){
							kprq = "";
	                	}else{
	                		kprqt = formatter.parse(kprq);
	                		kprq = for2.format(kprqt);
	                	}
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String errorReason = kplsvo3.getErrorReason();
					if(null ==errorReason || errorReason.equals("") || errorReason.equals("null")){
						errorReason = "";
					}
					
					String fpztmc = kplsvo3.getFpztmc();
					if(null ==fpztmc || fpztmc.equals("") || errorReason.equals("null")){
						fpztmc = "";
					}
					
					String fpdm = kplsvo3.getFpdm();
					if(null ==fpdm || fpdm.equals("") || fpdm.equals("null")){
						fpdm = "";
					}
					
					String fphm = kplsvo3.getFphm();
					if(null ==fphm || fphm.equals("") || fphm.equals("null")){
						fphm = "";
					}
					
					String pdfurl = kplsvo3.getPdfurl();
					if(null ==pdfurl || pdfurl.equals("") || pdfurl.equals("null")){
						pdfurl = "";
					}
					
					String tqm = kplsvo3.getTqm();
					if(null ==tqm || tqm.equals("") || tqm.equals("null")){
						tqm = "";
					}
                    result += "    <InvoiceItem>\n\t<SerialNumber>" + kplsvo3.getJylsh() + "</SerialNumber>\n"
                    		+ "\t<OrderNumber>" + kplsvo3.getDdh() + "</OrderNumber>\n"
                    		+ "\t<InvoiceStatus>" + fpztmc + "</InvoiceStatus>\n"
                    		+ "\t<ErrorMessage>" + errorReason + "</ErrorMessage>\n"
                    		+ "\t<InvoiceCode>" + fpdm + "</InvoiceCode>\n"
                    	    + "\t<InvoiceNumber>" + fphm + "</InvoiceNumber>\n"
                    	    + "\t<InvoiceDate>" + kprq + "</InvoiceDate>\n"
                    	    + "\t<Amount>" + kplsvo3.getHjje() + "</Amount>\n"
                    	    + "\t<TaxAmount>" + kplsvo3.getHjse() + "</TaxAmount>\n"
                    	    + "\t<PdfUrl>" + pdfurl + "</PdfUrl>\n"
                    	    + "\t<ExtractCode>" + tqm + "</ExtractCode>\n  </InvoiceItem>\n";
                }
                result += "  </Invoices>\n</Responese>";
            } else {
                result = "<Responese>\n  <ReturnCode>0000</ReturnCode>\n  <ReturnMessage>没有查询到相关记录</ReturnMessage>\n<Responese>";
            }
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

    public static void main(String[] args) throws Exception {
        String QueryData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Request>\n" +
                "  <!--以下查询条件可任选-->\n" +
                "  <SerialNumber>2016082412444500008</SerialNumber>\n" +
                "  <!--SerialNumber可选，交易流水号String20-->\n" +
                "  <OrderNumber>ME24156080</OrderNumber>\n" +
                "  <!--OrderNumber可选，来源系统订单号，String20-->\n" +
                "  <BuyerName>胡义明</BuyerName>\n" +
                "  <!--BuyerName可选，购买方名称String100-->\n" +
                "  <BuyerTel>13912345678</BuyerTel>\n" +
                "  <!--BuyerTel可选，购买方电话String20-->\n" +
                "</Request>\n";
      /* GetYkfpService getykfpservice = new GetYkfpService();
       String result = getykfpservice.CallQuery("RJ9b458d60149c", "f97e385c9bfeec4d72fbecb8deb3ed5d", QueryData);
       System.out.println(result);*/
    }



}
