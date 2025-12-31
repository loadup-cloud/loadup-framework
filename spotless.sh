#!/bin/bash
#
# Spotless 格式化命令的快捷方式
# 使用方法:
#   ./spotless.sh apply   - 格式化所有代码
#   ./spotless.sh check   - 检查代码格式
#

PLUGIN_COORDINATES="com.diffplug.spotless:spotless-maven-plugin:3.1.0"

case "$1" in
    apply)
        echo "正在格式化代码..."
        shift  # 移除第一个参数 (apply)
        mvn ${PLUGIN_COORDINATES}:apply "$@"
        ;;
    check)
        echo "正在检查代码格式..."
        shift  # 移除第一个参数 (check)
        mvn ${PLUGIN_COORDINATES}:check "$@"
        ;;
    *)
        echo "使用方法: $0 {apply|check} [Maven参数]"
        echo ""
        echo "示例:"
        echo "  $0 apply              # 格式化所有代码"
        echo "  $0 check              # 检查代码格式"
        echo "  $0 apply -pl commons  # 格式化特定模块"
        echo ""
        exit 1
        ;;
esac

