package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 保存推荐横幅请求
 * Save Recommendation Banner Request
 */
@Schema(description = "保存推荐横幅请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveBannerRequest {

    /**
     * 横幅 ID（更新时提供，创建时为 null）
     */
    @Schema(description = "横幅ID（更新时提供，创建时为null）", example = "1")
    private Long id;

    /**
     * 横幅标题（必填）
     */
    @Schema(description = "横幅标题", example = "春季校园招聘会", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 图片 URL（必填）
     */
    @Schema(description = "图片URL", example = "https://example.com/banner.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageUrl;

    /**
     * 链接 URL（必填）
     */
    @Schema(description = "链接URL", example = "https://example.com/event/123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String linkUrl;

    /**
     * 目标门户（必填）：student, enterprise, college, all
     */
    @Schema(description = "目标门户", example = "student", allowableValues = {"student", "enterprise", "college", "all"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetPortal;

    /**
     * 开始日期（必填）
     */
    @Schema(description = "开始日期", example = "2024-03-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    /**
     * 结束日期（必填）
     */
    @Schema(description = "结束日期", example = "2024-03-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    /**
     * 排序顺序（可选，默认 0）
     */
    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    /**
     * 状态（可选，默认 1=active）：1=active, 0=inactive
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}
