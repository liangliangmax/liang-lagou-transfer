package com.liang.spring.core.context;

public interface BeanFactory {

    Object getBean(String beanName);

    Object getBean(Class<?> beanType);

    Object getBean(String beanName,Class<?> beanType);

    boolean containsBean(String beanName);
}
