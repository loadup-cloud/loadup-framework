# Gateway Security Implementation

## 概述

Gateway 负责**认证（Authentication）**，通过 `SecurityStrategy` SPI 支持多种认证方式。

## 架构设计

```
Client Request
    ↓
DispatcherServlet
    ↓
GatewayHandlerAdapter
    ↓
ActionChain
    ├─ RouteAction (路由匹配)
    ├─ SecurityAction (认证) ← 根据 RouteConfig.securityCode 选择策略
    └─ ProxyAction (转发)
```

## SecurityStrategy SPI

### 接口定义

```java
public interface SecurityStrategy {
    String getCode();              // 策略唯一标识
    void process(GatewayContext context); // 执行认证逻辑
}
```

### 内置策略

| Code | 策略名称 | 说明 |
|------|---------|------|
| `OFF` | 无认证 | 跳过认证检查 |
| `default` | JWT认证 | 验证 JWT Token 并填充 SecurityContext |
| `signature` | 签名验签 | HMAC-SHA256 签名验证 |
| `internal` | 内部调用 | 基于 IP 白名单或内部标识 |

## 策略详解

### 1. OFF 策略

**使用场景**：公开接口（登录、注册、健康检查等）

**配置示例**：
```yaml
routes:
  - routeId: "login"
    path: "/api/v1/auth/login"
    securityCode: "OFF"  # 或省略
```

### 2. Default (JWT) 策略

**使用场景**：标准用户认证

**请求示例**：
```http
GET /api/v1/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**JWT Claims 要求**：
```json
{
  "sub": "user123",           // 用户ID
  "username": "admin",         // 用户名
  "roles": ["ADMIN", "USER"]   // 角色列表（数组或逗号分隔字符串）
}
```

**处理流程**：
1. 从 `Authorization` header 提取 Token
2. 验证 Token 签名和过期时间
3. 提取用户信息（userId, username, roles）
4. 填充到：
   - Request Headers (`X-User-Id`, `X-User-Name`, `X-User-Roles`)
   - Request Attributes (`userId`, `username`, `roles`, `claims`)
   - SecurityContext (供 `@PreAuthorize` 使用)

**配置示例**：
```yaml
routes:
  - routeId: "user-api"
    path: "/api/v1/users/**"
    securityCode: "default"
    
loadup:
  gateway:
    security:
      header: "Authorization"
      prefix: "Bearer "
      secret: "your-jwt-secret-key"
```

### 3. Signature (签名) 策略

**使用场景**：Open API、第三方集成、防篡改

**请求示例**：
```http
GET /api/v1/orders?orderId=12345&status=paid
X-App-Id: test-app-001
X-Timestamp: 1675247890
X-Nonce: abc123xyz
X-Signature: 3f8c2a1b4e5d6f7a8b9c0d1e2f3a4b5c...
```

**签名计算**：
```java
// 1. 排序查询参数
String signStr = "orderId=12345&status=paid&timestamp=1675247890&nonce=abc123xyz";

// 2. HMAC-SHA256
String signature = HMAC_SHA256(signStr, appSecret);
```

**验证流程**：
1. 检查时间戳（防重放攻击，默认 5 分钟有效期）
2. 根据 `X-App-Id` 查找对应的 Secret
3. 计算服务器端签名
4. 比较签名是否一致

**配置示例**：
```yaml
routes:
  - routeId: "open-api"
    path: "/open-api/**"
    securityCode: "signature"

# TODO: App secrets 应存储在数据库中
# 当前硬编码在 SignatureSecurityStrategy 中
```

### 4. Internal (内部) 策略

**使用场景**：服务间调用、内网 API

**验证方式**：
1. **Header 标识**：`X-Internal-Call: true`
2. **IP 白名单**：
   - `127.0.0.1` (localhost)
   - `10.*` (私有网段 Class A)
   - `172.16.*` ~ `172.31.*` (私有网段 Class B)
   - `192.168.*` (私有网段 Class C)

**请求示例**：
```http
POST /api/internal/cache/clear
X-Internal-Call: true
```

**配置示例**：
```yaml
routes:
  - routeId: "internal-api"
    path: "/api/internal/**"
    securityCode: "internal"
```

## SecurityContext 填充

**关键代码**（DefaultSecurityStrategy）：

```java
private void populateSecurityContext(String userId, String username, List<String> roles) {
    try {
        // 动态加载 LoadUpUser（避免强依赖 loadup-components-security）
        Class<?> userClass = Class.forName("io.github.loadup.components.security.core.LoadUpUser");
        Object user = userClass.getDeclaredConstructor().newInstance();
        
        // 反射设置字段
        userClass.getMethod("setUserId", String.class).invoke(user, userId);
        userClass.getMethod("setUsername", String.class).invoke(user, username);
        userClass.getMethod("setRoles", List.class).invoke(user, roles);

        // 填充 SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user, null, 
            ((UserDetails) user).getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
    } catch (ClassNotFoundException e) {
        log.debug("loadup-components-security not in classpath");
    }
}
```

**为什么使用反射？**
- Gateway 不强依赖 `loadup-components-security`
- 如果应用引入了 security 组件，自动填充 SecurityContext
- 如果未引入，不影响 Gateway 正常工作

## 自定义策略

### 1. 实现接口

```java
@Component
public class MyCustomStrategy implements SecurityStrategy {
    
    @Override
    public String getCode() {
        return "my-custom";
    }
    
    @Override
    public void process(GatewayContext context) {
        // 自定义认证逻辑
        String token = context.getRequest().getHeaders().get("X-Custom-Token");
        if (!validateToken(token)) {
            throw GatewayExceptionFactory.unauthorized("Invalid token");
        }
        // 填充用户信息...
    }
}
```

### 2. 配置路由

```yaml
routes:
  - routeId: "custom-api"
    path: "/custom/**"
    securityCode: "my-custom"
```

### 3. 自动注册

`SecurityStrategyManager` 会自动扫描所有 `SecurityStrategy` Bean 并注册。

## 配置参考

### RouteConfig.securityCode

| 值 | 行为 |
|----|------|
| `null` 或 `""` | 等同于 `OFF`，跳过认证 |
| `OFF` | 显式跳过认证 |
| `default` | JWT 认证 |
| `signature` | 签名验签 |
| `internal` | 内部调用验证 |
| 其他 | 自定义策略的 code |

### 错误处理

| 异常类型 | HTTP 状态码 | 说明 |
|---------|------------|------|
| `GatewayExceptionFactory.unauthorized()` | 401 | 认证失败（Token 无效、过期等） |
| `GatewayExceptionFactory.forbidden()` | 403 | 权限不足（IP 不在白名单等） |
| `GatewayExceptionFactory.systemError()` | 500 | 系统错误（签名计算失败等） |

## 与 loadup-components-security 协作

```
┌────────────────────────────────────────┐
│  Gateway (loadup-gateway-core)         │
│  ├─ SecurityAction                     │
│  ├─ SecurityStrategy                   │
│  └─ 填充 SecurityContext               │
└────────────────┬───────────────────────┘
                 │ 认证 (Authentication)
                 ↓
┌────────────────────────────────────────┐
│  Security 组件                          │
│  (loadup-components-security)          │
│  └─ @PreAuthorize 检查角色             │
└────────────────┬───────────────────────┘
                 │ 授权 (Authorization)
                 ↓
┌────────────────────────────────────────┐
│  业务逻辑 (UPMS Service)                │
└────────────────────────────────────────┘
```

## 最佳实践

1. **公开接口使用 `OFF`**
   ```yaml
   /api/v1/auth/** → OFF
   /actuator/** → OFF
   ```

2. **用户接口使用 `default`**
   ```yaml
   /api/v1/users/** → default
   /api/v1/orders/** → default
   ```

3. **Open API 使用 `signature`**
   ```yaml
   /open-api/** → signature
   ```

4. **内部接口使用 `internal`**
   ```yaml
   /api/internal/** → internal
   ```

5. **安全配置**
   - JWT Secret 使用强密钥（至少 256 位）
   - 定期轮换密钥
   - Signature App Secrets 存储在数据库，支持动态更新
   - 内网 API 额外配置 IP 白名单或使用 VPN

## 测试

### JWT 认证测试

```bash
# 1. 登录获取 Token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.data.token')

# 2. 使用 Token 访问受保护接口
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/users/profile
```

### 签名认证测试

```bash
# 计算签名（示例脚本）
TIMESTAMP=$(date +%s)
NONCE=$(uuidgen)
SIGN_STR="orderId=12345&status=paid&timestamp=$TIMESTAMP&nonce=$NONCE"
SIGNATURE=$(echo -n "$SIGN_STR" | openssl dgst -sha256 -hmac "test-secret-key-001" | cut -d' ' -f2)

# 发送请求
curl "http://localhost:8080/api/v1/orders?orderId=12345&status=paid" \
  -H "X-App-Id: test-app-001" \
  -H "X-Timestamp: $TIMESTAMP" \
  -H "X-Nonce: $NONCE" \
  -H "X-Signature: $SIGNATURE"
```

## FAQ

### Q: 为什么不在 Filter 中做认证？
**A**: Gateway 的路由是动态的，需要先匹配路由才能知道应该用哪种认证策略。Filter 在 DispatcherServlet 之前执行，此时还没有路由信息。

### Q: 支持多种认证方式并存吗？
**A**: 支持。每个路由可以配置不同的 `securityCode`，同一个应用可以同时支持 JWT、签名、内部调用等多种方式。

### Q: 如何实现 OAuth2？
**A**: 实现一个 `OAuth2SecurityStrategy`，在 `process` 方法中：
1. 验证 Access Token（调用授权服务器或本地验证）
2. 获取用户信息
3. 填充 SecurityContext

### Q: 性能影响？
**A**: 
- JWT 验证：~1ms（本地解析和校验）
- 签名验证：~2ms（HMAC 计算）
- 内部调用：~0.1ms（IP 判断）

### Q: 支持缓存吗？
**A**: 可以在具体策略中实现。例如 JWT Token 可以缓存解析结果（设置短时间 TTL），签名 App Secret 可以缓存到本地。

## 相关文档

- [SecurityStrategy.java](../loadup-gateway-facade/src/main/java/io/github/loadup/gateway/facade/spi/SecurityStrategy.java) - SPI 接口
- [SecurityAction.java](./src/main/java/io/github/loadup/gateway/core/action/SecurityAction.java) - 执行入口
- [loadup-components-security](../../loadup-components/loadup-components-security/README.md) - 授权组件
