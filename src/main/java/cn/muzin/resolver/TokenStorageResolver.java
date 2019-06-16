package cn.muzin.resolver;

import cn.muzin.entity.AccessToken;

public abstract class TokenStorageResolver {

    private AccessToken accessToken;

    public abstract AccessToken getToken();

    public abstract void saveToken(AccessToken accessToken);

    public TokenStorageResolver setAccessToken(AccessToken token){
        this.accessToken = token;
        return this;
    }

    public AccessToken getAccessToken(){
        return this.accessToken;
    }

}
