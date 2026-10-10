# LoadUp Dictionary Domain

纯 Java 领域模型与持久化 Gateway 端口。

## 接入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-dictionary-domain</artifactId>
</dependency>
```

版本由 `loadup-dependencies` BOM 管理。模型包为 `io.github.loadup.modules.dictionary.domain.model`，端口为 `io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway`；没有 Spring/ORM 依赖，适用于领域逻辑与自定义持久化适配。

## 能力契约

| 能力 | 本层提供 |
| --- | --- |
| 纯 Java 领域模型与持久化 Gateway 端口。 | 是 |
| HTTP 适配 | 由同一业务目录下可选 web 提供 |

完整业务 API、配置及使用示例见 [模块接入手册](../README.md)；内部职责见 [ARCHITECTURE.md](ARCHITECTURE.md)。
