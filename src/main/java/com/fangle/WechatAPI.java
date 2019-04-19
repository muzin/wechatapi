package com.fangle;

import com.fangle.entity.*;
import com.fangle.resolver.TicketStorageResolver;
import com.fangle.resolver.TokenStorageResolver;
import com.fangle.util.Base64Utils;
import com.fangle.util.CryptoUtils;
import com.fangle.util.HttpUtils;
import com.google.gson.*;

import java.io.File;
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

    private WechatAPIOptions options;

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

}

