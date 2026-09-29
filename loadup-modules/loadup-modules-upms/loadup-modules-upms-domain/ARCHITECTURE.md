# Loadup Modules UPMS Domain Layer 架构

## 职责与边界

UPMS 领域模型与网关接口，不直接依赖 HTTP 或 ORM 实现。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`

## 实现入口

主要入口文件：

- [`AccessDecisionService`](src/main/java/io/github/loadup/modules/upms/domain/service/AccessDecisionService.java)
- [`DepartmentGateway`](src/main/java/io/github/loadup/modules/upms/domain/gateway/DepartmentGateway.java)
- [`LoginLogGateway`](src/main/java/io/github/loadup/modules/upms/domain/gateway/LoginLogGateway.java)
- [`PermissionGateway`](src/main/java/io/github/loadup/modules/upms/domain/gateway/PermissionGateway.java)
- [`RoleGateway`](src/main/java/io/github/loadup/modules/upms/domain/gateway/RoleGateway.java)
- [`UserGateway`](src/main/java/io/github/loadup/modules/upms/domain/gateway/UserGateway.java)
- [`UserOAuthBindingGateway`](src/main/java/io/github/loadup/modules/upms/domain/gateway/UserOAuthBindingGateway.java)
- [`UserPermissionService`](src/main/java/io/github/loadup/modules/upms/domain/service/UserPermissionService.java)
