# LoadUp UPMS Web Adapter 架构

## 职责与边界

UPMS 的可选 Spring MVC Controller 适配模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-modules-upms-app`
- `loadup-components-authorization`
- `loadup-components-webmvc`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-webmvc`

## 实现入口

主要入口文件：

- [`UpmsWebAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/web/UpmsWebAutoConfiguration.java)
- [`AuthenticationController`](src/main/java/io/github/loadup/modules/upms/web/AuthenticationController.java)
- [`DepartmentController`](src/main/java/io/github/loadup/modules/upms/web/DepartmentController.java)
- [`PermissionController`](src/main/java/io/github/loadup/modules/upms/web/PermissionController.java)
- [`RoleController`](src/main/java/io/github/loadup/modules/upms/web/RoleController.java)
- [`UserController`](src/main/java/io/github/loadup/modules/upms/web/UserController.java)
- [`AccountSecurityController`](src/main/java/io/github/loadup/modules/upms/web/AccountSecurityController.java)

## 分层与调用路径

Web → App → Domain Gateway → Infrastructure；认证适配连接 UPMS 凭证校验与通用 AuthServer。

```text
HTTP /api/** → Controller → App Service → Client DTO 响应
```

账号安全 Controller 只接受旧密码、新密码和确认值，用户 ID 取自 Resource Server 验证后的 `LoadUpUser`。登录记录查询按该 ID 过滤；管理员用户管理 API 与本人安全 API 分开授权。

## 装配规则

- [`UpmsWebAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/web/UpmsWebAutoConfiguration.java) 是自动配置入口。

## 设计取舍

保持组件职责独立：业务调用依赖公开契约，自动配置处理框架装配，基础设施细节留在实现层。

集成方式与配置示例见 [README.md](./README.md)。

## 可选审计桥接

`UserSensitiveController` 使用方法权限和认证主体调用 app；`SensitiveReadAuditConfiguration` 在可选 AuditService 存在时装配默认 recorder。自动配置顺序在 UPMS app 和审计模块之后。默认 recorder 的独立事务先提交审计元数据，异常不吞掉；无 audit 依赖时不装配默认 recorder，app 对明文访问默认拒绝。
