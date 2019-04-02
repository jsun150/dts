package com.dts.framework.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Objects;

/**
 * @author Jook
 * @create 2019-03-25 19:48
 **/
public class TxRegistrationUtil {

    public static BeanDefinition registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                            Class<?> beanClass) {
        if (registry.containsBeanDefinition(beanName)) {
            return null;
        }

        String[] candidates = registry.getBeanDefinitionNames();

        for (String candidate : candidates) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
            if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
                return null;
            }
        }

        AbstractBeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition();
        annotationProcessor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(beanName, annotationProcessor);

        return annotationProcessor;
    }
}
