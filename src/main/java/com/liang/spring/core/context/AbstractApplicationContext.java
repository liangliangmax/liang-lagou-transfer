package com.liang.spring.core.context;

import com.liang.spring.core.annotation.Component;
import com.liang.spring.core.annotation.Configuration;
import com.liang.spring.core.annotation.Repository;
import com.liang.spring.core.annotation.Service;
import com.liang.spring.core.entity.BeanDefinition;
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


    //存放beanDefinition的map
    protected static Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();



    protected static Map<String,Object> singletonObject = new ConcurrentHashMap<>();  // 存储对象

    protected static Map<String,Object> notFinishedObject = new ConcurrentHashMap<>();  // 存放一些还没创建完的对象

    protected static Map<String,Object> createProxyObject = new ConcurrentHashMap<>();  // 存放一些需要创建代理对象的对象

    public AbstractApplicationContext(String scanPath){
        refresh(scanPath);
    }


    private void refresh(String scanPath){

        classes = scan(scanPath);

        loadProperties();

        createBeanDefinition();

        initBean();

        populateBean();

        afterProcess();

    }


    /**
     * 每种扫描器扫描的方式不一样，扫描完了把bean都放到map中
     * @param scanPath
     */
    abstract Set<Class<?>> scan(String scanPath);

    //加载beanDefinition
    protected abstract void createBeanDefinition();

    //加载配置文件
    abstract void loadProperties();

    //初始化bean
    abstract void initBean();

    //填充bean
    abstract void populateBean();


    //完成一些其他事情
    abstract void afterProcess();




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

        Object o = singletonObject.get(beanName);

        if(o != null) return o;

        Object o1 = notFinishedObject.get(beanName);

        if(o1 != null) return o1;
        return null;
    }

    @Override
    public Object getBean(Class beanType) {

        //先查一级缓存，然后查二级缓存
        Collection<Object> values = singletonObject.values();

        if(!CollectionsUtil.isEmpty(values)){

            for (Object value : values) {

                if(value.getClass() == beanType || beanType.isAssignableFrom(value.getClass())){
                    return value;
                }
            }
        }

        Collection<Object> notFinishedValues = notFinishedObject.values();

        if(!CollectionsUtil.isEmpty(notFinishedValues)){

            for (Object value : notFinishedValues) {

                if(value.getClass() == beanType || beanType.isAssignableFrom(value.getClass())){
                    return value;
                }
            }
        }

        return null;
    }

    @Override
    public Object getBean(String beanName,Class<?> beanType) {
        Object o = singletonObject.get(beanName);

        if(o != null && o.getClass() == beanType || beanType.isAssignableFrom(o.getClass())){
            return o;
        }

        return null;
    }

    @Override
    public boolean containsBean(String beanName) {
        return singletonObject.containsKey(beanName);
    }


}
