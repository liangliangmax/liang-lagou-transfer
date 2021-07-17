# 自定义spring框架总结
### 目录

1. 先说总结
2. 结构说明
3. 启动运行



### 正文

1. 发自肺腑的总结

   

2. 结构

   1） 项目说明

   - 本项目是一个普通的web项目，带有web.xml，由内嵌的tomcat启动运行。本项目是模拟**注解方式**使用spring框架，主要模拟解决依赖注入和声明式事务功能。

   

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

   - 首先定义了com.liang.spring.servlet.SpringServletContextListener，实现了tomcat的ServletContextListener接口，这样tomcat启动时候就会执行contextInitialized里面的方法。
   - 在web.xml中配置该监听器，并且配置了包扫描的的路径，即扫描用户自己项目的包目录。

   ```xml
   <web-app>
   	<display-name>Archetype Created Web Application</display-name>
   	<context-param>
   		<param-name>scanPath</param-name>
   		<param-value>com.lagou.edu</param-value>
   	</context-param>
   	<listener>
   		<listener-class>com.liang.spring.servlet.SpringServletContextListener</listener-class>
   	</listener>
   </web-app>
   ```

   

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

   - com.liang.spring.core.context.AbstractApplicationContext作为BeanFactory的抽象子类，里面有模板方法和声明了一些属性：

     - classLoader是作为整体的类加载器，加载扫描包的class时候会用到，如果用Thread获取当前的classLoader会出现加载不到类的异常，所以这里需要定义一个属性。

     - properties 是放置用户的配置属性

     - classes 放置所有包扫描中扫描到的class文件

     - beanDefinitionMap 放置了注解扫描的bean定义信息

     - singletonObject放置完整可用的bean；

     - notFinishedObject放置第一次填充bean过程中有别的依赖导致自身无法完成填充的bean；

     - createProxyObject放置的是需要生成代理对象的bean

       

   - 方法refresh(String scanPath) 是一个模板方法，里面定义了容器初始化的流程

     - scan(scanPath)  --> 扫描指定目录下所有的class文件信息

     - loadProperties()  --> 加载系统默认的配置文件以及PropertySource注解指定的配置文件

     - createBeanDefinition()  --> 根据扫描的class信息，为标记有指定注解的类生成相应的beanDefinition信息

     - initBean()  --> 通过beanDefinition信息，为bean生成对应的空对象

     - populateBean()  --> 根据规则填充bean对象，使其完成依赖注入

     - afterProcess()  --> 后续的一些处理，为实现接口的类注入实例

       

   iii:	com.liang.spring.core.context.AnnotationApplicationContext作为AbstractApplicationContext的子类，对上面的抽象方法做了实现

   - 

   iv:	

   v:	

   vi:	

   

   

3. 启动运行