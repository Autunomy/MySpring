package com.spring;

/**
 * @author:hty
 * @date:2023-04-02 22:48
 * @email:1156388927@qq.com
 * @description:
 */


public class MyBeanDefinition {
    private Class clazz;//Bean的类型
    private String scope;//Bean的作用域

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
