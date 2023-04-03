package com.hty.service;

import com.spring.*;

/**
 * @author:hty
 * @date:2023-04-02 19:50
 * @email:1156388927@qq.com
 * @description:
 */

@MyComponent("userService")
public class UserServiceImpl implements UserService {

    @MyAutowired
    private OrderService orderService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void test(){
        System.out.println(orderService);
        System.out.println(name);
    }
}
