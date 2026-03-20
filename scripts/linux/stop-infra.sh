#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 停止基础设施服务"
echo "========================================"
echo ""

echo "停止基础设施服务..."
cd docker
docker-compose -f docker-compose.dev.yml down
cd ..
echo "[✓] 基础设施服务已停止"
echo ""
