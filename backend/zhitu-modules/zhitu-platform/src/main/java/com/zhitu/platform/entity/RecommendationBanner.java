package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 推荐横幅实体 - platform_service.recommendation_banner
 */
@Data
@TableName(schema = "platform_service", value = "recommendation_banner")
public class RecommendationBanner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    /** student / enterprise / college / all */
    private String targetPortal;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer sortOrder;

    /** 1=active, 0=inactive */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
