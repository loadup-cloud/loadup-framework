# LoadUp Components Authorization 架构

## 职责与边界

提供Spring Security 方法级授权能力的独立模块。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework:spring-context`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`
- `com.github.spotbugs:spotbugs-annotations`

## 实现入口

主要入口文件：

- [`AuthorizationAutoConfiguration`](src/main/java/io/github/loadup/components/authorization/config/AuthorizationAutoConfiguration.java)

## 分层与调用路径

```text
SecurityContext → Spring 方法安全拦截器 → @PreAuthorize / @Secured → 业务方法
```

## 装配规则

- [`AuthorizationAutoConfiguration`](src/main/java/io/github/loadup/components/authorization/config/AuthorizationAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnProperty( prefix = "loadup.security.method-security", name = "enabled", havingValue = "true", matchIfMissing = true)`

## 配置归属

- [`AuthorizationProperties`](src/main/java/io/github/loadup/components/authorization/AuthorizationProperties.java) 绑定 `loadup.security.method-security`。

集成方式与配置示例见 [README.md](./README.md)。
