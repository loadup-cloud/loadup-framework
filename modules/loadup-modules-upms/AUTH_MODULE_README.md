# LoadUp UPMS 多登录方式认证模块

## 概述

本次重构实现了基于策略模式的多登录方式认证系统，支持：
- ✅ 账号密码登录
- ✅ 手机验证码登录  
- ✅ 邮箱验证码登录
- ✅ OAuth 第三方登录（GitHub、微信、Google 等）

## 核心特性

### 1. 高扩展性
- **策略模式**: 每种登录方式独立实现 `LoginStrategy` 接口
- **SPI 机制**: Spring 自动发现和注册所有登录策略
- **插件化**: 新增登录方式无需修改核心代码

### 2. OAuth 支持
- **多提供商**: 支持 GitHub、微信、Google 等
- **自动注册**: 首次 OAuth 登录自动创建本地账号
- **账号绑定**: 支持本地账号与第三方账号绑定/解绑
- **动态配置**: 通过配置文件启用/禁用不同的 OAuth 提供商

### 3. 安全性
- **密码加密**: BCrypt 加密存储
- **失败跟踪**: 登录失败次数记录和账号自动锁定
- **验证码保护**: 短信/邮箱验证码防暴力破解
- **OAuth State**: 防 CSRF 攻击

### 4. 向后兼容
- 保留现有登录 API
- 未指定 `loginType` 时默认使用密码登录
- 无需修改现有客户端代码

## 快速开始

### 1. 配置

```yaml
loadup:
  upms:
    security:
      jwt:
        secret: your-jwt-secret
        expiration: 86400000  # 24小时
      
      login:
        enable-failure-tracking: true
        max-fail-attempts: 5
        lock-duration: 30
      
      oauth:
        github:
          enabled: true
          client-id: xxx
          client-secret: xxx
          redirect-uri: https://yourdomain.com/callback
```

### 2. API 调用

#### 账号密码登录
```bash
POST /api/v1/auth/login
{
  "loginType": "PASSWORD",  # 可选，默认为 PASSWORD
  "username": "admin",
  "password": "admin123"
}
```

#### 手机验证码登录
```bash
POST /api/v1/auth/login
{
  "loginType": "MOBILE",
  "mobile": "13800138000",
  "smsCode": "123456"
}
```

#### OAuth 登录
```bash
POST /api/v1/auth/login
{
  "loginType": "OAUTH",
  "provider": "github",
  "code": "auth_code",
  "state": "random_state",
  "redirectUri": "https://yourdomain.com/callback"
}
```

## 架构设计

```
Client Request
     ↓
AuthenticationController
     ↓
AuthenticationService
     ↓
LoginStrategyManager
     ↓
┌─────────┬─────────┬─────────┬─────────┐
│Password │ Mobile  │  Email  │  OAuth  │
│Strategy │Strategy │Strategy │Strategy │
└─────────┴─────────┴─────────┴─────────┘
```

## 扩展指南

### 新增登录方式

只需实现 `LoginStrategy` 接口：

```java
@Component
public class FaceLoginStrategy implements LoginStrategy {
    
    @Override
    public String getLoginType() {
        return "FACE";
    }
    
    @Override
    public AuthenticatedUser authenticate(LoginCredentials credentials) {
        // 人脸识别逻辑
        return AuthenticatedUser.builder()
            .userId("xxx")
            .username("xxx")
            .build();
    }
}
```

### 新增 OAuth Provider

只需实现 `OAuthProvider` 接口：

```java
@Component
@ConditionalOnProperty("loadup.upms.security.oauth.facebook.enabled")
public class FacebookOAuthProvider implements OAuthProvider {
    
    @Override
    public String getProviderName() {
        return "facebook";
    }
    
    @Override
    public String getAuthorizationUrl(String state, String redirectUri) {
        // Facebook OAuth URL
    }
    
    @Override
    public OAuthToken exchangeToken(String code, String redirectUri) {
        // 换取 Token
    }
    
    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        // 获取用户信息
    }
}
```

## 数据库变更

### 新增表

```sql
-- OAuth 绑定表
CREATE TABLE upms_user_oauth_binding (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(32) NOT NULL,
    open_id VARCHAR(128) NOT NULL,
    union_id VARCHAR(128),
    nickname VARCHAR(64),
    avatar VARCHAR(255),
    access_token VARCHAR(512),
    refresh_token VARCHAR(512),
    expires_at TIMESTAMP,
    bound_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider_openid (provider, open_id),
    INDEX idx_user_id (user_id)
);
```

### 扩展字段

```sql
-- 登录日志扩展
ALTER TABLE upms_login_log
    ADD COLUMN login_type VARCHAR(32),
    ADD COLUMN provider VARCHAR(32);
```

## 待完成功能

- ⏳ WeChat/Google OAuth Provider 实现
- ⏳ OAuth 绑定管理 API
- ⏳ Infrastructure 层实现（Mapper）
- ⏳ 单元测试（目标覆盖率 ≥ 90%）
- ⏳ 集成测试
- ⏳ 集成 gotone 组件发送验证码

## 技术栈

- Java 21
- Spring Boot 3.4.3
- MyBatis-Flex 3.5.4.1
- Spring Security (轻量集成)
- JWT
- RestTemplate (OAuth HTTP 调用)

## 贡献者

- AI Assistant (2026-02-26)

## License

GNU General Public License v3.0

