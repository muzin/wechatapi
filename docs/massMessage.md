# 群发消息 API

- [uploadNews 上传多媒体文件](#uploadNews)
- [uploadMPVideo 上传视频素材](#uploadMPVideo)
- [updoadImage 上传图文消息内的图片获取URL](#updoadImage)
- [massSend 群发消息](#massSend)
- [massSendNews 群发图文（news）消息](#massSendNews)
- [massSendText 群发文字（text）消息](#massSendText)
- [massSendVoice 群发声音（voice）消息](#massSendVoice)
- [massSendImage 群发图片（image）消息](#massSendImage)
- [massSendVideo 群发视频（video）消息](#massSendVideo)
- [massSendMPVideo 群发视频（video）消息，直接通过上传文件得到的media id进行群发（自动生成素材）](#massSendMPVideo)
- [deleteMass 删除群发消息](#deleteMass)
- [previewNews 预览接口，预览图文消息](#previewNews)
- [previewText 预览接口，预览文本消息](#previewText)
- [previewVoice 预览接口，预览语音消息](#previewVoice)
- [previewImage 预览接口，预览图片消息](#previewImage)
- [previewVideo 预览接口，预览视频消息](#previewVideo)
- [getMassMessageStatus 查询群发消息状态](#getMassMessageStatus)



### uploadNews

上传多媒体文件，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）

详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>

Examples:
```
api.uploadNews(news);
```
News:
```
[
   {
     "thumb_media_id":"qI6_Ze_6PtV7svjolgs-rN6stStuHIjs9_DidOHaj0Q-mwvBelOXCFZiq2OsIU-p",
     "author":"xxx",
     "title":"Happy Day",
     "content_source_url":"www.qq.com",
     "content":"content",
     "digest":"digest",
     "show_cover_pic":"1"
  },
  {
     "thumb_media_id":"qI6_Ze_6PtV7svjolgs-rN6stStuHIjs9_DidOHaj0Q-mwvBelOXCFZiq2OsIU-p",
     "author":"xxx",
     "title":"Happy Day",
     "content_source_url":"www.qq.com",
     "content":"content",
     "digest":"digest",
     "show_cover_pic":"0"
  }
 ]
```
Result:
```
{
 "type":"news",
 "media_id":"CsEf3ldqkAYJAU6EJeIkStVDSvffUJ54vqbThMgplD-VJXXof6ctX5fI6-aYyUiQ",
 "created_at":1391857799
}
```


### uploadMPVideo
将通过上传下载多媒体文件得到的视频media_id变成视频素材
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.uploadMPVideo(opts);
```
Opts:
```
{
 "media_id": "rF4UdIMfYK3efUfyoddYRMU50zMiRmmt_l0kszupYh_SzrcW5Gaheq05p_lHuOTQ",
 "title": "TITLE",
 "description": "Description"
}
```
Result:
```
{
 "type":"video",
 "media_id":"IhdaAQXuvJtGzwwc0abfXnzeezfO0NgPK6AQYShD8RQYMTtfzbLdBIQkQziv2XJc",
 "created_at":1391857799
}
```

### updoadImage
上传图文消息内的图片获取URL

详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>

Examples:
```
api.uploadImage('filepath');
```
Result:
```
{"url":  "http://mmbiz.qpic.cn/mmbiz/gLO17UPS6FS2xsypf378iaNhWacZ1G1UplZYWEYfwvuU6Ont96b1roYsCNFwaRrSaKTPCUdBK9DgEHicsKwWCBRQ/0"}
```
Param:
- {String} filepath 图片文件路径


### massSend
群发消息，分别有图文（news）、文本(text)、语音（voice）、图片（image）和视频（video）
详情请见：<https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1481187827_i0l21>
Examples:
```
api.massSend(opts, receivers);
```
opts:
```
{
 "image":{
   "media_id":"123dsdajkasd231jhksad"
 },
 "msgtype":"image"
 "send_ignore_reprint":0
}
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```
Param：
- opts {Object}  待发送的数据
- receivers {String|Array|Boolean} 接收人。一个标签，或者openid列表,或者布尔值是否发送给全部用户
- clientMsgId {String|Array}  开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
- sendIgnoreReprint {Int}  图文消息被判定为转载时，是否继续群发。 1为继续群发（转载），0为停止群发。 该参数默认为0。

### massSendNews
群发图文（news）消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.massSendNews(mediaId, receivers);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```

### massSendText
群发文字（text）消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.massSendText(content, receivers);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```

### massSendVoice
群发声音（voice）消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.massSendVoice(media_id, receivers);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```

### massSendImage
群发图片（image）消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.massSendImage(media_id, receivers);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```

### massSendVideo
群发视频（video）消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.massSendVideo(mediaId, receivers);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```

### massSendMPVideo
群发视频（video）消息，直接通过上传文件得到的media id进行群发（自动生成素材）
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.massSendMPVideo(data, receivers);
```
Data:
```
{
 "media_id": "rF4UdIMfYK3efUfyoddYRMU50zMiRmmt_l0kszupYh_SzrcW5Gaheq05p_lHuOTQ",
 "title": "TITLE",
 "description": "Description"
}
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id":34182
}
```

### deleteMass
删除群发消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.deleteMass(message_id);
```
Result:
```
{
 "errcode":0,
 "errmsg":"ok"
}
```
     
### previewNews
预览接口，预览图文消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.previewNews(openid, mediaId);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id": 34182
}
``` 

### previewText
预览接口，预览文本消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.previewText(openid, content);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id": 34182
}
```

### previewVoice
预览接口，预览语音消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.previewVoice(openid, mediaId);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id": 34182
}
```
### previewImage
预览接口，预览图片消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.previewImage(openid, mediaId);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id": 34182
}
```

### previewVideo
预览接口，预览视频消息
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.previewVideo(openid, mediaId);
```
Result:
```
{
 "errcode":0,
 "errmsg":"send job submission success",
 "msg_id": 34182
}
```
### getMassMessageStatus
查询群发消息状态
详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
Examples:
```
api.getMassMessageStatus(messageId);
```
Result:
```
{
 "msg_id":201053012,
 "msg_status":"SEND_SUCCESS"
}
```