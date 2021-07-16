package com.liang.spring.core.util;

import com.liang.spring.core.annotation.Component;
import com.liang.spring.core.annotation.Repository;
import com.liang.spring.core.annotation.Service;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

public class GenerateBeanNameUtil {


    /**
     * 生成bean的名称
     * @param clazz
     * @return
     */
    public static String generateBeanName(Class clazz){

        //为生成的实例取id
        //如果指定了id，则直接取id，如果没有指定，则用类名
        //先默认给个类名为id
        String id = StringUtils.uncapitalize(clazz.getSimpleName());

        Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();

        for (Annotation declaredAnnotation : declaredAnnotations) {

            if(declaredAnnotation instanceof Service){
                String value = ((Service) declaredAnnotation).value();
                if(StringUtils.isNotBlank(value)){
                    id = value;
                }
            }else if(declaredAnnotation instanceof Repository){
                String value = ((Repository) declaredAnnotation).value();
                if(StringUtils.isNotBlank(value)){
                    id = value;
                }
            }else if(declaredAnnotation instanceof Component){
                String value = ((Component) declaredAnnotation).value();
                if(StringUtils.isNotBlank(value)){
                    id = value;
                }
            }

        }

        return id;

    }

}
