package com.hty.service;

import com.spring.MyBeanPostProcessor;
import com.spring.MyComponent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author:hty
 * @date:2023-04-03 11:16
 * @email:1156388927@qq.com
 * @description:
 */


@MyComponent
public class HtyBeanPostProcessor implements MyBeanPostProcessor {

    //在bean初始化之前执行
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
//        System.out.println("初始化前");
//        if(beanName.equals("userService")){
//            ((UserServiceImpl)bean).setName("hty");
//        }
        return bean;
    }

    //在bean初始化之后执行
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
//        System.out.println("初始化后");

        //AOP
        if(beanName.equals("userService")){
            Object proxyInstance = Proxy.newProxyInstance(HtyBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");//找切点
                    return method.invoke(bean,args);
                }
            });

            return proxyInstance;
        }

        return bean;
    }
}
