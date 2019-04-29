package com.dts.framework.manual;

import com.alibaba.fastjson.JSON;
import com.dts.framework.dlxmq.ManualMessageBean;
import com.dts.framework.support.ProxyMethodTXCache;
import com.dts.framework.support.TxContext;
import com.dts.framework.support.TxContextSupport;
import com.dts.framework.support.TxMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 事务手动提交 support
 *
 * @author jsun
 * @create 2019-04-26 15:17
 **/
public class ManualCommitSupport {

    public static void commit(ManualMessageBean manualMessageBean, AmqpTemplate txTemplate) {
        List<String> list = manualMessageBean.getMqInfoList();
        if (CollectionUtils.isEmpty(list)) return;
        TxMessage message = manualMessageBean.getTxMessage();
        for (String str : list) {
            String[] args = str.split("@");
            txTemplate.convertAndSend(args[0], args[1], JSON.toJSONString(message));
        }
    }

    /**
     * 手动提交状态下 获取事务相关信息
     *
     * @return
     */
    public static ManualMessageBean getTxCommitMessage() {
        TxContext txContext = TxContextSupport.getTxContextThreadLocal().get();
        if (txContext == null) return null;
        ManualMessageBean bean = new ManualMessageBean();
        for (String method : txContext.getInvokeMethodNameList()) {
            String mqinfo = ProxyMethodTXCache.getMqinfoByMethod(method);
            bean.getMqInfoList().add(mqinfo);
        }
        TxMessage message = new TxMessage();
        message.setTxFlow(txContext.getTxFlowId());
        message.setSuccess(true);
        bean.setTxMessage(message);
        return bean;
    }

    /**
     * 手动回滚
     */
    public static void txSessionRollBack() {
        TxContext txContext = TxContextSupport.getTxContextThreadLocal().get();
        if (txContext != null) txContext.setSuccess(false);
    }

}
