package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Offer 实体 - internship_svc.internship_offer
 */
@Schema(description = "实习Offer实体")
@Data
@TableName(schema = "internship_svc", value = "internship_offer")
public class InternshipOffer {

    @Schema(description = "Offer ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "申请记录ID", example = "2001")
    private Long applicationId;
    
    @Schema(description = "学生ID", example = "3001")
    private Long studentId;
    
    @Schema(description = "企业ID", example = "4001")
    private Long enterpriseId;
    
    @Schema(description = "岗位ID", example = "5001")
    private Long jobId;
    
    @Schema(description = "薪资（元/月）", example = "5000")
    private Integer salary;
    
    @Schema(description = "实习开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "实习结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "Offer状态：0-待确认，1-已接受，2-已拒绝", example = "1")
    /** 0=待确认 1=已接受 2=已拒绝 */
    private Integer status;
    
    @Schema(description = "高校审核状态：0-待审核，1-通过，2-拒绝", example = "1")
    /** 0=待审核 1=通过 2=拒绝 */
    private Integer collegeAudit;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
