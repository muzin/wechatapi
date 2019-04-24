# 客服消息 API


- [getRecords 获取客服聊天记录](#getRecords)
- [getCustomServiceList 获取客服基本信息](#getCustomServiceList)
- [getOnlineCustomServiceList 获取在线客服接待信息](#getOnlineCustomServiceList)
- [addKfAccount 添加客服信息](#addKfAccount)
- [inviteworker 邀请绑定客服帐号](#inviteworker)
- [updateKfAccount 设置客服账号](#updateKfAccount)
- [deleteKfAccount 删除客服账号](#deleteKfAccount)
- [setKfAccountAvatar 设置客服头像](#setKfAccountAvatar)
- [createKfSession 创建客服会话](#createKfSession)


### getRecords
获取客服聊天记录
详细请看：http://mp.weixin.qq.com/wiki/19/7c129ec71ddfa60923ea9334557e8b23.html
Opts:
```
{
  "starttime" : 123456789,
  "endtime" : 987654321,
  "openid": "OPENID", // 非必须
  "pagesize" : 10,
  "pageindex" : 1,
 }
 ```
Examples:
 ```
 JsonArray result = await api.getRecords(opts);
 ```
 Result:
 ```
 [
    {
      "worker": " test1",
      "openid": "oDF3iY9WMaswOPWjCIp_f3Bnpljk",
      "opercode": 2002,
      "time": 1400563710,
      "text": " 您好，客服test1为您服务。"
    },
    {
      "worker": " test1",
      "openid": "oDF3iY9WMaswOPWjCIp_f3Bnpljk",
      "opercode": 2003,
      "time": 1400563731,
      "text": " 你好，有什么事情？ "
    },
]
```

### getCustomServiceList
获取客服基本信息
详细请看：http://dkf.qq.com/document-3_1.html
Examples:
```
JsonArray result = api.getCustomServiceList();
```
Result:
```
[
    {
      "kf_account": "test1@test",
      "kf_nick": "ntest1",
      "kf_id": "1001"
    },
    {
      "kf_account": "test2@test",
      "kf_nick": "ntest2",
      "kf_id": "1002"
    },
    {
      "kf_account": "test3@test",
      "kf_nick": "ntest3",
      "kf_id": "1003"
    }
  ]
}
```

### getOnlineCustomServiceList
获取在线客服接待信息
详细请看：http://dkf.qq.com/document-3_2.htmlExamples:
```
JsonArray list = api.getOnlineCustomServiceList();
```
Result:
```
{
  "kf_online_list": [
    {
      "kf_account": "test1@test",
      "status": 1,
      "kf_id": "1001",
      "auto_accept": 0,
      "accepted_case": 1
    },
    {
      "kf_account": "test2@test",
      "status": 1,
      "kf_id": "1002",
      "auto_accept": 0,
      "accepted_case": 2
    }
  ]
}
```


### addKfAccount
添加客服账号
详细请看：http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044813&token=&lang=zh_CNExamples:
```
boolean result = api.addKfAccount('test@test', 'nickname', 'password');
```
Result:
```
{
 "errcode" : 0,
 "errmsg" : "ok",
}
```

### inviteworker
邀请绑定客服帐号
详细请看：https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044813&token=&lang=zh_CN
Examples:
```
boolean result = api.inviteworker('test@test', 'invite_wx');
```
Result:
```
{
 "errcode" : 0,
 "errmsg" : "ok",
}
```


### updateKfAccount
设置客服账号
详细请看：http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044813&token=&lang=zh_CNExamples:
```
boolean result = api.updateKfAccount('test@test', 'nickname', 'password');
```
Result:
```
{
 "errcode" : 0,
 "errmsg" : "ok",
}
```

### deleteKfAccount
删除客服账号
详细请看：http://mp.weixin.qq.com/wiki/9/6fff6f191ef92c126b043ada035cc935.htmlExamples:
```
api.deleteKfAccount('test@test');
```
Result:
```
{
 "errcode" : 0,
 "errmsg" : "ok",
}
```


### setKfAccountAvatar
设置客服头像
详细请看：http://mp.weixin.qq.com/wiki/9/6fff6f191ef92c126b043ada035cc935.htmlExamples:
```
api.setKfAccountAvatar('test@test', '/path/to/avatar.png');
```
Result:
```
{
 "errcode" : 0,
 "errmsg" : "ok",
}
```

### createKfSession
创建客服会话
详细请看：http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044820&token=&lang=zh_CNExamples:
```
api.createKfSession('test@test', 'OPENID');
```
Result:
```
{
 "errcode" : 0,
 "errmsg" : "ok",
}
```