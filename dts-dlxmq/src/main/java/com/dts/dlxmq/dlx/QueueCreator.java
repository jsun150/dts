package com.dts.dlxmq.dlx;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.*;

import java.util.Map;

/**
 * 队列创建  死信和接收队列
 *
 * @author jsun
 * @create 2019-03-28 17:04
 **/
public class QueueCreator {

    public static void register(BeanDefinitionRegistry registry) {
        registerConsumerQueue(registry);
        registerDlxQueue(registry);
    }

    /**
     * 注册死信队列beanDefinition
     *
     * @param registry
     * @throws Exception
     */
    private static void registerDlxQueue(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        builder.getRawBeanDefinition().setBeanClass(Queue.class);
//        ["name","durable","exclusive","autoDelete","arguments"]
        builder.addConstructorArgValue(new TypedStringValue(DlxConst.X_DEAD_Q_NAME));
        builder.addConstructorArgValue(new TypedStringValue("true"));
        builder.addConstructorArgValue(new TypedStringValue("false"));
        builder.addConstructorArgValue(new TypedStringValue("false"));
        Map<TypedStringValue, TypedStringValue> argMap = new ManagedMap<>();
        argMap.put(new TypedStringValue("x-dead-letter-exchange"), new TypedStringValue(DlxConst.X_DEAD_LETTER_EXCHANGE));
        argMap.put(new TypedStringValue("x-dead-letter-routing-key"), new TypedStringValue(DlxConst.CONSUMER_Q_NAME));
        builder.addConstructorArgValue(argMap);
        AbstractBeanDefinition definition = builder.getBeanDefinition();

        String[] aliases = {DlxConst.X_DEAD_Q_NAME};
        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, DlxConst.X_DEAD_Q_ID, aliases);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    /**
     * 注册消费死信队列beanDefinition
     *
     * @param registry
     * @throws Exception
     */
    private static void registerConsumerQueue(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        builder.getRawBeanDefinition().setBeanClass(Queue.class);
        builder.addConstructorArgValue(new TypedStringValue(DlxConst.CONSUMER_Q_NAME));
        builder.addConstructorArgValue(new TypedStringValue("true"));
        builder.addConstructorArgValue(new TypedStringValue("false"));
        builder.addConstructorArgValue(new TypedStringValue("false"));
        AbstractBeanDefinition definition = builder.getBeanDefinition();

        String[] aliases = {DlxConst.CONSUMER_Q_NAME};
        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, DlxConst.CONSUMER_Q_ID, aliases);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }


}
