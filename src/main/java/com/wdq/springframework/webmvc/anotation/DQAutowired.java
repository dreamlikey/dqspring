package com.wdq.springframework.webmvc.anotation;

import java.lang.annotation.*;

/**
 * @Author: wudq
 * @Date: 2018/10/14
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DQAutowired {
    boolean required() default true;
    String value() default "";
}
