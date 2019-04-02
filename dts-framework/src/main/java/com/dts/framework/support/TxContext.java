package com.dts.framework.support;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jook
 * @create 2019-03-26 16:14
 **/
public class TxContext {

    private boolean start = false;
    private boolean end = false;
    private String method;
    //{EXCHANGE}@{ROUTING}
    private List<String> checkMqList = new ArrayList<>();
    //回调确认方法
    private String check;
    private boolean success = false;
    //流水
    private String txFlowId;
    //mq消息体
    private TxMessage message;
    //事务开始结束时间
    private Long startTime = System.currentTimeMillis();
    private Long endTime;

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public TxMessage getMessage() {
        return message;
    }

    public void setMessage(TxMessage message) {
        this.message = message;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getTxFlowId() {
        return txFlowId;
    }

    public void setTxFlowId(String txFlowId) {
        this.txFlowId = txFlowId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getCheckMqList() {
        return checkMqList;
    }

    public void setCheckMqList(List<String> checkMqList) {
        this.checkMqList = checkMqList;
    }
}
