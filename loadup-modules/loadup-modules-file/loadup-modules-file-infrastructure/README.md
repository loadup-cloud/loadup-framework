# LoadUp File Infrastructure

MyBatis-Flex Gateway 默认实现、Flyway 迁移及持久化自动装配。

## 接入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-file-infrastructure</artifactId>
</dependency>
```

版本由 `loadup-dependencies` BOM 管理。需要 MySQL DataSource 和驱动；自动引入 Flyway 支持。`loadup.modules.file.enabled` 默认 true；通过提供 `FileResourceGateway` Bean 替换默认实现。迁移见 `src/main/resources/db/migration`。

## 能力契约

| 能力 | 本层提供 |
| --- | --- |
| MyBatis-Flex Gateway 默认实现、Flyway 迁移及持久化自动装配。 | 是 |
| HTTP 适配 | 由同一业务目录下可选 web 提供 |

完整业务 API、配置及使用示例见 [模块接入手册](../README.md)；内部职责见 [ARCHITECTURE.md](ARCHITECTURE.md)。
