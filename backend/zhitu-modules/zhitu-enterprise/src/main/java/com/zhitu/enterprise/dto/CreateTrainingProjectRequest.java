package com.zhitu.enterprise.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建实训项目请求
 */
@Data
public class CreateTrainingProjectRequest {

    private String name;
    private String description;
    private Integer difficulty;
    private List<String> techStack;
    private Integer maxTeams;
}
