# 微信小店 API

- [addExpressTemplate 增加邮费模板](#addExpressTemplate)
- [deleteExpressTemplate 修改邮费模板](#deleteExpressTemplate)
- [updateExpressTemplate 修改邮费模板](#updateExpressTemplate)
- [getExpressTemplateById 获取指定ID的邮费模板](#getExpressTemplateById)
- [getAllExpressTemplates 获取所有邮费模板的未封装版本](#getAllExpressTemplates)


- [createGoods 增加商品](#createGoods)
- [deleteGoods 删除商品](#deleteGoods)
- [updateGoods 修改商品](#updateGoods)
- [getGoods 查询商品](#getGoods)
- [getGoodsByStatus 获取指定状态的所有商品](#getGoodsByStatus)
- [updateGoodsStatus 商品上下架](#updateGoodsStatus)
- [getSubCats 获取指定分类的所有子分类](#getSubCats)
- [getSKUs 获取指定子分类的所有SKU](#getSKUs)
- [getProperties 获取指定分类的所有属性](#getProperties)


- [createGoodsGroup 创建商品分组](#createGoodsGroup)
- [deleteGoodsGroup 删除商品分组](#deleteGoodsGroup)
- [updateGoodsGroup 修改商品分组属性](#updateGoodsGroup)
- [updateGoodsForGroup 修改商品分组内的商品](#updateGoodsForGroup)
- [getAllGroups 获取所有商品分组](#getAllGroups)
- [getGroupById 根据ID获取商品分组](#getGroupById)


- [updateStock 增加库存](#updateStock)


- [createShelf 增加货架](#createShelf)
- [deleteShelf 删除货架](#deleteShelf)
- [updateShelf 修改货架](#updateShelf)
- [getAllShelves 获取所有货架](#getAllShelves)
- [getShelfById 根据货架ID获取货架信息](#getShelfById)


- [getOrderById 根据订单Id获取订单详情](#getOrderById)
- [getOrdersByStatus 根据订单状态/创建时间获取订单详情](#getOrdersByStatus)
- [setExpressForOrder 设置订单发货信息](#setExpressForOrder)
- [setNoDeliveryForOrder 设置订单发货信息－不需要物流配送](#setNoDeliveryForOrder)
- [closeOrder 关闭订单](#closeOrder)


- [addPoi 创建门店](#addPoi)
- [getPoi 获取门店](#getPoi)
- [getPois 获取门店列表](#getPois)
- [delPoi 删除门店](#delPoi)
- [updatePoi 修改门店服务信息](#updatePoi)



### addExpressTemplate
增加邮费模板

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.addExpress(express);
```
Express:
```
{
 "delivery_template": {
   "Name": "testexpress",
   "Assumer": 0,
   "Valuation": 0,
   "TopFee": [
     {
       "Type": 10000027,
       "Normal": {
         "StartStandards": 1,
         "StartFees": 2,
         "AddStandards": 3,
         "AddFees": 1
       },
       "Custom": [
         {
           "StartStandards": 1,
           "StartFees": 100,
           "AddStandards": 1,
           "AddFees": 3,
           "DestCountry": "中国",
           "DestProvince": "广东省",
           "DestCity": "广州市"
         }
       ]
     },
     {
       "Type": 10000028,
       "Normal": {
         "StartStandards": 1,
         "StartFees": 3,
         "AddStandards": 3,
         "AddFees": 2
       },
       "Custom": [
         {
           "StartStandards": 1,
           "StartFees": 10,
           "AddStandards": 1,
           "AddFees": 30,
           "DestCountry": "中国",
           "DestProvince": "广东省",
           "DestCity": "广州市"
         }
       ]
     },
     {
       "Type": 10000029,
       "Normal": {
         "StartStandards": 1,
         "StartFees": 4,
         "AddStandards": 3,
         "AddFees": 3
       },
       "Custom": [
         {
           "StartStandards": 1,
           "StartFees": 8,
           "AddStandards": 2,
           "AddFees": 11,
           "DestCountry": "中国",
           "DestProvince": "广东省",
           "DestCity": "广州市"
         }
       ]
     }
   ]
 }
}
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
 "template_id": 123456
}
```
### deleteExpressTemplate
修改邮费模板

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.deleteExpressTemplate(templateId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
### updateExpressTemplate
修改邮费模板

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateExpressTemplate(template);
```
Express:
```
{
 "template_id": 123456,
 "delivery_template": ...
}
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param: 
- template {Object} 邮费模版


### getExpressTemplateById
获取指定ID的邮费模板

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getExpressTemplateById(templateId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
 "template_info": {
   "Id": 103312916,
   "Name": "testexpress",
   "Assumer": 0,
   "Valuation": 0,
   "TopFee": [
     {
       "Type": 10000027,
       "Normal": {
         "StartStandards": 1,
         "StartFees": 2,
         "AddStandards": 3,
         "AddFees": 1
       },
       "Custom": [
         {
           "StartStandards": 1,
           "StartFees": 1000,
           "AddStandards": 1,
           "AddFees": 3,
           "DestCountry": "中国",
           "DestProvince": "广东省",
           "DestCity": "广州市"
         }
       ]
     },
     ...
   ]
 }
}
```
Param: 
- templateId {Number} 邮费模版Id

### getAllExpressTemplates
获取所有邮费模板的未封装版本

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getAllExpressTemplates();
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
 "templates_info": [
   {
     "Id": 103312916,
     "Name": "testexpress1",
     "Assumer": 0,
     "Valuation": 0,
     "TopFee": [...],
   },
   {
     "Id": 103312917,
     "Name": "testexpress2",
     "Assumer": 0,
     "Valuation": 2,
     "TopFee": [...],
   },
   {
     "Id": 103312918,
     "Name": "testexpress3",
     "Assumer": 0,
     "Valuation": 1,
     "TopFee": [...],
   }
 ]
}
```


### createGoods
增加商品

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.createGoods(goods);
```
Goods:
```
{
 "product_base":{
   "category_id":[
     "537074298"
   ],
   "property":[
     {"id":"1075741879","vid":"1079749967"},
     {"id":"1075754127","vid":"1079795198"},
     {"id":"1075777334","vid":"1079837440"}
   ],
   "name":"testaddproduct",
   "sku_info":[
     {
       "id":"1075741873",
       "vid":["1079742386","1079742363"]
     }
   ],
   "main_img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic0FD3vN0V8PILcibEGb2fPfEOmw/0",
   "img":[
     "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic0FD3vN0V8PILcibEGb2fPfEOmw/0"
   ],
   "detail":[
     {"text":"testfirst"},
     {"img": 4whpV1VZl2iccsvYbHvnphkyGtnvjD3ul1UcLcwxrFdwTKYhH9Q5YZoCfX4Ncx655ZK6ibnlibCCErbKQtReySaVA/0"},
     {"text":"testagain"}
   ],
   "buy_limit":10
 },
 "sku_list":[
   {
     "sku_id":"1075741873:1079742386",
     "price":30,
     "icon_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl28bJj62XgfHPibY3ORKicN1oJ4CcoIr4BMbfA8LqyyjzOZzqrOGz3f5K Wq1QGP3fo6TOTSYD3TBQjuw/0",
     "product_code":"testing",
     "ori_price":9000000,
     "quantity":800
   },
   {
     "sku_id":"1075741873:1079742363",
     "price":30,
     "icon_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl28bJj62XgfHPibY3ORKicN1oJ4CcoIr4BMbfA8LqyyjzOZzqrOGz3f5K Wq1QGP3fo6TOTSYD3TBQjuw/0",
     "product_code":"testingtesting",
     "ori_price":9000000,
     "quantity":800
   }
 ],
 "attrext":{
   "location":{
     "country":"中国",
     "province":"广东省",
     "city":"广州市",
     "address":"T.I.T创意园"
   },
   "isPostFree":0,
   "isHasReceipt":1,
   "isUnderGuaranty":0,
   "isSupportReplace":0
 },
 "delivery_info":{
   "delivery_type":0,
   "template_id":0,
   "express":[
     {"id":10000027,"price":100},
     {"id":10000028,"price":100},
     {"id":10000029,"price":100}
   ]
 }
}
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
 "product_id": "pDF3iYwktviE3BzU3BKiSWWi9Nkw"
}
```



### deleteGoods
删除商品

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.deleteGoods(productId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
}
```
     
### updateGoods
修改商品

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateGoods(goods);
```
Goods:
```
{
 "product_id":"pDF3iY6Kr_BV_CXaiYysoGqJhppQ",
 "product_base":{
   "category_id":[
     "537074298"
   ],
   "property":[
     {"id":"1075741879","vid":"1079749967"},
     {"id":"1075754127","vid":"1079795198"},
     {"id":"1075777334","vid":"1079837440"}
   ],
   "name":"testaddproduct",
   "sku_info":[
     {
       "id":"1075741873",
       "vid":["1079742386","1079742363"]
     }
   ],
   "main_img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic0FD3vN0V8PILcibEGb2fPfEOmw/0",
   "img":[
     "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic0FD3vN0V8PILcibEGb2fPfEOmw/0"
   ],
   "detail":[
     {"text":"testfirst"},
     {"img": 4whpV1VZl2iccsvYbHvnphkyGtnvjD3ul1UcLcwxrFdwTKYhH9Q5YZoCfX4Ncx655ZK6ibnlibCCErbKQtReySaVA/0"},
     {"text":"testagain"}
   ],
   "buy_limit":10
 },
 "sku_list":[
   {
     "sku_id":"1075741873:1079742386",
     "price":30,
     "icon_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl28bJj62XgfHPibY3ORKicN1oJ4CcoIr4BMbfA8LqyyjzOZzqrOGz3f5K Wq1QGP3fo6TOTSYD3TBQjuw/0",
     "product_code":"testing",
     "ori_price":9000000,
     "quantity":800
   },
   {
     "sku_id":"1075741873:1079742363",
     "price":30,
     "icon_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl28bJj62XgfHPibY3ORKicN1oJ4CcoIr4BMbfA8LqyyjzOZzqrOGz3f5K Wq1QGP3fo6TOTSYD3TBQjuw/0",
     "product_code":"testingtesting",
     "ori_price":9000000,
     "quantity":800
   }
 ],
 "attrext":{
   "location":{
     "country":"中国",
     "province":"广东省",
     "city":"广州市",
     "address":"T.I.T创意园"
   },
   "isPostFree":0,
   "isHasReceipt":1,
   "isUnderGuaranty":0,
   "isSupportReplace":0
 },
 "delivery_info":{
   "delivery_type":0,
   "template_id":0,
   "express":[
     {"id":10000027,"price":100},
     {"id":10000028,"price":100},
     {"id":10000029,"price":100}
   ]
 }
}
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
     
### getGoods
查询商品

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getGoods(productId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
 "product_info":{
   "product_id":"pDF3iY6Kr_BV_CXaiYysoGqJhppQ",
   "product_base":{
     "name":"testaddproduct",
     "category_id":[537074298],
     "img":[
       "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic0FD3vN0V8PILcibEGb2fPfEOmw/0"
     ],
     "property":[
       {"id":"品牌","vid":"Fujifilm/富⼠士"},
       {"id":"屏幕尺⼨寸","vid":"1.8英⼨寸"},
       {"id":"防抖性能","vid":"CCD防抖"}
     ],
     "sku_info":[
       {
         "id":"1075741873",
         "vid":[
           "1079742386",
           "1079742363"
         ]
       }
     ],
     "buy_limit":10,
     "main_img": 4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic 0FD3vN0V8PILcibEGb2fPfEOmw/0",
     "detail_html": "<div class=\"item_pic_wrp\" style= \"margin-bottom:8px;font-size:0;\"><img class=\"item_pic\" style= \"width:100%;\" alt=\"\" src=\"http://mmbiz.qpic.cn/mmbiz/ 4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic 0FD3vN0V8PILcibEGb2fPfEOmw/0\" ></div><p style=\"margin-bottom: 11px;margin-top:11px;\">test</p><div class=\"item_pic_wrp\" style=\"margin-bottom:8px;font-size:0;\"><img class=\"item_pic\" style=\"width:100%;\" alt=\"\" src=\"http://mmbiz.qpic.cn/mmbiz/ 4whpV1VZl2iccsvYbHvnphkyGtnvjD3ul1UcLcwxrFdwTKYhH9Q5YZoCfX4Ncx655 ZK6ibnlibCCErbKQtReySaVA/0\" ></div><p style=\"margin-bottom: 11px;margin-top:11px;\">test again</p>"
   },
   "sku_list":[
     {
       "sku_id":"1075741873:1079742386",
       "price":30,
       "icon_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2iccsvYbHvnphkyGtnvjD3ulEKogfsiaua49pvLfUS8Ym0GSYjViaLic0FD3vN0V8PILcibEGb2fPfEOmw/0",
       "quantity":800,
       "product_code":"testing",
       "ori_price":9000000
     },
     {
       "sku_id":"1075741873:1079742363",
       "price":30,
       "icon_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl28bJj62XgfHPibY3ORKicN1oJ4CcoIr4BMbfA8LqyyjzOZzqrOGz3f5KWq1QGP3fo6TOTSYD3TBQjuw/0",
       "quantity":800,
       "product_code":"testingtesting",
       "ori_price":9000000
     }
   ],
   "attrext":{
     "isPostFree":0,
     "isHasReceipt":1,
     "isUnderGuaranty":0,
     "isSupportReplace":0,
     "location":{
       "country":"中国",
       "province":"广东省",
       "city":"⼲州市",
       "address":"T.I.T创意园"
     }
   },
   "delivery_info":{
     "delivery_type":1,
     "template_id":103312920
   }
 }
}
```
     
     
### getGoodsByStatus
获取指定状态的所有商品

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getGoodsByStatus(GoodsStatus.ALL);           // 全部
api.getGoodsByStatus(GoodsStatus.UPPER_SHELF);   // 上架
api.getGoodsByStatus(GoodsStatus.LOWER_SHELF);   // 下架
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
 "products_info": [
   {
     "product_base": ...,
     "sku_list": ...,
     "attrext": ...,
     "delivery_info": ...,
     "product_id": "pDF3iY-mql6CncpbVajaB_obC321",
     "status": 1
   }
 ]
}
```
Param: 
- status {Number} 状态码。(0-全部, 1-上架, 2-下架)
     
     
### updateGoodsStatus
商品上下架

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateGoodsStatus(productId, GoodsStatus.ALL);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
- productId{String}  商品Id
- status {Number} 状态码。(0-全部, 1-上架, 2-下架)
     
     
### getSubCats
获取指定分类的所有子分类

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getSubCats(catId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
 "cate_list": [!
   {"id": "537074292","name": "数码相机"},
   {"id": "537074293","name": "家⽤用摄像机"},
   {"id": "537074298",! "name": "单反相机"}
 ]
}
```
Param: 
- {Number} cateId 大分类ID
     
     
### getSKUs
获取指定子分类的所有SKU

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getSKUs(catId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
 "sku_table": [
   {
     "id": "1075741873",
     "name": "颜⾊色",
     "value_list": [
       {"id": "1079742375", "name": "撞⾊色"},
       {"id": "1079742376","name": "桔⾊色"}
     ]
   }
 ]
}
```
Param: 
- {Number} cateId 大分类ID
     
     
### getProperties
获取指定分类的所有属性

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getProperties(catId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
 "properties": [
   {
     "id": "1075741879",
     "name": "品牌",
     "property_value": [
       {"id": "200050867","name": "VIC&#38"},
       {"id": "200050868","name": "Kate&#38"},
       {"id": "200050971","name": "M&#38"},
       {"id": "200050972","name": "Black&#38"}
     ]
   },
   {
     "id": "123456789",
     "name": "颜⾊色",
     "property_value": ...
   }
 ]
}
```
Param:
- cateId {Number} 分类ID
     
     
### createGoodsGroup
创建商品分组

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.createGoodsGroup(groupName, productList);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success",
 "group_id": 19
}
```
Param:
- groupName {String} 分组名
- productList {Array} 该组商品列表  

### deleteGoodsGroup
删除商品分组

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.deleteGoodsGroup(groupId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param:
- groupId {String} 分组ID


### updateGoodsGroup
修改商品分组属性

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateGoodsGroup(groupId, groupName);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param: 
- groupId {String}  分组ID
- groupName {String} 分组名

### updateGoodsForGroup
修改商品分组内的商品

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateGoodsForGroup(groupId, addProductList, delProductList);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param: 
- groupId {Object}  分组ID
- addProductList {Array}  待添加的商品数组
- delProductList {Array}  待删除的商品数组


### getAllGroups
获取所有商品分组

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getAllGroups();
```
Result:
```
{
"errcode": 0,
"errmsg": "success"
"groups_detail": [
  {
    "group_id": 200077549,
    "group_name": "新品上架"
  },{
    "group_id": 200079772,
    "group_name": "全球热卖"
  }
]
}
```


### getGroupById
根据ID获取商品分组

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getGroupById(groupId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
 "group_detail": {
   "group_id": 200077549,
   "group_name": "新品上架",
   "product_list": [
     "pDF3iYzZoY-Budrzt8O6IxrwIJAA",
     "pDF3iY3pnWSGJcO2MpS2Nxy3HWx8",
     "pDF3iY33jNt0Dj3M3UqiGlUxGrio"
   ]
 }
}
```
Param: 
- groupId {String}  分组ID

### updateStock
增加库存

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateStock(10, productId, sku); // 增加10件库存
api.updateStock(-10, productId, sku); // 减少10件库存
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param: 
- number {Number} 增加或者删除的数量
- productId {String}  商品ID
- sku {String} SKU信息



### createShelf
增加货架

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.createShelf(shelf);
```
Shelf:
```
{
  "shelf_data": {
    "module_infos": [
    {
      "group_info": {
        "filter": {
          "count": 2
        },
        "group_id": 50
      },
      "eid": 1
    },
    {
      "group_infos": {
        "groups": [
          {
            "group_id": 49
          },
          {
            "group_id": 50
          },
          {
            "group_id": 51
          }
        ]
      },
      "eid": 2
    },
    {
      "group_info": {
        "group_id": 52,
        "img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl29nqqObBwFwnIX3licVPnFV5Jm64z4I0TTicv0TjN7Vl9bykUUibYKIOjicAwIt6Oy0Y6a1Rjp5Tos8tg/0"
      },
      "eid": 3
    },
    {
      "group_infos": {
        "groups": [
          {
            "group_id": 49,
            "img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl29nqqObBwFwnIX3licVPnFV5uUQx7TLx4tB9qZfbe3JmqR4NkkEmpb5LUWoXF1ek9nga0IkeSSFZ8g/0"
          },
          {
            "group_id": 50,
            "img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl29nqqObBwFwnIX3licVPnFV5G1kdy3ViblHrR54gbCmbiaMnl5HpLGm5JFeENyO9FEZAy6mPypEpLibLA/0"
          },
          {
            "group_id": 52,
            "img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl29nqqObBwFwnIX3licVPnFV5uUQx7TLx4tB9qZfbe3JmqR4NkkEmpb5LUWoXF1ek9nga0IkeSSFZ8g/0"
          }
        ]
      },
      "eid": 4
    },
    {
      "group_infos": {
        "groups": [
          {
            "group_id": 43
          },
          {
            "group_id": 44
          },
          {
            "group_id": 45
          },
          {
            "group_id": 46
          }
        ],
      "img_background": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl29nqqObBwFwnIX3licVPnFV5uUQx7TLx4tB9qZfbe3JmqR4NkkEmpb5LUWoXF1ek9nga0IkeSSFZ8g/0"
      },
      "eid": 5
    }
    ]
  },
  "shelf_banner": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2ibrWQn8zWFUh1YznsMV0XEiavFfLzDWYyvQOBBszXlMaiabGWzz5B2KhNn2IDemHa3iarmCyribYlZYyw/0",
  "shelf_name": "测试货架"
}
```
Result:
```
{
  "errcode": 0,
  "errmsg": "success",
  "shelf_id": 12
}
```


### deleteShelf
删除货架

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.deleteShelf(shelfId);
```
Result:
```
{
  "errcode": 0,
  "errmsg": "success"
}
```
Param: 
- shelfId {String}  货架Id


### updateShelf
修改货架

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.updateShelf(shelf);
```
Shelf:
```
{
  "shelf_id": 12345,
  "shelf_data": ...,
  "shelf_banner": "http://mmbiz.qpic.cn/mmbiz/ 4whpV1VZl2ibrWQn8zWFUh1YznsMV0XEiavFfLzDWYyvQOBBszXlMaiabGWzz5B2K hNn2IDemHa3iarmCyribYlZYyw/0",
  "shelf_name": "货架名称"
}
```
Result:
```
{
  "errcode": 0,
  "errmsg": "success"
}
```


### getAllShelves
获取所有货架

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getAllShelf();
```
Result:
```
{
  "errcode": 0,
  "errmsg": "success",
  "shelves": [
    {
      "shelf_info": {
      "module_infos": [
        {
        "group_infos": {
          "groups": [
          {
            "group_id": 200080093
          },
          {
            "group_id": 200080118
          },
          {
            "group_id": 200080119
          },
          {
            "group_id": 200080135
          }
          ],
          "img_background": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl294FzPwnf9dAcaN7ButStztAZyy2yHY8pW6sTQKicIhAy5F0a2CqmrvDBjMFLtc2aEhAQ7uHsPow9A/0"
        },
        "eid": 5
        }
      ]
      },
      "shelf_banner": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl294FzPwnf9dAcaN7ButStztAZyy2yHY8pW6sTQKicIhAy5F0a2CqmrvDBjMFLtc2aEhAQ7uHsPow9A/0",
      "shelf_name": "新新人类",
      "shelf_id": 22
    },
    {
      "shelf_info": {
      "module_infos": [
        {
          "group_info": {
            "group_id": 200080119,
            "filter": {
              "count": 4
            }
          },
          "eid": 1
        }
      ]
      },
      "shelf_banner": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl294FzPwnf9dAcaN7ButStztAZyy2yHY8pW6sTQKicIhAy5F0a2CqmrvDBjMFLtc2aEhAQ7uHsPow9A/0",
      "shelf_name": "店铺",
      "shelf_id": 23
    }
  ]
}
```


### getShelfById
根据货架ID获取货架信息

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getShelfById(shelfId);
```
Result:
```
{
  "errcode": 0,
  "errmsg": "success",
  "shelf_info": {
    "module_infos": [...]
  },
  "shelf_banner": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2ibp2DgDXiaic6WdflMpNdInS8qUia2BztlPu1gPlCDLZXEjia2qBdjoLiaCGUno9zbs1UyoqnaTJJGeEew/0",
  "shelf_name": "新建货架",
  "shelf_id": 97
}
```
Param: 
- shelfId {String}  货架Id



### getOrderById
根据订单Id获取订单详情

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.(orderId);
```
Result:
```
{
  "errcode": 0,
  "errmsg": "success",
  "order": {
    "order_id": "7197417460812533543",
    "order_status": 6,
    "order_total_price": 6,
    "order_create_time": 1394635817,
    "order_express_price": 5,
    "buyer_openid": "oDF3iY17NsDAW4UP2qzJXPsz1S9Q",
    "buyer_nick": "likeacat",
    "receiver_name": "张小猫",
    "receiver_province": "广东省",
    "receiver_city": "广州市",
    "receiver_address": "华景路一号南方通信大厦5楼",
    "receiver_mobile": "123456789",
    "receiver_phone": "123456789",
    "product_id": "pDF3iYx7KDQVGzB7kDg6Tge5OKFo",
    "product_name": "安莉芳E-BRA专柜女士舒适内衣蕾丝3/4薄杯聚拢上托性感文胸KB0716",
    "product_price": 1,
    "product_sku": "10000983:10000995;10001007:10001010",
    "product_count": 1,
    "product_img": "http://img2.paipaiimg.com/00000000/item-52B87243-63CCF66C00000000040100003565C1EA.0.300x300.jpg",
    "delivery_id": "1900659372473",
    "delivery_company": "059Yunda",
    "trans_id": "1900000109201404103172199813"
  }
}
```
Param:
- orderId {String} 订单Id


### getOrdersByStatus
根据订单状态/创建时间获取订单详情

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.getOrdersByStatus([status,] [beginTime,] [endTime,]);
```
Usage:

当只传入callback参数时，查询所有状态，所有时间的订单

当传入一个参数，参数为Number类型，查询指定状态，所有时间的订单

当传入一个参数，参数为Date类型，查询所有状态，指定订单创建起始时间的订单(待测试)

当传入二个参数，第一参数为订单状态码，第二参数为订单创建起始时间

当传入三个参数，第一参数为订单状态码，第二参数为订单创建起始时间，第三参数为订单创建终止时间

Result:
```
{
  "errcode": 0,
  "errmsg": "success",
  "order_list": [
    {
      "order_id": "7197417460812533543",
      "order_status": 6,
      "order_total_price": 6,
      "order_create_time": 1394635817,
      "order_express_price": 5,
      "buyer_openid": "oDF3iY17NsDAW4UP2qzJXPsz1S9Q",
      "buyer_nick": "likeacat",
      "receiver_name": "张小猫",
      "receiver_province": "广东省",
      "receiver_city": "广州市",
      "receiver_address": "华景路一号南方通信大厦5楼",
      "receiver_mobile": "123456",
      "receiver_phone": "123456",
      "product_id": "pDF3iYx7KDQVGzB7kDg6Tge5OKFo",
      "product_name": "安莉芳E-BRA专柜女士舒适内衣蕾丝3/4薄杯聚拢上托性感文胸KB0716",
      "product_price": 1,
      "product_sku": "10000983:10000995;10001007:10001010",
      "product_count": 1,
      "product_img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2icND8WwMThBEcehjhDv2icY4GrDSG5RLM3B2qd9kOicWGVJcsAhvXfibhWRNoGOvCfMC33G9z5yQr2Qw/0",
      "delivery_id": "1900659372473",
      "delivery_company": "059Yunda",
      "trans_id": "1900000109201404103172199813"
    },
    {
      "order_id": "7197417460812533569",
      "order_status": 8,
      "order_total_price": 1,
      "order_create_time": 1394636235,
      "order_express_price": 0,
      "buyer_openid": "oDF3iY17NsDAW4UP2qzJXPsz1S9Q",
      "buyer_nick": "likeacat",
      "receiver_name": "张小猫",
      "receiver_province": "广东省",
      "receiver_city": "广州市",
      "receiver_address": "华景路一号南方通信大厦5楼",
      "receiver_mobile": "123456",
      "receiver_phone": "123456",
      "product_id": "pDF3iYx7KDQVGzB7kDg6Tge5OKFo",
      "product_name": "项坠333",
      "product_price": 1,
      "product_sku": "1075741873:1079742377",
      "product_count": 1,
      "product_img": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2icND8WwMThBEcehjhDv2icY4GrDSG5RLM3B2qd9kOicWGVJcsAhvXfibhWRNoGOvCfMC33G9z5yQr2Qw/0",
      "delivery_id": "1900659372473",
      "delivery_company": "059Yunda",
      "trans_id": "1900000109201404103172199813"
    }
  ]
}
```
Param:
- status {Number} 状态码。(无此参数-全部状态, 2-待发货, 3-已发货, 5-已完成, 8-维权中)
- beginTime {Date} 订单创建时间起始时间。(无此参数则不按照时间做筛选)
- endTime {Date} 订单创建时间终止时间。(无此参数则不按照时间做筛选)


### setExpressForOrder
设置订单发货信息

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.setExpressForOrder(orderId, deliveryCompany, deliveryTrackNo, isOthers);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param:
- orderId {String} 订单Id
- deliveryCompany {String} 物流公司 (物流公司Id请参考微信小店API手册)
- deliveryTrackNo {String} 运单Id
- isOthers {Boolean} 是否为6.4.5表之外的其它物流公司(0-否，1-是，无该字段默认为不是其它物流公司)
     
     
### setNoDeliveryForOrder
设置订单发货信息－不需要物流配送

适用于不需要实体物流配送的虚拟商品，完成本操作后订单即完成。

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.setNoDeliveryForOrder(orderId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param:
- orderId {String} 订单Id     


### closeOrder
关闭订单

详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>

Examples:
```
api.closeOrder(orderId);
```
Result:
```
{
 "errcode": 0,
 "errmsg": "success"
}
```
Param:
- {String} orderId 订单Id


### addPoi
创建门店 

Tips:

- 创建门店接口调用成功后不会实时返回poi_id。
- 成功创建后，门店信息会经过审核，审核通过后方可使用并获取poi_id。
- 图片photo_url必须为上传图片接口(api.uploadLogo，参见卡券接口)生成的url。
- 门店类目categories请参考微信公众号后台的门店管理部分。
Poi:
```
{
  "sid": "5794560",
  "business_name": "肯打鸡",
  "branch_name": "东方路店",
  "province": "上海市",
  "city": "上海市",
  "district": "浦东新区",
  "address": "东方路88号",
  "telephone": "021-5794560",
  "categories": ["美食,快餐小吃"],
  "offset_type": 1,
  "longitude": 125.5794560,
  "latitude": 45.5794560,
  "photo_list": [{
    "photo_url": "https://5794560.qq.com/1"
  }, {
    "photo_url": "https://5794560.qq.com/2"
  }],
  "recommend": "脉娜鸡腿堡套餐,脉乐鸡,全家捅",
  "special": "免费WIFE,外卖服务",
  "introduction": "肯打鸡是全球大型跨国连锁餐厅,2015年创立于米国,在世界上大约拥有3 亿间分店,主要售卖肯打鸡等垃圾食品",
  "open_time": "10:00-18:00",
  "avg_price": 88
}
```
Examples:
```
api.addPoi(poi);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```


### getPoi
获取门店信息

Examples:
```
api.getPoi(POI_ID);
```
Result:
```
{
  "sid": "5794560",
  "business_name": "肯打鸡",
  "branch_name": "东方路店",
  "province": "上海市",
  "city": "上海市",
  "district": "浦东新区",
  "address": "东方路88号",
  "telephone": "021-5794560",
  "categories": ["美食,快餐小吃"],
  "offset_type": 1,
  "longitude": 125.5794560,
  "latitude": 45.5794560,
  "photo_list": [{
    "photo_url": "https://5794560.qq.com/1"
  }, {
    "photo_url": "https://5794560.qq.com/2"
  }],
  "recommend": "脉娜鸡腿堡套餐,脉乐鸡,全家捅",
  "special": "免费WIFE,外卖服务",
  "introduction": "肯打鸡是全球大型跨国连锁餐厅,2015年创立于米国,在世界上大约拥有3 亿间分店,主要售卖肯打鸡等垃圾食品",
  "open_time": "10:00-18:00",
  "avg_price": 88,
  "available_state": 3,
  "update_status": 0
}
```
Param:
- poiId {String} 门店ID


### getPois
获取门店列表

Examples:
```
api.getPois(0, 20);
```
Result:
```
{
  "errcode": 0,
  "errmsg": "ok"
  "business_list": [{
    "base_info": {
      "sid": "100",
      "poi_id": "5794560",
      "business_name": "肯打鸡",
      "branch_name": "东方路店",
      "address": "东方路88号",
      "available_state": 3
    }
  }, {
    "base_info": {
      "sid": "101",
      "business_name": "肯打鸡",
      "branch_name": "西方路店",
      "address": "西方路88号",
      "available_state": 4
    }
  }],
  "total_count": "2",
}
```
Param: 
- begin {Number} 开始位置，0即为从第一条开始查询
- limit {Number} 返回数据条数，最大允许50，默认为20


### delPois
删除门店

Tips:
- 待审核门店不允许删除

Examples:
```
api.delPoi(POI_ID);
```
Param:
- poiId {String} 门店ID

### updatePoi
修改门店服务信息

Tips:
- 待审核门店不允许修改
Poi:
```
{
  "poi_id": "5794560",
  "telephone": "021-5794560",
  "photo_list": [{
    "photo_url": "https://5794560.qq.com/1"
  }, {
    "photo_url": "https://5794560.qq.com/2"
  }],
  "recommend": "脉娜鸡腿堡套餐,脉乐鸡,全家捅",
  "special": "免费WIFE,外卖服务",
  "introduction": "肯打鸡是全球大型跨国连锁餐厅,2015年创立于米国,在世界上大约拥有3 亿间分店,主要售卖肯打鸡等垃圾食品",
  "open_time": "10:00-18:00",
  "avg_price": 88
}
```
特别注意，以上7个字段，若有填写内容则为覆盖更新，若无内容则视为不修改，维持原有内容。

photo_list字段为全列表覆盖，若需要增加图片，需将之前图片同样放入list中，在其后增加新增图片。Examples:
```
api.updatePoi(poi);
```
Result:
```
{"errcode":0,"errmsg":"ok"}
```
Param:
- poi {Object} 门店对象