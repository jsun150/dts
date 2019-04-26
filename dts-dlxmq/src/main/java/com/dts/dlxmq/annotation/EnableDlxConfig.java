package com.dts.dlxmq.annotation;



import com.dts.dlxmq.dlx.DlxRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * config配置
 *
 * @author jsun
 * @create 2019-03-25 16:36
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DlxRegistrar.class)
public @interface EnableDlxConfig {

    /**
     * rabbit exchange
     * @return
     */
    String dlxExchange() default "";

    /**
     * 命名空间
     * @return
     */
    String nameSpace() default "";

    /**
     * 延迟任务消费beanID
     * EXTEND DlxMessageConsumer
     * @return
     */
    String dlxConsumer() default "";

    /**
     * rabbit tempalteId
     * @return
     */
    String dlxTemplate() default "";

    String dlxFactory() default "";

}
