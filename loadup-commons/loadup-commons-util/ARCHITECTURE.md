# Loadup Common Utils 架构

## 职责与边界

提供 JSON、日期、字符串等通用工具。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-commons-log`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.apache.commons:commons-collections4`
- `com.google.guava:guava`
- `org.apache.commons:commons-text`
- `org.apache.commons:commons-pool2`
- `commons-io:commons-io`
- `org.springframework.security:spring-security-crypto`
- `org.springframework:spring-context`
- `tools.jackson.core:jackson-databind`
- `tools.jackson.core:jackson-core`
- `com.fasterxml.jackson.core:jackson-annotations`
- `org.springframework.boot:spring-boot-starter-validation`
- 另有 4 项，详见 `pom.xml`。

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
