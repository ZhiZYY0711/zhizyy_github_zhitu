package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 企业审核DTO
 * Enterprise Audit DTO
 * 
 * Requirements: 31.7
 * - Include enterprise name, business license, contact person, and submission date
 */
@Schema(description = "企业审核信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseAuditDTO {
    
    /** Enterprise info ID */
    @Schema(description = "企业信息ID", example = "1")
    private Long id;
    
    /** Enterprise tenant ID */
    @Schema(description = "企业租户ID", example = "100")
    private Long tenantId;
    
    /** Enterprise name */
    @Schema(description = "企业名称", example = "阿里巴巴集团")
    private String enterpriseName;
    
    /** Business license number */
    @Schema(description = "营业执照号", example = "91330000MA27XYZ123")
    private String businessLicense;
    
    /** Contact person name */
    @Schema(description = "联系人姓名", example = "张三")
    private String contactPerson;
    
    /** Contact phone */
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;
    
    /** Industry */
    @Schema(description = "行业", example = "互联网")
    private String industry;
    
    /** City */
    @Schema(description = "城市", example = "杭州")
    private String city;
    
    /** Audit status: 0=pending, 1=passed, 2=rejected */
    @Schema(description = "审核状态", example = "0", allowableValues = {"0", "1", "2"})
    private Integer auditStatus;
    
    /** Audit remark/reason */
    @Schema(description = "审核备注", example = "待审核")
    private String auditRemark;
    
    /** Submission date */
    @Schema(description = "提交日期", example = "2024-03-15T10:30:00+08:00")
    private OffsetDateTime submissionDate;
}
