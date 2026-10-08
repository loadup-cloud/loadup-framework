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

组件传递引入 `loadup-components-observability`。Spring Boot 的 HTTP Observation 生成指标与 Span，响应头返回当前 Span 的 `traceId`，包括认证失败与错误响应。应用通过 `management.tracing.*` 配置采样和导出。JSON API 结果另以 `loadup.api.responses` 的 `outcome` 标签统计成功与失败，避免统一 HTTP 200 掩盖业务错误。

## JSON 约定

业务 API 前缀固定为 `/api`。组件使用 Spring Boot 4 的 `JsonMapperBuilderCustomizer`，保留 Boot 的 Java Time/JDK8 模块发现机制，并沿用 `JsonUtil` 的日期规则：`LocalDate` 为 `yyyy-MM-dd`，`LocalDateTime` 和传统 `Date` 为 `yyyy-MM-dd HH:mm:ss`；同时注册对应反序列化模块，日期与时长不写为时间戳。Boot 创建的 `ObjectMapper`、`JsonUtil` 和 DTO 序列化共用该日期配置；MVC 使用从它复制的响应专用 Mapper，另加展示脱敏规则。应用可用更高顺序的 `JsonMapperBuilderCustomizer` 覆盖规则。

## 自动装配

- [`LoadUpWebMvcAutoConfiguration`](src/main/java/io/github/loadup/components/webmvc/LoadUpWebMvcAutoConfiguration.java)

设计边界与装配路径见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 响应字段脱敏

组件传递引入 `loadup-commons-masking`。在输出 DTO 的 String 属性标注：

```java
public record ContactDTO(@Masked(MaskType.PHONE) String mobile) {}
```

HTTP JSON 输出中的 `13812345678` 变为 `138****5678`。注解支持字段、getter 和 record，嵌套 `result/data`、分页及集合均通过 Jackson 序列化生效。参见 [完整规则](../../loadup-commons/loadup-commons-masking/README.md)。

| 能力 | 行为 |
|---|---|
| JSON 字段脱敏 | MVC 服务端转换器使用响应专用 Jackson 3 Mapper |
| JSON 输入 | 反序列化不脱敏，原始输入保持原值 |
| JsonView、日期、其他模块 | 从 Boot Mapper 复制，保留既有序列化规则 |
| 全局 JSON、出站 HTTP、缓存 | 不安装 masking module，保持原值 |
| 明文查看 | 业务独立 DTO 与授权接口，组件没有权限豁免开关 |
| 日志、CSV/Excel、下载流 | 使用 `Masking` 显式处理；不会自动脱敏 |

不注册全局 `JacksonModule` 或替换全局 ObjectMapper，也不让权限改变普通接口的展示规则。`@Masked` 仅用于输出 DTO，不能替代数据库加密。避免先用全局 Mapper 将 DTO 变成字符串/Map 再返回，这会丢失注解信息。

装配通过顺序 100 的 `ServerHttpMessageConvertersCustomizer` 安装服务端 JSON 转换器。若集成方在更高顺序重新替换该转换器或使用其他响应方式，须保留脱敏适配并验证其行为。
