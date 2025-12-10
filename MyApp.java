package com.zstu.math;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MyApp {

    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
        System.out.println("应用启动成功！访问 http://localhost:8080 使用系统");
    }

}