package com.dts.dlxmq.dlx;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

/**
 * @author jsun
 * @create 2019-03-29 22:58
 **/
public abstract class DlxMessageConsumer implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            processMessage(message, channel);
        } catch (Exception e) {
            errorMessage(message, e);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 处理dlx返回的消息
     *
     * @return
     * @throws Exception
     */
    protected abstract void processMessage(Message message, Channel channel) throws Exception;

    protected abstract void errorMessage(Message message, Exception e);


}
