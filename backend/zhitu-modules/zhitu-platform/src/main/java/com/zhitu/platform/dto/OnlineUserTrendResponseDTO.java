package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 在线用户趋势响应 DTO
 * Requirements: 29.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserTrendResponseDTO {

    /** 过去24小时的在线用户趋势数据 */
    private List<OnlineUserTrendDTO> trend;
}
