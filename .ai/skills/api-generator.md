# Skill: API Generator — LoadUp COLA 4.0 全层代码生成规范

> 本技能覆盖从 client 到 app 的全层代码模板，以及如何通过 LoadUp Gateway 暴露接口。
> 所有模板中 `{mod}` / `{Mod}` / `{Entity}` / `{entity}` 为占位符，使用时替换为实际名称。

---

## 1. 业务模块分层结构（COLA 4.0）

```
loadup-modules-{mod}/
├── loadup-modules-{mod}-client/          # 对外 DTO + Command + Query（可被其他模块依赖）
├── loadup-modules-{mod}-domain/          # 纯 DDD 模型（POJO）+ Gateway 接口 + 枚举（零框架注解）
├── loadup-modules-{mod}-infrastructure/  # DO (extends BaseDO) + Mapper + GatewayImpl + Converter
├── loadup-modules-{mod}-app/             # @Service 业务编排 + AutoConfiguration
└── loadup-modules-{mod}-test/            # 集成测试 + 单元测试
```

**各层铁律**：

| 层 | 放什么 | 禁止放什么 |
|----|--------|-----------|
| client | DTO、Command、Query | 业务逻辑、DB 注解 |
| domain | POJO 模型、Gateway 接口、枚举 | `@Table`、`@Service`、任何框架注解 |
| infrastructure | `XxxDO extends BaseDO`、Mapper、GatewayImpl、Converter | 业务逻辑 |
| app | `@Service` 业务编排、AutoConfiguration | 直接操作 DB |

---

## 2. 包命名规范（根包：`io.github.loadup.modules.{mod}`）

| 层 | 包路径 |
|----|--------|
| client DTO | `.client.dto` |
| client Command | `.client.command` |
| client Query | `.client.query` |
| domain model | `.domain.model` |
| domain gateway 接口 | `.domain.gateway` |
| domain 枚举 | `.domain.enums` |
| infra DO | `.infrastructure.dataobject` |
| infra Mapper | `.infrastructure.mapper` |
| infra GatewayImpl | `.infrastructure.repository` |
| infra Converter | `.infrastructure.converter` |
| app Service | `.app.service` |
| app AutoConfig | `.app.autoconfigure` |

---

## 3. 命名约定

| 类型 | 规则 | 示例 |
|------|------|------|
| 数据库映射对象 | `XxxDO extends BaseDO` | `ConfigItemDO` |
| 对外 DTO | `XxxDTO` | `ConfigItemDTO` |
| 写操作入参（创建） | `XxxCreateCommand` | `ConfigItemCreateCommand` |
| 写操作入参（更新） | `XxxUpdateCommand` | `ConfigItemUpdateCommand` |
| 查询入参 | `XxxQuery` | `ConfigItemQuery` |
| Gateway 接口 | `XxxGateway` | `ConfigItemGateway` |
| Gateway 实现 | `XxxGatewayImpl` | `ConfigItemGatewayImpl` |
| Service | `XxxService`（直接 `@Service`，无 impl） | `ConfigItemService` |
| AutoConfig | `XxxModuleAutoConfiguration` | `ConfigModuleAutoConfiguration` |

---

## 4. 代码模板

### 4.1 DTO

```java
package io.github.loadup.modules.{mod}.client.dto;

import lombok.Data;

@Data
public class {Entity}DTO {
    private String id;
    // 业务字段
}
```

### 4.2 CreateCommand

```java
package io.github.loadup.modules.{mod}.client.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class {Entity}CreateCommand {
    @NotBlank
    private String name;
    // 业务字段
}
```

### 4.3 Domain Model（纯 POJO，零框架注解）

```java
package io.github.loadup.modules.{mod}.domain.model;

import lombok.Data;

@Data
public class {Entity} {
    private String id;
    // 业务字段（无 @Table、无 @Service、无任何 Spring/ORM 注解）
}
```

### 4.4 Gateway 接口

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

### 4.5 DO 实体（必须在 infrastructure.dataobject 包）

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
    // 业务字段
    // ⚠️ 禁止重复声明 id / createdAt / updatedAt / tenantId / deleted（已在 BaseDO 中）
    private String createdBy;
    private String updatedBy;
}
```

### 4.6 Mapper

```java
package io.github.loadup.modules.{mod}.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.modules.{mod}.infrastructure.dataobject.{Entity}DO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {Entity}DOMapper extends BaseMapper<{Entity}DO> {
    // 不写任何额外方法，用 QueryWrapper 在 GatewayImpl 中操作
}
```

### 4.7 MapStruct Converter

```java
package io.github.loadup.modules.{mod}.infrastructure.converter;

import io.github.loadup.modules.{mod}.domain.model.{Entity};
import io.github.loadup.modules.{mod}.infrastructure.dataobject.{Entity}DO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface {Entity}Converter {
    {Entity} toModel({Entity}DO entity);
    {Entity}DO toEntity({Entity} model);
}
```

### 4.8 GatewayImpl（存储实现）

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

    @Override
    public void update({Entity} entity) {
        mapper.update(converter.toEntity(entity));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return mapper.selectCountByCondition(
            {ENTITY}_DO.ID.eq(id)) > 0;
    }
}
```

### 4.9 App Service

```java
package io.github.loadup.modules.{mod}.app.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {Entity} 业务服务。
 * 此 Bean 通过 LoadUp Gateway 以 bean://{entity}Service:method 协议对外暴露，无需 Controller 层。
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

    /** 根据 ID 获取 */
    public {Entity}DTO getById(String id) {
        return gateway.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new IllegalArgumentException("Not found: " + id));
    }

    /** 创建（写操作加事务） */
    @Transactional(rollbackFor = Exception.class)
    public String create(@Valid {Entity}CreateCommand cmd) {
        String id = UUID.randomUUID().toString().replace("-", "");
        // TODO: 构建 domain model，调用 gateway.save()
        return id;
    }

    private {Entity}DTO toDTO({Entity} model) {
        // TODO: 使用 MapStruct 或手动映射
        return new {Entity}DTO();
    }
}
```

### 4.10 AutoConfiguration

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

## 5. Gateway 集成方式（暴露接口，无 Controller）

LoadUp Gateway 支持两种路由存储方式——**FILE（CSV）** 和 **DATABASE**，通过 `storage.type` 配置切换。
不需要 `@RestController`，Gateway 以 `bean://beanName:methodName` 协议直接调用 Spring Bean。

### 5.1 基础配置（application.yml）

```yaml
loadup:
  gateway:
    enabled: true
    storage:
      type: FILE          # FILE | DATABASE（config center 规划中，暂未实现）
    security:
      secret: "your-jwt-secret-key"
```

### 5.2 FILE 存储（开发/小规模部署，默认）

在 `src/main/resources/gateway-config/routes.csv` 追加一行：

```
path,method,target,securityCode,requestTemplate,responseTemplate,enabled,properties
/api/v1/config/list,POST,bean://configItemService:listAll,default,,,true,
/api/v1/config/create,POST,bean://configItemService:create,default,,,true,
/api/v1/config/value,POST,bean://configItemService:getValue,OFF,,,true,
```

**CSV 字段说明（8 列，顺序固定）**：

| 列 | 字段 | 说明 |
|----|------|------|
| 1 | `path` | 路径，支持 `**` 通配 |
| 2 | `method` | HTTP 方法 |
| 3 | `target` | 目标，见 §5.4 |
| 4 | `securityCode` | 认证策略，见 §5.5 |
| 5 | `requestTemplate` | 请求模板 ID（可为空） |
| 6 | `responseTemplate` | 响应模板 ID（可为空） |
| 7 | `enabled` | `true` / `false` |
| 8 | `properties` | 附加属性，格式 `key=val;key2=val2`（可为空） |

### 5.3 DATABASE 存储（生产推荐）

```yaml
loadup:
  gateway:
    storage:
      type: DATABASE
```

向 `gateway_routes` 表插入路由记录：

```sql
INSERT INTO gateway_routes (route_id, route_name, path, method, target, security_code, enabled)
VALUES ('config-list', '配置列表', '/api/v1/config/list', 'POST',
        'bean://configItemService:listAll', 'default', 1);
```

`gateway_routes` 表字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `route_id` | VARCHAR PK | 路由唯一标识 |
| `route_name` | VARCHAR | 路由名称 |
| `path` | VARCHAR | 路径 |
| `method` | VARCHAR | HTTP 方法 |
| `target` | VARCHAR | 目标（见 §5.4） |
| `security_code` | VARCHAR(32) | 认证策略（见 §5.5） |
| `request_template` | TEXT | 请求模板 ID（可为 NULL） |
| `response_template` | TEXT | 响应模板 ID（可为 NULL） |
| `enabled` | TINYINT | 1=启用，0=禁用 |
| `properties` | TEXT | 附加属性 JSON 或 `k=v;` 格式 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 更新时间 |

### 5.4 target 协议格式

| 协议 | 格式 | 说明 |
|------|------|------|
| **BEAN** | `bean://beanName:methodName` | 调用 Spring Bean 方法（主要方式） |
| HTTP | `http://host:port/path` | HTTP 反向代理转发 |
| RPC | `rpc://interfaceName:method:version` | Dubbo RPC（按需启用） |

### 5.5 securityCode 说明

| 值 | 含义 |
|----|------|
| `OFF` | 关闭所有安全校验（公开接口） |
| `default` | JWT Bearer Token 验证（用户接口） |
| `signature` | HMAC-SHA256 签名验签（Open API，需 `X-App-Id`/`X-Timestamp`/`X-Nonce`/`X-Signature` 头） |
| `internal` | 内部调用（IP 白名单：127.x / 10.x / 172.16-31.x / 192.168.x，或 `X-Internal-Call: true`） |
| 自定义 | 实现 `SecurityStrategy` SPI 并注册为 `@Component` |

---

## 6. pom.xml 规范

### 6.1 所有子模块 parent 统一指向根 loadup-parent

```xml
<parent>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-parent</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <relativePath>../../../pom.xml</relativePath>  <!-- 三层深的子模块 -->
</parent>
```

| 子模块位置 | relativePath |
|-----------|--------------|
| `modules/loadup-modules-xxx/loadup-modules-xxx-{layer}/` | `../../../pom.xml` |
| `commons/loadup-commons-xxx/` | `../../pom.xml` |
| `components/loadup-components-xxx/` | `../../pom.xml` |

### 6.2 新建模块必须在 loadup-dependencies BOM 中声明

```xml
<!-- loadup-dependencies/pom.xml <dependencyManagement> 中添加 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-{mod}-client</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<!-- ... domain / infrastructure / app / test 同理 -->
```

子模块中引用同项目模块**不写 `<version>`**：

```xml
<!-- ✅ 正确 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-{mod}-domain</artifactId>
</dependency>
```

---

## 7. 禁止项速查（最高优先级）

| # | 禁止行为 | 正确做法 |
|---|---------|---------|
| 1 | 创建 `@RestController` / `@Controller` | Gateway CSV/DB 路由替代 |
| 2 | `@Autowired` 字段注入 | `@RequiredArgsConstructor` + `final` 字段 |
| 3 | `@Table` 放在 domain 层 | 只放 `infrastructure.dataobject` |
| 4 | DO 重复声明 `id`/`createdAt`/`updatedAt` | 这些字段已在 `BaseDO` 中 |
| 5 | Mapper 中写额外 SQL 方法 | QueryWrapper 在 GatewayImpl 中操作 |
| 6 | 字符串拼接 SQL | 使用 `QueryWrapper` |
| 7 | 表主键 `BIGINT AUTO_INCREMENT` | `VARCHAR(64)` + `UUID.randomUUID()` |
| 8 | 子模块 `<parent>` 指向模块自身 pom | 统一指向根 `loadup-parent` |
| 9 | 子模块写 `<version>` 引用同项目模块 | 版本由 BOM 统一管理 |
| 10 | modules 横向互相依赖 | 严格单向：commons→components→modules→application |
| 11 | Java 文件头写 License 块 | CI `license-maven-plugin` 自动插入 |
