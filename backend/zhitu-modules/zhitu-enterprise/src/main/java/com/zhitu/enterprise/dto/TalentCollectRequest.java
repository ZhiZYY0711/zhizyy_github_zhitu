package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "人才收藏请求")
public class TalentCollectRequest {
    @NotNull
    @Schema(description = "学生ID", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;
    
    @Schema(description = "备注", example = "技术能力强，适合Java开发岗位")
    private String remark;
}
