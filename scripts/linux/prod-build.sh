#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 构建生产镜像"
echo "========================================"
echo ""

echo "检查Docker环境..."
if ! docker info > /dev/null 2>&1; then
    echo "[错误] Docker未运行，请先启动Docker"
    exit 1
fi
echo "[✓] Docker运行正常"
echo ""

echo "开始构建所有镜像 (首次构建耗时较长)..."
cd docker
docker-compose build --no-cache
if [ $? -ne 0 ]; then
    echo "[错误] 镜像构建失败，请检查Dockerfile和网络连接"
    exit 1
fi
cd ..
echo ""
echo "[✓] 所有镜像构建完成"
echo ""
