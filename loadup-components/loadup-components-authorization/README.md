# loadup-components-authorization

统一授权组件：**facade = Spring Security 标准 API**，LoadUp 只做薄装配与上下文适配。

## 设计定位

- 方法级授权：`@PreAuthorize` / `@Secured`（`@EnableMethodSecurity` 自动开启）
- 当前用户：`UserContext` 是 `SecurityContextHolder` 的薄适配器，`set(LoadUpUser)` 写入
  `Authentication`，`get/getUserId/getUsername/clear` 从 Spring Security 上下文读取
- 权限映射：roles → `ROLE_<role>` + 原始值 authority；permissions → 原始值 authority，
  因此 `hasRole('ADMIN')` 与 `hasAuthority('user:read')` 均可直接使用
- 默认 `SecurityFilterChain`：无状态 + 关闭 CSRF + 全放行（拦截逻辑在业务方法注解上），
  应用可自定义 `SecurityFilterChain` 覆盖

## 快速开始

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-authorization</artifactId>
</dependency>
```

```yaml
loadup:
  authorization:
    enabled: true                       # 总开关，默认 true
    default-security-filter-chain: true # 注册默认放行链，默认 true
```

```java
@Service
public class OrderService {

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(String orderId) {
        // ...
    }

    @PreAuthorize("hasAuthority('order:create')")
    public void createOrder(OrderCreateCommand command) {
        // ...
    }
}
```

Gateway `bean://` 路由调用前通过 `UserContext.set(LoadUpUser)` 写入上下文，业务代码用
`UserContext.getUserId()` / `getUsername()` 读取当前用户。

## 能力矩阵

| 能力 | 说明 |
|------|------|
| 方法级 RBAC | `@PreAuthorize`（角色 / 权限 / SpEL） |
| 当前用户上下文 | `UserContext`（SecurityContextHolder 适配） |
| 请求级过滤链 | 默认放行链，可被应用覆盖 |
| 异步上下文传递 | 遵循 `SecurityContextHolder` 策略（如 `INHERITABLETHREADLOCAL`） |

## 许可证

Apache-2.0 — 详见项目根目录 [LICENSE](../../LICENSE)。
