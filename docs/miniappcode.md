# 小程序 API

- [createWXAQRCode 获取小程序二维码，适用于需要的码数量较少的业务场景](#createWXAQRCode)
- [getWXACode 获取小程序码，适用于需要的码数量较少的业务场景](#getWXACode)
- [getWXACodeUnlimit 获取小程序码，适用于需要的码数量极多的业务场景](#getWXACodeUnlimit)




### createWXAQRCode
获取小程序二维码，适用于需要的码数量较少的业务场景

https://developers.weixin.qq.com/miniprogram/dev/api/createWXAQRCode.html

Examples:
```
String path = 'index?foo=bar'; // 小程序页面路径
api.createWXAQRCode(path, width);
```
Param: 
- path {String} 扫码进入的小程序页面路径，最大长度 128 字节，不能为空
- width {String} 二维码的宽度，单位 px。最小 280px，最大 1280px

### getWXACode
获取小程序码，适用于需要的码数量较少的业务场景

https://developers.weixin.qq.com/miniprogram/dev/api/getWXACode.html

Examples:
```
String path = 'index?foo=bar'; // 小程序页面路径
Map line_color = {"r": 0,"g": 0,"b": 0};
api.getWXACode(path, width, false, line_color, true);
```
Param: 
- path {String} 扫码进入的小程序页面路径，最大长度 128 字节，不能为空
- width {Integer} 二维码的宽度，单位 px。最小 280px，最大 1280px
- auto_color {Boolean} 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
- line_color {Object} auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
- is_hyaline {Boolean} 是否需要透明底色，为 true 时，生成透明底色的小程序码

### getWXACodeUnlimit
获取小程序码，适用于需要的码数量极多的业务场景

https://developers.weixin.qq.com/miniprogram/dev/api/getWXACodeUnlimit.html

Examples:
```
String scene = 'foo=bar';
String page = 'pages/index/index'; // 小程序页面路径
Map line_color = {"r": 0,"g": 0,"b": 0};
api.getWXACodeUnlimit(scene, page, width, auto_color, line_color, is_hyaline);
```
Param:
- scene {String} 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
- page {String} 必须是已经发布的小程序存在的页面（否则报错），例如 pages/index/index, 根路径前不要填加 /,不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面
- width {Integer} 二维码的宽度，单位 px。最小 280px，最大 1280px
- auto_color {Boolean} 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
- line_color {Object} auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
- is_hyaline {Boolean} 是否需要透明底色，为 true 时，生成透明底色的小程序码