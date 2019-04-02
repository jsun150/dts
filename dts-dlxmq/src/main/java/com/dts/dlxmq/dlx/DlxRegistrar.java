package com.dts.dlxmq.dlx;

import com.dts.dlxmq.annotation.EnableDlxConfig;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jook
 * @create 2019-03-30 0:01
 **/
public class DlxRegistrar implements ImportBeanDefinitionRegistrar {

    private AtomicBoolean init = new AtomicBoolean(false);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        if (!init.getAndSet(true)) {
            MultiValueMap<String, Object> paramMap = importingClassMetadata.getAllAnnotationAttributes(EnableDlxConfig.class.getName());
            DlxConst.X_DEAD_LETTER_EXCHANGE = paramMap.get("dlxExchange").get(0).toString();
            DlxConst.NAME_SPACE = paramMap.get("nameSpace").get(0).toString();
            DlxConst.DLX_MESSAGE_CONSUMER_BEAN_ID = paramMap.get("dlxConsumer").get(0).toString();
            DlxConst.DLX_RABBIT_TEMPLATE_ID = paramMap.get("dlxTemplate").get(0).toString();
            DlxConst.DLX_CONNECTION_FACTORY_ID = paramMap.get("dlxFactory").get(0).toString();

            ListenerContainerCreator.register(registry);
            QueueCreator.register(registry);
            TopicExchangeCreator.register(registry);
        }
    }


}
