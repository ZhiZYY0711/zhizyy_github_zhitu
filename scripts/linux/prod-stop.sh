#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 停止生产环境"
echo "========================================"
echo ""

cd docker
docker-compose down
cd ..
echo "[✓] 生产环境已停止"
echo ""
