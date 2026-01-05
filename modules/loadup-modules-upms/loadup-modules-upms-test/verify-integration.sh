#!/bin/bash

# UPMS TestContainers 集成验证脚本

echo "=========================================="
echo "UPMS TestContainers 集成验证"
echo "=========================================="
echo ""

# 检查 Docker
echo "1. 检查 Docker 状态..."
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker Desktop"
    exit 1
fi
echo "✅ Docker 运行正常"
echo ""

# 检查依赖
echo "2. 编译依赖模块..."
cd /Users/lise/PersonalSpace/loadup-cloud/loadup-framework
mvn clean install -pl components/loadup-components-testcontainers -am -DskipTests -q
if [ $? -ne 0 ]; then
    echo "❌ 依赖模块编译失败"
    exit 1
fi
echo "✅ 依赖模块编译成功"
echo ""

# 编译测试
echo "3. 编译 UPMS 测试模块..."
mvn clean test-compile -pl modules/loadup-modules-upms/loadup-modules-upms-test -am -q
if [ $? -ne 0 ]; then
    echo "❌ 测试模块编译失败"
    exit 1
fi
echo "✅ 测试模块编译成功"
echo ""

# 运行一个简单的测试
echo "4. 运行示例测试（BaseRepositoryTest）..."
mvn test -pl modules/loadup-modules-upms/loadup-modules-upms-test -Dtest=UserRepositoryTest#testSaveUser -q
if [ $? -ne 0 ]; then
    echo "❌ 测试执行失败"
    echo ""
    echo "可能的原因："
    echo "- Docker 内存不足"
    echo "- MySQL 镜像下载失败"
    echo ""
    echo "解决方案："
    echo "1. 增加 Docker Desktop 内存配置"
    echo "2. 手动拉取镜像: docker pull mysql:8.0"
    echo "3. 配置镜像加速器"
    exit 1
fi
echo "✅ 测试执行成功"
echo ""

echo "=========================================="
echo "✅ 集成验证完成！"
echo "=========================================="
echo ""
echo "下一步："
echo "1. 运行所有测试: mvn test -pl modules/loadup-modules-upms/loadup-modules-upms-test"
echo "2. 查看文档: cat modules/loadup-modules-upms/loadup-modules-upms-test/TESTCONTAINERS_INTEGRATION.md"
echo ""

