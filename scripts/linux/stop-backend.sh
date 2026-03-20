#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 停止后端服务"
echo "========================================"
echo ""

for svc in auth gateway system college enterprise student platform; do
    if [ -f "logs/${svc}.pid" ]; then
        kill $(cat logs/${svc}.pid) 2>/dev/null
        rm logs/${svc}.pid
        echo "[✓] ${svc} 已停止"
    fi
done
echo ""
