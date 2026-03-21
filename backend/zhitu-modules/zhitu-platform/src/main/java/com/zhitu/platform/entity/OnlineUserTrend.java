package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 在线用户趋势实体 - platform_service.online_user_trend
 */
@Data
@TableName(schema = "platform_service", value = "online_user_trend")
public class OnlineUserTrend {

    @TableId(type = IdType.AUTO)
    private Long id;

    private OffsetDateTime timestamp;

    private Integer onlineCount;

    private Integer studentCount;

    private Integer enterpriseCount;

    private Integer collegeCount;
}
