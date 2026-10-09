# LoadUp Contract Infrastructure

MySQL 合约持久化与租户 Gateway。主接入手册与完整报文见 [Contract README](../README.md)。

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-contract-infrastructure</artifactId>
</dependency>
```

版本由 LoadUp BOM 管理。需要管理 API 引入 web，需要本地服务引入 app；基础类型仅引入 client。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 当前职责 | MySQL 合约持久化与租户 Gateway |
| 生命周期 | 有效草稿、固定发布版本、初次签约和暂停/恢复/终止 |
| 验证状态 | 源码已提供，未编译或执行测试 |

三个表存储目录版本、合约头和不可变修订。Mapper 仅继承 BaseMapper；QueryWrapper 显式 tenant/deleted 约束，MapStruct 使用共享 Spring 配置。Flyway V20261009000001 自动提供结构。

## 接入约束

商户与租户必须来自可信身份和资料源。字段结构、权限、幂等重试、时间格式和限制见上层 README；审批、修订安排和管理页联调尚未交付。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。
