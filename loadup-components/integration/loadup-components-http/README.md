# HTTP

使用 Spring `RestClient` + Apache HttpClient 5 调用外部 API 和应用间 HTTP 服务。通过命名客户端与操作配置组织目标地址和协议参数，提供同步调用、原始响应和受限流式下载。沿用
Boot 的 RestClient 定制、Jackson 配置及 HTTP Observation。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-http</artifactId>
</dependency>
```

第三方版本由 LoadUp BOM 继承的 Spring Boot BOM 管理，Apache HttpClient 5 在 LoadUp BOM 中明确声明。组件不启动 Web 服务器，不依赖
UPMS、Outbox 或渠道 SDK。

## 配置

```yaml
loadup:
  http:
    enabled: true
    default-port: 8080
    clients:
      merchant:
        base-url: https://merchant.example.com
        connect-timeout: 3s
        read-timeout: 10s
        acquire-timeout: 3s
        max-connections: 50
        max-connections-per-route: 20
        max-request-bytes: 1048576
        max-response-bytes: 10485760
        credential-ref: merchant-notify-v1
        headers:
          Accept: application/json
        operations:
          notify:
            method: POST
            path: /callbacks/orders/{orderId}
            headers:
              Content-Type: application/json
          download:
            method: GET
            path: /statements/{date}
```

`base-url` 可省略。省略时客户端名被当作目标应用名，地址按 `http://<client-name>:<default-port>` 解析，默认端口
`8080`；该约定的目标同时允许私有/集群地址。显式 `base-url` 仍默认禁止私有目标，需要时配置
`allow-private-addresses: true`。连接、池和大小限制均有默认值，只有与默认值不同的客户端才需要单独覆盖。

配置中的客户端和操作名限定为 1–64 个字母、数字、下划线或连字符。路径模板与业务传入的变量分别处理，变量作为值编码，调用不能改变配置的
origin。连接池等待、连接建立和响应读取超时分别配置；读取超时不是整个下载的总时长限制。

默认禁止目标 DNS 解析到本地、私有地址、IPv6 ULA 等内部目标；测试或可信内部调用可显式配置 `allow-private-addresses: true`
。重定向始终关闭。代理使用 `proxy: http://proxy.example.com:8080`，必须是受信任的出站设施，并在代理侧执行目标网络访问策略。

客户端证书和信任配置复用 Spring Boot SSL Bundle：设置 `ssl-bundle` 引用 `spring.ssl.bundle.*` 已定义的名称。使用 Bundle 创建
SSLContext，保留默认主机名验证；首版不额外映射 Bundle 的协议/密码套件选项。证书换版后重新刷新客户端配置，使新连接池使用更新的
SSLContext。

## 使用

`HttpTemplate` 通过构造器注入：

```java
var response = http.exchange("merchant", "notify", new HttpCall(
    Map.of("orderId", orderId), Map.of(),
    Map.of("Idempotency-Key", eventId), Map.of("status", "SUCCEEDED")));
if (!response.getStatusCode().is2xxSuccessful()) {
    throw new IllegalStateException("Notification was not acknowledged");
}
```

对象请求体使用应用共享 Jackson mapper 转为 JSON；`String` 和 `byte[]` 直接发送，不加引号或统一报文。原始请求体未配置
Content-Type 时使用 `application/octet-stream`。不传 body 时使用 `HttpCall.empty()`。

`exchange(..., ResponseDTO.class)` 可解析 JSON，保留状态码和响应头。原始 `exchange` 返回 `ResponseEntity<byte[]>`，包括
4xx/5xx；是否重试由业务根据状态、报文和操作语义判断。JSON 解码失败抛出 `RESPONSE_DECODING`，非 JSON 错误响应应先使用原始接口判断。

```java
try (OutputStream output = Files.newOutputStream(targetPath)) {
    var response = http.download("merchant", "download", new HttpCall(
        Map.of("date", date), Map.of(), Map.of(), null), output);
    // Inspect response status before accepting the file as a valid statement.
}
```

下载限制与普通响应一致，组件关闭 HTTP 响应流但不关闭调用方 OutputStream。失败时文件可能已部分写入；业务应先写临时文件，检查状态及校验信息后再移动到正式位置。

## Spring HTTP Service Clients

接口使用标准 `@HttpExchange` / `@PostExchange`，通过 `http.serviceClient("customer", CustomerHttpService.class)` 创建，
然后在消费方配置类中将代理注册为 Bean。接口由提供方 client 模块持有，组件不扫描或绑定业务包。客户端名等于目标应用名时可省略全部配置：

```yaml
loadup:
  http:
    clients:
      customer:
        # http://customer:8080, framework defaults
```

只有地址或协议参数不同于约定时才覆盖：

```yaml
loadup:
  http:
    clients:
      customer:
        base-url: ${CUSTOMER_BASE_URL:https://customer.example.com}
        read-timeout: 5s
        max-response-bytes: 65536
```

未在配置中声明的客户端在首次创建 Service Client 时按应用名约定建立，默认使用 `http://<name>:<default-port>`、
框架默认超时/池/大小和私有地址放行；显式配置的同名客户端优先。命名操作（`exchange`）仍要求显式配置客户端和 operation。
每个代理由标准 `HttpServiceProxyFactory` + `RestClientAdapter` 执行；共享同名 HttpTemplate 客户端的 Apache 连接池和
Boot RestClient 定制/Observation，保留 TLS Bundle、DNS 地址策略及无自动重试/重定向/Cookie 状态。
接口的绝对 URI 也不能改变配置的 origin，防止把当次凭证发送给其他主机。

使用同步 JSON DTO、泛型响应或 ResponseEntity；响应先有界缓存并关闭底层响应，再进入标准消息转换。
大文件仍走现有 `download` 流式接口。接口参数显式使用 `@RequestHeader("Authorization")` 传递当次 bearer，
或使用既有 `credential-ref` 按次读取凭证；不默认转发调用线程的安全身份，也不缓存 token。
HTTP 非 2xx（含 3xx）抛出安全的 HttpCallException，不暴露原响应正文；HTTP 200 的业务结果可由 HttpServiceResponses.requiredData 校验成功码、状态与非空数据；失败通过 resultCode 提供格式受限的远端码，
由调用方显式白名单映射，未知码保持依赖失败。远端文案不进入异常，任意异常消息不能伪造远端结果码。

代理每次在当前客户端快照中查找标准代理，完整调用期间持有快照读锁；显式 `refresh` 后原代理使用新地址/连接池，
失败刷新保留旧快照，关闭后拒绝调用。ConfigMap 更新不会自动调用 refresh，默认随应用重启生效。

`HttpServiceClientsTest` 已补齐泛型 JSON、逐请求令牌、大小上限、超时、私网策略、无重试/重定向和刷新/关闭回归源码；尚未执行。

## 凭证与配置更新

提供一个 `HttpCredentialProvider` Bean，将 `credential-ref`
解析为认证头。每次调用解析引用，允许秘密设施自行轮换值；不把私钥、令牌直接放入普通操作配置。合并顺序为客户端头、操作头、调用头、凭证头；最后的凭证头优先。组件拒绝
CR/LF、Host、Content-Length 等非法或传输层管理的头。

```java
long version = http.refresh(new HttpProperties(validatedClients));
```

首版提供显式刷新接口，不自动订阅配置中心。应用可在 ConfigCenter
监听器中解析完整配置并调用刷新，回调不得从同一个正在执行的凭证解析过程内触发刷新。先校验并构建所有新客户端，失败保留旧快照；等待当前调用结束后原子替换并关闭旧连接池。下载可能延迟刷新，不提供刷新强制超时。

## 错误、观测与重试

`HttpCallException.Kind` 区分 CONNECTION、TIMEOUT、TRANSPORT、SIZE_LIMIT、SERIALIZATION、RESPONSE_DECODING。异常不包含目标
URL、报文或底层异常消息；HTTP 错误状态由 ResponseEntity 返回，不强制转为异常。

默认不记录请求/响应内容和认证头；禁止通过日志打印完整调用对象、配置或原始响应。Boot 的标准客户端 Observation 负责 Trace 和
HTTP 指标；另有 `loadup.http.calls`，使用配置中的 client、operation 和固定 outcome 标签，不放入订单、租户或 traceId。

**不自动重试、不跟随重定向、不使用共享 Cookie 状态。** Apache 自动重试明确关闭。需要业务重试时使用稳定渠道请求号或下游幂等协议，再由业务选择
PowerJob/Outbox；不能让多个组件重复包装重试。

## 能力矩阵

| 能力                        | 首版支持                                                                       |
|-----------------------------|--------------------------------------------------------------------------------|
| 命名客户端与操作            | 地址、方法、模板、默认头、凭证引用                                             |
| 声明式应用间调用            | Spring HTTP Service Clients，按服务地址分组，共享同名连接池                      |
| 请求体                      | JSON 对象、原始 String/byte[]                                                  |
| 响应                        | 原始状态/头/byte[]，JSON 解析                                                  |
| 下载                        | 有上限的流式复制，可用最终 URI/原字节 signer，自动关闭响应、不关闭调用方输出流 |
| 网络                        | Apache 连接池、分阶段超时、TLS Bundle、受信任代理                              |
| 配置更新                    | 显式原子刷新，旧池延后释放                                                     |
| 自动重试 / 重定向           | 明确关闭                                                                       |
| 配置中心自动监听 / 渠道协议 | 由应用适配                                                                     |
| 异步调用 / multipart 上传   | 首版未提供                                                                     |

`HttpTemplateTest` 使用本地 HTTP 服务验证协议状态、无重试与重定向、JSON、原始下载、大小限制、地址策略、URI
编码和刷新失败保留旧配置；HttpPoolMetricsTest 覆盖池饱和、计时边界、刷新、注册失败回收与 scope 隔离。
其余测试覆盖声明式代理、信封验证、配置与地址策略。后续恢复测试时，用户可在 Framework 根目录选择该模块及其依赖：

```shell
mvnd clean test -pl loadup-components/integration/loadup-components-http -am \
  -Dmaven.test.skip=false -DskipTests=false -Dskip.unit.tests=false \
  -Dtest=HttpTemplateTest,HttpPoolMetricsTest,HttpServiceClientsTest,HttpServiceResponsesTest,HttpClientDefaultsTest,HttpAutoConfigurationTest,HttpTargetPolicyTest,HttpConfigurationContractTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dspotless.skip=true -Dspotbugs.skip=true
```

本次实现未执行构建或测试。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 统一指标

使用消费工程提供的 Micrometer `MeterRegistry`，`loadup.http.calls` 的 outcome 统一为 success/failure；非 2xx 与传输失败均计为
failure。client/operation 仅取已配置的有限名称，目标参数和原始 URI 不进入标签。引入 Observability 后公共 application 标签由标准
management 配置统一提供。

命名操作与 HTTP Service Clients 共用以下池指标，均带 `scope/client` 标签：

| 指标 | 含义 |
| --- | --- |
| `loadup.http.pool.leased/available/pending` | Apache 原生池中已借出、空闲、等待获取的连接数 |
| `loadup.http.pool.max/route.max` | 总连接上限与默认每路由上限 |
| `loadup.http.pool.acquire` | 原生 lease/get 阶段计时；outcome 为 success/timeout/interrupted/failure |

acquire 包含无竞争时的连接借出和连接可用性校验，不包含后续 TCP 建连、TLS 握手及 HTTP 响应耗时；
不能把它当作仅排队时间或整个 HTTP 调用耗时。取消操作仍由 Apache 处理，未进入 get 的取消不产生完成计时。
池指标与 HTTP 总耗时应一起采集，不能用总耗时反推池等待。需要获取等待的分位数时，在消费工程原生
`management.metrics.distribution.percentiles-histogram` 中为 `loadup.http.pool.acquire` 启用直方图，并在压测前冻结桶和采集间隔。

默认 scope 为 application；手动创建多个 HttpTemplate 并共享 MeterRegistry 时，使用带 metricScope 的构造器指定固定且不同的 scope，
不要使用随机值、URI 或业务标识。OpenBao 使用 openbao-transit/openbao-admin。相同 scope/client 的并存实例会在装配时被拒绝，
指标过滤器应保留这两个身份标签。刷新保留同名客户端的累计计时并切换 gauge 的池引用；移除客户端或关闭模板时只清理其持有的池指标。
构建候选客户端失败保留原池及指标。这些采集与生命周期回归目前只有源码和静态检查，尚未运行。

## 最终请求签名与流式下载

`exchange(client, operation, call, signer)` 和 `download(client, operation, call, output, signer)` 共用同一路径：
先解析配置目标、编码实际 URI、序列化原请求字节并检查目标策略，再调用 `HttpRequestSigner`，将签名头最后合并后发送。
签名逻辑不应重新拼接 URI 或对业务对象再次序列化；可先核对传入的最终 method/URI/body，再调用 KMS。

```java
var response = http.download("channel-bill", "download", call, boundedOutput,
    (method, uri, bytes) -> protocol.sign(method, uri, bytes));
```

签名版本保留命名客户端的大小上限、固定缓冲区复制、连接/DNS 策略和禁止重定向/重试。
调用方还可传入更严格的有界 OutputStream，超限直接中断，不能先完整缓存再检查。
组件始终关闭响应，不关闭调用方输出；异常时可能有部分字节，调用方自行丢弃临时文件或未提交对象。
HTTP 4xx/5xx 仍返回 ResponseEntity，应先检查状态与完整性再认定文件有效。

`HttpTemplateTest.signedStreamObservesTheExactWireTargetAndPropagatesCallerOutputLimitsWithoutClosingOutput`
已补最终 URI/请求字节、输出内容、调用方异常和输出流生命周期测试源码；2026-10-28 未运行编译或测试。

声明式 JSON 转换明确使用注入的 Jackson 3 JsonMapper（与命名操作相同），保留应用的时间、金额与严格整数约定，
不会另建默认 mapper。共享 JSON 配置禁止浮点数截断为 int/long；新增回归源码验证标准代理同样拒绝该响应。

## 共享出站身份配置默认值

HttpClientDefaults.bind(environment, modulePrefix, targetApplication, Settings.class) 仅复用 Boot Binder 绑定默认配置，不负责认证。
模块显式配置优先，其次是 loadup.http.client.{service-id,audience,token-file}；
service-id 默认调用方 spring.application.name，audience 默认目标应用，token-file 默认 /var/run/loadup/secrets/<目标应用>.jwt。
多目标调用须分别指定 audience/token-file，避免把共享目标配置误用于另一服务。helper 不读/缓存 token、不设置全局认证头；
调用方仍逐次验证并读取凭证。旧 RPC 配置前缀不在此 helper 中提供兼容别名。
