package com.dts.framework.spring;

import com.dts.framework.annotation.EnableTxConfig;
import com.dts.framework.dlxmq.ManualMessageBean;
import com.dts.framework.support.TxConst;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

/**
 * @author jsun
 * @create 2019-03-25 16:29
 **/
public class TxRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        if (!registry.containsBeanDefinition(TxFactoryAttributeSourceAdvisor.class.getName())) {
            MultiValueMap<String, Object> paramMap = importingClassMetadata.getAllAnnotationAttributes(EnableTxConfig.class.getName());
            TxConst.CONNECTION_FACTORY_ID = paramMap.get("factory").get(0).toString();
            TxConst.TX_RABBIT_TEMPLATE_ID = paramMap.get("rabbitTemplate").get(0).toString();
            boolean targetClass = Boolean.valueOf(paramMap.get("proxyTargetClass").toString());

            BeanDefinition interceptor = TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                    TxInterceptor.class.getName(), TxInterceptor.class);

            BeanDefinition advisor = TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                    TxFactoryAttributeSourceAdvisor.class.getName(), TxFactoryAttributeSourceAdvisor.class);
            // 注入interceptor
            advisor.getPropertyValues().add("adviceBeanName", interceptor.getBeanClassName());

            //手动提交
            TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ManualMessageBean.class.getName(),
                    ManualMessageBean.class);

            //直接替换 成最高等级的creator. 没有别的方式去保证排序.
            BeanDefinition reg = TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                    AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME, InfrastructureAdvisorAutoProxyCreator.class);
            if (reg != null)
                reg.getPropertyValues().add("proxyTargetClass", targetClass);

            TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry, TxBeanDefinitionRegistryPostProcessor.class.getName(),
                    TxBeanDefinitionRegistryPostProcessor.class);

        }
    }
}
