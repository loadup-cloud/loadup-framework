---
mode: agent
description: 为 LoadUp 项目生成标准 COLA 4.0 业务模块脚手架代码
---

# 新模块代码生成器

## 使用方式

在"现在请填写变量并生成"一节填写以下变量，然后运行：

- `{mod}` — 模块英文名（小写，如 `order`、`product`）
- `{Entity}` — 实体 Pascal-case（如 `Order`、`ProductItem`）
- `{table}` — 数据库表名 snake_case（如 `order`、`product_item`）
- 功能描述 — 一句话描述模块职责

---

## 任务描述

为 LoadUp 项目创建一个新的业务模块 `loadup-modules-{mod}`，主实体为 `{Entity}`，对应数据库表 `{table}`。

请生成以下完整内容：

### 1. Maven 模块 pom.xml（共 5 个子模块）

为以下子模块生成 pom.xml，所有 `<parent>` 指向根 `loadup-parent`（`relativePath` 为 `../../../pom.xml`）：
- `loadup-modules-{mod}-client`
- `loadup-modules-{mod}-domain`
- `loadup-modules-{mod}-infrastructure`
- `loadup-modules-{mod}-app`
- `loadup-modules-{mod}-test`

### 2. client 层

包路径：`io.github.loadup.modules.{mod}.client`

生成：
- `{Entity}DTO`（对外数据传输对象）
- `{Entity}CreateCommand`（创建命令）
- `{Entity}UpdateCommand`（更新命令）
- `{Entity}Query`（分页查询条件）

### 3. domain 层

包路径：`io.github.loadup.modules.{mod}.domain`

生成：
- `{Entity}`（纯 POJO 领域模型，无任何框架注解）放至 `domain.model`
- `{Entity}Gateway`（仓储接口，含 findById / findAll / save / update / deleteById / existsById）放至 `domain.gateway`
- 业务相关枚举（如有）放至 `domain.enums`

⚠️ 此层禁止任何 `@Table`、`@Service`、Spring、JPA 注解。

### 4. infrastructure 层

包路径：`io.github.loadup.modules.{mod}.infrastructure`

生成：
- `{Entity}DO extends BaseDO`（`@Table("{table}")`，不重复定义 id/createdAt/updatedAt/tenantId/deleted）放至 `infrastructure.dataobject`
- `{Entity}DOMapper extends BaseMapper<{Entity}DO>`（只继承，不添加方法）放至 `infrastructure.mapper`
- `{Entity}Converter`（MapStruct `@Mapper(componentModel = "spring")`，toModel + toEntity）放至 `infrastructure.converter`
- `{Entity}GatewayImpl implements {Entity}Gateway`（用 QueryWrapper 查询，通过 converter 转换）放至 `infrastructure.repository`

### 5. app 层

包路径：`io.github.loadup.modules.{mod}.app`

生成：
- `{Entity}Service`（`@Service`，含 listAll / getById / create / update / delete）放至 `app.service`
  - 所有写操作加 `@Transactional(rollbackFor = Exception.class)`
  - 构造器注入（`@RequiredArgsConstructor` + final 字段）
  - 通过 `{Entity}Gateway` 访问数据，不直接用 Mapper
- `{Mod}ModuleAutoConfiguration`（`@AutoConfiguration` + `@MapperScan` + `@ComponentScan`）放至 `app.autoconfigure`
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

### 6. 数据库 schema

生成 `schema.sql`（用于测试），表结构包含所有 5 个标准字段：
- `id VARCHAR(64) NOT NULL PRIMARY KEY`
- `tenant_id VARCHAR(64)`
- `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP`
- `deleted TINYINT NOT NULL DEFAULT 0`

### 7. Gateway 路由配置片段

生成添加到 `loadup-application/src/main/resources/application.yml` 的路由配置，含 listAll / create / getById / update / delete 五个路由。

### 8. 集成测试

生成 `{Entity}ServiceIT.java`：
- `@SpringBootTest @EnableTestContainers(ContainerType.MYSQL)`
- 至少包含 `create_shouldPersist_whenValidCommand` 和 `delete_shouldSoftDelete_whenExists` 两个测试方法

---

## 生成规范约束

- ❌ 不生成 `@RestController` / Controller 类
- ❌ 不生成 Java 文件头 License 注释块（`/*- #%L ... #L% */`）
- ❌ 不在 DO 中重复定义 `id`/`createdAt`/`updatedAt`/`tenantId`/`deleted`
- ❌ 不使用 `@Autowired` 字段注入
- ❌ 子模块 pom.xml 不写 `<version>` 引用同项目模块
- ✅ 所有 DO 继承 `BaseDO`
- ✅ 使用 MapStruct 做 DO ↔ model 转换
- ✅ 集成测试使用 `@EnableTestContainers(ContainerType.MYSQL)`

---

## 现在请填写变量并生成

- mod =
- Entity =
- table =
- 功能描述 =
