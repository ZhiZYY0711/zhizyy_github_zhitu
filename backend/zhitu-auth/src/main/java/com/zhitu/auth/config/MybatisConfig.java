package com.zhitu.auth.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zhitu.auth.mapper")
public class MybatisConfig {
}
