# LoadUp Auth - 认证与授权架构

## 概述

LoadUp Auth 是基于 Gateway 和 UPMS 模块构建的统一认证授权体系，采用前后端分离架构，支持多种认证方式和灵活的权限控制。

## 🎯 核心特性

- ✅ **多种认证方式**: 用户名密码、手机号验证码、邮箱、第三方OAuth
- ✅ **无状态JWT**: 基于JWT的无状态认证，支持水平扩展
- ✅ **双Token机制**: AccessToken + RefreshToken，安全性与用户体验兼顾
- ✅ **Gateway认证**: 在网关层统一处理认证，后端服务无需关心
- ✅ **方法级授权**: 基于AOP的细粒度权限控制
- ✅ **第三方登录**: 支持微信、QQ、GitHub等多种社交平台
- ✅ **安全加固**: 登录锁定、验证码、防重放攻击等多重安全机制

## 🏗️ 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  (Web/Mobile/Mini Program/Desktop)                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ HTTP Request (with JWT Token)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    LoadUp Gateway                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ SecurityAction (认证层)                               │  │
│  │  - JWT验证                                            │  │
│  │  - Token解析                                          │  │
│  │  - 用户上下文设置                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                         │                                    │
│                         ▼                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ ProxyAction (转发层)                                  │  │
│  │  - 设置 UserContext                                   │  │
│  │  - 调用业务Bean                                       │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Business Service Layer                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Authorization Component (授权层)                      │  │
│  │  - @RequireRole AOP拦截                              │  │
│  │  - @RequirePermission AOP拦截                        │  │
│  │  - UserContext获取当前用户                           │  │
│  └──────────────────────────────────────────────────────┘  │
│                         │                                    │
│                         ▼                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ UPMS Service (用户权限管理)                          │  │
│  │  - 用户管理                                           │  │
│  │  - 角色管理                                           │  │
│  │  - 权限管理                                           │  │
│  │  - OAuth绑定管理                                     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 认证流程

#### 1. 用户名密码登录

```mermaid
sequenceDiagram
    participant C as 客户端
    participant G as Gateway
    participant U as UPMS
    participant D as Database
    
    C->>G: POST /api/v1/auth/login
    G->>G: RouteAction: 路由匹配
    G->>G: SecurityAction: securityCode=OFF (登录接口不需要认证)
    G->>U: ProxyAction: 转发到 authService.login()
    U->>D: 查询用户信息
    D->>U: 返回用户数据
    U->>U: BCrypt验证密码
    U->>U: 检查账号状态(锁定/停用)
    U->>U: 生成JWT Token (access + refresh)
    U->>D: 记录登录日志
    U->>G: 返回Token和用户信息
    G->>C: 返回响应
```

#### 2. JWT Token验证流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant G as Gateway
    participant S as SecurityAction
    participant U as UPMS Service
    
    C->>G: 请求业务接口 (Header: Authorization: Bearer xxx)
    G->>S: SecurityAction拦截
    S->>S: 从Header提取Token
    S->>S: 验证Token签名和有效期
    S->>S: 解析Token获取userId、roles等
    
    alt Token有效
        S->>S: 将用户信息存入 request.attributes
        S->>G: 继续执行后续Action
        G->>U: ProxyAction设置UserContext
        U->>U: 执行业务逻辑(@RequireRole检查)
        U->>G: 返回业务结果
        G->>C: 返回响应
    else Token无效/过期
        S->>C: 返回401 Unauthorized
    end
```

### 第三方登录架构

#### OAuth2.0 标准流程

LoadUp Auth 采用标准的 OAuth 2.0 授权码模式实现第三方登录：

```mermaid
sequenceDiagram
    participant U as 用户
    participant C as 客户端
    participant G as Gateway
    participant A as UPMS Auth
    participant O as OAuth Provider
    participant D as Database
    
    Note over U,D: 第一步：获取授权URL
    U->>C: 点击"微信登录"
    C->>G: POST /oauth/authorization-url {provider: WECHAT_OPEN}
    G->>A: 转发请求
    A->>D: 查询provider配置
    D->>A: 返回配置(appId, appSecret等)
    A->>A: 生成随机state并缓存(5分钟)
    A->>A: 拼接授权URL
    A->>C: 返回authorizationUrl
    C->>U: 展示二维码或跳转授权页
    
    Note over U,D: 第二步：用户授权
    U->>O: 扫码或同意授权
    O->>O: 用户确认授权
    O->>C: 302重定向到redirectUri?code=xxx&state=yyy
    
    Note over U,D: 第三步：处理回调
    C->>G: POST /oauth/callback {provider, code, state}
    G->>A: 转发请求
    A->>A: 验证state(防CSRF)
    A->>O: 用code换取access_token
    O->>A: 返回access_token
    A->>O: 用access_token获取用户信息
    O->>A: 返回openId、昵称、头像等
    A->>D: 根据provider+openId查询绑定
    
    alt 已绑定
        A->>A: 生成系统JWT Token
        A->>D: 更新最后登录时间
        A->>C: 返回accessToken和用户信息
        C->>U: 登录成功，跳转到首页
    else 未绑定
        A->>A: 生成临时bindToken(5分钟)
        A->>C: 返回bindToken和第三方用户信息
        C->>U: 提示绑定已有账号或创建新账号
    end
    
    Note over U,D: 第四步：绑定账号(如需要)
    U->>C: 输入用户名密码或创建新账号
    C->>G: POST /oauth/bind-existing {bindToken, username, password}
    G->>A: 转发请求
    A->>A: 验证bindToken
    A->>A: 验证用户名密码
    A->>D: 创建绑定记录(user_id + provider + open_id)
    A->>A: 生成系统JWT Token
    A->>C: 返回accessToken和用户信息
    C->>U: 登录成功
```

#### 支持的OAuth Provider

| Provider | 编码 | 授权方式 | 特点 |
|----------|------|---------|------|
| 微信开放平台 | `WECHAT_OPEN` | 扫码登录 | 支持UnionID统一账号体系 |
| 微信公众号 | `WECHAT_MP` | 网页授权 | 仅限公众号内使用 |
| QQ | `QQ` | QQ互联 | 需申请QQ互联开发者资质 |
| GitHub | `GITHUB` | OAuth Apps | 适合技术类应用 |
| Google | `GOOGLE` | Google Sign-In | 国际化应用首选 |
| 微博 | `WEIBO` | 微博开放平台 | 社交属性强 |
| 支付宝 | `ALIPAY` | 支付宝登录 | 适合金融类应用 |
| 钉钉 | `DINGTALK` | 钉钉登录 | 企业内部应用 |
| 企业微信 | `WECHAT_WORK` | 企业微信授权 | 企业应用 |

#### Provider SPI 扩展机制

系统提供了统一的 `OAuthProvider` SPI 接口，支持快速扩展新的登录渠道：

```java
public interface OAuthProvider {
    
    /**
     * 获取渠道编码（唯一标识）
     */
    String getProviderCode();
    
    /**
     * 构建授权URL
     * @param config 平台配置
     * @param redirectUri 回调地址
     * @param state 防CSRF随机串
     * @return 完整的授权URL
     */
    String buildAuthorizationUrl(OAuthConfig config, String redirectUri, String state);
    
    /**
     * 用授权码换取访问令牌
     * @param config 平台配置
     * @param code 授权码
     * @return OAuth令牌(access_token, refresh_token等)
     */
    OAuthToken getAccessToken(OAuthConfig config, String code);
    
    /**
     * 获取第三方用户信息
     * @param config 平台配置
     * @param accessToken 访问令牌
     * @return 用户信息(openId, 昵称, 头像等)
     */
    OAuthUserInfo getUserInfo(OAuthConfig config, String accessToken);
    
    /**
     * 刷新访问令牌（可选）
     */
    default OAuthToken refreshToken(OAuthConfig config, String refreshToken) {
        throw new UnsupportedOperationException("Token refresh not supported");
    }
}
```

**添加新Provider示例**：

```java
@Component
public class GiteeOAuthProvider implements OAuthProvider {
    
    private final RestTemplate restTemplate;
    
    @Override
    public String getProviderCode() {
        return "GITEE";
    }
    
    @Override
    public String buildAuthorizationUrl(OAuthConfig config, String redirectUri, String state) {
        return UriComponentsBuilder
            .fromHttpUrl("https://gitee.com/oauth/authorize")
            .queryParam("client_id", config.getAppId())
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("state", state)
            .build()
            .toUriString();
    }
    
    @Override
    public OAuthToken getAccessToken(OAuthConfig config, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", config.getAppId());
        params.add("client_secret", config.getAppSecret());
        params.add("code", code);
        params.add("redirect_uri", config.getRedirectUri());
        
        GiteeTokenResponse response = restTemplate.postForObject(
            "https://gitee.com/oauth/token",
            params,
            GiteeTokenResponse.class
        );
        
        return OAuthToken.builder()
            .accessToken(response.getAccessToken())
            .refreshToken(response.getRefreshToken())
            .expiresIn(response.getExpiresIn())
            .build();
    }
    
    @Override
    public OAuthUserInfo getUserInfo(OAuthConfig config, String accessToken) {
        String url = "https://gitee.com/api/v5/user?access_token=" + accessToken;
        GiteeUser giteeUser = restTemplate.getForObject(url, GiteeUser.class);
        
        return OAuthUserInfo.builder()
            .openId(giteeUser.getId().toString())
            .nickname(giteeUser.getName())
            .avatar(giteeUser.getAvatarUrl())
            .email(giteeUser.getEmail())
            .build();
    }
}
```

注册为Spring Bean后，系统会自动发现并支持 `GITEE` 渠道。

### 安全机制

#### 1. JWT Token 结构

**Access Token Payload**:
```json
{
  "sub": "1",                    // 用户ID
  "username": "admin",           // 用户名
  "roles": ["ADMIN", "USER"],    // 角色列表
  "iat": 1709107200,            // 签发时间
  "exp": 1709193600,            // 过期时间(24小时)
  "jti": "uuid"                 // Token唯一标识
}
```

**Refresh Token Payload**:
```json
{
  "sub": "1",
  "type": "refresh",
  "iat": 1709107200,
  "exp": 1709712000,            // 过期时间(7天)
  "jti": "uuid"
}
```

#### 2. 安全策略配置

```yaml
upms:
  security:
    # JWT配置
    jwt:
      secret: ${JWT_SECRET:your-256-bit-secret-key}  # 从环境变量读取
      access-token-expiration: 86400000   # 24小时
      refresh-token-expiration: 604800000 # 7天
      issuer: loadup-auth
      
    # 登录安全
    login:
      max-fail-attempts: 5          # 最大失败次数
      lock-duration-minutes: 30     # 锁定时长
      enable-captcha: true          # 启用验证码
      captcha-threshold: 3          # 失败3次后要求验证码
      
    # 密码策略
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: false
      
    # 白名单(不需要认证的路径)
    whitelist:
      - /api/v1/auth/login
      - /api/v1/auth/register
      - /api/v1/auth/oauth/**
      - /public/**
      - /health
      - /actuator/**
```

#### 3. 防重放攻击

- **Nonce机制**: 登录请求携带一次性随机数
- **Timestamp验证**: 请求时间戳在5分钟内有效
- **JTI唯一性**: 每个Token包含唯一标识，支持黑名单撤销

#### 4. 敏感操作保护

对于密码修改、账号绑定等敏感操作，需要二次验证：

```java
@PostMapping("/change-password")
@RequireRole("USER")
public Result changePassword(@RequestBody ChangePasswordRequest request) {
    // 1. 验证原密码
    userService.verifyCurrentPassword(UserContext.getUserId(), request.getOldPassword());
    
    // 2. 或验证短信验证码
    // captchaService.verify(request.getPhone(), request.getSmsCode());
    
    // 3. 执行修改
    userService.changePassword(UserContext.getUserId(), request.getNewPassword());
    
    return Result.success();
}
```

## 📡 完整认证流程示例

### 场景1：用户名密码登录

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captchaKey": "cap-key-123",
    "captchaCode": "ABCD"
  }'
```

**响应**:
```json
{
  "result": {
    "success": true
  },
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://example.com/avatar.jpg",
      "roles": ["ADMIN"],
      "permissions": ["user:create", "user:delete", ...]
    }
  }
}
```

### 场景2：微信扫码登录

**步骤1**: 获取授权URL
```bash
curl -X POST http://localhost:8080/api/v1/auth/oauth/authorization-url \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "WECHAT_OPEN",
    "redirectUri": "https://yourapp.com/oauth/callback"
  }'
```

响应包含二维码URL，前端展示二维码供用户扫码。

**步骤2**: 用户扫码授权后，微信回调到 redirectUri

**步骤3**: 处理回调
```bash
curl -X POST http://localhost:8080/api/v1/auth/oauth/callback \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "WECHAT_OPEN",
    "code": "CODE_FROM_WECHAT",
    "state": "STATE_FROM_STEP1"
  }'
```

**响应（已绑定）**:
```json
{
  "result": {"success": true},
  "data": {
    "bound": true,
    "accessToken": "...",
    "userInfo": {...}
  }
}
```

**响应（未绑定）**:
```json
{
  "result": {"success": true},
  "data": {
    "bound": false,
    "bindToken": "temp-bind-token-5min",
    "oauthUserInfo": {
      "provider": "WECHAT_OPEN",
      "openId": "wechat-open-id",
      "nickname": "微信昵称",
      "avatar": "https://wx.qlogo.cn/..."
    }
  }
}
```

**步骤4**: 绑定已有账号（如未绑定）
```bash
curl -X POST http://localhost:8080/api/v1/auth/oauth/bind-existing \
  -H "Content-Type: application/json" \
  -d '{
    "bindToken": "temp-bind-token-5min",
    "username": "myaccount",
    "password": "mypassword"
  }'
```

### 场景3：使用Token访问受保护资源

```bash
curl -X POST http://localhost:8080/api/v1/users/query \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10
  }'
```

Gateway的SecurityAction会自动验证Token，并将用户信息注入到请求上下文中。

### 场景4：刷新Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "refresh-token-here"
  }'
```

## 🔧 配置指南

### 开发环境配置

```yaml
# application-dev.yml
upms:
  security:
    jwt:
      secret: dev-secret-key-for-testing-only
      access-token-expiration: 86400000
    login:
      max-fail-attempts: 10        # 开发环境放宽限制
      enable-captcha: false        # 关闭验证码便于测试
    oauth:
      providers:
        github:
          app-id: your-github-dev-client-id
          app-secret: your-github-dev-secret
          enabled: true
```

### 生产环境配置

```yaml
# application-prod.yml
upms:
  security:
    jwt:
      secret: ${JWT_SECRET}         # 从环境变量读取
      access-token-expiration: 3600000  # 1小时(更安全)
    login:
      max-fail-attempts: 3
      lock-duration-minutes: 60
      enable-captcha: true
    oauth:
      providers:
        wechat-open:
          app-id: ${WECHAT_APP_ID}
          app-secret: ${WECHAT_APP_SECRET}
          enabled: true
        # 其他provider...
      bind-token-expiration: 300   # 5分钟
      auto-create-account: false   # 禁止自动创建账号
```

## 📊 监控与审计

### 登录日志

每次登录（成功或失败）都会记录到 `user_login_log` 表：

| 字段 | 说明 |
|------|------|
| user_id | 用户ID（失败时可能为null） |
| username | 登录用户名 |
| login_type | 登录类型：PASSWORD/OAUTH/SMS |
| provider | OAuth渠道（如WECHAT_OPEN） |
| ip_address | 登录IP |
| user_agent | 浏览器UA |
| status | SUCCESS/FAILED |
| failure_reason | 失败原因 |
| login_time | 登录时间 |

### 操作审计

使用 `@OperationLog` 注解记录敏感操作：

```java
@OperationLog(
    type = "BIND_OAUTH",
    module = "用户中心",
    description = "绑定微信账号"
)
public void bindWechat(String userId, String openId) {
    // 业务逻辑
}
```

## 🧪 测试

### 单元测试

```java
@SpringBootTest
class AuthServiceTest {
    
    @Autowired
    private AuthService authService;
    
    @Test
    void login_success_whenValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        
        LoginResponse response = authService.login(request);
        
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
    
    @Test
    void login_locked_whenExceedMaxAttempts() {
        // 模拟多次失败登录
        for (int i = 0; i < 5; i++) {
            try {
                authService.login(new LoginRequest("admin", "wrong"));
            } catch (Exception ignored) {}
        }
        
        // 第6次应该提示账号已锁定
        assertThrows(AccountLockedException.class, () -> {
            authService.login(new LoginRequest("admin", "admin123"));
        });
    }
}
```

### 集成测试（OAuth）

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableTestContainers(ContainerType.MYSQL)
class OAuthIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @MockBean
    private WechatOAuthProvider wechatProvider;
    
    @Test
    void oauthLogin_success_whenAlreadyBound() {
        // Mock 微信OAuth响应
        when(wechatProvider.getAccessToken(any(), eq("mock-code")))
            .thenReturn(new OAuthToken("mock-access-token", ...));
        when(wechatProvider.getUserInfo(any(), eq("mock-access-token")))
            .thenReturn(new OAuthUserInfo("open-id-123", "微信用户", ...));
        
        // 调用回调接口
        OAuthCallbackRequest request = new OAuthCallbackRequest();
        request.setProvider("WECHAT_OPEN");
        request.setCode("mock-code");
        request.setState("mock-state");
        
        ResponseEntity<Result<OAuthCallbackResponse>> response = 
            restTemplate.postForEntity("/api/v1/auth/oauth/callback", request, ...);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isBound());
        assertNotNull(response.getBody().getData().getAccessToken());
    }
}
```

## 🔗 相关文档

- [Gateway 文档](../gateway.md) - 网关认证实现
- [UPMS 文档](./upms.md) - 用户权限管理
- [Authorization 组件](../components/authorization.md) - 方法级授权
- [Signature 组件](../components/signature.md) - OAuth签名验证

## 许可证

Apache 2.0 License

---

**Built with ❤️ by LoadUp Framework Team**

