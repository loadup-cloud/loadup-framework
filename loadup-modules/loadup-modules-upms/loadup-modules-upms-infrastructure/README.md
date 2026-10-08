# Loadup Modules UPMS Infrastructure Layer

UPMS 持久化对象、Mapper 和网关实现。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-upms-infrastructure</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

接入 Spring MVC 时引入 web 与所需 app/infra；需要登录签发时增加 upms-authserver。

## 对外契约

- [`DepartmentConverter`](src/main/java/io/github/loadup/modules/upms/infrastructure/converter/DepartmentConverter.java)
- [`LoginLogConverter`](src/main/java/io/github/loadup/modules/upms/infrastructure/converter/LoginLogConverter.java)
- [`PermissionConverter`](src/main/java/io/github/loadup/modules/upms/infrastructure/converter/PermissionConverter.java)
- [`UserConverter`](src/main/java/io/github/loadup/modules/upms/infrastructure/converter/UserConverter.java)
- [`RoleConverter`](src/main/java/io/github/loadup/modules/upms/infrastructure/converter/RoleConverter.java)
