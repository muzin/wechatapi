package cn.muzin.util;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import java.io.UnsupportedEncodingException;

public class Base64Utils {

    /**
     * base64 编码器
     */
    private static Encoder encoder = null;

    private static Decoder decoder = null;

    static {
        decoder = Base64.getDecoder();
        encoder = Base64.getEncoder();
    }

    private Base64Utils(){}

    /**
     * 编码
     * @param str
     * @return
     */
    public static String encode(String str){
        byte[] textByte = new byte[0];
        try {
            textByte = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] encodedByte = encoder.encode(textByte);
        String encodedText = "";
        try {
            encodedText = new String(encodedByte, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedText;
    }

    /**
     * 解码
     * @param str
     * @return
     */
    public static String decode(String str){
        byte[] textByte = new byte[0];
        try {
            textByte = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] decodedByte = decoder.decode(textByte);
        String decodedText = "";
        try {
            decodedText = new String(decodedByte, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedText;
    }

}
