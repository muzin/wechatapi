package wechatapi;

import java.io.*;

public class ConfigEntity {

    public static String appid = null;

    public static String appsecret = null;

    // 在此文件中写入  appid 和 appsecret，    用空格隔开
    public static String filepath = "/home/www/wechatapi.conf";

    static {

        File file = new File(filepath);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String str = null;
        try {
            str = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        appid = str.split(" ")[0];
        appsecret = str.split(" ")[1];

    }

}
