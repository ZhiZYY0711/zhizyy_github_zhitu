package com.zhitu.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "预警干预请求")
@Data
public class InterveneRequest {
    @Schema(description = "干预措施说明", example = "已与学生沟通，了解缺勤原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String interveneNote;
    
    @Schema(description = "预期结果", example = "学生承诺改善出勤情况")
    private String expectedOutcome;
}
