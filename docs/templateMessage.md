# 模板消息 API

- [setIndustry 设置所属行业](#setIndustry)
- [addTemplate 获得模板ID](#addTemplate)
- [sendTemplate 发送模板消息](#sendTemplate)
- [sendMiniProgramTemplate 发送模板消息支持小程序](#sendMiniProgramTemplate)





### setIndustry
设置所属行业
Examples:
```
Object industryIds = {
 "industry_id1":'1',
 "industry_id2":"4"
};
api.setIndustry(industryIds);
```
Param:
- industryIds {Object} 公众号模板消息所属行业编号

### addTemplate
获得模板ID

Examples:
```
var templateIdShort = 'TM00015';
api.addTemplate(templateIdShort);
```
Param:
- templateIdShort {String} 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式

### sendTemplate
发送模板消息

Examples:
```
String templateId: '模板id';
// URL置空，则在发送后,点击模板消息会进入一个空白页面（ios）, 或无法点击（android）
String url: 'http://weixin.qq.com/download';
String topcolor = '#FF0000'; // 顶部颜色
Object data = {
 user:{
   "value":'黄先生',
   "color":"#173177"
 }
};
api.sendTemplate('openid', templateId, url, topColor, data);
```
Param:
- openid {String} 用户的openid
- templateId {String} 模板ID
- url {String} URL置空，则在发送后，点击模板消息会进入一个空白页面（ios），或无法点击（android）
- topColor {String} 字体颜色
- data {Object} 渲染模板的数据
- miniprogram {Object} 跳转小程序所需数据 {appid, pagepath}
     
### sendMiniProgramTemplate
发送模板消息支持小程序

Examples:
```
String templateId = '模板id';
String page = 'index?foo=bar'; // 小程序页面路径
String formId = '提交表单id';
String color = '#FF0000'; // 字体颜色
Object data = {
 keyword1: {
   "value":'黄先生',
   "color":"#173177"
 }
var emphasisKeyword = 'keyword1.DATA'
};
api.sendMiniProgramTemplate('openid', templateId, page, formId, data, color, emphasisKeyword);
```
Param: 
- openid {String} 接收者（用户）的 openid
- templateId {String} 所需下发的模板消息的id
- page {String} 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
- formId {String} 表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的 prepay_id
- data {Object} 模板内容，不填则下发空模板
- color {String} 模板内容字体的颜色，不填默认黑色 【废弃】
- emphasisKeyword {String} 模板需要放大的关键词，不填则默认无放大