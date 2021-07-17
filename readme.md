# 自定义spring框架总结
### 目录

1. 先说总结
2. 结构说明
3. 启动运行



### 正文

1. 总结

   

2. 结构

   1） 项目说明

   - 本项目是一个普通的web项目，带有web.xml，由内嵌的tomcat启动运行。本项目是模拟正常使用spring框架，主要模拟解决依赖注入和声明式事务功能。

   

   - 代码部分分为两部分，一部分模拟spring-core的功能，在com.liang.spring目录中；另一部分模拟正常的用户使用spring框架，在com.lagou.edu包中。com.lagou.edu包中就是正常用户使用spring框架的一些普通用法，没什么特别，此处略过。接下来重点说一下com.liang.spring包中如何模拟spring框架的。

   

   2） 结构说明

   com.liang.spring

   ​	-- core

   ​		-- annotation：包中定义了spring常用的一些注解，比如@Service，@Transactional等

   ​		-- context：包中定义了核心模块

   ​		-- entity：定义了封装类信息的实体

   ​		-- 

   

   ​	-- servlet	定义了tomcat启动时候的监听器，用于tomcat启动时候启动自定义的spring框架开始扫描

   ​	

   

   

   

3. 启动运行