package com.spring;

/**
 * @author:hty
 * @date:2023-04-03 11:01
 * @email:1156388927@qq.com
 * @description:初始化接口
 */


public interface MyInitializingBean {

    void afterPropertiesSet() throws Exception;
}
