package com.hty;

import com.hty.service.UserService;
import com.spring.MyApplicationContext;

/**
 * @author:hty
 * @date:2023-04-02 19:43
 * @email:1156388927@qq.com
 * @description:
 */


public class Test {
    public static void main(String[] args) throws Exception {
        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));


    }
}
