package com.liang.spring.core.context;

import com.liang.spring.core.annotation.Component;
import com.liang.spring.core.annotation.Configuration;
import com.liang.spring.core.annotation.Repository;
import com.liang.spring.core.annotation.Service;
import com.liang.spring.core.util.CollectionsUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractApplicationContext implements ApplicationContext {

    //类加载器
    private ClassLoader classLoader = this.getClass().getClassLoader();


    //存放全局的配置文件
    private Properties properties = new Properties();


    //启动时候指定包下面所有扫描到的类
    private Set<Class<?>> classes = new HashSet<>();



    protected static Map<String,Object> singletonObject = new ConcurrentHashMap<>();  // 存储对象

    public AbstractApplicationContext(String scanPath){
        refresh(scanPath);
    }


    private void refresh(String scanPath){

        classes = scan(scanPath);

        loadProperties();


        initBean();


        polpulateBean();




    }




    /**
     * 每种扫描器扫描的方式不一样，扫描完了把bean都放到map中
     * @param scanPath
     */
    abstract Set<Class<?>> scan(String scanPath);

    abstract void loadProperties();

    protected abstract void initBean();

    protected abstract void polpulateBean();




    //////////////////////////////////////////////////////////////////////


    public void addProperties(Properties properties){

        this.properties.putAll(properties);
    }

    public String getProperties(String key){

        return properties.getProperty(key);
    }

    public ClassLoader getClassLoader(){
        return classLoader;
    }


    public Set<Class<?>> getClasses() {
        return classes;
    }



    //////////////////////////////////////////////////////////////////////

    static boolean isNeedInit(Class clazz){

        return clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Component.class)
                || clazz.isAnnotationPresent(Service.class) || clazz.isAnnotationPresent(Repository.class);
    }


    @Override
    public Object getBean(String beanName) {

        return singletonObject.get(beanName);
    }

    @Override
    public Object getBean(Class beanType) {

        Collection<Object> values = singletonObject.values();

        if(!CollectionsUtil.isEmpty(values)){

            for (Object value : values) {

                if(value.getClass() == beanType){
                    return value;
                }
            }
        }

        return null;
    }

    @Override
    public Object getBean(String beanName,Class<?> beanType) {
        Object o = singletonObject.get(beanName);

        if(o != null && o.getClass() == beanType){
            return o;
        }

        return null;
    }

    @Override
    public boolean containsBean(String beanName) {
        return singletonObject.containsKey(beanName);
    }
}
