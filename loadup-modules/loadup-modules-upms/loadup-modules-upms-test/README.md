# UPMS 测试模块

本模块验证 UPMS 应用服务、持久化网关及对外契约。集成测试使用 Testify 和 Testcontainers；数据库相关用例连接真实容器，不以 Mock 替代。

## 运行条件

需要 Java 25、Maven 与可用的 Docker 环境。`application.yml` 激活 test profile；`application-test.yml` 和 `application-ci.yml` 分别保存本地与 CI 配置。

## 运行

```bash
mvn clean test -pl loadup-modules/loadup-modules-upms/loadup-modules-upms-test -am
```

测试模块不作为生产运行时依赖。分层与用例边界见 [ARCHITECTURE.md](./ARCHITECTURE.md)。
