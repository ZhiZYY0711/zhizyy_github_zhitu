#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "智途云平台 - 启动后端服务"
echo "========================================"
echo ""

mkdir -p logs

echo "启动各微服务..."
echo ""

BASE_DIR=$(pwd)

echo "启动 Auth 服务 (8081)..."
cd "$BASE_DIR/backend/zhitu-auth"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/auth.log" 2>&1 &
echo $! > "$BASE_DIR/logs/auth.pid"

sleep 3

echo "启动 Gateway 服务 (8080)..."
cd "$BASE_DIR/backend/zhitu-gateway"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/gateway.log" 2>&1 &
echo $! > "$BASE_DIR/logs/gateway.pid"

echo "启动 System 服务 (8082)..."
cd "$BASE_DIR/backend/zhitu-modules/zhitu-system"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/system.log" 2>&1 &
echo $! > "$BASE_DIR/logs/system.pid"

echo "启动 College 服务 (8083)..."
cd "$BASE_DIR/backend/zhitu-modules/zhitu-college"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/college.log" 2>&1 &
echo $! > "$BASE_DIR/logs/college.pid"

echo "启动 Enterprise 服务 (8084)..."
cd "$BASE_DIR/backend/zhitu-modules/zhitu-enterprise"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/enterprise.log" 2>&1 &
echo $! > "$BASE_DIR/logs/enterprise.pid"

echo "启动 Student 服务 (8085)..."
cd "$BASE_DIR/backend/zhitu-modules/zhitu-student"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/student.log" 2>&1 &
echo $! > "$BASE_DIR/logs/student.pid"

echo "启动 Platform 服务 (8086)..."
cd "$BASE_DIR/backend/zhitu-modules/zhitu-platform"
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/platform.log" 2>&1 &
echo $! > "$BASE_DIR/logs/platform.pid"

cd "$BASE_DIR"
echo ""
echo "[✓] 所有微服务启动中..."
echo ""
echo "========================================"
echo "服务端口:"
echo "  - Gateway:    http://localhost:8888"
echo "  - Auth:       http://localhost:8081"
echo "  - System:     http://localhost:8082"
echo "  - College:    http://localhost:8093"
echo "  - Enterprise: http://localhost:8084"
echo "  - Student:    http://localhost:8085"
echo "  - Platform:   http://localhost:8086"
echo "========================================"
echo ""
echo "日志文件: logs/<服务名>.log"
echo ""
