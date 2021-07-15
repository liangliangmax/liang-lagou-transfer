package com.liang.spring.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Bean {


    String name() default "";


}
