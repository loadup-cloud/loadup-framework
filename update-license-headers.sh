#!/bin/bash

##############################################################################
# LoadUp Framework - License Header Update Script
#
# 此脚本遍历所有Maven模块并使用org.codehaus.mojo:license-maven-plugin更新文件头
#
# 使用方法:
#   ./update-license-headers.sh [选项]
#
# 选项:
#   -h, --help              显示帮助信息
#   -m, --module <module>   仅更新指定模块
#   -d, --dry-run          干运行模式，不实际修改文件
#   -v, --verbose          详细输出模式
#   -c, --check            检查模式，仅检查license是否缺失
#   -f, --format           格式化模式，移除所有文件的license头
#   --skip-tests           跳过test目录
#
# 示例:
#   ./update-license-headers.sh                    # 更新所有模块
#   ./update-license-headers.sh -m commons         # 仅更新commons目录
#   ./update-license-headers.sh --check            # 仅检查
#   ./update-license-headers.sh -v -f              # 详细模式 + 移除license头
##############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 默认配置
VERBOSE=false
DRY_RUN=false
CHECK_MODE=false
FORMAT_MODE=false
SKIP_TESTS=false
TARGET_MODULE=""
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# 统计信息
TOTAL_MODULES=0
SUCCESS_MODULES=0
FAILED_MODULES=0
SKIPPED_MODULES=0

# 帮助信息
show_help() {
    cat << EOF
LoadUp Framework - License Header Update Script

使用方法: $(basename "$0") [选项]

选项:
  -h, --help              显示此帮助信息
  -m, --module <module>   仅更新指定模块（例如: commons, components, bom）
  -d, --dry-run          干运行模式，显示将要执行的操作但不实际修改
  -v, --verbose          详细输出模式
  -c, --check            检查模式，验证license头是否存在
  -f, --format           格式化模式，移除所有文件的license头（用于重新生成）
  --skip-tests           跳过test目录的文件

示例:
  $(basename "$0")                          # 更新所有模块的license头
  $(basename "$0") -m commons               # 仅更新commons目录下的模块
  $(basename "$0") -m components/loadup-components-scheduler  # 更新特定模块
  $(basename "$0") --check                  # 检查所有模块的license头
  $(basename "$0") -v -f                    # 详细模式下移除所有license头
  $(basename "$0") -d -m bom                # 干运行模式预览bom模块的更新

注意:
  - 需要在项目根目录下运行此脚本
  - 确保已在pom.xml中配置了org.codehaus.mojo:license-maven-plugin
  - 默认使用maven的license:update-file-header目标更新license头
  - 格式化模式(-f)会移除现有license头，通常用于清理后重新生成

EOF
}

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_verbose() {
    if [ "$VERBOSE" = true ]; then
        echo -e "${CYAN}[VERBOSE]${NC} $1"
    fi
}

# 解析命令行参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -m|--module)
                TARGET_MODULE="$2"
                shift 2
                ;;
            -d|--dry-run)
                DRY_RUN=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -c|--check)
                CHECK_MODE=true
                shift
                ;;
            -f|--format)
                FORMAT_MODE=true
                shift
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            *)
                log_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 检查Maven是否安装
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或不在PATH中"
        exit 1
    fi
    log_verbose "Maven版本: $(mvn -version | head -n 1)"
}

# 检查是否在项目根目录
check_project_root() {
    if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
        log_error "未找到pom.xml文件，请确保在项目根目录下运行脚本"
        exit 1
    fi
    log_verbose "项目根目录: $PROJECT_ROOT"
}

# 递归获取模块及其子模块
get_submodules() {
    local parent_path="$1"
    local pom_file="$PROJECT_ROOT/$parent_path/pom.xml"
    local submodules=()

    if [ ! -f "$pom_file" ]; then
        return
    fi

    # 读取当前pom.xml中的模块
    while IFS= read -r line; do
        if [[ $line =~ \<module\>(.*)\</module\> ]]; then
            local module="${BASH_REMATCH[1]}"
            # 跳过注释的模块
            if [[ ! $line =~ ^[[:space:]]*\<\!-- ]]; then
                if [ "$parent_path" = "." ]; then
                    submodules+=("$module")
                else
                    submodules+=("$parent_path/$module")
                fi
            fi
        fi
    done < "$pom_file"

    # 输出找到的子模块信息到 stderr
    if [ ${#submodules[@]} -gt 0 ]; then
        if [ "$VERBOSE" = true ]; then
            echo -e "${CYAN}[VERBOSE]${NC} 在 $parent_path 中找到 ${#submodules[@]} 个子模块" >&2
        fi
    fi

    echo "${submodules[@]}"
}

# 获取所有模块（包括嵌套的子模块）
get_modules() {
    local modules=()
    local to_process=()

    if [ -n "$TARGET_MODULE" ]; then
        # 指定了目标模块
        if [ -d "$PROJECT_ROOT/$TARGET_MODULE" ]; then
            to_process+=("$TARGET_MODULE")
        else
            log_error "模块不存在: $TARGET_MODULE"
            exit 1
        fi
    else
        # 从根项目开始
        to_process+=(".")
    fi

    # 使用广度优先搜索递归处理所有模块
    while [ ${#to_process[@]} -gt 0 ]; do
        local current="${to_process[0]}"
        to_process=("${to_process[@]:1}")

        # 添加当前模块
        modules+=("$current")

        # 查找子模块
        local submodules=($(get_submodules "$current"))
        if [ ${#submodules[@]} -gt 0 ]; then
            to_process+=("${submodules[@]}")
        fi
    done

    echo "${modules[@]}"
}

# 构建Maven命令
build_maven_command() {
    local cmd="mvn"

    if [ "$CHECK_MODE" = true ]; then
        cmd="$cmd license:check-file-header"
    elif [ "$FORMAT_MODE" = true ]; then
        cmd="$cmd license:update-file-header"
    else
        cmd="$cmd license:update-file-header"  # 默认使用update-file-header
    fi

    # 添加通用选项
    cmd="$cmd -N"  # 不递归子模块

    if [ "$SKIP_TESTS" = true ]; then
        cmd="$cmd -Dlicense.excludes=**/src/test/**"
    fi

    if [ "$VERBOSE" = false ]; then
        cmd="$cmd -q"  # 静默模式
    fi

    echo "$cmd"
}

# 更新单个模块的license
update_module_license() {
    local module_path="$1"
    local module_name=$(basename "$module_path")

    TOTAL_MODULES=$((TOTAL_MODULES + 1))

    # 检查模块是否有pom.xml
    if [ ! -f "$PROJECT_ROOT/$module_path/pom.xml" ]; then
        log_verbose "跳过 $module_path (无pom.xml)"
        SKIPPED_MODULES=$((SKIPPED_MODULES + 1))
        return
    fi

    log_info "处理模块: $module_path"

    # 构建Maven命令
    local maven_cmd=$(build_maven_command)

    # 干运行模式
    if [ "$DRY_RUN" = true ]; then
        log_warning "[DRY RUN] 将执行: cd $module_path && $maven_cmd"
        SUCCESS_MODULES=$((SUCCESS_MODULES + 1))
        return
    fi

    # 切换到模块目录并执行
    cd "$PROJECT_ROOT/$module_path"

    log_verbose "执行命令: $maven_cmd"

    if $maven_cmd; then
        log_success "✓ $module_name 处理完成"
        SUCCESS_MODULES=$((SUCCESS_MODULES + 1))
    else
        log_error "✗ $module_name 处理失败"
        FAILED_MODULES=$((FAILED_MODULES + 1))
    fi

    # 返回项目根目录
    cd "$PROJECT_ROOT"
}

# 打印统计信息
print_summary() {
    echo ""
    echo "=========================================="
    echo "License更新统计"
    echo "=========================================="
    echo -e "总模块数:     ${BLUE}$TOTAL_MODULES${NC}"
    echo -e "成功处理:     ${GREEN}$SUCCESS_MODULES${NC}"
    echo -e "处理失败:     ${RED}$FAILED_MODULES${NC}"
    echo -e "跳过模块:     ${YELLOW}$SKIPPED_MODULES${NC}"
    echo "=========================================="

    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}注意: 这是干运行模式，未实际修改文件${NC}"
    fi

    if [ $FAILED_MODULES -gt 0 ]; then
        echo -e "${RED}存在失败的模块，请检查日志${NC}"
        exit 1
    else
        echo -e "${GREEN}所有模块处理成功！${NC}"
    fi
}

# 主函数
main() {
    # 解析参数
    parse_args "$@"

    # 打印banner
    echo "=========================================="
    echo "LoadUp Framework - License Header Update"
    echo "=========================================="
    echo ""

    # 检查环境
    check_maven
    check_project_root

    # 显示模式信息
    if [ "$CHECK_MODE" = true ]; then
        log_info "运行模式: 检查模式"
    elif [ "$FORMAT_MODE" = true ]; then
        log_info "运行模式: 格式化模式"
    else
        log_info "运行模式: 标准更新模式"
    fi

    if [ "$DRY_RUN" = true ]; then
        log_warning "干运行模式已启用"
    fi

    if [ "$SKIP_TESTS" = true ]; then
        log_info "将跳过test目录"
    fi

    if [ -n "$TARGET_MODULE" ]; then
        log_info "目标模块: $TARGET_MODULE"
    fi

    echo ""

    # 获取所有模块
    local modules=($(get_modules))
    log_info "找到 ${#modules[@]} 个模块"
    echo ""

    # 处理每个模块
    for module in "${modules[@]}"; do
        update_module_license "$module"
    done

    # 打印统计
    print_summary
}

# 运行主函数
main "$@"

