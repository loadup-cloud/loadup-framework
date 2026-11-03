# RPC Proto helpers 架构

模块直接依赖 protobuf-java-util，与 Dubbo 运行 binder 分开。IDL mapper 在契约适配层使用标量 helper，
业务 app/domain 不依赖生成消息。不能用该模块替代 HTTP JSON 的类型、时间或 presence 约束。
选用协议的完整边界见 [RPC 模块](../README.md)，本模块不承诺运行平台或服务发现能力。
