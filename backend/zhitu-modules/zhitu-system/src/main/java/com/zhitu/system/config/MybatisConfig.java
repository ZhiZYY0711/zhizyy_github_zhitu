package com.zhitu.system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zhitu.system.mapper")
public class MybatisConfig {
}
