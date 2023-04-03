package com.spring;

/**
 * @author:hty
 * @date:2023-04-03 10:50
 * @email:1156388927@qq.com
 * @description: 可以给类中属性名为beanName的属性注入一个属性值，属性值就是所属类的beanName
 */


public interface MyBeanNameAware {
    void setBeanName(String name);
}
