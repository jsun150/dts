package com.dts.framework.manual;

import com.dts.framework.dlxmq.ManualMessageBean;
import com.dts.framework.support.TxContext;
import com.dts.framework.support.TxContextSupport;

/**
 * 事务手动提交 support
 * @author jsun
 * @create 2019-04-26 15:17
 **/
public class ManualCommitSupport {




    /**
     * 手动提交状态下 获取事务相关信息
     * @return
     */
    public static ManualMessageBean getTxCommitMessage() {
        TxContext txContext = TxContextSupport.getTxContextThreadLocal().get();
        if (txContext == null) return null;
        return txContext.getManualMessageBean();
    }

    /**
     * 手动回滚
     */
    public static void txSessionRollBack(){
        TxContext txContext = TxContextSupport.getTxContextThreadLocal().get();
        if (txContext != null) txContext.setSuccess(false);
    }



}
