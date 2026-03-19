package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddStaffRequest {
    @NotNull
    private Long userId;
    private String department;
    private String position;
    private Boolean isMentor;
}
