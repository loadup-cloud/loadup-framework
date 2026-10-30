# UPMS 测试模块

验证 UPMS 应用服务、持久化网关及对外契约；不作为生产运行时依赖。数据库集成测试使用 Testify + Testcontainers 连接真实 MySQL。

## 运行条件

需要 Java 25、Maven Daemon（`mvnd`）和可用的 Docker。`application.yml` 默认激活 `test` profile，`application-test.yml` 与
`application-ci.yml` 分别配置本地和 CI 环境。

## 执行测试

仓库根目录运行单元测试与集成测试：

```bash
mvnd -B -ntp -T 1 clean verify \
  -pl :loadup-dependencies,:loadup-modules-upms-test -am
```

仅回归真实数据库的用户创建用例，并使用 CI 配置：

```bash
mvnd -B -ntp -T 1 clean verify \
  -pl :loadup-dependencies,:loadup-modules-upms-test -am \
  -Dspring.profiles.active=ci -Dtestcontainers.reuse.enable=false \
  -Dskip.unit.tests=true -Dit.test=UserServiceIT \
  -Dfailsafe.failIfNoSpecifiedTests=false
```

`test` 阶段只运行 Surefire 单测，`verify` 才会执行和检查 Failsafe 集成测试。报告分别位于 `target/surefire-reports` 与
`target/failsafe-reports`；Failsafe verify 失败时先查看具体用例异常。

## 数据准备与装配

- `TestApplication` 使用 `@SpringBootConfiguration` + `@EnableAutoConfiguration`，由正式自动配置提供服务、Gateway、Converter、Mapper
  和 database 能力；不直接扫描 database 自动配置类。
- 公共 `application.yml` 先执行 `schema-test.sql`，再执行 database 组件提供的 Sequence 建表 SQL。本地及 CI 共用初始化顺序。
- `schema-test.sql` 仅重置测试数据库，包含清理旧 Sequence 表；真实消费工程仍应通过迁移管理 Sequence。
- 不配置 `mybatis-flex.global-config`，持久化约定由 database 代码统一维护；测试也不能绕过配置保护。

分层与用例边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。
