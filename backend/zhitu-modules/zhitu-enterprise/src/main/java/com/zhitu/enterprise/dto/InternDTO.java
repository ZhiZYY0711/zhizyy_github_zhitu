package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习生信息DTO
 */
@Data
@Schema(description = "实习生信息")
public class InternDTO {

    @Schema(description = "实习记录ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1534")
    private Long studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学号", example = "2021001001")
    private String studentNo;

    @Schema(description = "学生手机号", example = "13800138000")
    private String studentPhone;

    @Schema(description = "学校名称", example = "清华大学")
    private String schoolName;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "年级", example = "2021")
    private String grade;

    @Schema(description = "企业ID", example = "7")
    private Long enterpriseId;

    @Schema(description = "企业名称", example = "智图科技有限公司")
    private String enterpriseName;

    @Schema(description = "岗位ID", example = "135")
    private Long jobId;

    @Schema(description = "岗位名称", example = "Java开发实习生")
    private String jobTitle;

    @Schema(description = "企业导师ID", example = "52")
    private Long mentorId;

    @Schema(description = "企业导师姓名", example = "李导师")
    private String mentorName;

    @Schema(description = "学校指导教师ID", example = "38")
    private Long teacherId;

    @Schema(description = "学校指导教师姓名", example = "王老师")
    private String teacherName;

    @Schema(description = "实习开始日期", example = "2025-05-22")
    private LocalDate startDate;

    @Schema(description = "实习结束日期", example = "2025-08-22")
    private LocalDate endDate;

    @Schema(description = "实习状态：1-实习中，2-已结束", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2026-03-28T04:06:37.403295Z")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-03-28T04:06:37.403295Z")
    private OffsetDateTime updatedAt;
}
