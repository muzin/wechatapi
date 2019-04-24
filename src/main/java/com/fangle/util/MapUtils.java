package com.fangle.util;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapUtils {

    private MapUtils(){}

    public static Map<String, Object> createHashMap(){
        return new HashMap<String, Object>();
    }

    /**
     * 合并Map对象
     * @param map1
     * @param map2
     * @return
     */
    public static Map<String, Object> assign(Map<String, Object> map1, Map<String, Object> map2){
        Map<String, Object> newMap = new HashMap<String, Object>();

        Set<String> map1Sets = map1.keySet();
        Set<String> map2Sets = map2.keySet();

        for(String key : map1Sets){
            newMap.put(key, map1.get(key));
        }

        for(String key : map2Sets){
            newMap.put(key, map2.get(key));
        }

        return newMap;
    }


}
