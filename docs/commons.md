# 公共API


- [getAppid 获取Appid](#)
- [getAppsecret 获取getAppsecret](#)
- [getAccessToken 获取accessToken](#getAccessToken)
- [getLatestTicket 获取最新的票据](#getLatestTicket)
- [getIp 获取微信IP](#getIp)
- [uploadPicture 上传图片](#uploadPicture)
- [getTicket 获取js sdk所需的有效js ticket](#getTicket)


### getAccessToken
获取accessToken,

Examples:
```
api.getAccessToken();
```
> 不建议主动获取accessToken,程序中已经维护好accessToken的使用。
> 直接调用功能方法即可。

### getLatestTicket

获取最新的js api ticket

Examples:
```
api.getLatestTicket();
```

### getIp
获取微信服务器IP地址
详情请见：<http://mp.weixin.qq.com/wiki/index.php?title=%E8%8E%B7%E5%8F%96%E5%BE%AE%E4%BF%A1%E6%9C%8D%E5%8A%A1%E5%99%A8IP%E5%9C%B0%E5%9D%80>
Examples:
```
api.getIp();
```
Result:
```
["127.0.0.1","127.0.0.1"]
```

### uploadPicture
上传图片

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.uploadPicture('/path/to/your/img.jpg');
```

Result:
```
{
 "errcode": 0,
 "errmsg": "success"
 "image_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2ibl4JWwwnW3icSJGqecVtRiaPxwWEIr99eYYL6AAAp1YBo12CpQTXFH6InyQWXITLvU4CU7kic4PcoXA/0"
}
```
Param: 
- filepath {String} 文件路径


### getTicket
获取js sdk所需的有效js ticket
Example:
```
api.getTicket(type);

// or

api.getTicket();     // jsapi


```
Result: 
- `err`, 异常对象
- `result`, 正常获取时的数据Result:
- `errcode`, 0为成功
- `errmsg`, 成功为'ok'，错误则为详细错误信息
- `ticket`, js sdk有效票据，如：bxLdikRXVbTPdHSM05e5u5sUoXNKd8-41ZO3MhKoyN5OfkWITDGgnr2fwJ0m9E8NYzWKVZvdVtaUgWvsdshFKA
- `expires_in`, 有效期7200秒，开发者必须在自己的服务全局缓存jsapi_ticket