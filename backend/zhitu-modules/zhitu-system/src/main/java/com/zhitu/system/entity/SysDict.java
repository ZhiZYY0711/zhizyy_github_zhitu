package com.zhitu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "platform_service", value = "sys_dict")
@Schema(description = "系统字典实体")
public class SysDict {

    @TableId(type = IdType.AUTO)
    @Schema(description = "字典ID", example = "1")
    private Long id;

    @Schema(description = "字典分类", example = "industry_type", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @Schema(description = "字典编码", example = "IT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "字典标签", example = "信息技术", requiredMode = Schema.RequiredMode.REQUIRED)
    private String label;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    @Schema(description = "是否删除", example = "false")
    private Boolean isDeleted;
}
