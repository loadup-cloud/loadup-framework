# LoadUp Gateway 详细设计：SCG MVC 路由内核与 Service 方法入口

> 状态：目标设计；Service/HTTP 主路径、文件与配置中心来源、原子快照、基础限流、签名与 HTTP 熔断适配已经落地，完整治理能力仍按本文验收。
> 上位方案：[TARGET_ARCHITECTURE.md](./TARGET_ARCHITECTURE.md)。本文规定目标契约和验收边界；[ARCHITECTURE.md](./ARCHITECTURE.md) 描述当前实现和限制。
> 技术基线：Java 25、Spring Boot 4.1.x、项目 BOM 管理的 Spring Cloud Gateway Server MVC（下称 SCG MVC）。Service 与 HTTP 路由均已通过 BOM 版本的定向集成测试。

## 当前实施状态

| 已实现 | 尚待交付 |
|--------|----------|
| `loadup-gateway-api`；Service 暴露白名单与参数绑定；SCG 原生 HTTP handler；版本化 YAML；文件监听/轮询；配置中心来源；原子快照与失败保留；基础权限、指标和只读诊断；可选 Bucket4j 本地限流；HTTP 熔断适配；SCG 原生 StripPrefix/RewritePath/SetRequestHeader；HMAC 签名与可替换 nonce 存储；SCG 属性路由 ID 冲突检查；旧执行链清理 | RPC 目标、更多经过验证的 SCG 过滤器、统一超时、正式 JDBC 来源、Java DSL 路由冲突诊断、跨实例收敛验证及共享限流存储的验证 |

当前托管路由接受 `/api/**`，入口策略接受 `public`、`authenticated`、`authority`。具体用法与限制以 [README.md](./README.md) 和 [ARCHITECTURE.md](./ARCHITECTURE.md) 为准；本文后续章节仍是完整目标，不把未实现的能力当作已交付契约。

## 1. 目标与范围

LoadUp Gateway 是一个可嵌入应用的统一入口组件，有两种使用场景：

| 场景 | 请求目标 | 给集成方的价值 |
|------|----------|----------------|
| 单应用 | 同进程、显式暴露的 Spring Service 方法 | 无需编写 Controller；路由、入口授权、参数绑定和响应处理由框架承担 |
| 分布式应用 | 远程 HTTP 服务，必要时为 RPC | 直接使用 LoadUp 的动态路由与治理能力，而执行内核仍基于 SCG MVC |

两种场景使用同一份托管路由模型、同一路由来源 SPI、同一套策略和诊断。`service` 与 `http` 只是不同的终点 handler。目标是在修改外部文件、配置中心或数据库中的路由后，**无需重新打包或部署**即可更新路由；新增 Service 方法本身仍需正常发布应用代码。

不提供独立运行的网关产品，不实现第二套 HTTP 代理或通用过滤器系统，不提供任意 bean 方法反射调用，不把 Service 调用伪装为具有跨进程隔离能力的网络请求。

## 2. 架构与责任边界

```text
                外部文件 / 配置中心 / 数据库
                           │
                       RouteSource
                           │ 完整版本快照
                 校验 → 编译 → 原子发布
                           │
HTTP 请求 → Servlet SecurityFilterChain → SCG MVC RouterFunction
                                           │ 路由策略 / 限流 / 熔断 / 追踪
                                           ├─ service → Spring 代理 Service 方法
                                           ├─ http    → SCG HandlerFunctions.http()
                                           └─ rpc     → 可选协议适配器
```

SCG MVC 负责 WebMvc.fn 路由匹配、谓词与过滤器运行、HTTP 转发和已有通用过滤器。LoadUp 负责 Service 方法目录与调用、托管路由编译与热更新、框架策略、来源适配及诊断。SCG MVC 官方以 `RouterFunction` 为路由基础，并提供 `HandlerFunctions.http()` 和自定义 `HandlerFilterFunction` 扩展点：[路由 API](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/java-routes-api.html)、[扩展接口](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/writing-custom-predicates-and-filters.html)。

### 建议模块

| 模块 | 必选 | 职责与依赖规则 |
|------|------|----------------|
| `loadup-gateway-api` | 是 | 只放 Service 暴露注解、`RouteSource`/目标扩展 SPI、配置模型；不依赖具体来源或 RPC SDK |
| `loadup-gateway-webmvc` | 是 | SCG MVC 适配、Service 调用、路由编译/快照、基础诊断；依赖 api 与 SCG MVC |
| `loadup-gateway-starter` | 是 | 组合 api/webmvc 与默认自动装配；不传递引入数据库、配置中心或 RPC 客户端 |
| `repository-yaml-plugin` | 默认 | jar 外文件读取、变更监听与摘要校验；由 starter 引入，不负责路由执行 |
| `loadup-gateway-source-configcenter` | 可选 | 通过现有配置中心能力读取和订阅版本；不绑定单一厂商实现 |
| `loadup-gateway-source-jdbc` | 可选 | 读取已发布版本；管理写入接口单独受控 |
| `loadup-gateway-security-jwt` | 可选 | Spring Security JWT 资源服务器；路由授权策略位于 webmvc，不签发令牌 |
| `loadup-gateway-handler-rpc` | 可选 | RPC 目标 handler；只在有消费者时交付 |

模块名是建议，不是提前创建 jar 的要求。发布前根据实际独立消费和依赖隔离决定是否合并。核心与来源、协议适配之间只能通过 api 契约相连。

## 3. 托管路由配置契约

托管路由使用**一种与来源无关的版本化文档**。文件、配置中心和 DB 提供相同语义；来源只负责提供完整版本，不决定 HTTP 执行行为。

```yaml
schemaVersion: 1
routes:
  - id: user-detail
    order: 100
    path: /api/users/{id}
    methods: [GET]
    target:
      type: service
      bean: userService
      method: getUser
    access:
      type: authority
      anyOf: ["user:read"]

  - id: orders
    order: 200
    path: /api/orders/**
    methods: [GET, POST]
    target:
      type: http
      uri: lb://order-service
    access:
      type: authenticated
    filters:
      - name: StripPrefix
        args: { parts: 1 }
```

| 字段 | 契约 |
|------|------|
| `schemaVersion` | 必填；不支持的版本拒绝发布，避免按错误语义解释配置 |
| `id` | 必填且全局唯一；用于日志、指标、诊断与冲突提示，刷新时保持稳定 |
| `order` | 必填整数，数值越小优先级越高；同优先级且匹配范围重叠时拒绝发布，G0 确定可检测的重叠范围 |
| `path`、`methods` | 必填；由 SCG MVC 谓词匹配；托管路由限制在配置的网关路径空间 |
| `target` | 必填的带类型联合体；`service` 需要 bean/method，`http` 需要 URI，`rpc` 由插件声明字段 |
| `access` | 必填；`public`、`authenticated`、`authority`、`signature`，无默认放行 |
| `filters` | 可选；仅接受经注册和验证的 SCG MVC 过滤器及 LoadUp 策略，不允许配置任意 Java 类名 |

先支持路径与 HTTP 方法谓词，以及一组经过验证的过滤器；其他 SCG MVC 谓词/过滤器按消费需求加入注册表。静态 SCG 配置与 Java DSL 仍可直接使用，但不属于托管快照，也不保证由 LoadUp 热更新。静态与托管路由 ID 冲突时拒绝启动；路径重叠的优先级和风险在诊断中显示。路由配置不保存密钥等秘密值，只引用安全配置名称。

### 编译与目标处理

`ManagedRouteCompiler` 对整个文档一次性校验和编译，输出不可变的 WebMvc.fn `RouterFunction` 与只读的路由元数据索引。先校验所有路由 ID、方法、路径、目标、策略、插件及 filter 参数，再构建函数；不得在处理请求时临时解析配置或查找 Service 方法。编译器根据目标类型选择 handler，按固定阶段装配公共过滤器。单条路由无效使整个候选版本发布失败。

HTTP 目标通过 SCG MVC 官方 `HandlerFunctions.http()` 执行，使用其路径、请求头和转发头处理机制；不经过 `GatewayRequest`/`GatewayResponse` 统一字符串模型。`service` 目标使用 LoadUp handler；RPC 目标在插件提供时注册。目标类型决定各自的超时、重试和响应语义，公共路由模型只承诺入口匹配、授权与可观测性的一致性。

目标扩展注册表以 `target.type` 为键，每个类型只允许一个适配器。适配器在**编译期**校验自身配置并提供 `HandlerFunction<ServerResponse>`；缺失或重复的类型立即使候选快照失败。`service` 与 `http` 为内置类型，RPC 插件遵循同一契约；运行期请求不再查找或选择适配器。过滤器注册表也采用明确名称和参数模式，禁止从路由数据反射实例化类。

## 4. Service 方法暴露与调用

### 暴露规则

```java
@Service
public class UserService {
    @GatewayExpose
    @PreAuthorize("hasAuthority('user:read')")
    public UserDTO getUser(@PathVariable("id") String id) {
        return findUser(id);
    }
}
```

`@GatewayExpose` 是可调用方法的白名单，不自动生成路径。外部路由以 `bean + method` 指向它；没有路由时方法不对外可达。启动时枚举被标记的方法，建立 `ServiceMethodCatalog`；它记录目标 bean、可经代理调用的方法、参数绑定计划和返回类型。只允许公共实例方法；重载必须有显式、稳定的方法标识，否则拒绝。路由刷新时只可选用目录中的方法，不能通过配置扩大可调用范围。

调用**Spring 容器中的代理 bean**，使 `@Transactional`、`@PreAuthorize` 等代理行为生效；不解包目标对象再直接调用。G0 原型必须覆盖 JDK 动态代理与类代理，并验证接口方法上的暴露注解如何被发现。无法通过代理调用的方法在启动时失败，而不是首个请求才失败。

### 参数绑定与返回

第一版支持以下明确子集：`@PathVariable`、`@RequestParam`、`@RequestHeader`、`@RequestBody` 和 Jakarta Bean Validation。无注解且仅有一个复杂 DTO/record 参数时按 JSON body 处理；其余无注解参数拒绝暴露。每个方法最多一个 body 参数。绑定使用 Spring `ConversionService`、应用 `ObjectMapper` 和 validator；不实现完整 Spring MVC Controller 参数解析器集合。请求体有可配置大小上限，流式/上传类型留给后续专门契约。

普通对象与 `Result<T>` 序列化为 JSON；`void` 返回 204；若返回已有 `Result<T>`，不再二次包装。业务异常按框架错误码映射，参数/校验错误为 400，未认证为 401，权限不足为 403，未找到的路由为 404，未处理异常为 500 且不泄露内部堆栈。错误响应携带请求 ID；具体 `Result` 与 HTTP 状态映射在 G0 固化。方法级和路由级授权同时存在时取交集，任何一层拒绝都不能调用目标方法。

Service 调用与调用方在同一进程和线程执行；网关不能为其提供真实的网络超时取消、独立故障隔离或自动重试。默认不对 Service 方法启用重试；长耗时、上传、下载和异步返回需单独定义能力矩阵后才能支持。

## 5. HTTP 与分布式场景

HTTP 目标是 SCG MVC 的原生代理 handler。第一版须覆盖请求方法、路径/查询编码、多值头、请求体、上游状态与响应体原样转发；按官方规则移除 hop-by-hop 头并配置可信代理。上游 4xx/5xx 是上游响应，不默认改写成 LoadUp 业务错误。[官方头处理](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/httpheadersfilters.html)。

分布式场景至少需要路径改写、服务发现/负载均衡、入口认证与授权、限流、熔断、超时、追踪和路由热更新。优先使用 SCG MVC 的能力；LoadUp 只补上游没有而项目确需的策略。SCG MVC 官方提供熔断与基于 Bucket4j 的限流，但分布式状态、依赖与动态配置方式需按 BOM 版本验证：[熔断](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/filters/circuitbreaker-filter.html)、[限流](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/filters/ratelimiter.html)。

HTTP 路由不默认包装成功响应，不改变二进制或流式内容。重试只允许路由显式声明并限定幂等请求；总超时必须明确，客户端连接/读取、熔断和重试的时间预算不得各自无限叠加。Gateway 可以嵌入专门的边缘应用，但该应用由集成方组装和部署，框架仓库不提供独立生产服务。

## 6. 路由来源与运行时刷新

```java
public interface RouteSource {
    RouteDocument loadCurrent();
    // A change signal triggers loadCurrent(); it never carries partial route changes.
}

public record RouteDocument(String sourceId, String revision, int schemaVersion, List<ManagedRouteSpec> routes) {}
```

上述代码是概念签名，最终 Java 类型以模块设计为准。`revision` 对所有来源至少可用于相等比较；配置中心/DB 若提供单调序号，再用它拒绝倒退版本。文件来源以内容摘要为版本，收到文件事件后读取**当前文件**，不能用摘要推断新旧顺序。默认一个活动来源；多来源组合只有在明确命名空间、合并顺序和冲突规则后开放。

| 来源 | 变更发现 | 发布要求 |
|------|----------|----------|
| jar 外文件 | WatchService 通知 + 周期性摘要校验 | 写到临时文件后原子替换；classpath 资源不保证热更新 |
| 配置中心 | 客户端通知 + 断线重连后重新读取 + 周期校验 | 多实例读取同一已发布版本；事件丢失后仍能收敛 |
| 数据库 | 版本号轮询，可辅以通知 | 只读取已发布版本；编辑、校验、发布、回滚分离 |

### 刷新状态机

```text
ACTIVE(revision=N)
  ├─ change hint / poll → LOADING → VALIDATING → COMPILING → ACTIVE(revision=N+1)
  ├─ identical content ────────────────────────────────────→ ACTIVE(revision=N)
  └─ read/validate/compile failure → FAILED(last error) ───→ ACTIVE(revision=N)
```

刷新在单节点内串行；多个通知可合并，但每次都从来源读取当前完整文档。编译完成后先与最新候选版本比较，再原子替换 `RouterFunction` 和对应元数据，避免路由函数与诊断版本不一致。正在执行的请求继续使用进入时的快照。首次启动来源不可读、配置无效或没有可调用的 Service 目标时失败；明确允许空路由的配置例外。

来源断连时继续使用最后一次有效快照，标出陈旧时间与健康状态，并持续重试读取。多实例保证**最终版本收敛**，不声称全局原子切换；节点必须上报当前 `sourceId/revision`，便于部署系统确认收敛。文件来源只有在所有实例共享可靠的文件分发机制时才适合多实例；一般多实例优先配置中心。回滚通过来源发布旧配置的新版本实现，不直接在单个节点回退。

动态刷新是交付必需项。G0 要用 BOM 对应 SCG MVC 的公开 API 验证：静态/托管路由共存、原子快照、路由 ID 与过滤器元数据、刷新中的并发请求。若直接组合方式不可行，应先调整 WebMvc.fn 路由组合或使用可维护的公开扩展点；不退回自研 HTTP 引擎。[SCG MVC 工作原理](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/how-it-works.html)。

## 7. 安全与策略

Spring Security Servlet 链建立身份；LoadUp 路由策略在匹配路由后做入口授权。托管路由限制在显式的网关路径空间，安全链只匹配该空间；集成方其他端点由其自身安全链管理，不能用全局 `permitAll` 覆盖。网关只验令牌，不签发；JWT/opaque token 资源服务器由可选安全模块装配。[SCG MVC 安全集成](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/spring-security.html)。

| 策略 | 行为 |
|------|------|
| `public` | 允许匿名请求；携带无效 Bearer 凭证仍返回认证失败 |
| `authenticated` | 要求有效身份 |
| `authority` | 要求有效身份并满足角色/权限集合；无身份 401，权限不足 403 |
| `signature: true` | 在上述访问策略上叠加请求来源与内容验证，不自动产生用户身份 |

未声明策略拒绝发布。Service 方法本身的 `@PreAuthorize` 继续生效。签名与 JWT 可组合时须显式指定 AND/OR 语义，第一版仅支持 AND，避免隐式放宽。可信代理、内部来源和转发凭证规则在安全模块中集中配置；`internal` 不以客户端可伪造头作为唯一依据。限流与熔断位于目标调用前，过滤器短路响应与指标要记录路由 ID。

## 8. 配置与自动装配

建议配置键集中在 `loadup.gateway.*`：`enabled`、`base-path`、`source.type`、`source.file.path`、`source.refresh.poll-interval`、`security.*`、`limits.*`、`diagnostics.*`。SCG MVC 原生配置仍使用 `spring.cloud.gateway.server.webmvc.*`，不在 LoadUp 下复制每个 SCG 属性。starter 在缺少必需来源或目标插件时提供可定位的启动错误；关闭 gateway 后不注册其路由、安全链或监听线程。

默认安装最小 SCG MVC + Service handler + 路由快照；文件来源按明确依赖或配置启用。安全、配置中心、DB、RPC 模块都不得因 starter 的传递依赖意外启动。应用可提供自己的 `RouteSource` 和路由过滤器扩展；同类型多 Bean 无法确定选择时启动失败并列出候选。

## 9. 诊断、指标与运维

只读诊断应展示：来源、当前版本、已发布路由列表、每条路由的匹配条件与目标类型、策略、最近一次刷新时间、耗时、结果及失败原因。不得展示密钥、令牌或完整敏感 URI。指标采用 Micrometer，至少有请求数/耗时、按目标类型的失败数、刷新成功/失败数、快照年龄；标签只使用路由 ID、目标类型和状态类别，避免原始 URL 或用户 ID。追踪沿用项目 OpenTelemetry 上下文，Service 路由可创建内部调用 Span，HTTP 路由向下游传播受信任的上下文。

诊断默认只在进程内或受保护的 Actuator 端点可读；管理写入不随 starter 暴露。多实例部署从各节点读取 `revision` 判断是否收敛。日志输出结构化字段 `routeId`、`revision`、`targetType`、`requestId`，不记录请求体或认证凭证。

## 10. 验证矩阵与阶段门槛

| 阶段 | 最小交付 | 必须证明 |
|------|----------|----------|
| G0 关键原型 | SCG MVC 路由快照、一个 Service 方法、一个 HTTP 路由 | 公共 API 足以热替换；方法通过 Spring 代理执行；并发请求不读到半更新版本 |
| G1 单应用 | Service 目录、绑定/校验、外部文件来源、错误处理 | 无 Controller 暴露业务；修改路由不重启；事务、`@PreAuthorize`、参数错误均按契约生效 |
| G2 分布式 | SCG 原生 HTTP 目标、路由策略、指标、基础过滤器 | HTTP 语义完整；可用 LoadUp 配置替代集成方直接写 SCG 路由；无自研代理 |
| G3 多来源 | 配置中心来源、按需 DB/RPC | 来源切换不改变执行契约；通知丢失可收敛；失败保留旧版本；多实例版本可核对 |
| G4 发布 | 最小接入示例、能力矩阵、配置参考、排障说明 | 新开发者可分别完成 Service 路由与远程 HTTP 路由，并在不部署新包的情况下更新路由 |

验证重点包括：路径和方法冲突、Service 代理类型、参数绑定与校验、方法级安全、业务异常、HTTP 头和二进制响应、不同策略组合、来源断连、无效版本、连续多次更新、并发请求和多实例收敛。实施时按仓库约定使用目标模块的静态检查或窄范围测试，不默认运行全仓构建。

## 11. G0 必须确认的技术决策

1. SCG MVC 路由组合与快照替换是否能只使用 BOM 版本的公开 API；需要哪些路由元数据以保留上游过滤器行为。
2. Service 方法暴露注解在 JDK/类代理、接口/实现类上的发现规则，以及可调用方法的稳定标识。
3. 第一版允许的参数注解、返回类型、错误码到 HTTP 状态的完整映射。
4. 托管路由中可配置的 SCG 过滤器清单、执行顺序及过滤器参数校验方式。
5. 网关路径空间与应用已有 Spring Security 链的 matcher/顺序，尤其是 `public` 路由携带无效凭证时的行为。
6. 文件、配置中心、数据库的版本语义，以及单节点刷新和多实例收敛的可测目标。

这些决策形成 ADR 后再定最终 API 名称和模块坐标；不改变“Service 方法为主路径、动态路由必需、SCG MVC 为执行底座”的目标。
