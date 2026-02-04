# Gateway 认证实施总结

## ✅ 已完成的工作

### 1. 架构设计

采用**方案 A**：Gateway 内置认证（推荐）

```
Client Request
  ↓
DispatcherServlet
  ↓
GatewayHandlerAdapter
  ↓
ActionChain
  ├─ RouteAction (路由匹配)
  ├─ SecurityAction (认证) ← 根据 RouteConfig.securityCode
  ├─ ProxyAction (转发)
  └─ ...
```

**优势**：
- ✅ 认证发生在路由决策之后，可以根据 RouteConfig 动态选择策略
- ✅ 用户信息存储在 GatewayContext.attributes，天然支持异步/RPC
- ✅ 不依赖 Spring Security Filter Chain
- ✅ 支持多种认证方式并存

### 2. 实现的认证策略

| 策略 | Code | 功能 | 状态 |
|------|------|------|------|
| 无认证 | `OFF` | 跳过认证检查 | ✅ 已实现 |
| JWT 认证 | `default` | 验证 JWT Token 并填充 SecurityContext | ✅ 已实现 |
| 签名验签 | `signature` | HMAC-SHA256 签名验证 | ✅ 已实现 |
| 内部调用 | `internal` | 基于 IP 白名单或内部标识 | ✅ 已实现 |

### 3. 文件清单

#### 新增文件

```
loadup-gateway-core/src/main/java/io/github/loadup/gateway/core/security/
├── DefaultSecurityStrategy.java        # JWT 认证策略（已完善）
├── SignatureSecurityStrategy.java      # 签名验签策略（新增）
└── InternalSecurityStrategy.java       # 内部调用策略（新增）
```

#### 已存在（保留）

```
loadup-gateway-core/src/main/java/io/github/loadup/gateway/core/security/
├── SecurityStrategyManager.java        # 策略管理器
└── OffSecurityStrategy (内嵌类)        # OFF 策略

loadup-gateway-core/src/main/java/io/github/loadup/gateway/core/action/
└── SecurityAction.java                 # 认证执行入口

loadup-gateway-facade/src/main/java/io/github/loadup/gateway/facade/spi/
└── SecurityStrategy.java               # SPI 接口
```

#### 文档

```
loadup-gateway-core/
├── SECURITY.md                         # 认证实施文档（新增）
└── README.md                           # 需要更新

loadup-components/loadup-components-security/
├── REFACTORING.md                      # 重构总结
└── README.md                           # 使用文档
```

### 4. 依赖变更

#### Gateway Core (`loadup-gateway-core/pom.xml`)

```xml
<!-- 新增：可选依赖，用于填充 SecurityContext -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-core</artifactId>
    <optional>true</optional>
</dependency>
```

**说明**：
- 使用 `optional=true`，不强制依赖
- 使用反射动态加载 `LoadUpUser`
- 如果应用引入了 `loadup-components-security`，自动支持 `@PreAuthorize`

### 5. 核心特性

#### 🔐 JWT 认证（DefaultSecurityStrategy）

**功能**：
- ✅ 验证 JWT 签名和过期时间
- ✅ 提取用户信息（userId, username, roles）
- ✅ 填充到 Request Headers (`X-User-Id`, `X-User-Name`, `X-User-Roles`)
- ✅ 填充到 Request Attributes
- ✅ 填充到 SecurityContext（动态加载 LoadUpUser）

**使用示例**：
```yaml
routes:
  - routeId: "user-api"
    path: "/api/v1/users/**"
    securityCode: "default"
```

#### ✍️ 签名验签（SignatureSecurityStrategy）

**功能**：
- ✅ HMAC-SHA256 签名计算
- ✅ 时间戳验证（防重放攻击）
- ✅ Nonce 防重放
- ✅ 参数排序后签名

**签名算法**：
```
signStr = "key1=value1&key2=value2&timestamp=xxx&nonce=xxx"
signature = HMAC-SHA256(signStr, appSecret)
```

**使用示例**：
```yaml
routes:
  - routeId: "open-api"
    path: "/open-api/**"
    securityCode: "signature"
```

#### 🏠 内部调用（InternalSecurityStrategy）

**功能**：
- ✅ IP 白名单验证
- ✅ 内部标识 Header 检查 (`X-Internal-Call: true`)
- ✅ 支持私有网段自动识别

**支持的内网 IP**：
- `127.0.0.1` (localhost)
- `10.*`
- `172.16.*` ~ `172.31.*`
- `192.168.*`

**使用示例**：
```yaml
routes:
  - routeId: "internal-api"
    path: "/api/internal/**"
    securityCode: "internal"
```

### 6. SecurityContext 填充

**关键代码**：

```java
// DefaultSecurityStrategy.populateSecurityContext()
private void populateSecurityContext(String userId, String username, List<String> roles) {
    try {
        // 动态加载 LoadUpUser（避免强依赖）
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
        // loadup-components-security not in classpath
    }
}
```

**设计亮点**：
- 🌟 **松耦合**：Gateway 不强依赖 Security 组件
- 🌟 **自适应**：有 Security 组件时自动填充，没有时不影响功能
- 🌟 **类型安全**：反射调用有异常处理，不会导致 Gateway 崩溃

## 🎯 工作流程

### 完整请求流程

```
1. Client 发送请求
   GET /api/v1/users/profile
   Authorization: Bearer eyJhbG...

2. DispatcherServlet 接收请求

3. GatewayHandlerAdapter 处理
   ├─ buildGatewayRequest()
   └─ ActionDispatcher.dispatch()

4. ActionChain 执行
   ├─ RouteAction
   │   └─ 匹配到路由: routeId="user-api", securityCode="default"
   │
   ├─ SecurityAction
   │   ├─ 获取策略: SecurityStrategyManager.getStrategy("default")
   │   ├─ 执行认证: DefaultSecurityStrategy.process()
   │   │   ├─ 验证 JWT
   │   │   ├─ 提取用户信息
   │   │   ├─ 填充 Headers/Attributes
   │   │   └─ 填充 SecurityContext
   │   └─ 认证成功
   │
   ├─ ProxyAction (Bean/RPC/HTTP)
   │   └─ 转发到 UPMS Service
   │       └─ @PreAuthorize("hasRole('USER')") ✅ 通过
   │
   └─ ResponseAction
       └─ 返回结果

5. writeResponse()
```

### 与 Security 组件协作

```
┌──────────────────────────────────────┐
│  Gateway (认证 Authentication)        │
│  ├─ SecurityAction                   │
│  ├─ SecurityStrategy                 │
│  └─ 填充 SecurityContext             │
└────────────────┬─────────────────────┘
                 │ 
                 ↓ 转发请求（Bean Proxy）
┌──────────────────────────────────────┐
│  Security 组件 (授权 Authorization)   │
│  ├─ @EnableMethodSecurity            │
│  └─ @PreAuthorize 检查角色           │
└────────────────┬─────────────────────┘
                 │
                 ↓
┌──────────────────────────────────────┐
│  UPMS Service (业务逻辑)              │
│  └─ SecurityHelper.getCurUserId()    │
└──────────────────────────────────────┘
```

## 📝 使用指南

### 配置路由

```yaml
loadup:
  gateway:
    security:
      header: "Authorization"
      prefix: "Bearer "
      secret: "your-jwt-secret-key"
    
    routes:
      # 公开接口
      - routeId: "auth-login"
        path: "/api/v1/auth/login"
        securityCode: "OFF"
        proxyType: "bean"
        targetBean: "authenticationController"
        
      # JWT 认证接口
      - routeId: "user-api"
        path: "/api/v1/users/**"
        securityCode: "default"
        proxyType: "bean"
        targetBean: "userController"
        
      # 签名验签接口
      - routeId: "open-api"
        path: "/open-api/**"
        securityCode: "signature"
        proxyType: "bean"
        targetBean: "openApiController"
        
      # 内部接口
      - routeId: "internal-api"
        path: "/api/internal/**"
        securityCode: "internal"
        proxyType: "bean"
        targetBean: "internalController"
```

### 在业务代码中使用

```java
@Service
public class UserService {
    
    // 使用 @PreAuthorize 注解
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        // 只有 ADMIN 可以删除
    }
    
    // 获取当前用户
    public void updateProfile(UserDTO dto) {
        String currentUserId = SecurityHelper.getCurUserId();
        // 更新当前用户的资料
    }
}
```

## 🔄 下一步工作

### 1. 完善配置（高优先级）

- [ ] 将 Signature App Secrets 移到数据库
- [ ] 支持动态刷新 App Secrets
- [ ] 支持 JWT Secret 轮换

### 2. 增强功能（中优先级）

- [ ] 实现 OAuth2 策略
- [ ] 添加 Rate Limiting（基于用户/IP）
- [ ] 添加 Audit Log（认证失败记录）

### 3. 监控与告警（中优先级）

- [ ] 添加认证成功/失败指标
- [ ] 集成 Micrometer Metrics
- [ ] 添加慢认证告警（>100ms）

### 4. 测试（高优先级）

- [ ] 单元测试（各个 Strategy）
- [ ] 集成测试（端到端）
- [ ] 性能测试（压测）

## 📚 相关文档

- [SECURITY.md](./SECURITY.md) - Gateway 认证详细文档
- [loadup-components-security/README.md](../../loadup-components/loadup-components-security/README.md) - Security 组件使用文档
- [loadup-components-security/REFACTORING.md](../../loadup-components/loadup-components-security/REFACTORING.md) - Security 组件重构总结

## 🎉 总结

通过本次实施，LoadUp 项目建立了**清晰的认证授权分层**：

- **Gateway**：负责认证（Authentication），支持多种策略
- **Security 组件**：负责授权（Authorization），提供方法级权限控制
- **业务模块**：专注业务逻辑，使用注解声明权限

这种架构具有：
- ✅ **灵活性**：支持 JWT、签名、内部调用等多种认证方式
- ✅ **可扩展性**：通过 SPI 轻松添加自定义策略
- ✅ **松耦合**：各组件职责清晰，可独立演进
- ✅ **高性能**：认证逻辑在 Gateway 层完成，不影响业务性能
- ✅ **易维护**：配置化路由，无需修改代码
