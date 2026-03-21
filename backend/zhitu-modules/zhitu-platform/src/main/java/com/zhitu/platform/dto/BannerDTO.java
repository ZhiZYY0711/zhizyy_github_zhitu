package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 推荐横幅 DTO
 * Recommendation Banner Data Transfer Object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {

    /**
     * 横幅 ID
     */
    private Long id;

    /**
     * 横幅标题
     */
    private String title;

    /**
     * 图片 URL
     */
    private String imageUrl;

    /**
     * 链接 URL
     */
    private String linkUrl;

    /**
     * 目标门户：student, enterprise, college, all
     */
    private String targetPortal;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：1=active, 0=inactive
     */
    private Integer status;
}
