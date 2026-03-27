package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "推荐信息")
public class RecommendationDTO {

    @Schema(description = "推荐ID", example = "1")
    private Long id;

    @Schema(description = "推荐类型", example = "project", 
            allowableValues = {"project", "job", "course"})
    private String recType;

    @Schema(description = "引用ID（项目ID、岗位ID或课程ID）", example = "2001")
    private Long refId;

    @Schema(description = "推荐分数", example = "92.5")
    private BigDecimal score;

    @Schema(description = "推荐理由", example = "根据您的技能匹配度，推荐此项目")
    private String reason;

    @Schema(description = "创建时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime createdAt;
}
