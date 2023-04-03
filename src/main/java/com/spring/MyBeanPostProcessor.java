package com.spring;

/**
 * @author:hty
 * @date:2023-04-03 11:11
 * @email:1156388927@qq.com
 * @description:
 */


public interface MyBeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);
}