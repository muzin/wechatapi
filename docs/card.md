# 卡券管理 API

- [getCardExt 获取card ext](#getCardExt)


### getCardExt
获取card ext

Examples:
```
var param = {
 card_id: 'p-hXXXXXXX',
 code: '1234',
 openid: '111111',
 balance: 100
};
api.getCardExt(param);
```
- `result`, 调用正常时得到的card_ext对象，包含所需参数
Param:
- {Object} param 参数