# 客服消息 API


- [sendText 客服消息，发送文字消息](#sendText)
- [sendImage 客服消息，发送图片消息](#sendImage)
- [sendCard 客服消息，发送卡券](#sendCard)
- [sendVoice 客服消息，发送语音消息](#sendVoice)
- [sendVideo 客服消息，发送视频消息](#sendVideo)
- [sendMusic 客服消息，发送音乐消息](#sendMusic)
- [sendNews 客服消息，发送图文消息](#sendNews)
- [sendMpNews 客服消息，发送图文消息（点击跳转到图文消息页面）](#sendMpNews)
- [sendMiniProgram 客服消息，发送小程序卡片（要求小程序与公众号已关联）](#sendMiniProgram)
- [getAutoreply 获取自动回复规则](#getAutoreply)


### sendText
客服消息，发送文字消息

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息

Examples:
```
api.sendText('openid', 'Hello world');
```

### sendImage
客服消息，发送图片消息

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息

Examples:
```
api.sendImage('openid', 'media_id');
```

### sendCard
客服消息，发送卡券

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息

Examples:
```
api.sendCard('openid', 'card_id');
```

### sendVoice
客服消息，发送语音消息

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息

Examples:
```
api.sendVoice('openid', 'media_id');
```

### sendVideo
客服消息，发送视频消息
详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
Examples:
```
api.sendVideo('openid', 'media_id', 'thumb_media_id');
```
Param: 
- openid {String} 用户的openid
- mediaId {String} 媒体文件的ID
- thumbMediaId {String} 缩略图文件的ID

### sendMusic
客服消息，发送音乐消息

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息

Examples:
```
var music = {
 title: '音乐标题', // 可选
 description: '描述内容', // 可选
 musicurl: 'http://url.cn/xxx', 音乐文件地址
 hqmusicurl: "HQ_MUSIC_URL",
 thumb_media_id: "THUMB_MEDIA_ID"
};
api.sendMusic('openid', music);
```

### sendNews
客服消息，发送图文消息

详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息

Examples:
```
var articles = [
 {
   "title":"Happy Day",
   "description":"Is Really A Happy Day",
   "url":"URL",
   "picurl":"PIC_URL"
 },
 {
   "title":"Happy Day",
   "description":"Is Really A Happy Day",
   "url":"URL",
   "picurl":"PIC_URL"
 }];
api.sendNews('openid', articles);
```
Param:
- openid {String}用户的openid
- articles {Array} 图文列表

### sendMpNews
客服消息，发送图文消息（点击跳转到图文消息页面）

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140547

Examples:
```
api.sendMpNews('openid', 'mediaId');
```
Param: 
- openid {String} 用户的openid
- mediaId {String} 图文消息媒体文件的ID

### sendMiniProgram
客服消息，发送小程序卡片（要求小程序与公众号已关联）

详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140547

Examples:
```
var miniprogram = {
 title: '小程序标题', // 必填
 appid: '小程序appid', // 必填
 pagepath: 'pagepath', // 打开后小程序的地址，可以带query
 thumb_media_id: "THUMB_MEDIA_ID"
};
api.sendMiniProgram('openid', miniprogram);
```
Param:
- openid {String} 用户的openid
- miniprogram {Object} 小程序信息

### getAutoreply
获取自动回复规则

详细请看：<http://mp.weixin.qq.com/wiki/19/ce8afc8ae7470a0d7205322f46a02647.html>

Examples:
```
var result = await api.getAutoreply();
```
Result:
```
{
"is_add_friend_reply_open": 1,
"is_autoreply_open": 1,
"add_friend_autoreply_info": {
    "type": "text",
    "content": "Thanks for your attention!"
},
"message_default_autoreply_info": {
    "type": "text",
    "content": "Hello, this is autoreply!"
},
"keyword_autoreply_info": {
    "list": [
        {
            "rule_name": "autoreply-news",
            "create_time": 1423028166,
            "reply_mode": "reply_all",
            "keyword_list_info": [
                {
                    "type": "text",
                    "match_mode": "contain",
                    "content": "news测试"//此处content即为关键词内容
                }
            ],
            "reply_list_info": [
                {
                    "type": "news",
                    "news_info": {
                        "list": [
                            {
                                "title": "it's news",
                                "author": "jim",
                                "digest": "it's digest",
                                "show_cover": 1,
                                "cover_url": "http://mmbiz.qpic.cn/mmbiz/GE7et87vE9vicuCibqXsX9GPPLuEtBfXfKbE8sWdt2DDcL0dMfQWJWTVn1N8DxI0gcRmrtqBOuwQHeuPKmFLK0ZQ/0",
                                "content_url": "http://mp.weixin.qq.com/s?__biz=MjM5ODUwNTM3Ng==&mid=203929886&idx=1&sn=628f964cf0c6d84c026881b6959aea8b#rd",
                                "source_url": "http://www.url.com"
                            }
                        ]
                    }
                },
                {
                    ....
                }
            ]
        },
        {
            "rule_name": "autoreply-voice",
            "create_time": 1423027971,
            "reply_mode": "random_one",
            "keyword_list_info": [
                {
                    "type": "text",
                    "match_mode": "contain",
                    "content": "voice测试"
                }
            ],
            "reply_list_info": [
                {
                    "type": "voice",
                    "content": "NESsxgHEvAcg3egJTtYj4uG1PTL6iPhratdWKDLAXYErhN6oEEfMdVyblWtBY5vp"
                }
            ]
        },
        ...
    ]
}
}
```