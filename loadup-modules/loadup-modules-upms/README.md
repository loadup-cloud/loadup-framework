# UPMS

用户、角色、权限和部门业务模块。UPMS 校验凭证，提供角色继承的 RBAC 授权及基于资源所属人、部门的数据范围判定；令牌签发由 AuthServer 完成。

账号安全自助接口由 `loadup-modules-upms-web` 提供：`GET /api/account/security/overview` 查看本人状态，`GET /api/account/security/logins` 查看本人登录记录，`POST /api/account/security/password` 使用旧密码修改本人密码。用户 ID 从认证主体取得，不接受请求指定其他账号。新密码至少 8 字符、最多 72 个 UTF-8 字节且必须不同于旧密码。管理员的锁定与解锁接口仍在 `/api/upms/user/**`。

登录失败达到配置阈值后临时锁定；到期后的下一次登录会自动解锁。管理员手动锁定不会自动解锁。HTTP 登录记录使用服务端观察到的 IP，不信任客户端传入的转发头。当前 JWT 为无状态令牌，修改密码或锁定账号不会立即撤销已签发令牌；已有令牌可用至其到期，后续撤销机制见 Roadmap。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-app</artifactId>
</dependency>
```

前后端分离的用户名密码登录需引入 `loadup-modules-upms-authserver` 与 `loadup-components-authserver-binder-sas`，通过 `POST /api/auth/login` 获取 JWT。可设置 `loadup.security.auth-server.protocol-endpoints-enabled: false` 关闭 OAuth2 协议端点。

## 配置

```yaml
loadup:
  modules:
    upms:
      security:
        login:
          enable-failure-tracking: true
          max-fail-attempts: 5
          lock-duration: 30
```

`AuthenticationService.login` 是内部身份校验接口；HTTP 登录由 authserver 适配器完成。需要预置用户注册与 RBAC HTTP 接口时，再引入 `loadup-modules-upms-web`；也可以直接在应用 Controller 中调用 UPMS 服务。

业务代码注入 `AccessCheckService`，调用 `check(new AccessCheckCommand(userId, permissionCode, ownerUserId, departmentId))`。结果默认拒绝；仅当有效角色授予该权限，且该角色的数据范围匹配资源属性时放行。`@PreAuthorize` 可校验静态权限，资源级 ABAC 判定须显式调用此 API。

| 能力 | 所属模块 |
|------|----------|
| 用户凭证校验与登录策略 | `loadup-modules-upms-app` |
| 角色与权限数据 | `loadup-modules-upms-domain`、`-infrastructure` |
| RBAC + 资源属性判定 API | `loadup-modules-upms-client`、`-app` |
| 可选 HTTP Controller | `loadup-modules-upms-web` |
| SAS 用户认证适配 | `loadup-modules-upms-authserver` |
| 令牌签发与刷新 | `loadup-components-authserver-binder-sas` |
| Bearer 验签 | `loadup-components-resource-server` |
| 方法级授权 | `loadup-components-authorization` |

## 敏感数据展示与明文读取

普通 `UserDetailDTO` 的姓名、邮箱、手机号在 WebMVC JSON 输出中固定脱敏；内部对象与数据库保留原值。管理员也遵循同一展示规则。用户修改时省略敏感字段表示不变，显式空字符串表示清空；包含 `*` 的姓名、邮箱或手机号不能写入，避免把掩码保存为原数据。管理端编辑界面将这些字段留空，填写新值才更新。

`POST /api/upms/user/sensitive` 是独立明文读取入口，要求 JWT 静态 authority `upms:user:sensitive:read`，并重新执行有效角色权限与目标用户数据范围的 RBAC/ABAC 校验。请求示例：

```json
{"id":"target-user-id","purpose":"CUSTOMER_SUPPORT"}
```

purpose 只允许 `PROFILE_CORRECTION`、`CUSTOMER_SUPPORT`、`SECURITY_REVIEW`。接口没有超级管理员绕过规则；需要显式配置权限及相应角色数据范围，不自动授予现有账号。返回 `result/data`，data 只含 id、realName、email、mobile；响应为 `Cache-Control: no-store`。

明文返回前必须有 `SensitiveReadAudit` 同步持久化访问记录。消费工程显式引入 `loadup-modules-audit` 后，Web 适配器提供默认桥接，以独立事务记录 actor、目标 ID、用途、tenant 与 traceId，不记录字段原文。缺失审计组件或写入/提交失败则拒绝明文；也可提供自己的可靠 recorder。

## 业务配置命名空间

模块专属配置统一使用 `loadup.modules.upms.*`，配置中心与消费工程需同步迁移旧前缀；登录策略为 `loadup.modules.upms.security.login`，OAuth 为 `loadup.modules.upms.security.oauth`。认证协议与令牌校验继续使用全局 `loadup.security.*`。

## 统一接入契约

Java 消费方通过 `client.facade.XxxFacade` 注入公开业务入口；默认应用 Service 直接实现接口。引入 `*-app` 装配业务能力，引入 `*-web` 才提供 Controller，Web 适配不再提供独立 enabled 开关。模块整体启停仍使用 `loadup.modules.upms.enabled`。

JSON Controller 显式返回 SuccessResponse，分页保留已有分页报文契约；异常由全局 WebMVC 处理。下载仍为流式响应。请求与 DTO 字段声明 OpenAPI，凭证只写。持久化经 database 组件使用 MyBatis-Flex、Tables 常量和 Spring MapStruct Converter；数据库连接与可信租户来源由消费工程配置。新 schema 迁移与本轮 clean 编译、运行验证仍需本地执行。
