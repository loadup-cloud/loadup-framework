# loadup-components-configcenter

业务代码使用 `ConfigCenterTemplate` 查询、发布和订阅配置，或用 `@DynamicConfig` 将 Spring bean 字段和回调绑定到配置键。应用通过 binder 依赖和 `loadup.configcenter.binder-type` 选择 XXL-Conf、Nacos 或 Apollo。

## 快速开始

添加对应 binder。业务库可单独依赖 `loadup-components-configcenter-api`。

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-configcenter-binder-xxl-conf</artifactId>
</dependency>
```

```yaml
loadup:
  configcenter:
    binder-type: xxl-conf
    binder:
      xxl-conf:
        address: http://localhost:8080/xxl-conf
        access-token: ${XXL_CONF_ACCESS_TOKEN}
        app-name: payments
        env: test
```

```java
import io.github.loadup.components.configcenter.DynamicConfig;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {
    @DynamicConfig(value = "feature.payment.enabled", defaultValue = "false")
    private volatile boolean paymentEnabled;

    @DynamicConfig("feature.payment.mode")
    void paymentModeChanged(String mode) {
        // Apply the initial mode and subsequent changes. A missing value is null.
    }

    public boolean isPaymentEnabled() {
        return paymentEnabled;
    }
}
```

注解在单例 Spring bean 初始化后注册，应用启动时读取初值，后续变更异步生效。字段须为非静态、非 final 的 `volatile` 字段，使用 Spring 类型转换。回调签名为 `void method(String value)`。键和默认值支持 Spring 属性占位符。缺失或删除时使用默认值，省略默认值时传入 `null`。基本类型字段需要可转换的初值或默认值。Spring 销毁 bean 时解除注解订阅。

## Template 能力矩阵

| 能力 | XXL-Conf 2.2.1 | Nacos | Apollo |
|---|---|---|---|
| `getConfig(key[, default])`、`listKeys(prefix)` | OpenAPI，按环境和应用查询 | SDK 配置文档快照 | SDK 已发布配置 |
| `setConfig(key, value)`、`removeConfig(key)` | 返回 `false`，写入和删除通过 XXL-Conf 管理台 | SDK 按文档 CAS 发布 | 配置 Portal 凭证后编辑并发布 namespace |
| `addListener(key, consumer)`、`@DynamicConfig` | OpenAPI 长轮询与查值 | SDK 文档监听 | SDK namespace 监听 |

`setConfig` 或 `removeConfig` 返回 `true` 表示后端已接受操作。Nacos 和 Apollo 在 SDK 收到更新后刷新读取和监听。程序化监听只接收变更，配置删除时收到 `null`。`removeListener(key)` 仅移除当前 Template 登记的程序化监听。

## 迁移原 Local binder

将依赖换为 `loadup-components-configcenter-binder-xxl-conf`，设置 `binder-type: xxl-conf`，按应用和环境在 XXL-Conf 管理台录入配置键及初始值。原 Local binder 的进程内写入需要改由管理台完成，或使用支持程序化发布的 Nacos 或 Apollo。可用模块包括 [XXL-Conf](loadup-components-configcenter-binder-xxl-conf/README.md)、[Nacos](loadup-components-configcenter-binder-nacos/README.md) 和 [Apollo](loadup-components-configcenter-binder-apollo/README.md)。
