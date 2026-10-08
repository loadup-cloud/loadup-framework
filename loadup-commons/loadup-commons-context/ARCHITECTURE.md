# LoadUp Common Context 架构

## 设计目标

业务元数据从入口向下游单向传递，绑定生命周期与执行回调一致。基于 JDK 25 正式 API ScopedValue，无兼容 ThreadLocal 存储，无 preview/结构化并发依赖。类型化键避免调用方自行强转；不可变绑定避免下游修改影响后续调用顺序。

```text
入口构造 ExecutionContext
  → ContextHolder.runWith/callWith
    → Service / Repository 只读
    → 嵌套回调可绑定派生的新 context
  → JDK 恢复原绑定
```

## 类型与存储

- `ContextKey<T>`：名称与 Class 共同标识键，校验非空名称/引用类型。
- `ContextKeys`：提供共享 TENANT_ID；身份与 Trace 保持原权威来源。
- `ExecutionContext`：Map.copyOf 固定绑定，with 复制并派生新对象；不暴露内部 Map，toString 仅包含数量。值必须遵循不可变契约，容器不保证深不可变。
- `ContextHolder`：私有单一 ScopedValue<ExecutionContext>，通过 run/call 管理动态作用域；没有可变 frame、清理 API 或手动 close。
- `ServiceTemplate`：可选生命周期包装；使用 try-with-resources 保留异常及 suppressed，init/clean 在绑定作用域内执行。普通 Service 无需继承或全量套模板。

同一虚拟线程挂起/恢复不会丢失绑定；并发任务共享不可变元数据，不共享可变容器。没有每次执行深拷贝值对象，也没有基于 ThreadLocal 缓存昂贵资源。

## 传播与适配

普通 Executor 不自动继承绑定，wrap/wrapCallable 在包装时捕获当前 immutable context，执行整个回调期间绑定，空 context 也显式安装。该机制只传递业务元数据。

Micrometer ThreadLocalAccessor 的 set/reset 无法包裹动态作用域，因此移除旧 SPI，不建立伪 ThreadLocal 或手动作用域栈。Observability 提供 `LoadUpContextTaskDecorator`，Boot 4.1 的执行器组合它与其他装饰器；标准 `ContextPropagatingTaskDecorator` 继续负责 Observation/Trace/MDC。自定义执行器必须显式配置组合，不假设任何 Micrometer snapshot 都会捕获 ScopedValue。

WebMVC 与 Database 使用进程内请求属性 `ExecutionContext.class.getName()` 共享不可变对象。每次分派执行期间绑定它；可信元数据适配器显式更新属性并建立嵌套绑定，不能通过下游修改 current 回写请求。嵌套 ERROR 分派沿用当前作用域，独立 ASYNC/ERROR 分派从属性重新绑定。属性不会自动变成远程传输或认证信息。

## ServiceTemplate 边界

固定顺序：绑定 → init → 业务 → clean → 恢复。init 部分失败仍执行 clean，主异常不被 cleanup 覆盖；无主异常时 cleanup 失败正常抛出。hook 只允许 unchecked exception，业务回调保留 checked exception 类型。默认 hook 为无操作。

按应用入口和实际资源需求使用，不批量改写所有 Service。模板不创建事务、鉴权链、日志/指标、重试或持久任务引擎；Spring 代理边界与已有能力不变。调用内资源应局部持有，不保存在单例 lifecycle 可变字段中。

## 验证与后续

已编写用例覆盖不可变派生、类型读取、嵌套异常恢复、普通线程池复用、空上下文覆盖、虚拟线程隔离和显式传播；覆盖模板生命周期、初始化失败、主异常保留及 clean suppressed。TenantUtil、Servlet 重新分派与 Boot/Micrometer 装饰器组合有对应测试。未执行编译/测试，运行验收按根 AGENTS 由用户执行；真实入口、持久任务与性能验证保留在 ROADMAP。
