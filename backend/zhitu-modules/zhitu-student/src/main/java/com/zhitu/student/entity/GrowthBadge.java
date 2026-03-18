package com.zhitu.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 徽章/证书实体 - growth_service.growth_badge
 */
@Data
@TableName(schema = "growth_service", value = "growth_badge")
public class GrowthBadge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** certificate / badge */
    private String type;

    private String name;

    private LocalDate issueDate;

    private String imageUrl;

    /** 区块链存证哈希 */
    private String blockchainHash;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Boolean isDeleted;
}
