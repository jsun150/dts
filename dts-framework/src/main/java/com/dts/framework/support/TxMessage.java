package com.dts.framework.support;

/**
 * @author Jook
 * @create 2019-03-26 16:38
 **/
public class TxMessage {

    private boolean success;
    private String txFlow;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTxFlow() {
        return txFlow;
    }

    public void setTxFlow(String txFlow) {
        this.txFlow = txFlow;
    }
}
