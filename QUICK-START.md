# 智途云平台 - 快速启动指南

## 🚀 一键启动

### Windows
```bash
cd scripts
start.bat
```

### Linux/Mac
```bash
cd scripts
chmod +x *.sh
./start.sh
```

等待 5-10 分钟（首次需要构建镜像），然后访问 http://localhost

---

## 📋 服务地址

| 服务 | 地址 | 账号 | 密码 |
|------|------|------|------|
| 前端 | http://localhost | - | - |
| API 网关 | http://localhost:8888 | - | - |
| Nacos | http://localhost:8848/nacos | nacos | nacos |
| MinIO | http://localhost:9001 | minioadmin | minioadmin |

---

## 🛠️ 两种启动模式

### 1. 完整部署（生产/演示）

启动所有服务，包括前后端。

**Windows:** `cd scripts && start.bat`  
**Linux/Mac:** `cd scripts && ./start.sh`

### 2. 开发模式（推荐开发）

仅启动基础设施（PostgreSQL, Redis, Nacos, MinIO），代码在 IDE 中运行。

**Windows:** `cd scripts && start-dev.bat`  
**Linux/Mac:** `cd scripts && ./start-dev.sh`

然后：
1. IDE 中启动需要的后端服务
2. `cd frontend && npm run dev` 启动前端

---

## 📊 管理命令

### 查看服务状态
```bash
cd scripts
./status.sh      # Linux/Mac
status.bat       # Windows
```

### 健康检查
```bash
cd scripts
./test-services.sh      # Linux/Mac
test-services.bat       # Windows
```

### 停止服务
```bash
cd scripts
./stop.sh        # Linux/Mac
stop.bat         # Windows
```

### 查看日志
```bash
cd docker
docker-compose logs -f [服务名]

# 示例
docker-compose logs -f gateway
docker-compose logs -f auth
```

### 重启服务
```bash
cd docker
docker-compose restart [服务名]
```

### 重新构建
```bash
cd docker
docker-compose up -d --build [服务名]
```

---

## 🐳 Docker 镜像加速（可选）

如果镜像下载缓慢，配置 Docker 镜像加速器：

### Windows/Mac (Docker Desktop)

1. 打开 Docker Desktop
2. Settings → Docker Engine
3. 添加以下配置：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```

4. Apply & Restart

### Linux

编辑 `/etc/docker/daemon.json`：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```

重启 Docker：
```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

---

## 💡 常见问题

### Q: Docker 镜像拉取失败？

**错误信息：**
```
failed to fetch oauth token: Post "https://auth.docker.io/token": dial tcp: connectex...
```

**解决方案：**

运行镜像加速配置脚本：
```bash
cd scripts
./setup-docker-mirror.sh      # Linux/Mac
setup-docker-mirror.bat        # Windows
```

或手动配置：

**Windows/Mac (Docker Desktop):**
1. 打开 Docker Desktop
2. Settings → Docker Engine
3. 添加配置：
```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```
4. Apply & Restart

**Linux:**
```bash
sudo nano /etc/docker/daemon.json
# 添加上述配置
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### Q: 端口被占用？

修改 `docker/docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "新端口:容器端口"
```

### Q: 服务启动失败？

1. 查看日志：`cd docker && docker-compose logs [服务名]`
2. 检查 Docker 资源：确保至少 8GB 内存
3. 重启服务：`cd docker && docker-compose restart [服务名]`

### Q: Nacos 注册失败？

等待 Nacos 完全启动（约 1-2 分钟），然后重启后端服务：
```bash
cd docker
docker-compose restart auth system college enterprise student platform gateway
```

### Q: 如何完全清理？

```bash
cd docker
docker-compose down -v
rm -rf postgres/data redis/data nacos/logs minio/data
```

---

## 📦 服务架构

```
用户浏览器
    ↓
Frontend (Vite Preview) :80
    ↓
Gateway (Spring Cloud) :8888
    ↓
┌────────┬────────┬────────┬────────┐
Auth    System  College  Enterprise ...
:9200   :9201   :9202    :9203
    ↓
┌──────────┬────────┬────────┬────────┐
PostgreSQL  Redis   Nacos    MinIO
:15432     :6379   :8848    :9000
```

---

## 🎯 推荐工作流

### 首次使用
```bash
cd scripts
./start.sh              # 启动所有服务
./test-services.sh      # 验证服务状态
```

### 日常开发
```bash
cd scripts
./start-dev.sh          # 启动基础设施
# IDE 中启动后端服务
cd ../frontend && npm run dev
```

### 测试部署
```bash
cd scripts
./start.sh              # 完整部署
./test-services.sh      # 健康检查
```

---

## 📝 技术栈

**后端：**
- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.2
- PostgreSQL 15
- Redis 7.0
- Nacos 2.5.1

**前端：**
- React 19
- TypeScript
- Vite 7
- TailwindCSS 4

---

## 🔗 相关链接

- [项目主页](../README.md)
- [环境配置说明](../backend/环境配置说明.md)
- [开发任务清单](../backend/开发任务清单.md)

---

**需要帮助？** 提交 Issue 或查看项目文档
