#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 停止所有服务"
echo "========================================"
echo ""

echo "[1/3] 停止前端服务..."
if [ -f "logs/frontend.pid" ]; then
    kill $(cat logs/frontend.pid) 2>/dev/null
    rm logs/frontend.pid
fi
echo "[✓] 前端服务已停止"
echo ""

echo "[2/3] 停止后端微服务..."
for svc in auth gateway system college enterprise student platform; do
    if [ -f "logs/${svc}.pid" ]; then
        kill $(cat logs/${svc}.pid) 2>/dev/null
        rm logs/${svc}.pid
        echo "[✓] ${svc} 已停止"
    fi
done
echo ""

echo "[3/3] 停止基础设施服务..."
cd docker
docker-compose -f docker-compose.dev.yml down
cd ..
echo "[✓] 基础设施服务已停止"
echo ""

echo "========================================"
echo "所有服务已停止"
echo "========================================"
echo ""
