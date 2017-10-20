import com.alibaba.fastjson.JSONObject;
import com.rjxx.Application;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xlm on 2017/8/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class FpkjTest {
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void fphc() {
        List<String> ss = new ArrayList<>();
        //ss.add("43948560");
        ss.add("43946796");
        ss.add("43947340");
        ss.add("43946736");
        ss.add("43947333");
        ss.add("43947322");
        ss.add("43946713");
        ss.add("43947309");
        ss.add("43946710");
        ss.add("43947300");
        ss.add("43946697");
        ss.add("43946693");
        ss.add("43947286");
        ss.add("43947271");
        ss.add("43957852");

        Map result=new HashMap();
        for (int i = 0; i < ss.size(); i++) {
            Map parms = new HashMap();
            parms.put("fphm",ss.get(i));
            Kpls kpls = kplsService.findOneByParams(parms);
            try {
                Map resultMap=this.findjyxxsq(kpls.getKplsh());
                Jyxxsq jyxxsq=(Jyxxsq)resultMap.get("jyxxsq");
                List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
                kpls.setHzyfpdm(kpls.getFpdm());
                kpls.setHzyfphm(kpls.getFphm());
                kpls.setFpdm("");
                kpls.setFphm("");
                kpls.setJshj(-kpls.getJshj());
                kpls.setHjje(-kpls.getHjje());
                kpls.setHjse(-kpls.getHjse());

                Map params = new HashMap();
                params.put("kplsh", kpls.getKplsh());
                List<Kpspmx> kpspmxList = kpspmxService.findMxList(params);

                kpls.setKplsh(null);

                double hjje = 0.00;
                double hjse = 0.00;


                kplsService.save(kpls);
                for (int j = 0; j < kpspmxList.size(); j++) {
                    Kpspmx  kpspmx= kpspmxList.get(j);
                    hjje = hjje + kpspmx.getSpje();
                    hjse = hjse + kpspmx.getSpse();
                    kpspmx.setSps(-kpspmx.getSps());
                    kpspmx.setSpje(-kpspmx.getSpje());
                    kpspmx.setSpse(-kpspmx.getSpse());
                    kpspmx.setKplsh(kpls.getKplsh());
                }
                kpspmxService.save(kpspmxList);
                KplsVO5 kplsVO5 = new KplsVO5(kpls, jyxxsq);
                kplsVO5.setZsfs("0");

                Map params2 = new HashMap();
                params2.put("kplx", "1");
                Cszb cszb = cszbService.getSpbmbbh(kplsVO5.getGsdm(), kplsVO5.getXfid(), null, "spbmbbh");
                String spbmbbh = cszb.getCsz();

                params.put("spbmbbh",spbmbbh);
                params2.put("kpls", kplsVO5);
                params2.put("kpspmxList", kpspmxList);
                params2.put("mxCount", kpspmxList.size());
                params2.put("hjje", -hjje);
                params2.put("hjse", -hjse);
                /**
                 * 模板名称，电子票税控服务器报文
                 */
                String templateName = "dzfp-xml.ftl";
                String result2 = TemplateUtils.generateContent(templateName, params2);
                System.out.println(result2);
                logger.debug("封装传开票通的报文" + result2);
                String url = "http://210.14.78.228:7090/SKServer/SKDo";
                result = fpclService.DzfphttpPost(result2, url, kplsVO5.getDjh() + "$" + kplsVO5.getKplsh(), kplsVO5.getXfsh(),
                        kplsVO5.getJylsh());
                String  serialorder=fpclService.updateKpls(result);
                String  resultxml=serialorder;
                logger.debug("封装传开票通的返回报文" + JSONObject.toJSONString(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Map findjyxxsq(Integer kplsh) throws Exception {
        Map result = new HashMap();
        Kpls kpls = kplsService.findOne(kplsh);
        Jyxxsq jyxxsq = new Jyxxsq();
        String jylsh = "JY" + new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        jyxxsq.setJylsh(jylsh);
        jyxxsq.setDdh(jylsh);
        jyxxsq.setGflxr(kpls.getGflxr());
        jyxxsq.setGfyb(kpls.getGfyb());
        jyxxsq.setBz(kpls.getBz());
        jyxxsq.setClztdm("00");
        jyxxsq.setDdrq(new Date());
        jyxxsq.setFpczlxdm("14");
        jyxxsq.setFpzldm(kpls.getFpzldm());
        jyxxsq.setFhr(kpls.getFhr());
        jyxxsq.setGfdh(kpls.getGfdh());
        jyxxsq.setGfdz(kpls.getGfdz());
        jyxxsq.setGfemail(kpls.getGfemail());
        jyxxsq.setGfid(kpls.getGfid());
        jyxxsq.setGfmc(kpls.getGfmc());
        jyxxsq.setGfsh(kpls.getGfsh());
        jyxxsq.setGsdm(kpls.getGsdm());
        jyxxsq.setGfyh(kpls.getGfyh());
        jyxxsq.setGfyhzh(kpls.getGfyhzh());
        jyxxsq.setXfdh(kpls.getXfdh());
        jyxxsq.setXfdz(kpls.getXfdz());
        jyxxsq.setXfid(kpls.getXfid());
        jyxxsq.setXflxr(kpls.getXflxr());
        jyxxsq.setXgsj(new Date());
        jyxxsq.setXfmc(kpls.getXfmc());
        jyxxsq.setXfsh(kpls.getXfsh());
        jyxxsq.setXfyb(kpls.getXfyb());
        jyxxsq.setXfyh(kpls.getXfyh());
        jyxxsq.setXfyhzh(kpls.getXfyhzh());
        jyxxsq.setYxbz("1");
        jyxxsq.setYkpjshj(0d);
        jyxxsq.setLrsj(new Date());
        jyxxsq.setJshj(kpls.getJshj());
        jyxxsq.setHztzdh(kpls.getHztzdh());
        jyxxsq.setKpddm(kpls.getKpddm());
        jyxxsq.setZtbz("3");
        jyxxsq.setXgsj(kpls.getXgsj());
        jyxxsq.setSkr(kpls.getSkr());
        jyxxsq.setSkpid(kpls.getSkpid());
        jyxxsq.setKpr(kpls.getKpr());
        jyxxsq.setYfpdm(kpls.getFpdm());
        jyxxsq.setYfphm(kpls.getFphm());
        jyxxsq.setLrry(kpls.getLrry());
        jyxxsq.setSfdyqd(kpls.getSfdyqd());
        jyxxsq.setHsbz("1");
        result.put("jyxxsq", jyxxsq);
        Map parms = new HashMap();
        parms.put("kplsh", kpls.getKplsh());
        List<Kpspmx> kpspmxList = kpspmxService.findMxList(parms);
        List<Jymxsq> jymxsqList = new ArrayList<>();
        for (int i = 0; i < kpspmxList.size(); i++) {
            Kpspmx kpspmx = kpspmxList.get(i);
            Jymxsq jymxsq = new Jymxsq();
            jymxsq.setLrry(kpspmx.getLrry());
            jymxsq.setFphxz(kpspmx.getFphxz());
            jymxsq.setGsdm(kpspmx.getGsdm());
            jymxsq.setJshj(kpspmx.getSpje() + kpspmx.getSpse());
            jymxsq.setKkjje(kpspmx.getSpje() + kpspmx.getSpse());
            jymxsq.setKce(kpspmx.getKce());
            jymxsq.setKpddm(kpspmx.getKpddm());
            jymxsq.setLslbz(kpspmx.getLslbz());
            jymxsq.setSps(kpspmx.getSps());
            jymxsq.setSpdj(kpspmx.getSpdj());
            jymxsq.setSpdm(kpspmx.getSpdm());
            jymxsq.setSpse(kpspmx.getSpse());
            jymxsq.setSpdw(kpspmx.getSpdw());
            jymxsq.setSpje(kpspmx.getSpje());
            jymxsq.setSpsl(kpspmx.getSpsl());
            jymxsq.setSpggxh(kpspmx.getSpggxh());
            jymxsq.setSpmc(kpspmx.getSpmc());
            jymxsq.setSpmxxh(kpspmx.getSpmxxh());
            jymxsq.setYxbz("1");
            jymxsq.setYhzcbs(kpspmx.getYhzcbs());
            jymxsq.setYhzcmc(kpspmx.getYhzcmc());
            jymxsq.setSqlsh(jyxxsq.getSqlsh());
            jymxsq.setJshj(kpspmx.getSpje() + kpspmx.getSpse());
            jymxsq.setXgsj(new Date());
            jymxsq.setLrsj(new Date());
            jymxsqList.add(jymxsq);
        }
        result.put("jymxsqList",jymxsqList);
        return result;
    }
}
