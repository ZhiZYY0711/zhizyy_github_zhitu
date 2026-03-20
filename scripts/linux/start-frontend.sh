#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 启动前端服务"
echo "========================================"
echo ""

# 检查依赖
echo "[1/2] 检查依赖..."
if [ ! -d "frontend/node_modules" ]; then
    echo "[提示] 未找到node_modules，正在安装依赖..."
    cd frontend
    pnpm install
    cd ..
fi
echo "[✓] 依赖检查完成"
echo ""

# 启动前端
echo "[2/2] 启动前端服务..."
cd frontend
nohup pnpm run dev > ../logs/frontend.log 2>&1 &
echo $! > ../logs/frontend.pid
cd ..
echo "[✓] 前端服务启动中..."
echo ""

echo "========================================"
echo "前端服务启动完成！"
echo "========================================"
echo ""
