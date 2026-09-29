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
