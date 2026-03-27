package com.zhitu.common.doc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChangeDetector 单元测试
 */
class ChangeDetectorTest {
    
    private ChangeDetector changeDetector;
    
    @BeforeEach
    void setUp() {
        changeDetector = new ChangeDetector();
    }
    
    @Test
    void testDetectChanges_withNullSpecs_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            changeDetector.detectChanges(null, new OpenAPI());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            changeDetector.detectChanges(new OpenAPI(), null);
        });
    }
    
    @Test
    void testDetectChanges_withNoChanges_shouldReturnEmptyReport() {
        OpenAPI spec = createBasicOpenAPI("1.0.0");
        
        ChangeReport report = changeDetector.detectChanges(spec, spec);
        
        assertNotNull(report);
        assertEquals("1.0.0", report.getOldVersion());
        assertEquals("1.0.0", report.getNewVersion());
        assertTrue(report.getEndpointChanges().isEmpty());
        assertTrue(report.getSchemaChanges().isEmpty());
    }
    
    @Test
    void testCompareEndpoints_detectAddedEndpoint() {
        Paths oldPaths = new Paths();
        Paths newPaths = new Paths();
        
        // 添加新端点
        PathItem newPathItem = new PathItem();
        Operation getOperation = new Operation();
        getOperation.setSummary("Get user");
        newPathItem.setGet(getOperation);
        newPaths.addPathItem("/api/users", newPathItem);
        
        List<ChangeReport.EndpointChange> changes = changeDetector.compareEndpoints(oldPaths, newPaths);
        
        assertEquals(1, changes.size());
        ChangeReport.EndpointChange change = changes.get(0);
        assertEquals(ChangeReport.ChangeType.ADDED, change.getType());
        assertEquals("/api/users", change.getPath());
        assertEquals("GET", change.getMethod());
        assertTrue(change.getDescription().contains("Get user"));
    }
    
    @Test
    void testCompareEndpoints_detectRemovedEndpoint() {
        Paths oldPaths = new Paths();
        Paths newPaths = new Paths();
        
        // 删除端点
        PathItem oldPathItem = new PathItem();
        Operation deleteOperation = new Operation();
        deleteOperation.setSummary("Delete user");
        oldPathItem.setDelete(deleteOperation);
        oldPaths.addPathItem("/api/users/{id}", oldPathItem);
        
        List<ChangeReport.EndpointChange> changes = changeDetector.compareEndpoints(oldPaths, newPaths);
        
        assertEquals(1, changes.size());
        ChangeReport.EndpointChange change = changes.get(0);
        assertEquals(ChangeReport.ChangeType.REMOVED, change.getType());
        assertEquals("/api/users/{id}", change.getPath());
        assertEquals("DELETE", change.getMethod());
    }
    
    @Test
    void testCompareEndpoints_detectModifiedEndpoint_parameterAdded() {
        Paths oldPaths = new Paths();
        Paths newPaths = new Paths();
        
        // 旧端点
        PathItem oldPathItem = new PathItem();
        Operation oldOperation = new Operation();
        oldOperation.setSummary("Get users");
        oldPathItem.setGet(oldOperation);
        oldPaths.addPathItem("/api/users", oldPathItem);
        
        // 新端点（添加参数）
        PathItem newPathItem = new PathItem();
        Operation newOperation = new Operation();
        newOperation.setSummary("Get users");
        
        Parameter pageParam = new QueryParameter();
        pageParam.setName("page");
        pageParam.setRequired(false);
        pageParam.setSchema(new IntegerSchema());
        newOperation.addParametersItem(pageParam);
        
        newPathItem.setGet(newOperation);
        newPaths.addPathItem("/api/users", newPathItem);
        
        List<ChangeReport.EndpointChange> changes = changeDetector.compareEndpoints(oldPaths, newPaths);
        
        assertEquals(1, changes.size());
        ChangeReport.EndpointChange change = changes.get(0);
        assertEquals(ChangeReport.ChangeType.MODIFIED, change.getType());
        assertEquals("/api/users", change.getPath());
        assertEquals("GET", change.getMethod());
        assertEquals(1, change.getParameterChanges().size());
        
        ChangeReport.ParameterChange paramChange = change.getParameterChanges().get(0);
        assertEquals(ChangeReport.ChangeType.ADDED, paramChange.getType());
        assertEquals("page", paramChange.getName());
        assertEquals("query", paramChange.getLocation());
    }
    
    @Test
    void testCompareEndpoints_detectModifiedEndpoint_parameterTypeChanged() {
        Paths oldPaths = new Paths();
        Paths newPaths = new Paths();
        
        // 旧端点
        PathItem oldPathItem = new PathItem();
        Operation oldOperation = new Operation();
        Parameter oldParam = new QueryParameter();
        oldParam.setName("id");
        oldParam.setRequired(true);
        oldParam.setSchema(new StringSchema());
        oldOperation.addParametersItem(oldParam);
        oldPathItem.setGet(oldOperation);
        oldPaths.addPathItem("/api/users", oldPathItem);
        
        // 新端点（参数类型改变）
        PathItem newPathItem = new PathItem();
        Operation newOperation = new Operation();
        Parameter newParam = new QueryParameter();
        newParam.setName("id");
        newParam.setRequired(true);
        newParam.setSchema(new IntegerSchema());
        newOperation.addParametersItem(newParam);
        newPathItem.setGet(newOperation);
        newPaths.addPathItem("/api/users", newPathItem);
        
        List<ChangeReport.EndpointChange> changes = changeDetector.compareEndpoints(oldPaths, newPaths);
        
        assertEquals(1, changes.size());
        ChangeReport.EndpointChange change = changes.get(0);
        assertEquals(1, change.getParameterChanges().size());
        
        ChangeReport.ParameterChange paramChange = change.getParameterChanges().get(0);
        assertEquals(ChangeReport.ChangeType.MODIFIED, paramChange.getType());
        assertEquals("id", paramChange.getName());
        assertEquals("string", paramChange.getOldType());
        assertEquals("integer", paramChange.getNewType());
    }
    
    @Test
    void testCompareSchemas_detectAddedSchema() {
        Components oldComponents = new Components();
        Components newComponents = new Components();
        
        // 添加新数据模型
        Schema userSchema = new Schema();
        userSchema.setType("object");
        Map<String, Schema> properties = new HashMap<>();
        properties.put("id", new IntegerSchema());
        properties.put("name", new StringSchema());
        userSchema.setProperties(properties);
        
        newComponents.addSchemas("User", userSchema);
        
        List<ChangeReport.SchemaChange> changes = changeDetector.compareSchemas(oldComponents, newComponents);
        
        assertEquals(1, changes.size());
        ChangeReport.SchemaChange change = changes.get(0);
        assertEquals(ChangeReport.ChangeType.ADDED, change.getType());
        assertEquals("User", change.getSchemaName());
    }
    
    @Test
    void testCompareSchemas_detectRemovedSchema() {
        Components oldComponents = new Components();
        Components newComponents = new Components();
        
        // 删除数据模型
        Schema userSchema = new Schema();
        userSchema.setType("object");
        oldComponents.addSchemas("User", userSchema);
        
        List<ChangeReport.SchemaChange> changes = changeDetector.compareSchemas(oldComponents, newComponents);
        
        assertEquals(1, changes.size());
        ChangeReport.SchemaChange change = changes.get(0);
        assertEquals(ChangeReport.ChangeType.REMOVED, change.getType());
        assertEquals("User", change.getSchemaName());
    }
    
    @Test
    void testCompareSchemas_detectModifiedSchema_propertyAdded() {
        Components oldComponents = new Components();
        Components newComponents = new Components();
        
        // 旧数据模型
        Schema oldSchema = new Schema();
        oldSchema.setType("object");
        Map<String, Schema> oldProperties = new HashMap<>();
        oldProperties.put("id", new IntegerSchema());
        oldSchema.setProperties(oldProperties);
        oldComponents.addSchemas("User", oldSchema);
        
        // 新数据模型（添加属性）
        Schema newSchema = new Schema();
        newSchema.setType("object");
        Map<String, Schema> newProperties = new HashMap<>();
        newProperties.put("id", new IntegerSchema());
        newProperties.put("email", new StringSchema());
        newSchema.setProperties(newProperties);
        newComponents.addSchemas("User", newSchema);
        
        List<ChangeReport.SchemaChange> changes = changeDetector.compareSchemas(oldComponents, newComponents);
        
        assertEquals(1, changes.size());
        ChangeReport.SchemaChange change = changes.get(0);
        assertEquals(ChangeReport.ChangeType.MODIFIED, change.getType());
        assertEquals("User", change.getSchemaName());
        assertEquals(1, change.getPropertyChanges().size());
        
        ChangeReport.PropertyChange propChange = change.getPropertyChanges().get(0);
        assertEquals(ChangeReport.ChangeType.ADDED, propChange.getType());
        assertEquals("email", propChange.getName());
    }
    
    @Test
    void testCompareSchemas_detectModifiedSchema_propertyTypeChanged() {
        Components oldComponents = new Components();
        Components newComponents = new Components();
        
        // 旧数据模型
        Schema oldSchema = new Schema();
        oldSchema.setType("object");
        Map<String, Schema> oldProperties = new HashMap<>();
        oldProperties.put("age", new StringSchema());
        oldSchema.setProperties(oldProperties);
        oldComponents.addSchemas("User", oldSchema);
        
        // 新数据模型（属性类型改变）
        Schema newSchema = new Schema();
        newSchema.setType("object");
        Map<String, Schema> newProperties = new HashMap<>();
        newProperties.put("age", new IntegerSchema());
        newSchema.setProperties(newProperties);
        newComponents.addSchemas("User", newSchema);
        
        List<ChangeReport.SchemaChange> changes = changeDetector.compareSchemas(oldComponents, newComponents);
        
        assertEquals(1, changes.size());
        ChangeReport.SchemaChange change = changes.get(0);
        assertEquals(1, change.getPropertyChanges().size());
        
        ChangeReport.PropertyChange propChange = change.getPropertyChanges().get(0);
        assertEquals(ChangeReport.ChangeType.MODIFIED, propChange.getType());
        assertEquals("age", propChange.getName());
        assertEquals("string", propChange.getOldType());
        assertEquals("integer", propChange.getNewType());
    }
    
    @Test
    void testDetectChanges_fullIntegration() {
        OpenAPI oldSpec = createBasicOpenAPI("1.0.0");
        OpenAPI newSpec = createBasicOpenAPI("1.1.0");
        
        // 添加新端点到新规范
        Paths newPaths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setSummary("Create user");
        pathItem.setPost(operation);
        newPaths.addPathItem("/api/users", pathItem);
        newSpec.setPaths(newPaths);
        
        // 添加新数据模型到新规范
        Components newComponents = new Components();
        Schema userSchema = new Schema();
        userSchema.setType("object");
        newComponents.addSchemas("User", userSchema);
        newSpec.setComponents(newComponents);
        
        ChangeReport report = changeDetector.detectChanges(oldSpec, newSpec);
        
        assertNotNull(report);
        assertEquals("1.0.0", report.getOldVersion());
        assertEquals("1.1.0", report.getNewVersion());
        assertEquals(1, report.getEndpointChanges().size());
        assertEquals(1, report.getSchemaChanges().size());
        assertEquals(1, report.getStatistics().getAddedEndpoints());
        assertEquals(1, report.getStatistics().getAddedSchemas());
        assertTrue(report.getStatistics().hasChanges());
    }
    
    @Test
    void testCompareEndpoints_withNullPaths_shouldReturnEmptyList() {
        List<ChangeReport.EndpointChange> changes = changeDetector.compareEndpoints(null, null);
        assertNotNull(changes);
        assertTrue(changes.isEmpty());
    }
    
    @Test
    void testCompareSchemas_withNullComponents_shouldReturnEmptyList() {
        List<ChangeReport.SchemaChange> changes = changeDetector.compareSchemas(null, null);
        assertNotNull(changes);
        assertTrue(changes.isEmpty());
    }
    
    /**
     * 创建基本的 OpenAPI 规范
     */
    private OpenAPI createBasicOpenAPI(String version) {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setTitle("Test API");
        info.setVersion(version);
        openAPI.setInfo(info);
        return openAPI;
    }
}
