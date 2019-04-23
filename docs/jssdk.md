# JSSDK API

- [getJsConfig 获取微信JS SDK Config的所需参数](#getJsConfig)

### getJsConfig
获取微信JS SDK Config的所需参数

Examples:
```
var param = {
 debug: false,
 jsApiList: ['onMenuShareTimeline', 'onMenuShareAppMessage'],
 url: 'http://www.xxx.com'
};
api.getJsConfig(param);
```
`result`, 调用正常时得到的js sdk config所需参数
Param: 
- {Object} param 参数