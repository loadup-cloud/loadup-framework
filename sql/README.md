# Framework 完整初始化 SQL

组件的 `src/main/resources/db/schema/*.sql` 和业务模块的 `src/main/resources/db/migration/framework/<module>/V*.sql`
是维护源；修改源文件后运行：

```bash
python3 tools/export-schema.py
python3 tools/export-schema.py --check
```

脚本可从任意工作目录调用，固定生成 [framework-schema.sql](framework-schema.sql)。先按路径排列组件模板，
再按迁移版本号排列业务模块 SQL，同版本时按路径排序，原样拼接文件。在每段标注路径和 SHA-256；
不包含生成时间戳，因此相同输入生成相同文件。CI 使用 `--check` 检查新增、修改、删除的源文件是否已同步。

保留两种交付形式：

| 形式 | 用途 |
|---|---|
| 组件资源中的 `db/schema/*.sql` | 保留原始模板，消费应用可按依赖生成应用级 Flyway 迁移，由启动迁移自动执行 |
| 业务模块资源中的 `db/migration/framework/<module>/V*.sql` | 保留原始 Flyway 自动迁移文件及版本、内容，应用显式选择该模块的 location |
| `sql/framework-schema.sql` | 全部组件及业务模块的完整初始化整合版，可通过 psql 或数据库工具手动导入 |

当前整合六份组件模板（Sequence Allocation、DFS Database、Gotone JDBC Store、DelayedTask、GlobalUnique、Outbox）
及七份业务迁移（Audit、Dictionary、File、UPMS、Notification、Transfer，Notification 有两份），共十三份 SQL。
测试 SQL、历史独立 schema 示例不在合并范围，避免重复建表或混入测试数据。
组件模板本身不会被 Flyway 自动扫描；Pay 的 `scripts/gen-migrations.py` 只创建初始快照，已有快照由锁文件冻结。
模板更新不会改写应用历史，后续结构变化必须追加增量迁移。应用只选择 `db/migration/pay/<app>` 和获准的
`db/migration/framework/<module>`，避免共享扫描根将无关依赖 JAR 的迁移纳入执行。

## 手动导入

在 Framework 根目录执行，连接参数通过 PostgreSQL 标准环境变量或 `.pgpass` 提供：

```bash
PGHOST=localhost PGPORT=5432 PGUSER=admin \
  psql -X --dbname=loadup_payment --set=ON_ERROR_STOP=1 --single-transaction \
  --file=sql/framework-schema.sql
```

也可以将文件复制到 Pay 后导入，具体命令见 [Pay 脚本说明](../../loadup-pay/scripts/README.md)。
SQL 使用连接的 `search_path`，默认通常为 `public`；自定义 schema 时须预先创建，并通过连接配置选择与应用相同的 schema。
脚本不创建数据库或 schema，不写 Flyway 历史。整合版包含 Framework 全部组件表及业务模块表；
Pay 自己的收单、付款等业务表仍由 Pay 应用迁移创建。

整合版只用于空 schema 的一次性初始化。部分业务迁移的表和索引没有 `IF NOT EXISTS`，不能重复导入，
也不能导入后直接让 Flyway 从头重跑同一批迁移。原有自动迁移文件和启动行为保持不变；
手动管理迁移的环境可配置 `loadup.flyway.migrate-at-start=false`，并由部署方执行应用所需的其他迁移。
如需切回自动迁移，应先按实际导入的迁移范围建立匹配的 Flyway 基线；脚本不会伪造历史记录或自动 baseline。
