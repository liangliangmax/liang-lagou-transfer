package com.liang.spring.core.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.FIELD})
public @interface Qualifier {

    String value();
}
