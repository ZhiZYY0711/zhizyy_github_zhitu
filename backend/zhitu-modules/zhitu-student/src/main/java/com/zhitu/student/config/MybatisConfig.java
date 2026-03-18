package com.zhitu.student.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zhitu.student.mapper")
public class MybatisConfig {
}
