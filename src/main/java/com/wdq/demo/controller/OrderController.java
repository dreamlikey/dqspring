package com.wdq.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: wudq
 * @Date: 2018/10/14
 */
@Controller
public class OrderController {

    @RequestMapping("/get")
    public void get() {

    }

    @RequestMapping("/save")
    public void save(@RequestParam(name = "name", required = true) String name){

    }
}