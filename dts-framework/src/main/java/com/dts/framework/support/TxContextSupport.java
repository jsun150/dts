package com.dts.framework.support;

/**
 * 上下文存储
 * @author Jook
 * @create 2019-03-26 17:28
 **/
public class TxContextSupport {

    private static final ThreadLocal<TxContext> LOCAL_TX_CONTEXT = new ThreadLocal<TxContext>() {
        @Override
        protected TxContext initialValue() {
            return new TxContext();
        }
    };

    static ThreadLocal<TxContext> getTxContextThreadLocal(){
        return LOCAL_TX_CONTEXT;
    }



}
