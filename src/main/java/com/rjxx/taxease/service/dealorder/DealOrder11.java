package com.rjxx.taxease.service.dealorder;

import com.rjxx.taxease.service.request.Request11;
import com.rjxx.taxease.service.result.InvoiceItem;
import com.rjxx.taxease.service.result.Invoices;
import com.rjxx.taxease.service.result.Result11;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.vo.KpcxjgVo;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-05-18.
 */
@Service
public class DealOrder11 implements IDealOrder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Override
    public String execute(String gsdm, String orderData, String Operation) {
        Result11 result11 = new Result11();
        try {
            Request11 request11 = XmlJaxbUtils.convertXmlStrToObject(Request11.class, orderData);
            Map params = new HashMap();
            if (StringUtils.isNotBlank(request11.getClientNO())) {
                params.put("kpddm", request11.getClientNO());
            }
            if (StringUtils.isNotBlank(request11.getFplxdm())) {
                params.put("fpzldm", request11.getFplxdm());
            }
            if (StringUtils.isNotBlank(request11.getSerialNumber())) {
                params.put("jylsh", request11.getSerialNumber());
            }
            if (StringUtils.isNotBlank(request11.getOrderNumber())) {
                params.put("ddh", request11.getOrderNumber());
            }
            if (StringUtils.isNotBlank(request11.getExtractCode())) {
                params.put("tqm", request11.getExtractCode());
            }
            if (params.isEmpty()) {
                result11.setReturnCode("9999");
                result11.setReturnMessage("请输入查询参数");
                return XmlJaxbUtils.toXml(result11);
            }
            params.put("gsdm", gsdm);
            List<KpcxjgVo> dataList = kplsService.findAllKpjgByMap(params);
            result11.setReturnCode("0000");
            result11.setTotalCount(dataList.size());
            if (dataList.size() == 0) {
                result11.setReturnMessage("没有查询结果");
                return XmlJaxbUtils.toXml(result11);
            }
            Invoices invoices = new Invoices();
            result11.setInvoices(invoices);
            List<InvoiceItem> itemList = new ArrayList<>(dataList.size());
            invoices.setInvoiceItem(itemList);
            invoices.setCount(dataList.size());
            for (KpcxjgVo kpcxjgVo : dataList) {
                InvoiceItem item = new InvoiceItem();
                itemList.add(item);
                if (kpcxjgVo.getHjje() != null) {
                    item.setAmount(new BigDecimal(kpcxjgVo.getHjje()).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                if ("05".equals(kpcxjgVo.getFpztdm())) {
                    item.setErrorMessage(kpcxjgVo.getErrorreason());
                }
                item.setExtractCode(kpcxjgVo.getTqm());
                item.setInvoiceCode(kpcxjgVo.getFpdm());
                item.setInvoiceNumber(kpcxjgVo.getFphm());
                if (kpcxjgVo.getKprq() != null) {
                    item.setInvoiceDate(DateFormatUtils.format(kpcxjgVo.getKprq(), "yyyy-MM-dd"));
                }
                item.setInvoiceStatus(kpcxjgVo.getFpztmc());
                item.setPdfUrl(kpcxjgVo.getPdfurl());
                if (kpcxjgVo.getHjse() != null) {
                    item.setTaxAmount(new BigDecimal(kpcxjgVo.getHjse()).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                item.setExtractCode(kpcxjgVo.getTqm());
            }
            return XmlJaxbUtils.toXml(result11);
        } catch (Exception e) {
            logger.error("", e);
            result11.setReturnCode("9999");
            result11.setReturnMessage(e.getMessage());
            return XmlJaxbUtils.toXml(result11);
        }
    }
}
