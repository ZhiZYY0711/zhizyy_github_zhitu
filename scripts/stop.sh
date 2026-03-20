#!/bin/bash

while true; do
    clear
    echo "========================================"
    echo "智途云平台 - 停止服务"
    echo "========================================"
    echo ""
    echo "【开发环境】"
    echo "  1. 停止所有开发服务"
    echo "  2. 仅停止基础设施"
    echo "  3. 仅停止后端"
    echo "  4. 仅停止前端"
    echo ""
    echo "【生产环境】"
    echo "  5. 停止生产环境"
    echo ""
    echo "  6. 查看服务状态"
    echo "  0. 退出"
    echo ""
    echo "========================================"
    read -p "请输入选项 (0-6): " choice

    case $choice in
        1) clear; bash linux/stop-all.sh; read -p "按Enter键继续..." ;;
        2) clear; bash linux/stop-infra.sh; read -p "按Enter键继续..." ;;
        3) clear; bash linux/stop-backend.sh; read -p "按Enter键继续..." ;;
        4) clear; bash linux/stop-frontend.sh; read -p "按Enter键继续..." ;;
        5) clear; bash linux/prod-stop.sh; read -p "按Enter键继续..." ;;
        6)
            clear
            echo "========================================"
            echo "服务状态"
            echo "========================================"
            echo ""
            docker ps --filter "name=zhitu-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
            echo ""
            read -p "按Enter键继续..."
            ;;
        0) exit 0 ;;
        *) echo "无效选项，请重新选择"; sleep 2 ;;
    esac
done
