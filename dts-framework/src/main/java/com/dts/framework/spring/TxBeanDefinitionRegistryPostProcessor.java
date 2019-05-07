package com.dts.framework.spring;

import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 把spring的事务advisor order提升一级
 *
 * @author Jook
 * @create 2019-04-29 10:59
 **/
public class TxBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private AtomicBoolean orderBoolen = new AtomicBoolean(false);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!orderBoolen.getAndSet(true)) {
            ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) registry;
            String[] names = factory.getBeanNamesForType(AbstractBeanFactoryPointcutAdvisor.class, true, false);
            if (names == null) return;
            for (String name : names) {
                AbstractBeanFactoryPointcutAdvisor advisor =
                        ((ConfigurableListableBeanFactory) registry).getBean(name, AbstractBeanFactoryPointcutAdvisor.class);
                if (registry.getBeanDefinition(advisor.getAdviceBeanName()).getBeanClassName().equals(TransactionInterceptor.class.getName())) {
                    advisor.setOrder(getNeedOrder(advisor));
                }
            }
        }
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        return;
    }

    /**
     * 如果order为null 或者是 Integer.MAX_VALUE ,则设置成 Integer.MAX_VALUE - 1.
     * 其他类型的order不改变
     *
     * @param advisor
     * @return
     */
    private int getNeedOrder(AbstractBeanFactoryPointcutAdvisor advisor) {
        int defaultOrder = Integer.MAX_VALUE - 1;
        try {
            Field field = AbstractPointcutAdvisor.class.getDeclaredField("order");
            field.setAccessible(true);
            Object object = field.get(advisor);
            if (object == null) return defaultOrder;
            int advisorOrder = Integer.valueOf(object.toString());
            return advisorOrder == Integer.MAX_VALUE ? defaultOrder : advisorOrder;
        } catch (Exception e) {
        }
        return defaultOrder;

    }
}
