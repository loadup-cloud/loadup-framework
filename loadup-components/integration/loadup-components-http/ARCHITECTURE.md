# HTTP Architecture

## 定位与技术栈

单一 jar，采用 Spring RestClient 和 Apache HttpClient 5。RestClient 保留标准接口、Boot Customizer 和 Observation；Apache
负责连接池、网络超时、SSLContext、DNS 接入及显式禁用重试。没有第二套传输 SPI 或多后端 binder。

框架组织命名操作，渠道 SDK 和协议适配仍由业务选择；组件不承担入站路由、渠道状态机或支付幂等。

## 调用流程

```text
读取不可变客户端快照
→ 解析配置中的操作及编码变量
→ 校验目标与认证头
→ 将请求体序列化为受限 byte[]
→ 可选 signer 接收最终 method/URI/原字节，签名头最后合并
→ RestClient exchange
→ 读取受限响应或复制到调用方输出流
→ 保留协议状态与头
→ 释放响应及快照读锁
```

HTTP 状态错误与传输失败分别表达。4xx/5xx 不触发自动异常或重试，调用方检查业务协议；传输异常只保留安全分类，不暴露可能包含敏感
URL 的底层异常。

## 生命周期和配置

ConfigurationProperties 使用 record 与不可变
Map。每个命名客户端独立连接池和操作表，单次调用持有读锁直到响应处理完成。刷新串行构建全部候选客户端，再取得写锁切换；已进入调用的请求继续使用原配置，新配置不会影响半个请求。旧池在原调用完成后关闭，构建失败则关闭候选池并保留当前配置。

这是一种保守实现：刷新等待长下载完成。没有并行后台更新和复杂引用计数；若后续业务需要限制刷新等待，依据实际长连接需求再调整生命周期。调用过程不得递归刷新配置。

应用关闭时停止接收新调用并关闭所有池。SSL Bundle 和凭证管理设施由 Boot/应用负责，组件通过引用接入；证书更新需要刷新客户端，认证头由凭证
Provider 每次解析。

## 网络与安全约束

目标 origin 来源仅限可信客户端配置，操作只能使用路径模板。变量预编码，不能改变 host、scheme 或 port。每次连接 DNS
解析执行地址策略，额外预检查兼顾 IP 字面量；关闭重定向避免 Location 改变目标。该策略不是完整网络防火墙，生产仍需要出站网络隔离；显式代理必须在代理侧实施地址策略。

默认不开启私有目标和 Cookie 状态，不禁用证书链或主机名验证。客户端私钥不进入请求配置、Outbox
或日志。传输拥有的请求头不得由业务覆盖，认证头在业务头之后应用。

请求在发送前完成序列化和大小检查，响应根据 Content-Length
和实际读取累计值双重限制。下载以固定缓冲区复制，失败可能留下部分输出，消费工程负责临时文件提交。读超时约束等待阶段，不承诺持续返回数据的无限下载一定在固定总时间内结束。

## 观测和职责边界

Boot RestClient.Builder 被注入并 clone，保留其自动定制。默认不新增请求报文日志，指标以可信配置名称而非动态请求值作为标签。应用还需限制自定义
RestClient interceptor 及 tracing exporter 中的敏感信息采集。

组件不提供重试配置，Apache 自动重试关闭；需要重试时由业务确认幂等后接入现有任务或容错能力。Outbox
的失败重投和外部请求的受理状态分别管理，下游已受理但响应丢失时仍可能再次发送同一业务请求。

## 签名下载路径（2026-10-28）

带 signer 的流式下载与原始 exchange 共用 execute，避免业务为了签名先缓存完整响应或另拼编码不同的 URL。
调用方可用有界输出流收紧单次大小上限；框架仍执行客户端 Content-Length/实际字节限制和响应关闭。
输出流由调用方持有，写入失败不回滚已写字节，也不关闭该流；业务来源校验与正式提交在负责层完成。
最终签名不改变固定 origin、DNS/代理约束、无自动重试/无重定向。新增测试源码未执行。

## 声明式应用间调用（迁移首批）

HttpTemplate.serviceClient 提供标准接口代理；内部仍由 HttpServiceProxyFactory + RestClientAdapter 完成注解解析、URI 编码、
泛型类型和消息转换。同一 RuntimeClient 的命名操作与声明式客户端使用同一个 Apache transport，不建立第二套连接池或注册中心适配。

声明式请求通过当前快照中的 RestClient 发送。末端 interceptor 约束固定 origin、每次 DNS、请求大小和凭证，
有界读取并关闭响应后交给标准 converter；HTTP 非 2xx 映射为安全传输错误。Boot 的 Observation 仍生效，
声明式路径不生成 loadup.http.calls 的 operation 指标，以免动态 URI/方法参数产生高基数。

外层轻量代理只协调快照生命周期；每个快照缓存标准接口代理，refresh 后不复用旧 transport。
整个标准调用在读锁内完成，写锁等待调用结束后才关闭旧池；业务身份由方法参数或 HttpCredentialProvider 当次解析，
没有额外 ThreadLocal/token 缓存。内部入口鉴权和业务错误白名单属于应用，不属于传输组件。

## 共享业务信封验证

HttpServiceResponses 复用 commons-dto 的标准信封，只有 SUCCESS/S 且非空 data 才返回业务数据。
格式正确的失败仅保留受限 code 供消费方白名单处理；响应文案不进入异常，任意异常消息不参与码解析。
不提供全局业务码重映射，也不把认证或传输失败降为业务拒绝。成功但空 data 的协议应另行显式定义，当前方法不接受。

声明式路径通过标准 configureMessageConverters / JacksonJsonHttpMessageConverter 绑定原 JsonMapper；
其余 Boot 定制与非 JSON 转换保留。共享 JSON 定制关闭 ACCEPT_FLOAT_AS_INT，阻止金额、版本、计数和序号的静默截断。
