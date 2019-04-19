# Wechat API

微信公共平台API。

## 功能列表

- [公共API](docs/commons.md)
- [发送客服消息（文本、图片、语音、视频、音乐、图文）](docs/sendCustomerServiceMessage.md)
- [菜单操作（查询、创建、删除、个性化菜单）](docs/menuOperations.md)
- [二维码（创建临时、永久二维码，查看二维码URL）](docs/qrcode.md)
- [分组操作（查询、创建、修改、移动用户到分组）](docs/groupOperation.md)
- [用户信息（查询用户基本信息、获取关注者列表）](docs/userInfo.md)
- [媒体文件（上传、获取）](docs/mediaFile.md)
- [群发消息（文本、图片、语音、视频、图文）](docs/massMessage.md)
- [客服记录（查询客服记录，查看客服、查看在线客服）](docs/customerServiceRecord.md)
- [公众号支付（发货通知、订单查询）](docs/pay.md)
- [微信小店（商品管理、库存管理、邮费模板管理、分组管理、货架管理、订单管理、功能接口）](docs/wechatShop.md)
- [模版消息](docs/templateMessage.md)
- [网址缩短](docs/url.md)
- [语义查询](docs/semanticQuery.md)
- [数据分析](docs/dataAnalysis.md)
- [JSSDK服务端支持](docs/jssdk.md)
- [素材管理](docs/materialManager.md)
- [摇一摇周边](docs/shake.md)
- [卡劵管理](docs/card.md)

## Installtion

```
// 需引入以下 jar 包

// https://mvnrepository.com/artifact/com.google.code.gson/gson
compile group: 'com.google.code.gson', name: 'gson', version: '2.8.2'

// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
compile group: 'org.apache.httpcomponents', name: 'httpclient', version: '4.5.6'

// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpmime
compile group: 'org.apache.httpcomponents', name: 'httpmime', version: '4.5.6'

```

## Usage

```
  WechatAPI api = new WechatAPI(appid, appsecret);
```

## 多进程
当多进程时，token需要全局维护，以下为保存token的接口：
```
WechatAPI api = new WechatAPI(appid, appsecret, new TokenStorageResolver() {

    /**
     * 获取token
     * 程序内部将通过此方法获取token
     */
    @Override
    public AccessToken getToken() {
        AccessToken token = 从文件、redis等渠道获取保存的accessToken
        return token;
    }
    
    /**
     * 保存token
     * 程序内部每次更新accessToken时，将会通知此方法
     */
    @Override
    public void saveToken(AccessToken accessToken) {
        // code...
        // 保存到文件、redis等渠道
    }
});

```