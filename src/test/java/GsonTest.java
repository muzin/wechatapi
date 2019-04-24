import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GsonTest {

    private Gson gson = new Gson();
    private JsonParser jsonParser = new JsonParser();

    @Test
    public void GsonToJsonTest(){

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", "test");

        String data = gson.toJson(map);

        System.out.println(data);

        JsonObject dataJsonObj = (JsonObject) jsonParser.parse(data);



        Assert.assertTrue(
                "map 转换为 json",
                "{\"test\":\"test\"}".equals(data));

    }

}
