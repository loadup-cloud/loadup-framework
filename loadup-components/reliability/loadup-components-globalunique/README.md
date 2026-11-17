# LoadUp Components Global Unique

基于 PostgreSQL 唯一索引的租户级幂等声明组件；单一 jar 随应用部署，业务侧只注入 `GlobalUniqueTemplate`。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-globalunique</artifactId>
</dependency>
```

## 使用

```java
@Transactional
public void createOrder(OrderCreateCommand command) {
    var claim = new GlobalUniqueClaim("ORDER_CREATE", command.orderNo(), command.orderNo(), null);
    if (!globalUniqueTemplate.claim(claim)) {
        return;
    }
    // Business writes must use the same transaction.
}
```

## 配置

```yaml
loadup:
  global-unique:
    enabled: true
  database:
    schema: loadup_payment  # fixed business schema
      enabled: false
  flyway:
    locations: classpath:db/migration
```

API 在本部署范围内声明幂等键；数据库唯一维度为 `biz_type + unique_key`，不含 `tenant_id`。一套部署只服务一个租户并固定使用
`loadup.database.schema`，因此幂等记录天然只属于该部署。组件显式设置
ID、审计时间和删除标记。数据库隔离现状见 [database README](../../data/loadup-components-database/README.md)。

## 能力矩阵

| 能力                           | 支持                     |
|--------------------------------|--------------------------|
| 当前 schema 内并发幂等         | ✓ PostgreSQL 唯一索引   |
| 业务元数据、请求快照、记录查询 | ✓                       |
| 事务回滚后重试                 | ✓ 同一 Spring 事务      |
| ID、审计、逻辑删除             | ✓ database 组件统一提供 |
| 过期、自动归档、缓存           | ✗ 由业务维护            |
