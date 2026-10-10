# LoadUp Components Database

MyBatis-Flex integration for common persistent fields, audit timestamps, UUID identifiers, logical deletion, and tenant isolation.

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

This module brings the JDBC stack (`spring-boot-starter-jdbc`, so `DataSourceAutoConfiguration` and a
default Hikari pool); the application still supplies the JDBC driver and the `spring.datasource.*`
properties. This module does not select a database vendor.

Define each data object as `@Table(...) class XxxDO extends BaseDO`. `BaseDO` supplies `id`, `createdAt`, `updatedAt`, `tenantId`, and integer `deleted` (`0` normal, `1` deleted). The database processor generates `XxxDOMapper extends BaseMapper<XxxDO>` with MyBatis `@Mapper`, module-local `Tables` and TableDef sources. Do not declare these Mapper interfaces manually. Generated files remain under `target/`.

## Compile-time generation

Infrastructure modules enable the database-owned processor separately from the runtime dependency. The BOM manages its version. A provided dependency establishes Maven reactor ordering and keeps the processor out of deployment dependencies:

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database-processor</artifactId>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
```

Configure `maven-compiler-plugin` annotation processing (external consumers must also include MapStruct/Boot paths if used):

```xml
<configuration>
    <annotationProcessorPaths combine.children="append">
        <path>
            <groupId>com.mybatis-flex</groupId>
            <artifactId>mybatis-flex-processor</artifactId>
            <version>${mybatis.flex.version}</version>
        </path>
        <path>
            <groupId>io.github.loadup-cloud</groupId>
            <artifactId>loadup-components-database-processor</artifactId>
            <version>${loadup.framework.version}</version>
        </path>
    </annotationProcessorPaths>
    <annotationProcessors>
        <annotationProcessor>org.mapstruct.ap.MappingProcessor</annotationProcessor>
        <annotationProcessor>org.springframework.boot.configurationprocessor.ConfigurationMetadataAnnotationProcessor</annotationProcessor>
        <annotationProcessor>io.github.loadup.components.database.processor.LoadUpMyBatisFlexProcessor</annotationProcessor>
    </annotationProcessors>
</configuration>
```

External projects must define the example version properties to match the selected BOM; importing a BOM does not import its Maven properties or plugin configuration.

The explicit processor list avoids running upstream Flex a second time through service discovery. Both Flex paths are explicit so compilation works before the processor artifact is installed into the local repository. Build using `mvn clean ...` to avoid generating the same sources twice from stale output. Import `infrastructure.mapper.XxxDOMapper` and inject it into repositories; scan the generated mapper package with `@MapperScan`. MapStruct remains an independent processor for Spring-managed converters.

Fixed options live in `DatabaseAptConfiguration` Java code. No project `mybatis-flex.config` or application property is needed; ancestor config files cannot change these options. The generated config in compiler output is an upstream implementation bridge, not an application configuration file.

## Configuration

```yaml
loadup:
  database:
    multi-tenant:
      enabled: true
      required: true
      default-tenant-id: __default__ # used when enabled=false
      ignore-tables: [sys_tenant, sys_config]
      request: {header-name: X-Tenant-Id, parameter-name: tenantId}
```

When `enabled=false` (the default), the request filter binds `default-tenant-id` for every HTTP request and the database insert listener writes the same ID into `BaseDO.tenantId`, ignoring request-supplied tenant values. The default ID is `__default__` and can be overridden. When `enabled=true`, the filter uses the configured request source; missing tenant context is rejected when `required=true` rather than falling back to the single-tenant ID.

Use `TenantUtil.runWithTenant(...)` or `callWithTenant(...)` for non-HTTP jobs that need a tenant context. Tenant bindings are read-only within a JDK 25 ScopedValue scope; there is no set/clear API. The request filter saves immutable metadata under `ExecutionContext.class.getName()` and binds it around REQUEST/ASYNC/ERROR dispatch. Header/parameter/subdomain extraction remains a configurable convenience; authenticated tenant authorization must be enforced by the integrating application.

## Capability matrix

| Capability | MyBatis-Flex |
|---|---|
| CRUD, QueryWrapper, generated MyBatis Mapper, Tables and TableDef | ✓ |
| Audit timestamps and ID generation | ✓ |
| Integer logical deletion | ✓ |
| Tenant SQL isolation and request propagation | ✓ |

## Fixed framework conventions

UUID v4 IDs, UTC audit timestamps, `deleted = 0/1` and the `tenant_id` column are framework-owned defaults. They cannot be switched off or renamed through application properties. Extra `MyBatisFlexCustomizer` beans fail startup. DataSource settings, tenant activation and trusted tenant resolution remain deployment concerns. All mapped business tables must contain the five BaseDO fields; apply the module migrations before enabling the adapters.

Use generated `Tables` constants in repositories; do not concatenate SQL or sort expressions. MapStruct converters use the shared Spring configuration and constructor injection. `UpdateEntity.of` expresses partial updates, including intentional null assignments.
