import com.alibaba.fastjson.JSONObject;
import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
import com.rjxx.taxeasy.bizcomm.utils.FphcService;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.vo.KplsVO5;
import com.rjxx.utils.TemplateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
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

}
