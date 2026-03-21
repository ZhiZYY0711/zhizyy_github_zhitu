package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐榜单 DTO
 * Top List Data Transfer Object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopListDTO {

    /**
     * 榜单类型：mentor, course, project
     */
    private String listType;

    /**
     * 项目 ID 列表（按位置排序）
     */
    private List<Long> itemIds;
}
