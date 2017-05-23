package com.rjxx.taxease.service.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Administrator on 2017-05-22.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Invoices {

    @XmlAttribute
    private int count;

    private List<InvoiceItem> InvoiceItems;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<InvoiceItem> getInvoiceItems() {
        return InvoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItem> invoiceItems) {
        InvoiceItems = invoiceItems;
    }
}
