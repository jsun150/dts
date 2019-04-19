package com.dts.framework.support;

import com.dts.framework.annotation.TxClient;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jook
 * @create 2019-03-26 16:14
 **/
public class TxContext {

    private boolean start = false;
    private String method;
    //调用的所有tx rpc方法
    private List<String> invokeMethodNameList = new ArrayList<>();
    private boolean success = false;
    //流水
    private String txFlowId;
    //mq消息体
    private TxMessage message;
    //事务开始结束时间
    private Long startTime = System.currentTimeMillis();
    private Long endTime;
    //方法对应的回调路径
    private Map<String, RecheckBean> recheckMap = new ConcurrentHashMap<>();
    private TxClient txClient;


    public TxClient getTxClient() {
        return txClient;
    }

    public void setTxClient(TxClient txClient) {
        this.txClient = txClient;
    }

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

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getInvokeMethodNameList() {
        return invokeMethodNameList;
    }

    public void setInvokeMethodNameList(List<String> invokeMethodNameList) {
        this.invokeMethodNameList = invokeMethodNameList;
    }

    public Map<String, RecheckBean> getRecheckMap() {
        return recheckMap;
    }

    public void setRecheckMap(Map<String, RecheckBean> recheckMap) {
        this.recheckMap = recheckMap;
    }
}
