package com.zhitu.common.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 变更检测器
 * 
 * 用于比较两个版本的 OpenAPI 规范，识别端点变更、参数变更和数据模型变更。
 * 
 * @author Zhitu Platform
 * @since 1.0.0
 */
@Component
public class ChangeDetector {
    
    /**
     * 检测两个 OpenAPI 规范之间的变更
     * 
     * @param oldSpec 旧版本的 OpenAPI 规范
     * @param newSpec 新版本的 OpenAPI 规范
     * @return 变更报告
     */
    public ChangeReport detectChanges(OpenAPI oldSpec, OpenAPI newSpec) {
        if (oldSpec == null || newSpec == null) {
            throw new IllegalArgumentException("OpenAPI specifications cannot be null");
        }
        
        String oldVersion = oldSpec.getInfo() != null ? oldSpec.getInfo().getVersion() : "unknown";
        String newVersion = newSpec.getInfo() != null ? newSpec.getInfo().getVersion() : "unknown";
        
        ChangeReport report = new ChangeReport(oldVersion, newVersion);
        
        // 比较端点
        List<ChangeReport.EndpointChange> endpointChanges = compareEndpoints(
            oldSpec.getPaths(),
            newSpec.getPaths()
        );
        endpointChanges.forEach(report::addEndpointChange);
        
        // 比较数据模型
        List<ChangeReport.SchemaChange> schemaChanges = compareSchemas(
            oldSpec.getComponents(),
            newSpec.getComponents()
        );
        schemaChanges.forEach(report::addSchemaChange);
        
        return report;
    }
    
    /**
     * 比较端点变更
     * 
     * @param oldPaths 旧版本的路径集合
     * @param newPaths 新版本的路径集合
     * @return 端点变更列表
     */
    public List<ChangeReport.EndpointChange> compareEndpoints(Paths oldPaths, Paths newPaths) {
        List<ChangeReport.EndpointChange> changes = new ArrayList<>();
        
        if (oldPaths == null && newPaths == null) {
            return changes;
        }
        
        Map<String, PathItem> oldPathMap = oldPaths != null ? oldPaths : new HashMap<>();
        Map<String, PathItem> newPathMap = newPaths != null ? newPaths : new HashMap<>();
        
        Set<String> allPaths = new HashSet<>();
        allPaths.addAll(oldPathMap.keySet());
        allPaths.addAll(newPathMap.keySet());
        
        for (String path : allPaths) {
            PathItem oldPathItem = oldPathMap.get(path);
            PathItem newPathItem = newPathMap.get(path);
            
            if (oldPathItem == null) {
                // 新增的路径
                changes.addAll(detectAddedPath(path, newPathItem));
            } else if (newPathItem == null) {
                // 删除的路径
                changes.addAll(detectRemovedPath(path, oldPathItem));
            } else {
                // 修改的路径
                changes.addAll(detectModifiedPath(path, oldPathItem, newPathItem));
            }
        }
        
        return changes;
    }
    
    /**
     * 比较数据模型变更
     * 
     * @param oldComponents 旧版本的组件
     * @param newComponents 新版本的组件
     * @return 数据模型变更列表
     */
    public List<ChangeReport.SchemaChange> compareSchemas(Components oldComponents, Components newComponents) {
        List<ChangeReport.SchemaChange> changes = new ArrayList<>();
        
        Map<String, Schema> oldSchemas = oldComponents != null && oldComponents.getSchemas() != null 
            ? oldComponents.getSchemas() 
            : new HashMap<>();
        Map<String, Schema> newSchemas = newComponents != null && newComponents.getSchemas() != null 
            ? newComponents.getSchemas() 
            : new HashMap<>();
        
        Set<String> allSchemaNames = new HashSet<>();
        allSchemaNames.addAll(oldSchemas.keySet());
        allSchemaNames.addAll(newSchemas.keySet());
        
        for (String schemaName : allSchemaNames) {
            Schema oldSchema = oldSchemas.get(schemaName);
            Schema newSchema = newSchemas.get(schemaName);
            
            if (oldSchema == null) {
                // 新增的数据模型
                ChangeReport.SchemaChange change = new ChangeReport.SchemaChange(
                    ChangeReport.ChangeType.ADDED,
                    schemaName
                );
                change.setDescription("New schema added");
                changes.add(change);
            } else if (newSchema == null) {
                // 删除的数据模型
                ChangeReport.SchemaChange change = new ChangeReport.SchemaChange(
                    ChangeReport.ChangeType.REMOVED,
                    schemaName
                );
                change.setDescription("Schema removed");
                changes.add(change);
            } else {
                // 修改的数据模型
                ChangeReport.SchemaChange change = detectSchemaChanges(schemaName, oldSchema, newSchema);
                if (change != null && !change.getPropertyChanges().isEmpty()) {
                    changes.add(change);
                }
            }
        }
        
        return changes;
    }
    
    /**
     * 检测新增的路径
     */
    private List<ChangeReport.EndpointChange> detectAddedPath(String path, PathItem pathItem) {
        List<ChangeReport.EndpointChange> changes = new ArrayList<>();
        
        Map<String, Operation> operations = getOperations(pathItem);
        for (Map.Entry<String, Operation> entry : operations.entrySet()) {
            ChangeReport.EndpointChange change = new ChangeReport.EndpointChange(
                ChangeReport.ChangeType.ADDED,
                path,
                entry.getKey().toUpperCase()
            );
            
            Operation operation = entry.getValue();
            if (operation != null && operation.getSummary() != null) {
                change.setDescription("New endpoint: " + operation.getSummary());
            } else {
                change.setDescription("New endpoint added");
            }
            
            changes.add(change);
        }
        
        return changes;
    }
    
    /**
     * 检测删除的路径
     */
    private List<ChangeReport.EndpointChange> detectRemovedPath(String path, PathItem pathItem) {
        List<ChangeReport.EndpointChange> changes = new ArrayList<>();
        
        Map<String, Operation> operations = getOperations(pathItem);
        for (Map.Entry<String, Operation> entry : operations.entrySet()) {
            ChangeReport.EndpointChange change = new ChangeReport.EndpointChange(
                ChangeReport.ChangeType.REMOVED,
                path,
                entry.getKey().toUpperCase()
            );
            
            Operation operation = entry.getValue();
            if (operation != null && operation.getSummary() != null) {
                change.setDescription("Removed endpoint: " + operation.getSummary());
            } else {
                change.setDescription("Endpoint removed");
            }
            
            changes.add(change);
        }
        
        return changes;
    }
    
    /**
     * 检测修改的路径
     */
    private List<ChangeReport.EndpointChange> detectModifiedPath(String path, PathItem oldPathItem, PathItem newPathItem) {
        List<ChangeReport.EndpointChange> changes = new ArrayList<>();
        
        Map<String, Operation> oldOperations = getOperations(oldPathItem);
        Map<String, Operation> newOperations = getOperations(newPathItem);
        
        Set<String> allMethods = new HashSet<>();
        allMethods.addAll(oldOperations.keySet());
        allMethods.addAll(newOperations.keySet());
        
        for (String method : allMethods) {
            Operation oldOperation = oldOperations.get(method);
            Operation newOperation = newOperations.get(method);
            
            if (oldOperation == null) {
                // 新增的方法
                ChangeReport.EndpointChange change = new ChangeReport.EndpointChange(
                    ChangeReport.ChangeType.ADDED,
                    path,
                    method.toUpperCase()
                );
                change.setDescription("New method added to existing path");
                changes.add(change);
            } else if (newOperation == null) {
                // 删除的方法
                ChangeReport.EndpointChange change = new ChangeReport.EndpointChange(
                    ChangeReport.ChangeType.REMOVED,
                    path,
                    method.toUpperCase()
                );
                change.setDescription("Method removed from path");
                changes.add(change);
            } else {
                // 比较操作的变更
                ChangeReport.EndpointChange change = detectOperationChanges(
                    path,
                    method.toUpperCase(),
                    oldOperation,
                    newOperation
                );
                if (change != null) {
                    changes.add(change);
                }
            }
        }
        
        return changes;
    }
    
    /**
     * 检测操作的变更
     */
    private ChangeReport.EndpointChange detectOperationChanges(
            String path,
            String method,
            Operation oldOperation,
            Operation newOperation) {
        
        List<ChangeReport.ParameterChange> parameterChanges = compareParameters(
            oldOperation.getParameters(),
            newOperation.getParameters()
        );
        
        ChangeReport.ResponseChange responseChange = compareResponses(
            oldOperation.getResponses(),
            newOperation.getResponses()
        );
        
        // 只有在有实际变更时才创建 EndpointChange
        if (!parameterChanges.isEmpty() || responseChange != null) {
            ChangeReport.EndpointChange change = new ChangeReport.EndpointChange(
                ChangeReport.ChangeType.MODIFIED,
                path,
                method
            );
            change.setDescription("Endpoint modified");
            change.setParameterChanges(parameterChanges);
            change.setResponseChange(responseChange);
            return change;
        }
        
        return null;
    }
    
    /**
     * 比较参数
     */
    private List<ChangeReport.ParameterChange> compareParameters(
            List<Parameter> oldParams,
            List<Parameter> newParams) {
        
        List<ChangeReport.ParameterChange> changes = new ArrayList<>();
        
        Map<String, Parameter> oldParamMap = new HashMap<>();
        if (oldParams != null) {
            for (Parameter param : oldParams) {
                oldParamMap.put(param.getName(), param);
            }
        }
        
        Map<String, Parameter> newParamMap = new HashMap<>();
        if (newParams != null) {
            for (Parameter param : newParams) {
                newParamMap.put(param.getName(), param);
            }
        }
        
        Set<String> allParamNames = new HashSet<>();
        allParamNames.addAll(oldParamMap.keySet());
        allParamNames.addAll(newParamMap.keySet());
        
        for (String paramName : allParamNames) {
            Parameter oldParam = oldParamMap.get(paramName);
            Parameter newParam = newParamMap.get(paramName);
            
            if (oldParam == null) {
                // 新增参数
                ChangeReport.ParameterChange change = new ChangeReport.ParameterChange(
                    ChangeReport.ChangeType.ADDED,
                    paramName,
                    newParam.getIn()
                );
                change.setNewType(getParameterType(newParam));
                change.setRequired(newParam.getRequired());
                change.setDescription("New parameter added");
                changes.add(change);
            } else if (newParam == null) {
                // 删除参数
                ChangeReport.ParameterChange change = new ChangeReport.ParameterChange(
                    ChangeReport.ChangeType.REMOVED,
                    paramName,
                    oldParam.getIn()
                );
                change.setOldType(getParameterType(oldParam));
                change.setDescription("Parameter removed");
                changes.add(change);
            } else {
                // 检查参数是否有变更
                String oldType = getParameterType(oldParam);
                String newType = getParameterType(newParam);
                boolean typeChanged = !oldType.equals(newType);
                boolean requiredChanged = !oldParam.getRequired().equals(newParam.getRequired());
                
                if (typeChanged || requiredChanged) {
                    ChangeReport.ParameterChange change = new ChangeReport.ParameterChange(
                        ChangeReport.ChangeType.MODIFIED,
                        paramName,
                        newParam.getIn()
                    );
                    change.setOldType(oldType);
                    change.setNewType(newType);
                    change.setRequired(newParam.getRequired());
                    
                    if (typeChanged && requiredChanged) {
                        change.setDescription("Parameter type and required status changed");
                    } else if (typeChanged) {
                        change.setDescription("Parameter type changed");
                    } else {
                        change.setDescription("Parameter required status changed");
                    }
                    
                    changes.add(change);
                }
            }
        }
        
        return changes;
    }
    
    /**
     * 比较响应
     */
    private ChangeReport.ResponseChange compareResponses(
            ApiResponses oldResponses,
            ApiResponses newResponses) {
        
        // 简化实现：只比较 200 响应
        if (oldResponses == null || newResponses == null) {
            return null;
        }
        
        ApiResponse oldResponse = oldResponses.get("200");
        ApiResponse newResponse = newResponses.get("200");
        
        if (oldResponse == null && newResponse == null) {
            return null;
        }
        
        String oldType = getResponseType(oldResponse);
        String newType = getResponseType(newResponse);
        
        if (!oldType.equals(newType)) {
            ChangeReport.ResponseChange change = new ChangeReport.ResponseChange(
                ChangeReport.ChangeType.MODIFIED,
                "200"
            );
            change.setOldResponseType(oldType);
            change.setNewResponseType(newType);
            change.setDescription("Response type changed");
            return change;
        }
        
        return null;
    }
    
    /**
     * 检测数据模型的变更
     */
    private ChangeReport.SchemaChange detectSchemaChanges(
            String schemaName,
            Schema oldSchema,
            Schema newSchema) {
        
        Map<String, Schema> oldProperties = oldSchema.getProperties() != null 
            ? oldSchema.getProperties() 
            : new HashMap<>();
        Map<String, Schema> newProperties = newSchema.getProperties() != null 
            ? newSchema.getProperties() 
            : new HashMap<>();
        
        Set<String> allPropertyNames = new HashSet<>();
        allPropertyNames.addAll(oldProperties.keySet());
        allPropertyNames.addAll(newProperties.keySet());
        
        ChangeReport.SchemaChange schemaChange = new ChangeReport.SchemaChange(
            ChangeReport.ChangeType.MODIFIED,
            schemaName
        );
        
        for (String propertyName : allPropertyNames) {
            Schema oldProperty = oldProperties.get(propertyName);
            Schema newProperty = newProperties.get(propertyName);
            
            if (oldProperty == null) {
                // 新增属性
                ChangeReport.PropertyChange change = new ChangeReport.PropertyChange(
                    ChangeReport.ChangeType.ADDED,
                    propertyName
                );
                change.setNewType(getSchemaType(newProperty));
                change.setDescription("New property added");
                schemaChange.addPropertyChange(change);
            } else if (newProperty == null) {
                // 删除属性
                ChangeReport.PropertyChange change = new ChangeReport.PropertyChange(
                    ChangeReport.ChangeType.REMOVED,
                    propertyName
                );
                change.setOldType(getSchemaType(oldProperty));
                change.setDescription("Property removed");
                schemaChange.addPropertyChange(change);
            } else {
                // 检查属性类型是否变更
                String oldType = getSchemaType(oldProperty);
                String newType = getSchemaType(newProperty);
                
                if (!oldType.equals(newType)) {
                    ChangeReport.PropertyChange change = new ChangeReport.PropertyChange(
                        ChangeReport.ChangeType.MODIFIED,
                        propertyName
                    );
                    change.setOldType(oldType);
                    change.setNewType(newType);
                    change.setDescription("Property type changed");
                    schemaChange.addPropertyChange(change);
                }
            }
        }
        
        if (!schemaChange.getPropertyChanges().isEmpty()) {
            schemaChange.setDescription("Schema properties modified");
            return schemaChange;
        }
        
        return null;
    }
    
    /**
     * 获取路径项的所有操作
     */
    private Map<String, Operation> getOperations(PathItem pathItem) {
        Map<String, Operation> operations = new HashMap<>();
        
        if (pathItem.getGet() != null) {
            operations.put("get", pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            operations.put("post", pathItem.getPost());
        }
        if (pathItem.getPut() != null) {
            operations.put("put", pathItem.getPut());
        }
        if (pathItem.getDelete() != null) {
            operations.put("delete", pathItem.getDelete());
        }
        if (pathItem.getPatch() != null) {
            operations.put("patch", pathItem.getPatch());
        }
        if (pathItem.getOptions() != null) {
            operations.put("options", pathItem.getOptions());
        }
        if (pathItem.getHead() != null) {
            operations.put("head", pathItem.getHead());
        }
        
        return operations;
    }
    
    /**
     * 获取参数类型
     */
    private String getParameterType(Parameter parameter) {
        if (parameter == null || parameter.getSchema() == null) {
            return "unknown";
        }
        return getSchemaType(parameter.getSchema());
    }
    
    /**
     * 获取响应类型
     */
    private String getResponseType(ApiResponse response) {
        if (response == null || response.getContent() == null) {
            return "unknown";
        }
        
        if (response.getContent().get("application/json") != null) {
            var mediaType = response.getContent().get("application/json");
            if (mediaType.getSchema() != null) {
                return getSchemaType(mediaType.getSchema());
            }
        }
        
        return "unknown";
    }
    
    /**
     * 获取数据模型类型
     */
    private String getSchemaType(Schema schema) {
        if (schema == null) {
            return "unknown";
        }
        
        if (schema.get$ref() != null) {
            // 提取引用的模型名称
            String ref = schema.get$ref();
            int lastSlash = ref.lastIndexOf('/');
            return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
        }
        
        if (schema.getType() != null) {
            return schema.getType();
        }
        
        return "unknown";
    }
}
