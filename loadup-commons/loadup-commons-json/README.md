# LoadUp Common JSON

为 DTO、record、领域数据和 DO 提供统一 JSON `toString()`。仅依赖 Jackson 3 与 commons-masking，不依赖 Spring，也不复用 HTTP 的 ObjectMapper。

## 接入

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-commons-json</artifactId></dependency>
```

```java
@Override public String toString() {
    return ToStringUtils.reflectionToString(this);
}
```

## 能力矩阵

| 能力 | 支持 |
|---|---|
| record、继承字段、嵌套集合和标准 JSON | 是 |
| 凭证字段、WRITE_ONLY 和 Masked 保护 | 是 |
| 循环、深度、元素、字符串及总节点上限 | 是 |
| 调用 bean getter / 读取外部资源 | 否 |

凭证名称启发式不能替代显式标注。自定义敏感字段使用 `@Masked(FULL)`；仅在日志隐藏、HTTP 仍需明文的字段使用 `@DiagnosticHidden`；诊断结果不用于存储、签名或 API 传输。展示被截断时仍保持有效 JSON。
