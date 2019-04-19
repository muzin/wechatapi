import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GsonTest {

    private Gson gson = new Gson();

    @Test
    public void GsonToJsonTest(){

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", "test");

        String data = gson.toJson(map);

        System.out.println(data);

        Assert.assertTrue(
                "map 转换为 json",
                "{\"test\":\"test\"}".equals(data));

    }

}
