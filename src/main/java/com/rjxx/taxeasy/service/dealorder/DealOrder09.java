package com.rjxx.taxeasy.service.dealorder;

import com.rjxx.taxeasy.service.request.Request09;
import com.rjxx.taxeasy.service.result.DefaultResult;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.bizcomm.utils.SkService;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlJaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-05-23.
 */
@Service("dealOrder09")
public class DealOrder09 implements IDealOrder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SkService skService;

    @Autowired
    private KplsService kplsService;

    @Override
    public String execute(String gsdm, String orderData, String Operation) {
        DefaultResult defaultResult = new DefaultResult();
        try {
            Request09 request09 = XmlJaxbUtils.convertXmlStrToObject(Request09.class, orderData);
            Map params = new HashMap();
            if (StringUtils.isBlank(request09.getInvCode()) || StringUtils.isBlank(request09.getInvNo())) {
                defaultResult.setReturnCode("9999");
                defaultResult.setReturnMessage("发票代码或发票号码不能为空");
                return XmlJaxbUtils.toXml(defaultResult);
            }
            params.put("fpdm", request09.getInvCode());
            params.put("fphm", request09.getInvNo());
            params.put("gsdm", gsdm);
            Kpls kpls = kplsService.findOneByParams(params);
            if (kpls == null) {
                defaultResult.setReturnCode("9999");
                defaultResult.setReturnMessage("没有该笔数据");
                return XmlJaxbUtils.toXml(defaultResult);
            }
            InvoiceResponse invoiceResponse = skService.reprintInvoice(kpls.getKplsh());
            defaultResult.setReturnCode(invoiceResponse.getReturnCode());
            defaultResult.setReturnMessage(invoiceResponse.getReturnMessage());
            return XmlJaxbUtils.toXml(defaultResult);
        } catch (Exception e) {
            logger.error("", e);
            defaultResult.setReturnCode("9999");
            defaultResult.setReturnMessage(e.getMessage());
            return XmlJaxbUtils.toXml(defaultResult);
        }
    }

}
