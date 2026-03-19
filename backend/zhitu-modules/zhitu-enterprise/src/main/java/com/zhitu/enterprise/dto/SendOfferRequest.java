package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SendOfferRequest {
    @NotNull
    private Long applicationId;
    private Integer salary;
    private LocalDate startDate;
    private LocalDate endDate;
}
