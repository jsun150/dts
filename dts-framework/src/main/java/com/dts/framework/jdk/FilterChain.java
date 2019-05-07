package com.dts.framework.jdk;

/**
 * @author jsun
 * @create 2019-04-11 17:06
 **/
public abstract class FilterChain<T,S> implements Filter<T,S>{

    private FilterChain filter;

    public void doChainExcute(T t, S s){
        if(excute(t, s) && filter != null) {
            filter.doChainExcute(t, s);
        }
    }

    public FilterChain addFilter(FilterChain filter) {
        this.filter = filter;
        return filter;
    }

}
