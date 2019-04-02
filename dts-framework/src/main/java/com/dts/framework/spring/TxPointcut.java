package com.dts.framework.spring;

import com.dts.framework.annotation.TxClient;
import com.dts.framework.annotation.TxServer;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 方法切点判断
 *
 * @author Jook
 * @create 2019-03-25 19:33
 **/
public class TxPointcut extends StaticMethodMatcherPointcut implements Serializable {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return (method.getAnnotation(TxClient.class) != null ? true : false || method.getAnnotation(TxServer.class) != null ? true : false);
    }
}
