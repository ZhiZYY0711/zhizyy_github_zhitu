#!/bin/bash

while true; do
    clear
    echo "========================================"
    echo "智途云平台 - 启动服务"
    echo "========================================"
    echo ""
    echo "【开发环境】(基础设施Docker + 宿主机前后端)"
    echo "  1. 完整开发环境 (基础设施 + 后端 + 前端)"
    echo "  2. 仅基础设施服务"
    echo "  3. 仅后端服务 (含基础设施)"
    echo "  4. 仅前端服务"
    echo ""
    echo "【生产环境】(全部Docker镜像)"
    echo "  5. 启动生产环境"
    echo "  6. 构建生产镜像 (重新构建所有镜像)"
    echo ""
    echo "  0. 退出"
    echo ""
    echo "========================================"
    read -p "请输入选项 (0-6): " choice

    case $choice in
        1) clear; bash linux/start-all.sh; read -p "按Enter键继续..." ;;
        2) clear; bash linux/start-infra.sh; read -p "按Enter键继续..." ;;
        3) clear; bash linux/start-backend.sh; read -p "按Enter键继续..." ;;
        4) clear; bash linux/start-frontend.sh; read -p "按Enter键继续..." ;;
        5) clear; bash linux/prod-start.sh; read -p "按Enter键继续..." ;;
        6) clear; bash linux/prod-build.sh; read -p "按Enter键继续..." ;;
        0) exit 0 ;;
        *) echo "无效选项，请重新选择"; sleep 2 ;;
    esac
done
