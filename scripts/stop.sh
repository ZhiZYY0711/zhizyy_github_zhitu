#!/bin/bash

echo "=========================================="
echo "智途云平台 - 停止所有服务"
echo "=========================================="

cd "$(dirname "$0")/../docker"

echo "🛑 正在停止所有服务..."
docker-compose down

echo ""
echo "✅ 所有服务已停止"
echo ""
echo "💡 提示："
echo "  重新启动: cd scripts && ./start.sh"
echo "  删除数据: cd docker && docker-compose down -v"
echo "=========================================="
