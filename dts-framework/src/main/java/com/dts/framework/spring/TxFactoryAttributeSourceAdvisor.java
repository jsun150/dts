package com.dts.framework.spring;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author jsun
 * @create 2019-03-25 19:32
 **/
public class TxFactoryAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final TxPointcut pointcut = new TxPointcut();
    private BeanFactory beanFactory;

    @Override
    public Pointcut getPointcut() {
        pointcut.setBeanFactory(beanFactory);
        return pointcut;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        this.beanFactory = beanFactory;
    }
}
