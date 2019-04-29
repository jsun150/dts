package com.dts.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jsun
 * @create 2019-03-26 17:45
 **/
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TxServer {
    /**
     * 服务端接口对应的mq exchange和routekey
     *  exchange@routekey
     * @return
     */
    String mqInfo() default "";

    /**
     * 是否支持单接口mq反查
     * @return
     */
    boolean isSupporRecheck() default false;
}
