package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 保存推荐横幅请求
 * Save Recommendation Banner Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveBannerRequest {

    /**
     * 横幅 ID（更新时提供，创建时为 null）
     */
    private Long id;

    /**
     * 横幅标题（必填）
     */
    private String title;

    /**
     * 图片 URL（必填）
     */
    private String imageUrl;

    /**
     * 链接 URL（必填）
     */
    private String linkUrl;

    /**
     * 目标门户（必填）：student, enterprise, college, all
     */
    private String targetPortal;

    /**
     * 开始日期（必填）
     */
    private LocalDate startDate;

    /**
     * 结束日期（必填）
     */
    private LocalDate endDate;

    /**
     * 排序顺序（可选，默认 0）
     */
    private Integer sortOrder;

    /**
     * 状态（可选，默认 1=active）：1=active, 0=inactive
     */
    private Integer status;
}
