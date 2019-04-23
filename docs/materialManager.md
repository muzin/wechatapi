# 素材管理 API

- [uploadMaterial 上传永久素材，分别有图片（image）、语音（voice）、和缩略图（thumb）](#uploadMaterial)
- [uploadImageMaterial 上传图片素材](#uploadImageMaterial)
- [uploadVoiceMaterial 上传音频素材](#uploadVoiceMaterial)
- [uploadThumbMaterial 上传缩略图素材](#uploadThumbMaterial)
- [uploadVideoMaterial 上传视频素材](#uploadVideoMaterial)
- [uploadNewsMaterial 新增永久图文素材](#uploadNewsMaterial)
- [updateNewsMaterial 更新永久图文素材](#updateNewsMaterial)
- [getMaterial 根据媒体ID获取永久素材](#getMaterial)
- [removeMaterial 删除永久素材](#removeMaterial)
- [getMaterialCount 获取素材总数](#getMaterialCount)
- [getMaterials 获取永久素材列表](#getMaterials)


- [uploadMedia 新增临时素材，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）](#uploadMedia)
- [getMedia 获取临时素材](#getMedia)
- [uploadImage 上传图文消息内的图片获取URL](#uploadImage)




### uploadMaterial
上传永久素材，分别有图片（image）、语音（voice）、和缩略图（thumb）
详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>

Examples:
```
api.uploadMaterial('filepath', type);
```
Result:
```
{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
```

Shortcut:
- `uploadImageMaterial(filepath);`
- `uploadVoiceMaterial(filepath);`
- `uploadThumbMaterial(filepath);`

Param: 
- filepath {String} 文件路径
- type {String} 媒体类型，可用值有`image`、`voice`、`video`、`thumb`.

### uploadImageMaterial
上传永久图片素材
详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>

Examples:
```
api.uploadImageMaterial('filepath');
```
Result:
```
{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
```

Param: 
- filepath {String} 文件路径.

### uploadVoiceMaterial
上传永久音频素材
详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>

Examples:
```
api.uploadVoiceMaterial('filepath');
```
Result:
```
{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
```

Param: 
- filepath {String} 文件路径.

### uploadThumbMaterial
上传永久缩略图素材
详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>

Examples:
```
api.uploadThumbMaterial('filepath');
```
Result:
```
{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
```

Param: 
- filepath {String} 文件路径.

### uploadVideoMaterial
上传永久素材，视频（video）

详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>

Examples:
```
description = {
  "title":VIDEO_TITLE,
  "introduction":INTRODUCTION
};
api.uploadVideoMaterial('filepath', description);
```
Result:
```
{"media_id":"MEDIA_ID"}
```
Param: 
- filepath {String} 文件路径
- description {Object} 描述

### uploadNewsMaterial
新增永久图文素材

News:
```
[
   {
     "title": TITLE,
     "thumb_media_id": THUMB_MEDIA_ID,
     "author": AUTHOR,
     "digest": DIGEST,
     "show_cover_pic": SHOW_COVER_PIC(0 / 1),
     "content": CONTENT,
     "content_source_url": CONTENT_SOURCE_URL
   },
   //若新增的是多图文素材，则此处应还有几段articles结构
 ]
```
Examples:
```
api.uploadNewsMaterial(news);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```

### updateNewsMaterial
更新永久图文素材
News:
```
{
 "media_id":MEDIA_ID,
 "index":INDEX,
 "articles": [
   {
     "title": TITLE,
     "thumb_media_id": THUMB_MEDIA_ID,
     "author": AUTHOR,
     "digest": DIGEST,
     "show_cover_pic": SHOW_COVER_PIC(0 / 1),
     "content": CONTENT,
     "content_source_url": CONTENT_SOURCE_URL
   },
   //若新增的是多图文素材，则此处应还有几段articles结构
 ]
}
```
Examples:
```
api.uploadNewsMaterial(news);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```
     
### getMaterial
根据媒体ID获取永久素材
详情请见：<http://mp.weixin.qq.com/wiki/4/b3546879f07623cb30df9ca0e420a5d0.html>
Examples:
```
api.getMaterial('media_id');
```
Param:
- `result`, 调用正常时得到的文件Buffer对象
- `res`, HTTP响应对象

### removeMaterial
删除永久素材
详情请见：<http://mp.weixin.qq.com/wiki/5/e66f61c303db51a6c0f90f46b15af5f5.html>
Examples:
```
api.removeMaterial('media_id');
```
Param:
- `result`, 调用正常时得到的文件Buffer对象
- `res`, HTTP响应对象

### getMaterialCount
获取素材总数

详情请见：<http://mp.weixin.qq.com/wiki/16/8cc64f8c189674b421bee3ed403993b8.html>

Examples:
```
api.getMaterialCount();
```
Result:
```
{
 "voice_count":COUNT,
 "video_count":COUNT,
 "image_count":COUNT,
 "news_count":COUNT
}
```
Param:
- `result`, 调用正常时得到的文件Buffer对象
- `res`, HTTP响应对象


### getMaterials
获取永久素材列表

详情请见：<http://mp.weixin.qq.com/wiki/12/2108cd7aafff7f388f41f37efa710204.html>

Examples:
```
api.getMaterials(type, offset, count);
```

Result:
```
{
 "total_count": TOTAL_COUNT,
 "item_count": ITEM_COUNT,
 "item": [{
   "media_id": MEDIA_ID,
   "name": NAME,
   "update_time": UPDATE_TIME
 },
 //可能会有多个素材
 ]
}
```
Param:
    
- type {String} 素材的类型，图片（image）、视频（video）、语音 （voice）、图文（news）
- offset {Number} 从全部素材的该偏移位置开始返回，0表示从第一个素材 返回
- count {Number} 返回素材的数量，取值在1到20之间


### uploadMedia
新增临时素材，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）

详情请见：<http://mp.weixin.qq.com/wiki/5/963fc70b80dc75483a271298a76a8d59.html>

Examples:
```
api.uploadMedia('filepath', type);
```
Result:
```
{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
```
Shortcut:
- `api.uploadImageMedia(filepath);`
- `api.uploadVoiceMedia(filepath);`
- `api.uploadVideoMedia(filepath);`
- `api.uploadThumbMedia(filepath);`
Param:
- filepath {String|InputStream} 文件路径/文件Buffer数据
- type {String} 媒体类型，可用值有image、voice、video、thumb

### getMedia
获取临时素材

详情请见：<http://mp.weixin.qq.com/wiki/11/07b6b76a6b6e8848e855a435d5e34a5f.html>

Examples:
```
api.getMedia('media_id');
```
- `result`, 调用正常时得到的文件Buffer对象
- `res`, HTTP响应对象
Param:
- mediaId {String} 媒体文件的ID


###
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
- filepath {String} 图片文件路径