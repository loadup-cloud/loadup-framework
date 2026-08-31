# LoadUp Components :: Global Unique

基于**数据库唯一键约束**的全局幂等控制组件：在同一事务内 `INSERT` 幂等记录，
唯一键冲突即视为重复请求。无标准 OSS 直接对应，属于框架自研能力（DESIGN §5.13）。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-globalunique</artifactId>
</dependency>
```

## 使用

在业务事务内调用，返回 `true` 表示首次执行（继续业务），`false` 表示幂等拦截：

```java
@Transactional
public void createOrder(OrderCreateCommand cmd) {
    String key = "ORDER_CREATE:" + cmd.userId() + ":" + cmd.orderNo();
    if (!globalUniqueService.insertAndCheck(key, "ORDER", cmd.orderNo(), JsonUtil.toJson(cmd))) {
        return; // duplicate request
    }
    // business logic
}
```

## 配置

```yaml
loadup:
  components:
    globalunique:
      enabled: true          # 总开关，默认 true
      db-type: MYSQL         # MYSQL / POSTGRESQL / ORACLE
      table-prefix: ""       # 可选表名前缀
      table-name: global_unique
```

表结构由组件内 Flyway 迁移自动维护（MySQL / PostgreSQL / Oracle 三套脚本），
包含标准字段：`id` / `tenant_id` / `created_at` / `updated_at` / `deleted`。

## 能力矩阵

| 能力 | 支持 |
|------|------|
| 事务内幂等（唯一键 INSERT） | ✓ |
| 业务类型 / 业务 ID / 请求快照 | ✓ |
| MySQL / PostgreSQL / Oracle | ✓ |
| 并发安全（数据库唯一索引兜底） | ✓ |
| 失败回滚后可重试（随事务回滚） | ✓ |
| 记录过期 / 自动归档 | ✗（业务侧按需归档） |
| 独立缓存层 | ✗（保持数据库强一致） |

## 行为语义

- 幂等记录与业务在同一事务：业务回滚则幂等记录一并回滚，请求可重试；
  业务提交则记录持久化，后续重复请求被拦截。
- 唯一键由业务方自定义拼接，建议格式 `业务类型:维度1:维度2`，总长 ≤ 255。
- 组件只做密码学/存储原语之上的薄封装，不做状态机、不引入额外中间件。

## 许可证

Apache License 2.0 (Apache-2.0)
