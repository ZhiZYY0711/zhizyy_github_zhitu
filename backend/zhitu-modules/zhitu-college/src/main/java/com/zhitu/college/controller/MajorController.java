package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.entity.Organization;
import com.zhitu.college.mapper.OrganizationMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Major Management Controller
 * Handles major (专业) information management endpoints
 */
@Tag(name = "专业管理", description = "专业信息管理相关接口")
@RestController
@RequestMapping("/api/major/v1")
@RequiredArgsConstructor
public class MajorController {

    private final OrganizationMapper organizationMapper;

    /**
     * Get organization list with filtering and pagination
     */
    @Operation(
        summary = "获取组织列表",
        description = "分页查询组织列表（学院/专业/班级），支持按类型、父组织筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要高校管理员或教师权限\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 college_organization 表\n" +
            "- 无需其他数据预先存在，空列表也是有效结果"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/organizations")
    public Result<IPage<Organization>> getOrganizations(
            @Parameter(description = "组织类型：1-学院，2-专业，3-班级", example = "2")
            @RequestParam(value = "orgType", required = false) Integer orgType,
            @Parameter(description = "父组织ID", example = "100")
            @RequestParam(value = "parentId", required = false) Long parentId,
            @Parameter(description = "组织名称，支持模糊查询", example = "计算机")
            @RequestParam(value = "orgName", required = false) String orgName,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        if (orgType != null) {
            wrapper.eq(Organization::getOrgType, orgType);
        }
        if (parentId != null) {
            wrapper.eq(Organization::getParentId, parentId);
        }
        if (orgName != null && !orgName.isBlank()) {
            wrapper.like(Organization::getOrgName, orgName);
        }
        wrapper.orderByAsc(Organization::getSortOrder);
        
        IPage<Organization> organizations = organizationMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.ok(organizations);
    }

    /**
     * Get organization by ID
     */
    @Operation(
        summary = "获取组织详情",
        description = "根据ID获取组织详细信息\n\n" +
            "**测试前置条件：**\n" +
            "- 组织ID必须存在于 college_organization 表中\n" +
            "- 需要相应权限访问\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college_organization 表中的组织记录"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "组织不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"组织不存在\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/organizations/{id}")
    public Result<Organization> getOrganization(
            @Parameter(description = "组织ID", required = true, example = "1")
            @PathVariable(value = "id") Long id) {
        
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            return Result.fail(404, "组织不存在");
        }
        return Result.ok(organization);
    }

    /**
     * Get organization tree
     */
    @Operation(
        summary = "获取组织树",
        description = "获取完整的组织树结构（学院-专业-班级）\n\n" +
            "**测试前置条件：**\n" +
            "- 需要高校管理员或教师权限\n" +
            "- 高校ID必须存在于 college 表中\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college_organization 表中的组织记录\n" +
            "- 组织之间通过 parent_id 建立父子关系"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/organizations/tree")
    public Result<List<Organization>> getOrganizationTree(
            @Parameter(description = "根组织ID，不传则从顶层开始", example = "1")
            @RequestParam(value = "rootId", required = false) Long rootId) {
        
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        if (rootId != null) {
            wrapper.eq(Organization::getParentId, rootId);
        } else {
            wrapper.isNull(Organization::getParentId).or().eq(Organization::getParentId, 0);
        }
        wrapper.orderByAsc(Organization::getSortOrder);
        
        List<Organization> organizations = organizationMapper.selectList(wrapper);
        return Result.ok(organizations);
    }

    /**
     * Create a new organization
     */
    @Operation(
        summary = "创建组织",
        description = "创建新的组织（学院/专业/班级）\n\n" +
            "**测试前置条件：**\n" +
            "- 高校ID（collegeId）必须存在于 college 表中\n" +
            "- 如果是子组织，父组织ID（parentId）必须存在\n" +
            "- 需要高校管理员权限\n" +
            "- 组织名称在同一父组织下不能重复\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college 表中的高校记录\n" +
            "- 如果 parentId 不为 null，依赖 college_organization 表中的父组织记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "组织信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Organization.class),
                examples = {
                    @ExampleObject(
                        name = "创建学院",
                        summary = "创建一级学院组织",
                        value = "{\"parentId\": 0, \"orgType\": 1, \"orgName\": \"计算机学院\", \"orgCode\": \"CS001\", \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建专业",
                        summary = "在学院下创建专业",
                        value = "{\"parentId\": 100, \"orgType\": 2, \"orgName\": \"软件工程\", \"orgCode\": \"SE001\", \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建班级",
                        summary = "在专业下创建班级",
                        value = "{\"parentId\": 200, \"orgType\": 3, \"orgName\": \"软件工程2021级1班\", \"orgCode\": \"SE2021-1\", \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-根组织",
                        summary = "父ID为0的根组织",
                        value = "{\"parentId\": 0, \"orgType\": 1, \"orgName\": \"根学院\", \"orgCode\": \"ROOT001\", \"sortOrder\": 0}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大排序值",
                        summary = "排序值为999",
                        value = "{\"parentId\": 100, \"orgType\": 2, \"orgName\": \"最后专业\", \"orgCode\": \"LAST999\", \"sortOrder\": 999}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-长名称",
                        summary = "包含很长名称的组织",
                        value = "{\"parentId\": 100, \"orgType\": 2, \"orgName\": \"计算机科学与技术（人工智能与大数据方向）（国际合作办学）\", \"orgCode\": \"CS-AI-BD-INT\", \"sortOrder\": 5}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号编码",
                        summary = "包含特殊符号的组织编码",
                        value = "{\"parentId\": 100, \"orgType\": 2, \"orgName\": \"软件工程（专升本）\", \"orgCode\": \"SE_UG_2+2\", \"sortOrder\": 10}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中英文混合",
                        summary = "中英文混合的组织名称",
                        value = "{\"parentId\": 100, \"orgType\": 2, \"orgName\": \"Software Engineering软件工程\", \"orgCode\": \"SE-BILINGUAL\", \"sortOrder\": 15}"
                    ),
                    @ExampleObject(
                        name = "边界值-深层嵌套",
                        summary = "多层嵌套的组织结构",
                        value = "{\"parentId\": 999, \"orgType\": 3, \"orgName\": \"深层班级\", \"orgCode\": \"DEEP-CLASS-001\", \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-年份和批次",
                        summary = "包含年份和批次信息",
                        value = "{\"parentId\": 200, \"orgType\": 3, \"orgName\": \"2024级软件工程1班（春季）\", \"orgCode\": \"SE2024-S1\", \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小类型",
                        summary = "组织类型为1（学院）",
                        value = "{\"parentId\": 0, \"orgType\": 1, \"orgName\": \"新学院\", \"orgCode\": \"NEW001\", \"sortOrder\": 100}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大类型",
                        summary = "组织类型为3（班级）",
                        value = "{\"parentId\": 200, \"orgType\": 3, \"orgName\": \"测试班级\", \"orgCode\": \"TEST-CLASS\", \"sortOrder\": 50}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/organizations")
    public Result<Long> createOrganization(
            @Parameter(description = "组织信息", required = true)
            @Valid @RequestBody Organization organization) {
        
        Long tenantId = UserContext.getTenantId();
        organization.setTenantId(tenantId);
        organizationMapper.insert(organization);
        return Result.ok(organization.getId());
    }

    /**
     * Update organization information
     */
    @Operation(
        summary = "更新组织信息",
        description = "更新指定组织的信息\n\n" +
            "**测试前置条件：**\n" +
            "- 组织ID必须存在于 college_organization 表中\n" +
            "- 需要高校管理员权限\n" +
            "- 如果更新父组织ID，新的父组织必须存在\n" +
            "- 不能将组织的父组织设置为自己或自己的子组织（避免循环引用）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college_organization 表中的现有组织记录\n" +
            "- 如果更新 parentId，依赖新的父组织记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "组织信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Organization.class),
                examples = {
                    @ExampleObject(
                        name = "更新组织名称",
                        summary = "更新组织名称",
                        value = "{\"orgName\": \"计算机与软件学院\"}"
                    ),
                    @ExampleObject(
                        name = "更新排序",
                        summary = "调整组织排序",
                        value = "{\"sortOrder\": 5}"
                    ),
                    @ExampleObject(
                        name = "完整更新",
                        summary = "更新组织完整信息",
                        value = "{\"parentId\": 0, \"orgType\": 1, \"orgName\": \"计算机与软件学院\", \"orgCode\": \"CS001\", \"sortOrder\": 1}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "组织不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"组织不存在\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PutMapping("/organizations/{id}")
    public Result<Void> updateOrganization(
            @Parameter(description = "组织ID", required = true, example = "1")
            @PathVariable(value = "id") Long id,
            @Parameter(description = "组织信息", required = true)
            @Valid @RequestBody Organization organization) {
        
        Organization existing = organizationMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "组织不存在");
        }
        
        organization.setId(id);
        organizationMapper.updateById(organization);
        return Result.ok();
    }

    /**
     * Delete organization
     */
    @Operation(
        summary = "删除组织",
        description = "删除指定的组织（软删除）\n\n" +
            "**测试前置条件：**\n" +
            "- 组织ID必须存在于 college_organization 表中\n" +
            "- 需要高校管理员权限\n" +
            "- 组织下不能有子组织\n" +
            "- 组织下不能有关联的学生\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college_organization 表中的组织记录\n" +
            "- 删除前应检查是否有子组织和学生关联"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "删除成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "组织不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"组织不存在\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @DeleteMapping("/organizations/{id}")
    public Result<Void> deleteOrganization(
            @Parameter(description = "组织ID", required = true, example = "1")
            @PathVariable(value = "id") Long id) {
        
        Organization existing = organizationMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "组织不存在");
        }
        
        organizationMapper.deleteById(id);
        return Result.ok();
    }

    /**
     * Get children organizations
     */
    @Operation(
        summary = "获取子组织列表",
        description = "获取指定组织的所有直接子组织\n\n" +
            "**测试前置条件：**\n" +
            "- 父组织ID必须存在于 college_organization 表中\n" +
            "- 需要相应权限访问\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college_organization 表中的父组织记录\n" +
            "- 查询 parent_id 等于指定ID的所有组织"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/organizations/{id}/children")
    public Result<List<Organization>> getChildren(
            @Parameter(description = "父组织ID", required = true, example = "1")
            @PathVariable(value = "id") Long id) {
        
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getParentId, id);
        wrapper.orderByAsc(Organization::getSortOrder);
        
        List<Organization> children = organizationMapper.selectList(wrapper);
        return Result.ok(children);
    }
}
