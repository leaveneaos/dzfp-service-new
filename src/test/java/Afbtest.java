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

     String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
             "<root>\n" +
             "\t<fpxx>\n" +
             "\t\t<FPClientName>购方名称</FPClientName>\n" +
             "\t\t<FPClientTaxCode></FPClientTaxCode>\n" +
             "\t\t<FPClientBankAccount>购方开户行及账号</FPClientBankAccount>\n" +
             "\t\t<FPClientAddressTel>购方地址电话</FPClientAddressTel>\n" +
             "\t\t<FPSellerBankAccount>销方开户行及账号</FPSellerBankAccount>\n" +
             "\t\t<FPSellerAddressTel>销方地址电话</FPSellerAddressTel>\n" +
             "\t\t<FPNotes>备注</FPNotes>\n" +
             "\t\t<FPInvoicer>开票人</FPInvoicer>\n" +
             "\t\t<FPChecker>复核人</FPChecker>\n" +
             "\t\t<FPCashier>收款人</FPCashier>\n" +
             "\t\t<FPListName></FPListName>\n" +
             "\t\t<FPState>2</FPState>\n" +
             "\t\t<InvQDState>0</InvQDState>\n" +
             "\t\t\t<fpmx count=\"2\">\n" +
             "\t\t\t\t<group xh=\"1\">\n" +
             "\t\t\t\t\t<MXGoodsName>商品或劳务名称1</MXGoodsName>\n" +
             "\t\t\t\t\t<MXStandard>规格型号1</MXStandard>\n" +
             "\t\t\t\t\t<MXUnit>计</MXUnit>\n" +
             "\t\t\t\t\t<MXNumber>10</MXNumber>\n" +
             "\t\t\t\t\t<MXPrice>10</MXPrice>\n" +
             "\t\t\t\t\t<MXAmount>100</MXAmount>\n" +
             "\t\t\t\t\t<MXTaxRate>17</MXTaxRate>\n" +
             "\t\t\t\t\t<MXPriceKind>0</MXPriceKind>\n" +
             "\t\t\t\t\t<MXTaxAmount>10</MXTaxAmount>\n" +
             "\t\t\t\t</group>\n" +
             "\t\t\t\t<group xh=\"2\">\n" +
             "\t\t\t\t\t<MXGoodsName>商品或劳务名称2</MXGoodsName>\n" +
             "\t\t\t\t\t<MXStandard>规格型号2</MXStandard>\n" +
             "\t\t\t\t\t<MXUnit>计</MXUnit>\n" +
             "\t\t\t\t\t<MXNumber>10</MXNumber>\n" +
             "\t\t\t\t\t<MXPrice>10</MXPrice>\n" +
             "\t\t\t\t\t<MXAmount>100</MXAmount>\n" +
             "\t\t\t\t\t<MXTaxRate>17</MXTaxRate>\n" +
             "\t\t\t\t\t<MXPriceKind>0</MXPriceKind>\n" +
             "\t\t\t\t\t<MXTaxAmount>10</MXTaxAmount>\n" +
             "\t\t\t\t</group>\n" +
             "\t\t\t</fpmx>\n" +
             "\t</fpxx>\n" +
             "</root>\n";


     String result=uploadInvoiceService.callService2(xml);
    }
}
