# DFS API 架构

[`DfsTemplate`](src/main/java/io/github/loadup/components/dfs/DfsTemplate.java) 是业务调用与 binder 实现共用的接口。基础契约包含上传、下载、删除、存在性查询和元数据读取。预签名和分片上传由支持该能力的 binder 实现，默认抛出 `UnsupportedOperationException`。

## 装配规则

新增 binder 在自己的 `AutoConfiguration.imports` 注册配置，以 `loadup.dfs.binder-type` 匹配自己的标识，并在应用尚未提供 `DfsTemplate` 时创建一个 Bean。binder auto-configuration 声明 `before = DfsAutoConfiguration.class`。

[`DfsAutoConfiguration`](src/main/java/io/github/loadup/components/dfs/autoconfigure/DfsAutoConfiguration.java) 提供公共排序点。File 等可选消费者在此之后判断 `DfsTemplate` 是否存在。业务模块和 API 均可以通过 `DfsTemplate` 接入新增 binder。

集成方式见 [README.md](README.md)。
