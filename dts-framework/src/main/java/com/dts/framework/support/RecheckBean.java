package com.dts.framework.support;

import java.io.Serializable;

/**
 * @author jsun
 * @create 2019-03-26 19:50
 **/
public class RecheckBean implements Serializable{

    /**
     * 首次反查的时间
     * 默认60
     * @return
     */
    private long firstRecheckSecond;

    /**
     * 首次反查如果还在执行中的 下一次反查的时间间隔.毫秒
     * @return
     */
    private long delayTime;

    private String txFlowId;

    private String recheckFunction;
    /**
     * 执行次数
     */
    private Integer stime = 0;

    public Integer getStime() {
        return stime;
    }

    public void setStime(Integer stime) {
        this.stime = stime;
    }

    public long getFirstRecheckSecond() {
        return firstRecheckSecond;
    }

    public void setFirstRecheckSecond(long firstRecheckSecond) {
        this.firstRecheckSecond = firstRecheckSecond;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public String getTxFlowId() {
        return txFlowId;
    }

    public void setTxFlowId(String txFlowId) {
        this.txFlowId = txFlowId;
    }

    public String getRecheckFunction() {
        return recheckFunction;
    }

    public void setRecheckFunction(String recheckFunction) {
        this.recheckFunction = recheckFunction;
    }
}
