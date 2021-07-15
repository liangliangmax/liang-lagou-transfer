package com.lagou.edu.utils;

import com.liang.spring.core.ApplicationContextAware;
import com.liang.spring.core.annotation.Configuration;
import com.liang.spring.core.context.ApplicationContext;

@Configuration
public class IocUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {

        IocUtil.applicationContext = applicationContext;
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return (T) getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name,Class<T> clazz){
        return (T) getApplicationContext().getBean(name, clazz);
    }


}
