# Loadup Modules UPMS App Layer 架构

## 职责与边界

UPMS 应用服务与业务编排。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-modules-upms-domain`
- `loadup-modules-upms-infrastructure`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.springframework.boot:spring-boot-starter`
- `org.springframework:spring-context`
- `org.springframework:spring-web`

## 实现入口

主要入口文件：

- [`UpmsAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/app/autoconfigure/UpmsAutoConfiguration.java)
- [`DepartmentService`](src/main/java/io/github/loadup/modules/upms/app/service/DepartmentService.java)
- [`PasswordResetService`](src/main/java/io/github/loadup/modules/upms/app/service/PasswordResetService.java)
- [`PermissionService`](src/main/java/io/github/loadup/modules/upms/app/service/PermissionService.java)
- [`RoleService`](src/main/java/io/github/loadup/modules/upms/app/service/RoleService.java)
- [`UserService`](src/main/java/io/github/loadup/modules/upms/app/service/UserService.java)
- [`VerificationCodeService`](src/main/java/io/github/loadup/modules/upms/app/service/VerificationCodeService.java)
- [`GitHubOAuthProvider`](src/main/java/io/github/loadup/modules/upms/app/strategy/oauth/GitHubOAuthProvider.java)

## 分层与调用路径

Web → App → Domain Gateway → Infrastructure；认证适配连接 UPMS 凭证校验与通用 AuthServer。

```text
Client Service → 应用服务编排 → 领域服务/Gateway → 基础设施实现
```

## 装配规则

- [`UpmsAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/app/autoconfigure/UpmsAutoConfiguration.java) 是自动配置入口。

## 扩展契约

- [`LoginStrategy`](src/main/java/io/github/loadup/modules/upms/app/strategy/LoginStrategy.java)：由实现方或调用方按接口定义对接。
- [`OAuthProvider`](src/main/java/io/github/loadup/modules/upms/app/strategy/oauth/OAuthProvider.java)：由实现方或调用方按接口定义对接。

## 配置归属

- [`UpmsSecurityProperties`](src/main/java/io/github/loadup/modules/upms/app/autoconfigure/UpmsSecurityProperties.java) 绑定 `loadup.upms.security`。

## 设计取舍

保持组件职责独立：业务调用依赖公开契约，自动配置处理框架装配，基础设施细节留在实现层。

集成方式与配置示例见 [README.md](README.md)。

## 可靠敏感访问

`UserSensitiveReadService` 不依赖具体审计实现，通过 `ObjectProvider<SensitiveReadAudit>` 支持普通 UPMS 无审计部署；明文操作必须有 recorder。顺序为参数校验 → 租户范围内目标查询 → 最新角色及资源数据范围判定 → 可靠审计 → MapStruct 输出映射。任一步失败不返回原值。禁止超级管理员短路或根据请求参数关闭审计。
