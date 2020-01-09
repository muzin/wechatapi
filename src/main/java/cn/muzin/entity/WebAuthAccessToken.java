package cn.muzin.entity;

import java.util.Date;

public class WebAuthAccessToken {

    private String accessToken;

    private Integer expiresIn;

    private String refreshToken;

    private String openid;

    private String scope;

    public WebAuthAccessToken(){

    }

    public String getAccessToken() {
        return accessToken;
    }

    public WebAuthAccessToken setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public WebAuthAccessToken setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public WebAuthAccessToken setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public WebAuthAccessToken setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public WebAuthAccessToken setScope(String scope) {
        this.scope = scope;
        return this;
    }

}
