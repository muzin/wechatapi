# 授权登录API


- [getWebAuthAccessToken 获取网页授权登录AccessToken](#)



### getWebAuthAccessToken

获取网页授权登录AccessToken

Examples:
```
WebAuthAccessToken token = api.getWebAuthAccessToken(code);
```

Result: 
- `access_token`: 网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
- `expires_in`: access_token接口调用凭证超时时间，单位（秒）
- `refresh_token`: 用户刷新access_token
- `openid` : 用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
- `scope`: 用户授权的作用域，使用逗号（,）分隔