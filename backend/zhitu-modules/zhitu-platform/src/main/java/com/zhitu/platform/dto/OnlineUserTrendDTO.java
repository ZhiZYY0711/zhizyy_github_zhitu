package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 在线用户趋势数据点 DTO
 * Requirements: 29.6
 */
@Schema(description = "在线用户趋势数据点")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserTrendDTO {

    /** 时间戳 */
    @Schema(description = "时间戳", example = "2024-03-15T10:00:00+08:00")
    private OffsetDateTime timestamp;

    /** 在线用户总数 */
    @Schema(description = "在线用户总数", example = "1500")
    private Integer count;

    /** 学生用户数 */
    @Schema(description = "学生用户数", example = "800")
    private Integer studentCount;

    /** 企业用户数 */
    @Schema(description = "企业用户数", example = "500")
    private Integer enterpriseCount;

    /** 学院用户数 */
    @Schema(description = "学院用户数", example = "200")
    private Integer collegeCount;
}
