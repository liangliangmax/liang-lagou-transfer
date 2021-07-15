package com.liang.spring.core.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.TYPE})
@Component
public @interface Service {

    String value() default "";

}
