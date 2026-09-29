# Loadup Common DTO 架构

## 职责与边界

提供通用响应、分页、数据对象基类和 MapStruct 共享配置。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.fasterxml.jackson.core:jackson-annotations`
- `org.mapstruct:mapstruct`
- `com.mybatis-flex:mybatis-flex-annotation`
- `io.swagger.core.v3:swagger-annotations-jakarta`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
