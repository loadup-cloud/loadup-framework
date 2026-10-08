# Loadup Common Utils

提供 JSON、日期、字符串等通用工具。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-util</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

## 租户上下文

`TenantUtil` 复用 [commons-context](../loadup-commons-context/README.md) 的只读 TENANT_ID，基于 JDK 25 ScopedValue。使用 `runWithTenant` / `callWithTenant` 临时绑定租户并继承其他业务键，返回或抛异常后自动恢复。移除 setTenantId/clear；任务入口使用 ContextHolder.runWith/callWith 绑定不可变 ExecutionContext。
