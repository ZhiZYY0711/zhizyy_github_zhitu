package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TalentCollectRequest {
    @NotNull
    private Long studentId;
    private String remark;
}
