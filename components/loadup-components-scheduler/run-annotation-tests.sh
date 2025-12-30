#!/bin/bash

echo "=================================="
echo "测试注解驱动的调度任务"
echo "=================================="
echo ""

cd /Users/lise/PersonalSpace/loadup-cloud/loadup-framework/components/loadup-components-scheduler

echo "步骤1: 编译项目..."
mvn clean compile -DskipTests -pl loadup-components-scheduler-api,loadup-components-scheduler-binder-quartz,loadup-components-scheduler-binder-simplejob,loadup-components-scheduler-test

if [ $? -ne 0 ]; then
    echo "❌ 编译失败！"
    exit 1
fi

echo "✅ 编译成功！"
echo ""

cd loadup-components-scheduler-test

echo "步骤2: 运行 Quartz 注解调度测试..."
mvn test -Dtest=QuartzSchedulerIntegrationTest#testAnnotationBasedScheduling -DfailIfNoTests=false

QUARTZ_RESULT=$?

echo ""
echo "步骤3: 运行 SimpleJob 注解调度测试..."
mvn test -Dtest=SimpleJobSchedulerIntegrationTest#testAnnotationBasedScheduling -DfailIfNoTests=false

SIMPLEJOB_RESULT=$?

echo ""
echo "=================================="
echo "测试结果汇总"
echo "=================================="

if [ $QUARTZ_RESULT -eq 0 ]; then
    echo "✅ Quartz 注解调度测试: 通过"
else
    echo "❌ Quartz 注解调度测试: 失败"
fi

if [ $SIMPLEJOB_RESULT -eq 0 ]; then
    echo "✅ SimpleJob 注解调度测试: 通过"
else
    echo "❌ SimpleJob 注解调度测试: 失败"
fi

echo ""

if [ $QUARTZ_RESULT -eq 0 ] && [ $SIMPLEJOB_RESULT -eq 0 ]; then
    echo "🎉 所有测试通过！"
    exit 0
else
    echo "⚠️  有测试失败，请检查日志"
    exit 1
fi

