package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学生档案实体 - student_svc.student_info
 */
@Schema(description = "学生档案实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "student_svc", value = "student_info")
public class StudentInfo extends BaseEntity {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;
    
    @Schema(description = "租户ID", example = "2001")
    private Long tenantId;
    
    @Schema(description = "学号", example = "2021001001")
    private String studentNo;
    
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    @Schema(description = "性别：1-男，2-女", example = "1")
    private Integer gender;
    
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
    
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
    
    @Schema(description = "学院ID", example = "100")
    private Long collegeId;
    
    @Schema(description = "专业ID", example = "200")
    private Long majorId;
    
    @Schema(description = "班级ID", example = "300")
    private Long classId;
    
    @Schema(description = "年级", example = "2021")
    private String grade;
    
    @Schema(description = "入学日期", example = "2021-09-01")
    private LocalDate enrollmentDate;
    
    @Schema(description = "毕业日期", example = "2025-06-30")
    private LocalDate graduationDate;
    
    @Schema(description = "简历URL", example = "https://example.com/resume.pdf")
    private String resumeUrl;
    
    @Schema(description = "技能标签", example = "Java,Python,MySQL")
    private String skills;
}
