# Kubernetes RPC 配置与发布规范

适用于消费工程通过 LoadUp gRPC binder 接入的独立服务。binder 仅提供原生 starter，不生成业务协议或部署资源。配置代码见 [README](README.md#客户端)。

## 命名与配置归属

设服务的标准名为 `<service>`，使用小写字母、数字、连字符，不带环境后缀；为追加 `-headless` 后仍满足 Service 名称长度限制，标准名最长 54 字符。

| 对象 | 约定 | 示例 |
| --- | --- | --- |
| Spring application、Deployment、容器名 | `<service>` | `user-service` |
| Pod label 与 Service selector | `app.kubernetes.io/name: <service>` | `user-service` |
| HTTP ClusterIP Service（若有） | `<service>` | `user-service` |
| gRPC Headless Service | `<service>-headless` | `user-service-headless` |
| gRPC 命名端口 | `grpc`，固定 `50051/TCP` | Service 与 Pod 都为 50051 |
| 客户端 channel key | 提供方标准名 | `user-service` |
| 客户端 target | `dns:///<service>-headless:50051` | `dns:///user-service-headless:50051` |
| 环境 | Namespace 区分 | `pay-dev`、`pay-test`、`pay-prod` |

Java 使用 `@Value` 方法参数/构造器注入或 `@ConfigurationProperties` 读取 channel 名、deadline 等配置。主机名、端口、Namespace、TLS 及负载均衡策略不写进 Java 常量，不在代码中拼接地址。属性键与协议定义本身不属于部署地址硬编码。

地址直接放进调用方 `application.yml` 的 `spring.grpc.client.channel.<service>.target`，不增加按环境替换的 RPC 地址变量。服务端 `spring.grpc.server.port` 固定为 50051，容器监听所有 Pod 网络接口；HTTP 可继续使用 8080。

开发到生产提升同一个镜像 digest，各环境部署相同逻辑服务名。Pod 的 `ClusterFirst` DNS 搜索路径使短名称解析到调用方所在 Namespace；Headless Service 返回就绪端点的 Pod IP。调用双方必须在同一环境 Namespace，且 NetworkPolicy 允许 DNS 与 50051 通信。详见 [Kubernetes DNS](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/) 与 [Headless Service](https://kubernetes.io/docs/concepts/services-networking/service/#headless-services)。

“零配置维护”指 RPC **目标地址无需随环境修改**。Namespace、镜像仓库、资源配额、数据库、Secret、证书及服务身份仍由环境管理。集群外 IDE 不具备集群 DNS；跨 Namespace 调用需要显式全限定地址，不属于此约定。

## 提供方清单

以下为消费应用示例，不直接应用于框架或尚无 RPC 的应用。提供方须已引入 binder、注册实际 `BindableService` 并配置 50051；示例假定同时提供 Spring Boot Actuator HTTP 8080 探针。

`user-service.yml` 不填写 Namespace，由部署命令统一指定；同目录其他环境资源同样避免写死 Namespace。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service-headless
spec:
  clusterIP: None
  selector:
    app.kubernetes.io/name: user-service
  ports:
    - name: grpc
      port: 50051
      targetPort: grpc
      protocol: TCP
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 2
  revisionHistoryLimit: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: user-service
  template:
    metadata:
      labels:
        app.kubernetes.io/name: user-service
    spec:
      dnsPolicy: ClusterFirst
      terminationGracePeriodSeconds: 45
      containers:
        - name: user-service
          image: registry.example.com/user-service@sha256:REPLACE_WITH_RELEASE_DIGEST
          ports:
            - name: grpc
              containerPort: 50051
            - name: http
              containerPort: 8080
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            periodSeconds: 5
            failureThreshold: 60
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            periodSeconds: 5
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            periodSeconds: 10
```

启用 `management.endpoint.health.probes.enabled: true`，并允许探针访问。readiness 应覆盖实际 RPC 接收就绪状态；HTTP 健康不能代替业务 RPC 验收。不设置 `publishNotReadyAddresses: true`，避免 DNS 发布未就绪 Pod。Headless 直连 Pod，因此 Service 的 port 与实际监听端口必须一致，不能依赖 ClusterIP 端口转发。

提供方配置 `spring.grpc.server.shutdown.grace-period: 30s`，HTTP 应用同时配置 `server.shutdown: graceful`。终止窗口应覆盖流量摘除和在途请求；DNS 缓存、长连接与重解析有延迟，下线并非即时切流。客户端每次请求设置 deadline，重试遵循业务幂等语义，不能通过无条件重试掩盖发布错误。

示例清单没有 TLS/身份资源。生产在发布前通过原生 SSL Bundle/Secret 挂载配置 TLS、可信根和服务认证，证书 SAN 必须匹配客户端实际校验的服务名称；各环境保持同一套 RPC 地址，不关闭证书校验。负载均衡与安全配置参考 [Spring Boot gRPC](https://docs.spring.io/spring-boot/reference/io/grpc.html)、[gRPC Service Config](https://grpc.io/docs/guides/service-config/)。

## 发布与回滚

1. 在开发阶段完成 proto、生成代码、真实服务实现、stub 配置及对应 Headless 清单。跨版本滚动期间保证协议互通，先发布提供方新增能力，再发布消费者调用。
2. CI 按消费工程流程构建 JAR/镜像并记录不可变 digest。把清单中的镜像占位符替换为该 digest，开发验收后直接提升该镜像到测试、生产；不因 RPC 地址重新打包。
3. 在目标 Namespace 准备 Secret、证书、配置和网络策略。确认客户端及提供方属于同一 Namespace。先应用提供方清单，等待就绪，再发布消费者。
4. 检查 EndpointSlice、从消费者 Pod 检查 DNS，再执行带身份、TLS 与 deadline 的实际 RPC。验收多副本调用和滚动期间请求结果，不能只检查 Service 是否存在。
5. 发布失败时回滚失败的 Deployment 并重复验收。涉及协议/数据库时按消费工程的兼容方案处理；Deployment 回滚不会回滚数据库、Secret、ConfigMap 或 Service。

以下命令仅示意，需使用已渲染镜像的 `user-service.yml`；不由本文自动执行。显式填写 context，避免操作默认集群。

```bash
context=your-cluster
namespace=pay-dev
kubectl --context "$context" get namespace "$namespace"
kubectl --context "$context" -n "$namespace" apply -f user-service.yml
kubectl --context "$context" -n "$namespace" rollout status deployment/user-service --timeout=180s
kubectl --context "$context" -n "$namespace" get svc user-service-headless
kubectl --context "$context" -n "$namespace" get endpointslices \
  -l kubernetes.io/service-name=user-service-headless -o yaml
# 在已存在且有 DNS 工具的消费者 Pod 中执行；distroless 镜像使用环境批准的调试容器。
kubectl --context "$context" -n "$namespace" exec deployment/order-service -- \
  nslookup user-service-headless
# 随后通过消费者真实调用链执行实际 RPC 验收。

# 失败时先查看历史，选择已验证的 revision。
kubectl --context "$context" -n "$namespace" rollout history deployment/user-service
kubectl --context "$context" -n "$namespace" rollout undo deployment/user-service --to-revision=1
kubectl --context "$context" -n "$namespace" rollout status deployment/user-service --timeout=180s
```

EndpointSlice 中应有 `conditions.ready: true` 的地址，端口为 50051。DNS 工具返回 Pod 地址后仍需确认 gRPC 运行时实际解析、连通和证书校验；客户端启用 `round_robin` 才会在解析结果间分配请求。
