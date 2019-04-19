package com.fangle.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Base64Utils {

    /**
     * base64 编码器
     */
    private static BASE64Encoder encoder = new BASE64Encoder();

    private static BASE64Decoder decoder = new BASE64Decoder();

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
        String encodedText = encoder.encode(textByte);
        return encodedText;
    }

    /**
     * 解码
     * @param str
     * @return
     */
    public static String decode(String str){
        String encodedText = "";
        try {
            encodedText = new String(decoder.decodeBuffer(str), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedText;
    }

}
