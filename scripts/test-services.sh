#!/bin/bash

echo "=========================================="
echo "智途云平台 - 服务健康检查"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查服务函数
check_service() {
    local name=$1
    local url=$2
    
    echo -n "检查 $name ... "
    
    if curl -s -f -o /dev/null "$url"; then
        echo -e "${GREEN}✓ 正常${NC}"
        return 0
    else
        echo -e "${RED}✗ 异常${NC}"
        return 1
    fi
}

echo "🔍 开始检查服务状态..."
echo ""

# 检查基础设施
echo "【基础设施服务】"
check_service "Nacos      " "http://localhost:8848/nacos"
check_service "MinIO      " "http://localhost:9001"
echo ""

# 检查后端服务
echo "【后端微服务】"
check_service "Gateway    " "http://localhost:8888/actuator/health"
check_service "Auth       " "http://localhost:9200/actuator/health"
check_service "System     " "http://localhost:9201/actuator/health"
check_service "College    " "http://localhost:9202/actuator/health"
check_service "Enterprise " "http://localhost:9203/actuator/health"
check_service "Student    " "http://localhost:9204/actuator/health"
check_service "Platform   " "http://localhost:9205/actuator/health"
echo ""

# 检查前端
echo "【前端服务】"
check_service "Frontend   " "http://localhost"
echo ""

echo "=========================================="
echo "✅ 健康检查完成"
echo "=========================================="
