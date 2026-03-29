package com.zhitu.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习学生列表 DTO - 包含可读名称，供前端渲染使用
 */
@Schema(description = "实习学生列表项（包含关联名称）")
@Data
public class InternshipStudentDTO {

    // ── 实习记录基础字段 ──────────────────────────────────────────────
    @Schema(description = "实习记录ID", example = "10561")
    private Long id;

    @Schema(description = "学生ID", example = "1276")
    private Long studentId;

    @Schema(description = "企业ID", example = "16")
    private Long enterpriseId;

    @Schema(description = "岗位ID", example = "29")
    private Long jobId;

    @Schema(description = "企业导师ID", example = "84")
    private Long mentorId;

    @Schema(description = "指导教师ID", example = "31")
    private Long teacherId;

    @Schema(description = "实习开始日期", example = "2025-03-03")
    private LocalDate startDate;

    @Schema(description = "实习结束日期", example = "2025-10-03")
    private LocalDate endDate;

    /** 1=实习中 2=已结束 */
    @Schema(description = "实习状态：1-实习中，2-已结束", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    // ── 关联的可读名称（前端渲染所需）───────────────────────────────────
    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学号", example = "2021001001")
    private String studentNo;

    @Schema(description = "企业名称", example = "字节跳动")
    private String enterpriseName;

    @Schema(description = "岗位名称", example = "后端开发实习生")
    private String jobTitle;

    @Schema(description = "企业导师姓名", example = "李导师")
    private String mentorName;

    @Schema(description = "指导教师姓名", example = "王老师")
    private String teacherName;

    @Schema(description = "最近提交周报时间", example = "2025-03-15T10:00:00Z")
    private OffsetDateTime lastReportTime;

    @Schema(description = "状态文本：normal-正常，warning-异常", example = "normal")
    private String statusText;
}
