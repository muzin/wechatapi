package cn.muzin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    public static MessageDigest getMessageDigest(String type) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(type);
    }

    public static MessageDigest SHA1MessageDigest() throws NoSuchAlgorithmException {
        return getMessageDigest("SHA-1");
    }

    public static String byteToStr(byte[] byteArray) {
        StringBuilder sbd = new StringBuilder();
        for (byte aByteArray : byteArray) {
            sbd.append(byteToHexStr(aByteArray));
        }
        return sbd.toString();
    }

    public static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];

        return new String(tempArr);
    }
}
