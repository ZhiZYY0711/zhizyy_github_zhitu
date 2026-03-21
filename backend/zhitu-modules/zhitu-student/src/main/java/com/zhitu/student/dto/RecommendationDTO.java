package com.zhitu.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 推荐信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {

    /**
     * 推荐ID
     */
    private Long id;

    /**
     * 推荐类型：project / job / course
     */
    private String recType;

    /**
     * 引用ID（项目ID、岗位ID或课程ID）
     */
    private Long refId;

    /**
     * 推荐分数
     */
    private BigDecimal score;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
