#!/bin/bash

# 最终验证脚本 - Final Verification Script
# 用于验证修复是否成功

echo "======================================"
echo "调度器测试失败修复 - 最终验证"
echo "Scheduler Test Fix - Final Verification"
echo "======================================"
echo ""

BASE_DIR="/Users/lise/PersonalSpace/loadup-cloud/loadup-framework/components/loadup-components-scheduler"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

cd "$BASE_DIR" || exit 1

echo "步骤 1: 检查修改的文件..."
echo "Checking modified files..."
echo ""

REGISTRY_FILE="loadup-components-scheduler-api/src/main/java/com/github/loadup/components/scheduler/core/SchedulerTaskRegistry.java"

if [ -f "$REGISTRY_FILE" ]; then
    echo -e "${GREEN}✅ SchedulerTaskRegistry.java 存在${NC}"

    # 检查关键修改
    if grep -q "ApplicationListener<ContextRefreshedEvent>" "$REGISTRY_FILE"; then
        echo -e "${GREEN}✅ 已实现 ApplicationListener 接口${NC}"
    else
        echo -e "${RED}❌ 未实现 ApplicationListener 接口${NC}"
        exit 1
    fi

    if grep -q "PENDING_TASKS" "$REGISTRY_FILE"; then
        echo -e "${GREEN}✅ 已添加 PENDING_TASKS${NC}"
    else
        echo -e "${RED}❌ 未添加 PENDING_TASKS${NC}"
        exit 1
    fi

    if grep -q "onApplicationEvent" "$REGISTRY_FILE"; then
        echo -e "${GREEN}✅ 已实现 onApplicationEvent 方法${NC}"
    else
        echo -e "${RED}❌ 未实现 onApplicationEvent 方法${NC}"
        exit 1
    fi

    if grep -q "@Component" "$REGISTRY_FILE"; then
        echo -e "${RED}❌ 仍然有 @Component 注解（应该被移除）${NC}"
        exit 1
    else
        echo -e "${GREEN}✅ @Component 注解已移除${NC}"
    fi
else
    echo -e "${RED}❌ SchedulerTaskRegistry.java 文件不存在${NC}"
    exit 1
fi

echo ""
echo "步骤 2: 编译项目..."
echo "Compiling project..."
echo ""

mvn clean compile -DskipTests -q

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 编译成功${NC}"
else
    echo -e "${RED}❌ 编译失败${NC}"
    exit 1
fi

echo ""
echo "步骤 3: 运行失败的测试..."
echo "Running previously failed tests..."
echo ""

cd loadup-components-scheduler-test || exit 1

echo "3.1 运行 QuartzSchedulerIntegrationTest#testAnnotationBasedScheduling..."
mvn test -Dtest=QuartzSchedulerIntegrationTest#testAnnotationBasedScheduling -q

QUARTZ_RESULT=$?

echo ""
echo "3.2 运行 SimpleJobSchedulerIntegrationTest#testAnnotationBasedScheduling..."
mvn test -Dtest=SimpleJobSchedulerIntegrationTest#testAnnotationBasedScheduling -q

SIMPLEJOB_RESULT=$?

echo ""
echo "======================================"
echo "验证结果 - Verification Results"
echo "======================================"
echo ""

if [ $QUARTZ_RESULT -eq 0 ]; then
    echo -e "${GREEN}✅ Quartz 注解调度测试: 通过${NC}"
else
    echo -e "${RED}❌ Quartz 注解调度测试: 失败${NC}"
fi

if [ $SIMPLEJOB_RESULT -eq 0 ]; then
    echo -e "${GREEN}✅ SimpleJob 注解调度测试: 通过${NC}"
else
    echo -e "${RED}❌ SimpleJob 注解调度测试: 失败${NC}"
fi

echo ""

if [ $QUARTZ_RESULT -eq 0 ] && [ $SIMPLEJOB_RESULT -eq 0 ]; then
    echo -e "${GREEN}======================================"
    echo "🎉 所有测试通过！修复成功！"
    echo "🎉 All tests passed! Fix successful!"
    echo "======================================${NC}"
    exit 0
else
    echo -e "${YELLOW}======================================"
    echo "⚠️  有测试失败"
    echo "⚠️  Some tests failed"
    echo ""
    echo "请检查："
    echo "1. 查看上面的测试输出日志"
    echo "2. 运行 mvn test -X 查看详细日志"
    echo "3. 查看文档：修复快速参考.md"
    echo "======================================${NC}"
    exit 1
fi

