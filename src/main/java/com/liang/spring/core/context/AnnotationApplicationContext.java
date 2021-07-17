package com.liang.spring.core.context;

import com.liang.spring.core.ApplicationContextAware;
import com.liang.spring.core.annotation.*;
import com.liang.spring.core.entity.BeanDefinition;
import com.liang.spring.core.scaner.ClassScanner;
import com.liang.spring.core.transaction.TransactionManager;
import com.liang.spring.core.transaction.TransactionalProxyFactory;
import com.liang.spring.core.util.GenerateBeanNameUtil;
import com.liang.spring.core.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;


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

    /**
     * 初始化生成beanDefinitino
     */
    @Override
    protected void createBeanDefinition() {

        Set<Class<?>> classes = getClasses();

        for (Class<?> clazz : classes) {

            if(isNeedInit(clazz)){

                BeanDefinition beanDefinition = new BeanDefinition();

                String id = GenerateBeanNameUtil.generateBeanName(clazz);
                beanDefinition.setId(id);
                beanDefinition.setClazz(clazz);

                Field[] declaredFields = clazz.getDeclaredFields();

                for (Field declaredField : declaredFields) {
                    if(declaredField.isAnnotationPresent(Autowired.class)) {
                        Class<?> type = declaredField.getType();
                        beanDefinition.getDependsOn().add(type.getName());
                    }
                }

                Method[] declaredMethods = clazz.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if(declaredMethod.isAnnotationPresent(Transactional.class)){
                        beanDefinition.setCreateProxy(true);
                    }
                }

                beanDefinitionMap.put(id,beanDefinition);
            }
        }
    }


    /**
     * 先吧所有的带指定注解的bean创建出来，然后是给bean里面的属性赋值
     *
     * 将空实例放到一个未完成的map中，等待之后填充属性
     */
    @Override
    protected void initBean() {
        beanDefinitionMap.forEach((key,beanDefinition)->{

            Class beanClass = beanDefinition.getClazz();

            try {
                Object bean = beanClass.newInstance();

                //这种是没有依赖并且不需要创建代理对象，可以直接创建bean
                if(!beanDefinition.isCreateProxy() && beanDefinition.getDependsOn().isEmpty()) {

                    singletonObject.put(key, bean);
                } else if(!beanDefinition.getDependsOn().isEmpty() && !beanDefinition.isCreateProxy()){
                    //有依赖的bean
                    notFinishedObject.put(key, bean);
                }else {
                    //需要创建代理对象的bean
                    createProxyObject.put(key,bean);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        });


    }

    /**
     * 填充bean里面的属性
     *
     * 先处理带Configuration的，如果里面属性标记了@Value，这种是最优先处理的
     *
     * 如果Configuration中有@Autowired的属性，则这个类里面的bean先不初始化
     * 如果Configuration中没有@Autowired的属性，则这个类中的bean直接实例化然后填充别的属性
     *
     * 填充完毕的bean就可以移到一级缓存里面了
     *
     * 然后是遍历二级缓存的map，从里面找到@Autowired的属性，开始从一级缓存拿别的bean开始组装，组装完了就放到一级缓存里面，删除二级缓存的bean
     *
     * 等到别的都填充完了，再次找到刚才有Configuration并且有@Autowired的属性，进行填充
     *
     * 这样相互依赖的问题就解决了
     *
     *
     * 相互依赖的问题主要存在于
     *
     * Configuration中有的是@Value，有的是@Autowired，其中还有@Bean
     *
     * 此时想要生成bean，直接调用方法即可，@Value由于是先执行，所以这样的bean是可以生成的
     * 但是如果是@Autowired，此时这个属性可能还没有被赋值或者初始化，导致执行生成bean的方法生成出来的内容是空的，所以才会有二级缓存的需求
     *
     * 例如：
     * com.lagou.edu.config.DataSourceConfig 这个类里面可以生成datasource，
     * com.lagou.edu.config.TransactionConfig这个类里面需要注入datasource
     *
     * 当com.lagou.edu.config.TransactionConfig这个类初始化开始执行Autowired的时候，可能datasource还没生成呢，导致其@Bean生成失败
     *
     */
    @Override
    protected void populateBean() {

        //先填充直接生成的bean
        singletonObject.forEach((beanName,bean)->{
            Field[] declaredFields = bean.getClass().getDeclaredFields();

            for (Field declaredField : declaredFields) {

                if(declaredField.isAnnotationPresent(Value.class)){
                    Value valueAnno = declaredField.getAnnotation(Value.class);

                    String regex = valueAnno.value();

                    if(StringUtils.isBlank(regex)){

                        throw new RuntimeException(declaredField.getName()+"上占位符信息不能为空");
                    }

                    //如果没用表达式，直接写的字符串，则直接将字符串赋值给属性
                    if(bean !=null){
                        if(!regex.contains("${")){
                            ReflectionUtils.setFieldValue(bean,declaredField.getName(),regex);
                        }else {
                            String propKey = regex.replace("${", "").replace("}", "");

                            String propValue = getProperties(propKey);

                            if(StringUtils.isBlank(propValue)){
                                throw new RuntimeException("无法找到"+propKey+"对应的配置文件");
                            }
                            ReflectionUtils.setFieldValue(bean,declaredField.getName(),propValue);
                        }
                    }

                }
            }

            generateBeanAnnotation(bean);
        });

        //2.填充有依赖的bean
        notFinishedObject.forEach((beanName,bean)->{

            injectionAutowired(beanName, bean);

            //当autowired完成之后，就可以去实例化之前没有实例化的bean标签
            generateBeanAnnotation(bean);
        });

        //3.填充需要生成代理对象的bean
        createProxyObject.forEach((beanName,bean)->{

            Class<?> beanClass = bean.getClass();

            //如果有接口，则用jdk的动态代理
            if(beanClass.getInterfaces().length!=0){
                TransactionalProxyFactory transactionalProxyFactory = new TransactionalProxyFactory();

                TransactionManager transactionManager = (TransactionManager) getBean(TransactionManager.class);
                if(transactionManager == null){
                    throw  new RuntimeException("需要定义transactionManager");
                }

                transactionalProxyFactory.setTransactionManager(transactionManager);

                injectionAutowired(beanName,bean);

                Object jdkProxy = transactionalProxyFactory.getJdkProxy(bean);

                singletonObject.put(beanName,jdkProxy);
                createProxyObject.remove(beanName);

            }else {
                //如果没有接口，则用cglib的动态代理
                TransactionalProxyFactory transactionalProxyFactory = new TransactionalProxyFactory();

                TransactionManager transactionManager = (TransactionManager) getBean(TransactionManager.class);
                if(transactionManager == null){
                    throw  new RuntimeException("需要定义transactionManager");
                }

                transactionalProxyFactory.setTransactionManager(transactionManager);

                injectionAutowired(beanName,bean);

                Object jdkProxy = transactionalProxyFactory.getCglibProxy(bean);

                singletonObject.put(beanName,jdkProxy);
                createProxyObject.remove(beanName);
            }
        });
    }

    /**
     * 填充autowired的属性
     * @param beanName
     * @param bean
     */
    private void injectionAutowired(String beanName, Object bean) {
        Field[] declaredFields = bean.getClass().getDeclaredFields();

        for (Field declaredField : declaredFields) {

            if(declaredField.isAnnotationPresent(Autowired.class)){

                //如果是执行了注入名称，直接按名称赋值
                if(declaredField.isAnnotationPresent(Qualifier.class)){

                    Qualifier qualifierAnno = declaredField.getAnnotation(Qualifier.class);
                    String qualifierName = qualifierAnno.value();

                    Object o = getBean(qualifierName);
                    if(o !=null){
                        ReflectionUtils.setFieldValue(bean,declaredField.getName(),o);
                    }else {
                        throw new RuntimeException("未找到名称为"+beanName+"的bean");
                    }
                    singletonObject.put(beanName,o);
                    notFinishedObject.remove(beanName);
                }else {
                    //如果没有指定名字，就按照类型注入
                    Object o = getBean(declaredField.getType());
                    if(o !=null){
                        ReflectionUtils.setFieldValue(bean,declaredField.getName(),o);
                        singletonObject.put(beanName,bean);
                        notFinishedObject.remove(beanName);
                    }
                }

            }
        }
    }

    /**
     * 生成@Bean标记的方法
     * @param bean
     */
    private void generateBeanAnnotation(Object bean) {
        for (Method declaredMethod : bean.getClass().getDeclaredMethods()) {
            if(  declaredMethod.isAnnotationPresent(Bean.class)){
                try {
                    Object[] objects = new Object[]{};
                    Class<?>[] parameterTypes = declaredMethod.getParameterTypes();

                    for (int i = 0; i < parameterTypes.length; i++) {
                        Object paraObj = getBean(parameterTypes[i]);
                        objects[i] = paraObj;
                    }

                    Object beanObj = declaredMethod.invoke(bean, objects);
                    if(beanObj != null){
                        singletonObject.put(declaredMethod.getName(),beanObj);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 将实现了接口的类注入容器实例
     */
    @Override
    void afterProcess() {

        for (Object bean : singletonObject.values()) {

            if(bean instanceof ApplicationContextAware){

                Method[] declaredMethods = ApplicationContextAware.class.getDeclaredMethods();

                for (Method declaredMethod : declaredMethods) {

                    try {
                        declaredMethod.invoke(bean,this);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }


            }


        }

    }


}
