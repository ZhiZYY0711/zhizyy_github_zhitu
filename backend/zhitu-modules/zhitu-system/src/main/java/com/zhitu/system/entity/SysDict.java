package com.zhitu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(schema = "platform_service", value = "sys_dict")
public class SysDict {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;
    private String code;
    private String label;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
