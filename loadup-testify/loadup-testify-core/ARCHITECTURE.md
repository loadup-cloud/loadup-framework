# Testify Core 架构

## 职责与边界

Testify 测试框架的核心契约与执行基础。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `tools.jackson.core:jackson-databind`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
