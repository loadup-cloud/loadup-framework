# Testify Core

测试引擎共用的 JSON 工具层，当前公开实现是 `io.github.loadup.testify.core.util.JsonUtil`。多数集成方引入 Testify starter
即可，独立工具消费可直接引入本模块。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-core</artifactId>
    <scope>test</scope>
</dependency>
```

## 能力矩阵

| API                  | 用途                                   | 边界                     |
|----------------------|----------------------------------------|--------------------------|
| toJson / valueToTree | 对象转 JSON / JsonNode                 | 静态测试 Mapper          |
| readTree             | JSON 字符串解析                        | 无文件和 HTTP 访问       |
| convertValue         | Map、JsonNode 或已解析值绑定 Java 类型 | 按对应重载使用           |
| toListMap            | JsonNode 转记录集合                    | 不是数据查询             |
| equals               | 比较序列化后的字符串                   | 不等同 JSON 结构宽松比较 |

```java
JsonNode input = JsonUtil.readTree("{\"name\":\"Ada\"}");
String json = JsonUtil.toJson(Map.of("name", "Ada"));
```

JsonUtil 使用 Jackson 3 `tools.jackson` 类型，忽略绑定目标未知字段并关闭日期 timestamp 输出。它不注入 Boot
ObjectMapper，不继承 WebMVC 的时间格式、脱敏、视图或业务 modules。验证真实 HTTP 报文时应使用应用的 HTTP/MockMvc 输出。

解析和序列化异常会包装为
RuntimeException；错误详情可能包含输入片段，测试样本使用合成数据。字段断言使用 [断言引擎](../loadup-testify-assert-engine/README.md)
，整体接入见 [Testify](../README.md)。
