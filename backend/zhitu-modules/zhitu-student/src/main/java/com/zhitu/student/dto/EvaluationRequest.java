package com.zhitu.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交评价请求 DTO
 */
@Data
public class EvaluationRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotBlank(message = "来源类型不能为空")
    private String sourceType;   // enterprise / school / peer

    @NotBlank(message = "关联类型不能为空")
    private String refType;      // project / internship

    @NotNull(message = "关联ID不能为空")
    private Long refId;

    /** JSON 格式评分，如 {"technical":85,"attitude":90} */
    private String scores;

    private String comment;

    private String hireRecommendation;
}
