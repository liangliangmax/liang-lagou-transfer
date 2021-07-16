package com.lagou.edu.config;

import com.liang.spring.core.annotation.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;


@Component
@Aspect
public class AspectConfig {


    //在该无参无内容的方法上添加一个@Poincut()来声明切入点，在后来的@Around("pc()")直接写入该方法的名字就能在自动使用这些切点
    @Pointcut("execution(* com.lagou.edu.service..*.*(..)) ")
    public void pc() {}


    //环绕通知注解
    @Before("pc()")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {

        System.out.println("aspect");

        return null;
    }

    public static void main(String[] args) {

        String expression = "* com.lagou.edu.service..*.*(..))";



    }

}
