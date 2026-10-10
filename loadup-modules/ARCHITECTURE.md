# 业务模块架构

业务能力位于 `commons → components → modules → 消费工程` 的第三层。modules 只按业务领域组织聚合目录，各领域内部统一 COLA 分层。

```text
loadup-modules/
  loadup-modules-{business}/
    loadup-modules-{business}-client
    loadup-modules-{business}-domain
    loadup-modules-{business}-infrastructure
    loadup-modules-{business}-app
    loadup-modules-{business}-web
    loadup-modules-{business}-test
```

## 层与依赖

| 层 | 职责 | 依赖边界 |
| --- | --- | --- |
| client | Facade、Command、Query、DTO、消费方 SPI | 不依赖应用、HTTP 或持久化 |
| domain | 纯 Java 领域模型、规则、Gateway | 不依赖 Spring、ORM、client DTO |
| infrastructure | 持久化/外部系统适配、迁移 | 实现 domain Gateway |
| app | 用例、事务、应用装配、MapStruct | 依赖 client/domain，组合默认 infrastructure |
| web | Controller、可信请求上下文与授权 | 依赖 app/client，复用全局 WebMVC |
| test | 单测和集成验证源码 | 仅 test scope 使用运行模块 |

Transfer client 自有公共枚举，domain 保留领域枚举，应用层通过 MapStruct/名称映射转换；domain 不依赖 client。UPMS 保留所属 authserver 适配，Merchant 保留所属 contract 适配。

应用服务依赖 Gateway 接口，公开返回 client DTO；HTTP 层使用可见字段投影，文件 storageId 与通知接收者等内部信息不暴露。MapStruct 统一使用 LoadUpMapStructConfig 和 Spring 注入。

## 装配与配置

五个轻量业务模块的默认 MyBatis-Flex Gateway 与 Flyway 脚本归 infrastructure；持久化自动配置只创建端口适配。app 在其之后按 Gateway 和所需组件条件创建用例服务。消费者可替换 Gateway Bean。条件配置通过显式 Import 注册生成的 mapper，避免 REGISTER_BEAN 阶段条件与扫描冲突。

配置为 `loadup.modules.<business>.*`，Web 按 `*-web` Maven 依赖装配，无额外 enabled 开关；全局安全协议由技术组件的 `loadup.security.*` 管理。配置的 enabled 开关控制 Bean，不作为 Flyway 跳过脚本机制。用户需迁移旧前缀，无兼容别名。

## 跨业务边界

Transfer app 显式复用 File app 的权限、业务引用和 DFS 生命周期。UPMS web 的敏感查询审计通过 Audit client Facade记录。Merchant 通过可选 merchant-contract 适配和公开 MerchantQueryFacade 向 Contract 提供资料事实，两个核心模块不互相依赖。

## 本次调整范围

原顶层 audit/dictionary/file/notification/transfer 与各自 sibling web 已归入业务聚合目录，实际拆分协议、领域端口、持久化及用例。保留路由、JSON 字段、数据库历史脚本与事务语义。运行回归未执行，待办记录于根 ROADMAP。

接入见 [README.md](README.md)，各领域的业务与一致性细节见所属 ARCHITECTURE。
