package com.rjxx.taxease.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Administrator on 2017-05-18.
 */
public class XmlMapUtils {

    public static OMElement xml2OMElement(String xml) throws XMLStreamException, UnsupportedEncodingException {
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
        StAXBuilder builder = new StAXOMBuilder(xmlStream);
        OMElement documentElement = builder.getDocumentElement();
        return documentElement;
    }


    public static Map xml2Map(OMElement doc, String listTagNames) {
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

    public static List xml2List(OMElement doc, String listTagNames) {
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

}
