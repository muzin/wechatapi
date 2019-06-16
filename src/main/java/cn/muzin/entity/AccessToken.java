package cn.muzin.entity;

import cn.muzin.util.StringUtils;

import java.util.Date;

public class AccessToken {

    private String accessToken;

    private Long expireTime;

    public AccessToken(String accessToken, Long expireTime){
        this.accessToken = accessToken;
        this.expireTime = expireTime;
    }

    /**
     * 设置 token
     * Examples:
     * ```
     * token.setAccessToken("...");
     * ```
     * @param token accessToken
     * @return AccessToken
     */
    public AccessToken setAccessToken(String token){
        this.accessToken = token;
        return this;
    }

    /**
     * 获取 token
     * Examples:
     * ```
     * String token = token.getAccessToken();
     * ```
     * @return String
     */
    public String getAccessToken(){
        return this.accessToken;
    }

    /**
     * 获取过期时间
     * Examples:
     * ```
     * Long time = token.getExpireTime();
     * ```
     * @return String
     */
    public Long getExpireTime(){
        return this.expireTime;
    }

    /**
     * 设置 token
     * Examples:
     * ```
     * token.setExpireTime(123456789);
     * ```
     * @param expireTime 过期时间
     * @return AccessToken
     */
    public AccessToken setExpireTime(Long expireTime){
        this.expireTime = expireTime;
        return this;
    }


    /**
     * 检查AccessToken是否有效，检查规则为当前时间和过期时间进行对比
     * Examples:
     * ```
     * token.isValid();
     * ```
     */
    public boolean isValid () {
        return StringUtils.notEmpty(this.accessToken)
                && new Date().getTime() < this.expireTime;
    }

}
