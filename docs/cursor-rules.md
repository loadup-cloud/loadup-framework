# LoadUp 项目 Cursor AI 规则

> 本文件是 `.cursor/rules/loadup.mdc` 的文档镜像版本，供开发者查阅。  
> Cursor IDE 实际使用 `.cursor/rules/loadup.mdc`（包含 frontmatter）。  
> **两份文件须保持同步。**

---

## ⚡ 最高优先级（违反则立即提醒）

1. **🚫 不生成 License 文件头** — Java 文件不得包含 `/*- #%L ... #L% */` 块，CI 由 `license-maven-plugin` 统一插入（License: GPL-3.0）
2. **🚫 不生成 Controller / adapter 层** — 通过 `bean://beanName:method` Gateway 协议直接调用 App Service
3. **🚫 集成测试不用 `@MockBean`** — 使用 `@EnableTestContainers(ContainerType.MYSQL)` + `loadup-testify-spring-boot-starter`
4. **🚫 不用 `@Autowired` 字段注入** — 全部使用 `@RequiredArgsConstructor` 构造器注入
5. **🚫 不拼接 SQL 字符串** — 使用 MyBatis-Flex `QueryWrapper`

---

## 技术栈

| 技术 | 选型 | 版本 |
|------|------|------|
| 语言 | Java | **21** |
| 框架 | Spring Boot | **3.4.3** |
| ORM | MyBatis-Flex | **1.11.5** |
| 数据库 | MySQL | 8.0+ |
| 本地缓存 | Caffeine | - |
| 分布式缓存 | Redis (Redisson) | - |
| 认证 | JWT | - |
| 测试 | JUnit 5 + Testify + Testcontainers | - |
| 格式化 | Spotless (Palantir Java Format) | - |
| 构建 | Maven | 3.6+ |
| License | **GPL-3.0** | - |

> 新增第三方依赖必须在 `loadup-dependencies/pom.xml` 中声明。

---

## 项目结构（Monorepo）

```
loadup-parent/
├── loadup-dependencies/        # BOM，统一依赖版本
├── commons/
│   ├── loadup-commons-api/     # 通用接口、常量
│   ├── loadup-commons-dto/     # Result<T>、PageDTO
│   └── loadup-commons-util/    # 工具类
├── components/                 # 可复用技术组件（无业务逻辑）
│   ├── loadup-components-authorization/   # @RequirePermission
│   ├── loadup-components-cache/           # Caffeine/Redis binder
│   ├── loadup-components-database/        # MyBatis-Flex 配置、多租户
│   ├── loadup-components-dfs/             # 文件存储
│   ├── loadup-components-flyway/
│   ├── loadup-components-globalunique/    # 全局幂等
│   ├── loadup-components-gotone/          # 消息通知 (Email/SMS/Push/Webhook)
│   ├── loadup-components-retrytask/       # 重试任务
│   ├── loadup-components-scheduler/
│   ├── loadup-components-signature/       # RSA/DSA/MD5 签名
│   ├── loadup-components-testcontainers/  # 测试容器
│   └── loadup-components-tracer/          # OpenTelemetry
├── middleware/
│   ├── loadup-gateway/         # 自研 API 网关（Spring MVC，非 WebFlux）
│   │   ├── loadup-gateway-facade/   # SPI、Model、配置属性
│   │   ├── loadup-gateway-core/     # 路由解析、Action 责任链
│   │   └── loadup-gateway-starter/
│   └── loadup-testify/         # 集成测试脚手架
├── modules/                    # 业务模块（COLA 4.0，无 adapter 层）
│   ├── loadup-modules-upms/    # 用户权限管理
│   └── loadup-modules-config/  # 配置管理（系统参数+数据字典）
└── loadup-application/         # 启动器
```

---

## 模块依赖规则（严格单向）

```
dependencies → commons → components → modules → application

gateway  ← 依赖 commons/components，不被 modules/application 依赖
testify  ← 仅 test scope

modules 之间：禁止横向相互依赖
```

### 业务模块内部分层（无 adapter 层）

```
loadup-modules-{xxx}/
├── -client/          对外 DTO + Command（可被其他模块引用）
├── -domain/          纯 DDD 模型（POJO）+ Gateway 接口 + 枚举
│                     ⚠️ 无 Spring 注解、无 @Table、无 ORM 依赖
├── -infrastructure/  XxxDO extends BaseDO、Mapper、GatewayImpl、本地缓存
├── -app/             @Service 业务编排 + AutoConfiguration
└── -test/            集成+单元测试（parent 指向根 loadup-parent pom）
```

| 层 | 放什么 | 禁止 |
|----|--------|------|
| client | DTO、Command、Query | 业务逻辑、DB 注解 |
| domain | POJO 模型、Gateway 接口、枚举 | `@Table`、`@Service`、任何框架注解 |
| infrastructure | `XxxDO extends BaseDO`、Mapper、GatewayImpl、Cache | 业务逻辑 |
| app | `@Service`、AutoConfiguration | 直接操作 DB（通过 Gateway） |

---

## Gateway 集成方式（取代 Controller）

```yaml
loadup:
  gateway:
    routes:
      - path: /api/v1/config/list
        method: POST
        target: "bean://configItemService:listAll"
        securityCode: "default"          # JWT 认证
      - path: /api/v1/config/create
        method: POST
        target: "bean://configItemService:create"
        securityCode: "default"
      - path: /api/v1/public/dict
        method: POST
        target: "bean://dictService:getDictData"
        securityCode: "OFF"              # 关闭认证
```

| securityCode | 含义 |
|---|---|
| `OFF` | 关闭所有安全校验 |
| `default` | JWT Token 验证 |
| `hmac` | HMAC 签名验签 |
| 自定义 | 实现 `SecurityStrategy` SPI |

---

## 包命名规范

业务模块根包：`io.github.loadup.modules.{mod}`

| 子模块 | 层 | 包路径 |
|--------|-----|--------|
| `{mod}-client` | DTO | `io.github.loadup.modules.{mod}.client.dto` |
| `{mod}-client` | Command | `io.github.loadup.modules.{mod}.client.command` |
| `{mod}-client` | Query | `io.github.loadup.modules.{mod}.client.query` |
| `{mod}-client` | Service接口 | `io.github.loadup.modules.{mod}.client.service` |
| `{mod}-domain` | model（纯 POJO）| `io.github.loadup.modules.{mod}.domain.model` |
| `{mod}-domain` | gateway 接口 | `io.github.loadup.modules.{mod}.domain.gateway` |
| `{mod}-domain` | enums | `io.github.loadup.modules.{mod}.domain.enums` |
| `{mod}-domain` | valueobject | `io.github.loadup.modules.{mod}.domain.valueobject` |
| `{mod}-infrastructure` | DO 实体 | `io.github.loadup.modules.{mod}.infrastructure.dataobject` |
| `{mod}-infrastructure` | Mapper（APT 生成）| `io.github.loadup.modules.{mod}.infrastructure.mapper` |
| `{mod}-infrastructure` | Tables/TableDef（APT 生成）| `io.github.loadup.modules.{mod}.infrastructure.dataobject.table` |
| `{mod}-infrastructure` | MapStruct Converter | `io.github.loadup.modules.{mod}.infrastructure.converter` |
| `{mod}-infrastructure` | GatewayImpl | `io.github.loadup.modules.{mod}.infrastructure.repository` |
| `{mod}-infrastructure` | 本地缓存 | `io.github.loadup.modules.{mod}.infrastructure.cache` |
| `{mod}-app` | @Service 业务服务 | `io.github.loadup.modules.{mod}.app.service` |
| `{mod}-app` | AutoConfiguration | `io.github.loadup.modules.{mod}.app.autoconfigure` |

---

## 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 数据库映射对象 | `XxxDO` | `ConfigItemDO` |
| DTO（对外） | `XxxDTO` | `ConfigItemDTO` |
| 创建命令 | `XxxCreateCommand` | `ConfigItemCreateCommand` |
| 更新命令 | `XxxUpdateCommand` | `ConfigItemUpdateCommand` |
| Gateway 接口 | `XxxGateway` | `ConfigItemGateway` |
| Gateway 实现 | `XxxGatewayImpl` | `ConfigItemGatewayImpl` |
| Mapper | `XxxMapper` | `ConfigItemMapper` |
| Service（无 impl 分离）| `XxxService` | `ConfigItemService` |
| 本地缓存 | `XxxLocalCache` | `ConfigLocalCache` |
| AutoConfig | `XxxModuleAutoConfiguration` | `ConfigModuleAutoConfiguration` |
| 集成测试 | `XxxServiceIT` | `ConfigItemServiceIT` |
| 单元测试 | `XxxServiceTest` | `ConfigItemServiceTest` |

---

## 代码模板

### DO 实体（infrastructure 层，继承 BaseDO）

> ⚠️ **DO 放 `infrastructure.dataobject` 包，domain 层只放纯 POJO + Gateway 接口，禁止 `@Table`。**
> 所有 DO **必须继承 `BaseDO`**，使用 `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor` + `@EqualsAndHashCode(callSuper = true)`。
> **不使用 `@Builder` / `@SuperBuilder`**（MyBatis-Flex 通过反射填充，无需 builder）。
> `BaseDO` 已提供 `id`、`createdAt`、`updatedAt`、`tenantId`、`deleted`，子类不得重复定义。

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

    // 业务字段（id/createdAt/updatedAt/tenantId/deleted 已在 BaseDO 中，勿重复定义）
    private String createdBy;
    private String updatedBy;
    // ... 其他业务字段
}
```

### 对象转换（MapStruct）

**DO ↔ domain model 转换必须使用 MapStruct**，禁止手写 setter 链或 builder 链。

```java
package io.github.loadup.modules.{mod}.infrastructure.converter;

import io.github.loadup.modules.{mod}.infrastructure.dataobject.{Entity}DO;
import io.github.loadup.modules.{mod}.model.{Entity};
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface {Entity}Converter {
    {Entity} toModel({Entity}DO entity);
    {Entity}DO toEntity({Entity} model);
}
```

### Gateway 实现

> **Mapper**：使用 APT 生成的 `{Entity}DOMapper`（`infrastructure.mapper` 包），不手写 Mapper。
> **表字段引用**：统一通过 `Tables.{ENTITY}_DO` 静态导入，禁止直接用 `{Entity}DOTableDef.{ENTITY}_D_O`。
>
> ✅ **`mybatis-flex.config` 只需放在项目根目录一份**，MyBatis-Flex APT 编译时会自动向上逐层查找并合并（冒泡机制），各子模块无需重复放置。
>
> 根目录 `mybatis-flex.config`（`loadup-parent/mybatis-flex.config`）：
>
> ```properties
> processor.tables-generate-enable=true
> processor.entity-generate-enable=false
> processor.allInTables.enable=true
> processor.tables-class-name=Tables
> processor.mapper.generateEnable=true
> processor.mapper.annotation=true
> ```
>
> ⚠️ 不能通过 Maven `-A` compilerArg 传入这些配置，因为 key 含连字符（如 `tables-generate-enable`）不是合法 Java 标识符，会导致编译报错。

```java
package io.github.loadup.modules.{mod}.infrastructure.repository;

import static io.github.loadup.modules.{mod}.infrastructure.dataobject.table.Tables.{ENTITY}_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.{mod}.infrastructure.converter.{Entity}Converter;
import io.github.loadup.modules.{mod}.infrastructure.mapper.{Entity}DOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class {Entity}GatewayImpl implements {Entity}Gateway {

    private final {Entity}DOMapper mapper;
    private final {Entity}Converter converter;

    @Override
    public Optional<{Entity}> findById(String id) {
        return Optional.ofNullable(
                mapper.selectOneByQuery(QueryWrapper.create().where({ENTITY}_DO.ID.eq(id))))
                .map(converter::toModel);
    }
    // ... 其他方法
}
```

### App Service（被 Gateway 直接调用）

```java
package io.github.loadup.modules.{mod}.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 通过 Gateway 路由暴露：bean://{entity}Service:method */
@Slf4j
@Service
@RequiredArgsConstructor
public class {Entity}Service {

    private final {Entity}Gateway gateway;
    private final {Entity}Converter converter;

    public List<{Entity}DTO> listAll() {
        return gateway.findAll().stream().map(converter::toModel).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public String create(@Valid {Entity}CreateCommand cmd) {
        // 1. 校验  2. 构建 domain model（UUID id）  3. gateway.save  4. return id
    }
}
```

### AutoConfiguration

```java
package io.github.loadup.modules.{mod}.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "io.github.loadup.modules.{mod}")
@MapperScan("io.github.loadup.modules.{mod}.infrastructure.mapper")
public class {Mod}ModuleAutoConfiguration {}
```

---

## 测试模板

### 集成测试（Testify + Testcontainers）

```java
@SpringBootTest
@EnableTestContainers(ContainerType.MYSQL)          // 真实 MySQL 容器
class {Entity}ServiceIT {

    @Autowired private {Entity}Service service;

    @BeforeEach void setUp() { /* 清数据 */ }

    @Test
    void create_shouldPersist_whenValidCommand() {
        var cmd = new {Entity}CreateCommand();
        String id = service.create(cmd);
        assertThat(id).isNotBlank();
    }
}
```

### 单元测试（Mockito，无 DB）

```java
@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {
    @Mock {Entity}Gateway gateway;
    @InjectMocks {Entity}Service service;

    @Test
    void create_shouldThrow_whenKeyExists() {
        when(gateway.existsByKey(any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(new {Entity}CreateCommand()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

**test 模块 pom.xml 的 parent 指向根 `loadup-parent`，不是模块 pom：**

```xml
<parent>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-parent</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <relativePath>../../../pom.xml</relativePath>
</parent>
```

### 测试模块三文件规范 🚫

**每个 `*-test` 模块的 `src/test/resources/` 目录下必须包含以下三个 yml 文件：**

| 文件 | 作用 | 关键配置 |
|------|------|---------|
| `application.yml` | 入口，激活 `test` profile | `spring.profiles.active: test` |
| `application-test.yml` | 本地开发配置 | `testcontainers.reuse.enable: true`，详细日志，`print-sql: true` |
| `application-ci.yml` | CI 流水线配置 | `testcontainers.reuse.enable: false`，精简连接池，`print-sql: false` |

```yaml
# application.yml（固定内容）
spring:
  profiles:
    active: test
```

```yaml
# application-test.yml（本地开发）
spring:
  application:
    name: loadup-{mod}-test
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      pool-name: LoadupTestPool
      minimum-idle: 2
      maximum-pool-size: 10
      connection-timeout: 30000
      connection-test-query: SELECT 1
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      continue-on-error: false
mybatis-flex:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    cache-enabled: false
  global-config:
    print-sql: true
testcontainers:
  reuse:
    enable: true
logging:
  level:
    io.github.loadup: DEBUG
    org.springframework.jdbc: DEBUG
```

```yaml
# application-ci.yml（CI 环境）
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      continue-on-error: false
mybatis-flex:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
  global-config:
    print-sql: false
testcontainers:
  reuse:
    enable: false
logging:
  level:
    root: WARN
    io.github.loadup: INFO
```

---

## 数据库规范

### 必备标准字段 🚫

**每张表都必须包含以下 5 个标准字段，缺一不可：**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | `VARCHAR(64)` | `NOT NULL PRIMARY KEY` | UUID，业务层赋值，**禁止** `BIGINT AUTO_INCREMENT` |
| `tenant_id` | `VARCHAR(64)` | 可为 NULL | 多租户隔离 |
| `created_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |
| `deleted` | `TINYINT` | `NOT NULL DEFAULT 0` | 软删除标记（0/1），**禁止**用 `BOOLEAN` |

```sql
-- 标准 DDL 模板
id         VARCHAR(64)  NOT NULL                                    COMMENT 'ID',
tenant_id  VARCHAR(64)                                              COMMENT '租户ID',
...业务字段...
created_by VARCHAR(64)                                              COMMENT '创建人',
created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP          COMMENT '创建时间',
updated_by VARCHAR(64)                                              COMMENT '更新人',
updated_at DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP   COMMENT '更新时间',
deleted    TINYINT      NOT NULL DEFAULT 0                          COMMENT '删除标记',
PRIMARY KEY (id),
```

### 其他规范

- 表名：`snake_case`（不加 `t_` 前缀，如 `config_item`、`dict_type`）
- 审计字段（可选）：`created_by VARCHAR(64)`, `updated_by VARCHAR(64)`
- 大表按月分区：`PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at))`
- Schema：模块根目录 `schema.sql`
- Flyway：`src/main/resources/db/migration/V{n}__{desc}.sql`
- **测试 schema 必须与生产 schema 字段保持一致**

---

## 安全规范

- 密码字段 `@JsonIgnore`，DTO 不暴露
- 日志不打印 Token / 密码 / 敏感字段
- 权限校验：`@RequirePermission("module:action")`（`loadup-components-authorization`）
- SQL 全部用 `QueryWrapper`

---

## 禁止项速查 🚫

| 禁止行为 | 正确做法 |
|---------|---------|
| `/*- #%L ... #L% */` License 头 | 不写，CI 自动插入 |
| `@RestController` / adapter 层 | Gateway `bean://` 路由 |
| `@Autowired` 字段注入 | `@RequiredArgsConstructor` |
| 集成测试用 `@MockBean` 替换 DB | `@EnableTestContainers(ContainerType.MYSQL)` |
| 字符串拼接 SQL | `QueryWrapper` |
| `SELECT *` | 显式列出字段 |
| modules 间横向依赖 | 通过 client 模块共享 |
| domain 层加 `@Service` / `@Table` 等框架注解 | domain 纯 POJO |
| DO 直接 `implements Serializable` 而不继承 `BaseDO` | 继承 `BaseDO`，使用 `@SuperBuilder` |
| DO 中重复定义 `id`/`createdAt`/`updatedAt` | 这些字段已在 `BaseDO` 中定义 |
| DO 放在 domain 层 | DO 放在 infrastructure 层 |
| 在 `XxxMapper` 中写额外方法 | 用 `QueryWrapper` 在 GatewayImpl 中操作，Mapper 只继承 `BaseMapper<XxxDO>` |
| 表主键使用 `BIGINT AUTO_INCREMENT` | 统一使用 `VARCHAR(64)`，业务层赋 UUID |
| 表字段使用 `BOOLEAN`/`BOOL` 类型 | 统一使用 `TINYINT`（0=false, 1=true） |
| 表缺少 `tenant_id`/`deleted`/`created_at`/`updated_at` | 每张表必须包含这 5 个标准字段 |
| 测试 schema 与生产 schema 字段不一致 | 新增/修改字段时同步更新测试 schema |
| 日志打印密码/Token | 脱敏或不打印 |
| 新依赖不在 `loadup-dependencies` 声明 | 先在 BOM 中声明 |
| pom.xml `<parent>` 指向模块自身聚合 pom | 所有子模块 parent 统一指向根 `loadup-parent` |

---

## pom.xml parent 规范

**所有子模块的 `<parent>` 必须统一指向根 `loadup-parent`**，`relativePath` 按子模块到根 `pom.xml` 的实际相对路径填写：

```xml
<parent>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-parent</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <relativePath>../../../pom.xml</relativePath>
</parent>
```

| 子模块位置 | relativePath |
|-----------|--------------|
| `modules/loadup-modules-xxx/loadup-modules-xxx-{layer}/` | `../../../pom.xml` |
| `commons/loadup-commons-xxx/` | `../../pom.xml` |
| `components/loadup-components-xxx/` | `../../pom.xml` |
| `middleware/loadup-gateway/loadup-gateway-xxx/` | `../../../pom.xml` |
| `middleware/loadup-testify/loadup-testify-xxx/` | `../../../pom.xml` |

聚合 pom（如 `modules/loadup-modules-xxx/pom.xml`）才指向其直接父层（`../../pom.xml`）。

---

## loadup-dependencies 版本管理规范 🚫

**所有项目内模块的版本管理必须统一在 `loadup-dependencies/pom.xml` 的 `<dependencyManagement>` 中声明。**

### 规则

1. **新建任何模块**，必须同步在 `loadup-dependencies/pom.xml` 的 `<dependencyManagement>` 中添加全部子模块条目：

```xml
<!-- ========== loadup-modules-xxx start ==========-->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-xxx-client</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-xxx-domain</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-xxx-infrastructure</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-xxx-app</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-xxx-test</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<!-- ========== loadup-modules-xxx end ==========-->
```

2. **子模块 pom.xml 中引用同项目内其他模块时，不得写 `<version>`**：

```xml
<!-- ✅ 正确：版本由 BOM 管理 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-config-domain</artifactId>
</dependency>

<!-- 🚫 禁止：手动写 version -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-config-domain</artifactId>
    <version>${project.version}</version>
</dependency>
```

3. **新增第三方依赖**也必须先在 `loadup-dependencies/pom.xml` 声明版本，子模块中不写 `<version>`。

---

## 质量门

- `mvn clean verify` 通过（含测试）
- 覆盖率 ≥ 80%（核心 Service）
- `mvn spotless:check` 格式化通过
- 无循环依赖（ArchUnit）
- 无高危依赖漏洞（OWASP Dependency-Check）
