package com.fangle;

import com.fangle.entity.*;
import com.fangle.resolver.TicketStorageResolver;
import com.fangle.resolver.TokenStorageResolver;
import com.fangle.util.Base64Utils;
import com.fangle.util.CryptoUtils;
import com.fangle.util.HttpUtils;
import com.google.gson.*;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class WechatAPI {

    private String appid;

    private String appsecret;

    private TokenStorageResolver tokenStorageResolver;

    private TicketStorageResolver ticketStorageResolver;

    private String PREFIX = "https://api.weixin.qq.com/cgi-bin/";

    private String MP_PREFIX = "https://mp.weixin.qq.com/cgi-bin/";

    private String FILE_SERVER_PREFIX = "http://file.api.weixin.qq.com/cgi-bin/";

    private String PAY_PREFIX = "https://api.weixin.qq.com/pay/";

    private String MERCHANT_PREFIX = "https://api.weixin.qq.com/merchant/";

    private String CUSTOM_SERVICE_PREFIX = "https://api.weixin.qq.com/customservice/";

    private String WXA_PREFIX = "https://api.weixin.qq.com/wxa/";

    private JsonParser jsonParser;

    private Gson gson;


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
     * WechatAPI api = new WechatAPI('appid', 'secret');
     * ```
     * ```
     * @param {String} appid 在公众平台上申请得到的appid
     * @param {String} appsecret 在公众平台上申请得到的app secret
     * @param {TokenStorageResolver} tokenStorageResolver 可选的。获取全局token对象的方法，多进程模式部署时需在意
     */
    public WechatAPI(String appid, String appsecret, TokenStorageResolver tokenStorageResolver){
        this.appid = appid;
        this.appsecret = appsecret;
        this.tokenStorageResolver = tokenStorageResolver;
        this.jsonParser = new JsonParser();
        this.gson = new Gson();

        this.ticketStorageResolver = new TicketStorageResolver(new TicketStore()) {
            @Override
            public Ticket getTicket(String type) {
                return this.getTicketStore().get(type);
            }

            @Override
            public void saveTicket(String type, Ticket ticket) {
                this.getTicketStore().put(type, ticket);
            }
        };

    }

    /**
     * 设置 tokenStorageResolver
     * @param tokenStorageResolver
     * @return
     */
    public WechatAPI setTokenStorageResolver(TokenStorageResolver tokenStorageResolver){
        this.tokenStorageResolver = tokenStorageResolver;
        return this;
    }

    /**
     * 设置 ticketStorageResolver
     * @param ticketStorageResolver
     * @return
     */
    public WechatAPI setTicketStorageResolver(TicketStorageResolver ticketStorageResolver){
        this.ticketStorageResolver = ticketStorageResolver;
        return this;
    }


    /*!
     * 根据创建API时传入的appid和appsecret获取access token
     * 进行后续所有API调用时，需要先获取access token
     * 详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=获取access_token> * 应用开发者无需直接调用本API。 * Examples:
     * ```
     * AccessToken token = api.getAccessToken();
     * ```
     * - `err`, 获取access token出现异常时的异常对象
     * - `result`, 成功时得到的响应结果 * Result:
     * ```
     * {"access_token": "ACCESS_TOKEN","expires_in": 7200}
     * ```
     */
    public AccessToken getAccessToken() {
        String url = this.PREFIX + "token?grant_type=client_credential&appid=" + this.appid + "&secret=" + this.appsecret;
        String dataStr = HttpUtils.sendGetRequest(url);
        JsonObject data = (JsonObject) jsonParser.parse(dataStr);

        // 过期时间，因网络延迟等，将实际过期时间提前10秒，以防止临界点
        Long expireTime = new Date().getTime() + (data.get("expires_in").getAsLong() - 10) * 1000;
        AccessToken token = new AccessToken(data.get("access_token").getAsString(), expireTime);

        tokenStorageResolver.saveToken(token);
        tokenStorageResolver.setAccessToken(token);

        return token;
    }

    /*!
     * 需要access token的接口调用如果采用preRequest进行封装后，就可以直接调用。
     * 无需依赖 getAccessToken 为前置调用。
     * 应用开发者无需直接调用此API。
     * Examples:
     * ```
     * api.ensureAccessToken();
     * ```
     */
    public AccessToken ensureAccessToken() {
        // 调用用户传入的获取token的异步方法，获得token之后使用（并缓存它）。
        AccessToken token = tokenStorageResolver.getAccessToken();
        if (token != null && token.isValid()) {
            return token;
        }
        return this.getAccessToken();
    }

    /**
     * 获取js sdk所需的有效js ticket
     * - `err`, 异常对象
     * - `result`, 正常获取时的数据 * Result:
     * - `errcode`, 0为成功
     * - `errmsg`, 成功为'ok'，错误则为详细错误信息
     * - `ticket`, js sdk有效票据，如：bxLdikRXVbTPdHSM05e5u5sUoXNKd8-41ZO3MhKoyN5OfkWITDGgnr2fwJ0m9E8NYzWKVZvdVtaUgWvsdshFKA
     * - `expires_in`, 有效期7200秒，开发者必须在自己的服务全局缓存jsapi_ticket
     */
    public Ticket getTicket (String type) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "ticket/getticket?access_token=" + accessToken + "&type=" + type;

        Map<String, Object> reqOpts = new HashMap<String, Object>();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        reqOpts.put("headers", headers);
        String dataStr = HttpUtils.sendGetRequest(url, reqOpts,"utf-8");
        JsonObject data = (JsonObject) jsonParser.parse(dataStr);

        // 过期时间，因网络延迟等，将实际过期时间提前10秒，以防止临界点
        Long expireTime = new Date().getTime() + (data.get("expires_in").getAsLong() - 10) * 1000;
        Ticket ticket = new Ticket(data.get("ticket").getAsString(), expireTime);
        ticketStorageResolver.saveTicket(type, ticket);
        return ticket;
    }

    public Ticket getTicket () {
        return this.getTicket("jsapi");
    }


    /**
     * 创建 随机字符串
     * @return
     */
    public static String createNonceStr () {
        String ret = "";
        byte[] tmp = new byte[20];
        for(int i = 0; i < 20; i++){
            tmp[i] = (byte) ((Math.random() * (122-97)) + 97);
        }
        return new String(tmp);
    }

    /**
     * 生成时间戳
     */
    public static String createTimestamp () {
        return "" + Math.floor(new Date().getTime() / 1000);
    }


    /*!
     * 排序查询字符串 */
    public static String raw (Map<String, String> args) {
        Set<String> setKeys = args.keySet();

        Map<String, String> map = new TreeMap<String, String>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        // 升序排序
                        return obj1.compareTo(obj2);
                    }
                });

        for(String key : setKeys){
            map.put(key, args.get(key));
        }

        Set<String> mapSetKeys = map.keySet();

        String string = "";
        for (String key : mapSetKeys) {
            Object val = args.get(key);
            string += '&' + key + '=' + val;
        }
        return string.substring(1);
    }

    /*!
     * 签名算法 * @param {String} nonceStr 生成签名的随机串
     * @param {String} jsapi_ticket 用于签名的jsapi_ticket
     * @param {String} timestamp 时间戳
     * @param {String} url 用于签名的url，注意必须与调用JSAPI时的页面URL完全一致 */
    public String ticketSign (String nonceStr, String jsapi_ticket, String timestamp, String url) {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonceStr);
        ret.put("timestamp", timestamp);
        ret.put("url", url);

        String string = raw(ret);
        MessageDigest SHA1MessageDigest = null;
        try {
            SHA1MessageDigest = CryptoUtils.SHA1MessageDigest();
            SHA1MessageDigest.reset();
            SHA1MessageDigest.update(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String signature = CryptoUtils.byteToStr(SHA1MessageDigest.digest());

        return signature;
    }

    /*!
     * 卡券card_ext里的签名算法
     * @name signCardExt
     * @param {String} api_ticket 用于签名的临时票据，获取方式见2.获取api_ticket。
     * @param {String} card_id 生成卡券时获得的card_id
     * @param {String} timestamp 时间戳，商户生成从1970 年1 月1 日是微信卡券接口文档00:00:00 至今的秒数,即当前的时间,且最终需要转换为字符串形式;由商户生成后传入。
     * @param {String} code 指定的卡券code 码，只能被领一次。use_custom_code 字段为true 的卡券必须填写，非自定义code 不必填写。
     * @param {String} openid 指定领取者的openid，只有该用户能领取。bind_openid 字段为true 的卡券必须填写，非自定义code 不必填写。
     * @param {String} balance 红包余额，以分为单位。红包类型（LUCKY_MONEY）必填、其他卡券类型不必填。 */
    public String signCardExt (String api_ticket, String card_id, String timestamp, String code, String openid, String balance) {

        List<String> values = new ArrayList<String>();
        values.add(api_ticket);
        values.add(card_id);
        values.add(timestamp);
        values.add(code != null ? code : "");
        values.add(openid != null ? openid : "");
        values.add(balance != null ? balance : "");

        Collections.sort(values, new Comparator< String >() {
            @Override
            public int compare(String lhs, String rhs) {
                int i = lhs.compareTo(rhs);
                if (i > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });


        String string = "";
        for(String i : values){
            string += i;
        }

        MessageDigest SHA1MessageDigest = null;
        try {
            SHA1MessageDigest = CryptoUtils.SHA1MessageDigest();
            SHA1MessageDigest.reset();
            SHA1MessageDigest.update(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String signature = CryptoUtils.byteToStr(SHA1MessageDigest.digest());

        return signature;
    };

    public Ticket ensureTicket (String type) {
        Ticket cache = ticketStorageResolver.getTicket(type);

        Ticket ticket = null;
        // 有ticket并且ticket有效直接调用
        if (cache != null) {
            ticket = new Ticket(cache.getTicket(), cache.getExpireTime());
        }

        // 没有ticket或者无效
        if (ticket == null || !ticket.isValid()) {
            // 从微信端获取ticket
            ticket = ticketStorageResolver.getTicket(type);
        }
        return ticket;
    };

    /**
     * 获取微信JS SDK Config的所需参数
     * Examples:
     * ```
     * var param = {
     *  debug: false,
     *  jsApiList: ['onMenuShareTimeline', 'onMenuShareAppMessage'],
     *  url: 'http://www.xxx.com'
     * };
     * api.getJsConfig(param);
     * ```
     * - `result`, 调用正常时得到的js sdk config所需参数
     * @param {Object} param 参数
     */
    public JsConfig getJsConfig (Map<String, Object> param) {
        Ticket ticket = this.ensureTicket("jsapi");
        String nonceStr = createNonceStr();
        String jsAPITicket = ticket.getTicket();
        String timestamp = createTimestamp();
        String signature = ticketSign(nonceStr, jsAPITicket, timestamp, param.get("url").toString());
        String debug = param.get("debug").toString();
        List<String> jsApiList = (List<String>) param.get("jsApiList");

        JsConfig jsConfig = new JsConfig(
                debug,
                this.appid,
                timestamp,
                nonceStr,
                signature,
                jsApiList);

        return jsConfig;
    }

    /**
     * 获取微信JS SDK Config的所需参数
     * Examples:
     * ```
     * var param = {
     *  card_id: 'p-hXXXXXXX',
     *  code: '1234',
     *  openid: '111111',
     *  balance: 100
     * };
     * api.getCardExt(param);
     * ```
     * - `result`, 调用正常时得到的card_ext对象，包含所需参数
     * @name getCardExt
     * @param {Object} param 参数
     */
    public Map<String, String> getCardExt (Map<String, String> param) {
        Ticket apiTicket = this.ensureTicket("wx_card");
        String timestamp = createTimestamp();
        String signature = signCardExt(apiTicket.getTicket(),
                param.get("card_id"),
                timestamp,
                param.get("code"),
                param.get("openid"),
                param.get("balance"));

        Map<String, String> result = new HashMap<String, String>();
        result.put("timestamp", timestamp);
        result.put("signature", signature);
        result.put("code", param.get("code") != null ? param.get("code").toString() : "");
        result.put("openid", param.get("openid") != null ? param.get("openid").toString() : "");

        if (param.containsKey("balance")) {
            result.put("balance", param.get("balance"));
        }

        return result;
    };

    /**
     * 获取最新的js api ticket
     * Examples:
     * ```
     * api.getLatestTicket();
     * ```
     * - `err`, 获取js api ticket出现异常时的异常对象
     * - `ticket`, 获取的ticket
     */
    public Ticket getLatestTicket () {
        return this.ensureTicket("jsapi");
    }


    /**
     * 获取微信服务器IP地址
     * 详情请见：<http://mp.weixin.qq.com/wiki/index.php?title=%E8%8E%B7%E5%8F%96%E5%BE%AE%E4%BF%A1%E6%9C%8D%E5%8A%A1%E5%99%A8IP%E5%9C%B0%E5%9D%80>
     * Examples:
     * ```
     * api.getIp();
     * ```
     * Result:
     * ```
     * ["127.0.0.1","127.0.0.1"]
     * ```
     */
    public List<String> getIp () {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=ACCESS_TOKEN
        String url = this.PREFIX + "getcallbackip?access_token=" + accessToken;

        Map<String, Object> reqOpts = new HashMap<String, Object>();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        reqOpts.put("headers", headers);
        String dataStr = HttpUtils.sendGetRequest(url, reqOpts,"utf-8");
        JsonObject data = (JsonObject) jsonParser.parse(dataStr);

        JsonArray array = data.get("ip_list").getAsJsonArray();

        List<String> list = new ArrayList<String>();
        for(int i = 0; i < array.size(); i++){
            list.add(array.get(i).getAsString());
        }

        return list;
    }


    /**
     * 获取客服聊天记录
     * 详细请看：http://mp.weixin.qq.com/wiki/19/7c129ec71ddfa60923ea9334557e8b23.html
     * Opts:
     * ```
     * {
     *  "starttime" : 123456789,
     *  "endtime" : 987654321,
     *  "openid": "OPENID", // 非必须
     *  "pagesize" : 10,
     *  "pageindex" : 1,
     * }
     * ```
     * Examples:
     * ```
     * JsonArray result = await api.getRecords(opts);
     * ```
     * Result:
     * ```
     * [
     *    {
     *      "worker": " test1",
     *      "openid": "oDF3iY9WMaswOPWjCIp_f3Bnpljk",
     *      "opercode": 2002,
     *      "time": 1400563710,
     *      "text": " 您好，客服test1为您服务。"
     *    },
     *    {
     *      "worker": " test1",
     *      "openid": "oDF3iY9WMaswOPWjCIp_f3Bnpljk",
     *      "opercode": 2003,
     *      "time": 1400563731,
     *      "text": " 你好，有什么事情？ "
     *    },
     *  ]
     * ```
     * @param {Object} opts 查询条件
     */
    public JsonArray getRecords (Map<String, Object> opts) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/customservice/msgrecord/getrecord?access_token=ACCESS_TOKEN
        String url = this.CUSTOM_SERVICE_PREFIX + "msgrecord/getrecord?access_token=" + accessToken;
        String data = gson.toJson(opts);
        String respStr = HttpUtils.sendPostJsonRequest(url, data);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        JsonArray recordlist = resp.get("recordlist").getAsJsonArray();
        return recordlist;
    };

    /**
     * 获取客服基本信息
     * 详细请看：http://dkf.qq.com/document-3_1.html
     * Examples:
     * ```
     * JsonArray result = api.getCustomServiceList();
     * ```
     * Result:
     * ```
     * [
     *     {
     *       "kf_account": "test1@test",
     *       "kf_nick": "ntest1",
     *       "kf_id": "1001"
     *     },
     *     {
     *       "kf_account": "test2@test",
     *       "kf_nick": "ntest2",
     *       "kf_id": "1002"
     *     },
     *     {
     *       "kf_account": "test3@test",
     *       "kf_nick": "ntest3",
     *       "kf_id": "1003"
     *     }
     *   ]
     * }
     * ```
     */
    public JsonArray getCustomServiceList () {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/cgi-bin/customservice/getkflist?access_token= ACCESS_TOKEN
        String url = this.PREFIX + "customservice/getkflist?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        JsonArray kf_list = resp.get("kf_list").getAsJsonArray();
        return kf_list;
    };

    /**
     * 获取在线客服接待信息
     * 详细请看：http://dkf.qq.com/document-3_2.html * Examples:
     * ```
     * JsonArray list = api.getOnlineCustomServiceList();
     * ```
     * Result:
     * ```
     * {
     *   "kf_online_list": [
     *     {
     *       "kf_account": "test1@test",
     *       "status": 1,
     *       "kf_id": "1001",
     *       "auto_accept": 0,
     *       "accepted_case": 1
     *     },
     *     {
     *       "kf_account": "test2@test",
     *       "status": 1,
     *       "kf_id": "1002",
     *       "auto_accept": 0,
     *       "accepted_case": 2
     *     }
     *   ]
     * }
     * ```
     */
    public JsonArray getOnlineCustomServiceList() {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/cgi-bin/customservice/getkflist?access_token= ACCESS_TOKEN
        String url = this.PREFIX + "customservice/getonlinekflist?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        JsonArray kf_online_list = resp.get("kf_online_list").getAsJsonArray();
        return kf_online_list;
    };

    /**
     * 添加客服账号
     * 详细请看：http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044813&token=&lang=zh_CN * Examples:
     * ```
     * boolean result = api.addKfAccount('test@test', 'nickname', 'password');
     * ```
     * Result:
     * ```
     * {
     *  "errcode" : 0,
     *  "errmsg" : "ok",
     * }
     * ```
     * @param {String} account 账号名字，格式为：前缀@公共号名字
     * @param {String} nick 昵称
     */
    public boolean addKfAccount (String account, String nick, String password) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/customservice/kfaccount/add?access_token=ACCESS_TOKEN
        String prefix = "https://api.weixin.qq.com/";
        String url = prefix + "customservice/kfaccount/add?access_token=" + accessToken;

        Map<String, String> data = new HashMap<String, String>();
        data.put("kf_account", account);
        data.put("nickname", nick);
        data.put("password", password);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));

        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        Integer errCode = resp.get("errcode").getAsInt();

        if(0 == errCode){
            return true;
        }else{
            return false;
        }

    };

    /**
     * 邀请绑定客服帐号
     * 详细请看：https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044813&token=&lang=zh_CN
     * Examples:
     * ```
     * boolean result = api.inviteworker('test@test', 'invite_wx');
     * ```
     * Result:
     * ```
     * {
     *  "errcode" : 0,
     *  "errmsg" : "ok",
     * }
     * ```
     * @param {String} account 账号名字，格式为：前缀@公共号名字
     * @param {String} wx 邀请绑定的个人微信账号
     */
    public boolean inviteworker (String account, String wx) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/customservice/kfaccount/inviteworker?access_token=ACCESS_TOKEN
        String prefix = "https://api.weixin.qq.com/";
        String url = prefix + "customservice/kfaccount/inviteworker?access_token=" + accessToken;

        Map<String, String> data = new HashMap<String, String>();
        data.put("kf_account", account);
        data.put("invite_wx", wx);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));

        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        Integer errCode = resp.get("errcode").getAsInt();

        if(0 == errCode){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 设置客服账号
     * 详细请看：http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044813&token=&lang=zh_CN * Examples:
     * ```
     * boolean result = api.updateKfAccount('test@test', 'nickname', 'password');
     * ```
     * Result:
     * ```
     * {
     *  "errcode" : 0,
     *  "errmsg" : "ok",
     * }
     * ```
     * @param {String} account 账号名字，格式为：前缀@公共号名字
     * @param {String} nick 昵称
     */
    public boolean updateKfAccount (String account, String nick, String password) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/customservice/kfaccount/add?access_token=ACCESS_TOKEN
        String prefix = "https://api.weixin.qq.com/";
        String url = prefix + "customservice/kfaccount/update?access_token=" + accessToken;
        Map<String, String> data = new HashMap<String, String>();
        data.put("kf_account", account);
        data.put("nickname", nick);
        data.put("password", password);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));

        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        Integer errCode = resp.get("errcode").getAsInt();

        if(0 == errCode){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 删除客服账号
     * 详细请看：http://mp.weixin.qq.com/wiki/9/6fff6f191ef92c126b043ada035cc935.html * Examples:
     * ```
     * api.deleteKfAccount('test@test');
     * ```
     * Result:
     * ```
     * {
     *  "errcode" : 0,
     *  "errmsg" : "ok",
     * }
     * ```
     * @param {String} account 账号名字，格式为：前缀@公共号名字
     */
     public boolean deleteKfAccount (String account) {
         AccessToken token = this.ensureAccessToken();
         String accessToken = token.getAccessToken();
            // https://api.weixin.qq.com/customservice/kfaccount/del?access_token=ACCESS_TOKEN
         String prefix = "https://api.weixin.qq.com/";
         String url = prefix + "customservice/kfaccount/del?access_token=" + accessToken + "&kf_account=" + account;

         Map<String, Object> reqOpts = new HashMap<String, Object>();
         Map<String, String> headers = new HashMap<String, String>();
         headers.put("content-type", "application/json");
         reqOpts.put("headers", headers);
         String dataStr = HttpUtils.sendGetRequest(url, reqOpts,"utf-8");

         JsonObject resp = (JsonObject) jsonParser.parse(dataStr);
         Integer errCode = resp.get("errcode").getAsInt();

         if(0 == errCode){
             return true;
         }else{
             return false;
         }
     };

    /**
     * 设置客服头像
     * 详细请看：http://mp.weixin.qq.com/wiki/9/6fff6f191ef92c126b043ada035cc935.html * Examples:
     * ```
     * api.setKfAccountAvatar('test@test', '/path/to/avatar.png');
     * ```
     * Result:
     * ```
     * {
     *  "errcode" : 0,
     *  "errmsg" : "ok",
     * }
     * ```
     * @param {String} account 账号名字，格式为：前缀@公共号名字
     * @param {String} filepath 头像路径
     */
    public boolean setKfAccountAvatar (String account, String filepath) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // http://api.weixin.qq.com/customservice/kfaccount/uploadheadimg?access_token=ACCESS_TOKEN&kf_account=KFACCOUNT
        Map<String, Object> formData = new HashMap<String, Object>();
        formData.put("media", new File(filepath));
        String prefix = "https://api.weixin.qq.com/";
        String url = prefix + "customservice/kfaccount/uploadheadimg?access_token=" + accessToken + "&kf_account=" + account;

        String respStr = HttpUtils.sendPostFormDataRequest(url, formData);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        Integer errCode = resp.get("errcode").getAsInt();

        if(0 == errCode){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 创建客服会话
     * 详细请看：http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1458044820&token=&lang=zh_CN * Examples:
     * ```
     * api.createKfSession('test@test', 'OPENID');
     * ```
     * Result:
     * ```
     * {
     *  "errcode" : 0,
     *  "errmsg" : "ok",
     * }
     * ```
     * @param {String} account 账号名字，格式为：前缀@公共号名字
     * @param {String} openid openid
     */
    public boolean createKfSession (String account, String openid) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();
        // https://api.weixin.qq.com/customservice/kfsession/create?access_token=ACCESS_TOKEN
        String prefix = "https://api.weixin.qq.com/";
        String url = prefix + "customservice/kfsession/create?access_token=" + accessToken;

        Map<String, String> data = new HashMap<String, String>();
        data.put("kf_account", account);
        data.put("openid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));

        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        Integer errCode = resp.get("errcode").getAsInt();

        if(0 == errCode){
            return true;
        }else{
            return false;
        }

    }

    /**
     * 上传Logo
     * Examples:
     * ```
     * api.uploadLogo('filepath');
     * ```
     *
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"ok",
     *  "url":"http://mmbiz.qpic.cn/mmbiz/iaL1LJM1mF9aRKPZJkmG8xXhiaHqkKSVMMWeN3hLut7X7hicFNjakmxibMLGWpXrEXB33367o7zHN0CwngnQY7zb7g/0"
     * }
     * ``` * @name uploadLogo
     * @param {String} filepath 文件路径
     */
    public JsonObject uploadLogo (String filepath) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        Map<String, Object> formData = new HashMap<String, Object>();
        formData.put("buffer", new File(filepath));

        String url = this.FILE_SERVER_PREFIX + "media/uploadimg?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostFormDataRequest(url, formData);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }

    /**
     * @name addLocations
     * @param {Array} locations 位置
     */
     public JsonObject addLocations (List<String> locations) {
         AccessToken token = this.ensureAccessToken();
         String accessToken = token.getAccessToken();

         Map<String, Object> data = new HashMap<String, Object>();
         data.put("location_list", locations);

         String url = "https://api.weixin.qq.com/card/location/batchadd?access_token=" + accessToken;

         String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
         JsonObject resp = (JsonObject) jsonParser.parse(respStr);
         return resp;
     };

    /**
     * @name getLocations
     * @param {Array} locations 位置
     */
    public JsonObject getLocations (String offset, int count) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("offset", offset);
        data.put("count", count);

        String url = "https://api.weixin.qq.com/card/location/batchget?access_token=" + accessToken;
        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    public JsonObject getColors () {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/getcolors?access_token=" + accessToken;
        Map<String, Object> reqOpts = new HashMap<String, Object>();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        reqOpts.put("headers", headers);
        String respStr = HttpUtils.sendGetRequest(url, reqOpts,"utf-8");
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }

    /**
     * 添加 卡劵
     * Example：
     * ```
     * JsonObject ret = api.createCard(card))
     * String card_id = res.get("card_id");
     * ```
     * @param card
     * @return
     */
    public JsonObject createCard (Map<String, Object> card) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/create?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("card", card);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    public void getRedirectUrl (String url, String encryptCode, String cardId) {
        // TODO
    };

    public JsonObject createQRCode (Map<String, Object> card) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/qrcode/create?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
            data.put("action_name", "QR_CARD");
            Map<String, Object> actionInfo = new HashMap<String, Object>();
                actionInfo.put("card", card);
            data.put("action_info", actionInfo);
            data.put("card", card);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    public JsonObject consumeCode (String code, String cardId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/code/consume?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", code);
        data.put("cardId", cardId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    public JsonObject decryptCode (String encryptCode) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/code/decrypt?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("encrypt_code", encryptCode);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    public JsonObject deleteCard (String cardId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/delete?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("card_id", cardId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    public JsonObject getCode (String code) {
        return getCode(code, null);
    }

    public JsonObject getCode (String code, String cardId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/code/get?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", code);
        if(cardId != null) {
            data.put("card_id", cardId);
        }
        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };


    public JsonObject getCards (int offset, int count) {
        return getCards(offset, count, null);
    }

    public JsonObject getCards (int offset, int count, List<String> status_list) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/batchget?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("offset", offset);
        data.put("count", count);
        if(status_list != null) {
            data.put("status_list", status_list);
        }
        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }

    public JsonObject getCard (String cardId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/get?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("card_id", cardId);
        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }
    /**
     * 获取用户已领取的卡券
     * 详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1451025272&token=&lang=zh_CN
     * Examples:
     * ```
     * api.getCardList('openid', 'card_id');
     * ```
     *
     * @param {String} openid 用户的openid
     * @param {String} cardId 卡券的card_id
     */
     public JsonObject getCardList (String openid, String cardId) {

         AccessToken token = this.ensureAccessToken();
         String accessToken = token.getAccessToken();

         String prefix = "https://api.weixin.qq.com/";
         String url = prefix + "card/user/getcardlist?access_token=" + accessToken;

         Map<String, Object> data = new HashMap<String, Object>();
         data.put("openid", openid);
         data.put("card_id", cardId);

         String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
         JsonObject resp = (JsonObject) jsonParser.parse(respStr);

         return resp;
     }

     public JsonObject updateCode (String code, String cardId, String newcode) {

         AccessToken token = this.ensureAccessToken();
         String accessToken = token.getAccessToken();

         String url = "https://api.weixin.qq.com/card/code/update?access_token=" + accessToken;

         Map<String, Object> data = new HashMap<String, Object>();
         data.put("code", code);
         data.put("card_id", cardId);
         data.put("newcode", newcode);

         String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
         JsonObject resp = (JsonObject) jsonParser.parse(respStr);

         return resp;
    }

    public JsonObject unavailableCode (String code) {
         return unavailableCode(code, null);
    }
    public JsonObject unavailableCode (String code, String cardId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/code/unavailable?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", code);

        if(cardId != null) {
            data.put("card_id", cardId);
        }

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject updateCard (String cardId, Map<String, Object> cardInfo) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/update?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("card_id", cardId);
        data.put("member_card", cardInfo);


        if(cardId != null) {
            data.put("card_id", cardId);
        }

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject updateCardStock (String cardId, int num) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/modifystock?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("card_id", cardId);

        if (num > 0) {
            data.put("increase_stock_value", Math.abs(num));
        } else {
            data.put("reduce_stock_value", Math.abs(num));
        }

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;

    };

    public JsonObject activateMembercard (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/membercard/activate?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject getActivateMembercardUrl (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/membercard/activate/geturl?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };


    public JsonObject updateMembercard (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/membercard/updateuser?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject getActivateTempinfo (Map<String, Object> activate_ticket) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/membercard/activatetempinfo/get?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("activate_ticket", activate_ticket);
        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject activateUserForm (Map<String, Object> data) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/membercard/activateuserform/set?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject updateMovieTicket (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/movieticket/updateuser?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject checkInBoardingPass (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/boardingpass/checkin?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject updateLuckyMonkeyBalance (String code, String cardId, String balance) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/luckymonkey/updateuserbalance?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", code);
        data.put("card_id", cardId);
        data.put("balance", balance);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject updateMeetingTicket (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/meetingticket/updateuser?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject setTestWhitelist (Map<String, Object> info) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/card/testwhitelist/set?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(info));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 公众平台官网数据统计模块
     * 详情请见：<http://mp.weixin.qq.com/wiki/8/c0453610fb5131d1fcb17b4e87c82050.html>
     * Examples:
     * ```
     * JsonArray result = api.datacube(
     *      DatacubeType.getArticleSummary,
     *      startDate,
     *      endDate
     *      );          // 获取接口分析分时数据
     * ```
     * >   // 用户分析数据接口
     * >   getUserSummary,                 // 获取用户增减数据
     * >   getUserCumulate,                // 获取累计用户数据
     * >
     * >   // 图文分析数据接口
     * >   getArticleSummary,              // 获取图文群发每日数据
     * >   getArticleTotal,                // 获取图文群发总数据
     * >   getUserRead,                    // 获取图文统计数据
     * >   getUserReadHour,                // 获取图文统计分时数据
     * >   getUserShare,                   // 获取图文分享转发数据
     * >   getUserShareHour,               // 获取图文分享转发分时数据
     * >
     * >   // 消息分析数据接口
     * >   getUpstreamMsg,                 //获取消息发送概况数据
     * >   getUpstreamMsgHour,             // 获取消息分送分时数据
     * >   getUpstreamMsgWeek,             // 获取消息发送周数据
     * >   getUpstreamMsgMonth,            // 获取消息发送月数据
     * >   getUpstreamMsgDist,             // 获取消息发送分布数据
     * >   getUpstreamMsgDistWeek,         // 获取消息发送分布周数据
     * >   getUpstreamMsgDistMonth,        // 获取消息发送分布月数据
     * >
     * >   // 接口分析数据接口
     * >   getInterfaceSummary,            // 获取接口分析数据
     * >   getInterfaceSummaryHour,        // 获取接口分析分时数据
     * > startDate 起始日期，格式为 2014-12-08
     * > endDate 结束日期，格式为 2014-12-08
     * Result:
     * ```
     * [{
     *     ...
     * }] // 详细请参见<http://mp.weixin.qq.com/wiki/8/c0453610fb5131d1fcb17b4e87c82050.html>
     *
     * ```
     * @param {String} startDate 起始日期，格式为2014-12-08
     * @param {String} endDate 结束日期，格式为2014-12-08
     */
    public JsonArray datacube (DatacubeType type, String begin, String end) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = "https://api.weixin.qq.com/datacube/" + type + "?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("begin_date", begin);
        data.put("end_date", end);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        JsonArray list = resp.get("list").getAsJsonArray();

        return list;
    }


    /**
     * 传输消息
     * Examples:
     * ```
     * JsonObject result = api.transferMessage(
     *      'deviceType',
     *      'deviceId',
     *      'openid',
     *      'content'
     * );
     * ```
     *
     * @param deviceType
     * @param deviceId
     * @param openid
     * @param content
     * @return
     */
    public JsonObject transferMessage (String deviceType, String deviceId, String openid, String content) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/transmsg?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/transmsg?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("device_type", deviceType);
        data.put("device_id", deviceId);
        data.put("open_id", openid);
        data.put("content", Base64Utils.encode(content));

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject transferStatus  (String deviceType, String deviceId, String openid, String status) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/transmsg?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/transmsg?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("device_type", deviceType);
        data.put("device_id", deviceId);
        data.put("open_id", openid);
        data.put("msg_type", 2);
        data.put("device_status", status);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 创建设备二维码
     * Examples:
     * ```
     * JsonObject result = api.createDeviceQRCode(List<String> deviceIds);
     * ```
     * @param deviceIds
     * @return
     */
    public JsonObject createDeviceQRCode (List<String> deviceIds) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/create_qrcode?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/create_qrcode?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("device_num", deviceIds.size());
        data.put("device_id_list", deviceIds);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject authorizeDevices (List<Map<String, Object>> devices, String optype) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/authorize_device?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/authorize_device?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("device_num", devices.size());
        data.put("device_list", devices);
        data.put("op_type", optype);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;

    };

    public JsonObject getDeviceQRCode () {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/getqrcode?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/getqrcode?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject bindDevice (String deviceId, String openid, String ticket) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/bind?access_token=ACCESS_TOKEN
        String  url = "https://api.weixin.qq.com/device/bind?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ticket", ticket);
        data.put("device_id", deviceId);
        data.put("openid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject unbindDevice (String deviceId, String openid, String ticket) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/unbind?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/unbind?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ticket", ticket);
        data.put("device_id", deviceId);
        data.put("openid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    public JsonObject compelBindDevice (String deviceId, String openid) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/compel_bind?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/compel_bind?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("device_id", deviceId);
        data.put("openid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    public JsonObject compelUnbindDevice (String deviceId, String openid) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/compel_unbind?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/compel_unbind?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("device_id", deviceId);
        data.put("openid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    public JsonObject getDeviceStatus (String deviceId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/get_stat?access_token=ACCESS_TOKEN&device_id=DEVICE_ID
        String url = "https://api.weixin.qq.com/device/get_stat?access_token=" + accessToken + "&device_id=" + deviceId;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    public JsonObject verifyDeviceQRCode  (String ticket) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/verify_qrcode?access_token=ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/device/verify_qrcode?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ticket", ticket);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    public JsonObject getOpenID (String deviceId, String deviceType) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/get_openid?access_token=ACCESS_TOKEN&device_type=DEVICE_TYPE&device_id=DEVICE_ID
        String url = "https://api.weixin.qq.com/device/get_openid?access_token="
                + accessToken
                + "&device_id="
                + deviceId
                + "&device_type="
                + deviceType;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    public JsonObject getBindDevice (String openid) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/device/get_bind_device?access_token=ACCESS_TOKEN&openid=OPENID
        String url = "https://api.weixin.qq.com/device/get_bind_device?access_token="
                + accessToken
                + "&openid="
                + openid;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 标记客户的投诉处理状态
     * Examples:
     * ```
     * api.updateFeedback(openid, feedbackId);
     * ```
     *
     * Result:
     * ```
     * {
     *  "errcode": 0,
     *  "errmsg": "success"
     * }
     * ```
     * @param {String} openid 用户ID
     * @param {String} feedbackId 投诉ID
     */
    public boolean updateFeedback (String openid, String feedbackId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String feedbackUrl = "https://api.weixin.qq.com/payfeedback/update?access_token=";
        // https://api.weixin.qq.com/payfeedback/update?access_token=xxxxx&openid=XXXX&feedbackid=xxxx
        String url = feedbackUrl
                + accessToken
                + "&openid="
                + openid
                + "&feedbackid="
                + feedbackId;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取分组列表
     * 详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>
     * Examples:
     * ```
     * api.getGroups();
     * ```
     * Result:
     * ```
     * {
     *  "groups": [
     *    {"id": 0, "name": "未分组", "count": 72596},
     *    {"id": 1, "name": "黑名单", "count": 36}
     *  ]
     * }
     * ```
     */
    public JsonArray getGroups () {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/groups/get?access_token=ACCESS_TOKEN
        String url = this.PREFIX + "groups/get?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        JsonArray groups = resp.get("groups").getAsJsonArray();
        return groups;
    };

    /**
     * 查询用户在哪个分组
     * 详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>
     * Examples:
     * ```
     * api.getWhichGroup(openid);
     * ```
     * Result:
     * ```
     * {
     *  "groupid": 102
     * }
     * ```
     * @param {String} openid Open ID
     */
    public JsonObject getWhichGroup (String openid) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/groups/getid?access_token=ACCESS_TOKEN
        String url = this.PREFIX + "groups/getid?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("openid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 创建分组
     * 详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>
     * Examples:
     * ```
     * api.createGroup('groupname');
     * ```
     * Result:
     * ```
     * {"group": {"id": 107, "name": "test"}}
     * ```
     * @param {String} name 分组名字
     */
    public JsonObject createGroup (String name) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/groups/create?access_token=ACCESS_TOKEN
        // POST数据格式：json
        // POST数据例子：{"group":{"name":"test"}}
        String url = this.PREFIX + "groups/create?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> group = new HashMap<String, Object>();
            group.put("name", name);
        data.put("group", group);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 更新分组名字
     * 详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>
     * Examples:
     * ```
     * api.updateGroup(107, 'new groupname');
     * ```
     * Result:
     * ```
     * {"errcode": 0, "errmsg": "ok"}
     * ```
     * @param {Number} id 分组ID
     * @param {String} name 新的分组名字
     */
    public boolean updateGroup (String id, String name) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // http请求方式: POST（请使用https协议）
        // https://api.weixin.qq.com/cgi-bin/groups/update?access_token=ACCESS_TOKEN
        // POST数据格式：json
        // POST数据例子：{"group":{"id":108,"name":"test2_modify2"}}
        String url = this.PREFIX + "groups/update?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> group = new HashMap<String, Object>();
                group.put("id", id);
                group.put("name", name);
        data.put("group", group);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 移动用户进分组
     * 详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>
     * Examples:
     * ```
     * api.moveUserToGroup(openid, groupId);
     * ```
     * Result:
     * ```
     * {"errcode": 0, "errmsg": "ok"}
     * ```
     * @param {String} openid 用户的openid
     * @param {Number} groupId 分组ID
     */
    public boolean moveUserToGroup (String openid, String groupId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // http请求方式: POST（请使用https协议）
        // https://api.weixin.qq.com/cgi-bin/groups/members/update?access_token=ACCESS_TOKEN
        // POST数据格式：json
        // POST数据例子：{"openid":"oDF3iYx0ro3_7jD4HFRDfrjdCM58","to_groupid":108}
        String url = this.PREFIX + "groups/members/update?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("openid", openid);
        data.put("to_groupid", groupId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 批量移动用户分组
     * 详情请见：<http://mp.weixin.qq.com/wiki/8/d6d33cf60bce2a2e4fb10a21be9591b8.html>
     * Examples:
     * ```
     * api.moveUsersToGroup(openids, groupId);
     * ```
     * Result:
     * ```
     * {"errcode": 0, "errmsg": "ok"}
     * ```
     * @param {String} openids 用户的openid数组
     * @param {Number} groupId 分组ID
     */
    public boolean moveUsersToGroup (List<String> openids, String groupId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // http请求方式: POST（请使用https协议）
        // https://api.weixin.qq.com/cgi-bin/groups/members/batchupdate?access_token=ACCESS_TOKEN
        // POST数据格式：json
        // POST数据例子：{"openid_list":["oDF3iYx0ro3_7jD4HFRDfrjdCM58","oDF3iY9FGSSRHom3B-0w5j4jlEyY"],"to_groupid":108}
        String url = this.PREFIX + "groups/members/batchupdate?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("openid_list", openids);
        data.put("to_groupid", groupId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };


    /**
     * 删除分组
     * 详情请见：<http://mp.weixin.qq.com/wiki/0/56d992c605a97245eb7e617854b169fc.html>
     * Examples:
     * ```
     * api.removeGroup(groupId);
     * ```
     * Result:
     * ```
     * {"errcode": 0, "errmsg": "ok"}
     * ```
     * @param {Number} groupId 分组ID
     */
    public boolean removeGroup (String groupId) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "groups/delete?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> group = new HashMap<String, Object>();
            group.put("id", groupId);
        data.put("group", group);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    }



    /**
     * 微信公众号支付: 发货通知
     * 详情请见：<http://mp.weixin.qq.com/htmledition/res/bussiness-faq/wx_mp_pay.zip> 接口文档订单发货通知 * Data:
     * ```
     * {
     *   "appid" : "wwwwb4f85f3a797777",
     *   "openid" : "oX99MDgNcgwnz3zFN3DNmo8uwa-w",
     *   "transid" : "111112222233333",
     *   "out_trade_no" : "555666uuu",
     *   "deliver_timestamp" : "1369745073",
     *   "deliver_status" : "1",
     *   "deliver_msg" : "ok",
     *   "app_signature" : "53cca9d47b883bd4a5c85a9300df3da0cb48565c",
     *   "sign_method" : "sha1"
     * }
     * ```
     * Examples:
     * ```
     * api.deliverNotify(data);
     * ```
     * Result:
     * ```
     * {"errcode":0, "errmsg":"ok"}
     * ``` * @param {Object} package package对象
     */
    public boolean deliverNotify (Map<String, Object> data) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PAY_PREFIX + "delivernotify?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 微信公众号支付: 订单查询
     * 详情请见：<http://mp.weixin.qq.com/htmledition/res/bussiness-faq/wx_mp_pay.zip> 接口文档订单查询部分 * Package:
     * ```
     * {
     *   "appid" : "wwwwb4f85f3a797777",
     *   "package" : "out_trade_no=11122&partner=1900090055&sign=4e8d0df3da0c3d0df38f",
     *   "timestamp" : "1369745073",
     *   "app_signature" : "53cca9d47b883bd4a5c85a9300df3da0cb48565c",
     *   "sign_method" : "sha1"
     * }
     * ```
     * Examples:
     * ```
     * api.orderQuery(query);
     * ```
     * Result:
     * ```
     * {
     *   "errcode":0,
     *   "errmsg":"ok",
     *   "order_info": {
     *     "ret_code":0,
     *     "ret_msg":"",
     *     "input_charset":"GBK",
     *     "trade_state":"0",
     *     "trade_mode":"1",
     *     "partner":"1900000109",
     *     "bank_type":"CMB_FP",
     *     "bank_billno":"207029722724",
     *     "total_fee":"1",
     *     "fee_type":"1",
     *     "transaction_id":"1900000109201307020305773741",
     *     "out_trade_no":"2986872580246457300",
     *     "is_split":"false",
     *     "is_refund":"false",
     *     "attach":"",
     *     "time_end":"20130702175943",
     *     "transport_fee":"0",
     *     "product_fee":"1",
     *     "discount":"0",
     *     "rmb_total_fee":""
     *   }
     * }
     * ``` * @param {Object} query query对象
     */
    public JsonObject orderQuery (Map<String, String> query) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PAY_PREFIX + "orderquery?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(query));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 创建临时二维码
     * 详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=生成带参数的二维码>
     * Examples:
     * ```
     * api.createTmpQRCode(10000, 1800);
     * ```
     *
     * Result:
     * ```
     * {
     *  "ticket":"gQG28DoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL0FuWC1DNmZuVEhvMVp4NDNMRnNRAAIEesLvUQMECAcAAA==",
     *  "expire_seconds":1800
     * }
     * ```
     * @param {Number} sceneId 场景ID
     * @param {Number} expire 过期时间，单位秒。最大不超过1800
     */
    public JsonObject createTmpQRCode  (Integer sceneId, Integer expire) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "qrcode/create?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> action_info = new HashMap<String, Object>();
                Map<String, Object> scene = new HashMap<String, Object>();
                scene.put("scene_id", sceneId);
            action_info.put("scene", scene);
        data.put("expire_seconds", expire);
        data.put("action_name", "QR_SCENE");
        data.put("action_info", action_info);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 创建永久二维码
     * 详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=生成带参数的二维码>
     * Examples:
     * ```
     * api.createLimitQRCode(100);
     * ```
     *
     * Result:
     * ```
     * {
     *  "ticket":"gQG28DoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL0FuWC1DNmZuVEhvMVp4NDNMRnNRAAIEesLvUQMECAcAAA=="
     * }
     * ```
     * @param {Number} sceneId 场景ID。ID不能大于100000
     */
    public String createLimitQRCode (Integer sceneId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "qrcode/create?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> action_info = new HashMap<String, Object>();
                Map<String, Object> scene = new HashMap<String, Object>();
                scene.put("scene_id", sceneId);
            action_info.put("scene", scene);
        data.put("action_name", "QR_LIMIT_SCENE");
        data.put("action_info", action_info);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        String ticket = resp.get("ticket").getAsString();

        return ticket;
    }

    /**
     * 生成显示二维码的链接。微信扫描后，可立即进入场景
     * Examples:
     * ```
     * api.showQRCodeURL(ticket);
     * // => https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET
     * ```
     * @param {String} ticket 二维码Ticket
     * @return {String} 显示二维码的URL地址，通过img标签可以显示出来 */
    public String showQRCodeURL (String ticket) {
        return this.MP_PREFIX + "showqrcode?ticket=" + ticket;
    }


    /**
     * 短网址服务
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=长链接转短链接接口
     * Examples:
     * ```
     * api.shortUrl('http://mp.weixin.com');
     * ```
     * @param {String} longUrl 需要转换的长链接，支持http://、https://、weixin://wxpay格式的url
     */
    public JsonObject shortUrl (String longUrl) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/shorturl?access_token=ACCESS_TOKEN
        String url = this.PREFIX + "shorturl?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();

        data.put("action", "long2short");
        data.put("long_url", longUrl);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }



    /**
     * 上传多媒体文件，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.uploadNews(news);
     * ```
     * News:
     * ```
     * [
     *    {
     *      "thumb_media_id":"qI6_Ze_6PtV7svjolgs-rN6stStuHIjs9_DidOHaj0Q-mwvBelOXCFZiq2OsIU-p",
     *      "author":"xxx",
     *      "title":"Happy Day",
     *      "content_source_url":"www.qq.com",
     *      "content":"content",
     *      "digest":"digest",
     *      "show_cover_pic":"1"
     *   },
     *   {
     *      "thumb_media_id":"qI6_Ze_6PtV7svjolgs-rN6stStuHIjs9_DidOHaj0Q-mwvBelOXCFZiq2OsIU-p",
     *      "author":"xxx",
     *      "title":"Happy Day",
     *      "content_source_url":"www.qq.com",
     *      "content":"content",
     *      "digest":"digest",
     *      "show_cover_pic":"0"
     *   }
     *  ]
     * ```
     * Result:
     * ```
     * {
     *  "type":"news",
     *  "media_id":"CsEf3ldqkAYJAU6EJeIkStVDSvffUJ54vqbThMgplD-VJXXof6ctX5fI6-aYyUiQ",
     *  "created_at":1391857799
     * }
     * ```
     * @param {Object} news 图文消息对象
     */
    public JsonObject uploadNews (List<Map<String, Object>> news) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=ACCESS_TOKEN
        String url = this.PREFIX + "media/uploadnews?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("articles", news);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 将通过上传下载多媒体文件得到的视频media_id变成视频素材
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.uploadMPVideo(opts);
     * ```
     * Opts:
     * ```
     * {
     *  "media_id": "rF4UdIMfYK3efUfyoddYRMU50zMiRmmt_l0kszupYh_SzrcW5Gaheq05p_lHuOTQ",
     *  "title": "TITLE",
     *  "description": "Description"
     * }
     * ```
     * Result:
     * ```
     * {
     *  "type":"video",
     *  "media_id":"IhdaAQXuvJtGzwwc0abfXnzeezfO0NgPK6AQYShD8RQYMTtfzbLdBIQkQziv2XJc",
     *  "created_at":1391857799
     * }
     * ```
     * @param {Object} opts 待上传为素材的视频
     */
    public JsonObject uploadMPVideo (Map<String, Object> opts) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://file.api.weixin.qq.com/cgi-bin/media/uploadvideo?access_token=ACCESS_TOKEN
        String url = this.FILE_SERVER_PREFIX + "media/uploadvideo?access_token=" + accessToken;
        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 群发消息，分别有图文（news）、文本(text)、语音（voice）、图片（image）和视频（video）
     * 详情请见：<https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1481187827_i0l21>
     * Examples:
     * ```
     * api.massSend(opts, receivers);
     * ```
     * opts:
     * ```
     * {
     *  "image":{
     *    "media_id":"123dsdajkasd231jhksad"
     *  },
     *  "msgtype":"image"
     *  "send_ignore_reprint":0
     * }
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {Object} opts 待发送的数据
     * @param {String|Array|Boolean} receivers 接收人。一个标签，或者openid列表,或者布尔值是否发送给全部用户
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {Int} sendIgnoreReprint 图文消息被判定为转载时，是否继续群发。 1为继续群发（转载），0为停止群发。 该参数默认为0。
     */
    public JsonObject massSend (Map<String, Object> opts) {
        return massSend(opts, true);
    }
    public JsonObject massSend (Map<String, Object> opts, Object receivers) {
        return massSend(opts, receivers, null);
    }
    public JsonObject massSend (Map<String, Object> opts, List<String> receivers) {
        return massSend(opts, receivers, null);
    }
    public JsonObject massSend (Map<String, Object> opts, String receivers) {
        return massSend(opts, receivers, null);
    }
    public JsonObject massSend (Map<String, Object> opts, Boolean receivers) {
        return massSend(opts, receivers, null);
    }
    public JsonObject massSend (Map<String, Object> opts, Object receivers, List<String> clientMsgId) {
        return massSend(opts, receivers, clientMsgId, 0);
    }
    public JsonObject massSend (Map<String, Object> opts, List<String> receivers, List<String> clientMsgId) {
        return massSend(opts, receivers, clientMsgId, 0);
    }
    public JsonObject massSend (Map<String, Object> opts, String receivers, List<String> clientMsgId) {
        return massSend(opts, receivers, clientMsgId, 0);
    }
    public JsonObject massSend (Map<String, Object> opts, Boolean receivers, List<String> clientMsgId) {
        return massSend(opts, receivers, clientMsgId, 0);
    }
    public JsonObject massSend (Map<String, Object> opts, Object receivers, List<String> clientMsgId, Integer sendIgnoreReprint) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = null;
        if (sendIgnoreReprint != null) {
            opts.put("send_ignore_reprint", sendIgnoreReprint);
        }
        if (clientMsgId != null) {
            opts.put("clientmsgid", clientMsgId);
        }
        if (receivers instanceof List) {
            opts.put("touser", receivers);
            url = this.PREFIX + "message/mass/send?access_token=" + accessToken;
        } else {
            Map<String, Object> filter = new HashMap<String, Object>();
            if (receivers instanceof Boolean) {
                filter.put("is_to_all", receivers);
            } else if(receivers instanceof String) {
                filter.put("tag_id", receivers);
            } else {
                filter.put("is_to_all", true);
            }
            url = this.PREFIX + "message/mass/sendall?access_token=" + accessToken;
        }
        // https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=ACCESS_TOKEN

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 群发图文（news）消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.massSendNews(mediaId, receivers);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {String} mediaId 图文消息的media id
     * @param {String|Array|Boolean} receivers 接收人。一个组，或者openid列表, 或者true（群发给所有人）
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {Int} sendIgnoreReprint 图文消息被判定为转载时，是否继续群发。 1为继续群发（转载），0为停止群发。 该参数默认为0。
     */
    public JsonObject massSendNews (String mediaId, Object receivers, List<String> clientMsgId, int sendIgnoreReprint) {
        Map<String, Object> opts = new HashMap<String, Object>();
            Map<String, Object> mpnews = new HashMap<String, Object>();
            mpnews.put("media_id", mediaId);
        opts.put("mpnews", mpnews);
        opts.put("msgtype", "mpnews");
        return this.massSend(opts, receivers, clientMsgId, sendIgnoreReprint);
    };

    /**
     * 群发文字（text）消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.massSendText(content, receivers);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {String} content 文字消息内容
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {String|Array} receivers 接收人。一个组，或者openid列表
     */
    public JsonObject massSendText (String content, Object receivers, List<String> clientMsgId) {
        Map<String, Object> opts = new HashMap<String, Object>();
            Map<String, Object> text = new HashMap<String, Object>();
            text.put("content", content);
        opts.put("text", text);
        opts.put("msgtype", "text");
        return this.massSend(opts, receivers, clientMsgId);
    };

    /**
     * 群发声音（voice）消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.massSendVoice(media_id, receivers);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {String} mediaId 声音media id
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {String|Array} receivers 接收人。一个组，或者openid列表
     */
    public JsonObject massSendVoice (String mediaId, Object receivers, List<String> clientMsgId) {
        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> voice = new HashMap<String, Object>();
        voice.put("media_id", mediaId);
        opts.put("voice", voice);
        opts.put("msgtype", "voice");
        return this.massSend(opts, receivers, clientMsgId);
    };

    /**
     * 群发图片（image）消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.massSendImage(media_id, receivers);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {String} mediaId 图片media id
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {String|Array} receivers 接收人。一个组，或者openid列表
     */
    public JsonObject massSendImage (String mediaId, Object receivers, List<String> clientMsgId) {
        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> image = new HashMap<String, Object>();
        image.put("media_id", mediaId);
        opts.put("image", image);
        opts.put("msgtype", "image");
        return this.massSend(opts, receivers, clientMsgId);
    };

    /**
     * 群发视频（video）消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.massSendVideo(mediaId, receivers);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {String} mediaId 视频media id
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {String|Array} receivers 接收人。一个组，或者openid列表
     */
    public JsonObject massSendVideo (String mediaId, Object receivers, List<String> clientMsgId) {
        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> mpvideo = new HashMap<String, Object>();
        mpvideo.put("media_id", mediaId);
        opts.put("mpvideo", mpvideo);
        opts.put("msgtype", "mpvideo");
        return this.massSend(opts, receivers, clientMsgId);
    };

    /**
     * 群发视频（video）消息，直接通过上传文件得到的media id进行群发（自动生成素材）
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.massSendMPVideo(data, receivers);
     * ```
     * Data:
     * ```
     * {
     *  "media_id": "rF4UdIMfYK3efUfyoddYRMU50zMiRmmt_l0kszupYh_SzrcW5Gaheq05p_lHuOTQ",
     *  "title": "TITLE",
     *  "description": "Description"
     * }
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id":34182
     * }
     * ```
     * @param {Object} data 视频数据
     * @param {String|Array} clientMsgId 开发者侧群发msgid，长度限制64字节，如不填，则后台默认以群发范围和群发内容的摘要值做为clientmsgid
     * @param {String|Array} receivers 接收人。一个组，或者openid列表
     */
    public JsonObject massSendMPVideo (Map<String, Object> data, Object receivers, List<String> clientMsgId) throws Exception {
        // 自动帮转视频的media_id
        JsonObject result = this.uploadMPVideo(data);
        if(!result.has("media_id")){
            throw new Exception("upload mpvideo faild");
        }
        String mediaId = result.get("media_id").getAsString();
        return this.massSendVideo(mediaId, receivers, clientMsgId);
    };

    /**
     * 删除群发消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.deleteMass(message_id);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"ok"
     * }
     * ```
     * @param {String} messageId 待删除群发的消息id
     */
    public boolean deleteMass  (String messageId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/delete?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        opts.put("msg_id", messageId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 预览接口，预览图文消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.previewNews(openid, mediaId);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id": 34182
     * }
     * ```
     * @param {String} openid 用户openid
     * @param {String} mediaId 图文消息mediaId
     */
    public JsonObject previewNews (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/preview?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> mpnews = new HashMap<String, Object>();
        mpnews.put("media_id", mediaId);
        opts.put("mpnews", mpnews);
        opts.put("msgtype", "mpnews");
        opts.put("touser", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 预览接口，预览文本消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.previewText(openid, content);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id": 34182
     * }
     * ```
     * @param {String} openid 用户openid
     * @param {String} content 文本消息
     */
    public JsonObject previewText (String openid, String content) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/preview?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> text = new HashMap<String, Object>();
        text.put("content", content);
        opts.put("text", text);
        opts.put("msgtype", "text");
        opts.put("touser", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 预览接口，预览语音消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.previewVoice(openid, mediaId);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id": 34182
     * }
     * ```
     * @param {String} openid 用户openid
     * @param {String} mediaId 语音mediaId
     */
    public JsonObject previewVoice (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/preview?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> voice = new HashMap<String, Object>();
        voice.put("media_id", mediaId);
        opts.put("voice", voice);
        opts.put("msgtype", "voice");
        opts.put("touser", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 预览接口，预览图片消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.previewImage(openid, mediaId);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id": 34182
     * }
     * ```
     * @param {String} openid 用户openid
     * @param {String} mediaId 图片mediaId
     */
    public JsonObject previewImage (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/preview?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> image = new HashMap<String, Object>();
        image.put("media_id", mediaId);
        opts.put("image", image);
        opts.put("msgtype", "image");
        opts.put("touser", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 预览接口，预览视频消息
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.previewVideo(openid, mediaId);
     * ```
     * Result:
     * ```
     * {
     *  "errcode":0,
     *  "errmsg":"send job submission success",
     *  "msg_id": 34182
     * }
     * ```
     * @param {String} openid 用户openid
     * @param {String} mediaId 视频mediaId
     */
    public JsonObject previewVideo (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/preview?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        Map<String, Object> mpvideo = new HashMap<String, Object>();
        mpvideo.put("media_id", mediaId);
        opts.put("mpvideo", mpvideo);
        opts.put("msgtype", "mpvideo");
        opts.put("touser", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 查询群发消息状态
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.getMassMessageStatus(messageId);
     * ```
     * Result:
     * ```
     * {
     *  "msg_id":201053012,
     *  "msg_status":"SEND_SUCCESS"
     * }
     * ```
     * @param {String} messageId 消息ID
     */
    public JsonObject getMassMessageStatus (String messageId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "message/mass/get?access_token=" + accessToken;

        Map<String, Object> opts = new HashMap<String, Object>();
        opts.put("msg_id", messageId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }





    /**
     * 上传永久素材，分别有图片（image）、语音（voice）、和缩略图（thumb）
     * 详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>
     * Examples:
     * ```
     * api.uploadMaterial('filepath', type);
     * ```
     * Result:
     * ```
     * {"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
     * ```
     * Shortcut:
     * - `uploadImageMaterial(filepath);`
     * - `uploadVoiceMaterial(filepath);`
     * - `uploadThumbMaterial(filepath);`
     * @param {String} filepath 文件路径
     * @param {String} type 媒体类型，可用值有image、voice、video、thumb
     */
    public JsonObject uploadMaterial (String filepath, MaterialType type) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/add_material?access_token=" + accessToken + "&type=" + type;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("media", new File(filepath));

        String respStr = HttpUtils.sendPostFormDataRequest(url, data);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;

    };

    public JsonObject uploadImageMaterial (String filepath) {
        return uploadMaterial(filepath, MaterialType.image);
    }

    public JsonObject uploadVoiceMaterial (String filepath) {
        return uploadMaterial(filepath, MaterialType.voice);
    }

    public JsonObject uploadThumbMaterial (String filepath) {
        return uploadMaterial(filepath, MaterialType.thumb);
    }

    /**
     * 上传永久素材，视频（video）
     * 详情请见：<http://mp.weixin.qq.com/wiki/14/7e6c03263063f4813141c3e17dd4350a.html>
     * Examples:
     * ```
     * var description = {
     *   "title":VIDEO_TITLE,
     *   "introduction":INTRODUCTION
     * };
     * api.uploadVideoMaterial('filepath', description);
     * ```
     *
     * Result:
     * ```
     * {"media_id":"MEDIA_ID"}
     * ```
     * @param {String} filepath 视频文件路径
     * @param {Object} description 描述
     */
    public JsonObject uploadVideoMaterial (String filepath, Map<String, Object> description) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/add_material?access_token=" + accessToken + "&type=video";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("media", new File(filepath));
        data.put("description", gson.toJson(description));

        String respStr = HttpUtils.sendPostFormDataRequest(url, data);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 新增永久图文素材
     *
     * News:
     * ```
     * [
     *    {
     *      "title": TITLE,
     *      "thumb_media_id": THUMB_MEDIA_ID,
     *      "author": AUTHOR,
     *      "digest": DIGEST,
     *      "show_cover_pic": SHOW_COVER_PIC(0 / 1),
     *      "content": CONTENT,
     *      "content_source_url": CONTENT_SOURCE_URL
     *    },
     *    //若新增的是多图文素材，则此处应还有几段articles结构
     *  ]
     * ```
     * Examples:
     * ```
     * api.uploadNewsMaterial(news);
     * ```
     *
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     * @param {Object} news 图文对象
     */
    public boolean uploadNewsMaterial (List<Map<String, Object>> news) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/add_news?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("articles", news);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 更新永久图文素材
     * News:
     * ```
     * {
     *  "media_id":MEDIA_ID,
     *  "index":INDEX,
     *  "articles": [
     *    {
     *      "title": TITLE,
     *      "thumb_media_id": THUMB_MEDIA_ID,
     *      "author": AUTHOR,
     *      "digest": DIGEST,
     *      "show_cover_pic": SHOW_COVER_PIC(0 / 1),
     *      "content": CONTENT,
     *      "content_source_url": CONTENT_SOURCE_URL
     *    },
     *    //若新增的是多图文素材，则此处应还有几段articles结构
     *  ]
     * }
     * ```
     * Examples:
     * ```
     * api.uploadNewsMaterial(news);
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     * @param {Object} news 图文对象
     */
    public boolean updateNewsMaterial (Map<String, Object> news) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/add_news?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(news));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 根据媒体ID获取永久素材
     * 详情请见：<http://mp.weixin.qq.com/wiki/4/b3546879f07623cb30df9ca0e420a5d0.html>
     * Examples:
     * ```
     * api.getMaterial('media_id');
     * ```
     *
     * - `result`, 调用正常时得到的文件Buffer对象
     * - `res`, HTTP响应对象
     * @param {String} mediaId 媒体文件的ID
     */
    public JsonObject getMaterial (String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/get_material?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("media_id", mediaId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    /**
     * 删除永久素材
     * 详情请见：<http://mp.weixin.qq.com/wiki/5/e66f61c303db51a6c0f90f46b15af5f5.html>
     * Examples:
     * ```
     * api.removeMaterial('media_id');
     * ```
     *
     * - `result`, 调用正常时得到的文件Buffer对象
     * - `res`, HTTP响应对象
     * @param {String} mediaId 媒体文件的ID
     */
    public JsonObject removeMaterial (String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/del_material?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("media_id", mediaId);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    /**
     * 获取素材总数
     * 详情请见：<http://mp.weixin.qq.com/wiki/16/8cc64f8c189674b421bee3ed403993b8.html>
     * Examples:
     * ```
     * api.getMaterialCount();
     * ```
     *
     * - `result`, 调用正常时得到的文件Buffer对象
     * - `res`, HTTP响应对象 * Result:
     * ```
     * {
     *  "voice_count":COUNT,
     *  "video_count":COUNT,
     *  "image_count":COUNT,
     *  "news_count":COUNT
     * }
     * ```
     */
    public JsonObject getMaterialCount () {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/get_materialcount?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    /**
     * 获取永久素材列表
     * 详情请见：<http://mp.weixin.qq.com/wiki/12/2108cd7aafff7f388f41f37efa710204.html>
     * Examples:
     * ```
     * api.getMaterials(type, offset, count);
     * ```
     *
     * - `result`, 调用正常时得到的文件Buffer对象
     * - `res`, HTTP响应对象 * Result:
     * ```
     * {
     *  "total_count": TOTAL_COUNT,
     *  "item_count": ITEM_COUNT,
     *  "item": [{
     *    "media_id": MEDIA_ID,
     *    "name": NAME,
     *    "update_time": UPDATE_TIME
     *  },
     *  //可能会有多个素材
     *  ]
     * }
     * ```
     * @param {String} type 素材的类型，图片（image）、视频（video）、语音 （voice）、图文（news）
     * @param {Number} offset 从全部素材的该偏移位置开始返回，0表示从第一个素材 返回
     * @param {Number} count 返回素材的数量，取值在1到20之间
     */
    public JsonObject getMaterials (String type, int offset, int count) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "material/batchget_material?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", type);
        data.put("offset", offset);
        data.put("count", count);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }

    /**
     * 发送语义理解请求
     * 详细请看：http://mp.weixin.qq.com/wiki/index.php?title=%E8%AF%AD%E4%B9%89%E7%90%86%E8%A7%A3 * Opts:
     * ```
     * {
     *   "query":"查一下明天从北京到上海的南航机票",
     *   "city":"北京",
     *   "category": "flight,hotel"
     * }
     * ```
     * Examples:
     * ```
     * api.semantic(uid, opts);
     * ```
     * Result:
     * ```
     * {
     *   "errcode":0,
     *   "query":"查一下明天从北京到上海的南航机票",
     *   "type":"flight",
     *   "semantic":{
     *       "details":{
     *           "start_loc":{
     *               "type":"LOC_CITY",
     *               "city":"北京市",
     *               "city_simple":"北京",
     *               "loc_ori":"北京"
     *               },
     *           "end_loc": {
     *               "type":"LOC_CITY",
     *               "city":"上海市",
     *               "city_simple":"上海",
     *               "loc_ori":"上海"
     *             },
     *           "start_date": {
     *               "type":"DT_ORI",
     *               "date":"2014-03-05",
     *               "date_ori":"明天"
     *             },
     *          "airline":"中国南方航空公司"
     *       },
     *   "intent":"SEARCH"
     * }
     * ```
     * @param {String} openid 用户ID
     * @param {Object} opts 查询条件
     */
    public JsonObject semantic (String openid, Map<String, Object> opts) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/semantic/semproxy/search?access_token=YOUR_ACCESS_TOKEN
        String url = "https://api.weixin.qq.com/semantic/semproxy/search?access_token=" + accessToken;
        opts.put("appid", this.appid);
        opts.put("uid", openid);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(opts));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }




    /**
     * 获取小程序二维码，适用于需要的码数量较少的业务场景
     * https://developers.weixin.qq.com/miniprogram/dev/api/createWXAQRCode.html
     * Examples:
     * ```
     * String path = 'index?foo=bar'; // 小程序页面路径
     * api.createWXAQRCode(path, width);
     * ```
     * @param {String} path 扫码进入的小程序页面路径，最大长度 128 字节，不能为空
     * @param {String} width 二维码的宽度，单位 px。最小 280px，最大 1280px
     */
    public JsonObject createWXAQRCode (String path, int width) throws Exception {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        if(width < 280 || width > 1280){
            if(width < 280){
                throw new Exception("the value of \"width\" is too small");
            }
            if(width < 1280){
                throw new Exception("the value of \"width\" is too large");
            }
        }

        String url = this.PREFIX + "wxaapp/createwxaqrcode?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("path", path);
        data.put("width", width);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 获取小程序码，适用于需要的码数量较少的业务场景
     * https://developers.weixin.qq.com/miniprogram/dev/api/getWXACode.html
     * Examples:
     * ```
     * var path = 'index?foo=bar'; // 小程序页面路径
     * api.getWXACode(path);
     * ```
     * @param {String} path 扫码进入的小程序页面路径，最大长度 128 字节，不能为空
     * @param {String} width 二维码的宽度，单位 px。最小 280px，最大 1280px
     * @param {String} auto_color 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
     * @param {Object} line_color auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
     * @param {Bool} is_hyaline 是否需要透明底色，为 true 时，生成透明底色的小程序码
     */
    public JsonObject getWXACode (String path, int width, boolean auto_color, Map<String, Object> line_color, boolean is_hyaline) {

        if(width < 280 || width > 1280){
            width = 430;
        }

        if(line_color == null){
            line_color = new HashMap<String, Object>();
            line_color.put("r", 0);
            line_color.put("g", 0);
            line_color.put("b", 0);
        }

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.WXA_PREFIX + "getwxacode?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("path", path);
        data.put("width", width);
        data.put("auto_color", auto_color);
        data.put("line_color", line_color);
        data.put("is_hyaline", is_hyaline);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };


    /**
     * 获取小程序码，适用于需要的码数量极多的业务场景
     * https://developers.weixin.qq.com/miniprogram/dev/api/getWXACodeUnlimit.html
     * Examples:
     * ```
     * var scene = 'foo=bar';
     * var page = 'pages/index/index'; // 小程序页面路径
     * api.getWXACodeUnlimit(scene, page);
     * ```
     * @param {String} scene 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     * @param {String} page 必须是已经发布的小程序存在的页面（否则报错），例如 pages/index/index, 根路径前不要填加 /,不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面
     * @param {String} width 二维码的宽度，单位 px。最小 280px，最大 1280px
     * @param {String} auto_color 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
     * @param {Object} line_color auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
     * @param {Bool} is_hyaline 是否需要透明底色，为 true 时，生成透明底色的小程序码
     */
    public JsonObject getWXACodeUnlimit (String scene, String page, int width, boolean auto_color, Map<String, Object> line_color, boolean is_hyaline) {

        if(width < 280 || width > 1280){
            width = 430;
        }

        if(line_color == null){
            line_color = new HashMap<String, Object>();
            line_color.put("r", 0);
            line_color.put("g", 0);
            line_color.put("b", 0);
        }

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.WXA_PREFIX + "getwxacodeunlimit?access_token=" + accessToken;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("scene", scene);
        data.put("page", page);
        data.put("width", width);
        data.put("auto_color", auto_color);
        data.put("line_color", line_color);
        data.put("is_hyaline", is_hyaline);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 上传图片
     * 详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=微信小店接口>
     * Examples:
     * ```
     * api.uploadPicture('/path/to/your/img.jpg');
     * ```
     *
     * Result:
     * ```
     * {
     *  "errcode": 0,
     *  "errmsg": "success"
     *  "image_url": "http://mmbiz.qpic.cn/mmbiz/4whpV1VZl2ibl4JWwwnW3icSJGqecVtRiaPxwWEIr99eYYL6AAAp1YBo12CpQTXFH6InyQWXITLvU4CU7kic4PcoXA/0"
     * }
     * ```
     * @param {String} filepath 文件路径
     */
     public JsonObject uploadPicture (String filepath) {

         AccessToken token = this.ensureAccessToken();
         String accessToken = token.getAccessToken();

         File file = new File(filepath);
         String basename = file.getName();

         String url = this.MERCHANT_PREFIX + "common/upload_img?access_token=" +
                 accessToken + "&filename=" + basename;

         String respStr = HttpUtils.sendPostFileRequest(url, file);
         JsonObject resp = (JsonObject) jsonParser.parse(respStr);

         return resp;
     }





    /**
     * 设置所属行业
     * Examples:
     * ```
     * Object industryIds = {
     *  "industry_id1":'1',
     *  "industry_id2":"4"
     * };
     * api.setIndustry(industryIds);
     * ```
     * @param {Object} industryIds 公众号模板消息所属行业编号
     */
    public JsonObject setIndustry (Map<String, Object> industryIds) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "template/api_set_industry?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(industryIds));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 获得模板ID
     * Examples:
     * ```
     * var templateIdShort = 'TM00015';
     * api.addTemplate(templateIdShort);
     * ```
     * @param {String} templateIdShort 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式
     */
    public JsonObject addTemplate (String templateIdShort) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "template/api_add_template?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("template_id_short", templateIdShort);

        String respStr = HttpUtils.sendPostJsonRequest(url, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 发送模板消息
     * Examples:
     * ```
     * String templateId: '模板id';
     * // URL置空，则在发送后,点击模板消息会进入一个空白页面（ios）, 或无法点击（android）
     * String url: 'http://weixin.qq.com/download';
     * String topcolor = '#FF0000'; // 顶部颜色
     * Object data = {
     *  user:{
     *    "value":'黄先生',
     *    "color":"#173177"
     *  }
     * };
     * api.sendTemplate('openid', templateId, url, topColor, data);
     * ```
     * @param {String} openid 用户的openid
     * @param {String} templateId 模板ID
     * @param {String} url URL置空，则在发送后，点击模板消息会进入一个空白页面（ios），或无法点击（android）
     * @param {String} topColor 字体颜色
     * @param {Object} data 渲染模板的数据
     * @param {Object} miniprogram 跳转小程序所需数据 {appid, pagepath}
     */
    public JsonObject sendTemplate (String openid,
                                    String templateId,
                                    String url,
                                    String topColor,
                                    Map<String, Object> data,
                                    Map<String, Object> miniprogram) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/template/send?access_token=" + accessToken;

        Map<String, Object> template = new HashMap<String, Object>();
        data.put("touser", openid);
        data.put("template_id", templateId);
        data.put("url", url);
        data.put("miniprogram", miniprogram);
        data.put("color", topColor);
        data.put("data", data);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(template));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    /**
     * 发送模板消息支持小程序
     * Examples:
     * ```
     * String templateId = '模板id';
     * String page = 'index?foo=bar'; // 小程序页面路径
     * String formId = '提交表单id';
     * String color = '#FF0000'; // 字体颜色
     * Object data = {
     *  keyword1: {
     *    "value":'黄先生',
     *    "color":"#173177"
     *  }
     * var emphasisKeyword = 'keyword1.DATA'
     * };
     * api.sendMiniProgramTemplate('openid', templateId, page, formId, data, color, emphasisKeyword);
     * ```
     * @param {String} openid 接收者（用户）的 openid
     * @param {String} templateId 所需下发的模板消息的id
     * @param {String} page 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
     * @param {String} formId 表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的 prepay_id
     * @param {Object} data 模板内容，不填则下发空模板
     * @param {String} color 模板内容字体的颜色，不填默认黑色 【废弃】
     * @param {String} emphasisKeyword 模板需要放大的关键词，不填则默认无放大
     */
    public JsonObject sendMiniProgramTemplate (String openid,
                                               String templateId,
                                               String page,
                                               String formId,
                                               Map<String, Object> data,
                                               String color,
                                               String emphasisKeyword) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/wxopen/template/send?access_token=" + accessToken;

        Map<String, Object> template = new HashMap<String, Object>();
        data.put("touser", openid);
        data.put("template_id", templateId);
        data.put("page", page);
        data.put("form_id", formId);
        data.put("data", data);
        data.put("color", color);
        data.put("emphasis_keyword", emphasisKeyword);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(template));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 新增临时素材，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * 详情请见：<http://mp.weixin.qq.com/wiki/5/963fc70b80dc75483a271298a76a8d59.html>
     * Examples:
     * ```
     * api.uploadMedia('filepath', type);
     * ```
     *
     * Result:
     * ```
     * {"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
     * ```
     * Shortcut:
     * - `api.uploadImageMedia(filepath);`
     * - `api.uploadVoiceMedia(filepath);`
     * - `api.uploadVideoMedia(filepath);`
     * - `api.uploadThumbMedia(filepath);`
     *
     * @param {String|InputStream} filepath 文件路径/文件Buffer数据
     * @param {String} type 媒体类型，可用值有image、voice、video、thumb
     */
    public JsonObject uploadMedia (Object filepath, String type) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "media/upload?access_token=" + accessToken + "&type=" + type;

        Map<String, Object> data = new HashMap<String, Object>();

        if(filepath instanceof String) {
            String filepathStr = (String) filepath;
            data.put("media", new File(filepathStr));
        }else if(filepath instanceof InputStream){
            data.put("media", filepath);
        }

        String respStr = HttpUtils.sendPostFormDataRequest(apiUrl, data);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };

    public JsonObject uploadImageMedia (Object filepath) {
        return uploadMedia(filepath, "image");
    }
    public JsonObject uploadVoiceMedia (Object filepath) {
        return uploadMedia(filepath, "voice");
    }
    public JsonObject uploadVideoMedia (Object filepath) {
        return uploadMedia(filepath, "video");
    }
    public JsonObject uploadThumbMedia (Object filepath) {
        return uploadMedia(filepath, "thumb");
    }

    /**
     * 获取临时素材
     * 详情请见：<http://mp.weixin.qq.com/wiki/11/07b6b76a6b6e8848e855a435d5e34a5f.html>
     * Examples:
     * ```
     * api.getMedia('media_id');
     * ```
     * - `result`, 调用正常时得到的文件Buffer对象
     * - `res`, HTTP响应对象
     * @param {String} mediaId 媒体文件的ID
     */
    public JsonObject getMedia (String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String url = this.PREFIX + "media/get?access_token=" + accessToken + "&media_id=" + mediaId;

        String respStr = HttpUtils.sendGetRequest(url);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    };
    /**
     * 上传图文消息内的图片获取URL
     * 详情请见：<http://mp.weixin.qq.com/wiki/15/5380a4e6f02f2ffdc7981a8ed7a40753.html>
     * Examples:
     * ```
     * api.uploadImage('filepath');
     * ```
     * Result:
     * ```
     * {"url":  "http://mmbiz.qpic.cn/mmbiz/gLO17UPS6FS2xsypf378iaNhWacZ1G1UplZYWEYfwvuU6Ont96b1roYsCNFwaRrSaKTPCUdBK9DgEHicsKwWCBRQ/0"}
     * ```
     * @param {String} filepath 图片文件路径
     */
    public JsonObject uploadImage (String filepath) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "media/uploadimg?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("media", new File(filepath));

        String respStr = HttpUtils.sendPostFormDataRequest(apiUrl, data);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);

        return resp;
    }


    /**
     * 创建自定义菜单
     * 详细请看：http://mp.weixin.qq.com/wiki/index.php?title=自定义菜单创建接口 * Menu:
     * ```
     * {
     *  "button":[
     *    {
     *      "type":"click",
     *      "name":"今日歌曲",
     *      "key":"V1001_TODAY_MUSIC"
     *    },
     *    {
     *      "name":"菜单",
     *      "sub_button":[
     *        {
     *          "type":"view",
     *          "name":"搜索",
     *          "url":"http://www.soso.com/"
     *        },
     *        {
     *          "type":"click",
     *          "name":"赞一下我们",
     *          "key":"V1001_GOOD"
     *        }]
     *      }]
     *    }
     *  ]
     * }
     * ```
     * Examples:
     * ```
     * var result = await api.createMenu(menu);
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     * @param {Object} menu 菜单对象
     */
    public boolean createMenu (Map<String, Object> menu) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "menu/create?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(menu));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else {
            return false;
        }
    };

    /**
     * 获取菜单
     * 详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=自定义菜单查询接口> * Examples:
     * ```
     * var result = await api.getMenu();
     * ```
     * Result:
     * ```
     * // 结果示例
     * {
     *  "menu": {
     *    "button":[
     *      {"type":"click","name":"今日歌曲","key":"V1001_TODAY_MUSIC","sub_button":[]},
     *      {"type":"click","name":"歌手简介","key":"V1001_TODAY_SINGER","sub_button":[]},
     *      {"name":"菜单","sub_button":[
     *        {"type":"view","name":"搜索","url":"http://www.soso.com/","sub_button":[]},
     *        {"type":"view","name":"视频","url":"http://v.qq.com/","sub_button":[]},
     *        {"type":"click","name":"赞一下我们","key":"V1001_GOOD","sub_button":[]}]
     *      }
     *    ]
     *  }
     * }
     * ```
     */
    public JsonObject getMenu () {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "menu/get?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(apiUrl);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        JsonObject menu = resp.get("menu").getAsJsonObject();
        return menu;
    };

    /**
     * 删除自定义菜单
     * 详细请看：<http://mp.weixin.qq.com/wiki/index.php?title=自定义菜单删除接口>
     * Examples:
     * ```
     * var result = await api.removeMenu();
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     */
    public boolean removeMenu () {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "menu/delete?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(apiUrl);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errcode = resp.get("errcode").getAsInt();
        if(errcode == 0){
            return true;
        }else {
            return false;
        }
    };

    /**
     * 获取自定义菜单配置
     * 详细请看：<http://mp.weixin.qq.com/wiki/17/4dc4b0514fdad7a5fbbd477aa9aab5ed.html>
     * Examples:
     * ```
     * var result = await api.getMenuConfig();
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     */
    public JsonObject getMenuConfig () {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "get_current_selfmenu_info?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(apiUrl);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }

    /**
     * 创建个性化自定义菜单
     * 详细请看：http://mp.weixin.qq.com/wiki/0/c48ccd12b69ae023159b4bfaa7c39c20.html * Menu:
     * ```
     * {
     *  "button":[
     *  {
     *      "type":"click",
     *      "name":"今日歌曲",
     *      "key":"V1001_TODAY_MUSIC"
     *  },
     *  {
     *    "name":"菜单",
     *    "sub_button":[
     *    {
     *      "type":"view",
     *      "name":"搜索",
     *      "url":"http://www.soso.com/"
     *    },
     *    {
     *      "type":"view",
     *      "name":"视频",
     *      "url":"http://v.qq.com/"
     *    },
     *    {
     *      "type":"click",
     *      "name":"赞一下我们",
     *      "key":"V1001_GOOD"
     *    }]
     * }],
     * "matchrule":{
     *  "group_id":"2",
     *  "sex":"1",
     *  "country":"中国",
     *  "province":"广东",
     *  "city":"广州",
     *  "client_platform_type":"2"
     *  }
     * }
     * ```
     * Examples:
     * ```
     * var result = await api.addConditionalMenu(menu);
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     * @param {Object} menu 菜单对象
     */
    public boolean addConditionalMenu (Map<String, Object> menu) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token=ACCESS_TOKEN
        String apiUrl = this.PREFIX + "menu/addconditional?access_token=" + accessToken;

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(menu));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 删除个性化自定义菜单
     * 详细请看：http://mp.weixin.qq.com/wiki/0/c48ccd12b69ae023159b4bfaa7c39c20.html * Menu:
     * ```
     * {
     *  "menuid":"208379533"
     * }
     * ```
     * Examples:
     * ```
     * var result = await api.delConditionalMenu(menuid);
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     * @param {String} menuid 菜单id
     */
    public boolean delConditionalMenu (String menuid) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/menu/delconditional?access_token=ACCESS_TOKEN
        String apiUrl = this.PREFIX + "menu/delconditional?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("menuid", menuid);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

    /**
     * 测试个性化自定义菜单
     * 详细请看：http://mp.weixin.qq.com/wiki/0/c48ccd12b69ae023159b4bfaa7c39c20.html * Menu:
     * ```
     * {
     *  "user_id":"nickma"
     * }
     * ```
     * Examples:
     * ```
     * var result = await api.tryConditionalMenu(user_id);
     * ```
     * Result:
     * ```
     * {
     *    "button": [
     *        {
     *            "type": "view",
     *            "name": "tx",
     *            "url": "http://www.qq.com/",
     *            "sub_button": [ ]
     *        },
     *        {
     *            "type": "view",
     *            "name": "tx",
     *            "url": "http://www.qq.com/",
     *            "sub_button": [ ]
     *        },
     *        {
     *            "type": "view",
     *            "name": "tx",
     *            "url": "http://www.qq.com/",
     *            "sub_button": [ ]
     *        }
     *    ]
     * }
     * ```
     * @param {String} user_id user_id可以是粉丝的OpenID，也可以是粉丝的微信号。
     */
    public JsonObject tryConditionalMenu (String user_id) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        // https://api.weixin.qq.com/cgi-bin/menu/trymatch?access_token=ACCESS_TOKEN
        String apiUrl = this.PREFIX + "menu/trymatch?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("user_id", user_id);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };


    /**
     * 客服消息，发送文字消息
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * api.sendText('openid', 'Hello world');
     * ```
     * @param {String} openid 用户的openid
     * @param {String} text 发送的消息内容
     */
    public JsonObject sendText (String openid, String text) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> textMap = new HashMap<String, Object>();
            textMap.put("content", text);
        data.put("touser", openid);
        data.put("msgtype", "text");
        data.put("text", textMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 客服消息，发送图片消息
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * api.sendImage('openid', 'media_id');
     * ```
     * @param {String} openid 用户的openid
     * @param {String} mediaId 媒体文件的ID，参见uploadMedia方法
     */
    public JsonObject sendImage (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> imageMap = new HashMap<String, Object>();
        imageMap.put("media_id", mediaId);
        data.put("touser", openid);
        data.put("msgtype", "image");
        data.put("image", imageMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }

    /**
     * 客服消息，发送卡券
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * api.sendCard('openid', 'card_id');
     * ```
     * @param {String} openid 用户的openid
     * @param {String} card_id 卡券的ID
     */
    public JsonObject sendCard (String openid, String cardid) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> wxcardMap = new HashMap<String, Object>();
        wxcardMap.put("card_id", cardid);
        data.put("touser", openid);
        data.put("msgtype", "wxcard");
        data.put("wxcard", wxcardMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }

    /**
     * 客服消息，发送语音消息
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * api.sendVoice('openid', 'media_id');
     * ```
     * @param {String} openid 用户的openid
     * @param {String} mediaId 媒体文件的ID
     */
    public JsonObject sendVoice (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> voiceMap = new HashMap<String, Object>();
        voiceMap.put("media_id", mediaId);
        data.put("touser", openid);
        data.put("msgtype", "voice");
        data.put("voice", voiceMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 客服消息，发送视频消息
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * api.sendVideo('openid', 'media_id', 'thumb_media_id');
     * ```
     * @param {String} openid 用户的openid
     * @param {String} mediaId 媒体文件的ID
     * @param {String} thumbMediaId 缩略图文件的ID
     */
    public JsonObject sendVideo (String openid, String mediaId, String thumbMediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> videoMap = new HashMap<String, Object>();
        videoMap.put("media_id", mediaId);
        videoMap.put("thumb_media_id", thumbMediaId);
        data.put("touser", openid);
        data.put("msgtype", "video");
        data.put("video", videoMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 客服消息，发送音乐消息
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * var music = {
     *  title: '音乐标题', // 可选
     *  description: '描述内容', // 可选
     *  musicurl: 'http://url.cn/xxx', 音乐文件地址
     *  hqmusicurl: "HQ_MUSIC_URL",
     *  thumb_media_id: "THUMB_MEDIA_ID"
     * };
     * api.sendMusic('openid', music);
     * ```
     * @param {String} openid 用户的openid
     * @param {Object} music 音乐文件
     */
    public JsonObject sendMusic (String openid, Map<String, Object> music) {
        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("touser", openid);
        data.put("msgtype", "music");
        data.put("music", music);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 客服消息，发送图文消息
     * 详细细节 http://mp.weixin.qq.com/wiki/index.php?title=发送客服消息
     * Examples:
     * ```
     * var articles = [
     *  {
     *    "title":"Happy Day",
     *    "description":"Is Really A Happy Day",
     *    "url":"URL",
     *    "picurl":"PIC_URL"
     *  },
     *  {
     *    "title":"Happy Day",
     *    "description":"Is Really A Happy Day",
     *    "url":"URL",
     *    "picurl":"PIC_URL"
     *  }];
     * api.sendNews('openid', articles);
     * ```
     * @param {String} openid 用户的openid
     * @param {Array} articles 图文列表
     */
    public JsonObject sendNews (String openid, List<Map<String, Object>> articles) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> newsMap = new HashMap<String, Object>();
            newsMap.put("articles", articles);
        data.put("touser", openid);
        data.put("msgtype", "news");
        data.put("news", newsMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 客服消息，发送图文消息（点击跳转到图文消息页面）
     * 详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140547
     * Examples:
     * ```
     * api.sendMpNews('openid', 'mediaId');
     * ```
     * @param {String} openid 用户的openid
     * @param {String} mediaId 图文消息媒体文件的ID
     */
    public JsonObject sendMpNews (String openid, String mediaId) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> mpnewsMap = new HashMap<String, Object>();
        mpnewsMap.put("media_id", mediaId);
        data.put("touser", openid);
        data.put("msgtype", "mpnews");
        data.put("mpnews", mpnewsMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 客服消息，发送小程序卡片（要求小程序与公众号已关联）
     * 详细细节 https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140547
     * Examples:
     * ```
     * var miniprogram = {
     *  title: '小程序标题', // 必填
     *  appid: '小程序appid', // 必填
     *  pagepath: 'pagepath', // 打开后小程序的地址，可以带query
     *  thumb_media_id: "THUMB_MEDIA_ID"
     * };
     * api.sendMiniProgram('openid', miniprogram);
     * ```
     * @param {String} openid 用户的openid
     * @param {Object} miniprogram 小程序信息
     */
    public JsonObject sendMiniProgram (String openid, Map<String, Object> miniprogram) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "message/custom/send?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("touser", openid);
        data.put("msgtype", "miniprogrampage");
        data.put("miniprogrampage", miniprogram);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    };

    /**
     * 获取自动回复规则
     * 详细请看：<http://mp.weixin.qq.com/wiki/19/ce8afc8ae7470a0d7205322f46a02647.html>
     * Examples:
     * ```
     * var result = await api.getAutoreply();
     * ```
     * Result:
     * ```
     * {
     * "is_add_friend_reply_open": 1,
     * "is_autoreply_open": 1,
     * "add_friend_autoreply_info": {
     *     "type": "text",
     *     "content": "Thanks for your attention!"
     * },
     * "message_default_autoreply_info": {
     *     "type": "text",
     *     "content": "Hello, this is autoreply!"
     * },
     * "keyword_autoreply_info": {
     *     "list": [
     *         {
     *             "rule_name": "autoreply-news",
     *             "create_time": 1423028166,
     *             "reply_mode": "reply_all",
     *             "keyword_list_info": [
     *                 {
     *                     "type": "text",
     *                     "match_mode": "contain",
     *                     "content": "news测试"//此处content即为关键词内容
     *                 }
     *             ],
     *             "reply_list_info": [
     *                 {
     *                     "type": "news",
     *                     "news_info": {
     *                         "list": [
     *                             {
     *                                 "title": "it's news",
     *                                 "author": "jim",
     *                                 "digest": "it's digest",
     *                                 "show_cover": 1,
     *                                 "cover_url": "http://mmbiz.qpic.cn/mmbiz/GE7et87vE9vicuCibqXsX9GPPLuEtBfXfKbE8sWdt2DDcL0dMfQWJWTVn1N8DxI0gcRmrtqBOuwQHeuPKmFLK0ZQ/0",
     *                                 "content_url": "http://mp.weixin.qq.com/s?__biz=MjM5ODUwNTM3Ng==&mid=203929886&idx=1&sn=628f964cf0c6d84c026881b6959aea8b#rd",
     *                                 "source_url": "http://www.url.com"
     *                             }
     *                         ]
     *                     }
     *                 },
     *                 {
     *                     ....
     *                 }
     *             ]
     *         },
     *         {
     *             "rule_name": "autoreply-voice",
     *             "create_time": 1423027971,
     *             "reply_mode": "random_one",
     *             "keyword_list_info": [
     *                 {
     *                     "type": "text",
     *                     "match_mode": "contain",
     *                     "content": "voice测试"
     *                 }
     *             ],
     *             "reply_list_info": [
     *                 {
     *                     "type": "voice",
     *                     "content": "NESsxgHEvAcg3egJTtYj4uG1PTL6iPhratdWKDLAXYErhN6oEEfMdVyblWtBY5vp"
     *                 }
     *             ]
     *         },
     *         ...
     *     ]
     * }
     * }
     * ```
     */
    public JsonObject getAutoreply () {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "get_current_autoreply_info?access_token=" + accessToken;

        String respStr = HttpUtils.sendGetRequest(apiUrl);
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        return resp;
    }


    /**
     * 创建门店 * Tips:
     * - 创建门店接口调用成功后不会实时返回poi_id。
     * - 成功创建后，门店信息会经过审核，审核通过后方可使用并获取poi_id。
     * - 图片photo_url必须为上传图片接口(api.uploadLogo，参见卡券接口)生成的url。
     * - 门店类目categories请参考微信公众号后台的门店管理部分。 * Poi:
     * ```
     * {
     *   "sid": "5794560",
     *   "business_name": "肯打鸡",
     *   "branch_name": "东方路店",
     *   "province": "上海市",
     *   "city": "上海市",
     *   "district": "浦东新区",
     *   "address": "东方路88号",
     *   "telephone": "021-5794560",
     *   "categories": ["美食,快餐小吃"],
     *   "offset_type": 1,
     *   "longitude": 125.5794560,
     *   "latitude": 45.5794560,
     *   "photo_list": [{
     *     "photo_url": "https://5794560.qq.com/1"
     *   }, {
     *     "photo_url": "https://5794560.qq.com/2"
     *   }],
     *   "recommend": "脉娜鸡腿堡套餐,脉乐鸡,全家捅",
     *   "special": "免费WIFE,外卖服务",
     *   "introduction": "肯打鸡是全球大型跨国连锁餐厅,2015年创立于米国,在世界上大约拥有3 亿间分店,主要售卖肯打鸡等垃圾食品",
     *   "open_time": "10:00-18:00",
     *   "avg_price": 88
     * }
     * ```
     * Examples:
     * ```
     * api.addPoi(poi);
     * ```
     * Result:
     * ```
     * {"errcode":0,"errmsg":"ok"}
     * ```
     * @name addPoi
     * @param {Object} poi 门店对象
     */
    public boolean addPoi (Map<String, Object> poi) {

        AccessToken token = this.ensureAccessToken();
        String accessToken = token.getAccessToken();

        String apiUrl = this.PREFIX + "poi/addpoi?access_token=" + accessToken;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> businessMap = new HashMap<String, Object>();
        data.put("base_info", poi);
        data.put("business", businessMap);

        String respStr = HttpUtils.sendPostJsonRequest(apiUrl, gson.toJson(data));
        JsonObject resp = (JsonObject) jsonParser.parse(respStr);
        int errCode = resp.get("errcode").getAsInt();
        if(errCode == 0){
            return true;
        }else{
            return false;
        }
    };

//    /**
//     * 获取门店信息 * Examples:
//     * ```
//     * api.getPoi(POI_ID);
//     * ```
//     * Result:
//     * ```
//     * {
//     *   "sid": "5794560",
//     *   "business_name": "肯打鸡",
//     *   "branch_name": "东方路店",
//     *   "province": "上海市",
//     *   "city": "上海市",
//     *   "district": "浦东新区",
//     *   "address": "东方路88号",
//     *   "telephone": "021-5794560",
//     *   "categories": ["美食,快餐小吃"],
//     *   "offset_type": 1,
//     *   "longitude": 125.5794560,
//     *   "latitude": 45.5794560,
//     *   "photo_list": [{
//     *     "photo_url": "https://5794560.qq.com/1"
//     *   }, {
//     *     "photo_url": "https://5794560.qq.com/2"
//     *   }],
//     *   "recommend": "脉娜鸡腿堡套餐,脉乐鸡,全家捅",
//     *   "special": "免费WIFE,外卖服务",
//     *   "introduction": "肯打鸡是全球大型跨国连锁餐厅,2015年创立于米国,在世界上大约拥有3 亿间分店,主要售卖肯打鸡等垃圾食品",
//     *   "open_time": "10:00-18:00",
//     *   "avg_price": 88,
//     *   "available_state": 3,
//     *   "update_status": 0
//     * }
//     * ```
//     * @name getPoi
//     * @param {Number} poiId 门店ID
//     */
//    public getPoi (String poiId) {
//  const { accessToken } = await this.ensureAccessToken();
//        var url = this.prefix + 'poi/getpoi?access_token=' + accessToken;
//        var data = {
//                poi_id: poiId
//  };
//        return this.request(url, postJSON(data));
//    };
//
///**
// * 获取门店列表
// * Examples:
// * ```
// * api.getPois(0, 20);
// * ```
// * Result:
// * ```
// * {
// *   "errcode": 0,
// *   "errmsg": "ok"
// *   "business_list": [{
// *     "base_info": {
// *       "sid": "100",
// *       "poi_id": "5794560",
// *       "business_name": "肯打鸡",
// *       "branch_name": "东方路店",
// *       "address": "东方路88号",
// *       "available_state": 3
// *     }
// *   }, {
// *     "base_info": {
// *       "sid": "101",
// *       "business_name": "肯打鸡",
// *       "branch_name": "西方路店",
// *       "address": "西方路88号",
// *       "available_state": 4
// *     }
// *   }],
// *   "total_count": "2",
// * }
// * ```
// * @name getPois
// * @param {Number} begin 开始位置，0即为从第一条开始查询
// * @param {Number} limit 返回数据条数，最大允许50，默认为20
// */
//    exports.getPois = async function (begin, limit) {
//  const { accessToken } = await this.ensureAccessToken();
//        var url = this.prefix + 'poi/getpoilist?access_token=' + accessToken;
//        var data = {
//                begin: begin,
//                limit: limit
//  };
//        return this.request(url, postJSON(data));
//    };
//
///**
// * 删除门店
// * Tips:
// * - 待审核门店不允许删除 * Examples:
// * ```
// * api.delPoi(POI_ID);
// * ```
// * @name delPoi
// * @param {Number} poiId 门店ID
// */
//    exports.delPoi = async function (poiId) {
//  const { accessToken } = await this.ensureAccessToken();
//        var url = this.prefix + 'poi/delpoi?access_token=' + accessToken;
//        var data = {
//                poi_id: poiId
//  };
//        return this.request(url, postJSON(data));
//    };
//
//    /**
//     * 修改门店服务信息 * Tips: * - 待审核门店不允许修改 * Poi:
//     * ```
//     * {
//     *   "poi_id": "5794560",
//     *   "telephone": "021-5794560",
//     *   "photo_list": [{
//     *     "photo_url": "https://5794560.qq.com/1"
//     *   }, {
//     *     "photo_url": "https://5794560.qq.com/2"
//     *   }],
//     *   "recommend": "脉娜鸡腿堡套餐,脉乐鸡,全家捅",
//     *   "special": "免费WIFE,外卖服务",
//     *   "introduction": "肯打鸡是全球大型跨国连锁餐厅,2015年创立于米国,在世界上大约拥有3 亿间分店,主要售卖肯打鸡等垃圾食品",
//     *   "open_time": "10:00-18:00",
//     *   "avg_price": 88
//     * }
//     * ```
//     * 特别注意，以上7个字段，若有填写内容则为覆盖更新，若无内容则视为不修改，维持原有内容。
//     * photo_list字段为全列表覆盖，若需要增加图片，需将之前图片同样放入list中，在其后增加新增图片。 * Examples:
//     * ```
//     * api.updatePoi(poi);
//     * ```
//     * Result:
//     * ```
//     * {"errcode":0,"errmsg":"ok"}
//     * ```
//     * @name updatePoi
//     * @param {Object} poi 门店对象
//     */
//        exports.updatePoi = async function (poi) {
//  const { accessToken } = await this.ensureAccessToken();
//        var data = {
//                business: {
//            base_info: poi
//        }
//  };
//        var url = this.prefix + 'poi/updatepoi?access_token=' + accessToken;
//        return this.request(url, postJSON(data));
//    };
    // TODO api_shakearound.js
    // TODO api_shop_common.js
    // TODO api_shop_express.js
    // TODO api_shop_goods.js
    // TODO api_shop_group.js
    // TODO api_shop_order.js
    // TODO api_shop_shelf.js
    // TODO api_shop_stock.js
    // TODO api_user.js


}

