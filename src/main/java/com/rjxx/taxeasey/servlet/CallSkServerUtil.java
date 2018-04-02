package com.rjxx.taxeasey.servlet;

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

    private static final String xml ="<business id=\"FPKJ\" comment=\"发票开具\" version=\"2.0\">\n" +
            "    <REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\">\n" +
            "        <COMMON_FPKJ_FPT class=\"COMMON_FPKJ_FPT\">\n" +
            "            <FPQQLSH>4347</FPQQLSH>\n" +
            "            <KPLX>0</KPLX>\n" +
            "            <SGBZ>0</SGBZ>\n" +
            "            <HSBZ>0</HSBZ>\n" +
            "            <ZSFS>2</ZSFS>\n" +
            "            <XSF_NSRSBH>500102010003643</XSF_NSRSBH>\n" +
            "            <XSF_MC>上海百旺测试3643</XSF_MC>\n" +
            "            <XSF_DZDH>漕宝路　021-23443453</XSF_DZDH>\n" +
            "            <XSF_YHZH>建设银行　34567865435</XSF_YHZH>\n" +
            "            <GMF_NSRSBH>91370600050948561M</GMF_NSRSBH>\n" +
            "            <GMF_MC><![CDATA[个人]]></GMF_MC>\n" +
            "            <GMF_DZDH></GMF_DZDH>\n" +
            "            <GMF_YHZH></GMF_YHZH>\n" +
            "            <KPR>刘先生</KPR>\n" +
            "            <SKR>王五</SKR>\n" +
            "            <FHR>李四</FHR>\n" +
            "            <YFP_DM></YFP_DM>\n" +
            "            <YFP_HM></YFP_HM>\n" +
            "            <JSHJ>15.8</JSHJ>\n" +
            "            <HJJE>13.5</HJJE>\n" +
            "            <HJSE>2.3</HJSE>\n" +
            "            <KCE>10</KCE>\n" +
            "            <BZ>订单号：A001724293;订单日期：2017-08-15 16:58:21; 真的好开心呀 </BZ>\n" +
            "            <BMB_BBH>13.0</BMB_BBH>\n" +
            "        </COMMON_FPKJ_FPT>\n" +
            "        <COMMON_FPKJ_XMXXS size=\"1\" class=\"COMMON_FPKJ_XMXX\">\n" +
            "            <COMMON_FPKJ_XMXX>\n" +
            "                <FPHXZ>0</FPHXZ>\n" +
            "                <XMMC><![CDATA[Creamel凯利太妃味甜酒200ml]]></XMMC>\n" +
            "                <GGXH></GGXH>\n" +
            "                <DW></DW>\n" +
            "                <XMSL>1</XMSL>\n" +
            "                <XMDJ>13.5</XMDJ>\n" +
            "                <XMJE>13.5</XMJE>\n" +
            "                <SL>0.17</SL>\n" +
            "                <SE>2.3</SE>\n" +
            "                <SPBM>1030306000000000000</SPBM>\n" +
            "                <ZXBM></ZXBM>\n" +
            "                <YHZCBS>\n" +
            "                            0\n" +
            "                </YHZCBS>\n" +
            "                <LSLBS></LSLBS>\n" +
            "                <ZZSTSGL></ZZSTSGL>\n" +
            "            </COMMON_FPKJ_XMXX>\n" +
            "        </COMMON_FPKJ_XMXXS>\n" +
            "    </REQUEST_COMMON_FPKJ>\n" +
            "</business>";

    public static void main(String args[]){
        Map resultMap = new HashMap();
        String url = "http://datarj.imwork.net:24825/SKServer/SKDo";

        try {
            resultMap = httpPost(xml, url, "202016080" + "$" + "412216675", "500102010003643",
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
