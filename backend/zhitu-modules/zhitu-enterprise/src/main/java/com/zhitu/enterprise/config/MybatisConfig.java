package com.zhitu.enterprise.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zhitu.enterprise.mapper")
public class MybatisConfig {
}
