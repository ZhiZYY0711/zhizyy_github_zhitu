package com.zhitu.platform.dto;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseAuditDTO {
    
    /** Enterprise info ID */
    private Long id;
    
    /** Enterprise tenant ID */
    private Long tenantId;
    
    /** Enterprise name */
    private String enterpriseName;
    
    /** Business license number */
    private String businessLicense;
    
    /** Contact person name */
    private String contactPerson;
    
    /** Contact phone */
    private String contactPhone;
    
    /** Industry */
    private String industry;
    
    /** City */
    private String city;
    
    /** Audit status: 0=pending, 1=passed, 2=rejected */
    private Integer auditStatus;
    
    /** Audit remark/reason */
    private String auditRemark;
    
    /** Submission date */
    private OffsetDateTime submissionDate;
}
