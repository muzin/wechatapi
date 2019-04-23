# 公共API


- [getAccessToken 获取accessToken](#getAccessToken)
- [getLatestTicket 获取最新的票据](#getLatestTicket)
- [getIp 获取微信IP](#getIp)
- [uploadPicture 上传图片](#uploadPicture)

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