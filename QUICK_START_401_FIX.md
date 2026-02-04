# 401 错误修复 - 快速验证指南

## ✅ 已完成的修复

1. **添加了 `SecurityFilterChain` 配置**
   - 位置: `loadup-components-security/.../SecurityAutoConfiguration.java`
   - 配置: `anyRequest().permitAll()` - 放行所有请求

2. **添加了必要依赖**
   - 添加: `spring-security-web` 依赖

3. **清理了无效配置**
   - 移除: `loadup.security.ignore-urls` 配置

## 🚀 快速验证步骤

### 步骤 1: 启动应用

```bash
cd /Users/lise/PersonalSpace/loadup-cloud/loadup-parent/loadup-application

# 方式 1: Maven
mvn spring-boot:run

# 方式 2: Java
mvn clean package -DskipTests
java -jar target/loadup-application-0.0.2-SNAPSHOT.jar
```

### 步骤 2: 测试基本访问

```bash
# 测试 actuator（如果配置为 OFF）
curl http://localhost:8080/actuator/health

# 期望: 200 OK
# 返回: {"status":"UP"}
```

### 步骤 3: 检查日志

启动后检查日志中是否有这些信息：

```
✅ 成功的日志:
- Initialized SecurityStrategyManager with strategies: [OFF, default, signature, internal]
- Started SecurityAutoConfiguration
- Gateway enabled: true

❌ 如果看到这些错误:
- 401 Unauthorized (所有请求) → 说明 SecurityFilterChain 配置未生效
- Bean creation error → 检查依赖是否正确
```

## 📋 当前状态说明

### ✅ 现在可以正常工作

由于 `SecurityFilterChain` 配置为 `permitAll()`，所有请求都会放行到 Gateway 层。

**但是**：如果没有配置路由（RouteConfig），Gateway 会找不到对应的处理器。

### ⚠️ 下一步需要做什么

**配置路由文件**，指定哪些路径需要认证、使用哪种认证策略。

## 🔧 配置路由（必须）

### 方式 1: FILE 存储（推荐用于开发）

**创建路由配置文件**:

```bash
mkdir -p /Users/lise/PersonalSpace/loadup-cloud/loadup-parent/loadup-application/src/main/resources/gateway-config
```

**创建示例路由**: `gateway-config/upms-routes.json`

```json
[
  {
    "routeId": "auth-login",
    "path": "/api/v1/auth/login",
    "method": "POST",
    "securityCode": "OFF",
    "proxyType": "bean",
    "targetBean": "authenticationController",
    "targetMethod": "login",
    "enabled": true
  },
  {
    "routeId": "auth-register",
    "path": "/api/v1/auth/register",
    "method": "POST",
    "securityCode": "OFF",
    "proxyType": "bean",
    "targetBean": "authenticationController",
    "targetMethod": "register",
    "enabled": true
  },
  {
    "routeId": "user-profile",
    "path": "/api/v1/users/profile",
    "method": "GET",
    "securityCode": "default",
    "proxyType": "bean",
    "targetBean": "userController",
    "targetMethod": "getProfile",
    "enabled": true
  },
  {
    "routeId": "actuator-health",
    "path": "/actuator/health",
    "method": "GET",
    "securityCode": "OFF",
    "proxyType": "http",
    "targetUrl": "http://localhost:8080/actuator/health",
    "enabled": true
  }
]
```

**更新 application.yml**:

```yaml
loadup:
  gateway:
    enabled: true
    storage:
      type: FILE
      file:
        base-path: classpath:gateway-config  # 改为相对路径
```

### 方式 2: DATABASE 存储（推荐用于生产）

**创建数据库表** (如果不存在):

```sql
CREATE TABLE t_gateway_route (
  id VARCHAR(64) PRIMARY KEY,
  route_id VARCHAR(128) NOT NULL UNIQUE,
  path VARCHAR(255) NOT NULL,
  method VARCHAR(10),
  security_code VARCHAR(32),
  proxy_type VARCHAR(32) NOT NULL,
  target_bean VARCHAR(255),
  target_method VARCHAR(128),
  target_url VARCHAR(512),
  enabled TINYINT(1) DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**插入示例数据**:

```sql
INSERT INTO t_gateway_route VALUES
  ('1', 'auth-login', '/api/v1/auth/login', 'POST', 'OFF', 'bean', 'authenticationController', 'login', NULL, 1, NOW(), NOW()),
  ('2', 'user-profile', '/api/v1/users/profile', 'GET', 'default', 'bean', 'userController', 'getProfile', NULL, 1, NOW(), NOW());
```

**更新 application.yml**:

```yaml
loadup:
  gateway:
    enabled: true
    storage:
      type: DATABASE
```

## 🧪 完整测试流程

### 1. 测试公开接口（securityCode=OFF）

```bash
# 登录接口（应该返回 200）
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 期望返回:
# {
#   "code": "200",
#   "status": "SUCCESS",
#   "data": {
#     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#     "userId": "1",
#     "username": "admin"
#   }
# }
```

### 2. 测试受保护接口（securityCode=default）

```bash
# 保存 Token
TOKEN="<从登录接口获取的 token>"

# 不带 Token（应该返回 401）
curl http://localhost:8080/api/v1/users/profile

# 带 Token（应该返回 200）
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/users/profile
```

### 3. 测试方法级权限（@PreAuthorize）

假设在 UserService 中有：

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(String userId) { ... }
```

```bash
# 普通用户尝试删除（应该返回 403 Forbidden）
curl -X DELETE -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8080/api/v1/users/123

# 管理员删除（应该返回 200）
curl -X DELETE -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/v1/users/123
```

## 🐛 常见问题排查

### 问题 1: 仍然返回 401

**原因**: SecurityFilterChain 配置未生效

**检查**:
```bash
# 查看日志中是否有
grep "SecurityAutoConfiguration" application.log

# 应该看到:
# o.s.s.web.DefaultSecurityFilterChain : Will secure any request with [...]
```

**解决**: 确保 `loadup-components-security` 依赖已正确引入

### 问题 2: 返回 404 Not Found

**原因**: 路由未配置或未匹配

**检查**:
```bash
# 查看日志中是否有
grep "Route not found" application.log
```

**解决**: 检查路由配置文件或数据库中的路由数据

### 问题 3: JWT 认证失败（Invalid token）

**原因**: JWT Secret 配置错误或 Token 过期

**检查 application.yml**:
```yaml
loadup:
  gateway:
    security:
      secret: "your-jwt-secret-key"  # 必须与生成 Token 时使用的 Secret 一致
```

### 问题 4: @PreAuthorize 不生效

**原因**: SecurityContext 未填充或角色格式不对

**检查**:
```java
// 在 Service 方法中打印
String userId = SecurityHelper.getCurUserId();
LoadUpUser user = SecurityHelper.getCurUser();
System.out.println("Current user: " + user);
System.out.println("Roles: " + user.getRoles());
```

**确保**:
- Gateway 成功认证并填充了 SecurityContext
- JWT Claims 中包含 `roles` 字段
- 角色格式: `["ROLE_ADMIN", "ROLE_USER"]` 或 `"ROLE_ADMIN,ROLE_USER"`

## 📚 相关文档

- [FIX_401_ERROR.md](./FIX_401_ERROR.md) - 详细修复说明
- [GATEWAY_AUTH_DELIVERY.md](./GATEWAY_AUTH_DELIVERY.md) - Gateway 认证实施总交付
- [loadup-gateway-core/SECURITY.md](./loadup-gateway/loadup-gateway-core/SECURITY.md) - 认证策略文档

## ✅ 验证清单

- [ ] 应用能正常启动（无 Bean creation 错误）
- [ ] 日志中显示 SecurityStrategyManager 初始化成功
- [ ] 公开接口可以访问（如 /actuator/health）
- [ ] 路由配置已创建（FILE 或 DATABASE）
- [ ] JWT 认证接口测试通过
- [ ] 方法��权限（@PreAuthorize）测试通过

---

**如果以上步骤都完成，401 错误应该已经解决！** 🎉
