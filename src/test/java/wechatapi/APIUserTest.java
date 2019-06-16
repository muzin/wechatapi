package wechatapi;

import cn.muzin.WechatAPI;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class APIUserTest {

    private WechatAPI wechatAPI = WechatAPIEntity.getWechatAPI();

    @Test
    public void getUserTest(){
        JsonObject userInfo = wechatAPI.getUser("oOAKn1EqJnrnOWP_pU4Z4DtaZ3Zk");

        System.out.println(userInfo);

        Assert.assertNotNull(userInfo);
        Assert.assertTrue(userInfo.has("nickname"));
        Assert.assertTrue(userInfo.has("sex"));
        Assert.assertTrue(userInfo.has("openid"));

    }
}
