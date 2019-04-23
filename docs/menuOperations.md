# 菜单管理 API


- [createMenu 创建自定义菜单](#createMenu)
- [getMenu 获取菜单](#getMenu)
- [removeMenu 删除自定义菜单](#removeMenu)
- [getMenuConfig 获取自定义菜单配置](#getMenuConfig)
- [addConditionalMenu 创建个性化自定义菜单](#addConditionalMenu)
- [delConditionalMenu 删除个性化自定义菜单](#delConditionalMenu)
- [tryConditionalMenu 测试个性化自定义菜单](#tryConditionalMenu)



### createMenu
创建自定义菜单

详细请看：http://mp.weixin.qq.com/wiki/index.php?title=自定义菜单创建接口

Menu:
```
{
 "button":[
   {
     "type":"click",
     "name":"今日歌曲",
     "key":"V1001_TODAY_MUSIC"
   },
   {
     "name":"菜单",
     "sub_button":[
       {
         "type":"view",
         "name":"搜索",
         "url":"http://www.soso.com/"
       },
       {
         "type":"click",
         "name":"赞一下我们",
         "key":"V1001_GOOD"
       }]
     }]
   }
 ]
}
```
Examples:
```
var result = await api.createMenu(menu);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```


### getMenu
获取菜单

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=自定义菜单查询接口>
Examples:

```
JsonObject result = await api.getMenu();
```
Result:
```
// 结果示例
{
 "menu": {
   "button":[
     {"type":"click","name":"今日歌曲","key":"V1001_TODAY_MUSIC","sub_button":[]},
     {"type":"click","name":"歌手简介","key":"V1001_TODAY_SINGER","sub_button":[]},
     {"name":"菜单","sub_button":[
       {"type":"view","name":"搜索","url":"http://www.soso.com/","sub_button":[]},
       {"type":"view","name":"视频","url":"http://v.qq.com/","sub_button":[]},
       {"type":"click","name":"赞一下我们","key":"V1001_GOOD","sub_button":[]}]
     }
   ]
 }
}
```


### removeMenu
删除自定义菜单

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=自定义菜单删除接口>

Examples:
```
var result = await api.removeMenu();
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```

### getMenuConfig
获取自定义菜单配置

详细请看：<http://mp.weixin.qq.com/wiki/17/4dc4b0514fdad7a5fbbd477aa9aab5ed.html>

Examples:
```
var result = await api.getMenuConfig();
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```
### addConditionalMenu
创建个性化自定义菜单

详细请看：http://mp.weixin.qq.com/wiki/0/c48ccd12b69ae023159b4bfaa7c39c20.html

Menu:
```
{
 "button":[
 {
     "type":"click",
     "name":"今日歌曲",
     "key":"V1001_TODAY_MUSIC"
 },
 {
   "name":"菜单",
   "sub_button":[
   {
     "type":"view",
     "name":"搜索",
     "url":"http://www.soso.com/"
   },
   {
     "type":"view",
     "name":"视频",
     "url":"http://v.qq.com/"
   },
   {
     "type":"click",
     "name":"赞一下我们",
     "key":"V1001_GOOD"
   }]
}],
"matchrule":{
 "group_id":"2",
 "sex":"1",
 "country":"中国",
 "province":"广东",
 "city":"广州",
 "client_platform_type":"2"
 }
}
```
Examples:
```
var result = await api.addConditionalMenu(menu);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```
### delConditionalMenu
删除个性化自定义菜单

详细请看：http://mp.weixin.qq.com/wiki/0/c48ccd12b69ae023159b4bfaa7c39c20.html

Menu:
```
{
 "menuid":"208379533"
}
```
Examples:
```
var result = await api.delConditionalMenu(menuid);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```
### tryConditionalMenu
测试个性化自定义菜单

详细请看：http://mp.weixin.qq.com/wiki/0/c48ccd12b69ae023159b4bfaa7c39c20.html

Menu:
```
{
 "user_id":"nickma"
}
```
Examples:
```
var result = await api.tryConditionalMenu(user_id);
```
Result:
```
{
   "button": [
       {
           "type": "view",
           "name": "tx",
           "url": "http://www.qq.com/",
           "sub_button": [ ]
       },
       {
           "type": "view",
           "name": "tx",
           "url": "http://www.qq.com/",
           "sub_button": [ ]
       },
       {
           "type": "view",
           "name": "tx",
           "url": "http://www.qq.com/",
           "sub_button": [ ]
       }
   ]
}
```
Param: 
- user_id {String} user_id可以是粉丝的OpenID，也可以是粉丝的微信号。