package com.zhitu.common.doc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解验证报告模型
 * 
 * 用于记录 Controller 类和 DTO 类中缺失的 Swagger 注解信息。
 * 
 * @author Zhitu Platform
 * @since 1.0.0
 */
public class ValidationReport {
    
    /**
     * 报告生成时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 扫描的包路径
     */
    private String packageName;
    
    /**
     * 缺少 @Tag 注解的 Controller 列表
     */
    private List<ControllerViolation> controllersWithoutTag;
    
    /**
     * 缺少 @Operation 注解的方法列表
     */
    private List<MethodViolation> methodsWithoutOperation;
    
    /**
     * 缺少 @Parameter 注解的参数列表
     */
    private List<ParameterViolation> parametersWithoutAnnotation;
    
    /**
     * 缺少 @Schema 注解的 DTO 类和字段列表
     */
    private List<SchemaViolation> schemasWithoutAnnotation;
    
    /**
     * 验证统计信息
     */
    private ValidationStatistics statistics;
    
    public ValidationReport() {
        this.timestamp = LocalDateTime.now();
        this.controllersWithoutTag = new ArrayList<>();
        this.methodsWithoutOperation = new ArrayList<>();
        this.parametersWithoutAnnotation = new ArrayList<>();
        this.schemasWithoutAnnotation = new ArrayList<>();
        this.statistics = new ValidationStatistics();
    }
    
    public ValidationReport(String packageName) {
        this();
        this.packageName = packageName;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public List<ControllerViolation> getControllersWithoutTag() {
        return controllersWithoutTag;
    }
    
    public void setControllersWithoutTag(List<ControllerViolation> controllersWithoutTag) {
        this.controllersWithoutTag = controllersWithoutTag;
    }
    
    public List<MethodViolation> getMethodsWithoutOperation() {
        return methodsWithoutOperation;
    }
    
    public void setMethodsWithoutOperation(List<MethodViolation> methodsWithoutOperation) {
        this.methodsWithoutOperation = methodsWithoutOperation;
    }
    
    public List<ParameterViolation> getParametersWithoutAnnotation() {
        return parametersWithoutAnnotation;
    }
    
    public void setParametersWithoutAnnotation(List<ParameterViolation> parametersWithoutAnnotation) {
        this.parametersWithoutAnnotation = parametersWithoutAnnotation;
    }
    
    public List<SchemaViolation> getSchemasWithoutAnnotation() {
        return schemasWithoutAnnotation;
    }
    
    public void setSchemasWithoutAnnotation(List<SchemaViolation> schemasWithoutAnnotation) {
        this.schemasWithoutAnnotation = schemasWithoutAnnotation;
    }
    
    public ValidationStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(ValidationStatistics statistics) {
        this.statistics = statistics;
    }
    
    /**
     * 添加 Controller 违规
     */
    public void addControllerViolation(ControllerViolation violation) {
        this.controllersWithoutTag.add(violation);
        this.statistics.controllersWithoutTag++;
    }
    
    /**
     * 添加方法违规
     */
    public void addMethodViolation(MethodViolation violation) {
        this.methodsWithoutOperation.add(violation);
        this.statistics.methodsWithoutOperation++;
    }
    
    /**
     * 添加参数违规
     */
    public void addParameterViolation(ParameterViolation violation) {
        this.parametersWithoutAnnotation.add(violation);
        this.statistics.parametersWithoutAnnotation++;
    }
    
    /**
     * 添加 Schema 违规
     */
    public void addSchemaViolation(SchemaViolation violation) {
        this.schemasWithoutAnnotation.add(violation);
        this.statistics.schemasWithoutAnnotation++;
    }
    
    /**
     * 是否有违规
     */
    public boolean hasViolations() {
        return statistics.getTotalViolations() > 0;
    }
    
    @Override
    public String toString() {
        return "ValidationReport{" +
                "timestamp=" + timestamp +
                ", packageName='" + packageName + '\'' +
                ", controllersWithoutTag=" + controllersWithoutTag.size() +
                ", methodsWithoutOperation=" + methodsWithoutOperation.size() +
                ", parametersWithoutAnnotation=" + parametersWithoutAnnotation.size() +
                ", schemasWithoutAnnotation=" + schemasWithoutAnnotation.size() +
                ", statistics=" + statistics +
                '}';
    }
    
    /**
     * Controller 违规
     */
    public static class ControllerViolation {
        private String className;
        private String message;
        
        public ControllerViolation() {
        }
        
        public ControllerViolation(String className, String message) {
            this.className = className;
            this.message = message;
        }
        
        public String getClassName() {
            return className;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        @Override
        public String toString() {
            return "ControllerViolation{" +
                    "className='" + className + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    
    /**
     * 方法违规
     */
    public static class MethodViolation {
        private String className;
        private String methodName;
        private String message;
        
        public MethodViolation() {
        }
        
        public MethodViolation(String className, String methodName, String message) {
            this.className = className;
            this.methodName = methodName;
            this.message = message;
        }
        
        public String getClassName() {
            return className;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        @Override
        public String toString() {
            return "MethodViolation{" +
                    "className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    
    /**
     * 参数违规
     */
    public static class ParameterViolation {
        private String className;
        private String methodName;
        private String parameterName;
        private String message;
        
        public ParameterViolation() {
        }
        
        public ParameterViolation(String className, String methodName, String parameterName, String message) {
            this.className = className;
            this.methodName = methodName;
            this.parameterName = parameterName;
            this.message = message;
        }
        
        public String getClassName() {
            return className;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
        
        public String getParameterName() {
            return parameterName;
        }
        
        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        @Override
        public String toString() {
            return "ParameterViolation{" +
                    "className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", parameterName='" + parameterName + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    
    /**
     * Schema 违规
     */
    public static class SchemaViolation {
        private String className;
        private String fieldName;
        private String message;
        private ViolationType type;
        
        public SchemaViolation() {
        }
        
        public SchemaViolation(String className, String fieldName, String message, ViolationType type) {
            this.className = className;
            this.fieldName = fieldName;
            this.message = message;
            this.type = type;
        }
        
        public String getClassName() {
            return className;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public ViolationType getType() {
            return type;
        }
        
        public void setType(ViolationType type) {
            this.type = type;
        }
        
        @Override
        public String toString() {
            return "SchemaViolation{" +
                    "className='" + className + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    ", message='" + message + '\'' +
                    ", type=" + type +
                    '}';
        }
        
        public enum ViolationType {
            CLASS_MISSING_SCHEMA,
            FIELD_MISSING_SCHEMA
        }
    }
    
    /**
     * 验证统计信息
     */
    public static class ValidationStatistics {
        private int controllersWithoutTag;
        private int methodsWithoutOperation;
        private int parametersWithoutAnnotation;
        private int schemasWithoutAnnotation;
        
        public ValidationStatistics() {
        }
        
        public int getControllersWithoutTag() {
            return controllersWithoutTag;
        }
        
        public void setControllersWithoutTag(int controllersWithoutTag) {
            this.controllersWithoutTag = controllersWithoutTag;
        }
        
        public int getMethodsWithoutOperation() {
            return methodsWithoutOperation;
        }
        
        public void setMethodsWithoutOperation(int methodsWithoutOperation) {
            this.methodsWithoutOperation = methodsWithoutOperation;
        }
        
        public int getParametersWithoutAnnotation() {
            return parametersWithoutAnnotation;
        }
        
        public void setParametersWithoutAnnotation(int parametersWithoutAnnotation) {
            this.parametersWithoutAnnotation = parametersWithoutAnnotation;
        }
        
        public int getSchemasWithoutAnnotation() {
            return schemasWithoutAnnotation;
        }
        
        public void setSchemasWithoutAnnotation(int schemasWithoutAnnotation) {
            this.schemasWithoutAnnotation = schemasWithoutAnnotation;
        }
        
        public int getTotalViolations() {
            return controllersWithoutTag + methodsWithoutOperation + 
                   parametersWithoutAnnotation + schemasWithoutAnnotation;
        }
        
        @Override
        public String toString() {
            return "ValidationStatistics{" +
                    "controllersWithoutTag=" + controllersWithoutTag +
                    ", methodsWithoutOperation=" + methodsWithoutOperation +
                    ", parametersWithoutAnnotation=" + parametersWithoutAnnotation +
                    ", schemasWithoutAnnotation=" + schemasWithoutAnnotation +
                    ", totalViolations=" + getTotalViolations() +
                    '}';
        }
    }
}
