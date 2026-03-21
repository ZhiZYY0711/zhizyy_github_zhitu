package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 在线用户趋势数据点 DTO
 * Requirements: 29.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserTrendDTO {

    /** 时间戳 */
    private OffsetDateTime timestamp;

    /** 在线用户总数 */
    private Integer count;

    /** 学生用户数 */
    private Integer studentCount;

    /** 企业用户数 */
    private Integer enterpriseCount;

    /** 学院用户数 */
    private Integer collegeCount;
}
