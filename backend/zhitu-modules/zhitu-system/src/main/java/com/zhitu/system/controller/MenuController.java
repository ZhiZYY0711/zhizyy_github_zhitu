package com.zhitu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.Result;
import com.zhitu.system.entity.SysMenu;
import com.zhitu.system.mapper.SysMenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "菜单管理", description = "菜单配置管理相关接口")
@RestController
@RequestMapping("/api/system/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuMapper menuMapper;

    @Operation(
        summary = "获取菜单列表",
        description = "分页查询系统菜单列表，支持按菜单名称、类型、状态筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要管理员权限\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 sys_menu 表\n" +
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
    @GetMapping
    public Result<Page<SysMenu>> list(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "菜单名称，支持模糊查询", example = "用户管理")
            @RequestParam(required = false) String menuName,
            @Parameter(description = "菜单类型：1-目录，2-菜单，3-按钮", example = "2")
            @RequestParam(required = false) Integer menuType,
            @Parameter(description = "状态：1-启用，0-禁用", example = "1")
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(menuName)) {
            wrapper.like(SysMenu::getMenuName, menuName);
        }
        if (menuType != null) {
            wrapper.eq(SysMenu::getMenuType, menuType);
        }
        if (status != null) {
            wrapper.eq(SysMenu::getStatus, status);
        }
        wrapper.orderByAsc(SysMenu::getSortOrder);

        Page<SysMenu> page = menuMapper.selectPage(new Page<>(current, size), wrapper);
        return Result.ok(page);
    }

    @Operation(
        summary = "获取菜单树",
        description = "获取完整的菜单树结构，用于前端菜单渲染\n\n" +
            "**测试前置条件：**\n" +
            "- 需要已登录用户\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_menu 表中的菜单记录\n" +
            "- 菜单之间通过 parent_id 建立父子关系"
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
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree(
            @Parameter(description = "状态：1-启用，0-禁用", example = "1")
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysMenu::getStatus, status);
        }
        wrapper.orderByAsc(SysMenu::getSortOrder);

        List<SysMenu> menus = menuMapper.selectList(wrapper);
        return Result.ok(menus);
    }

    @Operation(
        summary = "根据ID获取菜单详情",
        description = "通过菜单ID查询菜单的详细信息\n\n" +
            "**测试前置条件：**\n" +
            "- 菜单ID必须存在于 sys_menu 表中\n" +
            "- 需要管理员权限\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_menu 表中的菜单记录"
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
            description = "菜单不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"菜单不存在\", \"data\": null}"
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
    @GetMapping("/{id}")
    public Result<SysMenu> getById(
            @Parameter(description = "菜单ID", required = true, example = "1")
            @PathVariable Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            return Result.fail(404, "菜单不存在");
        }
        return Result.ok(menu);
    }

    @Operation(
        summary = "创建菜单",
        description = "创建新的系统菜单\n\n" +
            "**测试前置条件：**\n" +
            "- 如果是子菜单，父菜单ID（parentId）必须存在于 sys_menu 表中\n" +
            "- 需要管理员权限\n" +
            "- 菜单路径（path）不能与同级菜单重复\n\n" +
            "**数据依赖：**\n" +
            "- 如果 parentId 不为 null，依赖 sys_menu 表中的父菜单记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "菜单信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SysMenu.class),
                examples = {
                    @ExampleObject(
                        name = "创建目录",
                        summary = "创建一级目录菜单",
                        value = "{\"parentId\": 0, \"menuName\": \"系统管理\", \"menuPath\": \"/system\", \"icon\": \"setting\", \"menuType\": 1, \"status\": 1, \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建菜单",
                        summary = "创建二级菜单",
                        value = "{\"parentId\": 1, \"menuName\": \"用户管理\", \"menuPath\": \"/system/user\", \"icon\": \"user\", \"menuType\": 2, \"permission\": \"system:user:list\", \"status\": 1, \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建按钮",
                        summary = "创建操作按钮",
                        value = "{\"parentId\": 2, \"menuName\": \"新增用户\", \"menuType\": 3, \"permission\": \"system:user:create\", \"status\": 1, \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-根菜单",
                        summary = "父ID为0的根菜单",
                        value = "{\"parentId\": 0, \"menuName\": \"根菜单\", \"menuPath\": \"/root\", \"icon\": \"home\", \"menuType\": 1, \"status\": 1, \"sortOrder\": 0}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大排序值",
                        summary = "排序值为999",
                        value = "{\"parentId\": 1, \"menuName\": \"最后菜单\", \"menuPath\": \"/last\", \"menuType\": 2, \"status\": 1, \"sortOrder\": 999}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中文路径",
                        summary = "包含中文的菜单路径",
                        value = "{\"parentId\": 1, \"menuName\": \"用户中心\", \"menuPath\": \"/系统/用户\", \"icon\": \"user\", \"menuType\": 2, \"status\": 1, \"sortOrder\": 5}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号路径",
                        summary = "包含特殊符号的路径",
                        value = "{\"parentId\": 1, \"menuName\": \"高级设置\", \"menuPath\": \"/system/settings-advanced_v2\", \"icon\": \"setting\", \"menuType\": 2, \"status\": 1, \"sortOrder\": 10}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-权限标识",
                        summary = "复杂的权限标识符",
                        value = "{\"parentId\": 2, \"menuName\": \"批量删除\", \"menuType\": 3, \"permission\": \"system:user:batch:delete\", \"status\": 1, \"sortOrder\": 5}"
                    ),
                    @ExampleObject(
                        name = "边界值-空图标",
                        summary = "图标字段为空（可选字段）",
                        value = "{\"parentId\": 1, \"menuName\": \"无图标菜单\", \"menuPath\": \"/no-icon\", \"menuType\": 2, \"status\": 1, \"sortOrder\": 20}"
                    ),
                    @ExampleObject(
                        name = "边界值-禁用状态",
                        summary = "创建时即为禁用状态",
                        value = "{\"parentId\": 1, \"menuName\": \"禁用菜单\", \"menuPath\": \"/disabled\", \"icon\": \"stop\", \"menuType\": 2, \"status\": 0, \"sortOrder\": 100}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-URL参数",
                        summary = "包含查询参数的路径",
                        value = "{\"parentId\": 1, \"menuName\": \"带参数菜单\", \"menuPath\": \"/system/user?tab=profile&view=detail\", \"icon\": \"link\", \"menuType\": 2, \"status\": 1, \"sortOrder\": 15}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-外部链接",
                        summary = "外部链接菜单",
                        value = "{\"parentId\": 1, \"menuName\": \"帮助文档\", \"menuPath\": \"https://docs.example.com/help\", \"icon\": \"question\", \"menuType\": 2, \"status\": 1, \"sortOrder\": 99}"
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
    @PostMapping
    public Result<SysMenu> create(
            @Parameter(description = "菜单信息", required = true)
            @RequestBody SysMenu menu) {
        menu.setCreatedAt(OffsetDateTime.now());
        menu.setUpdatedAt(OffsetDateTime.now());
        menuMapper.insert(menu);
        return Result.ok(menu);
    }

    @Operation(
        summary = "更新菜单信息",
        description = "根据菜单ID更新菜单信息\n\n" +
            "**测试前置条件：**\n" +
            "- 菜单ID必须存在于 sys_menu 表中\n" +
            "- 需要管理员权限\n" +
            "- 如果更新父菜单ID，新的父菜单必须存在\n" +
            "- 不能将菜单的父菜单设置为自己或自己的子菜单（避免循环引用）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_menu 表中的现有菜单记录\n" +
            "- 如果更新 parentId，依赖新的父菜单记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "菜单信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SysMenu.class),
                examples = {
                    @ExampleObject(
                        name = "更新菜单状态",
                        summary = "禁用菜单",
                        value = "{\"status\": 0}"
                    ),
                    @ExampleObject(
                        name = "更新菜单信息",
                        summary = "更新菜单名称和图标",
                        value = "{\"menuName\": \"用户中心\", \"icon\": \"user-circle\", \"sortOrder\": 2}"
                    ),
                    @ExampleObject(
                        name = "完整更新",
                        summary = "更新菜单完整信息",
                        value = "{\"parentId\": 1, \"menuName\": \"用户管理\", \"menuPath\": \"/system/users\", \"icon\": \"user\", \"menuType\": 2, \"permission\": \"system:user:list\", \"status\": 1, \"sortOrder\": 1}"
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
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "菜单不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"菜单不存在\", \"data\": null}"
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
    @PutMapping("/{id}")
    public Result<SysMenu> update(
            @Parameter(description = "菜单ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "菜单信息", required = true)
            @RequestBody SysMenu menu) {
        SysMenu existing = menuMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "菜单不存在");
        }

        menu.setId(id);
        menu.setUpdatedAt(OffsetDateTime.now());
        menuMapper.updateById(menu);
        return Result.ok(menu);
    }

    @Operation(
        summary = "删除菜单",
        description = "根据菜单ID删除菜单（逻辑删除）\n\n" +
            "**测试前置条件：**\n" +
            "- 菜单ID必须存在于 sys_menu 表中\n" +
            "- 需要管理员权限\n" +
            "- 菜单下不能有子菜单\n" +
            "- 菜单未被任何角色关联\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_menu 表中的菜单记录\n" +
            "- 删除前应检查是否有子菜单和角色关联"
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
            description = "菜单不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"菜单不存在\", \"data\": null}"
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
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "菜单ID", required = true, example = "1")
            @PathVariable Long id) {
        SysMenu existing = menuMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "菜单不存在");
        }

        menuMapper.deleteById(id);
        return Result.ok();
    }
}
