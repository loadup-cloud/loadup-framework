# LoadUp — GitHub Copilot 指令

> 本文件指导 GitHub Copilot / AI 代码生成工具在 LoadUp 项目中自动生成或修改代码时的行为约束。
> **请严格遵守所有规则**，尤其是标注 🚫 的禁止项。

---

## 0. 核心原则（优先级最高）

### 0.1 不生成 License 文件头注释 🚫

- 所有 `.java` 文件均**不得**包含 `/*- #%L ... #L% */` 形式的 License 头注释块
- License 头由 `license-maven-plugin` 在 CI 阶段统一插入，手动生成会导致重复
- 本项目 License：**GPL-3.0**

### 0.2 不生成 adapter / controller 层 🚫

- 本项目通过 **LoadUp Gateway** 以 `bean://serviceName:method` 协议直接调用 App 层 `@Service` Bean
- **不需要**也**不应该**创建 `@RestController`、`@Controller` 等 HTTP 控制器
- API 路由统一在 Gateway 路由配置（YAML / 数据库）中声明

### 0.3 测试必须使用 Testify 组件 🚫

- 集成测试必须使用 `loadup-testify-spring-boot-starter` + `loadup-components-testcontainers`
- 禁止在集成测试中用 `@MockBean` 替代真实数据库；使用 `@EnableTestContainers(ContainerType.MYSQL)` 启动真实容器
- 纯逻辑单元测试（无 DB 交互）可使用 Mockito + `@ExtendWith(MockitoExtension.class)`

---

## 1. 角色定义

你是 Java 21 / Spring Boot 3.4.3 专家，专注于**单体应用优先**的企业级开发。
生成的代码应符合以下原则：安全、可测试、可维护、零循环依赖。

---

## 2. 技术栈（必须遵守）

| 技术领域 | 选型 | 版本 |
|---------|------|------|
| 语言 | Java | **21** |
| 框架 | Spring Boot | **3.4.3** |
| ORM | MyBatis-Flex | **1.11.5** |
| 数据库 | MySQL | 8.0+ |
| 本地缓存 | Caffeine | - |
| 分布式缓存 | Redis (Redisson) | - |
| 认证 | JWT | - |
| API 文档 | OpenAPI / Swagger v3 | - |
| 测试框架 | JUnit 5 + Testify + Testcontainers | - |
| 代码格式化 | Spotless (Palantir Java Format) | - |
| 构建工具 | Maven | 3.6+ |
| License | **GPL-3.0** | - |

> ⚠️ 新增第三方依赖必须在 `loadup-dependencies/pom.xml` 中声明并经过评审。

---

## 3. 项目结构（Monorepo）

```
loadup-parent/
├── loadup-dependencies/        # BOM，统一依赖版本（所有子模块依赖从此引入）
├── commons/                    # 最底层通用基础能力，无业务逻辑
│   ├── loadup-commons-api/     # 通用接口、常量
│   ├── loadup-commons-dto/     # 通用 DTO：Result<T>、PageDTO 等
│   └── loadup-commons-util/    # 工具类：StringUtils、DateUtils 等
├── components/                 # 可复用技术组件，无业务逻辑
│   ├── loadup-components-authorization/   # 方法级授权 @RequirePermission
│   ├── loadup-components-cache/           # 缓存抽象（Caffeine / Redis binder）
│   │   ├── loadup-components-cache-api/
│   │   ├── loadup-components-cache-binder-caffeine/
│   │   └── loadup-components-cache-binder-redis/
│   ├── loadup-components-database/        # MyBatis-Flex 配置、多租户、审计
│   ├── loadup-components-dfs/             # 分布式文件存储（Local / S3 / DB）
│   ├── loadup-components-flyway/          # DB migration 支持
│   ├── loadup-components-globalunique/    # 全局幂等性控制（数据库唯一键）
│   ├── loadup-components-gotone/          # 统一消息通知（Email/SMS/Push/Webhook）
│   ├── loadup-components-retrytask/       # 分布式重试任务框架
│   ├── loadup-components-scheduler/       # 任务调度（Quartz/XXL-Job/PowerJob）
│   ├── loadup-components-signature/       # 数字签名（RSA/DSA/MD5 等）
│   ├── loadup-components-testcontainers/  # 测试容器封装
│   └── loadup-components-tracer/          # OpenTelemetry 链路追踪
├── middleware/
│   ├── loadup-gateway/         # 自研 API 网关（基于 Spring MVC，非 WebFlux）
│   │   ├── loadup-gateway-facade/   # 模型、SPI 接口、配置属性
│   │   ├── loadup-gateway-core/     # 路由解析、Action 责任链、插件
│   │   ├── loadup-gateway-starter/  # AutoConfiguration
│   │   └── loadup-gateway-test/
│   └── loadup-testify/         # 测试框架（集成测试脚手架）
├── modules/                    # 业务模块（COLA 4.0 无 adapter 分层）
│   ├── loadup-modules-upms/    # 用户权限管理（RBAC3 + OAuth2 三方登录）
│   └── loadup-modules-config/  # 系统参数 + 数据字典管理
└── loadup-application/         # SpringBoot 启动器，聚合所有模块
```

---

## 4. 模块依赖规则（严格单向，禁止反向依赖）

```
dependencies
    ↑
commons
    ↑
components
    ↑
modules  ←——  不得横向相互依赖
    ↑
application

middleware/gateway  ← 可依赖 commons、components，不被 modules/application 依赖
middleware/testify  ← 仅测试 scope 使用
```

### 业务模块内部分层（COLA 4.0）

```
loadup-modules-xxx/
├── loadup-modules-xxx-client/          # 对外 DTO + Command（可被其他模块依赖）
├── loadup-modules-xxx-domain/          # 纯 DDD 模型（POJO）+ Gateway 接口 + 枚举
│                                       # ⚠️ 无 Spring 注解，无 @Table，无 ORM 依赖
├── loadup-modules-xxx-infrastructure/  # DO（extends BaseDO）+ Mapper + GatewayImpl + 缓存
├── loadup-modules-xxx-app/             # @Service 业务编排（AutoConfiguration 在此）
└── loadup-modules-xxx-test/            # 集成 + 单元测试（依赖 root pom，非 xxx pom）
```

**各层职责说明**：

| 层 | 放什么 | 禁止放什么 |
|----|--------|-----------|
| client | DTO、Command、Query | 业务逻辑、DB 注解 |
| domain | POJO 模型、Gateway 接口、枚举 | `@Table`、`@Service`、任何框架注解 |
| infrastructure | `XxxDO extends BaseDO`、`XxxMapper`、`XxxGatewayImpl`、本地缓存 | 业务逻辑 |
| app | `@Service` 业务服务、AutoConfiguration | 直接操作 DB（通过 Gateway 抽象） |

> ✅ **无 adapter 子模块** — Controller / REST 端点由 Gateway 路由配置替代。

---

## 5. Gateway 集成方式

所有业务接口通过 Gateway 路由配置暴露，无需编写 Controller：

```yaml
# loadup-application/src/main/resources/application.yml
loadup:
  gateway:
    routes:
      - path: /api/v1/config/list
        method: POST
        target: "bean://configItemService:listAll"
        securityCode: "default"        # JWT 认证
      - path: /api/v1/config/create
        method: POST
        target: "bean://configItemService:create"
        securityCode: "default"
      - path: /api/v1/config/value
        method: POST
        target: "bean://configItemService:getValue"
        securityCode: "OFF"            # 关闭认证
```

**target 格式说明**：

| 协议 | 格式 | 说明 |
|------|------|------|
| BEAN | `bean://beanName:methodName` | 调用 Spring Bean 方法（主要方式） |
| HTTP | `http://host:port/path` | HTTP 反向代理转发 |
| RPC  | `rpc://serviceName:method` | Dubbo RPC（按需启用） |

**securityCode 说明**：

| 值 | 含义 |
|----|------|
| `OFF` | 关闭所有安全校验 |
| `default` | 默认 JWT Token 验证 |
| `hmac` | HMAC 签名验签 |
| 自定义 | 通过 `SecurityStrategy` SPI 扩展 |

---

## 6. 包命名规范

业务模块根包：`io.github.loadup.modules.{mod}`

| 子模块 | 层 | 包路径 | 说明 |
|--------|-----|--------|------|
| `{mod}-client` | client DTO | `io.github.loadup.modules.{mod}.client.dto` | 对外数据传输对象 |
| `{mod}-client` | client Command | `io.github.loadup.modules.{mod}.client.command` | 写操作入参 |
| `{mod}-client` | client Query | `io.github.loadup.modules.{mod}.client.query` | 查询条件对象 |
| `{mod}-client` | client Service接口 | `io.github.loadup.modules.{mod}.client.service` | 对外暴露的服务接口 |
| `{mod}-domain` | domain model | `io.github.loadup.modules.{mod}.domain.model` | 纯 DDD 领域模型（POJO） |
| `{mod}-domain` | domain gateway | `io.github.loadup.modules.{mod}.domain.gateway` | 仓储/端口抽象接口 |
| `{mod}-domain` | domain enums | `io.github.loadup.modules.{mod}.domain.enums` | 业务枚举 |
| `{mod}-domain` | domain valueobject | `io.github.loadup.modules.{mod}.domain.valueobject` | 值对象 |
| `{mod}-infrastructure` | infra dataobject | `io.github.loadup.modules.{mod}.infrastructure.dataobject` | DO（`XxxDO extends BaseDO`）|
| `{mod}-infrastructure` | infra mapper | `io.github.loadup.modules.{mod}.infrastructure.mapper` | APT 生成的 `XxxDOMapper` |
| `{mod}-infrastructure` | infra table | `io.github.loadup.modules.{mod}.infrastructure.dataobject.table` | APT 生成的 `Tables`、`XxxDOTableDef` |
| `{mod}-infrastructure` | infra converter | `io.github.loadup.modules.{mod}.infrastructure.converter` | MapStruct Converter |
| `{mod}-infrastructure` | infra repository | `io.github.loadup.modules.{mod}.infrastructure.repository` | Gateway 实现（`XxxGatewayImpl`）|
| `{mod}-infrastructure` | infra cache | `io.github.loadup.modules.{mod}.infrastructure.cache` | 本地缓存封装 |
| `{mod}-app` | app service | `io.github.loadup.modules.{mod}.app.service` | `@Service` 业务编排 |
| `{mod}-app` | app autoconfigure | `io.github.loadup.modules.{mod}.app.autoconfigure` | `AutoConfiguration` 及配置属性 |

---

## 7. 命名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 数据库映射对象 | `XxxDO` | `ConfigItemDO` |
| 对外 DTO | `XxxDTO` | `ConfigItemDTO` |
| 创建命令 | `XxxCreateCommand` | `ConfigItemCreateCommand` |
| 更新命令 | `XxxUpdateCommand` | `ConfigItemUpdateCommand` |
| 查询对象 | `XxxQuery` | `ConfigItemQuery` |
| Gateway 接口 | `XxxGateway` | `ConfigItemGateway` |
| Gateway 实现 | `XxxGatewayImpl` | `ConfigItemGatewayImpl` |
| Mapper | `XxxMapper` | `ConfigItemMapper` |
| Service | `XxxService`（直接 `@Service`，无 impl） | `ConfigItemService` |
| 本地缓存 | `XxxLocalCache` | `ConfigLocalCache` |
| AutoConfig | `XxxModuleAutoConfiguration` | `ConfigModuleAutoConfiguration` |

---

## 8. 代码生成模板

> ⚠️ 所有生成的 Java 文件均**不包含** License 头注释。

### 8.1 DO 实体

> ⚠️ **DO 实体（`@Table` 注解）必须放在 `infrastructure.dataobject` 包，不得放在 domain 层。**
> domain 层只存放纯 DDD 模型（POJO，放 `domain.model`）和 Gateway 接口（`domain.gateway`），不依赖任何 ORM 框架。

**所有 DO 必须继承 `BaseDO`**，不使用 `@Builder` / `@SuperBuilder`：

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
}
```

### 8.2 Gateway 接口

```java
package io.github.loadup.modules.{mod}.domain.gateway;

import io.github.loadup.modules.{mod}.domain.model.{Entity};
import java.util.List;
import java.util.Optional;

public interface {Entity}Gateway {
    Optional<{Entity}> findById(String id);
    List<{Entity}> findAll();
    void save({Entity} entity);
    void update({Entity} entity);
    void deleteById(String id);
    boolean existsById(String id);
}
```

### 8.3 Mapper

MyBatis-Flex APT **只生成 `XxxDOTableDef`**（在 `entity.table` 包下），`XxxMapper` 仍需手动创建，继承 `BaseMapper<XxxDO>` 即可，**不要在 Mapper 中写额外方法**（用 `QueryWrapper` 在 GatewayImpl 中操作）。

```java
package io.github.loadup.modules.{mod}.mapper;

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.modules.{mod}.entity.{Entity}DO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {Entity}Mapper extends BaseMapper<{Entity}DO> {}
```

### 8.3.1 Tables 表名引用规范

MyBatis-Flex APT 同时生成 `Tables` 聚合类（`entity.table.Tables`）和各 `XxxDOMapper`（`entity.mapper` 包）。

- **表字段引用**：统一通过 `Tables.XXXX_DO` 静态导入，不使用 `XxxDOTableDef.XXXX_D_O`
- **Mapper 引用**：使用 APT 生成的 `XxxDOMapper`，不手写 Mapper

```java
// ✅ 正确：通过 Tables 引用
import static io.github.loadup.modules.{mod}.entity.table.Tables.CONFIG_ITEM_DO;

// 🚫 禁止：直接用 TableDef 类名
import static io.github.loadup.modules.{mod}.entity.table.ConfigItemDOTableDef.CONFIG_ITEM_D_O;
```

### 8.4 对象转换（MapStruct）

**DO ↔ domain model 转换必须使用 MapStruct**，禁止手写 setter 链或 builder 链转换。

```java
package io.github.loadup.modules.{mod}.infrastructure.converter;

import io.github.loadup.modules.{mod}.infrastructure.dataobject.{Entity}DO;
import io.github.loadup.modules.{mod}.domain.model.{Entity};
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface {Entity}Converter {

    {Entity} toModel({Entity}DO entity);

    {Entity}DO toEntity({Entity} model);
}
```

### 8.4 Gateway 实现

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

    @Override
    public Optional<{Entity}> findById(String id) {
        return Optional.ofNullable(mapper.selectOneById(id)).map(converter::toModel);
    }

    @Override
    public List<{Entity}> findAll() {
        return mapper.selectAll().stream().map(converter::toModel).toList();
    }

    @Override
    public void save({Entity} entity) {
        mapper.insert(converter.toEntity(entity));
    }
}
```

### 8.5 App Service

```java
package io.github.loadup.modules.{mod}.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {Entity} 业务服务。
 *
 * <p>此 Bean 直接暴露给 LoadUp Gateway（bean://{entity}Service:method），无需 Controller 层。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class {Entity}Service {

    private final {Entity}Gateway gateway;

    /** 查询全部 */
    public List<{Entity}DTO> listAll() {
        return gateway.findAll().stream().map(this::toDTO).toList();
    }

    /** 创建（写操作加事务） */
    @Transactional(rollbackFor = Exception.class)
    public String create(@Valid {Entity}CreateCommand cmd) {
        // 1. 校验
        // 2. 构建 DO
        // 3. 持久化
        // 4. 返回 id
    }

    private {Entity}DTO toDTO({Entity}DO item) { ... }
}
```

### 8.6 AutoConfiguration

```java
package io.github.loadup.modules.{mod}.app.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "io.github.loadup.modules.{mod}")
@MapperScan("io.github.loadup.modules.{mod}.infrastructure.mapper")
public class {Mod}ModuleAutoConfiguration {}
```

在 `loadup-modules-{mod}-app/src/main/resources/META-INF/spring/` 创建：
`org.springframework.boot.autoconfigure.AutoConfiguration.imports`
```
io.github.loadup.modules.{mod}.app.autoconfigure.{Mod}ModuleAutoConfiguration
```

---

## 9. 测试规范

### 9.1 集成测试（首选，真实数据库）

```java
package io.github.loadup.modules.{mod}.service;

import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableTestContainers(ContainerType.MYSQL)
class {Entity}ServiceIT {

    @Autowired
    private {Entity}Service service;

    @BeforeEach
    void setUp() {
        // 清理测试数据，保证测试独立性
    }

    @Test
    void create_shouldPersist_whenValidCommand() {
        // given
        var cmd = new {Entity}CreateCommand();

        // when
        String id = service.create(cmd);

        // then
        assertThat(id).isNotBlank();
        assertThat(service.getById(id)).isNotNull();
    }
}
```

### 9.2 单元测试（纯逻辑，无 DB）

```java
@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {

    @Mock
    {Entity}Gateway gateway;

    @InjectMocks
    {Entity}Service service;

    @Test
    void create_shouldThrow_whenKeyExists() {
        when(gateway.existsByKey(any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(new {Entity}CreateCommand()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

**约定**：
- 集成测试类名：`XxxServiceIT.java`
- 单元测试类名：`XxxServiceTest.java`
- 测试方法命名：`methodName_shouldResult_whenCondition`
- 所有测试放在 `loadup-modules-xxx-test` 子模块
- test 模块的 parent 指向根 `loadup-parent` pom，不是模块自身 pom
- 覆盖率目标：核心 Service ≥ 80%

---

## 10. 安全规范

- 密码字段加 `@JsonIgnore`，DTO 层不返回
- 敏感信息（手机号/身份证/邮箱）使用 `commons` 工具脱敏后再输出日志或响应
- **禁止**字符串拼接 SQL，全部使用 MyBatis-Flex `QueryWrapper`
- **禁止**将 Token、密码写入日志
- 权限校验使用 `@RequirePermission("xxx:yyy")`（`loadup-components-authorization`）

---

## 11. 性能规范

- 读多写少的数据使用 Caffeine 本地缓存；写操作后主动 `evict`
- 批量写入使用 `mapper.insertBatch()` / `updateBatch()`，单批 ≤ 1000 条
- 避免 N+1 查询，使用 `in(ids)` 批量查询或 join
- 大结果集使用分页，单页上限 ≤ 200 条

---

## 12. 数据库规范

- 主键：`VARCHAR(64)`，业务层用 `UUID.randomUUID().toString().replace("-","")` 赋值
- 表名：`snake_case`（如 `config_item`、`dict_type`）
- 必备审计字段：`created_by VARCHAR(64)`, `created_at DATETIME`, `updated_by VARCHAR(64)`, `updated_at DATETIME`
- 软删除字段（按需）：`deleted BOOLEAN NOT NULL DEFAULT FALSE`
- 大表按月分区：`PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at))`
- Schema 脚本放模块根目录 `schema.sql`；Flyway migration 放 `src/main/resources/db/migration/V{n}__{desc}.sql`

---

## 13. 禁止项清单 🚫

| # | 禁止行为 | 原因 |
|---|---------|------|
| 1 | 生成 Java 文件头 License 注释块 | CI 插件统一插入，手动会重复 |
| 2 | 创建 `@RestController` / adapter 层 | Gateway bean 协议替代 |
| 3 | 在 Service / Repository 中使用 `@Autowired` 字段注入 | 用构造器注入（`@RequiredArgsConstructor`） |
| 4 | 字符串拼接 SQL | SQL 注入风险 |
| 5 | 集成测试中用 `@MockBean` 替代真实 DB | 测试失真，用 Testcontainers |
| 6 | modules 之间横向依赖 | 违反模块边界 |
| 7 | domain 层引入 Spring / ORM 框架注解（`@Service`、`@Table` 等） | 破坏领域纯洁性 |
| 8 | `SELECT *` 查询 | 性能浪费，字段不明确 |
| 9 | 日志打印密码/Token/敏感字段 | 安全合规 |
| 10 | 新增三方依赖不在 `loadup-dependencies` 声明 | 版本管理混乱 |
| 11 | pom.xml 的 `<parent>` 指向模块自身 pom | 所有子模块 parent 统一指向根 `loadup-parent` |
| 12 | DO 直接 `implements Serializable` 而不继承 `BaseDO` | 必须继承 `BaseDO`，使用 `@SuperBuilder` |
| 13 | DO 中重复定义 `id`/`createdAt`/`updatedAt` | 这些字段已在 `BaseDO` 中定义 |
| 14 | DO 放在 domain 层 | DO 放在 infrastructure 层 |
| 15 | 在 `XxxMapper` 中写额外 SQL 方法 | 用 `QueryWrapper` 在 GatewayImpl 中操作，Mapper 只继承 `BaseMapper<XxxDO>` |

### 13.1 pom.xml parent 规范

**所有子模块（包括 client / domain / infrastructure / app / test）的 `<parent>` 必须统一指向根 `loadup-parent`**，不得指向模块自身的聚合 pom：

```xml
<parent>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-parent</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <!-- relativePath 按子模块到根 pom.xml 的实际相对路径填写 -->
    <relativePath>../../../pom.xml</relativePath>  <!-- 三层深的子模块 -->
</parent>
```

相对路径规则：

| 子模块位置 | relativePath |
|-----------|--------------|
| `modules/loadup-modules-xxx/loadup-modules-xxx-client/` | `../../../pom.xml` |
| `modules/loadup-modules-xxx/loadup-modules-xxx-domain/` | `../../../pom.xml` |
| `modules/loadup-modules-xxx/loadup-modules-xxx-infrastructure/` | `../../../pom.xml` |
| `modules/loadup-modules-xxx/loadup-modules-xxx-app/` | `../../../pom.xml` |
| `modules/loadup-modules-xxx/loadup-modules-xxx-test/` | `../../../pom.xml` |
| `commons/loadup-commons-xxx/` | `../../pom.xml` |
| `components/loadup-components-xxx/` | `../../pom.xml` |
| `middleware/loadup-gateway/loadup-gateway-xxx/` | `../../../pom.xml` |

**模块聚合 pom**（如 `loadup-modules-xxx/pom.xml`）才指向其直接父层：

```xml
<!-- modules/loadup-modules-xxx/pom.xml -->
<parent>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-parent</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
```

---

### 13.2 loadup-dependencies 版本管理规范 🚫

**所有模块（含 commons / components / modules / middleware 下的所有子模块）的版本管理必须统一在 `loadup-dependencies/pom.xml` 的 `<dependencyManagement>` 中声明。**

#### 规则

1. **新建任何模块**，必须同步在 `loadup-dependencies/pom.xml` 的 `<dependencyManagement>` 中添加对应条目：

```xml
<!-- ========== loadup-modules-xxx start ==========-->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-xxx</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
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

2. **子模块 pom.xml 中引用同项目内其他模块时，不得写 `<version>`**，版本由 BOM 统一管理：

```xml
<!-- ✅ 正确：不写 version -->
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

3. **新增第三方依赖**也必须在 `loadup-dependencies/pom.xml` 中声明版本，子模块中不写 `<version>`。

---

## 14. 质量门

- `mvn clean verify` 通过（含所有测试）
- 核心 Service 覆盖率 ≥ 80%
- `mvn spotless:check` 格式化通过
- 无循环依赖（ArchUnit 检查）
- 无高危依赖漏洞（OWASP Dependency-Check）

---

遵循本指令生成符合 LoadUp 代码风格的代码。
模板中 `{Xxx}` / `{xxx}` / `{mod}` 为占位符，使用时替换为实际名称。
