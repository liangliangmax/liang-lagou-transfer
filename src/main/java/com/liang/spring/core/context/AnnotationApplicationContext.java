package com.liang.spring.core.context;

import com.liang.spring.core.annotation.*;
import com.liang.spring.core.scaner.ClassScanner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class AnnotationApplicationContext extends AbstractApplicationContext {

    public AnnotationApplicationContext(String scanPackage) {
        super(scanPackage);
    }


    @Override
    public Set<Class<?>> scan(String scanPath) {

        //获取所有的class类，判断是否带有注解
        try {
            Set<Class<?>> classes = ClassScanner.getScanner().scan(scanPath,getClassLoader());
            System.out.println("启动扫描包为："+scanPath);

            for (Class<?> aClass : classes) {
                System.out.println("被加载扫描的类是: "+aClass);
            }

            return classes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashSet<>();
    }


    @Override
    public void loadProperties() {

        //加载主配置文件
        InputStream mainStream = getClassLoader().getResourceAsStream("application.properties");
        Properties mainProps = new Properties();
        try {
            mainProps.load(mainStream);

            System.out.println("加载了的主配置文件内容是："+mainProps);

            addProperties(mainProps);

        } catch (IOException e) {
            e.printStackTrace();
        }


        //加载其他指定的配置文件
        for (Class<?> clazz : getClasses()) {
            if(clazz.isAnnotationPresent(PropertySource.class)){

                PropertySource propertySource = clazz.getAnnotation(PropertySource.class);

                String configPath = propertySource.value();

                if(StringUtils.isNotBlank(configPath)){

                    if(configPath.startsWith("classpath:")){

                        configPath = configPath.replace("classpath:","");
                    }

                    InputStream stream = getClassLoader().getResourceAsStream(configPath);
                    Properties props = new Properties();
                    try {
                        props.load(stream);

                        System.out.println("加载了的配置文件内容是："+props);

                        addProperties(props);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


            }


        }

    }





}
