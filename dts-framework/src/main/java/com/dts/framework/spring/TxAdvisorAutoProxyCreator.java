package com.dts.framework.spring;

import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;

/**
 * 如果项目没有AdvisorAutoProxyCreator 就用这个做默认  AutoProxyCreator
 * @author Jook
 * @create 2019-04-23 14:14
 **/
public class TxAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {
}
