package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交评价请求 DTO
 */
@Data
@Schema(description = "提交评价请求")
public class EvaluationRequest {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID", example = "1001", required = true)
    private Long studentId;

    @NotBlank(message = "来源类型不能为空")
    @Schema(description = "来源类型", example = "enterprise", 
            allowableValues = {"enterprise", "school", "peer"}, required = true)
    private String sourceType;

    @NotBlank(message = "关联类型不能为空")
    @Schema(description = "关联类型", example = "project", 
            allowableValues = {"project", "internship"}, required = true)
    private String refType;

    @NotNull(message = "关联ID不能为空")
    @Schema(description = "关联ID", example = "2001", required = true)
    private Long refId;

    @Schema(description = "JSON 格式评分", example = "{\"technical\":85,\"attitude\":90}")
    private String scores;

    @Schema(description = "评价意见", example = "表现优秀，积极主动")
    private String comment;

    @Schema(description = "录用推荐", example = "强烈推荐")
    private String hireRecommendation;
}
