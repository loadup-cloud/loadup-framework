# 技术组件

LoadUp 组件通过功能分类目录组织源码。消费工程引入 `loadup-dependencies` BOM 后，按需选择组件坐标；多后端组件再选择 binder。分类目录不是 Maven 模块，组件坐标、Java 包名、配置前缀和 API 保持不变。

## 功能分类

| 目录 | 职责 | 组件 |
|---|---|---|
| `security/` | 安全与身份 | [authorization](security/loadup-components-authorization/README.md)、[authserver](security/loadup-components-authserver/README.md)、[resource-server](security/loadup-components-resource-server/README.md)、[captcha](security/loadup-components-captcha/README.md)、[kms](security/loadup-components-kms/README.md)、[signature](security/loadup-components-signature/README.md) |
| `web/` | Web 接入 | [webmvc](web/loadup-components-webmvc/README.md)、[springdoc](web/loadup-components-springdoc/README.md) |
| `data/` | 数据与存储 | [cache](data/loadup-components-cache/README.md)、[database](data/loadup-components-database/README.md)、[dfs](data/loadup-components-dfs/README.md) |
| `integration/` | 外部集成 | [http](integration/loadup-components-http/README.md)、[gotone](integration/loadup-components-gotone/README.md) |
| `reliability/` | 可靠性 | [lock](reliability/loadup-components-lock/README.md)、[outbox](reliability/loadup-components-outbox/README.md)、[globalunique](reliability/loadup-components-globalunique/README.md)、[resilience4j](reliability/loadup-components-resilience4j/README.md) |
| `execution/` | 任务与执行 | [scheduler](execution/loadup-components-scheduler/README.md)、[retrytask](execution/loadup-components-retrytask/README.md)、[pipeline](execution/loadup-components-pipeline/README.md) |
| `platform/` | 平台基础 | [configcenter](platform/loadup-components-configcenter/README.md)、[extension](platform/loadup-components-extension/README.md)、[observability](platform/loadup-components-observability/README.md)、[testcontainers](platform/loadup-components-testcontainers/README.md) |

## 目录与接入

```text
loadup-components/
├── security/       authorization, authserver, resource-server, captcha, kms, signature
├── web/            webmvc, springdoc
├── data/           cache, database, dfs
├── integration/    http, gotone
├── reliability/    lock, outbox, globalunique, resilience4j
├── execution/      scheduler, retrytask, pipeline
└── platform/       configcenter, extension, observability, testcontainers
```

每个组件目录仍使用 `loadup-components-{name}`，内部 API / binder / engine / store / test 结构沿用该组件的设计。所有 Maven 模块直接继承根 `loadup-parent`，分类目录没有额外 POM 或发布坐标。

组件选择与参数以对应 README 为准；分类仅帮助定位，不表示必须整组引入。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-webmvc</artifactId>
</dependency>
```

从仓库根目录做定向开发时，推荐用 Maven artifact 选择器，如 `-pl :loadup-components-webmvc`，避免命令绑定目录深度。需要构建或测试时遵循根 AGENTS 的构建纪律，不默认运行全 reactor。

能力边界与目录决策见 [ARCHITECTURE.md](ARCHITECTURE.md)。
