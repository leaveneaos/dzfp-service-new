package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxeasy.bizcomm.utils.SaveOrderData;
import com.rjxx.taxeasy.domains.Jymxsq;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.domains.Yh;
import com.rjxx.taxeasy.service.JyxxsqService;
import com.rjxx.taxeasy.service.YhService;
import com.rjxx.utils.CheckOrderUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017-05-18.
 */
@Service
public class DealOrder02 implements IDealOrder {

    @Autowired
    private CheckOrderUtil checkorderutil;

    @Autowired
    private SaveOrderData saveorderdata;

    @Autowired
    private YhService yhservice;

    @Autowired
    private JyxxsqService jyxxsqService;

    @Override
    public String execute(String gsdm, String orderData, String Operation) {
        String result = "";
        // dealOperation01
        Map map = (Map) dealOperation02(gsdm, orderData);
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
        return result;
    }

    /**
     * 处理交易信息xml
     *
     * @param gsdm
     * @param OrderData
     * @return
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


}
