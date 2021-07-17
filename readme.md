# 自定义spring框架总结
### 目录

1. 先说总结
2. 结构说明
3. 启动运行



### 正文

1. 发自肺腑的总结

   

2. 结构

   1） 项目说明

   - 本项目是一个普通的web项目，带有web.xml，由内嵌的tomcat启动运行。本项目是模拟正常使用spring框架，主要模拟解决依赖注入和声明式事务功能。

   

   - 代码部分分为两部分，一部分模拟spring-core的功能，在com.liang.spring目录中；另一部分模拟正常的用户使用spring框架，在com.lagou.edu包中。com.lagou.edu包中就是正常用户使用spring框架的一些普通用法，没什么特别，此处略过。接下来重点说一下com.liang.spring包中如何模拟spring框架的。

   

   2） 结构说明

   com.liang.spring

   ​	-- core

   ​		&ensp;&ensp;-- annotation：包中定义了spring常用的一些注解，比如@Service，@Transactional等

   ​		&ensp;&ensp;-- context：包中定义了核心模块

   ​		&ensp;&ensp;-- entity：定义了封装类信息的实体

   ​		&ensp;&ensp;-- scanner：工具类，用于扫描指定路径下的class文件

   ​		&ensp;&ensp;-- transaction：定义了事务相关的类

   ​		&ensp;&ensp;-- util：一些工具类

   ​		&ensp;&ensp;-- ApplicationContextAware：容器注入的接口

   

   ​	-- servlet	定义了tomcat启动时候的监听器，用于tomcat启动时候启动自定义的spring框架开始扫描

   ​	

   3）启动过程描述

   i:	由于项目是web项目，而且没有引入spring-web的相关内容，所以需要使用tomcat的相关功能启动容器。

   - 首相定义了com.liang.spring.servlet.SpringServletContextListener，实现了tomcat的ServletContextListener接口，这样tomcat启动时候就会执行contextInitialized里面的方法。

   - 在web.xml中配置该监听器，并且配置了包扫描的的路径，即扫描用户自己项目的包目录。

   - 当tomcat启动时候，就会调用监听器，监听器里面获取了扫描包参数后，将扫描包的目录传给AnnotationApplicationContext容器，随即开始容器的初始化

     ```java
     	@Override
         public void contextInitialized(ServletContextEvent servletContextEvent) {
             System.out.println("启动中，请稍后...");
             String scanPath = "";
             ServletContext servletContext = servletContextEvent.getServletContext();
             Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
             while (initParameterNames.hasMoreElements()){
                 String parameter = initParameterNames.nextElement();
                 if (parameter.equalsIgnoreCase("scanPath")){
                     scanPath = servletContext.getInitParameter(parameter);
                     break;
                 }
             }
     
             new AnnotationApplicationContext(scanPath);
         }
     ```

     

   ii:	系统中定义了BeanFactory作为容器的顶级接口，里面定义了getBean的方法。

   - com.liang.spring.core.context.AbstractApplicationContext作为BeanFactory的抽象子类，里面声明了一些属性：
     - classLoader是作为整体的类加载器，加载扫描包的class时候会用到，如果用Thread获取当前的classLoader会出现加载不到类的异常，所以这里需要定义一个属性。
     - 
   - 

   iii:	

   iv:	

   v:	

   vi:	

   

   

3. 启动运行