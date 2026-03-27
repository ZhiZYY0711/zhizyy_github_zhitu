package com.zhitu.common.doc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 变更报告模型
 * 
 * 用于记录 API 文档在不同版本之间的变更信息，包括端点变更、数据模型变更和统计信息。
 * 
 * @author Zhitu Platform
 * @since 1.0.0
 */
public class ChangeReport {
    
    /**
     * 报告生成时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 旧版本号
     */
    private String oldVersion;
    
    /**
     * 新版本号
     */
    private String newVersion;
    
    /**
     * 端点变更列表
     */
    private List<EndpointChange> endpointChanges;
    
    /**
     * 数据模型变更列表
     */
    private List<SchemaChange> schemaChanges;
    
    /**
     * 变更统计信息
     */
    private ChangeStatistics statistics;
    
    public ChangeReport() {
        this.timestamp = LocalDateTime.now();
        this.endpointChanges = new ArrayList<>();
        this.schemaChanges = new ArrayList<>();
        this.statistics = new ChangeStatistics();
    }
    
    public ChangeReport(String oldVersion, String newVersion) {
        this();
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getOldVersion() {
        return oldVersion;
    }
    
    public void setOldVersion(String oldVersion) {
        this.oldVersion = oldVersion;
    }
    
    public String getNewVersion() {
        return newVersion;
    }
    
    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }
    
    public List<EndpointChange> getEndpointChanges() {
        return endpointChanges;
    }
    
    public void setEndpointChanges(List<EndpointChange> endpointChanges) {
        this.endpointChanges = endpointChanges;
    }
    
    public List<SchemaChange> getSchemaChanges() {
        return schemaChanges;
    }
    
    public void setSchemaChanges(List<SchemaChange> schemaChanges) {
        this.schemaChanges = schemaChanges;
    }
    
    public ChangeStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(ChangeStatistics statistics) {
        this.statistics = statistics;
    }
    
    /**
     * 添加端点变更
     * 
     * @param change 端点变更
     */
    public void addEndpointChange(EndpointChange change) {
        this.endpointChanges.add(change);
        updateStatistics(change.getType(), true);
    }
    
    /**
     * 添加数据模型变更
     * 
     * @param change 数据模型变更
     */
    public void addSchemaChange(SchemaChange change) {
        this.schemaChanges.add(change);
        updateStatistics(change.getType(), false);
    }
    
    /**
     * 更新统计信息
     * 
     * @param type 变更类型
     * @param isEndpoint 是否为端点变更
     */
    private void updateStatistics(ChangeType type, boolean isEndpoint) {
        if (isEndpoint) {
            switch (type) {
                case ADDED:
                    statistics.addedEndpoints++;
                    break;
                case REMOVED:
                    statistics.removedEndpoints++;
                    break;
                case MODIFIED:
                    statistics.modifiedEndpoints++;
                    break;
            }
        } else {
            switch (type) {
                case ADDED:
                    statistics.addedSchemas++;
                    break;
                case REMOVED:
                    statistics.removedSchemas++;
                    break;
                case MODIFIED:
                    statistics.modifiedSchemas++;
                    break;
            }
        }
    }
    
    @Override
    public String toString() {
        return "ChangeReport{" +
                "timestamp=" + timestamp +
                ", oldVersion='" + oldVersion + '\'' +
                ", newVersion='" + newVersion + '\'' +
                ", endpointChanges=" + endpointChanges.size() +
                ", schemaChanges=" + schemaChanges.size() +
                ", statistics=" + statistics +
                '}';
    }
    
    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        /**
         * 新增
         */
        ADDED,
        
        /**
         * 删除
         */
        REMOVED,
        
        /**
         * 修改
         */
        MODIFIED
    }

    
    /**
     * 端点变更
     */
    public static class EndpointChange {
        
        /**
         * 变更类型
         */
        private ChangeType type;
        
        /**
         * 端点路径
         */
        private String path;
        
        /**
         * HTTP 方法
         */
        private String method;
        
        /**
         * 变更描述
         */
        private String description;
        
        /**
         * 参数变更列表
         */
        private List<ParameterChange> parameterChanges;
        
        /**
         * 响应变更
         */
        private ResponseChange responseChange;
        
        public EndpointChange() {
            this.parameterChanges = new ArrayList<>();
        }
        
        public EndpointChange(ChangeType type, String path, String method) {
            this();
            this.type = type;
            this.path = path;
            this.method = method;
        }
        
        public ChangeType getType() {
            return type;
        }
        
        public void setType(ChangeType type) {
            this.type = type;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<ParameterChange> getParameterChanges() {
            return parameterChanges;
        }
        
        public void setParameterChanges(List<ParameterChange> parameterChanges) {
            this.parameterChanges = parameterChanges;
        }
        
        public ResponseChange getResponseChange() {
            return responseChange;
        }
        
        public void setResponseChange(ResponseChange responseChange) {
            this.responseChange = responseChange;
        }
        
        /**
         * 添加参数变更
         * 
         * @param change 参数变更
         */
        public void addParameterChange(ParameterChange change) {
            this.parameterChanges.add(change);
        }
        
        @Override
        public String toString() {
            return "EndpointChange{" +
                    "type=" + type +
                    ", path='" + path + '\'' +
                    ", method='" + method + '\'' +
                    ", description='" + description + '\'' +
                    ", parameterChanges=" + parameterChanges.size() +
                    ", responseChange=" + responseChange +
                    '}';
        }
    }
    
    /**
     * 参数变更
     */
    public static class ParameterChange {
        
        /**
         * 变更类型
         */
        private ChangeType type;
        
        /**
         * 参数名称
         */
        private String name;
        
        /**
         * 参数位置（query、path、header、body）
         */
        private String location;
        
        /**
         * 旧类型
         */
        private String oldType;
        
        /**
         * 新类型
         */
        private String newType;
        
        /**
         * 是否必填
         */
        private Boolean required;
        
        /**
         * 变更描述
         */
        private String description;
        
        public ParameterChange() {
        }
        
        public ParameterChange(ChangeType type, String name, String location) {
            this.type = type;
            this.name = name;
            this.location = location;
        }
        
        public ChangeType getType() {
            return type;
        }
        
        public void setType(ChangeType type) {
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getOldType() {
            return oldType;
        }
        
        public void setOldType(String oldType) {
            this.oldType = oldType;
        }
        
        public String getNewType() {
            return newType;
        }
        
        public void setNewType(String newType) {
            this.newType = newType;
        }
        
        public Boolean getRequired() {
            return required;
        }
        
        public void setRequired(Boolean required) {
            this.required = required;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "ParameterChange{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    ", location='" + location + '\'' +
                    ", oldType='" + oldType + '\'' +
                    ", newType='" + newType + '\'' +
                    ", required=" + required +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
    
    /**
     * 响应变更
     */
    public static class ResponseChange {
        
        /**
         * 变更类型
         */
        private ChangeType type;
        
        /**
         * HTTP 状态码
         */
        private String statusCode;
        
        /**
         * 旧响应类型
         */
        private String oldResponseType;
        
        /**
         * 新响应类型
         */
        private String newResponseType;
        
        /**
         * 变更描述
         */
        private String description;
        
        public ResponseChange() {
        }
        
        public ResponseChange(ChangeType type, String statusCode) {
            this.type = type;
            this.statusCode = statusCode;
        }
        
        public ChangeType getType() {
            return type;
        }
        
        public void setType(ChangeType type) {
            this.type = type;
        }
        
        public String getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }
        
        public String getOldResponseType() {
            return oldResponseType;
        }
        
        public void setOldResponseType(String oldResponseType) {
            this.oldResponseType = oldResponseType;
        }
        
        public String getNewResponseType() {
            return newResponseType;
        }
        
        public void setNewResponseType(String newResponseType) {
            this.newResponseType = newResponseType;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "ResponseChange{" +
                    "type=" + type +
                    ", statusCode='" + statusCode + '\'' +
                    ", oldResponseType='" + oldResponseType + '\'' +
                    ", newResponseType='" + newResponseType + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    
    /**
     * 数据模型变更
     */
    public static class SchemaChange {
        
        /**
         * 变更类型
         */
        private ChangeType type;
        
        /**
         * 数据模型名称
         */
        private String schemaName;
        
        /**
         * 属性变更列表
         */
        private List<PropertyChange> propertyChanges;
        
        /**
         * 变更描述
         */
        private String description;
        
        public SchemaChange() {
            this.propertyChanges = new ArrayList<>();
        }
        
        public SchemaChange(ChangeType type, String schemaName) {
            this();
            this.type = type;
            this.schemaName = schemaName;
        }
        
        public ChangeType getType() {
            return type;
        }
        
        public void setType(ChangeType type) {
            this.type = type;
        }
        
        public String getSchemaName() {
            return schemaName;
        }
        
        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }
        
        public List<PropertyChange> getPropertyChanges() {
            return propertyChanges;
        }
        
        public void setPropertyChanges(List<PropertyChange> propertyChanges) {
            this.propertyChanges = propertyChanges;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * 添加属性变更
         * 
         * @param change 属性变更
         */
        public void addPropertyChange(PropertyChange change) {
            this.propertyChanges.add(change);
        }
        
        @Override
        public String toString() {
            return "SchemaChange{" +
                    "type=" + type +
                    ", schemaName='" + schemaName + '\'' +
                    ", propertyChanges=" + propertyChanges.size() +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
    
    /**
     * 属性变更
     */
    public static class PropertyChange {
        
        /**
         * 变更类型
         */
        private ChangeType type;
        
        /**
         * 属性名称
         */
        private String name;
        
        /**
         * 旧类型
         */
        private String oldType;
        
        /**
         * 新类型
         */
        private String newType;
        
        /**
         * 是否必填
         */
        private Boolean required;
        
        /**
         * 变更描述
         */
        private String description;
        
        public PropertyChange() {
        }
        
        public PropertyChange(ChangeType type, String name) {
            this.type = type;
            this.name = name;
        }
        
        public ChangeType getType() {
            return type;
        }
        
        public void setType(ChangeType type) {
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getOldType() {
            return oldType;
        }
        
        public void setOldType(String oldType) {
            this.oldType = oldType;
        }
        
        public String getNewType() {
            return newType;
        }
        
        public void setNewType(String newType) {
            this.newType = newType;
        }
        
        public Boolean getRequired() {
            return required;
        }
        
        public void setRequired(Boolean required) {
            this.required = required;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "PropertyChange{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    ", oldType='" + oldType + '\'' +
                    ", newType='" + newType + '\'' +
                    ", required=" + required +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
    
    /**
     * 变更统计信息
     */
    public static class ChangeStatistics {
        
        /**
         * 新增的端点数量
         */
        private int addedEndpoints;
        
        /**
         * 删除的端点数量
         */
        private int removedEndpoints;
        
        /**
         * 修改的端点数量
         */
        private int modifiedEndpoints;
        
        /**
         * 新增的数据模型数量
         */
        private int addedSchemas;
        
        /**
         * 删除的数据模型数量
         */
        private int removedSchemas;
        
        /**
         * 修改的数据模型数量
         */
        private int modifiedSchemas;
        
        public ChangeStatistics() {
        }
        
        public int getAddedEndpoints() {
            return addedEndpoints;
        }
        
        public void setAddedEndpoints(int addedEndpoints) {
            this.addedEndpoints = addedEndpoints;
        }
        
        public int getRemovedEndpoints() {
            return removedEndpoints;
        }
        
        public void setRemovedEndpoints(int removedEndpoints) {
            this.removedEndpoints = removedEndpoints;
        }
        
        public int getModifiedEndpoints() {
            return modifiedEndpoints;
        }
        
        public void setModifiedEndpoints(int modifiedEndpoints) {
            this.modifiedEndpoints = modifiedEndpoints;
        }
        
        public int getAddedSchemas() {
            return addedSchemas;
        }
        
        public void setAddedSchemas(int addedSchemas) {
            this.addedSchemas = addedSchemas;
        }
        
        public int getRemovedSchemas() {
            return removedSchemas;
        }
        
        public void setRemovedSchemas(int removedSchemas) {
            this.removedSchemas = removedSchemas;
        }
        
        public int getModifiedSchemas() {
            return modifiedSchemas;
        }
        
        public void setModifiedSchemas(int modifiedSchemas) {
            this.modifiedSchemas = modifiedSchemas;
        }
        
        /**
         * 获取总变更数量
         * 
         * @return 总变更数量
         */
        public int getTotalChanges() {
            return addedEndpoints + removedEndpoints + modifiedEndpoints +
                   addedSchemas + removedSchemas + modifiedSchemas;
        }
        
        /**
         * 是否有变更
         * 
         * @return 如果有任何变更返回 true，否则返回 false
         */
        public boolean hasChanges() {
            return getTotalChanges() > 0;
        }
        
        @Override
        public String toString() {
            return "ChangeStatistics{" +
                    "addedEndpoints=" + addedEndpoints +
                    ", removedEndpoints=" + removedEndpoints +
                    ", modifiedEndpoints=" + modifiedEndpoints +
                    ", addedSchemas=" + addedSchemas +
                    ", removedSchemas=" + removedSchemas +
                    ", modifiedSchemas=" + modifiedSchemas +
                    ", totalChanges=" + getTotalChanges() +
                    '}';
        }
    }
}
