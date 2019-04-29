package com.dts.framework.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法对应的注解
 * @author jsun
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

    /**
     * 远程接口对应的mqinfo
     */
    private static final Map<String, String> METHOD_MQINFO = new ConcurrentHashMap<>();

    public static void putMethodMqinfo(String method, String mqinfo) {
        METHOD_MQINFO.put(method, mqinfo);
    }

    public static String getMqinfoByMethod(String method){
        return METHOD_MQINFO.get(method);
    }



}
