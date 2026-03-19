package com.zhitu.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系统服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.zhitu.system", "com.zhitu.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhitu")
public class SystemApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
    
}