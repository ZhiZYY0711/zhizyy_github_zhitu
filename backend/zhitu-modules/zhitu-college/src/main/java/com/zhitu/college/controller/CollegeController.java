package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.entity.CollegeInfo;
import com.zhitu.college.mapper.CollegeInfoMapper;
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

/**
 * College Management Controller
 * Handles college information management endpoints
 */
@Tag(name = "高校管理", description = "高校信息管理相关接口")
@RestController
@RequestMapping("/api/college/v1")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeInfoMapper collegeInfoMapper;

    /**
     * Get college list with filtering and pagination
     */
    @Operation(
        summary = "获取高校列表",
        description = "分页查询高校列表，支持按省份、城市、合作等级筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要平台管理员或高校管理员权限\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 college 表\n" +
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
    @GetMapping("/colleges")
    public Result<IPage<CollegeInfo>> getColleges(
            @Parameter(description = "省份，支持模糊查询", example = "广东省")
            @RequestParam(value = "province", required = false) String province,
            @Parameter(description = "城市，支持模糊查询", example = "深圳市")
            @RequestParam(value = "city", required = false) String city,
            @Parameter(description = "合作等级：1-普通，2-重点，3-战略", example = "2")
            @RequestParam(value = "cooperationLevel", required = false) Integer cooperationLevel,
            @Parameter(description = "状态：1-正常，0-停用", example = "1")
            @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        LambdaQueryWrapper<CollegeInfo> wrapper = new LambdaQueryWrapper<>();
        if (province != null && !province.isBlank()) {
            wrapper.like(CollegeInfo::getProvince, province);
        }
        if (city != null && !city.isBlank()) {
            wrapper.like(CollegeInfo::getCity, city);
        }
        if (cooperationLevel != null) {
            wrapper.eq(CollegeInfo::getCooperationLevel, cooperationLevel);
        }
        if (status != null) {
            wrapper.eq(CollegeInfo::getStatus, status);
        }
        wrapper.orderByDesc(CollegeInfo::getId);
        
        IPage<CollegeInfo> colleges = collegeInfoMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.ok(colleges);
    }

    /**
     * Get college by ID
     */
    @Operation(
        summary = "获取高校详情",
        description = "根据ID获取高校详细信息\n\n" +
            "**测试前置条件：**\n" +
            "- 高校ID必须存在于 college 表中\n" +
            "- 需要相应权限访问\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college 表中的高校记录"
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
            description = "高校不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"高校不存在\", \"data\": null}"
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
    @GetMapping("/colleges/{id}")
    public Result<CollegeInfo> getCollege(
            @Parameter(description = "高校ID", required = true, example = "1")
            @PathVariable(value = "id") Long id) {
        
        CollegeInfo college = collegeInfoMapper.selectById(id);
        if (college == null) {
            return Result.fail(404, "高校不存在");
        }
        return Result.ok(college);
    }

    /**
     * Create a new college
     */
    @Operation(
        summary = "创建高校",
        description = "创建新的高校信息\n\n" +
            "**测试前置条件：**\n" +
            "- 需要平台管理员权限\n" +
            "- 高校名称不能与现有高校重复\n" +
            "- 必须提供有效的省份和城市信息\n\n" +
            "**数据依赖：**\n" +
            "- 无强制依赖，但建议省份和城市数据标准化",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "高校信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CollegeInfo.class),
                examples = {
                    @ExampleObject(
                        name = "创建普通合作高校",
                        summary = "创建普通合作等级的高校",
                        value = "{\"collegeName\": \"深圳职业技术学院\", \"collegeCode\": \"10001\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区学苑大道1088号\", \"logoUrl\": \"https://example.com/logo.png\", \"contactName\": \"张老师\", \"contactPhone\": \"0755-12345678\", \"contactEmail\": \"contact@college.edu.cn\", \"cooperationLevel\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建重点合作高校",
                        summary = "创建重点合作等级的高校",
                        value = "{\"collegeName\": \"深圳大学\", \"collegeCode\": \"10590\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区南海大道3688号\", \"logoUrl\": \"https://example.com/szu-logo.png\", \"contactName\": \"李主任\", \"contactPhone\": \"0755-26536114\", \"contactEmail\": \"admin@szu.edu.cn\", \"cooperationLevel\": 2}"
                    ),
                    @ExampleObject(
                        name = "创建战略合作高校",
                        summary = "创建战略合作等级的高校",
                        value = "{\"collegeName\": \"清华大学深圳国际研究生院\", \"collegeCode\": \"10003\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区西丽大学城\", \"logoUrl\": \"https://example.com/tsinghua-logo.png\", \"contactName\": \"王院长\", \"contactPhone\": \"0755-26036000\", \"contactEmail\": \"info@sz.tsinghua.edu.cn\", \"cooperationLevel\": 3}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小合作等级",
                        summary = "合作等级为1（最小值）",
                        value = "{\"collegeName\": \"测试学院\", \"collegeCode\": \"99999\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"测试地址\", \"contactName\": \"测试\", \"contactPhone\": \"0755-00000000\", \"contactEmail\": \"test@test.edu.cn\", \"cooperationLevel\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大合作等级",
                        summary = "合作等级为3（最大值）",
                        value = "{\"collegeName\": \"顶级合作学院\", \"collegeCode\": \"00001\", \"province\": \"北京市\", \"city\": \"北京市\", \"address\": \"海淀区\", \"contactName\": \"院长\", \"contactPhone\": \"010-12345678\", \"contactEmail\": \"top@college.edu.cn\", \"cooperationLevel\": 3}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-长名称",
                        summary = "包含很长名称的高校",
                        value = "{\"collegeName\": \"中国人民解放军国防科技大学深圳研究院附属职业技术学院\", \"collegeCode\": \"10001\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区科技园南区深圳湾科技生态园\", \"contactName\": \"张主任\", \"contactPhone\": \"0755-12345678\", \"contactEmail\": \"contact@long-name-college.edu.cn\", \"cooperationLevel\": 2}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号",
                        summary = "包含特殊符号的联系信息",
                        value = "{\"collegeName\": \"深圳学院\", \"collegeCode\": \"SZ-001\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区科技园（A区）1号楼\", \"contactName\": \"李老师（招生办）\", \"contactPhone\": \"0755-1234-5678\", \"contactEmail\": \"li.teacher@college.edu.cn\", \"cooperationLevel\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-空Logo",
                        summary = "Logo URL为空（可选字段）",
                        value = "{\"collegeName\": \"无Logo学院\", \"collegeCode\": \"10002\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"福田区\", \"contactName\": \"王老师\", \"contactPhone\": \"0755-88888888\", \"contactEmail\": \"contact@nologo.edu.cn\", \"cooperationLevel\": 1}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-国际邮箱",
                        summary = "包含国际域名的邮箱",
                        value = "{\"collegeName\": \"国际学院\", \"collegeCode\": \"INT001\", \"province\": \"上海市\", \"city\": \"上海市\", \"address\": \"浦东新区\", \"contactName\": \"John Smith\", \"contactPhone\": \"021-12345678\", \"contactEmail\": \"contact@international-college.org\", \"cooperationLevel\": 2}"
                    ),
                    @ExampleObject(
                        name = "边界值-最短编码",
                        summary = "最短的高校编码",
                        value = "{\"collegeName\": \"简码学院\", \"collegeCode\": \"001\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"罗湖区\", \"contactName\": \"赵老师\", \"contactPhone\": \"0755-99999999\", \"contactEmail\": \"zhao@short.edu.cn\", \"cooperationLevel\": 1}"
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
    @PostMapping("/colleges")
    public Result<Long> createCollege(
            @Parameter(description = "高校信息", required = true)
            @Valid @RequestBody CollegeInfo college) {
        
        Long tenantId = UserContext.getTenantId();
        college.setTenantId(tenantId);
        college.setStatus(1); // 默认状态为正常
        collegeInfoMapper.insert(college);
        return Result.ok(college.getId());
    }

    /**
     * Update college information
     */
    @Operation(
        summary = "更新高校信息",
        description = "更新指定高校的信息\n\n" +
            "**测试前置条件：**\n" +
            "- 高校ID必须存在于 college 表中\n" +
            "- 需要平台管理员或对应高校管理员权限\n" +
            "- 如果更新高校名称，新名称不能与其他高校重复\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college 表中的现有高校记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "高校信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CollegeInfo.class),
                examples = {
                    @ExampleObject(
                        name = "更新联系方式",
                        summary = "更新高校联系人信息",
                        value = "{\"contactName\": \"王老师\", \"contactPhone\": \"0755-88888888\", \"contactEmail\": \"newcontact@college.edu.cn\"}"
                    ),
                    @ExampleObject(
                        name = "升级合作等级",
                        summary = "将合作等级升级为战略合作",
                        value = "{\"cooperationLevel\": 3}"
                    ),
                    @ExampleObject(
                        name = "完整更新",
                        summary = "更新高校完整信息",
                        value = "{\"collegeName\": \"深圳职业技术大学\", \"collegeCode\": \"10001\", \"province\": \"广东省\", \"city\": \"深圳市\", \"address\": \"南山区学苑大道1088号\", \"logoUrl\": \"https://example.com/new-logo.png\", \"contactName\": \"张主任\", \"contactPhone\": \"0755-12345678\", \"contactEmail\": \"contact@college.edu.cn\", \"cooperationLevel\": 2, \"status\": 1}"
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
            description = "高校不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"高校不存在\", \"data\": null}"
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
    @PutMapping("/colleges/{id}")
    public Result<Void> updateCollege(
            @Parameter(description = "高校ID", required = true, example = "1")
            @PathVariable(value = "id") Long id,
            @Parameter(description = "高校信息", required = true)
            @Valid @RequestBody CollegeInfo college) {
        
        CollegeInfo existing = collegeInfoMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "高校不存在");
        }
        
        college.setId(id);
        collegeInfoMapper.updateById(college);
        return Result.ok();
    }

    /**
     * Delete college
     */
    @Operation(
        summary = "删除高校",
        description = "删除指定的高校信息（软删除）\n\n" +
            "**测试前置条件：**\n" +
            "- 高校ID必须存在于 college 表中\n" +
            "- 需要平台管理员权限\n" +
            "- 高校下不能有关联的学院、专业或学生数据\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college 表中的高校记录\n" +
            "- 删除前应检查关联的组织和学生数据"
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
            description = "高校不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"高校不存在\", \"data\": null}"
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
    @DeleteMapping("/colleges/{id}")
    public Result<Void> deleteCollege(
            @Parameter(description = "高校ID", required = true, example = "1")
            @PathVariable(value = "id") Long id) {
        
        CollegeInfo existing = collegeInfoMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "高校不存在");
        }
        
        collegeInfoMapper.deleteById(id);
        return Result.ok();
    }

    /**
     * Update college cooperation level
     */
    @Operation(
        summary = "更新合作等级",
        description = "更新高校的合作等级\n\n" +
            "**测试前置条件：**\n" +
            "- 高校ID必须存在于 college 表中\n" +
            "- 需要平台管理员权限\n" +
            "- 合作等级值必须在有效范围内（1-5）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college 表中的高校记录"
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
            description = "高校不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"高校不存在\", \"data\": null}"
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
    @PatchMapping("/colleges/{id}/cooperation-level")
    public Result<Void> updateCooperationLevel(
            @Parameter(description = "高校ID", required = true, example = "1")
            @PathVariable(value = "id") Long id,
            @Parameter(description = "合作等级：1-普通，2-重点，3-战略", required = true, example = "2")
            @RequestParam(value = "level") Integer level) {
        
        CollegeInfo existing = collegeInfoMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "高校不存在");
        }
        
        existing.setCooperationLevel(level);
        collegeInfoMapper.updateById(existing);
        return Result.ok();
    }

    /**
     * Update college status
     */
    @Operation(
        summary = "更新高校状态",
        description = "启用或停用高校\n\n" +
            "**测试前置条件：**\n" +
            "- 高校ID必须存在于 college 表中\n" +
            "- 需要平台管理员权限\n" +
            "- 状态值必须为 1（启用）或 0（停用）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 college 表中的高校记录\n" +
            "- 停用高校会影响该高校下所有用户的访问权限"
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
            description = "高校不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"高校不存在\", \"data\": null}"
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
    @PatchMapping("/colleges/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "高校ID", required = true, example = "1")
            @PathVariable(value = "id") Long id,
            @Parameter(description = "状态：1-正常，0-停用", required = true, example = "1")
            @RequestParam(value = "status") Integer status) {
        
        CollegeInfo existing = collegeInfoMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "高校不存在");
        }
        
        existing.setStatus(status);
        collegeInfoMapper.updateById(existing);
        return Result.ok();
    }
}
