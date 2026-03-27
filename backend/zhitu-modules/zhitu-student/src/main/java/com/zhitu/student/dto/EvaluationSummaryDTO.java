package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价汇总DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价汇总信息")
public class EvaluationSummaryDTO {
    
    @Schema(description = "平均分数", example = "87.5")
    private BigDecimal averageScore;
    
    @Schema(description = "评价列表")
    private List<EvaluationItemDTO> evaluations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "评价条目")
    public static class EvaluationItemDTO {
        
        @Schema(description = "评价人姓名", example = "张老师")
        private String evaluatorName;
        
        @Schema(description = "来源类型", example = "enterprise", 
                allowableValues = {"enterprise", "college", "mentor"})
        private String sourceType;
        
        @Schema(description = "评价日期", example = "2024-01-15T10:30:00")
        private LocalDateTime evaluationDate;
        
        @Schema(description = "评分", example = "90")
        private Integer score;
        
        @Schema(description = "评价意见", example = "表现优秀")
        private String comment;
    }
}
