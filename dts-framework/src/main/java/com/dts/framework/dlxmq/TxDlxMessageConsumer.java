package com.dts.framework.dlxmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dts.dlxmq.dlx.DlxMessageProducer;
import com.dts.framework.support.RecheckBean;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * server端使用延迟取确认的consumer
 *
 * @author Jook
 * @create 2019-03-29 22:58
 **/
public abstract class TxDlxMessageConsumer implements ChannelAwareMessageListener, ApplicationContextAware {

    private DlxMessageProducer dlxMessageProducer;
    private ApplicationContext applicationContext;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        boolean next = true;
        RecheckBean recheckBean = null;
        try {
            recheckBean = JSONObject.parseObject(new String(message.getBody()), RecheckBean.class);
            recheckBean.setStime(recheckBean.getStime() + 1);
            next = !doDlxConfirm(recheckBean);
        } catch (Exception e) {
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            if (next && recheckBean != null) {
                getDlxProducer().sendMessageToDlx(JSON.toJSONString(recheckBean), recheckBean.getDelayTime());
            }
        }
    }

    /**
     * 处理dlx返回的消息
     *
     * @param bean
     * @return
     * @throws Exception
     */
    protected abstract boolean doDlxConfirm(RecheckBean bean) throws Exception;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private DlxMessageProducer getDlxProducer() {
        if (this.dlxMessageProducer == null) {
            dlxMessageProducer = applicationContext.getBean("dlxMessageProducer", DlxMessageProducer.class);
        }
        return dlxMessageProducer;
    }

}
