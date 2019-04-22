# 用户信息管理 API

- [getUser 获取用户基本信息](#getUser)
- [batchGetUsers 批量获取用户基本信息](#batchGetUsers)
- [getFollowers 获取关注者列表](#getFollowers)
- [updateRemark 设置用户备注名](#updateRemark)
- [createTags 创建标签](#createTags)
- [getTags 获取公众号已创建的标签](#getTags)
- [updateTag 编辑标签](#updateTag)
- [deleteTag 删除标签](#deleteTag)
- [getUsersFromTag 获取标签下粉丝列表](#getUsersFromTag)
- [batchTagging 批量为用户打标签](#batchTagging)
- [batchUnTagging 批量为用户取消标签](#batchUnTagging)
- [getUserTagList 获取用户身上的标签列表](#getUserTagList)



### getUser
获取用户基本信息。可以设置lang，其中zh_CN 简体，zh_TW 繁体，en 英语。默认为en

详情请见：<http://mp.weixin.qq.com/wiki/index.php?title=获取用户基本信息>

Examples:
```
api.getUser(openid);
api.getUser({openid: 'openid', lang: 'en'});
```

Result:
```
{
 "subscribe": 1,
 "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
 "nickname": "Band",
 "sex": 1,
 "language": "zh_CN",
 "city": "广州",
 "province": "广东",
 "country": "中国",
 "headimgurl": "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
 "subscribe_time": 1382694957
}
```
Param:
- openid {String} 用户的openid。
- language {String} 语言，zh_CN 简体，zh_TW 繁体，en 英语。默认为zh_CN。

### batchGetUsers
批量获取用户基本信息

Example:
```
api.batchGetUsers(['openid1', 'openid2'])
api.batchGetUsers(['openid1', 'openid2'], 'en')
```
Result:
```
{
  "user_info_list": [{
    "subscribe": 1,
    "openid": "otvxTs4dckWG7imySrJd6jSi0CWE",
    "nickname": "iWithery",
    "sex": 1,
    "language": "zh_CN",
    "city": "Jieyang",
    "province": "Guangdong",
    "country": "China",
    "headimgurl": "http://wx.qlogo.cn/mmopen/xbIQx1GRqdvyqkMMhEaGOX802l1CyqMJNgUzKP8MeAeHFicRDSnZH7FY4XB7p8XHXIf6uJA2SCunTPicGKezDC4saKISzRj3nz/0",
    "subscribe_time": 1434093047,
    "unionid": "oR5GjjgEhCMJFyzaVZdrxZ2zRRF4",
    "remark": "",
    "groupid": 0
  }, {
    "subscribe": 0,
    "openid": "otvxTs_JZ6SEiP0imdhpi50fuSZg",
    "unionid": "oR5GjjjrbqBZbrnPwwmSxFukE41U",
  }]
}
```
Param: 
- openids {Array} 用户的openid数组。
- lang {String} 语言(zh_CN, zh_TW, en),默认简体中文(zh_CN)

### getFollowers
获取关注者列表

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=获取关注者列表

Examples:
```
api.getFollowers();
// or
api.getFollowers(nextOpenid);
```
Result:
```
{
 "total":2,
 "count":2,
 "data":{
   "openid":["","OPENID1","OPENID2"]
 },
 "next_openid":"NEXT_OPENID"
}
```
Param: 
- nextOpenid {String} 调用一次之后，传递回来的nextOpenid。第一次获取时可不填

### updateRemark
设置用户备注名

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=设置用户备注名接口

Examples:
```
api.updateRemark(openid, remark);
```
Result:
```
{
 "errcode":0,
 "errmsg":"ok"
}
```
Param: 
- openid {String} 用户的openid
- remark {String} 新的备注名，长度必须小于30字符

### createTags
创建标签

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.createTags(name);
```
Result:
```
{
 "id":tagId,
 "name":tagName
}
```
Param: 
- {String} name 标签名

### getTags
获取公众号已创建的标签

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.getTags();
```
Result:
```
 {
   "tags":[{
       "id":1,
       "name":"每天一罐可乐星人",
       "count":0 //此标签下粉丝数
 },{
   "id":2,
   "name":"星标组",
   "count":0
 },{
   "id":127,
   "name":"广东",
   "count":5
 }
   ]
 }
```

### updateTag
编辑标签

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.updateTag(id,name);
```
Result:
```
 {
   "errcode":0,
   "errmsg":"ok"
 }
```
Param: 
- id {String} 标签id
- name {String} 标签名

### deleteTag
删除标签

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.deleteTag(id);
```
Result:
```
 {
   "errcode":0,
   "errmsg":"ok"
 }
```
Param:
- tagId {String} 标签id
 
### getUsersFromTag
获取标签下粉丝列表

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.getUsersFromTag(tagId,nextOpenId);
```
Result:
```
 {
 "count":2,//这次获取的粉丝数量
 "data":{//粉丝列表
   "openid":[
      "ocYxcuAEy30bX0NXmGn4ypqx3tI0",
      "ocYxcuBt0mRugKZ7tGAHPnUaOW7Y"
    ]
  },
```
Param: 
- tagId {String} 标签id
- nextOpenId {String} 第一个拉取的OPENID，不填默认从头开始拉取

### batchTagging
批量为用户打标签

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.batchTagging(openIds,tagId);
```
Result:
```
 {
   "errcode":0,
   "errmsg":"ok"
 }
```
- openIds {Array} openId列表
- tagId {String} 标签id

### batchUnTagging
批量为用户取消标签

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.batchUnTagging(openIds,tagId);
```
Result:
```
 {
   "errcode":0,
   "errmsg":"ok"
 }
```
Param:
- openIds {Array} openId列表
- tagId {String} 标签id
     
### getUserTagList
获取用户身上的标签列表

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN

Examples:
```
api.getUserTagList(openId);
```
Result:
```
 {
   "tagid_list":[//被置上的标签列表 134,2]
  }
```
