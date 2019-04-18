package com.fanglesoft;

import com.fanglesoft.entity.AccessToken;
import com.fanglesoft.entity.WechatAPIOptions;
import com.fanglesoft.resolver.TokenStorageResolver;

public class WechatAPI {

    private String appid;

    private String appsecret;

    private TokenStorageResolver tokenStorageResolver;

    private String PREFIX = "https://api.weixin.qq.com/cgi-bin/";

    private String MP_PREFIX = "https://mp.weixin.qq.com/cgi-bin/";

    private String FILE_SERVER_PREFIX = "http://file.api.weixin.qq.com/cgi-bin/";

    private String PAY_PREFIX = "https://api.weixin.qq.com/pay/";

    private String MERCHANT_PREFIX = "https://api.weixin.qq.com/merchant/";

    private String CUSTOM_SERVICE_PREFIX = "https://api.weixin.qq.com/customservice/";

    private String WXA_PREFIX = "https://api.weixin.qq.com/wxa/";

    private WechatAPIOptions options;


    /**
     * 根据 appid 和 appsecret 创建API的构造函数
     * 如需跨进程跨机器进行操作Wechat API（依赖access token），access token需要进行全局维护
     * 使用策略如下：
     * 1. 调用用户传入的获取 token 的异步方法，获得 token 之后使用
     * 2. 使用appid/appsecret获取 token 。并调用用户传入的保存 token 方法保存
     * Tips:
     * - 如果跨机器运行wechat模块，需要注意同步机器之间的系统时间。
     * Examples:
     * ```
     * import com.fanglesoft.WechatAPI;
     * WechatAPI api = new WechatAPI('appid', 'secret');
     * ```
     * 以上即可满足单进程使用。
     * 当多进程时，token 需要全局维护，以下为保存 token 的接口。
     * ```
     * WechatAPI api = new WechatAPI('appid', 'secret');
     * ```
     * @param {String} appid 在公众平台上申请得到的appid
     * @param {String} appsecret 在公众平台上申请得到的app secret
     */
    public WechatAPI(String appid, String appsecret){
        this(appid, appsecret, new TokenStorageResolver() {
            @Override
            public AccessToken getToken() {
                return this.getAccessToken();
            }

            @Override
            public void saveToken(AccessToken accessToken) {
                this.setAccessToken(accessToken);
            }
        });
    }

    /**
     * 根据 appid 和 appsecret 创建API的构造函数
     * 如需跨进程跨机器进行操作Wechat API（依赖access token），access token需要进行全局维护
     * 使用策略如下：
     * 1. 调用用户传入的获取 token 的异步方法，获得 token 之后使用
     * 2. 使用appid/appsecret获取 token 。并调用用户传入的保存 token 方法保存
     * Tips:
     * - 如果跨机器运行wechat模块，需要注意同步机器之间的系统时间。
     * Examples:
     * ```
     * import com.fanglesoft.WechatAPI;
     * WechatAPI api = new WechatAPI('appid', 'secret');
     * ```
     * 以上即可满足单进程使用。
     * 当多进程时，token 需要全局维护，以下为保存 token 的接口。
     * ```
     * WechatAPI api = new WechatAPI('appid', 'secret', async function () {
     *   // 传入一个获取全局 token 的方法
     *   var txt = await fs.readFile('access_token.txt', 'utf8');
     *   return JSON.parse(txt);
     * }, async function (token) {
     *   // 请将 token 存储到全局，跨进程、跨机器级别的全局，比如写到数据库、redis等
     *   // 这样才能在cluster模式及多机情况下使用，以下为写入到文件的示例
     *   await fs.writeFile('access_token.txt', JSON.stringify(token));
     * });
     * ```
     * @param {String} appid 在公众平台上申请得到的appid
     * @param {String} appsecret 在公众平台上申请得到的app secret
     * @param {TokenStorageResolver} tokenStorageResolver 可选的。获取全局token对象的方法，多进程模式部署时需在意
     * @param {AsyncFunction} saveToken 可选的。保存全局token对象的方法，多进程模式部署时需在意
     */
    public WechatAPI(String appid, String appsecret, TokenStorageResolver tokenStorageResolver){

    }


    /**
     * 用于设置urllib的默认options * Examples:
     * ```
     * WechatAPIOptions options = new WechatAPIOptions();
     * options.put("timeout": 15000)
     * api.setOpts({timeout: 15000});
     * ```
     * @param {Object} opts 默认选项
     */
    public void setOpts(WechatAPIOptions opts) {
        this.options = opts;
    }

}
