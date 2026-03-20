# 智途云平台 - 开发环境脚本

## 📁 目录结构

```
scripts/
├── windows/                    # Windows批处理脚本
│   ├── start-all.bat          # 启动完整环境
│   ├── start-infra.bat        # 启动基础设施
│   ├── start-backend.bat      # 启动后端
│   ├── start-frontend.bat     # 启动前端
│   ├── stop-all.bat           # 停止所有服务
│   ├── stop-infra.bat         # 停止基础设施
│   ├── stop-backend.bat       # 停止后端
│   ├── stop-frontend.bat      # 停止前端
│   └── setup-docker-mirror.bat # Docker镜像源配置
├── linux/                      # Linux Shell脚本
│   ├── start-all.sh           # 启动完整环境
│   ├── start-infra.sh         # 启动基础设施
│   ├── start-backend.sh       # 启动后端
│   ├── start-frontend.sh      # 启动前端
│   ├── stop-all.sh            # 停止所有服务
│   ├── stop-infra.sh          # 停止基础设施
│   ├── stop-backend.sh        # 停止后端
│   ├── stop-frontend.sh       # 停止前端
│   └── setup-docker-mirror.sh # Docker镜像源配置
├── start.bat                   # Windows启动入口（菜单）
├── stop.bat                    # Windows停止入口（菜单）
├── start.sh                    # Linux启动入口（菜单）
├── stop.sh                     # Linux停止入口（菜单）
└── README.md                   # 本文档
```

## 🚀 快速开始

### Windows系统

1. 启动服务：双击 `start.bat`
2. 停止服务：双击 `stop.bat`

### Linux/Mac系统

首次使用需要添加执行权限：
```bash
chmod +x start.sh stop.sh
chmod +x linux/*.sh
```

然后运行：
```bash
# 启动服务
./start.sh

# 停止服务
./stop.sh
```

## 📋 服务选项

### 启动服务 (start.bat / start.sh)

1. **完整开发环境** - 启动所有服务（基础设施 + 后端 + 前端）
2. **基础设施服务** - 仅启动Docker容器（PostgreSQL + Redis + Nacos + MinIO）
3. **后端服务** - 启动基础设施 + 后端Spring Boot应用
4. **前端服务** - 仅启动前端Vite开发服务器

### 停止服务 (stop.bat / stop.sh)

1. **停止所有服务** - 停止前端、后端和基础设施
2. **停止基础设施服务** - 仅停止Docker容器
3. **停止后端服务** - 仅停止后端应用
4. **停止前端服务** - 仅停止前端开发服务器
5. **查看服务状态** - 查看Docker容器运行状态

## 🌐 服务访问地址

启动成功后，可以访问以下地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost:5173 | React + Vite开发服务器 |
| 后端网关 | http://localhost:8888 | Spring Cloud Gateway |
| Nacos控制台 | http://localhost:8848/nacos | 用户名/密码: nacos/nacos |
| MinIO控制台 | http://localhost:9001 | 用户名/密码: minioadmin/minioadmin |
| PostgreSQL | localhost:15432 | 数据库端口 |
| Redis | localhost:6379 | 缓存端口 |

## 📦 基础设施服务说明

### PostgreSQL
- 端口: 15432
- 用户名: postgres
- 密码: 123456
- 数据库: zhitu_cloud

### Redis
- 端口: 6379
- 密码: 123456

### Nacos
- HTTP端口: 8848
- gRPC端口: 9848
- 控制台: 8080

### MinIO
- API端口: 9000
- 控制台端口: 9001

## ⚙️ 前置要求

### 必需软件

1. **Docker Desktop** - 运行基础设施服务
2. **Java 17+** - 后端开发
3. **Maven 3.6+** - 后端构建工具
4. **Node.js 18+** - 前端开发
5. **pnpm** - 前端包管理器

### 检查安装

```bash
# 检查Docker
docker --version

# 检查Java
java -version

# 检查Maven
mvn -version

# 检查Node.js
node --version

# 检查pnpm
pnpm --version
```

## 📝 环境说明

### 开发环境（当前脚本）
- **基础设施**: Docker容器运行（PostgreSQL, Redis, Nacos, MinIO）
- **后端服务**: 宿主机运行（Maven Spring Boot）
- **前端服务**: 宿主机运行（pnpm + Vite）

### 生产环境
- **所有服务**: Docker镜像运行
- **部署方式**: 使用项目中的Dockerfile文件构建镜像

## 💡 使用场景

### 场景1：完整开发
同时开发前后端时，选择"完整开发环境"。

### 场景2：仅开发后端
前端不需要修改时，选择"后端服务"。

### 场景3：仅开发前端
后端已在其他地方运行时，选择"前端服务"。

### 场景4：仅测试基础设施
需要测试数据库、缓存等服务时，选择"基础设施服务"。

## 🔧 故障排查

### Docker镜像拉取失败
**问题**: 无法从Docker Hub拉取镜像

**解决方案**:
运行镜像配置脚本：
- Windows: `windows\setup-docker-mirror.bat`
- Linux: `linux/setup-docker-mirror.sh`

该脚本会指导你配置国内Docker镜像源（中科大、网易、腾讯云等）。

### Docker未运行
**问题**: 提示"Docker未运行"

**解决方案**:
1. 启动Docker Desktop
2. 等待Docker完全启动（图标变绿）
3. 重新运行脚本

### 端口被占用
**问题**: 端口冲突

**解决方案**:
1. 检查是否有其他服务占用端口
2. 使用停止脚本停止所有服务
3. 重新启动

### 服务启动失败
**问题**: 服务无法启动

**解决方案**:
1. 查看Docker容器日志: `docker logs <容器名>`
2. 检查配置文件是否正确
3. 确保所有依赖服务已启动

### Maven构建失败
**问题**: 后端启动失败

**解决方案**:
1. 检查Java版本是否为17+
2. 清理Maven缓存: `mvn clean`
3. 检查网络连接（Maven需要下载依赖）

### 前端依赖安装失败
**问题**: pnpm install失败

**解决方案**:
1. 确保已安装pnpm: `npm install -g pnpm`
2. 删除 `frontend/node_modules` 目录
3. 删除 `frontend/pnpm-lock.yaml`
4. 重新运行脚本

## 📝 日志文件

### Windows
- 后端日志: 在启动的命令行窗口中查看
- 前端日志: 在启动的命令行窗口中查看
- Docker日志: `docker logs <容器名>`

### Linux
- 后端日志: `logs/backend.log`
- 前端日志: `logs/frontend.log`
- Docker日志: `docker logs <容器名>`

## 🛠️ 自定义配置

### 修改Docker服务配置
编辑文件: `docker/docker-compose.dev.yml`

### 修改后端配置
编辑文件: `backend/.env.dev`

### 修改前端配置
编辑文件: `frontend/.env` (如果存在)

## 📌 注意事项

1. 首次启动可能需要较长时间（下载Docker镜像、安装依赖等）
2. 停止服务时会保留数据，下次启动可以继续使用
3. 如需清理所有数据，请手动删除 `docker/` 目录下的数据文件夹
4. Linux系统需要确保脚本有执行权限
5. 建议使用入口脚本（start.bat/start.sh）进行日常开发管理

## 🆘 获取帮助

如遇到问题，请检查：
1. 所有前置软件是否正确安装
2. Docker Desktop是否正常运行
3. 端口是否被其他程序占用
4. 配置文件是否正确

更多信息请参考项目根目录的 `QUICK-START.md` 和 `DOCKER-GUIDE.md`。
