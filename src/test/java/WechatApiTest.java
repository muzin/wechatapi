import cn.muzin.WechatAPI;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WechatApiTest {

    @Test
    public void rawTest(){

        WechatAPI wechatAPI = new WechatAPI("", "");

        Map<String, String> map = new HashMap<String, String>();
        map.put("aw","");
        map.put("dw","");
        map.put("d","");
        map.put("yw","");
        map.put("zw","");
        map.put("qw","");
        map.put("dw","");

        String ret = wechatAPI.raw(map);
        System.out.println(ret);
        Assert.assertEquals(ret, "aw=&d=&dw=&qw=&yw=&zw=");

    }

}
