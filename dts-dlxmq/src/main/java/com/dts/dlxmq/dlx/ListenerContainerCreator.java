package com.dts.dlxmq.dlx;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.*;

import java.util.List;

/**
 * 监听
 *
 * @author jsun
 * @create 2019-03-28 17:05
 **/
public class ListenerContainerCreator {

    public static void register(BeanDefinitionRegistry registry) {
        registerConsumerContainer(registry);
        createProduce(registry);
    }


    /**
     * 注册消费队列container
     *
     * @param registry
     * @throws Exception
     */
    private static void registerConsumerContainer(BeanDefinitionRegistry registry) {
        RootBeanDefinition listenerDef = new RootBeanDefinition();
        listenerDef.setSource(null);
        String ref = DlxConst.DLX_MESSAGE_CONSUMER_BEAN_ID;
        listenerDef.getPropertyValues().add("delegate", new RuntimeBeanReference(ref));
        listenerDef.getPropertyValues().add("defaultListenerMethod", null);

        RootBeanDefinition containerDef = new RootBeanDefinition(SimpleMessageListenerContainer.class);
        containerDef.getPropertyValues().add("connectionFactory",
                new RuntimeBeanReference(DlxConst.DLX_CONNECTION_FACTORY_ID));
//        containerDef.getPropertyValues().add("taskExecutor", new RuntimeBeanReference(taskExecutorBeanName));
        containerDef.getPropertyValues().add("acknowledgeMode", AcknowledgeMode.MANUAL);
        containerDef.getPropertyValues().add("autoStartup", new TypedStringValue("true"));
        containerDef.getPropertyValues().add("autoDeclare", new TypedStringValue("true"));

        listenerDef.setBeanClass(MessageListenerAdapter.class);
        containerDef.getPropertyValues().add("messageListener", listenerDef);
        String beanName = BeanDefinitionReaderUtils.generateBeanName(containerDef, registry);

        List<RuntimeBeanReference> values = new ManagedList<>();
        values.add(new RuntimeBeanReference(DlxConst.CONSUMER_Q_ID));
        containerDef.getPropertyValues().add("queues", values);

        BeanDefinitionHolder holder = new BeanDefinitionHolder(containerDef, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private static void createProduce(BeanDefinitionRegistry registry) {
        BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(DlxMessageProducer.class).getBeanDefinition();
        definition.getPropertyValues().add("dlxAmqpTemplate", new RuntimeBeanReference(DlxConst.DLX_RABBIT_TEMPLATE_ID));
        registry.registerBeanDefinition("dlxMessageProducer", definition);
    }


}
