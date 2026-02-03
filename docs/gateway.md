# loadup-gateway 概览

`loadup-gateway` 提供 API 网关能力，负责路由、鉴权、限流及边界策略。

主要职责：
- 路由转发与负载均衡
- 认证与鉴权过滤器（JWT）
- 限流/熔断/健康检查

集成建议：
- Gateway 可依赖 `commons` 与 `components`，但不得被 `modules` 或 `application` 反向依赖。
- 鉴权逻辑建议由 `components` 中的认证组件实现并由 gateway 调用。

查看 `loadup-gateway/README.md` 以获取实现细节。
