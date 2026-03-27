package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐榜单 DTO
 * Top List Data Transfer Object
 */
@Schema(description = "推荐榜单")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopListDTO {

    /**
     * 榜单类型：mentor, course, project
     */
    @Schema(description = "榜单类型", example = "project", allowableValues = {"mentor", "course", "project"})
    private String listType;

    /**
     * 项目 ID 列表（按位置排序）
     */
    @Schema(description = "项目ID列表（按位置排序）", example = "[1, 2, 3, 4, 5]")
    private List<Long> itemIds;
}
