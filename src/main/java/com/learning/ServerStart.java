package com.learning;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerStart {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("rpc-invoke-config.xml");
    }
}
