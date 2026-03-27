package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 在线用户趋势响应 DTO
 * Requirements: 29.6
 */
@Schema(description = "在线用户趋势响应")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserTrendResponseDTO {

    /** 过去24小时的在线用户趋势数据 */
    @Schema(description = "过去24小时的在线用户趋势数据")
    private List<OnlineUserTrendDTO> trend;
}
