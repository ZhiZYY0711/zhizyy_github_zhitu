package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 推荐榜单实体 - platform_service.recommendation_top_list
 */
@Data
@TableName(schema = "platform_service", value = "recommendation_top_list")
public class RecommendationTopList {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** mentor / course / project */
    private String listType;

    /** JSON array of IDs, ordered by position */
    private String itemIds;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
