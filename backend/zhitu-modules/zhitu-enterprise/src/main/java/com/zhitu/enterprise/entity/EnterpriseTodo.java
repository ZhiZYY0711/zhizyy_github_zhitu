package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 企业待办事项实体 - enterprise_svc.enterprise_todo
 */
@Data
@TableName(schema = "enterprise_svc", value = "enterprise_todo")
public class EnterpriseTodo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long userId;

    /** application_review / interview_schedule / report_review / evaluation_pending */
    private String todoType;

    private String refType;

    private Long refId;

    private String title;

    /** 1=low, 2=medium, 3=high */
    private Integer priority;

    private OffsetDateTime dueDate;

    /** 0=pending, 1=completed */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
