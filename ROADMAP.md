# LoadUp Framework Roadmap

## 已批准：Kubernetes 原生部署与 HTTP 默认调用能力

决策日期：2026-11-02。状态：HTTP Service Clients 及 MVC 入站大小限制源码已接入；运行验收及其余配套实施待完成。完整迁移阶段和 Pay 应用验收以
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

- [x] 首批在现有 HTTP 单 jar 接入标准 HTTP Service Clients，共享命名 transport；复用 Web MVC、Resource Server、业务上下文和 Boot Observation。未增加容错重试包装。
- [ ] 完成声明式客户端分组装配与连接配置，保留租户、令牌、幂等、超时、错误码及追踪语义，写请求默认不自动重试。
- [ ] 配合 Pay 的全量 HTTP 迁移，去掉应用默认依赖中的 Alibaba/Nacos 和 Dubbo；可选组件保持独立接入与版本管理。
- [x] 提供版本化 ConfigMap/Secret、滚动发布及配置回退的接入说明，明确文件/环境变量更新不等于 Bean 自动刷新。
- [x] 提供 Gateway API / Envoy Gateway 的部署参考，明确平台入口与业务 BFF 的边界，不把网关控制器打包进 SDK。
- [ ] 验证 MVC 虚拟线程对客户端、身份和上下文传播的影响；以压测结果决定默认配置，不全局替换所有组件的执行器。
- [ ] 在相应实施阶段同步 AGENTS、ARCHITECTURE、模块文档及文档站，清理与目标冲突的旧默认接入描述。

验收以消费方能够在无独立注册中心的 K8s 环境中完成 HTTP 调用为准，同时保持 SDK 的按需依赖边界。
文档记录不代表这些工作已实现或测试通过；构建、集成测试与部署仍遵循既有项目约定。

2026-11-03 用户反馈本地编译无问题，测试验证暂缓；助手未重跑构建或测试。
这条反馈不代表测试、部署或业务验收通过，后续源码变更按实际版本重新核对。

首批：HttpTemplate.serviceClient 按提供方注册声明式接口，无逐方法 URL 配置；TLS、超时、池容量、响应大小及无重试策略复用已有客户端。
回归测试源码已补充，未执行构建或测试；Pay 七应用 44/44 操作已迁移源码，协议生成/运行依赖已清理；版本化配置/排空已补发布源码和说明；Envoy Gateway 已补应用侧原生资源与参考说明；虚拟线程与全部运行验收仍待实施。

MVC 入站大小限制在 WebMVC 组件中按显式配置启用，Pay 七应用均已配置内部路径字节上限；HTTP 413 保留传输状态。
回归源码覆盖已知/未知长度、虚假长度、UTF-8 字节、完整尾随输入及 MVC 映射路径，未执行。

Pay 的显式版本化快照、可变服务令牌目录投射、预检与镜像/配置引用归档已补，见 [配置与发布](../loadup-pay/deploy/CONFIGURATION.md)。
这属于应用部署参考，不为 SDK 增加 K8s API 客户端或默认配置中心依赖；实际滚动、轮换和回退仍待验收。

默认平台入口参考已补，见 [Pay Gateway API / Envoy Gateway](../loadup-pay/deploy/gateway/README.md)。
固定兼容版本、TLS/窄范围引用授权、请求限额/超时/本地限流和敏感信息受限日志，保持 SDK 无控制器/注册中心依赖。
七应用 HTTP NetworkPolicy 源码已补，安装、CNI、证书和业务流量仍待环境验收。

MVC 虚拟线程阶段已补原生候选 profile 与回归源码：真实嵌入式请求线程/上下文隔离、原生任务执行器的 ScopedValue/Micrometer 传播、Local fixed-delay 串行唤醒。
HTTP 组件已补 Apache 原生池状态和 lease 获取计时采集，并按 scope/client 管理刷新和关闭时的指标归属；回归源码覆盖饱和超时、刷新连续性及独立 scope。
未执行构建、测试或压测，默认平台线程。完整容量与端到端证据见 [Pay 最终验收](../loadup-pay/deploy/ACCEPTANCE.md)；池指标真实抓取与容量验证仍待执行。
