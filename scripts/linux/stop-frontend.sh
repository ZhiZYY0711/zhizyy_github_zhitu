#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 停止前端服务"
echo "========================================"
echo ""

echo "停止前端服务..."
if [ -f "logs/frontend.pid" ]; then
    kill $(cat logs/frontend.pid) 2>/dev/null
    rm logs/frontend.pid
    echo "[✓] 前端服务已停止"
else
    echo "[提示] 未找到前端进程"
fi
echo ""
