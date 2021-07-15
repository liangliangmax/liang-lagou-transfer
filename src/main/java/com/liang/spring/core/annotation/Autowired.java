package com.liang.spring.core.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.FIELD})
public @interface Autowired {

    boolean required() default true;

}
