#!/bin/bash

# 智途平台数据库部署脚本
# 用法: ./deploy.sh [init|upgrade|verify]

set -e

# 配置
DB_USER="zhitu_user"
DB_NAME="zhitu_cloud"
DB_HOST="localhost"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查PostgreSQL连接
check_connection() {
    log_info "检查数据库连接..."
    if psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -c "SELECT 1" > /dev/null 2>&1; then
        log_info "数据库连接成功"
        return 0
    else
        log_error "数据库连接失败，请检查配置"
        return 1
    fi
}

# 初始化数据库
init_database() {
    log_info "开始初始化数据库..."
    
    cd v1.0.0-init
    
    # 执行schema文件
    for file in 0*.sql; do
        log_info "执行: $file"
        psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -f "$file"
    done
    
    # 询问是否导入测试数据
    read -p "是否导入测试数据? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "导入测试数据..."
        psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -f test_data.sql
    fi
    
    cd ..
    log_info "数据库初始化完成"
}

# 升级数据库
upgrade_database() {
    log_info "开始升级数据库..."
    
    # v1.1.0
    log_info "应用 v1.1.0 迁移..."
    psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -f v1.1.0-missing-api/upgrade.sql
    
    # v1.2.0
    log_info "应用 v1.2.0 迁移..."
    psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -f v1.2.0-talent-pool/upgrade.sql
    
    # v1.3.0
    log_info "应用 v1.3.0 迁移..."
    psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -f v1.3.0-growth-fix/upgrade.sql
    
    log_info "数据库升级完成"
}

# 验证数据库
verify_database() {
    log_info "开始验证数据库..."
    
    # 检查schema
    log_info "检查schema..."
    psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -c "
        SELECT schema_name FROM information_schema.schemata 
        WHERE schema_name LIKE '%svc' OR schema_name IN ('auth_center', 'platform_service');
    "
    
    # 检查表数量
    log_info "检查表数量..."
    psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -c "
        SELECT 
            table_schema, 
            COUNT(*) as table_count 
        FROM information_schema.tables 
        WHERE table_schema IN ('auth_center', 'platform_service', 'student_svc', 
                               'college_svc', 'enterprise_svc', 'internship_svc', 
                               'training_svc', 'growth_svc')
        GROUP BY table_schema;
    "
    
    # 执行v1.3.0验证脚本
    log_info "执行v1.3.0验证..."
    psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -f v1.3.0-growth-fix/verify.sql
    
    log_info "数据库验证完成"
}

# 显示帮助
show_help() {
    echo "智途平台数据库部署脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  init      - 初始化数据库（全新安装）"
    echo "  upgrade   - 升级数据库（应用所有迁移）"
    echo "  verify    - 验证数据库状态"
    echo "  help      - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 init      # 全新安装"
    echo "  $0 upgrade   # 升级到最新版本"
    echo "  $0 verify    # 验证数据库"
}

# 主函数
main() {
    case "$1" in
        init)
            check_connection && init_database && upgrade_database
            ;;
        upgrade)
            check_connection && upgrade_database
            ;;
        verify)
            check_connection && verify_database
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
