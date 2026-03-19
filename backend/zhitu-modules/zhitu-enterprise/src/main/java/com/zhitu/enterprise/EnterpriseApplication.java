package com.zhitu.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 企业服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.zhitu.enterprise", "com.zhitu.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhitu")
public class EnterpriseApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApplication.class, args);
    }
    
}