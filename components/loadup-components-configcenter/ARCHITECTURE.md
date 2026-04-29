# loadup-components-configcenter — 架构设计

## 1. 概述

`loadup-components-configcenter` 基于 LoadUp 的通用 **Binder/Binding 框架**（`loadup-commons-api`）构建，提供：

- **统一配置读写接口**：`getConfig`、`publishConfig`、`removeConfig`
- **实时变更推送**：`addListener` / `removeListener`
- **透明切换**：仅修改 `loadup.configcenter.default-binder` 即可在 Local / Nacos / Apollo 之间切换
- **Spring 事件集成**：配置变更发布 `ConfigChangeEvent` 到 Spring 事件总线
- **SPI 扩展点**：`ConfigCenterBinderPlugin` 接口，预留第三方后端接入能力

---

## 2. 整体架构

```
┌──────────────────────────────────────────────────────────┐
│                  业务应用层                               │
│   @Service 通过 ConfigCenterBindingManager 获取 Binding  │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│             ConfigCenterBindingManager                   │
│  extends BindingManagerSupport<Binder, Binding>         │
│  配置前缀：loadup.configcenter                           │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│           DefaultConfigCenterBinding                     │
│  implements ConfigCenterBinding                          │
│  - 委托底层 Binder 完成读写                               │
│  - 发布 ConfigChangeEvent 到 Spring 事件总线              │
└──────┬──────────────────┬──────────────────┬─────────────┘
       │                  │                  │
┌──────▼──────┐  ┌────────▼──────┐  ┌───────▼───────┐
│ Local       │  │ Nacos         │  │ Apollo        │
│ Binder      │  │ Binder        │  │ Binder        │
│ 文件 + 轮询  │  │ nacos-client  │  │ apollo-client │
└─────────────┘  └───────────────┘  └───────────────┘
```

---

## 3. 核心接口与类

### 3.1 ConfigCenterBinder（低层驱动接口）

```java
public interface ConfigCenterBinder<C extends ConfigCenterBinderCfg,
                                    S extends ConfigCenterBindingCfg>
        extends Binder<C, S> {
    Optional<String> getConfig(String dataId, String group, S settings);
    boolean publishConfig(String dataId, String group, String content, S settings);
    boolean removeConfig(String dataId, String group, S settings);
    void addListener(String dataId, String group, ConfigChangeListener listener, S settings);
    void removeListener(String dataId, String group, ConfigChangeListener listener, S settings);
}
```

所有具体实现（Local / Nacos / Apollo）继承 `AbstractConfigCenterBinder`，后者继承框架 `AbstractBinder`，在 `afterConfigInjected()` 钩子中完成 SDK 初始化。

### 3.2 ConfigCenterBinding（高层业务接口）

```java
public interface ConfigCenterBinding extends Binding<Binder, ConfigCenterBindingCfg> {
    Optional<String> getConfig(String dataId);
    Optional<String> getConfig(String dataId, String group);
    void publishConfig(String dataId, String content);
    void publishConfig(String dataId, String group, String content);
    boolean removeConfig(String dataId);
    boolean removeConfig(String dataId, String group);
    void addListener(String dataId, ConfigChangeListener listener);
    void addListener(String dataId, String group, ConfigChangeListener listener);
    void removeListener(String dataId, ConfigChangeListener listener);
    void removeListener(String dataId, String group, ConfigChangeListener listener);
}
```

`DefaultConfigCenterBinding` 是唯一实现，同时实现 `ApplicationEventPublisherAware` 以便发布 Spring 事件。

### 3.3 group 优先级解析

调用时 `group` 参数的解析优先级：

```
方法参数 group（非空）
    ↓
bindingCfg.group（非空）
    ↓
binderCfg.defaultGroup（默认 "DEFAULT_GROUP"）
```

### 3.4 ConfigChangeEvent

```java
public class ConfigChangeEvent extends ApplicationEvent {
    String dataId;       // 配置项 ID
    String group;        // 分组
    String namespace;    // 命名空间
    String oldContent;   // 变更前内容（新增时为 null）
    String newContent;   // 变更后内容（删除时为 null）
    ConfigChangeType changeType; // ADDED | MODIFIED | DELETED
}
```

---

## 4. 各 Binder 设计

### 4.1 Local Binder

| 关注点 | 设计决策 |
|--------|---------|
| 文件路径 | `{basePath}/{group}/{dataId}` |
| 读取策略 | 先读文件，文件不存在则回退 Spring `Environment.getProperty(dataId)` |
| 写入 | `Files.writeString`，自动创建父目录 |
| 删除 | `Files.deleteIfExists` |
| 变更监听 | 单线程 `ScheduledExecutorService`（daemon，名为 `configcenter-local-poller`），按 `refreshInterval` 轮询 `lastModified` |
| 线程安全 | 监听器列表使用 `CopyOnWriteArrayList`，注册表使用 `ConcurrentHashMap` |

### 4.2 Nacos Binder

| 关注点 | 设计决策 |
|--------|---------|
| 初始化 | `NacosFactory.createConfigService(Properties)` 在 `afterConfigInjected` 中调用 |
| 销毁 | `configService.shutDown()` 在 `afterDestroy` 中调用 |
| 读取 | `configService.getConfig(dataId, group, timeout)` |
| 写入 | `configService.publishConfig(dataId, group, content)` |
| 监听 | `AbstractListener.receiveConfigInfo()` 包装为 `ConfigChangeEvent` |
| 取消监听 | 保存 nacos `Listener` 引用（`nacosListenerMap`）以便后续 `removeListener` |
| 依赖 | `com.alibaba.nacos:nacos-client:2.4.3` |

### 4.3 Apollo Binder

| 关注点 | 设计决策 |
|--------|---------|
| 初始化 | 设置系统属性（`app.id`、`apollo.meta`、`env`、`apollo.cluster`），然后 `ConfigService.getConfig(namespace)` |
| 读取 | `Config.getProperty(dataId, null)` |
| 写入 | ⚠️ 未实现，需 Apollo Portal Open API（WARN 日志 + 返回 false） |
| 删除 | ⚠️ 同上 |
| 监听 | `Config.addChangeListener(listener, Set.of(dataId))`，按 dataId 过滤 |
| group 映射 | 传入的 `group`（非默认时）视为 Apollo namespace 动态加载 |
| 依赖 | `com.ctrip.framework.apollo:apollo-client:2.3.0` |

---

## 5. 自动配置与注册机制

### 5.1 API 模块（`ConfigCenterBindingAutoConfiguration`）

- 注册 `ConfigCenterBindingManager` Bean
- 激活 `ConfigCenterGroupProperties`（`loadup.configcenter.*`）

### 5.2 各 Binder 模块（以 Nacos 为例）

```java
@AutoConfiguration
@ConditionalOnClass(NacosFactory.class)
public class NacosConfigCenterAutoConfiguration {
    @Bean
    public BindingMetadata<?, ?, ?, ?> nacosConfigCenterMetadata() {
        return new BindingMetadata<>(
            "nacos",
            DefaultConfigCenterBinding.class,
            NacosConfigCenterBinder.class,
            ConfigCenterBindingCfg.class,
            NacosConfigCenterBinderCfg.class,
            ctx -> new DefaultConfigCenterBinding());
    }
}
```

`BindingManagerSupport` 在首次调用 `getBinding()` 时懒加载 `BindingMetadata`，按 `binderType` 匹配，注入配置并初始化对应 Binder 实例。

### 5.3 可选 Spring 刷新（`ConfigAutoRefreshConfiguration`）

- `@ConditionalOnClass(ContextRefresher.class)`，只有引入 `spring-cloud-context` 时激活
- 监听 `ConfigChangeEvent`，调用 `contextRefresher.refresh()` 触发 `@RefreshScope` 刷新
- 通过 `@EnableConfigAutoRefresh` 注解手动导入，不自动生效

---

## 6. 配置属性结构

```yaml
loadup:
  configcenter:
    default-binder: local             # 全局默认 Binder 类型

    binders:                          # Binder 级别配置（key = binderType）
      local:
        base-path: config
        refresh-interval: 30s
        namespace: ""
        default-group: DEFAULT_GROUP
      nacos:
        server-addr: 127.0.0.1:8848
        namespace: ""
        default-group: DEFAULT_GROUP
        username: nacos
        password: nacos
        timeout: 3000
      apollo:
        meta: http://apollo-configservice:8080
        app-id: my-app
        env: DEV
        cluster: default
        apollo-namespace: application
        default-group: DEFAULT_GROUP

    bindings:                         # Binding 级别配置（key = bizTag）
      my-feature-config:
        binder-type: nacos            # 覆盖全局 default-binder
        data-id: feature.flags
        group: FEATURE_GROUP
```

---

## 7. SPI 扩展点

实现 `ConfigCenterBinderPlugin` + `AbstractConfigCenterBinder` 并注册 `BindingMetadata` Bean：

```java
@AutoConfiguration
@ConditionalOnClass(ConsulClient.class)
public class ConsulConfigCenterAutoConfiguration {
    @Bean
    public BindingMetadata<?, ?, ?, ?> consulMetadata() {
        return new BindingMetadata<>(
            "consul",
            DefaultConfigCenterBinding.class,
            ConsulConfigCenterBinder.class,
            ConfigCenterBindingCfg.class,
            ConsulConfigCenterBinderCfg.class,
            ctx -> new DefaultConfigCenterBinding());
    }
}
```

配置 `loadup.configcenter.default-binder: consul` 即可生效。

---

## 8. 模块依赖关系

```
loadup-commons-api (Binder/Binding 框架接口)
        ↑
loadup-components-configcenter-api
        ↑
        ├── loadup-components-configcenter-binder-local
        ├── loadup-components-configcenter-binder-nacos
        ├── loadup-components-configcenter-binder-apollo
        └── loadup-components-configcenter-test
```

> **注意**：各 binder 子模块之间无横向依赖；应用按需引入一个或多个 binder。
