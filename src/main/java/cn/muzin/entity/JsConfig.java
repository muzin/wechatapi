package cn.muzin.entity;

import java.util.List;

public class JsConfig {

    private String debug;

    private String appId;

    private String timestamp;

    private String nonceStr;

    private String signature;

    private List<String> jsApiList;

    public JsConfig () {}

    public JsConfig(String debug, String appId, String timestamp, String nonceStr, String signature, List<String> jsApiList){
        this.debug = debug;
        this.appId = appId;
        this.timestamp = timestamp;
        this.nonceStr = nonceStr;
        this.signature = signature;
        this.jsApiList = jsApiList;
    }

    public String getDebug() {
        return debug;
    }

    public String getAppId() {
        return appId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public String getSignature() {
        return signature;
    }

    public List<String> getJsApiList() {
        return jsApiList;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setJsApiList(List<String> jsApiList) {
        this.jsApiList = jsApiList;
    }
}
