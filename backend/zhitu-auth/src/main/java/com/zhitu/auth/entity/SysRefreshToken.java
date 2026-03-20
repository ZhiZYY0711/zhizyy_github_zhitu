package com.zhitu.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "auth_center", value = "sys_refresh_token")
public class SysRefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String tokenHash;  // SHA-256 哈希
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
}
