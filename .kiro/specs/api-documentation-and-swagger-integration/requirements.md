# 需求文档

## 简介

智途云平台是一个基于 Spring Cloud 微服务架构的高校产教融合智能服务平台。在前后端联调过程中发现大量前后端接口不一致的情况，导致集成困难和开发效率低下。本功能旨在解决这一问题，通过生成标准化的 API 文档并优化 Swagger 集成，确保前后端接口规范的一致性和可维护性。

## 术语表

- **API_Documentation_System**: API 文档系统，负责生成和维护接口文档
- **Swagger_Integration**: Swagger 集成模块，提供交互式 API 文档界面
- **Backend_Service**: 后端微服务，包括 zhitu-auth、zhitu-gateway、zhitu-college、zhitu-enterprise、zhitu-platform、zhitu-student、zhitu-system
- **Controller**: Spring MVC 控制器，定义 REST API 端点
- **API_Specification**: API 规范文档，描述接口的请求和响应格式
- **SpringDoc_OpenAPI**: SpringDoc OpenAPI 库，用于生成 OpenAPI 3.0 规范文档
- **Module**: 微服务模块，按业务领域划分的独立服务单元
- **Gateway**: API 网关服务，统一入口和路由管理
- **Authentication_Service**: 认证服务，处理用户登录和令牌管理

## 需求

### 需求 1: 生成模块化 API 文档

**用户故事:** 作为前端开发人员，我希望能够查看按模块组织的 API 文档，以便快速找到所需的接口定义。

#### 验收标准

1. THE API_Documentation_System SHALL 为每个 Backend_Service 生成独立的 API 文档文件
2. THE API_Documentation_System SHALL 将文档按照微服务模块划分到对应的子目录中
3. THE API_Documentation_System SHALL 在文档中包含接口的 HTTP 方法、路径、请求参数、响应格式和状态码
4. THE API_Documentation_System SHALL 在文档中包含请求和响应的数据模型定义
5. THE API_Documentation_System SHALL 在文档中包含认证和授权要求说明
6. THE API_Documentation_System SHALL 将所有文档输出到 `docs/api` 目录下的模块子目录中
7. THE API_Documentation_System SHALL 为每个模块创建独立的 README 文件，说明该模块的功能范围

### 需求 2: 标准化接口文档格式

**用户故事:** 作为 API 使用者，我希望所有接口文档遵循统一的格式规范，以便提高文档的可读性和一致性。

#### 验收标准

1. THE API_Specification SHALL 使用 Markdown 格式编写
2. THE API_Specification SHALL 包含接口概述、请求示例和响应示例
3. THE API_Specification SHALL 明确标注必填参数和可选参数
4. THE API_Specification SHALL 包含错误码和错误信息的完整列表
5. THE API_Specification SHALL 包含接口版本信息
6. THE API_Specification SHALL 包含接口的变更历史记录
7. FOR ALL Controller 方法，THE API_Specification SHALL 包含对应的文档条目

### 需求 3: 验证 Swagger 集成

**用户故事:** 作为开发人员，我希望确认项目中已正确集成 Swagger 工具，以便通过交互式界面测试 API。

#### 验收标准

1. THE Swagger_Integration SHALL 在所有 Backend_Service 的 pom.xml 中包含 springdoc-openapi-starter-webmvc-ui 依赖
2. THE Swagger_Integration SHALL 在公共模块中提供统一的 OpenAPI 配置类
3. THE Swagger_Integration SHALL 配置 JWT Bearer 认证方案
4. THE Swagger_Integration SHALL 为每个微服务提供独立的 Swagger UI 访问地址
5. WHEN 访问 Swagger UI 地址时，THE Swagger_Integration SHALL 显示该服务的所有 API 端点
6. THE Swagger_Integration SHALL 在 Swagger UI 中支持直接测试 API 请求
7. THE Swagger_Integration SHALL 在 Gateway 中聚合所有微服务的 API 文档

### 需求 4: 增强 Swagger 注解

**用户故事:** 作为后端开发人员，我希望在代码中添加详细的 Swagger 注解，以便自动生成高质量的 API 文档。

#### 验收标准

1. FOR ALL Controller 类，THE Backend_Service SHALL 添加 @Tag 注解描述 API 分组
2. FOR ALL Controller 方法，THE Backend_Service SHALL 添加 @Operation 注解描述接口功能
3. FOR ALL 请求参数，THE Backend_Service SHALL 添加 @Parameter 注解描述参数用途
4. FOR ALL 响应对象，THE Backend_Service SHALL 添加 @Schema 注解描述数据模型
5. FOR ALL DTO 类的字段，THE Backend_Service SHALL 添加 @Schema 注解描述字段含义
6. WHEN 接口返回错误时，THE Backend_Service SHALL 使用 @ApiResponse 注解描述错误场景
7. THE Backend_Service SHALL 在注解中包含示例值以提高文档可读性

### 需求 5: 配置 Gateway 文档聚合

**用户故事:** 作为 API 使用者，我希望通过统一的网关入口访问所有微服务的 API 文档，而不需要分别访问每个服务。

#### 验收标准

1. THE Gateway SHALL 配置 SpringDoc 的分组功能以聚合所有微服务文档
2. THE Gateway SHALL 为每个 Backend_Service 创建独立的文档分组
3. WHEN 访问 Gateway 的 Swagger UI 时，THE Gateway SHALL 提供下拉菜单选择不同的服务文档
4. THE Gateway SHALL 通过服务发现机制自动获取各微服务的 OpenAPI 规范
5. THE Gateway SHALL 在聚合文档中保留各服务的原始路径前缀
6. THE Gateway SHALL 支持在聚合界面中直接测试各微服务的 API
7. IF 某个微服务不可用，THEN THE Gateway SHALL 在文档界面中显示相应的提示信息

### 需求 6: 生成接口变更对比报告

**用户故事:** 作为项目经理，我希望能够生成接口变更对比报告，以便跟踪 API 的演进历史和识别破坏性变更。

#### 验收标准

1. THE API_Documentation_System SHALL 记录每次文档生成的时间戳
2. THE API_Documentation_System SHALL 比较当前接口定义与上一版本的差异
3. THE API_Documentation_System SHALL 识别新增的接口端点
4. THE API_Documentation_System SHALL 识别已删除的接口端点
5. THE API_Documentation_System SHALL 识别接口参数的变更（新增、删除、类型修改）
6. THE API_Documentation_System SHALL 识别响应格式的变更
7. THE API_Documentation_System SHALL 生成变更报告并保存到 `docs/api/CHANGELOG.md` 文件

### 需求 7: 配置文档访问权限

**用户故事:** 作为系统管理员，我希望能够控制 Swagger UI 的访问权限，以便在生产环境中保护敏感的 API 信息。

#### 验收标准

1. THE Swagger_Integration SHALL 支持通过配置文件启用或禁用 Swagger UI
2. THE Swagger_Integration SHALL 在生产环境配置中默认禁用 Swagger UI
3. THE Swagger_Integration SHALL 在开发和测试环境中默认启用 Swagger UI
4. WHERE Swagger UI 启用时，THE Swagger_Integration SHALL 要求用户进行身份认证
5. THE Swagger_Integration SHALL 支持配置允许访问文档的用户角色列表
6. THE Swagger_Integration SHALL 在未授权访问时返回 403 Forbidden 状态码
7. THE Swagger_Integration SHALL 记录所有文档访问的审计日志

### 需求 8: 自动化文档生成流程

**用户故事:** 作为 DevOps 工程师，我希望将 API 文档生成集成到 CI/CD 流程中，以便在每次代码变更后自动更新文档。

#### 验收标准

1. THE API_Documentation_System SHALL 提供 Maven 插件或脚本以自动生成文档
2. THE API_Documentation_System SHALL 在构建过程中验证所有 Controller 都有完整的注解
3. IF 发现缺少必要注解的接口，THEN THE API_Documentation_System SHALL 输出警告信息
4. THE API_Documentation_System SHALL 在构建成功后自动更新 `docs/api` 目录中的文档
5. THE API_Documentation_System SHALL 生成 OpenAPI JSON/YAML 规范文件
6. THE API_Documentation_System SHALL 将生成的规范文件输出到 `docs/api/openapi` 目录
7. THE API_Documentation_System SHALL 支持通过命令行参数指定输出目录和格式

### 需求 9: 提供接口测试数据

**用户故事:** 作为测试人员，我希望文档中包含接口的测试数据示例，以便快速进行接口测试和验证。

#### 验收标准

1. THE API_Specification SHALL 为每个接口提供至少一个有效的请求示例
2. THE API_Specification SHALL 为每个接口提供至少一个成功的响应示例
3. THE API_Specification SHALL 为每个接口提供常见错误场景的示例
4. THE API_Specification SHALL 包含边界值和特殊字符的测试用例
5. THE API_Specification SHALL 说明测试数据的前置条件和依赖关系
6. THE Swagger_Integration SHALL 在 Swagger UI 中预填充示例数据
7. THE Swagger_Integration SHALL 支持导出 Postman Collection 格式的测试集合

### 需求 10: 文档国际化支持

**用户故事:** 作为国际化团队成员，我希望 API 文档支持多语言，以便不同地区的开发人员都能理解接口定义。

#### 验收标准

1. THE API_Documentation_System SHALL 支持生成中文和英文两种语言的文档
2. THE API_Documentation_System SHALL 在文档目录中按语言代码组织文档文件
3. THE Swagger_Integration SHALL 支持通过配置切换 Swagger UI 的显示语言
4. THE API_Specification SHALL 为所有接口描述提供中英文双语版本
5. THE API_Specification SHALL 为所有错误消息提供中英文双语版本
6. THE Swagger_Integration SHALL 在注解中使用 i18n 消息键而非硬编码文本
7. WHERE 缺少翻译时，THE API_Documentation_System SHALL 回退到默认语言（中文）
