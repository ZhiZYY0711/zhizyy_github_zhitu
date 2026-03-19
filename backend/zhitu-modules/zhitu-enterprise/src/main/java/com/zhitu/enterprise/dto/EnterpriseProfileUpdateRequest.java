package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnterpriseProfileUpdateRequest {
    @NotBlank
    private String enterpriseName;
    private String industry;
    private String scale;
    private String province;
    private String city;
    private String address;
    private String logoUrl;
    private String website;
    private String description;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
}
