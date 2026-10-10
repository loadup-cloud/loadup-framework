# LoadUp Modules

可按需消费的业务能力，每个业务目录是 COLA 聚合 POM。通过 `loadup-dependencies` BOM 管理版本，消费工程引入具体 `*-app` 或 `*-web` jar。

## 业务目录

| 模块 | 能力 |
| --- | --- |
| [UPMS](loadup-modules-upms/README.md) | 用户、RBAC/ABAC、账号安全及可选认证适配 |
| [Merchant](loadup-modules-merchant/README.md) | 商户基本资料与可选合约事实适配 |
| [Contract](loadup-modules-contract/README.md) | 产品、组合、销售方案与商户合约 |
| [Audit](loadup-modules-audit/README.md) | 审计记录、查询与可选 MVC 采集 |
| [Dictionary](loadup-modules-dictionary/README.md) | 字典类型、条目及启用选项 |
| [File](loadup-modules-file/README.md) | 文件元数据、访问控制、引用与清理 |
| [Notification](loadup-modules-notification/README.md) | 持久化收件箱与 Gotone IN_APP 渠道 |
| [Transfer](loadup-modules-transfer/README.md) | 导入导出任务、进度、结果与重试 |

## 分层选择

每个业务目录统一包含 `client / domain / infrastructure / app / web / test` 子模块。UPMS 的 authserver、Merchant 的 contract 适配也位于所属业务目录。web 是可选接口适配层，不在 modules 下单列为业务模块。

- 仅共享协议：引入 `*-client`。
- 程序化使用用例：引入 `*-app`，自动带入默认持久化适配。
- 提供 HTTP 接口：引入同一业务目录下的 `*-web`，自动带入 app。
- 扩展持久化：依赖 domain Gateway，实现并注册接口 Bean。
- test 只用于验证，不引入生产工程。

## 配置命名空间

业务配置统一使用 `loadup.modules.<module>.*`，开关为 `enabled`：

```yaml
loadup:
  modules:
    file:
      enabled: true
      web:
        enabled: true
    audit:
      enabled: true
      web:
        include-paths:
          - /api/files/*
    transfer:
      max-input-bytes: 20971520
      max-output-bytes: 104857600
    upms:
      security:
        login:
          max-fail-attempts: 5
```

模块专有业务策略使用 modules 前缀。全局认证协议配置仍由 `loadup.security.*` 管理，观测使用标准 `management.*` 与 `loadup.<domain>.*` 指标命名。旧业务配置前缀已移除，集成方需更新配置；具体属性以各模块文档为准。

设计边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。
