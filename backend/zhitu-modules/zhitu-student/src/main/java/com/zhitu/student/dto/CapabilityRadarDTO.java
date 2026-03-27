package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "能力雷达图数据")
public class CapabilityRadarDTO {

    @Schema(description = "能力维度列表")
    private List<DimensionScore> dimensions;

    /**
     * 能力维度分数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "能力维度分数")
    public static class DimensionScore {
        
        @Schema(description = "维度名称", example = "technical_skill", 
                allowableValues = {"technical_skill", "communication", "teamwork", "problem_solving", "innovation"})
        private String name;

        @Schema(description = "分数 (0-100)", example = "85", minimum = "0", maximum = "100")
        private Integer score;
    }
}
