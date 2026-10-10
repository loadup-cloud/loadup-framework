# LoadUp Dictionary Client Architecture

## 职责

不可变 DTO、Command/Query 和对外协议。

独立维护程序化及 HTTP 请求/响应协议，不导入 app、infrastructure 或 web。领域模型与 DTO 分离，映射由 app 内 MapStruct Spring converter 完成。

整体职责、事务和失败语义见 [业务架构](../ARCHITECTURE.md)，接入见 [README.md](README.md)。
