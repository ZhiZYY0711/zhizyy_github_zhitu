# 脚本说明

本目录包含项目的管理脚本。

## 📁 脚本列表

### 启动脚本

| 脚本 | 说明 |
|------|------|
| `start.bat` / `start.sh` | 完整启动（所有服务） |
| `start-dev.bat` / `start-dev.sh` | 开发模式（仅基础设施） |

### 管理脚本

| 脚本 | 说明 |
|------|------|
| `stop.bat` / `stop.sh` | 停止所有服务 |
| `status.bat` / `status.sh` | 查看服务状态 |
| `test-services.bat` / `test-services.sh` | 健康检查 |

### 配置脚本

| 脚本 | 说明 |
|------|------|
| `setup-docker-mirror.bat` / `setup-docker-mirror.sh` | Docker 镜像加速配置指南 |

## 🚀 使用方法

### Windows
直接双击 `.bat` 文件或在命令行运行：
```cmd
start.bat
```

### Linux/Mac
首次使用添加执行权限：
```bash
chmod +x *.sh
```

然后运行：
```bash
./start.sh
```

## ⚠️ 常见问题

### Docker 镜像拉取失败

如果看到类似错误：
```
failed to fetch oauth token: Post "https://auth.docker.io/token"
```

运行镜像加速配置脚本：
```bash
./setup-docker-mirror.sh      # Linux/Mac
setup-docker-mirror.bat        # Windows
```

### Windows 中文乱码

所有脚本已使用英文输出，避免编码问题。

## 📖 详细文档

查看 [快速启动指南](../QUICK-START.md) 获取完整说明。
