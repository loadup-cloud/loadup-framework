# Testify Data Engine 架构

## 职责与边界

Testify 测试框架的执行引擎模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-testify-core`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework:spring-expression`
- `net.datafaker:datafaker`
- `tools.jackson.core:jackson-databind`
- `org.springframework:spring-context`
- `org.slf4j:slf4j-api`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
