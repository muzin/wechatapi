# 二维码 API

- [createTmpQRCode 创建临时二维码](#createTmpQRCode)
- [createLimitQRCode 创建永久二维码](#createLimitQRCode)
- [showQRCodeURL 生成显示二维码的链接](#showQRCodeURL)


### createTmpQRCode
创建临时二维码

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=生成带参数的二维码>

Examples:
```
api.createTmpQRCode(10000, 1800);
```

Result:
```
{
 "ticket":"gQG28DoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL0FuWC1DNmZuVEhvMVp4NDNMRnNRAAIEesLvUQMECAcAAA==",
 "expire_seconds":1800
}
```


### createLimitQRCode
创建永久二维码

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=生成带参数的二维码>

Examples:
```
api.createLimitQRCode(100);
```

Result:
```
{
 "ticket":"gQG28DoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL0FuWC1DNmZuVEhvMVp4NDNMRnNRAAIEesLvUQMECAcAAA=="
}
```


### showQRCodeURL
生成显示二维码的链接。

微信扫描后，可立即进入场景

Examples:
```
api.showQRCodeURL(ticket);
// => https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET
```
