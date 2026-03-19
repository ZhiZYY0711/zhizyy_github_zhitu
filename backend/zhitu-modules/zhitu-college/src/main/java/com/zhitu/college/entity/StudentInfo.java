package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学生档案实体 - student_svc.student_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "student_svc", value = "student_info")
public class StudentInfo extends BaseEntity {

    private Long userId;
    private Long tenantId;
    private String studentNo;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private String avatarUrl;
    private Long collegeId;
    private Long majorId;
    private Long classId;
    private String grade;
    private LocalDate enrollmentDate;
    private LocalDate graduationDate;
    private String resumeUrl;
    private String skills;
}
