package com.zhitu.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应体
 */
@Data
@Schema(description = "分页响应体")
public class PageResult<T> implements Serializable {

    @Schema(description = "总记录数", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long total;
    
    @Schema(description = "当前页数据列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> records;
    
    @Schema(description = "当前页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer page;
    
    @Schema(description = "每页记录数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
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
