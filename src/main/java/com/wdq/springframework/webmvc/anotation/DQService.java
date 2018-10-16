package com.wdq.springframework.webmvc.anotation;

import java.lang.annotation.*;

/**
 * @Author: wudq
 * @Date: 2018/10/14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DQService {
    String value() default "";
}
