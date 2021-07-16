package com.liang.spring.servlet;

import com.liang.spring.core.context.AnnotationApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

public class SpringServletContextListener implements ServletContextListener {


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

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
