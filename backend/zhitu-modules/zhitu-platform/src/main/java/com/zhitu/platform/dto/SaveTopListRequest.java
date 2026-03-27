package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存推荐榜单请求
 * Save Top List Request
 */
@Schema(description = "保存推荐榜单请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveTopListRequest {

    /**
     * 榜单类型（必填）：mentor, course, project
     */
    @Schema(description = "榜单类型", example = "project", allowableValues = {"mentor", "course", "project"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String listType;

    /**
     * 项目 ID 列表（必填，最多 10 个）
     */
    @Schema(description = "项目ID列表（最多10个）", example = "[1, 2, 3, 4, 5]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> itemIds;
}
