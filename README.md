# 智途云平台 (Zhitu Cloud Platform)

高校产教融合智能服务平台 - 基于 Spring Cloud 微服务架构

## 🚀 快速开始

### 完整部署

一键启动所有服务（基础设施 + 后端 + 前端）

**Windows:**
```bash
cd scripts
start.bat
```

**Linux/Mac:**
```bash
cd scripts
chmod +x *.sh
./start.sh
```

访问 http://localhost

### 开发模式

仅启动基础设施，代码在 IDE 中运行

**Windows:**
```bash
cd scripts
start-dev.bat
```

**Linux/Mac:**
```bash
cd scripts
./start-dev.sh
```

然后在 IDE 中启动后端服务，运行 `cd frontend && npm run dev` 启动前端。

📖 详细说明请查看 [快速启动指南](QUICK-START.md)

## 📋 项目结构

```
zhitu-cloud/
├── backend/                 # 后端微服务
│   ├── Dockerfile.*        # 各服务 Docker 镜像
│   ├── zhitu-gateway/      # API 网关
│   ├── zhitu-auth/         # 认证服务
│   ├── zhitu-common/       # 公共模块
│   └── zhitu-modules/      # 业务模块
│       ├── zhitu-system/   # 系统服务
│       ├── zhitu-college/  # 高校服务
│       ├── zhitu-enterprise/ # 企业服务
│       ├── zhitu-student/  # 学生服务
│       └── zhitu-platform/ # 平台服务
├── frontend/               # 前端应用 (React + Vite)
├── docker/                 # Docker 配置
│   ├── docker-compose.yml      # 生产环境
│   ├── docker-compose.dev.yml  # 开发环境
│   └── daemon.json.example     # Docker 镜像加速配置
├── scripts/                # 管理脚本
│   ├── start.bat/sh       # 完整启动
│   ├── start-dev.bat/sh   # 开发模式
│   ├── stop.bat/sh        # 停止服务
│   ├── status.bat/sh      # 查看状态
│   └── test-services.bat/sh # 健康检查
└── docs/                   # 文档
```

## 🛠️ 技术栈

### 后端
- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.2
- Spring Cloud Alibaba 2022.0.0.0
- PostgreSQL 15
- Redis 7.0
- Nacos 2.5.1
- MinIO 8.5.10

### 前端
- React 19
- TypeScript
- Vite 7
- TailwindCSS 4
- React Router 7

## 📦 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Frontend | 80 | 前端应用 |
| Gateway | 8888 | API 网关 |
| Auth | 9200 | 认证服务 |
| System | 9201 | 系统服务 |
| College | 9202 | 高校服务 |
| Enterprise | 9203 | 企业服务 |
| Student | 9204 | 学生服务 |
| Platform | 9205 | 平台服务 |
| Nacos | 8848 | 注册中心 |
| PostgreSQL | 15432 | 数据库 |
| Redis | 6379 | 缓存 |
| MinIO | 9000/9001 | 对象存储 |

## 📚 文档

- [快速启动指南](QUICK-START.md) ⭐ 推荐阅读
- [Docker 部署指南](DOCKER-GUIDE.md)
- [环境配置说明](backend/环境配置说明.md)
- [后端项目环境搭建](backend/后端项目环境搭建执行清单.md)
- [开发任务清单](backend/开发任务清单.md)

## 🔧 开发指南

### 后端开发

1. 导入 Maven 项目到 IDE
2. 配置环境变量（参考 .env.example）
3. 启动基础设施服务
4. 运行各个微服务

### 前端开发

```bash
cd frontend
npm install
npm run dev
```

## 📝 许可证

[MIT License](LICENSE)

## 👥 贡献

欢迎提交 Issue 和 Pull Request

## 📧 联系方式

如有问题，请提交 Issue
