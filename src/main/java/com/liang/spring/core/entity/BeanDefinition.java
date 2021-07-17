package com.liang.spring.core.entity;

import java.util.ArrayList;
import java.util.List;

public class BeanDefinition {

    private String id;

    private Class clazz;

    private boolean createProxy = false;

    private List<String> dependsOn = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public boolean isCreateProxy() {
        return createProxy;
    }

    public void setCreateProxy(boolean createProxy) {
        this.createProxy = createProxy;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }
}
