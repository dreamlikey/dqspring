package com.wdq.demo.controller;

import com.wdq.demo.service.UserService;
import com.wdq.springframework.webmvc.anotation.DQAutowired;
import com.wdq.springframework.webmvc.anotation.DQController;
import com.wdq.springframework.webmvc.anotation.DQRequestMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: wudq
 * @Date: 2018/10/14
 */
@DQController
@DQRequestMapping("/user")
public class UserController {

    @DQAutowired
    private UserService userService;

    @DQRequestMapping("/get")
    public String get() {
        return "hello spring";
    }

    @DQRequestMapping("/save")
    public void save(@RequestParam(name = "name", required = true) String name){

    }

    public static void main(String[] args) {

    }
}