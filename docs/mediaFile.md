### Media API

- [uploadMedia 新增临时素材，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
](#uploadMedia)
- [uploadImageMedia 上传图片素材](#uploadImageMedia)
- [uploadVideoMedia 上传视频素材](#uploadVideoMedia)
- [uploadVoiceMedia 上传音频素材](#uploadVoiceMedia)
- [uploadThumbMedia 上传缩略图素材](#uploadThumbMedia)
- [getMedia 获取临时素材](#getMedia)



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
- {String|InputStream} filepath 文件路径/文件Buffer数据
- {String} type 媒体类型，可用值有image、voice、video、thumb

### uploadImageMedia
Examples:
```
api.uploadImageMedia('filepath');
```
### uploadVoiceMedia
Examples:
```
api.uploadVoiceMedia('filepath');
```
### uploadVideoMedia 
Examples:
```
api.uploadVideoMedia('filepath');
```
### uploadThumbMedia
Examples:
```
api.uploadThumbMedia('filepath');
```

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
- {String} mediaId 媒体文件的ID

###

###