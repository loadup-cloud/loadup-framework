# LoadUp Common Context

基于 **JDK 25 ScopedValue** 的执行链共享元数据。入口绑定不可变 `ExecutionContext`，下游只读；作用域随回调结束自动解除绑定。纯 Java，无 Spring 或 Micrometer 运行依赖，无需启用 preview。

## 引入

引入 LoadUp BOM 后：

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-commons-context</artifactId>
</dependency>
```

WebMVC、Observability 与 commons-util 已传递引入。执行环境要求 JDK 25+。

## 同步执行链

```java
public static final ContextKey<String> ORDER_ID = new ContextKey<>("payments.orderId", String.class);

var context = ExecutionContext.empty().with(ORDER_ID, "order-123");
ContextHolder.runWith(context, () -> orderService.execute());

// Inside the downstream service callback.
String orderId = ContextHolder.get(ORDER_ID);
```

`get` 在不存在绑定/键时返回 null。键的相等性由名称和类型共同决定，名称使用业务命名空间；基础类型使用包装类。`current()` 返回当前上下文或空上下文，`isBound()` 区分空绑定与无绑定。

### 补充数据与临时覆盖

```java
var nested = ContextHolder.current().with(ORDER_ID, "another-order");
ContextHolder.runWith(nested, () -> orderService.execute());
```

`with` **返回新对象**，不会修改原上下文；`with(key, null)` 在新对象中移除键。返回后恢复上层绑定，即使回调抛异常。需要返回值或 checked exception 时使用 `ContextHolder.callWith(context, () -> ...)`。公共 API 没有 put、set、remove、clear 或可手动关闭的 scope。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 类型化读取 | `ContextKey<T>` + `get` |
| 不可变绑定 | `ExecutionContext.empty().with(...)`，容器不可修改 |
| 执行入口 | `runWith` / `callWith` |
| 嵌套与异常 | JDK 自动恢复上层绑定 |
| 平台线程/虚拟线程 | 相同作用域语义 |
| 普通 Executor | `wrap` / `wrapCallable` 在包装时捕获 |
| Spring 异步 | Observability 的 TaskDecorator 与标准 Micrometer 装饰器组合 |
| Service 生命周期 | 可选 `ServiceTemplate.execute/run`，支持 init/clean |
| Trace、登录身份 | 由 Micrometer 与 Spring Security 管理 |
| 跨进程、持久任务、响应式 | 显式传递，不自动处理 |

绑定容器不可变，值对象不深拷贝。只保存 String、不可变 record 等轻量元数据；不要保存请求/响应、连接、可变实体、密码或令牌。`toString()` 仅输出条目数。业务上下文不是认证依据，不会自动进入日志、指标标签或 HTTP header。

## 异步执行

```java
ContextHolder.runWith(context, () -> {
    executor.execute(ContextHolder.wrap(() -> orderService.execute()));
});
```

普通平台/虚拟线程 Executor 不自动继承 ScopedValue。包装时捕获不可变上下文，执行时建立作用域，退出恢复工作线程原绑定；空上下文也显式绑定，避免读取目标线程的上层数据。`wrapCallable` 用于返回值任务。

引入 Observability 后，它提供 `LoadUpContextTaskDecorator` Bean。Boot 自动配置执行器会组合多个 TaskDecorator；开启 `spring.task.execution.propagate-context=true` 后，Micrometer 负责 Observation/Trace，LoadUp 装饰器负责业务元数据。自定义 Executor 应显式安装这些装饰器，详见 [Observability](../../loadup-components/platform/loadup-components-observability/README.md)。Spring Security 身份仍需独立的标准传播机制。

## ServiceTemplate：按入口使用

普通 Service 已在请求作用域内时可以直接读取上下文。**不要求每个 Service 方法包装模板，也不要求继承 BaseService**。模板适合消息消费、后台任务，或确有每次初始化/资源释放需求的应用服务入口。

```java
var lifecycle = new ServiceTemplate.Lifecycle() {
    @Override
    public void init() {
        prepareResources();
    }

    @Override
    public void clean() {
        releaseResources();
    }
};

var result = ServiceTemplate.execute(context, lifecycle, () -> executeBusiness());
```

生命周期顺序：绑定 context → init → 业务 → clean → 恢复上层 context。

- init 和 clean 均可读取已绑定上下文；它们不修改绑定。元数据应在传入模板前准备好。
- init 失败仍调用 clean，因此 clean 必须能处理部分初始化；失败时不执行业务回调。
- 业务/初始化异常优先保留，clean 异常加入 suppressed；只有 clean 失败时直接抛出。
- lifecycle 默认无操作；execute 支持返回值和 checked exception，run 支持无返回值。
- 每次调用需要独立的资源状态，或使用无状态、线程安全的 lifecycle；不要在 Spring 单例中共享每次调用的可变字段。
- 事务、鉴权、限流、重试、指标使用 Spring/Micrometer 等已有机制。clean 负责释放资源，不承担事务提交或可靠消息发送；返回值的流式消费发生在模板结束之后时，资源生命周期应交给流本身。

## Servlet 请求与租户

WebMVC 在入口绑定空上下文或请求属性 `ExecutionContext.class.getName()` 中的显式上下文。REQUEST/ASYNC/ERROR 分派重新绑定该不可变对象；下层临时覆盖不会回写父请求。可信租户适配器可准备新的请求上下文、写入同一属性并包裹 FilterChain，使重新分派能恢复元数据。请求属性是进程内部对象，不从客户端自动反序列化；认证仍由 Security 负责。

`TenantUtil` 保留只读 get/has 和 `runWithTenant/callWithTenant`，临时租户覆盖会继承其他键，去除空白并在退出后恢复。持久任务/消息必须保存必要元数据，并在执行入口重新验证权限；内存上下文不能替代持久化或跨进程协议。

设计边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。
