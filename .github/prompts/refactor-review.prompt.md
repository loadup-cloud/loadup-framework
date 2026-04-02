---
mode: agent
description: 对 LoadUp 代码进行分层规范合规审查，输出问题清单与改进建议
---

# 重构合规审查

## 使用方式

将待审查的代码片段、文件路径或模块名粘贴到"审查范围"一节，然后运行。

---

## 任务描述

请审查以下 LoadUp 代码，按照 COLA 4.0 分层规范和项目编码约束，逐项检查并输出问题清单。

对每个问题，给出：
1. 问题描述（一句话）
2. 违反的规则（引用具体条目）
3. 改进建议（可直接执行的代码修改）

---

## 检查维度

### A. 分层与依赖方向

- `domain` 层是否包含 `@Table`、`@Service` 或 Spring/ORM 注解？
- `app` 层 Service 是否直接注入 Mapper 或操作 DO？
- 是否存在 modules 之间横向依赖？
- 是否存在从底层到高层的反向依赖？

### B. 持久层规范

- DO 是否继承 `BaseDO`？是否重复定义 `id`/`createdAt`/`updatedAt`/`tenantId`/`deleted`？
- Mapper 是否只继承 `BaseMapper<XxxDO>`（不添加额外 SQL 方法）？
- 是否有字符串拼接 SQL？
- 是否有 `SELECT *` 查询？
- DO ↔ model 转换是否使用 MapStruct？

### C. 代码规范

- 是否存在 `@Autowired` 字段注入？
- 是否存在 `@RestController` / `@Controller`？
- Java 文件头是否包含 License 注释块（`/*- #%L ... #L% */`）？
- Service 是否有 impl / 接口拆分（应直接 `@Service` 无 impl）？

### D. 数据库表结构（如提供 SQL）

- 是否缺少 5 个标准字段：`id`、`tenant_id`、`created_at`、`updated_at`、`deleted`？
- 主键是否使用 `VARCHAR(64)`？
- 是否使用了 `BOOLEAN`/`BOOL`？

### E. pom.xml（如提供）

- 子模块 `<parent>` 是否指向根 `loadup-parent`？
- 是否对同项目模块写了 `<version>`？
- 新增三方依赖是否已在 `loadup-dependencies` 声明？

### F. 测试代码（如提供）

- 集成测试是否使用 `@EnableTestContainers(ContainerType.MYSQL)`？
- 是否有 `@MockBean` 替代 DB？
- 测试方法是否遵循 `methodName_shouldResult_whenCondition` 命名？
- 测试模块 `src/test/resources/` 是否包含三个 yml 文件（application.yml / application-test.yml / application-ci.yml）？

---

## 输出格式

```
## 审查结论

共发现 {N} 个问题（严重 {X} / 警告 {Y} / 建议 {Z}）

### 严重问题（必须修复）

1. [文件/行号] 问题描述
   - 违反规则：...
   - 改进：...

### 警告（建议修复）

...

### 建议（可选优化）

...

## 合规评分：{score}/100
```

---

## 审查范围

<!-- 在此粘贴待审查的代码、文件路径或模块名 -->
