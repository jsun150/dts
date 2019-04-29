package com.dts.framework.dlxmq;

import com.dts.framework.support.TxMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jsun
 * @create 2019-04-26 14:28
 **/
public class ManualMessageBean {

    private List<String> mqInfoList = new ArrayList<>();
    private TxMessage txMessage;

    public List<String> getMqInfoList() {
        return mqInfoList;
    }

    public void setMqInfoList(List<String> mqInfoList) {
        this.mqInfoList = mqInfoList;
    }

    public TxMessage getTxMessage() {
        return txMessage;
    }

    public void setTxMessage(TxMessage txMessage) {
        this.txMessage = txMessage;
    }
}
