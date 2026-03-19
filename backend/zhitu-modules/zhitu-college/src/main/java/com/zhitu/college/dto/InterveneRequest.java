package com.zhitu.college.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterveneRequest {
    @NotBlank
    private String interveneNote;
}
