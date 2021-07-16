package com.liang.spring.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Transactional {

    Class rollbackFor() default RuntimeException.class;

}
