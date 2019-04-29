package com.dts.dlxmq.dlx;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;


/**
 * 延迟发送
 *
 * @author jsun
 * @create 2019-03-29 23:20
 **/
public class DlxMessageProducer {

    private RabbitTemplate dlxAmqpTemplate;
    private volatile MessageConverter messageConverter = new SimpleMessageConverter();

    /**
     * 延迟发送
     *
     * @param message
     * @param second
     */
    public void sendMessageToDlx(String message, Long second) {
        Long millisecond = second * 1000;
        MessageProperties properties = new MessageProperties();
        properties.setExpiration(String.valueOf(millisecond));
        dlxAmqpTemplate.send(DlxConst.X_DEAD_LETTER_EXCHANGE, DlxConst.X_DEAD_Q_NAME, convertMessageIfNecessary(message, properties), null);
    }

    private Message convertMessageIfNecessary(Object o, MessageProperties properties) {
        return messageConverter.toMessage(o, properties);
    }

    protected RabbitTemplate getDlxAmqpTemplate() {
        return dlxAmqpTemplate;
    }

    protected void setDlxAmqpTemplate(RabbitTemplate dlxAmqpTemplate) {
        this.dlxAmqpTemplate = dlxAmqpTemplate;
    }
}
