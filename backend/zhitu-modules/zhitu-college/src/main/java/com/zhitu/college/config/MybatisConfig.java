package com.zhitu.college.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zhitu.college.mapper")
public class MybatisConfig {
}
