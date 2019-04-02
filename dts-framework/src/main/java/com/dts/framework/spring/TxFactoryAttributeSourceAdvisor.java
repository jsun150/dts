package com.dts.framework.spring;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author Jook
 * @create 2019-03-25 19:32
 **/
public class TxFactoryAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final TxPointcut pointcut = new TxPointcut();

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}
