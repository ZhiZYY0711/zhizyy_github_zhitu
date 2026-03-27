package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "企业资料更新请求")
public class EnterpriseProfileUpdateRequest {
    @NotBlank
    @Schema(description = "企业名称", example = "智途科技有限公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enterpriseName;
    
    @Schema(description = "所属行业", example = "互联网")
    private String industry;
    
    @Schema(description = "企业规模", example = "100-500人")
    private String scale;
    
    @Schema(description = "省份", example = "北京市")
    private String province;
    
    @Schema(description = "城市", example = "北京")
    private String city;
    
    @Schema(description = "详细地址", example = "朝阳区建国路88号")
    private String address;
    
    @Schema(description = "企业Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;
    
    @Schema(description = "企业网站", example = "https://www.example.com")
    private String website;
    
    @Schema(description = "企业简介", example = "专注于教育科技的创新企业")
    private String description;
    
    @Schema(description = "联系人姓名", example = "李经理")
    private String contactName;
    
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;
    
    @Schema(description = "联系邮箱", example = "contact@example.com")
    private String contactEmail;
}
