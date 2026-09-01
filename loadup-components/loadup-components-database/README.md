# LoadUp Components Database

MyBatis-Flex integration for common persistent fields, audit timestamps, configurable IDs, logical deletion, and tenant isolation.

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

The application supplies its JDBC driver and datasource; this module does not select a database vendor.

## Entity and mapper

```java
@Table("app_user")
public class UserDO extends BaseDO {
    private String username;
}

@Mapper
public interface UserDOMapper extends BaseMapper<UserDO> {}
```

`BaseDO` supplies `id`, `createdAt`, `updatedAt`, `tenantId`, and integer `deleted` (`0` normal, `1` deleted). TableDef and mapper sources are generated from the root `mybatis-flex.config`.

## Configuration

```yaml
loadup:
  database:
    audit:
      enabled: true
    id-generator:
      strategy: uuid-v7 # random | uuid-v4 | uuid-v7 | snowflake
      random-length: 20
      uuid-with-hyphens: false
      snowflake-worker-id: 0
      snowflake-datacenter-id: 0
    logical-delete: {enabled: true, column-name: deleted, normal-value: 0, deleted-value: 1}
    multi-tenant:
      enabled: true
      required: true
      ignore-tables: [sys_tenant, sys_config]
      request: {header-name: X-Tenant-Id, parameter-name: tenantId}
```

Use `TenantContextHolder.setTenantId(...)` for non-HTTP jobs. `runWithTenant` scopes nested work and restores the previous context.

## Capability matrix

| Capability | MyBatis-Flex |
|---|---|
| CRUD, QueryWrapper, generated TableDef and mapper | ✓ |
| Audit timestamps and ID generation | ✓ |
| Integer logical deletion | ✓ |
| Tenant SQL isolation and request propagation | ✓ |
