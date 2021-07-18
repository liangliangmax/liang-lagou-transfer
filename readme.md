# 自定义spring框架总结
### 目录

1. 先说总结
2. 结构说明
3. 启动运行



### 正文

1. 发自肺腑的总结

   - 之前看源码的时候，大家都在讲三级缓存，大概也知道是干什么用的，于是这次就实验一下，不用三级缓存能不能解决依赖的问题。

     - 最开始时候定义一个map

       - 如果代码中结构比较简单（@Configuration类中没有@Autowired属性，只有@Value的属性和@Bean的方法；@Component中只有@Autowired的属性，没有代理对象）并且控制一下实例化顺序：加载class--> 加载配置文件 --> 生成空的bean对象 --> 填充配置类 --> 生成@Bean对象 --> 遍历@Autowired 注入配置好的bean，**这是可以通过一个map搞定的**。

         如：这是个配置类，其中的属性都是@Value，没有@Autowired这种，则这样的bean可以直接被生成出来，通过调用@Bean的方法，将对应的基础bean也生成出来放到map中。

         ```java
         @Configuration
         @PropertySource("classpath:jdbc.properties")
         public class DataSourceConfig {
         
             @Value("${jdbc.driver}")
             private String driverClassName;
             @Value("${jdbc.url}")
             private String url;
             @Value("${jdbc.username}")
             private String username;
             @Value("${jdbc.password}")
             private String password;
         
             @Bean
             public DataSource dataSource(){
                 DruidDataSource druidDataSource = new DruidDataSource();
                 druidDataSource.setDriverClassName(driverClassName);
                 druidDataSource.setUrl(url);
                 druidDataSource.setUsername(username);
                 druidDataSource.setPassword(password);
                 return druidDataSource;
             }
         }
         ```

         其他的都是@Autowired这种，如：

         ```java
         @Repository
         public class JdbcAccountDaoImpl implements AccountDao {
         
             @Autowired
             private DataSource dataSource;
             .......
         }
         ```

         这样他们填充bean的时候，基础bean已经是完整的bean，可以通过类型或者名称直接被注入，无所谓service先注入dao还是dao先注入dataSource，只要是@Autowired的这种，即使注入空对象也无所谓，空对象会被慢慢填充完成，到最后互相依赖的属性会被填满。

         

         但是如果出现了这种情况：

         ```java
         @Configuration
         @PropertySource("classpath:jdbc.properties")
         public class DataSourceConfig {
         
             @Value("${jdbc.driver}")
             private String driverClassName;
             @Value("${jdbc.url}")
             private String url;
             @Value("${jdbc.username}")
             private String username;
             @Value("${jdbc.password}")
             private String password;
         
             @Bean
             public DataSource dataSource(){
                 DruidDataSource druidDataSource = new DruidDataSource();
                 druidDataSource.setDriverClassName(driverClassName);
                 druidDataSource.setUrl(url);
                 druidDataSource.setUsername(username);
                 druidDataSource.setPassword(password);
                 return druidDataSource;
             }
         }
         ```

         ```java
         @Configuration
         public class TransactionConfig {
         
             @Autowired
             private DataSource dataSource;
         
             @Bean
             public ConnectionUtils connectionUtils(){
                 ConnectionUtils connectionUtils = new ConnectionUtils();
                 connectionUtils.setDataSource(dataSource);
         
                 return connectionUtils;
             }
         
             @Bean
             public TransactionManager transactionManager(){
                 DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
                 transactionManager.setConnectionUtils(connectionUtils());
                 return transactionManager;
             }
         
         }
         ```

         这两个类同时出现，问题就不好解决了。因为这两个bean都是configuration，**是同等级别**的，不能确保谁一定先创建完毕，如果第一个dataSource先创建的话还好办，如果第二个bean先被创建并且填充的时候，由于第一个@Bean还未执行，系统中还没有dataSource的bean，所以第二个bean填充时候就会找不到bean，但是这个bean已经执行了填充，之后就不会再操作这个bean了。**这时候就需要第二个map，去放置没有填充完毕的bean**。

         

     - 定义两个map

       当定义了两个map的时候，还是刚才那种情况，在class解析的时候如果有@Configuration并且有@Autowired，这样的bean直接扔到二级缓存中，因为他自身没办法填充完毕，得等待同样有@Configuration并且只有@Value的这种bean先创建出来，才能通过注入更基础的bean来完成自身的填充。当然两个map也是有执行顺序的，加载class--> 加载配置文件 --> 生成空的bean对象 --> 填充配置类 --> 如果配置类中Autowired，放二级缓存 --> 生成一级缓存中的@Bean对象  --> 遍历二级缓存，生成其Bean对象 --> 遍历@Autowired 注入配置好的bean，这样的话两个map就搞定了大部分情况。

       

       如果系统发现需要创建代理对象，这时候两个map也搞不定了。因为没有填充完的bean和代理对象放在一个map，无法保证哪个先被填充，如果代理对象先填充，但是原对象还没被填充，这种情况是有问题的。所以需要单独把代理对象放在一个地方。

       

     - 定义三个map

       这种情况上面的问题就都没有了，只需要三级缓存按顺序执行即可。不用担心互相依赖时候找不到他的依赖。

       

   - 为什么要定义BeanDefinition

     - 刚开始并没有定义beanDefinition类，而是直接通过扫描class上的注解，然后挨个去遍历的，这样的话虽然也是可以正常写的，但是后期就会出现比较麻烦的情况，就是有没有依赖呀，需不需要创建代理对象啊，虽然通过遍历循环的方式也可以慢慢搞定，但是每次都遍历实在是太麻烦了，所以在扫描完class文件之后直接扫描注解，就可以知道是否有依赖，是否需要创建代理对象。将这些信息保存到beanDefinition中，以后再使用就方便了。

   - 

     

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

   - scan(scanPath) 中，调用了工具类ClassScanner，对指定的目录进行扫描，并且用指定的classLoader去加载class类：

     ```java
     public Set<Class<?>> scan(String scanPath) {
         //获取所有的class类，
         try {
             Set<Class<?>> classes = ClassScanner.getScanner().scan(scanPath,getClassLoader());
             return classes;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return new HashSet<>();
     }
     ```

   - loadProperties() 中，首先扫描resources包中的application.properties文件，将其解析成properties，放在全局的配置项中。

     然后再查找有注解Configuration的，并且有PropertySource注解的，将其属性拿出来，就是要加载的文件，也是获取文件内容，将其解析成properties放入到全局的配置项中。这样配置文件加载完成。

     

   - createBeanDefinition() 开始生成beanDefinition。

     根据扫描的类上的注解，如果有Configuration，Component，Service，Repository注解的，就认为是需要生成beanDefinition的，对其进行处理。

     

     所有的beanDefinition设置id和class属性；如果其中有@Autowired标注的属性，则对其dependsOn属性设置依赖名称；如果方法上有@Transactional修饰，则认为是需要创建代理对象的，将其createProxy设置为true。生成完成的beanDefinition保存到beanDefinitionMap中，供下一步bean生成做准备。这样beanDefinition解析完毕。

     

     这一步只是生成了beanDefinition信息，还未生成bean实例。

     

   - initBean() 通过beanDefinition信息，进行bean的初始化工作。

     先调用class的无参构造方法，创建bean的实例对象，

     

     如果检测到没有dependsOn并且不需要生成代理对象，则将生成的bean放到singletonObject中，后期填充时候直接填充完成就是一个可用的bean。

     

     如果检测到有dependsOn，但是不需要生成代理对象，则将bean放到notFinishedObject中，因为需要确保他依赖的bean先生成，他自身才能生成。

     

     如果检测到需要生成代理对象，则直接放入createProxyObject中

     

   - 现在到了populateBean()方法，也是解决依赖关系的方法。

     - 先循环singletonObject，因为其中的bean是通过配置文件就能填充完成的bean。扫描bean中的属性，如果被@Value所修饰，则通过表达式中的字符串作为key，去解析好的配置文件信息去找，找到之后通过反射将值赋值给该属性。

     - 然后循环notFinishedObject，这部分是需要注入别的bean的。先判断要注入的是什么名称或类型，然后去singletonObject中按名称或这按类型去找，找到了就给属性赋值。赋值完成后，将对应的bean从notFinishedObject中移动到singletonObject。

     - 最后处理需要生成代理对象的bean。现在因为不能使用spring的aop，而且单独使用aspectj异常的麻烦，所以这里简单的认为方法上标了Transactional，就是需要声明式事务的，需要创建代理对象的。

       完成对依赖的注入之后，将生成的bean从createProxyObject中移除掉，添加到singletonObject中。

       通过调用com.liang.spring.core.transaction.TransactionalProxyFactory来获取有事务控制的代理对象，等到运行方法时候，就会通过jdbc去控制事务开启。

     

   - 填充bean完成后，所有的bean都是可用的，剩下的就是调用afterProcess()方法，为实现了com.liang.spring.core.ApplicationContextAware接口的的类注入容器本身，这样在servlet中获取bean的时候就可以获取容器实例，进而获取bean

   

3. 启动运行

   启动tomcat，然后请求地址接口，即可看到事务控制的效果