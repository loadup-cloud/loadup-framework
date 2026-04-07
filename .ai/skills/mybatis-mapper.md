# Skill: MyBatis-Flex Mapper — LoadUp 内部 ORM 编写规范

> 本技能基于 MyBatis-Flex 1.11.5 + LoadUp 项目约定，覆盖 DO / Mapper / GatewayImpl / Converter 完整规范。
> 所有模板中 `{mod}` / `{Entity}` / `{ENTITY}` / `{table_name}` 为占位符，使用时替换为实际名称。

---

## 1. BaseDO 已有字段（禁止在子类重复声明）

所有 DO 必须继承 `BaseDO`，以下字段已由父类提供：

| Java 字段 | DB 列 | 类型 | 说明 |
|-----------|-------|------|------|
| `id` | `id` | `VARCHAR(64)` | 主键，业务层赋 UUID |
| `tenantId` | `tenant_id` | `VARCHAR(64)` | 多租户隔离 |
| `createdAt` | `created_at` | `DATETIME` | 创建时间 |
| `updatedAt` | `updated_at` | `DATETIME` | 更新时间 |
| `deleted` | `deleted` | `TINYINT` | 软删除（0=未删除，1=已删除） |

> ⚠️ **禁止**在子类 DO 中重复声明上述任何字段。

---

## 2. DO 实体模板

```java
package io.github.loadup.modules.{mod}.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("{table_name}")
public class {Entity}DO extends BaseDO {

    // 业务字段（id/createdAt/updatedAt/tenantId/deleted 已在 BaseDO，勿重复）
    private String createdBy;    // 可选：操作人
    private String updatedBy;    // 可选：操作人
    // 其他业务字段...
}
```

**DO 必须放在 `infrastructure.dataobject` 包**，不得放在 domain 层。

---

## 3. Mapper 接口模板

```java
package io.github.loadup.modules.{mod}.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.modules.{mod}.infrastructure.dataobject.{Entity}DO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {Entity}DOMapper extends BaseMapper<{Entity}DO> {
    // 不写任何额外方法
    // 所有查询逻辑用 QueryWrapper 在 GatewayImpl 中实现
}
```

> ⚠️ Mapper 只继承 `BaseMapper<XxxDO>`，**不写任何自定义方法**（无论 XML 还是注解）。

---

## 4. mybatis-flex.config 配置

**只在项目根目录放一份**（`loadup-parent/mybatis-flex.config`），APT 编译时自动冒泡查找，各子模块无需重复放置。

```properties
processor.tables-generate-enable=true
processor.entity-generate-enable=false
processor.allInTables.enable=true
processor.tables-class-name=Tables
processor.mapper.generateEnable=true
processor.mapper.annotation=true
```

> ⚠️ 不可通过 Maven `<compilerArg>-Aprocessor.xxx</compilerArg>` 传入，因为含连字符的 key 不是合法 Java 标识符。

---

## 5. APT 生成的 Tables 引用规范

MyBatis-Flex APT 生成：
- `entity.table.Tables` — 聚合类，包含所有 TableDef 的静态常量
- `entity.table.{Entity}DOTableDef` — 单个表的字段定义

**统一通过 `Tables` 引用表和字段，禁止直接用 `XxxDOTableDef`**：

```java
// ✅ 正确
import static io.github.loadup.modules.{mod}.infrastructure.dataobject.table.Tables.CONFIG_ITEM_DO;

// 在 QueryWrapper 中使用
QueryWrapper.create().where(CONFIG_ITEM_DO.CONFIG_KEY.eq("some.key"));

// 🚫 禁止
import static io.github.loadup.modules.{mod}.infrastructure.dataobject.table.ConfigItemDOTableDef.CONFIG_ITEM_D_O;
```

---

## 6. GatewayImpl 中的 QueryWrapper 常用模式

```java
package io.github.loadup.modules.{mod}.infrastructure.repository;

import static io.github.loadup.modules.{mod}.infrastructure.dataobject.table.Tables.{ENTITY}_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.{mod}.domain.gateway.{Entity}Gateway;
import io.github.loadup.modules.{mod}.domain.model.{Entity};
import io.github.loadup.modules.{mod}.infrastructure.converter.{Entity}Converter;
import io.github.loadup.modules.{mod}.infrastructure.mapper.{Entity}DOMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class {Entity}GatewayImpl implements {Entity}Gateway {

    private final {Entity}DOMapper mapper;
    private final {Entity}Converter converter;

    // --- 常用 QueryWrapper 模式 ---

    // 按 ID 查单条
    @Override
    public Optional<{Entity}> findById(String id) {
        return Optional.ofNullable(mapper.selectOneById(id)).map(converter::toModel);
    }

    // 条件查单条
    public Optional<{Entity}> findByKey(String key) {
        var query = QueryWrapper.create()
            .where({ENTITY}_DO.CONFIG_KEY.eq(key))
            .limit(1);
        return Optional.ofNullable(mapper.selectOneByQuery(query)).map(converter::toModel);
    }

    // 批量 IN 查询（避免 N+1）
    public List<{Entity}> findByIds(List<String> ids) {
        var query = QueryWrapper.create()
            .where({ENTITY}_DO.ID.in(ids));
        return mapper.selectListByQuery(query).stream().map(converter::toModel).toList();
    }

    // 模糊查询
    public List<{Entity}> searchByName(String keyword) {
        var query = QueryWrapper.create()
            .where({ENTITY}_DO.NAME.like(keyword))
            .orderBy({ENTITY}_DO.CREATED_AT.desc());
        return mapper.selectListByQuery(query).stream().map(converter::toModel).toList();
    }

    // 分页查询
    public com.mybatisflex.core.paginate.Page<{Entity}> page(int pageNum, int pageSize) {
        var query = QueryWrapper.create()
            .orderBy({ENTITY}_DO.CREATED_AT.desc());
        return mapper.paginateAs(pageNum, Math.min(pageSize, 200), query, {Entity}.class);
    }

    // 存在性检查
    @Override
    public boolean existsById(String id) {
        return mapper.selectCountByCondition({ENTITY}_DO.ID.eq(id)) > 0;
    }

    // 批量保存（单批 ≤ 1000 条）
    public void saveBatch(List<{Entity}> entities) {
        var doList = entities.stream().map(converter::toEntity).toList();
        mapper.insertBatch(doList);
    }

    @Override
    public void save({Entity} entity) {
        mapper.insert(converter.toEntity(entity));
    }

    @Override
    public void update({Entity} entity) {
        mapper.update(converter.toEntity(entity));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public List<{Entity}> findAll() {
        return mapper.selectAll().stream().map(converter::toModel).toList();
    }
}
```

---

## 7. MapStruct Converter 模板

```java
package io.github.loadup.modules.{mod}.infrastructure.converter;

import io.github.loadup.modules.{mod}.domain.model.{Entity};
import io.github.loadup.modules.{mod}.infrastructure.dataobject.{Entity}DO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface {Entity}Converter {

    /** DO → domain model */
    {Entity} toModel({Entity}DO entity);

    /** domain model → DO */
    {Entity}DO toEntity({Entity} model);
}
```

> ⚠️ **必须使用 MapStruct**，禁止手写 setter 链或 builder 链进行对象转换。

---

## 8. 数据库表规范

### 8.1 每张表必须包含的 5 个标准字段

```sql
id         VARCHAR(64)  NOT NULL PRIMARY KEY             COMMENT 'ID',
tenant_id  VARCHAR(64)                                   COMMENT '租户ID',
created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_at DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
deleted    TINYINT      NOT NULL DEFAULT 0               COMMENT '删除标记（0=未删除，1=已删除）',
```

### 8.2 完整建表模板

```sql
CREATE TABLE {table_name} (
    id         VARCHAR(64)  NOT NULL                      COMMENT 'ID',
    tenant_id  VARCHAR(64)                                COMMENT '租户ID',
    -- 业务字段
    name       VARCHAR(255) NOT NULL                      COMMENT '名称',
    status     TINYINT      NOT NULL DEFAULT 1            COMMENT '状态（1=启用，0=禁用）',
    -- 审计字段（可选）
    created_by VARCHAR(64)                                COMMENT '创建人',
    updated_by VARCHAR(64)                                COMMENT '更新人',
    -- 标准字段（必须）
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT      NOT NULL DEFAULT 0            COMMENT '删除标记',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{表描述}';
```

### 8.3 字段规范

| 规则 | 说明 |
|------|------|
| 主键类型 | `VARCHAR(64)` + 业务层 `UUID.randomUUID().toString().replace("-","")` 赋值 |
| 禁止 | `BIGINT AUTO_INCREMENT` 主键 |
| 布尔值 | `TINYINT`（0/1），**禁止** `BOOLEAN`/`BOOL` |
| 表名 | `snake_case`（如 `config_item`、`dict_type`） |
| 缺失字段 | 5 个标准字段（id/tenant_id/created_at/updated_at/deleted）缺一不可 |

---

## 9. Flyway Migration 规范

- 脚本位置：`src/main/resources/db/migration/V{n}__{描述}.sql`
- 命名示例：`V1__create_config_item_table.sql`，`V2__add_security_code_column.sql`
- 测试用 `schema.sql`（`*-test/src/test/resources/schema.sql`）**必须与生产 schema 字段完全一致**

---

## 10. 性能约束

| 场景 | 约束 |
|------|------|
| 批量写入 | `insertBatch()` / `updateBatch()`，单批 ≤ 1000 条 |
| 列表查询 | 必须分页，单页 ≤ 200 条 |
| 关联查询 | 避免 N+1，用 `.in(ids)` 批量查 或 JOIN |
| 读多写少数据 | 用 Caffeine 本地缓存；写操作后主动 evict |
| 大量数据统计 | 避免 `SELECT *`，只查所需字段 |

---

## 11. 禁止项速查

| 禁止行为 | 正确做法 |
|---------|---------|
| `@Table` 放在 domain 层 | DO 只放 `infrastructure.dataobject` |
| DO 重复声明 `id`/`createdAt`/`updatedAt` | 这些字段在 `BaseDO` 中 |
| Mapper 写额外 SQL 方法 | QueryWrapper 在 GatewayImpl 中 |
| 字符串拼接 SQL | 使用 `QueryWrapper` |
| 直接使用 `XxxDOTableDef` | 统一通过 `Tables.XXXX_DO` |
| 手写 setter 链对象转换 | 使用 MapStruct |
| `SELECT *` | 指定查询字段 |
| 表字段用 `BOOLEAN`/`BOOL` | 统一 `TINYINT`（0/1） |
| 主键 `BIGINT AUTO_INCREMENT` | `VARCHAR(64)` + UUID |
| 子模块各自放 `mybatis-flex.config` | 只在根目录放一份 |
