package com.dts.framework.support;

import org.springframework.util.StringUtils;

/**
 * @author Jook
 * @create 2019-03-26 17:24
 **/
public class TxFlowUtil {

    /**
     * 获取tx流水号
     *
     * @param txFlowId 不为null时以传入的流水号作为当前事务流水号
     * @return
     */
    public static String getTxFlowId(String txFlowId) {
        TxContext context = TxContextSupport.getTxContextThreadLocal().get();
        txFlowId = !StringUtils.isEmpty(txFlowId) ? txFlowId : String.valueOf(KeyWorker.nextId());
        context.setTxFlowId(txFlowId);
        return txFlowId;
    }
}
