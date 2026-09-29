# Loadup Modules UPMS Infrastructure Layer 架构

## 职责与边界

UPMS 持久化对象、Mapper 和网关实现。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-modules-upms-domain`
- `loadup-components-database`
- `loadup-modules-upms-client`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.springframework.boot:spring-boot-starter-aspectj`
- `tools.jackson.core:jackson-databind`
- `org.mapstruct:mapstruct`
- `com.zaxxer:HikariCP`
- `org.springframework.data:spring-data-commons`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
