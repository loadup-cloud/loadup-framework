# LoadUp TestContainers

`loadup-components-testcontainers` 为测试提供共享容器及连接属性注入能力。支持以下类型，可通过注解或配置方式启动：

| 容器类型 | 注解选择 | 配置启动 |
|---|---|---|
| MySQL | `ContainerType.MYSQL` | `loadup.testcontainers.mysql` |
| PostgreSQL | `ContainerType.POSTGRESQL` | `loadup.testcontainers.postgresql` |
| MongoDB | `ContainerType.MONGODB` | `loadup.testcontainers.mongodb` |
| Redis | `ContainerType.REDIS` | `loadup.testcontainers.redis` |
| Kafka | `ContainerType.KAFKA` | `loadup.testcontainers.kafka` |
| Elasticsearch | `ContainerType.ELASTICSEARCH` | `loadup.testcontainers.elasticsearch` |
| LocalStack | `ContainerType.LOCALSTACK` | `loadup.testcontainers.localstack` |

## 接入

测试依赖使用 test scope：

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
```

注解方式在 Spring 测试上下文中选择容器：

```java
@SpringBootTest
@EnableTestContainers({ContainerType.MYSQL, ContainerType.REDIS})
class RepositoryIT {
}
```

`@EnableTestContainers` 的 `reuse` 默认值为 `true`。Testcontainers 复用还要求在
`~/.testcontainers.properties` 中设置 `testcontainers.reuse.enable=true`。

配置方式由测试上下文中的 `TestContainersPropertyInitializer` 读取 `loadup.testcontainers`：

```yaml
loadup:
  testcontainers:
    enabled: true
    mysql:
      enabled: true
      version: mysql:8.0
      database: testdb
      username: test
      password: test
    redis:
      enabled: true
      version: redis:7-alpine
```

配置模式需要将 initializer 接入 Spring 测试上下文；例如：

```java
@SpringBootTest
@ContextConfiguration(initializers = TestContainersPropertyInitializer.class)
class RepositoryIT {
}
```

Initializer 只启动已配置且全局、单类型开关均启用的容器，并将连接属性写入 Spring 测试环境。单类型禁用时由应用配置实际服务连接；
容器不可用或启动失败不会自动切换到真实服务。更多类型配置见 `TestContainersProperties`。

## 边界

- 容器实例在 JVM 内共享，成功启动后的实例不会因后续调用而改变配置；不同配置应放在独立测试进程中验证。
- 此模块提供测试基础设施，不负责生产环境服务部署或客户端业务封装。
- 测试环境需能访问 Docker 和所选镜像。

见[架构与生命周期约束](ARCHITECTURE.md)。
