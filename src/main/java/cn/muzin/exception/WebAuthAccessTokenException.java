package cn.muzin.exception;

public class WebAuthAccessTokenException extends Exception {

    public WebAuthAccessTokenException(String message){
        this(message, "");
    }

    public WebAuthAccessTokenException(String message, String respData){
        super(message + " " + respData);
    }

}
