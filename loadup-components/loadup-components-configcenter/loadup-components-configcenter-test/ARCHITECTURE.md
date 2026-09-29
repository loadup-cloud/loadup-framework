# LoadUp ConfigCenter Test 架构

## 职责与边界

配置中心的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。
