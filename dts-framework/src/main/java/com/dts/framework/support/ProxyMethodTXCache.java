package com.dts.framework.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jook
 * @create 2019-04-19 10:59
 **/
public class ProxyMethodTXCache {

    private static final Map<String, Annotation> CACHE = new ConcurrentHashMap<>();


    public static void put(Method key, Annotation annotation) {
        CACHE.put(key.toString(), annotation);
    }

    public static Annotation get(String key) {
        return CACHE.get(key);
    }

}
