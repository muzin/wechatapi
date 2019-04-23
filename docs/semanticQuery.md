# 语义理解 API

- [semantic 发送语义理解请求](#semantic)


### semantic
发送语义理解请求

详细请看：http://mp.weixin.qq.com/wiki/index.php?title=%E8%AF%AD%E4%B9%89%E7%90%86%E8%A7%A3Opts:

```
{
  "query":"查一下明天从北京到上海的南航机票",
  "city":"北京",
  "category": "flight,hotel"
}
```
Examples:
```
api.semantic(uid, opts);
```
Result:
```
{
  "errcode":0,
  "query":"查一下明天从北京到上海的南航机票",
  "type":"flight",
  "semantic":{
      "details":{
          "start_loc":{
              "type":"LOC_CITY",
              "city":"北京市",
              "city_simple":"北京",
              "loc_ori":"北京"
              },
          "end_loc": {
              "type":"LOC_CITY",
              "city":"上海市",
              "city_simple":"上海",
              "loc_ori":"上海"
            },
          "start_date": {
              "type":"DT_ORI",
              "date":"2014-03-05",
              "date_ori":"明天"
            },
         "airline":"中国南方航空公司"
      },
  "intent":"SEARCH"
}
```
Param: 
- openid {String} 用户ID
- opts {Object} 查询条件