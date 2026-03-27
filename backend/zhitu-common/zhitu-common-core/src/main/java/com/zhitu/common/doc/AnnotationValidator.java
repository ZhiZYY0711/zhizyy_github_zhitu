package com.zhitu.common.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 注解验证器
 * 
 * 用于扫描 Controller 类和 DTO 类，检查 Swagger 注解的完整性。
 * 
 * @author Zhitu Platform
 * @since 1.0.0
 */
@Component
public class AnnotationValidator {
    
    private static final String CLASS_PATTERN = "/**/*.class";
    
    /**
     * 验证指定包下的所有 Controller 类
     * 
     * @param packageName 包名
     * @return 验证报告
     */
    public ValidationReport validate(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        
        ValidationReport report = new ValidationReport(packageName);
        
        try {
            // 扫描包下的所有类
            List<Class<?>> classes = scanPackage(packageName);
            
            // 分离 Controller 和 DTO 类
            List<Class<?>> controllers = new ArrayList<>();
            Set<Class<?>> dtos = new HashSet<>();
            
            for (Class<?> clazz : classes) {
                if (isController(clazz)) {
                    controllers.add(clazz);
                    // 收集 Controller 中使用的 DTO 类
                    collectDtosFromController(clazz, dtos);
                }
            }
            
            // 验证 Controller 注解
            for (Class<?> controller : controllers) {
                validateController(controller, report);
            }
            
            // 验证 DTO 注解
            for (Class<?> dto : dtos) {
                validateDto(dto, report);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate annotations in package: " + packageName, e);
        }
        
        return report;
    }
    
    /**
     * 验证指定的 Controller 类列表
     * 
     * @param controllers Controller 类列表
     * @return 验证报告
     */
    public ValidationReport validate(List<Class<?>> controllers) {
        if (controllers == null) {
            throw new IllegalArgumentException("Controllers list cannot be null");
        }
        
        ValidationReport report = new ValidationReport();
        Set<Class<?>> dtos = new HashSet<>();
        
        for (Class<?> controller : controllers) {
            if (!isController(controller)) {
                continue;
            }
            
            validateController(controller, report);
            collectDtosFromController(controller, dtos);
        }
        
        // 验证 DTO 注解
        for (Class<?> dto : dtos) {
            validateDto(dto, report);
        }
        
        return report;
    }
    
    /**
     * 扫描包下的所有类
     */
    private List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                packageName.replace('.', '/') + CLASS_PATTERN;
        
        Resource[] resources = resolver.getResources(pattern);
        
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                try {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
        
        return classes;
    }
    
    /**
     * 判断是否为 Controller 类
     */
    private boolean isController(Class<?> clazz) {
        return clazz.isAnnotationPresent(RestController.class) ||
               clazz.isAnnotationPresent(Controller.class);
    }
    
    /**
     * 验证 Controller 类
     */
    private void validateController(Class<?> controller, ValidationReport report) {
        String className = controller.getName();
        
        // 检查 @Tag 注解
        if (!controller.isAnnotationPresent(Tag.class)) {
            report.addControllerViolation(new ValidationReport.ControllerViolation(
                className,
                "Controller class is missing @Tag annotation"
            ));
        }
        
        // 检查所有公共方法
        for (Method method : controller.getDeclaredMethods()) {
            if (isRequestMappingMethod(method)) {
                validateControllerMethod(controller, method, report);
            }
        }
    }
    
    /**
     * 判断是否为请求映射方法
     */
    private boolean isRequestMappingMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) && (
            method.isAnnotationPresent(RequestMapping.class) ||
            method.isAnnotationPresent(GetMapping.class) ||
            method.isAnnotationPresent(PostMapping.class) ||
            method.isAnnotationPresent(PutMapping.class) ||
            method.isAnnotationPresent(DeleteMapping.class) ||
            method.isAnnotationPresent(PatchMapping.class)
        );
    }
    
    /**
     * 验证 Controller 方法
     */
    private void validateControllerMethod(Class<?> controller, Method method, ValidationReport report) {
        String className = controller.getName();
        String methodName = method.getName();
        
        // 检查 @Operation 注解
        if (!method.isAnnotationPresent(Operation.class)) {
            report.addMethodViolation(new ValidationReport.MethodViolation(
                className,
                methodName,
                "Method is missing @Operation annotation"
            ));
        }
        
        // 检查方法参数的 @Parameter 注解
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        for (java.lang.reflect.Parameter parameter : parameters) {
            validateMethodParameter(controller, method, parameter, report);
        }
    }
    
    /**
     * 验证方法参数
     */
    private void validateMethodParameter(Class<?> controller, Method method, 
                                        java.lang.reflect.Parameter parameter, 
                                        ValidationReport report) {
        String className = controller.getName();
        String methodName = method.getName();
        String parameterName = parameter.getName();
        
        // 检查是否有请求参数注解
        boolean hasRequestAnnotation = 
            parameter.isAnnotationPresent(RequestParam.class) ||
            parameter.isAnnotationPresent(PathVariable.class) ||
            parameter.isAnnotationPresent(RequestHeader.class) ||
            parameter.isAnnotationPresent(RequestBody.class);
        
        // 如果有请求参数注解但没有 @Parameter 注解，则报告违规
        if (hasRequestAnnotation && !parameter.isAnnotationPresent(Parameter.class)) {
            report.addParameterViolation(new ValidationReport.ParameterViolation(
                className,
                methodName,
                parameterName,
                "Parameter is missing @Parameter annotation"
            ));
        }
    }
    
    /**
     * 从 Controller 中收集 DTO 类
     */
    private void collectDtosFromController(Class<?> controller, Set<Class<?>> dtos) {
        for (Method method : controller.getDeclaredMethods()) {
            if (!isRequestMappingMethod(method)) {
                continue;
            }
            
            // 收集参数中的 DTO
            for (java.lang.reflect.Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    Class<?> paramType = parameter.getType();
                    if (isDtoClass(paramType)) {
                        dtos.add(paramType);
                    }
                }
            }
            
            // 收集返回值中的 DTO
            Class<?> returnType = method.getReturnType();
            if (isDtoClass(returnType)) {
                dtos.add(returnType);
            }
        }
    }
    
    /**
     * 判断是否为 DTO 类
     */
    private boolean isDtoClass(Class<?> clazz) {
        // 排除基本类型、包装类、集合类、Spring 类等
        if (clazz.isPrimitive() || 
            clazz.getName().startsWith("java.") ||
            clazz.getName().startsWith("javax.") ||
            clazz.getName().startsWith("org.springframework.") ||
            clazz.isArray() ||
            clazz.isEnum()) {
            return false;
        }
        
        // 检查是否在项目包下
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        String className = clazz.getSimpleName();
        
        // 检查包名或类名是否符合 DTO 模式
        return (packageName.startsWith("com.zhitu") && 
                (packageName.contains(".dto") || 
                 packageName.contains(".entity") ||
                 packageName.contains(".vo"))) ||
               // 支持测试类中的 DTO（类名包含 Dto 或 DTO）
               (className.contains("Dto") || className.contains("DTO"));
    }
    
    /**
     * 验证 DTO 类
     */
    private void validateDto(Class<?> dto, ValidationReport report) {
        String className = dto.getName();
        
        // 检查类级别的 @Schema 注解
        if (!dto.isAnnotationPresent(Schema.class)) {
            report.addSchemaViolation(new ValidationReport.SchemaViolation(
                className,
                null,
                "DTO class is missing @Schema annotation",
                ValidationReport.SchemaViolation.ViolationType.CLASS_MISSING_SCHEMA
            ));
        }
        
        // 检查字段的 @Schema 注解
        for (Field field : dto.getDeclaredFields()) {
            // 跳过静态字段和 transient 字段
            if (Modifier.isStatic(field.getModifiers()) || 
                Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            
            if (!field.isAnnotationPresent(Schema.class)) {
                report.addSchemaViolation(new ValidationReport.SchemaViolation(
                    className,
                    field.getName(),
                    "Field is missing @Schema annotation",
                    ValidationReport.SchemaViolation.ViolationType.FIELD_MISSING_SCHEMA
                ));
            }
        }
    }
}
