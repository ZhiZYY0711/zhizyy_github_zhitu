package com.zhitu.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 能力雷达图响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityRadarDTO {

    /**
     * 能力维度列表
     */
    private List<DimensionScore> dimensions;

    /**
     * 能力维度分数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionScore {
        /**
         * 维度名称: technical_skill, communication, teamwork, problem_solving, innovation
         */
        private String name;

        /**
         * 分数 (0-100)
         */
        private Integer score;
    }
}
