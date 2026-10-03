# Loadup Modules UPMS App Layer

UPMS 应用服务与业务编排。

`AccountSecurityService` 提供本人安全概览、按用户 ID 查询登录历史以及使用旧密码修改密码的应用入口。HTTP 适配见 `loadup-modules-upms-web`。登录失败跟踪和临时锁定由 `loadup.upms.security.login` 控制；管理员手动锁定不会因超时自动解除。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-upms-app</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `UpmsAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

接入 Spring MVC 时引入 web 与所需 app/infra；需要登录签发时增加 upms-authserver。

## 配置入口

| 前缀 | 配置类 |
|---|---|
| `loadup.upms.security` | [`UpmsSecurityProperties`](src/main/java/io/github/loadup/modules/upms/app/autoconfigure/UpmsSecurityProperties.java) |

## 对外契约

- [`LoginStrategy`](src/main/java/io/github/loadup/modules/upms/app/strategy/LoginStrategy.java)
- [`OAuthProvider`](src/main/java/io/github/loadup/modules/upms/app/strategy/oauth/OAuthProvider.java)

## 自动装配

- [`UpmsAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/app/autoconfigure/UpmsAutoConfiguration.java)
