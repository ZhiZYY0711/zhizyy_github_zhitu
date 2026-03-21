package com.zhitu.student.dto;

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
public class EvaluationSummaryDTO {
    private BigDecimal averageScore;
    private List<EvaluationItemDTO> evaluations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationItemDTO {
        private String evaluatorName;
        private String sourceType; // enterprise, college, mentor
        private LocalDateTime evaluationDate;
        private Integer score;
        private String comment;
    }
}
