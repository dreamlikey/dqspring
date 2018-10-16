package com.wdq.springframework.webmvc.anotation;

import java.lang.annotation.*;

/**
 * @Author: wudq
 * @Date: 2018/10/14
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DQRequestMapping {
    String value() default "";
}
