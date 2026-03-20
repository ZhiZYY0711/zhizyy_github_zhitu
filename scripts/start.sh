#!/bin/bash

echo "=========================================="
echo "智途云平台 - 一键启动脚本"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装"
    exit 1
fi

echo "✅ Docker 环境检查通过"
echo ""

# 进入 docker 目录
cd "$(dirname "$0")/../docker"

echo "🚀 开始构建和启动所有服务..."
echo ""

# 构建并启动所有服务
docker-compose up -d --build

echo ""
echo "=========================================="
echo "✅ 所有服务已启动！"
echo "=========================================="
echo ""
echo "📋 服务访问地址："
echo "  前端应用:        http://localhost"
echo "  API 网关:        http://localhost:8888"
echo "  Nacos 控制台:    http://localhost:8848/nacos"
echo "  MinIO 控制台:    http://localhost:9001"
echo ""
echo "🔑 默认账号密码："
echo "  Nacos:  nacos / nacos"
echo "  MinIO:  minioadmin / minioadmin"
echo ""
echo "📊 查看服务状态: cd docker && docker-compose ps"
echo "📝 查看服务日志: cd docker && docker-compose logs -f [服务名]"
echo "🛑 停止所有服务: cd docker && docker-compose down"
echo ""
echo "⏳ 提示：首次启动需要构建镜像，请等待 5-10 分钟"
echo "   后端服务需要等待 Nacos 完全启动后才能正常注册"
echo "=========================================="
