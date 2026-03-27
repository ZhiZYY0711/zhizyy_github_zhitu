package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证书DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "证书信息")
public class CertificateDTO {
    
    @Schema(description = "证书ID", example = "1")
    private Long id;
    
    @Schema(description = "类型", example = "certificate")
    private String type;
    
    @Schema(description = "证书名称", example = "Java开发工程师认证")
    private String name;
    
    @Schema(description = "颁发日期", example = "2024-01-15")
    private LocalDate issueDate;
    
    @Schema(description = "证书图片URL", example = "https://example.com/cert.png")
    private String imageUrl;
    
    @Schema(description = "PDF下载链接", example = "https://example.com/cert.pdf")
    private String downloadUrl;
    
    @Schema(description = "区块链哈希值", example = "0x1234567890abcdef")
    private String blockchainHash;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
