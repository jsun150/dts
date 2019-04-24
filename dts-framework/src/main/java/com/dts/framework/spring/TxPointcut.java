package com.dts.framework.spring;

import com.dts.framework.annotation.TxClient;
import com.dts.framework.annotation.TxServer;
import com.dts.framework.support.ProxyMethodTXCache;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.BeanFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 方法切点判断
 * StaticMethodMatcherPointcut
 *
 * @author Jook
 * @create 2019-03-25 19:33
 **/
public class TxPointcut extends StaticMethodMatcherPointcut implements Serializable {

    private BeanFactory beanFactory;

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        //初始化情况下匹配 - 类匹配
        if ((method.getAnnotation(TxClient.class) != null ? true : false || method.getAnnotation(TxServer.class) != null ? true : false)) {
            return true;
        }
        //dubbo 方法调用首次匹配 - 方法匹配(用于兼容cglib代理)
        if (method.getDeclaringClass().getName().startsWith("com.alibaba.dubbo")) {
            Annotation annotation = ProxyMethodTXCache.get(method.toString());
            if (annotation != null) {
                if (annotation instanceof TxServer) return true;
            } else {
                Class[] classes = method.getDeclaringClass().getInterfaces();
                for (Class cla_ : classes) {
                    try {
                        TxServer txServer;
                        if ((txServer = cla_.getMethod(method.getName(), method.getParameterTypes()).getAnnotation(TxServer.class)) != null) {
                            ProxyMethodTXCache.put(method, txServer);
                            return true;
                        }
                    } catch (NoSuchMethodException e) {
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        return false;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
