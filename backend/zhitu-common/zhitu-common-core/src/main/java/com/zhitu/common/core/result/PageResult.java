package com.zhitu.common.core.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应体
 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private List<T> records;
    private Integer page;
    private Integer size;

    public static <T> PageResult<T> of(Long total, List<T> records, Integer page, Integer size) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}
