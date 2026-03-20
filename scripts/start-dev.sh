#!/bin/bash

echo "=========================================="
echo "智途云平台 - 开发环境启动"
echo "=========================================="
echo ""
echo "⚠️  仅启动基础设施服务（数据库、缓存、注册中心等）"
echo "    后端和前端服务需要在 IDE 中手动启动"
echo ""

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

echo "✅ Docker 环境检查通过"
echo ""

# 进入 docker 目录
cd "$(dirname "$0")/../docker"

echo "🚀 启动基础设施服务..."
docker-compose -f docker-compose.dev.yml up -d

echo ""
echo "=========================================="
echo "✅ 基础设施服务已启动！"
echo "=========================================="
echo ""
echo "📋 服务访问地址："
echo "  PostgreSQL:      localhost:15432"
echo "  Redis:           localhost:6379"
echo "  Nacos 控制台:    http://localhost:8848/nacos"
echo "  MinIO 控制台:    http://localhost:9001"
echo ""
echo "🔑 默认账号密码："
echo "  Nacos:  nacos / nacos"
echo "  MinIO:  minioadmin / minioadmin"
echo ""
echo "💡 下一步："
echo "  1. 在 IDE 中启动后端服务"
echo "  2. 运行 'cd frontend && npm run dev' 启动前端"
echo ""
echo "🛑 停止服务: cd docker && docker-compose -f docker-compose.dev.yml down"
echo "=========================================="
