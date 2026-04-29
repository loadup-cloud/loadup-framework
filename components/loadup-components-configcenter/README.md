# loadup-components-configcenter

`loadup-components-configcenter` 是 LoadUp 平台的统一配置中心组件，采用 **Binder/Binding 架构**屏蔽底层配置源差异，支持本地文件（Local）、Nacos、Apollo 等多种实现，并预留 Spring Cloud Config / Consul / etcd 扩展点。

## 模块结构

```
loadup-components-configcenter/
├── loadup-components-configcenter-api/              # 核心接口、模型、自动配置
├── loadup-components-configcenter-binder-local/     # 本地文件 Binder
├── loadup-components-configcenter-binder-nacos/     # Nacos Binder
├── loadup-components-configcenter-binder-apollo/    # Apollo Binder
└── loadup-components-configcenter-test/             # 集成测试 + 单元测试
```

## 快速接入

### 1. 引入依赖（BOM 管理，无需写版本号）

```xml
<!-- 核心 API（必须） -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-configcenter-api</artifactId>
</dependency>

<!-- 选择一个 Binder 实现 -->
<!-- Local（开发 / 无外部依赖） -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-configcenter-binder-local</artifactId>
</dependency>

<!-- 或 Nacos -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-configcenter-binder-nacos</artifactId>
</dependency>

<!-- 或 Apollo -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-configcenter-binder-apollo</artifactId>
</dependency>
```

### 2. 配置 application.yml

```yaml
loadup:
  configcenter:
    default-binder: local   # local | nacos | apollo

    binders:
      local:
        base-path: config
        refresh-interval: 30s

      nacos:
        server-addr: 127.0.0.1:8848
        namespace: ""
        default-group: DEFAULT_GROUP
        username: nacos
        password: nacos
        timeout: 3000

      apollo:
        meta: http://apollo-configservice:8080
        app-id: my-application
        env: DEV
        cluster: default
        apollo-namespace: application
```

### 3. 注入并使用

```java
@Service
@RequiredArgsConstructor
public class MyService {

    private final ConfigCenterBindingManager configCenter;

    public void demo() {
        ConfigCenterBinding binding = configCenter.getBinding();

        // 读取配置
        Optional<String> value = binding.getConfig("feature.enabled");

        // 写入配置
        binding.publishConfig("feature.enabled", "true");

        // 监听变更
        binding.addListener("feature.enabled", event ->
            log.info("{} changed: {} -> {}", event.getDataId(),
                event.getOldContent(), event.getNewContent()));
    }
}
```

## 各 Binder 能力对比

| 能力 | Local | Nacos | Apollo |
|------|-------|-------|--------|
| 读配置 | ✅ 文件优先，Environment 兜底 | ✅ | ✅ |
| 写配置 | ✅ | ✅ | ⚠️ 需 Apollo Portal Open API |
| 删配置 | ✅ | ✅ | ⚠️ 需 Apollo Portal Open API |
| 监听变更 | ✅ mtime 轮询 | ✅ Nacos 长轮询推送 | ✅ Apollo 实时推送 |

## Spring 自动刷新（可选）

引入 `spring-cloud-context` 后，在启动类加注解即可让配置变更自动刷新 `@RefreshScope` Bean：

```java
@SpringBootApplication
@EnableConfigAutoRefresh
public class MyApplication { ... }
```

## SPI 扩展

实现 `ConfigCenterBinderPlugin` 并注册为 Spring Bean，可接入 Consul、etcd 等自定义后端。

## 测试

```bash
# 单元测试
mvn test -pl components/loadup-components-configcenter/loadup-components-configcenter-test \
    -Dtest=DefaultConfigCenterBindingTest

# 集成测试（Local binder，无外部依赖）
mvn test -pl components/loadup-components-configcenter/loadup-components-configcenter-test \
    -Dtest=ConfigCenterLocalBindingIT
```

## 许可证

GPL-3.0 — 详见项目根目录 [LICENSE](../../LICENSE)。
