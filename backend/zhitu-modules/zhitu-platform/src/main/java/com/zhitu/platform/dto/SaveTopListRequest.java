package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存推荐榜单请求
 * Save Top List Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveTopListRequest {

    /**
     * 榜单类型（必填）：mentor, course, project
     */
    private String listType;

    /**
     * 项目 ID 列表（必填，最多 10 个）
     */
    private List<Long> itemIds;
}
