package com.dts.framework.jdk;

/**
 * @author jsun
 * @create 2019-04-11 17:07
 **/
public interface Filter<T,S> {


    boolean excute(T t, S s);

}
