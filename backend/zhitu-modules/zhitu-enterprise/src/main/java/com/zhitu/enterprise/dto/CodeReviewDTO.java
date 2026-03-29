package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 代码评审DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "代码评审数据")
public class CodeReviewDTO {
    @Schema(description = "评审ID", example = "1")
    private Long id;

    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    @Schema(description = "项目名称", example = "电商高并发秒杀系统")
    private String projectName;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "文件名", example = "UserService.java")
    private String file;

    @Schema(description = "行号", example = "45")
    private Integer line;

    @Schema(description = "代码片段", example = "for(User u : users) { db.save(u); }")
    private String codeSnippet;

    @Schema(description = "评审意见", example = "建议使用批量插入优化性能")
    private String comment;

    @Schema(description = "状态: pending/resolved/closed", example = "pending")
    private String status;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "解决时间")
    private OffsetDateTime resolvedAt;
}
