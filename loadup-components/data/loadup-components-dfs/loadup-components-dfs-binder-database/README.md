# DFS Binder Database

Database binder 适用于对象存储接入前的过渡期小文件场景。Flyway 初始化 PostgreSQL 的 `dfs_file` 表，Spring JDBC 将文件内容写入 `BYTEA`，将 JSON 元数据写为文本。预签名和分片相关能力见 [DFS 能力矩阵](../README.md#能力矩阵)。

## 接入步骤

在引入 `loadup-framework-bom` BOM 的应用中添加：

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-dfs-binder-database</artifactId></dependency>
```

将 `loadup.dfs.binder-type` 设为 `database`，业务层注入 `DfsTemplate`。
此 jar 负责接入具体技术实现；业务模块继续面向 API/facade 编程。

## 对外契约

- [`DatabaseDfsTemplate`](src/main/java/io/github/loadup/components/dfs/database/DatabaseDfsTemplate.java)实现 `DfsTemplate`。
- [`DfsFileInsert`](src/main/java/io/github/loadup/components/dfs/database/dataobject/DfsFileInsert.java)映射 `dfs_file` 表。

## 自动装配

- [ `DatabaseDfsAutoConfiguration`](src/main/java/io/github/loadup/components/dfs/database/autoconfigure/DatabaseDfsAutoConfiguration.java)
    - 启用条件：`@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "database")`。

设计边界与装配路径见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 最小配置

```yaml
loadup:
  dfs:
    binder-type: database
```

同一应用启用与该值对应的 binder。其他后端见 [DFS 接入](../README.md)。

## 默认时区

日期默认使用 JVM 系统本地时区；无偏移日期按本地时区解释，显式偏移和 epoch 时间保持绝对时间语义。

Spring 装配使用 `loadup-components-runtime` 提供的应用 JSON mapper / Clock；消费者通过参数注入，不创建默认兜底实例。独立测试需显式提供所需 Bean。
