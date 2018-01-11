package com.rjxx.taxease.servlet;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CallSkServerUtil {

    private static final String xml ="<?xml version=\"1.0\" encoding=\"GBK\"?>\n" +
            " <business id=\"FPKJ\" comment=\"发票开具\" version=\"2.0\">\n" +
            "     <REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\">\n" +
            "         <COMMON_FPKJ_FPT class=\"COMMON_FPKJ_FPT\">\n" +
            "             <FPQQLSH>2111203817</FPQQLSH>\n" +
            "             <KPLX>0</KPLX>\n" +
            "             <SGBZ>0</SGBZ>\n" +
            "             <HSBZ>0</HSBZ>\n" +
            "             <XSF_NSRSBH>500102010003699</XSF_NSRSBH>\n" +
            "             <XSF_MC>升级版测试用户3699</XSF_MC>\n" +
            "             <XSF_DZDH>山东省青岛市市北区黑龙江南路16、18号B2-15号　021-59895352</XSF_DZDH>\n" +
            "             <XSF_YHZH>中国光大银行股份有限公司青岛威海路支行　38220188000052025</XSF_YHZH>\n" +
            "             <GMF_NSRSBH>91370214682564742K</GMF_NSRSBH>\n" +
            "             <GMF_MC><![CDATA[青岛你和我超市管理有限公司]]></GMF_MC>\n" +
            "             <GMF_DZDH></GMF_DZDH>\n" +
            "             <GMF_YHZH></GMF_YHZH>\n" +
            "             <KPR>俞梦妮</KPR>\n" +
            "             <SKR>闫庆凡</SKR>\n" +
            "             <FHR>全明珠</FHR>\n" +
            "             <YFP_DM></YFP_DM>\n" +
            "             <YFP_HM></YFP_HM>\n" +
            "             <JSHJ>51</JSHJ>\n" +
            "             <HJJE>49.51</HJJE>\n" +
            "             <HJSE>1.49</HJSE>\n" +
            "             <BZ></BZ>\n" +
            "             <BMB_BBH>12.0</BMB_BBH>\n" +
            "         </COMMON_FPKJ_FPT>\n" +
            "         <COMMON_FPKJ_XMXXS size=\"1\" class=\"COMMON_FPKJ_XMXX\">\n" +
            "             <COMMON_FPKJ_XMXX>\n" +
            "                 <FPHXZ>0</FPHXZ>\n" +
            "                 <XMMC><![CDATA[餐饮服务]]></XMMC>\n" +
            "                 <GGXH></GGXH>\n" +
            "                 <DW></DW>\n" +
            "                 <XMSL></XMSL>\n" +
            "                 <XMDJ></XMDJ>\n" +
            "                 <XMJE>49.51</XMJE>\n" +
            "                 <SL>0.03</SL>\n" +
            "                 <SE>1.49</SE>\n" +
            "                 <SPBM>3070401000000000000</SPBM>\n" +
            "                 <ZXBM></ZXBM>\n" +
            "                 <YHZCBS>0</YHZCBS>\n" +
            "                 <LSLBS></LSLBS>\n" +
            "                 <ZZSTSGL></ZZSTSGL>\n" +
            "             </COMMON_FPKJ_XMXX>\n" +
            "         </COMMON_FPKJ_XMXXS>\n" +
            "     </REQUEST_COMMON_FPKJ>\n" +
            " </business>";

    public static void main(String args[]){
        Map resultMap = new HashMap();
        String url = "http://192.168.1.123:8080/SKServer/SKDo";

        try {
            resultMap = httpPost(xml, url, "202016080" + "$" + "4182216675", "500102010003698",
                   "JY20171110100554712");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Map httpPost(String sendMes, String url, String key, String xfsh, String jylsh) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "text/xml");
        String strMessage = "";
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        Map resultMap = null;
        try {
            StringEntity requestEntity = new StringEntity(sendMes, "GBK");
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                        + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                reader = new BufferedReader(new InputStreamReader(entity.getContent(), "gbk"));
                while ((strMessage = reader.readLine()) != null) {
                    buffer.append(strMessage);
                }
            }
            System.out.println("接收返回值:" + buffer.toString());
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultMap;
    }

}
