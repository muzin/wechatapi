# 数据分析 API

- [datacube 数据立方体](#datacube)

### datacube
公众平台官网数据统计模块
详情请见：<http://mp.weixin.qq.com/wiki/8/c0453610fb5131d1fcb17b4e87c82050.html>
Examples:
```
JsonArray result = api.datacube(
     DatacubeType.getArticleSummary,
     startDate,
     endDate
     );          // 获取接口分析分时数据
```
> 数据立方体类型如下：
>
>   // 用户分析数据接口
>
>   getUserSummary,                 // 获取用户增减数据
>
>   getUserCumulate,                // 获取累计用户数据
>
>   // 图文分析数据接口
>
>   getArticleSummary,              // 获取图文群发每日数据
>
>   getArticleTotal,                // 获取图文群发总数据
>
>   getUserRead,                    // 获取图文统计数据
>
>   getUserReadHour,                // 获取图文统计分时数据
>
>   getUserShare,                   // 获取图文分享转发数据
>
>   getUserShareHour,               // 获取图文分享转发分时数据
>
>
>   // 消息分析数据接口
>
>   getUpstreamMsg,                 //获取消息发送概况数据
>
>   getUpstreamMsgHour,             // 获取消息分送分时数据
>
>   getUpstreamMsgWeek,             // 获取消息发送周数据
>
>   getUpstreamMsgMonth,            // 获取消息发送月数据
>
>   getUpstreamMsgDist,             // 获取消息发送分布数据
>
>   getUpstreamMsgDistWeek,         // 获取消息发送分布周数据
>
>   getUpstreamMsgDistMonth,        // 获取消息发送分布月数据
>
>
>   // 接口分析数据接口
>
>   getInterfaceSummary,            // 获取接口分析数据
>
>   getInterfaceSummaryHour,        // 获取接口分析分时数据
>
> 参数如下：
>
> startDate 起始日期，格式为 2014-12-08
>
> endDate 结束日期，格式为 2014-12-08

Result:
```
[{
    ...
}] // 详细请参见<http://mp.weixin.qq.com/wiki/8/c0453610fb5131d1fcb17b4e87c82050.html>
 *
```