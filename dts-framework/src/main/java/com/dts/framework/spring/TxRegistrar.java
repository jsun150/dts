package com.dts.framework.spring;

import com.dts.framework.annotation.EnableTxConfig;
import com.dts.framework.support.TxConst;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

/**
 * @author Jook
 * @create 2019-03-25 16:29
 **/
public class TxRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        if (!registry.containsBeanDefinition(TxFactoryAttributeSourceAdvisor.class.getName())) {
            MultiValueMap<String, Object> paramMap = importingClassMetadata.getAllAnnotationAttributes(EnableTxConfig.class.getName());
            TxConst.CONNECTION_FACTORY_ID = paramMap.get("factory").get(0).toString();
            TxConst.TX_RABBIT_TEMPLATE_ID = paramMap.get("rabbitTemplate").get(0).toString();

            BeanDefinition interceptor = TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                    TxInterceptor.class.getName(), TxInterceptor.class);

            BeanDefinition advisor = TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                    TxFactoryAttributeSourceAdvisor.class.getName(), TxFactoryAttributeSourceAdvisor.class);
            // 注入interceptor
            advisor.getPropertyValues().add("adviceBeanName", interceptor.getBeanClassName());

            //如果已经有其他高等级的 TxAdvisorAutoProxyCreator 不创建或者会被覆盖
            if (!registry.containsBeanDefinition(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME)) {
                TxRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                        AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME, TxAdvisorAutoProxyCreator.class);
            }
        }
    }
}
