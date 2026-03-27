package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 岗位申请DTO
 */
@Data
@Schema(description = "岗位申请信息")
public class ApplicationDTO {
    @Schema(description = "申请ID", example = "1")
    private Long id;
    
    @Schema(description = "岗位ID", example = "100")
    private Long jobId;
    
    @Schema(description = "岗位标题", example = "Java开发实习生")
    private String jobTitle;
    
    @Schema(description = "学生ID", example = "2001")
    private Long studentId;
    
    @Schema(description = "学生姓名", example = "张三")
    private String studentName;
    
    @Schema(description = "申请状态：0=待处理 1=面试 2=Offer 3=拒绝 4=录用", example = "0", allowableValues = {"0", "1", "2", "3", "4"})
    private Integer status;
    
    @Schema(description = "申请时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime appliedAt;
}
