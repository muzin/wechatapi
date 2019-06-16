package wechatapi;

import cn.muzin.WechatAPI;
import cn.muzin.entity.AccessToken;
import cn.muzin.entity.Ticket;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class APICommonsTest {

    private WechatAPI wechatAPI = WechatAPIEntity.getWechatAPI();

    @Test
    public void getAppidTest(){
        String appid = wechatAPI.getAppid();

        Assert.assertEquals(appid, ConfigEntity.appid);
    }
    @Test
    public void getAppsecretTest(){
        String appsecret = wechatAPI.getAppsecret();

        Assert.assertEquals(appsecret, ConfigEntity.appsecret);
    }

    @Test
    public void getAccessTokenTest(){

        AccessToken accessToken = wechatAPI.getAccessToken();

        Assert.assertNotEquals(accessToken.getAccessToken(), null);
        Assert.assertNotEquals(accessToken.getExpireTime(), null);

    }

    @Test
    public void getIpTest(){

        List<String> ips = wechatAPI.getIp();

        Assert.assertTrue(ips.size() > 0);

    }

    @Test
    public void getLatestTicketTest(){

        Ticket ticket = wechatAPI.getLatestTicket();

        Assert.assertNotEquals(ticket.getTicket(), null);
        System.out.println(ticket.getTicket());

    }

    @Test
    public void uploadPicture(){
        JsonObject ret = wechatAPI.uploadPicture(ConfigEntity.filepath);

        System.out.println(ret);
        Assert.assertEquals(ret.get("errcode").getAsInt(), 0);

    }


}
