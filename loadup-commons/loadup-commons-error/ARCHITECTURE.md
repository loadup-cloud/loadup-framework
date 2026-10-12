# Common Error 架构

## 职责与依赖

```text
commons-error (JDK only)
    ↑
commons-json / commons-context / 组件 API
    ↑
commons-dto (Result、响应 envelope)
    ↑
业务 client.error 枚举 → domain / infrastructure / app / web
```

ResultCode、状态枚举、通用错误枚举和 CommonException 位于独立基础 jar，避免 JSON 抛出规范异常时反向依赖 DTO。CommonException 不引用 JSON 库，toString 只输出已校验的枚举码与安全文案，手动转义 JSON，不展开 Throwable 或 cause。

## 边界

- ResultCode 是错误定义契约；Result 是不可变的报文快照，不再实现 ResultCode。
- 应用定义错误时只使用枚举。公共响应工厂不接收字符串错误码，也不允许替换枚举文案。
- CommonException 只保留 ResultCode 和可选 cause；不提供字符串 message 构造器。对外响应读取枚举，诊断保留异常链。
- 各业务模块公开 client.error 词汇，domain 仅依赖这些纯错误枚举；client 不反向依赖 domain。
- 技术组件保留 KmsException/HttpCallException 等可识别异常类型，其分类枚举实现 ResultCode。SDK 前置条件、平台启动配置与第三方标准协议异常保留负责层的语义；WebMVC 不透传其异常字符串。

## 演进与验证

固定码一经发布不随文案变化重命名，不重复使用旧码表达新语义。增加枚举项时检查命名空间、长度和唯一性。错误失败保留 HTTP 200、result/data；OAuth/下载等协议由对应适配器处理。未知系统错误使用 SYS_ERROR。

CommonExceptionTest 验证文案/原因分离与 enum-only 契约，ErrorCodeContractTest 校验业务命名空间及码唯一性，ArchitectureTest/SourceStandardsTest 检查枚举类型及业务裸异常，MVC 测试验证响应和可读报文。
