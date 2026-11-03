# RPC Proto helpers

坐标 io.github.loadup-cloud:loadup-components-rpc-proto，版本由 LoadUp BOM 管理。
为显式使用 Protobuf IDL 的集成方提供 RpcScalarMapper 标量转换；不创建 RPC 客户端、注册中心或 Web 服务。
默认 HTTP 调用和已迁移的 Pay 不需要本模块；仅在 IDL mapper 需要时直接依赖。
