# Loadup Gotone Test 架构

## 职责与边界

多渠道通知的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`
- `loadup-components-gotone-engine`
- `loadup-components-gotone-store-jdbc`
- `loadup-components-gotone-binder-email`
- `loadup-components-gotone-binder-push`
- `loadup-components-gotone-binder-sms`
- `loadup-components-gotone-binder-webhook`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-jdbc`
- `org.springframework.boot:spring-boot-starter-json`
- `com.mysql:mysql-connector-j`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
