import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
import com.rjxx.taxeasy.bizcomm.utils.FphcService;
import com.rjxx.taxeasy.dao.KplsJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.utils.alipay.AlipaySignUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

/**
 * Created by xlm on 2017/8/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class FphcTest {
    @Autowired
    private KplsService kplsService;
    @Autowired
    private JylsService jylsService;
    @Autowired
    private KpspmxService kpspmxService;
    @Autowired
    private CszbService cszbService;
    @Autowired
    private FpclService fpclService;
    @Autowired
    private FphcService fphcService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @Ignore
    public void fphc() {

        Map parms = new HashMap();
        parms.put("gsdm","cmsc");
        List<Kpls> kplsList = kplsService.findFphc(parms);
        Map result=new HashMap();
        for (int i = 0; i < kplsList.size(); i++) {
            try {
                Kpls kpls=kplsList.get(i);
                Map paramsMap=new HashMap();
                paramsMap.put("kplsh",kpls.getKplsh());
                List<Kpspmx> kpspmxList=  kpspmxService.findMxList(paramsMap);
                String ss="";
                String hcje="";
                for(int j=0;j<kpspmxList.size();j++){
                    Kpspmx kpspmx=kpspmxList.get(j);
                    ss+=kpspmx.getId()+",";
                    hcje+=kpspmx.getKhcje()+",";
                }
                fphcService.hccl(kpls.getKplsh(),kpls.getLrry(),kpls.getGsdm(),hcje,ss,"","");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Autowired
    private KplsJpaDao kplsJpaDao;
    @Autowired
    private AlipaySignUtil alipaySignUtil;

    @Test
    public void testSyncAlipay() {
        Kpls kpls = kplsJpaDao.findOneByDjh(1238339);
        Map paramsMap=new HashMap();
        paramsMap.put("kplsh",kpls.getKplsh());
        List<Kpspmx> kpspmxList=  kpspmxService.findMxList(paramsMap);
        try {
            String s = AlipaySignUtil.syncInvoiceAlipay("2018052800152005720000005511", kpls, kpspmxList, "STANDARD_INVOICE", "STANDARD_INVOICE");
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRefuse() {
        alipaySignUtil.refuse("ddh123","2018052800152005720000005511","税号位数错误！");
        System.out.println();
    }

}
