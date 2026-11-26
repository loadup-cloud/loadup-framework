# 数据与存储组件接入

消费应用导入 [Framework BOM](../../loadup-framework-bom/README.md)，再按需求选择下表中的工件。此目录的聚合 POM 用于组织 Maven 子模块。

| 组件 | 适用场景 | 接入要点 |
| --- | --- | --- |
| [KV 与缓存](loadup-components-kv/README.md) | Spring Cache 的本地、分布式或多级缓存 | 按所需的过期、并发和跨实例语义选择 Caffeine、Redis 或 JetCache。 |
| [数据库](loadup-components-database/README.md) | PostgreSQL 持久化、事务、业务 ID | 在宿主指定固定 schema，并将连接与迁移指向该 schema。 |
| [数据库处理器](loadup-components-database-processor/README.md) | 生成 MyBatis-Flex Mapper 和表定义 | 配置在 Java 编译器的注解处理器路径。 |
| [文件存储](loadup-components-dfs/README.md) | 文件内容、元数据与可选分片 | 按需选择 Local、S3 或 Database binder。 |

[分类架构](ARCHITECTURE.md) · [全部组件](../README.md)
