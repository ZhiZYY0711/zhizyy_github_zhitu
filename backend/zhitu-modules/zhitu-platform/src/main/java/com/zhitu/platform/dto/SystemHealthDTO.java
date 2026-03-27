package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统健康状态 DTO
 * Requirements: 29.1-29.7
 */
@Schema(description = "系统健康状态")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDTO {

    /** 所有微服务的健康状态 */
    @Schema(description = "所有微服务的健康状态")
    private List<ServiceHealthDTO> services;
}
