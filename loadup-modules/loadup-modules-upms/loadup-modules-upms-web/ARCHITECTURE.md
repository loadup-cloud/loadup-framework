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
