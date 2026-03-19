package com.zhitu.college;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 高校服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.zhitu.college", "com.zhitu.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhitu")
public class CollegeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CollegeApplication.class, args);
    }
    
}