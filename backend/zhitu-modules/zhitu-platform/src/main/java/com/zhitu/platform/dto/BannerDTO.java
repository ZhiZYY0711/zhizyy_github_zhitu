package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 推荐横幅 DTO
 * Recommendation Banner Data Transfer Object
 */
@Schema(description = "推荐横幅")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {

    /**
     * 横幅 ID
     */
    @Schema(description = "横幅ID", example = "1")
    private Long id;

    /**
     * 横幅标题
     */
    @Schema(description = "横幅标题", example = "春季校园招聘会")
    private String title;

    /**
     * 图片 URL
     */
    @Schema(description = "图片URL", example = "https://example.com/banner.jpg")
    private String imageUrl;

    /**
     * 链接 URL
     */
    @Schema(description = "链接URL", example = "https://example.com/event/123")
    private String linkUrl;

    /**
     * 目标门户：student, enterprise, college, all
     */
    @Schema(description = "目标门户", example = "student", allowableValues = {"student", "enterprise", "college", "all"})
    private String targetPortal;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期", example = "2024-03-31")
    private LocalDate endDate;

    /**
     * 排序顺序
     */
    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    /**
     * 状态：1=active, 0=inactive
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;
}
