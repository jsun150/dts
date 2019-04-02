package com.dts.framework.annotation;


import com.dts.framework.spring.TxRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Jook
 * @create 2019-03-25 16:30
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(TxRegistrar.class)
public @interface EnableTxConfig {

    /**
     * rabbit factoryId
     *
     * @return
     */
    String factory() default "";

    /**
     * rabbit templateid
     *
     * @return
     */
    String rabbitTemplate() default "";

}
