# LoadUp Audit Client

不可变 DTO、Command/Query 和对外协议。

## 接入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-audit-client</artifactId>
</dependency>
```

版本由 `loadup-dependencies` BOM 管理。协议包为 `io.github.loadup.modules.audit.client`，DTO 均为 record，响应字段与既有 HTTP 协议一致。请求中的身份与租户必须由适配入口校验。

## 能力契约

| 能力 | 本层提供 |
| --- | --- |
| 不可变 DTO、Command/Query 和对外协议。 | 是 |
| HTTP 适配 | 由同一业务目录下可选 web 提供 |

完整业务 API、配置及使用示例见 [模块接入手册](../README.md)；内部职责见 [ARCHITECTURE.md](ARCHITECTURE.md)。
