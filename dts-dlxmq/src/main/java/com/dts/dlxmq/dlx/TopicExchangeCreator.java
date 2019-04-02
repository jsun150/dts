package com.dts.dlxmq.dlx;

import org.springframework.amqp.rabbit.config.BindingFactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;

/**
 * 创建exchange
 *
 * @author Jook
 * @create 2019-03-28 17:04
 **/
public class TopicExchangeCreator {


    public static void register(BeanDefinitionRegistry registry) {
        createConsumerExchange(registry);
        createDlxExchange(registry);
    }

    /**
     * 注册私信队列exchange
     *
     * @param registry
     * @throws Exception
     */
    private static void createDlxExchange(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(BindingFactoryBean.class);
        String BINDING_QUEUE_ATTR = DlxConst.X_DEAD_Q_ID;
        //queue
        builder.addPropertyReference("destinationQueue", BINDING_QUEUE_ATTR);
        builder.addPropertyValue("exchange", new TypedStringValue(DlxConst.X_DEAD_LETTER_EXCHANGE));
        //pattern
        builder.addPropertyValue("routingKey", new TypedStringValue(DlxConst.X_DEAD_Q_NAME));
        String beanName = new DefaultBeanNameGenerator().generateBeanName(builder.getBeanDefinition(), registry);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    /**
     * 注册消费队列exchange
     *
     * @param registry
     * @throws Exception
     */
    private static void createConsumerExchange(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(BindingFactoryBean.class);
        String BINDING_QUEUE_ATTR = DlxConst.CONSUMER_Q_ID;
        builder.addPropertyReference("destinationQueue", BINDING_QUEUE_ATTR);
        builder.addPropertyValue("exchange", new TypedStringValue(DlxConst.X_DEAD_LETTER_EXCHANGE));
        builder.addPropertyValue("routingKey", new TypedStringValue(DlxConst.CONSUMER_Q_NAME));
        String beanName = new DefaultBeanNameGenerator().generateBeanName(builder.getBeanDefinition(), registry);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }


}
