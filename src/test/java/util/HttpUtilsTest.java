package util;

import cn.muzin.util.HttpUtils;
import org.junit.Test;
import wechatapi.ConfigEntity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilsTest {


    @Test
    public void uploadFormDataTest(){


        String url = "http://127.0.0.1:8087/file";
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("file", new File(ConfigEntity.filepath));
        data.put("text", "test1");
        HttpUtils.sendPostFormDataRequest(url, data);


    }

}
