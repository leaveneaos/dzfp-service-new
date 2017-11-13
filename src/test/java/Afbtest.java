import com.rjxx.Application;
import com.rjxx.taxease.service.UploadInvoiceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by xlm on 2017/11/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class Afbtest {


    @Autowired
    private UploadInvoiceService  uploadInvoiceService;

    @Test
    public void analysis(){

     String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
             "<root>\n" +
             "\t<fpxx>\n" +
             "\t\t<SerialNo>201711131814166155</SerialNo><!--交易流水号，每次开票请求唯一，用于开票数据回写匹配-->\n" +
             "\t\t<OrderNo>171113004C1</OrderNo><!--业务订单号，主要用于业务人员识别该笔业务数据-->\n" +
             "\t\t<OrderDate>2017-11-13 18:14:16</OrderDate><!--业务订单时间-->\n" +
             "<FPClientName>上海孚晟压缩机净化设备有限公司</FPClientName>\n" +
             "\t\t<FPClientTaxCode>91310118630592370P</FPClientTaxCode>\n" +
             "\t\t<FPClientBankAccount>购方开户行及账号</FPClientBankAccount>\n" +
             "\t\t<FPClientAddressTel>购方地址电话</FPClientAddressTel>\n" +
             "\t\t<FPSellerTaxCode>500102010003643</FPSellerTaxCode>\n" +
             "\t\t<FPSellerName>上海百旺测试3643</FPSellerName>\n" +
             "\t\t<FPSellerBankAccount>销方开户行及账号</FPSellerBankAccount>\n" +
             "\t\t<FPSellerAddressTel>销方地址电话</FPSellerAddressTel>\n" +
             "\t\t<FPNotes>备注</FPNotes>\n" +
             "\t\t<FPInvoicer>于凤梅</FPInvoicer>\n" +
             "\t\t<FPChecker>方丽萍</FPChecker>\n" +
             "\t\t<FPCashier>于凤梅</FPCashier>\n" +
             "\t\t<TotalAmount>3000</TotalAmount>\n" +
             "\t\t<PriceKind>0</PriceKind>\n" +
             "\t\t<Email></Email>\n" +
             "\t\t<FPListName/>\n" +
             "\t\t<FPState>2</FPState>\n" +
             "\t\t<InvQDState>0</InvQDState>\n" +
             "\t\t<fpmx count=\"1\">\n" +
             "\t\t\t<group xh=\"1\">\n" +
             "\t\t\t\t<MXGoodsName>预付卡</MXGoodsName>\n" +
             "\t\t\t\t<MXStandard>规格型号1</MXStandard>\n" +
             "\t\t\t\t<MXUnit>计</MXUnit>\n" +
             "\t\t\t\t<MXNumber>1</MXNumber>\n" +
             "\t\t\t\t<MXPrice>3000</MXPrice>\n" +
             "\t\t\t\t<MXAmount>3000</MXAmount>\n" +
             "\t\t\t\t<MXTaxRate>0</MXTaxRate>\n" +
             "\t\t\t\t<MXTaxAmount>0</MXTaxAmount>\n" +
             "\t\t\t</group>\n" +
             "\t\t</fpmx>\n" +
             "\t</fpxx>\n" +
             "</root>\n";


     String result=uploadInvoiceService.callService2(xml);
    }
}
