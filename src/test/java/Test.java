import com.rjxx.kpt.ws.WebServiceHelper;

/**
 * Created by Administrator on 2016/12/6.
 */
public class Test {

    public static void main(String[] args) throws Exception{
        String templatePath = WebServiceHelper.class.getResource("/template/return_message.xml").getFile();
        System.out.println(templatePath);
    }

}
