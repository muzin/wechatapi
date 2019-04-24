### 分组管理 API

- [getGroups 获取分组列表](#getGroups)
- [getWhichGroup 查询用户在哪个分组](#getWhichGroup)
- [createGroup 创建分组](#createGroup)
- [updateGroup 更新分组名字](#updateGroup)
- [moveUserToGroup 移动用户进分组](#moveUserToGroup)
- [moveUsersToGroup 批量移动用户分组](#moveUsersToGroup)
- [removeGroup 删除分组](#removeGroup)



### getGroups
获取分组列表

详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>

Examples:
```
api.getGroups();
```
Result:
```
{
 "groups": [
   {"id": 0, "name": "未分组", "count": 72596},
   {"id": 1, "name": "黑名单", "count": 36}
 ]
}
```

### getWhichGroup
查询用户在哪个分组

详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>

Examples:
```
api.getWhichGroup(openid);
```
Result:
```
{
 "groupid": 102
}
```
Param:
- {String} openid Open ID

### createGroup
创建分组

详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>

Examples:
```
api.createGroup('groupname');
```
Result:
```
{"group": {"id": 107, "name": "test"}}
```
Param:
- {String} name 分组名字

### updateGroup
更新分组名字

详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>

Examples:
```
api.updateGroup(107, 'new groupname');
```
Result:
```
{"errcode": 0, "errmsg": "ok"}
```
Param:
- {Number} id 分组ID
- {String} name 新的分组名字

### moveUserToGroup
移动用户进分组

详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>

Examples:
```
api.moveUserToGroup(openid, groupId);
```
Result:
```
{"errcode": 0, "errmsg": "ok"}
```
Param:
- {String} openid 用户的openid
- {Number} groupId 分组ID
     
### moveUsersToGroup
批量移动用户分组

详情请见：<http://mp.weixin.qq.com/wiki/8/d6d33cf60bce2a2e4fb10a21be9591b8.html>

Examples:
```
api.moveUsersToGroup(openids, groupId);
```
Result:
```
{"errcode": 0, "errmsg": "ok"}
```
Param:
- {String} openids 用户的openid数组
- {Number} groupId 分组ID


### removeGroup
删除分组

详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>

Examples:
```
api.removeGroup(groupId);
```
Result:
```
{"errcode": 0, "errmsg": "ok"}
```
Param:
- {Number} groupId 分组ID