package com.zhitu.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contract DTO for pending contracts list
 * Maps to frontend Contract interface
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "待审核合同信息")
public class ContractDTO {

    @Schema(description = "Offer ID", example = "1")
    private Long id;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "企业名称", example = "阿里巴巴")
    private String companyName;

    @Schema(description = "岗位名称", example = "Java开发实习生")
    private String position;

    @Schema(description = "提交时间", example = "2024-03-15T10:30:00Z")
    private String submitTime;

    @Schema(description = "审核状态", example = "pending")
    private String status;
}
