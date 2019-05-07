package com.dts.framework.jdk;

/**
 * @author jsun
 * @create 2019-04-12 11:20
 **/
public class DefaultFilterChain<T,S> extends FilterChain<T,S> {

    @Override
    public boolean excute(T o, S o2) {
        return true;
    }
}
