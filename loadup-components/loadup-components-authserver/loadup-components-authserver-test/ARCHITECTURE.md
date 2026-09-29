# LoadUp Components AuthServer Test 架构

## 职责与边界

OAuth2 授权服务器与令牌签发的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-authserver-api`
- `loadup-components-authserver-binder-sas`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
