#!/bin/bash

echo "=========================================="
echo "智途云平台 - 服务状态"
echo "=========================================="
echo ""

cd "$(dirname "$0")/../docker"

echo "📊 服务运行状态："
echo ""
docker-compose ps

echo ""
echo "=========================================="
echo "💡 常用命令："
echo "  查看日志: cd docker && docker-compose logs -f [服务名]"
echo "  重启服务: cd docker && docker-compose restart [服务名]"
echo "  停止服务: cd docker && docker-compose stop [服务名]"
echo "=========================================="
