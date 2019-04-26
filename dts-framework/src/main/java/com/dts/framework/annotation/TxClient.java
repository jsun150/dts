package com.dts.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jsun
 * @create 2019-03-26 10:13
 **/
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TxClient {

    /**
     * 反查接口名字 参数都为json类型. 如果为空则不需要反查
     * @return
     */
    String recheckFunction() default "";

    /**
     * 首次反查的时间
     * 默认60
     * @return
     */
    long firstRecheckSecond() default 60L;

    /**
     * 首次反查如果还在执行中的 下一次反查的时间间隔.
     * @return
     */
    long delayTime() default 60*10L;

    CommintType commitType() default CommintType.AUTO;
}
