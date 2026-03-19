package com.zhitu.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 学生服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.zhitu.student", "com.zhitu.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhitu")
public class StudentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StudentApplication.class, args);
    }
    
}