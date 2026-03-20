# Docker 部署指南

## ✅ 已完成的工作

### 1. Docker 镜像
- ✅ 7 个后端服务 Dockerfile（多阶段构建）
- ✅ 前端 Dockerfile（使用 Vite 预览服务器）
- ✅ 优化的 .dockerignore 文件

### 2. Docker Compose 配置
- ✅ `docker/docker-compose.yml` - 生产环境（所有服务）
- ✅ `docker/docker-compose.dev.yml` - 开发环境（仅基础设施）
- ✅ 健康检查和依赖管理
- ✅ 自动重启策略

### 3. 管理脚本
所有脚本位于 `scripts/` 目录：
- ✅ `start.bat/sh` - 完整启动
- ✅ `start-dev.bat/sh` - 开发模式
- ✅ `stop.bat/sh` - 停止服务
- ✅ `status.bat/sh` - 查看状态
- ✅ `test-services.bat/sh` - 健康检查

### 4. 文档
- ✅ `README.md` - 项目主文档
- ✅ `QUICK-START.md` - 快速启动指南（唯一详细文档）
- ✅ `scripts/README.md` - 脚本说明

## 🚀 快速开始

### 完整部署
```bash
cd scripts
./start.sh      # Linux/Mac
start.bat       # Windows
```

### 开发模式
```bash
cd scripts
./start-dev.sh  # Linux/Mac
start-dev.bat   # Windows
```

## 📦 服务清单

| 服务 | 端口 | 说明 |
|------|------|------|
| Frontend | 80 | 前端应用（Vite 预览） |
| Gateway | 8888 | API 网关 |
| Auth | 9200 | 认证服务 |
| System | 9201 | 系统服务 |
| College | 9202 | 高校服务 |
| Enterprise | 9203 | 企业服务 |
| Student | 9204 | 学生服务 |
| Platform | 9205 | 平台服务 |
| PostgreSQL | 15432 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 注册中心 |
| MinIO | 9000/9001 | 对象存储 |

## 🔧 技术细节

### 前端镜像
- 不使用 Nginx，直接使用 Vite 预览服务器
- 更轻量，配置更简单
- 端口：4173（映射到主机 80）

### 后端镜像
- 多阶段构建：Maven 构建 + JRE 运行
- 基础镜像：Eclipse Temurin 17 Alpine
- 自动注册到 Nacos

### Docker 镜像加速
配置示例：`docker/daemon.json.example`

**Windows/Mac (Docker Desktop):**
1. Settings → Docker Engine
2. 添加 `daemon.json.example` 中的配置
3. Apply & Restart

**Linux:**
```bash
sudo cp docker/daemon.json.example /etc/docker/daemon.json
sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 📊 架构图

```
用户
 ↓
Frontend (Vite) :80
 ↓
Gateway :8888
 ↓
┌─────┬──────┬────────┬──────────┬────────┬────────┬────────┐
Auth  System College  Enterprise Student Platform  ...
:9200 :9201  :9202    :9203      :9204   :9205
 ↓
┌──────────┬────────┬────────┬────────┐
PostgreSQL  Redis   Nacos    MinIO
:15432     :6379   :8848    :9000
```

## 💡 最佳实践

### 开发环境
1. 使用 `start-dev.sh` 启动基础设施
2. IDE 中运行需要开发的服务
3. 前端使用 `npm run dev` 热重载

### 生产环境
1. 修改默认密码
2. 配置外部数据卷
3. 添加资源限制
4. 配置 HTTPS
5. 使用负载均衡器

### 故障排查
```bash
# 查看日志
cd docker
docker-compose logs -f [服务名]

# 重启服务
docker-compose restart [服务名]

# 重新构建
docker-compose up -d --build [服务名]

# 完全清理
docker-compose down -v
```

## 📝 文件清单

### 根目录
- `README.md` - 项目主文档
- `QUICK-START.md` - 快速启动指南
- `DOCKER-GUIDE.md` - 本文档

### backend/
- `Dockerfile.gateway` - Gateway 镜像
- `Dockerfile.auth` - Auth 镜像
- `Dockerfile.system` - System 镜像
- `Dockerfile.college` - College 镜像
- `Dockerfile.enterprise` - Enterprise 镜像
- `Dockerfile.student` - Student 镜像
- `Dockerfile.platform` - Platform 镜像
- `.dockerignore` - 构建忽略文件

### frontend/
- `Dockerfile` - 前端镜像
- `.dockerignore` - 构建忽略文件

### docker/
- `docker-compose.yml` - 生产环境配置
- `docker-compose.dev.yml` - 开发环境配置
- `daemon.json.example` - Docker 镜像加速配置

### scripts/
- `start.bat/sh` - 完整启动
- `start-dev.bat/sh` - 开发模式
- `stop.bat/sh` - 停止服务
- `status.bat/sh` - 查看状态
- `test-services.bat/sh` - 健康检查
- `README.md` - 脚本说明

## 🎯 下一步

1. ✅ 运行 `cd scripts && ./start.sh` 启动系统
2. ✅ 访问 http://localhost 测试前端
3. ✅ 访问 http://localhost:8848/nacos 查看服务注册
4. ✅ 运行 `cd scripts && ./test-services.sh` 验证服务

## 📞 获取帮助

- 📖 查看 [QUICK-START.md](QUICK-START.md)
- 📖 查看 [scripts/README.md](scripts/README.md)
- 🐛 提交 Issue

---

**版本：** v1.0.0  
**更新日期：** 2024-03-20  
**状态：** ✅ 完成
