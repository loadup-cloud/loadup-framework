# LoadUp Common JSON

统一提供 Jackson 3 JSON 转换、日期规则及安全的 JSON `toString()`。JSON 能力集中在本模块，commons-util 不再维护 JSON 类。内部使用 commons-log 记录转换失败、commons-masking 处理诊断脱敏；不提供 Spring 自动装配。

## 接入

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-commons-json</artifactId></dependency>
```

```java
import io.github.loadup.commons.json.JsonUtil;
import io.github.loadup.commons.json.ToStringUtils;

String json = JsonUtil.toJson(value);
MyDTO dto = JsonUtil.fromJson(json, MyDTO.class);

@Override public String toString() {
    return ToStringUtils.reflectionToString(this);
}
```

## 能力矩阵

| 能力 | 支持 |
|---|---|
| 对象、泛型、Map、字节、文件与 JSON 树转换 | JsonUtil |
| LocalDate / LocalDateTime / Date 日期规则 | JsonUtil.customize 与 MultiDateDeserializer |
| 接入应用 ObjectMapper | WebMVC 装配后调用 JsonUtil.setObjectMapper |
| record、继承字段、嵌套集合和标准 JSON | 是 |
| 凭证字段、WRITE_ONLY 和 Masked 保护 | 是 |
| 循环、深度、元素、字符串及总节点上限 | 是 |
| 调用 bean getter / 读取外部资源 | 否 |

凭证名称启发式不能替代显式标注。自定义敏感字段使用 `@Masked(FULL)`；仅在日志隐藏、HTTP 仍需明文的字段使用 `@DiagnosticHidden`；诊断结果不用于存储、签名或 API 传输。展示被截断时仍保持有效 JSON。

## JSON 与日期约定

`JsonUtil` 输出格式仍为 `yyyy-MM-dd` 和 `yyyy-MM-dd HH:mm:ss`，保留 null 字段，忽略未知属性和空 Bean 错误。Jackson 3 自带 Java Time 支持，本模块补充项目日期格式。WebMVC 复用 `JsonUtil.customize`，装配后将应用 mapper 交给 JsonUtil；ToStringUtils 始终使用独立的诊断 mapper。

### 多格式输入

| 类型 | 接受的输入 |
|---|---|
| LocalDate | `2024-02-29`、`2024/02/29`、`2024.02.29`、`20240229` |
| LocalDateTime | 上述前三种日期 + 空格或 `T` + `HH:mm` 或 `HH:mm:ss`；秒后支持 1–9 位小数；紧凑格式 `20240229123456` 也可带小数秒 |
| Date | 上述日期和日期时间；ISO 时间带 `Z`、`+08:00` 或区域时区；RFC 1123（如 `Thu, 29 Feb 2024 12:34:56 GMT`）；JSON 整数表示 Unix 毫秒 |

```java
LocalDate day = JsonUtil.fromJson("\"2024/02/29\"", LocalDate.class);
LocalDateTime time = JsonUtil.fromJson("\"2024-02-29T12:34:56.123456\"", LocalDateTime.class);
Date instant = JsonUtil.fromJson("\"2024-02-29T20:34:56+08:00\"", Date.class);
```

日期文本先去除首尾空白，再完整、严格解析；无效闰日、24 点、尾随内容、空字符串以及歧义格式 `01/02/2024` 均拒绝。JSON null 保留为 null。紧凑日期是字符串；仅 Date 的 JSON 整数解释为毫秒，不猜测秒/毫秒，也不解析数字字符串为时间戳。

Date 的无时区文本按应用 ObjectMapper 的时区转换，日期取该时区的零点；带时区文本保留其真实时间点。Date 精度为毫秒，额外小数精度会截断。LocalDate 不截取日期时间，LocalDateTime 不自动补日期的零点；两者拒绝时间戳和带时区文本，需表达时间点时使用 Date、Instant 或 OffsetDateTime。Instant / OffsetDateTime / ZonedDateTime 继续使用 Jackson 原生 ISO 规则。

LocalDate / LocalDateTime 的显式 `@JsonFormat(pattern = "...")` 仍由 Jackson 按字段格式解析；多格式是未指定字段格式时的默认策略。

迁移后的包为 `io.github.loadup.commons.json`，旧 `commons.util.JsonUtil` 和 `commons.util.json.MultiDateDeserializer` 已移除。现有转换失败时返回 null/空集合、String 输入直接返回等行为保持不变；需要强制失败语义时直接使用注入的 Jackson ObjectMapper。
