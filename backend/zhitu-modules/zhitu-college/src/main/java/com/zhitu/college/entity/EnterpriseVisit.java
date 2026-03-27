package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 企业走访记录实体 - college_svc.enterprise_visit
 */
@Schema(description = "企业走访记录实体")
@Data
@TableName(schema = "college_svc", value = "enterprise_visit")
public class EnterpriseVisit {

    @Schema(description = "走访记录ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "高校租户ID", example = "1001")
    private Long collegeTenantId;

    @Schema(description = "企业租户ID", example = "2001")
    private Long enterpriseTenantId;

    @Schema(description = "走访日期", example = "2024-03-15")
    private LocalDate visitDate;

    @Schema(description = "走访人ID", example = "3001")
    private Long visitorId;

    @Schema(description = "走访人姓名", example = "李老师")
    private String visitorName;

    @Schema(description = "走访目的", example = "了解企业用人需求")
    private String purpose;

    @Schema(description = "走访成果", example = "达成初步合作意向")
    private String outcome;

    @Schema(description = "后续行动", example = "安排学生实习对接")
    private String nextAction;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @Schema(description = "是否删除", example = "false")
    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
