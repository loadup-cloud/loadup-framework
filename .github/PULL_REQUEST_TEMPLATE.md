---
name: Pull Request
about: 提交代码变更
---

## 变更类型

- [ ] 新功能（新增业务模块 / Feature）
- [ ] Bug 修复
- [ ] 重构（不影响外部行为的代码结构调整）
- [ ] 性能优化
- [ ] 文档更新
- [ ] 依赖升级
- [ ] 其他：___

---

## 变更描述

<!-- 简要描述本次变更的内容和原因 -->

---

## 关联 Issue

Closes #

---

## 代码合规检查

### 架构规范 🚫

- [ ] 无 `@RestController` / `@Controller`（API 通过 Gateway bean:// 路由）
- [ ] 无 `@Autowired` 字段注入（构造器注入 + `@RequiredArgsConstructor`）
- [ ] 无 Java 文件头 License 注释块（CI 插件统一插入）
- [ ] domain 层无 `@Table`、`@Service` 等框架注解
- [ ] 无 modules 间横向依赖
- [ ] 子模块 `<parent>` 指向根 `loadup-parent`，非模块自身 pom

### 持久层规范

- [ ] 无字符串拼接 SQL（全部使用 `QueryWrapper`）
- [ ] 无 `SELECT *`（显式指定查询字段）
- [ ] DO 类继承 `BaseDO`，未重复定义 `id`/`createdAt`/`updatedAt`
- [ ] 新 Mapper 只继承 `BaseMapper<XxxDO>`，无额外 SQL 方法
- [ ] DO ↔ domain model 转换使用 MapStruct，非手写 setter 链

### 数据库变更（有 schema 变更时必填）

- [ ] 新表包含 5 个标准字段（`id`/`tenant_id`/`created_at`/`updated_at`/`deleted`）
- [ ] 主键为 `VARCHAR(64)`，非 `BIGINT AUTO_INCREMENT`
- [ ] 无 `BOOLEAN`/`BOOL` 类型字段（统一使用 `TINYINT`）
- [ ] 已同步更新 `modules/{mod}/schema.sql`
- [ ] 已同步更新 `{mod}-test/src/test/resources/schema.sql`
- [ ] 新 Flyway 脚本版本号不重复，格式 `V{n}__{描述}.sql`

### pom.xml 规范

- [ ] 新增依赖已在 `loadup-dependencies/pom.xml` 声明版本
- [ ] 引用同项目模块时未写 `<version>`

### 测试规范

- [ ] 集成测试使用 `@EnableTestContainers(ContainerType.MYSQL)`
- [ ] 无 `@MockBean` 替代真实数据库
- [ ] `@BeforeEach` 清理了测试专用数据
- [ ] 测试方法命名格式：`方法名_期望结果_触发条件`

### 安全检查

- [ ] 密码/Token 字段有 `@JsonIgnore`
- [ ] 无敏感信息写入日志（密码、Token、手机号/身份证明文）
- [ ] 写操作接口有 `@RequirePermission` 或在 Gateway 路由中配置了 `securityCode: default`

---

## 文档同步检查

> 根据本次变更类型，确认对应文档是否需要更新：

| 变更类型 | 需同步的文档 |
|---------|------------|
| 新增业务模块 | `mkdocs/docs/module-dependency-map.md` → 4.3 节；`CLAUDE.md` |
| 新增组件使用 | `mkdocs/docs/component-integration-quick-ref.md` |
| 新增 Gateway 路由 | `loadup-application/src/main/resources/application.yml` |
| 数据库 schema 变更 | `schema.sql`（模块根）+ 测试 `schema.sql` |
| 新增故障排查场景 | `mkdocs/docs/troubleshooting-guide.md` |
| 版本/依赖升级 | `mkdocs/docs/commands-reference.md`（如命令有变） |

- [ ] 已确认上表中涉及的文档均已同步更新，**或** 此次变更不涉及以上文档

---

## 测试验证

```bash
# 本地验证命令（提交前请执行）

# 1. 格式化
mvn spotless:apply

# 2. 编译 + 单元测试
mvn clean test -DskipITs=true

# 3. 集成测试（有 DB 变更必须执行）
mvn verify -pl modules/loadup-modules-{mod}/loadup-modules-{mod}-test

# 4. 全量验证（推荐）
mvn clean verify
```

- [ ] `mvn spotless:check` 通过
- [ ] 新增/变更的 Service 有对应集成测试通过
- [ ] `mvn clean verify` 本地通过（或说明为何未执行）

---

## 其他说明

<!-- 需要 Reviewer 重点关注的地方、已知局限、后续计划等 -->
