package cn.muzin.resolver;

import cn.muzin.entity.AccessToken;

public abstract class TokenStorageResolver {

    /**
     * Access Token 对象
     */
    private AccessToken accessToken;

    /**
     *
     * 是否使用 缓存 的 accessToken
     *
     * 默认不使用缓存，在多进程，多节点服务中建议关闭
     * 单进程服务建议开启缓存提高 获取 accessToken
     */
    private Boolean useCache = false;

    /**
     *
     * 获取 AccessToken
     *
     * 用于获取 存于文件，缓存，数据库 中的 accessToken
     *
     * `getToken` 是抽象方法，需要实现
     *
     * @return
     */
    public abstract AccessToken getToken();

    /**
     *
     * 获取 AccessToken
     *
     * 用于获取 存于文件，缓存，数据库 中的 accessToken
     *
     * @return
     */
    public abstract void saveToken(AccessToken accessToken);


    public TokenStorageResolver setUseCache(boolean bool){
        this.useCache = bool;
        return this;
    }

    public Boolean getUseCache(){
        return this.useCache;
    }

    public TokenStorageResolver setAccessToken(AccessToken token){
        this.accessToken = token;
        return this;
    }

    public AccessToken getAccessToken(){
        // 开启缓存 使用数据
        if(this.getUseCache() == true) {
            return this.accessToken;
        }else{
            // 未开启缓存，调用 抽象 getToken的实现 获取 AccessToken
            return this.getToken();
        }
    }

}
