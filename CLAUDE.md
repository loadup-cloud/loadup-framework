# LoadUp — Claude Code 项目上下文

> 完整编码规范见 `.github/copilot-instructions.md`。本文件是 Claude Code 每次 session 启动时的精简快速上下文。

---

## 构建与质量命令

```bash
# 全量构建 + 测试
mvn clean verify

# 跳过测试（仅编译打包）
mvn clean install -DskipTests

# 格式化代码（本地修复）
mvn spotless:apply

# 检查格式化（不修改文件，CI 使用此命令）
mvn spotless:check

# 单模块构建（含依赖）
mvn clean verify -pl modules/loadup-modules-config/loadup-modules-config-app -am

# 单模块测试
mvn test -pl modules/loadup-modules-config/loadup-modules-config-test
```

---

## 硬性禁止项（违反会导致构建失败或逻辑错误）

| # | 禁止行为 | 正确做法 |
|---|---------|---------|
| 1 | Java 文件头写 `/*- #%L ... #L% */` License 块 | 不写，CI 的 `license-maven-plugin` 自动插入 |
| 2 | 创建 `@RestController` / `@Controller` | 在 `application.yml` 用 `bean://serviceName:method` 路由 |
| 3 | 集成测试中用 `@MockBean` 替代 DB | 用 `@EnableTestContainers(ContainerType.MYSQL)` 启动真实容器 |
| 4 | `@Autowired` 字段注入 | 构造器注入：类加 `@RequiredArgsConstructor`，字段加 `final` |
| 5 | 字符串拼接 SQL | 使用 MyBatis-Flex `QueryWrapper` |
| 6 | `@Table` 放在 domain 层 | DO（`XxxDO extends BaseDO`）只放在 `infrastructure.dataobject` |
| 7 | 子模块 `<parent>` 指向模块自身 pom | 所有子模块 `<parent>` 统一指向根 `loadup-parent` |
| 8 | 子模块内写 `<version>` 引用同项目模块 | 版本由 `loadup-dependencies` BOM 统一管理，不写 `<version>` |
| 9 | 表主键 `BIGINT AUTO_INCREMENT` | 主键 `VARCHAR(64)`，业务层用 `UUID.randomUUID()` 赋值 |
| 10 | modules 之间横向互相依赖 | 严格单向：commons → components → modules → application |

---

## 模块依赖方向（严格单向）

```
loadup-dependencies (BOM)
        ↑
   commons/*
        ↑
  components/*
        ↑
   modules/*   ← 模块间禁止横向依赖
        ↑
loadup-application

middleware/loadup-gateway   → 可依赖 commons、components；不被 modules/application 依赖
middleware/loadup-testify   → 仅 test scope
```

---

## 业务模块内部分层（COLA 4.0）

```
loadup-modules-{mod}/
├── {mod}-client/          # DTO、Command、Query（可被其他模块依赖）
├── {mod}-domain/          # 纯 POJO + Gateway 接口 + 枚举（零框架注解）
├── {mod}-infrastructure/  # DO extends BaseDO、Mapper、GatewayImpl、Converter
├── {mod}-app/             # @Service 业务编排、AutoConfiguration
└── {mod}-test/            # 集成测试 + 单元测试（parent = 根 loadup-parent）
```

**domain 层铁律**：无 `@Table`、无 `@Service`、无任何 Spring / ORM 注解。

---

## API 暴露方式（无 Controller）

```yaml
# loadup-application/src/main/resources/application.yml
loadup:
  gateway:
    routes:
      - path: /api/v1/config/list
        method: POST
        target: "bean://configItemService:listAll"
        securityCode: "default"   # JWT 认证
      - path: /api/v1/config/value
        method: POST
        target: "bean://configItemService:getValue"
        securityCode: "OFF"       # 关闭认证
```

---

## 数据库表规范

每张表必须包含 5 个标准字段（缺一不可）：

```sql
id         VARCHAR(64)  NOT NULL PRIMARY KEY,
tenant_id  VARCHAR(64),
created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
deleted    TINYINT      NOT NULL DEFAULT 0
```

- 禁止 `BOOLEAN`/`BOOL`，统一用 `TINYINT`（0/1）
- Flyway 脚本命名：`V{n}__{描述}.sql`，放 `src/main/resources/db/migration/`

---

## 包命名（根包：`io.github.loadup.modules.{mod}`）

| 层 | 包路径 |
|----|--------|
| client DTO | `.client.dto` |
| client Command | `.client.command` |
| domain model | `.domain.model` |
| domain gateway 接口 | `.domain.gateway` |
| infra DO | `.infrastructure.dataobject` |
| infra Mapper | `.infrastructure.mapper` |
| infra GatewayImpl | `.infrastructure.repository` |
| infra Converter | `.infrastructure.converter` |
| app Service | `.app.service` |
| app AutoConfig | `.app.autoconfigure` |

---

## 命名约定

| 类型 | 规则 | 示例 |
|------|------|------|
| 数据库映射对象 | `XxxDO extends BaseDO` | `ConfigItemDO` |
| 对外 DTO | `XxxDTO` | `ConfigItemDTO` |
| 写操作入参 | `XxxCreateCommand` / `XxxUpdateCommand` | `ConfigItemCreateCommand` |
| 查询入参 | `XxxQuery` | `ConfigItemQuery` |
| Gateway 接口 | `XxxGateway` | `ConfigItemGateway` |
| Gateway 实现 | `XxxGatewayImpl` | `ConfigItemGatewayImpl` |
| Service | `XxxService`（直接 `@Service`，无 impl） | `ConfigItemService` |
| AutoConfig | `XxxModuleAutoConfiguration` | `ConfigModuleAutoConfiguration` |

---

## 技术栈速查

- Java **21** | Spring Boot **3.4.3** | MyBatis-Flex **1.11.5**
- MySQL 8.0+ | Caffeine（本地缓存）| Redis/Redisson（分布式缓存）
- JUnit 5 + `loadup-testify-spring-boot-starter` + Testcontainers
- Spotless (Palantir Java Format) | Maven 3.6+
- License: **GPL-3.0**

---

## 关键参考文件

| 文件 | 用途 |
|------|------|
| `.github/copilot-instructions.md` | 完整编码规范（代码模板、禁止项清单、测试规范） |
| `mkdocs/docs/architecture.md` | 框架整体架构与分层设计 |
| `mkdocs/docs/ai-project-context.md` | 模块职责与常用工具类位置 |
| `components/loadup-components-database/ARCHITECTURE.md` | MyBatis-Flex 集成与审计设计 |
| `modules/loadup-modules-upms/ARCHITECTURE.md` | 业务模块 COLA 4.0 实现参考 |
