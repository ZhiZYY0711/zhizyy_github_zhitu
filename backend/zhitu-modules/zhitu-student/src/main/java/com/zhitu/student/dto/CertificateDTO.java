package com.zhitu.student.dto;

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
public class CertificateDTO {
    private Long id;
    private String type; // certificate
    private String name;
    private LocalDate issueDate;
    private String imageUrl;
    private String downloadUrl; // PDF下载链接
    private String blockchainHash;
    private LocalDateTime createdAt;
}
