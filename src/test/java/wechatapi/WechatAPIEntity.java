package wechatapi;

import cn.muzin.WechatAPI;

public class WechatAPIEntity {

    private static WechatAPI wechatAPI = null;

    static {

        wechatAPI = new WechatAPI(ConfigEntity.appid, ConfigEntity.appsecret);

    }

    public static WechatAPI getWechatAPI(){
        return wechatAPI;
    }


}
