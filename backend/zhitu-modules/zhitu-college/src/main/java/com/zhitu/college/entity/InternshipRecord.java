package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习记录实体 - internship_svc.internship_record
 */
@Schema(description = "实习记录实体")
@Data
@TableName(schema = "internship_svc", value = "internship_record")
public class InternshipRecord {

    @Schema(description = "实习记录ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "学生ID", example = "3001")
    private Long studentId;
    
    @Schema(description = "企业ID", example = "4001")
    private Long enterpriseId;
    
    @Schema(description = "岗位ID", example = "5001")
    private Long jobId;
    
    @Schema(description = "企业导师ID", example = "6001")
    private Long mentorId;
    
    @Schema(description = "指导教师ID", example = "7001")
    private Long teacherId;
    
    @Schema(description = "实习开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "实习结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "实习状态：1-实习中，2-已结束", example = "1")
    /** 1=实习中 2=已结束 */
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
