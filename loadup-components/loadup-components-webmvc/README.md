# Web MVC

统一 Spring MVC 业务 API 的 JSON 报文和错误处理，不依赖 UPMS。`/api/**` 的成功响应包含 `result`、`data`，分页另含 `pageInfo`；错误响应由 `result.code` 表示原因，HTTP 状态为 200。已返回 `IResponse` 的 Controller 不会重复包装，字符串响应也会转换为 JSON 报文。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-webmvc</artifactId>
</dependency>
```

UPMS Web 适配模块已经传递引入此组件。其他 Controller 应用可直接引入；文件下载等非 JSON 响应不做包装。无返回值的接口应显式返回 `SuccessResponse.success()`。`/api` 以外的错误维持原 HTTP 状态并返回 JSON 错误信息，文档和 Actuator 响应不套业务报文。

## JSON 约定

业务 API 前缀固定为 `/api`。组件使用 Spring Boot 4 的 `JsonMapperBuilderCustomizer`，保留 Boot 的 Java Time/JDK8 模块发现机制，并沿用 `JsonUtil` 的日期规则：`LocalDate` 为 `yyyy-MM-dd`，`LocalDateTime` 和传统 `Date` 为 `yyyy-MM-dd HH:mm:ss`；同时注册对应反序列化模块，日期与时长不写为时间戳。Boot 创建的 `ObjectMapper`、MVC、`JsonUtil` 和 DTO 日志序列化共用该配置。应用可用更高顺序的 `JsonMapperBuilderCustomizer` 覆盖规则。
