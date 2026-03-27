package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "添加员工请求")
public class AddStaffRequest {
    @NotNull
    @Schema(description = "用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    @Schema(description = "部门", example = "技术部")
    private String department;
    
    @Schema(description = "职位", example = "高级工程师")
    private String position;
    
    @Schema(description = "是否为导师", example = "true")
    private Boolean isMentor;
}
