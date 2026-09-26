# LoadUp Gateway 目标架构方案：基于 Spring Cloud Gateway Server MVC

> 状态：目标架构；Service/HTTP 主路径、动态来源、签名及旧链清理已实施。本文仍包含未交付的目标，不要求兼容旧 `RouteDefinition`、`ProxyProcessor` 或路由 YAML。具体契约与实施状态见 [DETAILED_DESIGN.md](./DETAILED_DESIGN.md)，当前行为见 [ARCHITECTURE.md](./ARCHITECTURE.md)。
>
> 适用范围：作为依赖嵌入集成方的 Spring Boot Servlet 应用；版本由 LoadUp BOM 中的 Spring Cloud release train 管理，落地前以该 BOM 对应的 SCG Server MVC API 验证本文假设。

## 1. 设计结论

以 **Spring Cloud Gateway Server MVC（SCG MVC）作为路由、谓词与过滤器执行底座**。LoadUp Gateway 的核心价值是把外部请求映射到明确暴露的业务 Service 方法，在单应用中免写 Controller；在分布式应用中，同一套路由和治理能力也可连接远程 HTTP 服务，作为集成方直接使用 SCG 的替代入口。HTTP 转发只是一个目标 handler，不是整个组件的中心。

SCG MVC 基于 Spring WebMvc.fn 的 `RouterFunction`、`HandlerFunction` 与 `HandlerFilterFunction`。LoadUp 提供 Service 方法调用 handler、统一的动态路由来源与策略；远程 HTTP 目标直接使用官方 `HandlerFunctions.http()` 和可用的 SCG 过滤器。[SCG MVC 工作原理](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/how-it-works.html)、[Java Routes API](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/java-routes-api.html)。

**定位边界**：保持 Servlet MVC、以依赖方式嵌入应用；单应用与分布式应用共用同一组件，不要求项目提供独立网关服务。分布式场景的“替代 SCG”是集成方使用 LoadUp 的入口、路由来源与策略能力，底层仍基于 SCG MVC，不承诺 WebFlux 的响应式语义。SCG MVC 在 Tomcat、Jetty 等 Servlet 运行时工作；响应式库的能力不能直接推定适用。[官方 starter 说明](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/starter.html)。

### 设计原则

1. **Service 方法是一等目标**：业务 Service 显式暴露的方法可直接绑定外部路由，保留 Spring 代理上的事务和方法级授权；业务方无需编写 Controller 或网关适配类。
2. **单一请求执行链**：所有 LoadUp 路由进入 SCG MVC 的 WebMvc.fn 路由链；Service、HTTP、RPC 由目标 handler 分派，共用路由治理与策略语义。
3. **HTTP 使用上游能力**：HTTP 转发、路径/头处理、负载均衡及可用的标准过滤器优先使用 SCG MVC；LoadUp 不维护第二套通用实现。
4. **动态路由是一等能力**：路由刷新与快照发布由核心模块承担；外部文件、配置中心、数据库只是可替换的配置来源。RPC、认证和分布式限流按需装配。
5. **默认安全且可诊断**：未声明访问策略的路由拒绝发布；非法路由在启用前失败；运行中刷新失败保留上一个完整快照并暴露状态。
6. **集成方可使用上游能力**：LoadUp 自定义能力通过 SCG MVC 公开的 handler、predicate、filter 扩展点提供；不阻断集成方直接使用 SCG MVC 的路由 DSL 与过滤器。[官方扩展方式](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/writing-custom-predicates-and-filters.html)。

## 2. 开发者体验与模块边界

| 模块（建议坐标） | 职责 | 集成方何时引入 |
|----------------|------|----------------|
| `loadup-gateway-starter` | SCG MVC、Service 方法调用 handler、路由来源 SPI、动态快照、基础配置、路由诊断；不强制引入数据库或配置中心 | 单应用免 Controller 或分布式网关入口 |
| `loadup-gateway-security-jwt` | 可选 JWT 资源服务器装配；基础路由授权在 webmvc 中 | 使用 JWT 路由时 |
| `repository-yaml-plugin` | 读取 jar 外文件，监听变更并触发刷新；由 starter 引入 | 单机开发或由部署平台分发配置文件时 |
| `loadup-gateway-source-configcenter` | 适配已有配置中心的变更通知与版本读取 | 多实例共用配置中心时 |
| `loadup-gateway-source-jdbc` | 从数据库读取已发布版本；可选提供管理写入 SPI | 需要审计、审批或自建管理后台时 |
| `loadup-gateway-handler-rpc` | RPC 目标适配 | 确有 RPC 网关场景时 |

这是**目标模块责任**，不是要求机械拆成固定数量的 jar。若一个可选能力没有独立消费者，先留在最小模块内；只有传递依赖、部署要求或生命周期需要隔离时再拆坐标。BOM 管理所有坐标，starter 的依赖树应可在文档中直接查看。

### 两条接入路径

**单应用免 Controller（主路径）**：集成方给 Service 方法做显式暴露声明，在外部文件或配置中心定义 `service` 目标路由。请求通过 SCG MVC 的路由与策略链后，由 Service handler 调用 Spring 容器中的目标方法。业务方只维护 Service、输入输出类型和路由配置。

**分布式网关入口**：在同一个路由模型中把目标设为 `http`（可选 RPC），使用 SCG MVC 的 HTTP handler、路径与头处理、负载均衡和过滤器能力。目标应用仍可使用自身的 Controller 或其他 HTTP 接口；LoadUp 不要求远端应用采用 Service 方法路由。集成方也可直接添加 SCG MVC 原生静态路由或 Java DSL 扩展。官方 DSL 已支持 `RouterFunction`、路由 ID 和 `HandlerFunctions.http()`。[Java Routes API](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/java-routes-api.html)。

两条路径都默认使用**动态路由**：选择一个来源，配置 jar 外文件、配置中心或数据库。三者向核心模块提交相同的完整 `ManagedRouteSpec` 快照；核心模块负责校验、编译和原子发布。编辑并发布配置后无需重新打包或部署应用。动态模型只覆盖经验证可编译的 SCG MVC 功能子集，不能宣称与 SCG 原生属性格式完全等价。

默认只启用**一个托管路由来源**；同时存在多个来源时启动失败，除非明确启用组合模式并声明优先级、路由 ID 命名空间与冲突规则。静态 SCG 路由与托管路由也不得重用 ID 或发生未声明的路径覆盖。

开发者文档按任务组织：① Service 方法暴露与路由热更新；② 远程 HTTP 服务路由；③ 改用配置中心或数据库；④ 加 JWT 与路由权限；⑤ 排查当前生效版本。每条路径列出依赖、最小配置、外部服务和实际自动装配结果。

## 3. 路由与执行模型

```text
外部文件 / 配置中心 / DB → RouteSource → 完整快照
                            → 校验 → 编译 → 原子发布 ──→ WebMvc.fn RouterFunction
静态 SCG 配置 / Java DSL ────────────────────────────────→         │
Servlet SecurityFilterChain → 路由策略过滤器 ──────────────────────┤─ SERVICE：调用 Spring Service 方法
                                                                ├─ HTTP：SCG HandlerFunctions.http()
                                                                └─ RPC：可选协议 handler
```

### 路由定义

- `id`、`order`、匹配条件、目标、策略元数据是动态路由的基础字段。路由 ID 在静态与动态来源之间全局唯一；明确排序和冲突规则，不依赖配置加载顺序。目标类型为 `service`、`http`，按需增加 `rpc`。
- `service` 目标定位为 `beanName + methodName`，但**仅允许调用显式标记为网关可访问的 Service 方法**。应用启动时建立方法目录，拒绝不存在、未暴露、重载不明确或签名不支持的目标；动态路由刷新时再次校验。实际调用容器中的代理 bean，使 `@Transactional`、`@PreAuthorize` 等 Spring AOP 行为继续生效。
- Service 方法入参以单个 `record`/DTO 命令对象为默认模式；路径、查询和请求头参数使用明确的绑定注解，复杂上下文可显式注入只读请求上下文。绑定、Bean Validation 和类型转换失败统一返回 400；业务异常交由框架错误映射，不能被当作代理失败重试。方法返回普通对象、`Result<T>` 或受支持的原始响应类型；响应包装只对业务结果生效。
- HTTP 目标采用 SCG 的 URI 与 `HandlerFunctions.http()`；路径改写、请求/响应头、负载均衡等使用 SCG 提供的过滤器。HTTP 响应默认保持上游状态、头和正文，不自动套统一 JSON 外壳。
- RPC 目标以单独插件将 RPC 结果转换为 WebMvc.fn 响应。协议发现、序列化、重试由 RPC SDK 负责；网关只管理入口策略和结果映射。
- 自定义路由能力采用 SCG 的 predicate/filter/handler 扩展接口。LoadUp 仅提供项目语义，例如 `RequireAuthority`、`VerifySignature`、`WrapBusinessResponse`，不复制 `Path`、`StripPrefix` 等已有通用能力。

Service 方法直接暴露属于**进程内调用**：不经过 HTTP 客户端和服务发现，路由层不提供进程隔离。`service` 目标与 `http` 目标共享入口策略，但超时、重试、事务、流式响应等能力矩阵分别声明；不能为了统一模型假装两种目标语义相同。

### 开发者视角示例

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

```yaml
schemaVersion: 1
routes:
  - id: user-detail
    order: 100
    path: /api/users/{id}
    methods: [GET]
    target: { type: service, bean: userService, method: getUser }
    access: { type: authority, anyOf: ["user:read"] }
  - id: remote-orders
    order: 200
    path: /api/orders/**
    methods: [GET]
    target: { type: http, uri: "lb://order-service" }
    access: { type: authenticated }
```

这是当前托管路由格式的简例。`@GatewayExpose` 只承担可调用方法的白名单职责，路径、HTTP 方法和访问策略可由外部路由配置热更新。若一个方法被多条路由复用，方法级 `@PreAuthorize` 与路由级授权均须通过；前者保护业务动作，后者保护入口。

### 过滤器顺序与响应边界

1. Servlet 过滤器链建立身份与请求上下文；SCG 路由匹配后才执行路由级授权。
2. 路由级顺序固定为：请求上下文与追踪 → 授权/签名 → 限流 → 熔断/超时 → 目标 handler → 响应处理与指标。每个过滤器的异常及短路行为在契约中声明。
3. Service 方法响应按框架业务契约处理；HTTP 代理响应保持上游语义。业务响应包装可用于 Service/RPC，HTTP 路由须主动启用且只作用于支持的媒体类型。流式、二进制、下载响应不进行 JSON 包装。
4. 使用 SCG 的 hop-by-hop 与转发头处理能力，明确可信代理配置；不自行拼接 `Forwarded`/`X-Forwarded-*` 或复制连接级头。[官方请求头处理](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/httpheadersfilters.html)。

## 4. 安全架构

**认证归 Spring Security，路由授权归 LoadUp 策略过滤器。** LoadUp 托管路由限制在显式配置的网关路径空间内；可选 JWT 模块提供的 `SecurityFilterChain` 只匹配该空间，不能用一个全局 `permitAll` 链覆盖集成方其他端点；路径空间外的 SCG 原生路由由集成方负责安全配置。当前可选模块采用 OAuth2 Resource Server 验证 JWT；签发由 UPMS/authserver/外部 IdP 的身份契约决定，不在 gateway 内进行。SCG MVC 官方支持 Spring Security 和 token relay。[官方安全集成](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/spring-security.html)。

| 路由策略 | 语义 |
|----------|------|
| `public` | 明确允许匿名；无令牌可访问。有非法 Bearer 令牌时仍按认证失败处理 |
| `authenticated` | 需要已验证身份 |
| `authority` | 在已认证基础上检查声明的角色/权限；无身份为 401、权限不足为 403 |
| `signature` | 单独的请求来源与完整性证明；不自动生成用户身份 |

访问策略必须显式声明。密钥、issuer、audience 和 access token 用途校验由身份契约集中定义；gateway 不再提供自造的 token 解析器。当前签名协议由 gateway 内的 HMAC 验证器实现，`signature: true` 可与 JWT 授权叠加；多实例需提供共享 nonce 存储。`internal` 若保留，必须明确可信代理和来源网络模型，不能仅依赖客户端可伪造的头。

安全链、路由策略和方法级授权各负责一层，不把它们合并成重复的“认证策略”SPI。集成方自定义安全链时，文档给出 matcher、优先级和冲突诊断规则。

## 5. 动态路由治理

### 来源契约与适配器

核心模块定义只读 `RouteSource`：`loadCurrent()` 返回完整、不可变的 `RouteDocument(sourceId, revision, schemaVersion, routes)`。来源可发出“版本可能改变”的通知；没有可靠通知时核心按可配置间隔轮询。通知只是触发重新读取，不能把单条增删事件直接应用到运行中的路由表。`revision` 用于判断内容是否变化；来源有单调版本号时用于顺序比较，外部文件则使用内容摘要并始终重读当前文件。`RouteCompiler` 把文档编译为 WebMvc.fn `RouterFunction`，运行态另存已发布版本、时间和结果。这个 SPI 属于 LoadUp 的**配置控制面**，请求执行仍由 SCG MVC 完成。

| 来源 | 更新方式 | 适用范围与约束 |
|------|----------|----------------|
| jar 外文件 | 监听文件变化并周期校验摘要；建议临时文件写完后原子替换 | 本地开发、单实例，或由部署平台可靠分发同一版本到所有节点；classpath 内文件不提供热更新承诺 |
| 配置中心 | 使用已有客户端的变更通知，辅以版本/摘要校验与断线后重读 | 多实例首选；各节点读取相同的已发布配置版本 |
| 数据库 | 读取已发布版本并轮询版本号，或配合可靠通知 | 需要后台编辑、审批、审计时；DB 是来源选项，不是动态路由前提 |

刷新流程：收到通知或轮询发现新版本 → 串行读取来源的当前完整版本 → 校验结构、路由 ID 与匹配冲突 → 验证 handler 和策略能力 → 编译全部路由 → 原子替换快照 → 记录本节点已发布版本。任一步失败均不替换旧快照；首次启动没有有效快照时启动失败。相同内容重复通知不得重复发布；旧通知只触发重读，不能携带旧快照覆盖新版本。本地 Spring 事件只唤醒本节点，不视为集群同步。

编辑、校验和发布应区分：来源支持写入时先产生候选版本，校验通过后发布；只读来源由其外部管理工具负责发布。文件可通过原子替换回滚到旧内容；配置中心和数据库使用其版本历史回滚。管理写入 API 为可选控制面能力，starter 不暴露公网管理端点。

多实例以**最终收敛到同一来源版本**为目标，暴露每个节点的当前版本和最近刷新状态；若业务要求全节点同时切换，需单独设计发布协调，不能仅靠本地原子快照声称全局原子性。断开配置源后继续使用最后一次有效快照，并持续报告陈旧时长；是否在超过阈值后拒绝服务由部署方配置。

**验证门槛**：动态更新是本方案必需能力。G0 须用 BOM 对应的 SCG MVC 公开 API 证明静态与托管路由共存、请求中途刷新、过滤器元数据及路由 ID 可正确工作。若公开 API 不足，先调整托管路由与静态路由的组合方式并评估可维护扩展点；不能把动态路由从交付范围中静默移除，也不因此重造 HTTP 代理。

## 6. 韧性与可观测性

- 熔断、限流优先比较 SCG MVC 官方过滤器与当前语义；仅在缺少明确业务能力时保留 LoadUp 过滤器。官方 MVC 熔断可接 Spring Cloud CircuitBreaker；MVC 限流使用 Bucket4j，支持本地和分布式 bucket，但 key resolver 的部分配置需要 Java DSL。选择前以实际 BOM 版本验证功能和部署依赖。[熔断](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/filters/circuitbreaker-filter.html)、[限流](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/filters/ratelimiter.html)。
- 每个路由显式配置总超时；HTTP 客户端连接/读取超时、熔断等待、RPC 超时必须有一致的优先级。只对明确幂等且允许重试的请求启用重试，禁止默认重试写请求。
- 指标采用 Micrometer，追踪采用 OpenTelemetry/Spring 上下文。指标标签以路由 ID、目标类型、状态类别为主；不使用原始 URL、用户 ID、token 等高基数或敏感值。
- 提供只读诊断信息：当前路由 ID 与来源、目标类型、策略、快照版本、最近刷新结果、失败原因、每路由请求量和延迟。诊断端点默认不公开，交由 Actuator 安全配置控制。

## 7. 交付顺序与验收

| 阶段 | 交付 | 退出条件 |
|------|------|----------|
| G0 能力验证 | 用 BOM 锁定的 SCG MVC 版本验证 Service 方法 handler、原生 HTTP handler、过滤器与动态快照公开 API | Service 方法可经 Spring 代理正确执行；动态刷新有可运行原型，未通过则先解决架构阻碍 |
| G1 单应用主路径 | starter、Service 方法目录与参数绑定、外部文件 RouteSource、热刷新诊断 | 只写 Service 和路由配置即可暴露接口；修改 jar 外文件无需重新打包部署；事务、方法级授权与错误映射行为明确 |
| G2 分布式与策略 | SCG 原生 HTTP 目标、路由授权、签名、指标、响应边界 | 可路由到远程服务；请求与上游响应按代理契约传递；public/authenticated/authority 结果一致；无自研 HTTP 代理 |
| G3 来源与目标扩展 | 配置中心来源；按真实需求加入 JDBC 与 RPC | 换来源不改变路由执行语义；非法路由不能发布；刷新失败保留旧版本；多实例最终看到同一版本 |
| G4 集成体验 | 单应用与分布式两个最小工程、能力矩阵、排障指南、发布说明 | 新开发者可独立完成免 Controller 的 Service 路由与路由热更新，也能将目标切至远程服务；文档与自动装配行为一致 |

验证以 Service 方法真实 Spring 代理行为、SCG MVC 路由测试和真实 HTTP 后端为主；仅数据库来源需要数据库容器。重点覆盖参数绑定与校验、事务和方法安全、HTTP 代理语义、安全链共存、路由冲突、文件原子替换、来源断连、热刷新并发、刷新失败与多实例版本收敛。按仓库构建纪律，实施时使用目标模块测试，不以全仓构建作为默认验证。

## 8. 明确不做

- 不实现第二套 HTTP 客户端代理、通用路由 DSL 或 SCG 过滤器大全。
- 不让路由配置调用未显式暴露的 bean 方法；Service 方法目标必须通过启动及刷新校验。
- 不把嵌入式 MVC 网关描述为具备独立进程的隔离、弹性和吞吐特性。
- 不以“未来可能支持”为理由预建 WebFlux、RPC、分布式限流或管理控制台模块。
- 不为了兼容旧 API 保留 `GatewayRequest/GatewayResponse` 作为所有目标必须经过的中间模型；只在确有跨目标业务语义时定义最小类型。

## 9. 需要在 G0 固化的决策

1. 静态 SCG 路由与动态快照的路径空间及路由 ID 冲突规则。
2. SCG MVC 官方限流/熔断与 LoadUp 现有策略的能力差距和选型。
3. Service 方法的显式暴露方式、参数绑定、返回类型、Spring 代理调用与错误映射契约。
4. 路由来源的版本语义、断线后的陈旧阈值和多实例收敛时间目标。
5. starter 默认包含的安全能力、路由来源和诊断能力，以及集成方覆盖方式。
