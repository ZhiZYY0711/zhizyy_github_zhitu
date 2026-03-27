package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * 预警记录实体 - growth_svc.warning_record
 */
@Schema(description = "预警记录实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "growth_svc", value = "warning_record")
public class WarningRecord extends BaseEntity {

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;
    
    @Schema(description = "学生ID", example = "3001")
    private Long studentId;
    
    @Schema(description = "预警类型：attendance-考勤，report-报告，evaluation-评价", example = "attendance")
    /** attendance / report / evaluation */
    private String warningType;
    
    @Schema(description = "预警等级：1-轻微，2-一般，3-严重", example = "2")
    /** 1=轻微 2=一般 3=严重 */
    private Integer warningLevel;
    
    @Schema(description = "预警描述", example = "连续3天未打卡")
    private String description;
    
    @Schema(description = "处理状态：0-待处理，1-已干预，2-已关闭", example = "1")
    /** 0=待处理 1=已干预 2=已关闭 */
    private Integer status;
    
    @Schema(description = "干预措施说明", example = "已与学生沟通，了解缺勤原因")
    private String interveneNote;
    
    @Schema(description = "干预人ID", example = "4001")
    private Long intervenedBy;
    
    @Schema(description = "干预时间")
    private OffsetDateTime intervenedAt;
}
