package com.liang.spring.core;

import com.liang.spring.core.context.ApplicationContext;

@FunctionalInterface
public interface ApplicationContextAware {

    void setApplicationContext(ApplicationContext applicationContext);

}
