# LoadUp Framework Roadmap

## 已批准：Kubernetes 原生部署与 HTTP 默认调用能力

决策日期：2026-11-02。状态：目标已确认，配套实施待完成。完整迁移阶段和 Pay 应用验收以
[LoadUp Pay Roadmap](../loadup-pay/ROADMAP.md) 为主。

Framework 是可选组件组成的 SDK；本目标不要求所有消费方部署 Kubernetes 或网关。
Spring Boot 保持 4.1.x，Java 保持 25。此前试接入的 Cloud Alibaba 模块、BOM 导入及应用默认依赖已撤回；
已有可选 ConfigCenter Nacos binder 和 Dubbo 能力保留，后续配套迁移仍待实施。

| 方向 | 后续工作 |
| --- | --- |
| HTTP 调用 | 默认推荐 Spring HTTP Service Clients + RestClient，复用现有 HTTP 组件、连接池、鉴权和观测能力，按目标服务配置基础地址 |
| 发现 | K8s 部署默认使用 Service DNS；不强制依赖注册中心或 Spring Cloud Kubernetes API 客户端；支持集成方覆盖本地/普通服务器地址 |
| 配置 | 普通配置由 ConfigMap/Secret 与滚动发布管理；配置热刷新不是默认能力，Nacos/Alibaba 仅按动态推送或灰度配置等需求选用 |
| 网关 | 部署参考默认使用 Gateway API + Envoy Gateway；Spring Cloud Gateway 仅为明确的 BFF、聚合或复杂 Java 过滤需求提供选项 |
| 执行模型 | MVC + 虚拟线程在压测后启用；保留独立的连接池、并发隔离和下游容量约束 |
| RPC | Dubbo Triple binder 保留为可选能力，不作为普通 HTTP 应用的传递依赖；Protobuf/gRPC 按跨语言契约或流式通信需求评估 |

- [ ] 盘点并复用 HTTP、Web MVC、Resource Server、上下文、Resilience4j、可观测组件，确定 HTTP Service Clients 的最小接入位置。
- [ ] 完成声明式客户端分组装配与连接配置，保留租户、令牌、幂等、超时、错误码及追踪语义，写请求默认不自动重试。
- [ ] 配合 Pay 的全量 HTTP 迁移，去掉应用默认依赖中的 Alibaba/Nacos 和 Dubbo；可选组件保持独立接入与版本管理。
- [ ] 提供版本化 ConfigMap/Secret、滚动发布及配置回退的接入说明，明确文件/环境变量更新不等于 Bean 自动刷新。
- [ ] 提供 Gateway API / Envoy Gateway 的部署参考，明确平台入口与业务 BFF 的边界，不把网关控制器打包进 SDK。
- [ ] 验证 MVC 虚拟线程对客户端、身份和上下文传播的影响；以压测结果决定默认配置，不全局替换所有组件的执行器。
- [ ] 在相应实施阶段同步 AGENTS、ARCHITECTURE、模块文档及文档站，清理与目标冲突的旧默认接入描述。

验收以消费方能够在无独立注册中心的 K8s 环境中完成 HTTP 调用为准，同时保持 SDK 的按需依赖边界。
文档记录不代表这些工作已实现或测试通过；构建、集成测试与部署仍遵循既有项目约定。
