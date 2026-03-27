package com.zhitu.common.doc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档元数据模型
 * 
 * 用于记录 API 文档生成的元信息，包括服务名称、版本、生成时间等。
 * 
 * @author Zhitu Platform
 * @since 1.0.0
 */
public class DocumentMetadata {
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务版本
     */
    private String version;
    
    /**
     * 文档生成时间
     */
    private LocalDateTime generatedAt;
    
    /**
     * 文档生成者（系统或用户）
     */
    private String generatedBy;
    
    /**
     * 运行环境（dev、test、prod）
     */
    private String environment;
    
    /**
     * 自定义属性
     */
    private Map<String, String> customProperties;
    
    public DocumentMetadata() {
        this.customProperties = new HashMap<>();
        this.generatedAt = LocalDateTime.now();
    }
    
    public DocumentMetadata(String serviceName, String version, String environment) {
        this();
        this.serviceName = serviceName;
        this.version = version;
        this.environment = environment;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public String getGeneratedBy() {
        return generatedBy;
    }
    
    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }
    
    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
    
    /**
     * 添加自定义属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void addCustomProperty(String key, String value) {
        this.customProperties.put(key, value);
    }
    
    /**
     * 获取自定义属性
     * 
     * @param key 属性键
     * @return 属性值，如果不存在返回 null
     */
    public String getCustomProperty(String key) {
        return this.customProperties.get(key);
    }
    
    @Override
    public String toString() {
        return "DocumentMetadata{" +
                "serviceName='" + serviceName + '\'' +
                ", version='" + version + '\'' +
                ", generatedAt=" + generatedAt +
                ", generatedBy='" + generatedBy + '\'' +
                ", environment='" + environment + '\'' +
                ", customProperties=" + customProperties +
                '}';
    }
}
