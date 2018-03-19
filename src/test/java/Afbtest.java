import com.alibaba.fastjson.JSON;
import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.GetLsvBz;
import com.rjxx.taxeasey.service.UploadInvoiceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;

/**
 * Created by xlm on 2017/11/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class Afbtest {


    @Autowired
    private UploadInvoiceService  uploadInvoiceService;
    @Autowired
    private GetLsvBz getLsvBz;
    @Test
    public void analysis(){

     String xml="<root>\n" +
             "<fpxx>\n" +
             "<SerialNo>ddbcee06256ab9996be1fcf3d296f4b8</SerialNo>\n" +
             "<OrderNo>17120700007</OrderNo>\n" +
             "<OrderDate>2017-12-08 09:03:29</OrderDate>\n" +
             "<ClientNo>afb_01</ClientNo>\n" +
             "<FPClientName>个人</FPClientName>\n" +
             "<FPClientTaxCode></FPClientTaxCode>\n" +
             "<FPClientBankAccount></FPClientBankAccount>\n" +
             "<FPClientAddressTel></FPClientAddressTel>\n" +
             "<FPSellerTaxCode>91310107566588632G</FPSellerTaxCode>\n" +
             "<FPSellerName>安付宝商务有限公司</FPSellerName>\n" +
             "<FPSellerBankAccount>中国银行上海市溧阳路支行 044023445560219230</FPSellerBankAccount>\n" +
             "<FPSellerAddressTel>上海市普陀区真光路1258号6楼608室 021-52629933</FPSellerAddressTel>\n" +
             "<FPNotes></FPNotes>\n" +
             "<FPInvoicer>周睿</FPInvoicer>\n" +
             "<FPChecker>邹瑾</FPChecker>\n" +
             "<FPCashier>周睿</FPCashier>\n" +
             "<TotalAmount>10</TotalAmount>\n" +
             "<PriceKind>0</PriceKind>\n" +
             "<Email>1193951186@qq.com</Email>\n" +
             "<FPListName></FPListName>\n" +
             "<InvQDState>0</InvQDState>\n" +
             "<fpmx count=\"1\">\n" +
             "<group xh=\"1\">\n" +
             "<MXGoodsName>预付卡</MXGoodsName>\n" +
             "<MXStandard></MXStandard>\n" +
             "<MXUnit></MXUnit>\n" +
             "<MXNumber>1</MXNumber>\n" +
             "<MXPrice>10</MXPrice>\n" +
             "<MXAmount>10</MXAmount>\n" +
             "<MXTaxRate>0</MXTaxRate>\n" +
             "<MXTaxAmount>0</MXTaxAmount>\n" +
             "</group>\n" +
             "</fpmx>\n" +
             "</fpxx>\n" +
             "</root>";


     //String result=uploadInvoiceService.callService2(xml);
        Map yhzcMap=getLsvBz.getLsvBz(0d,"1060201010700000000");
        System.out.println(JSON.toJSONString(yhzcMap));
    }
}
