# LoadUp UPMS - User Permission Management System

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.2-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://www.oracle.com/java/)
[![MyBatis-Flex](https://img.shields.io/badge/MyBatis--Flex-1.11.5-blue.svg)](https://mybatis-flex.com/)

基于 **COLA 4.0** 架构的企业级用户权限管理系统，实现 **RBAC3** (角色继承与约束) 权限模型，采用 **MyBatis-Flex** 提供类型安全的数据访问。

## 🎯 核心特性

### 1. RBAC3 权限模型
- ✅ **角色继承**: 支持多级角色继承，子角色自动继承父角色权限
- ✅ **静态职责分离 (SSD)**: 互斥角色约束
- ✅ **动态职责分离 (DSD)**: 会话级别的角色激活约束
- ✅ **数据权限**: 5种数据范围控制（全部/自定义/本部门/本部门及子部门/仅本人）

### 2. 组织架构管理
- 📁 **无限层级部门树**: 支持任意深度的组织结构
- 👥 **部门维度授权**: 支持按部门分配角色和权限
- 🔄 **部门迁移**: 用户和子部门的批量转移

### 3. 用户中心
- 🔐 **多种登录方式**: 用户名/邮箱/手机号登录
- 📱 **第三方登录**: 支持微信、QQ、GitHub等社交账号
- 🖼️ **头像管理**: 集成DFS组件，支持头像上传
- 🔒 **安全策略**: 
  - 登录失败自动锁定
  - 密码强度校验
  - JWT令牌管理
  - 双Token机制（access + refresh）

### 4. 系统监控
- 📊 **操作日志**: AOP异步记录用户行为
- 🔍 **多维度查询**: 按用户/时间/操作类型/IP等条件检索
- 📈 **登录审计**: 完整的登录/登出日志记录
- ⚡ **性能监控**: 接口执行时间统计

## 🏗️ 架构设计

采用 **COLA 4.0** 分层架构，严格遵循领域驱动设计（DDD）原则：

```
loadup-modules-upms/
├── loadup-modules-upms-adapter/      # 适配层：REST API、DTO
├── loadup-modules-upms-app/          # 应用层：业务编排、Command/Query
├── loadup-modules-upms-domain/       # 领域层：实体、值对象、Repository接口
├── loadup-modules-upms-infrastructure/ # 基础设施层：Repository实现、Security配置
└── loadup-modules-upms-starter/      # Starter：自动配置
```

### 架构优势

| 层次 | 职责 | 依赖方向 |
|------|------|----------|
| **Adapter** | 接收外部请求，数据转换 | → App |
| **App** | 业务流程编排，事务管理 | → Domain |
| **Domain** | 核心业务逻辑，领域规则 | 无依赖 |
| **Infrastructure** | 技术实现，外部集成 | → Domain |

## 📊 数据库设计

### 核心表结构

#### 1. 用户表 (`upms_user`)
存储用户基本信息和安全策略配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(200) | BCrypt加密密码 |
| dept_id | BIGINT | 所属部门 |
| status | SMALLINT | 状态：1-正常 0-停用 2-锁定 |
| login_fail_count | INT | 登录失败次数 |
| locked_time | TIMESTAMP | 锁定时间 |

#### 2. 角色表 (`upms_role`)
支持角色继承的角色定义。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| role_code | VARCHAR(50) | 角色编码（唯一） |
| parent_role_id | BIGINT | 父角色ID（用于继承） |
| data_scope | SMALLINT | 数据权限范围 |

#### 3. 权限表 (`upms_permission`)
树状结构的权限资源定义。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| permission_code | VARCHAR(100) | 权限编码（唯一） |
| permission_type | SMALLINT | 类型：1-菜单 2-按钮 3-接口 |
| resource_path | VARCHAR(200) | 资源路径/URL |

#### 4. 部门表 (`upms_department`)
支持无限层级的组织架构树。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| parent_id | BIGINT | 父部门ID |
| dept_level | INT | 部门层级 |

### ER图概览

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   upms_user │───┬───│ upms_user_  │───┬───│   upms_role │
│             │   │   │    role     │   │   │             │
└─────────────┘   │   └─────────────┘   │   └─────────────┘
                  │                     │            │
                  │                     │            │ parent_role_id
                  │                     │            ▼
                  │                     │   ┌─────────────┐
                  │                     │   │  upms_role  │
                  │                     │   │  (inherit)  │
                  │                     │   └─────────────┘
                  │                     │            │
                  │   ┌─────────────┐   │            │
                  └───│   upms_     │◄──┘            │
                      │ department  │                │
                      └─────────────┘                │
                                                     │
                      ┌─────────────┐                │
                      │  upms_role_ │◄───────────────┘
                      │ permission  │
                      └─────────────┘
                              │
                              ▼
                      ┌─────────────┐
                      │   upms_     │
                      │ permission  │
                      └─────────────┘
```

完整的数据库Schema请参考：[schema.sql](./schema.sql)

## 🚀 快速开始

### 1. 依赖引入

在你的Spring Boot项目中添加依赖：

```xml
<dependency>
    <groupId>com.github.loadup.modules</groupId>
    <artifactId>loadup-modules-upms-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 数据库初始化

执行数据库脚本创建表结构：

```bash
mysql -u root -p loadup_upms < schema.sql
```

默认创建管理员账号：
- 用户名：`admin`
- 密码：`admin123`

### 3. 配置文件

复制 `application.yml.example` 并根据实际环境修改：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/loadup_upms?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password

upms:
  security:
    jwt:
      secret: your-secret-key-at-least-32-characters-long
    login:
      max-fail-attempts: 5
      lock-duration: 30
    captcha:
      enabled: true
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

访问 Swagger 文档：`http://localhost:8080/swagger-ui.html`

## 📖 MyBatis-Flex 使用指南

本模块使用 **MyBatis-Flex** 提供类型安全的数据库访问。

### 快速开始

#### 1. 导入 Tables 定义

```java
import static com.github.loadup.modules.upms.infrastructure.dataobject.Tables.*;
```

#### 2. 基础查询

```java
// 单条件查询
QueryWrapper query = QueryWrapper.create()
                .where(USER.USERNAME.eq("admin"));

// 多条件查询  
QueryWrapper query = QueryWrapper.create()
        .where(USER.STATUS.eq((short) 1))
        .and(USER.DEPT_ID.in(deptIds))
        .orderBy(USER.CREATE_TIME.desc());
```

#### 3. 分页查询

```java
Page<UserDO> page = userMapper.paginate(
        Page.of(pageNum, pageSize),
        query
);
```

### 常用查询模式

| 方法                | SQL                 | 示例                                     |
|-------------------|---------------------|----------------------------------------|
| `eq(value)`       | `= value`           | `USER.STATUS.eq(1)`                    |
| `like(value)`     | `LIKE '%value%'`    | `USER.USERNAME.like("admin")`          |
| `in(values)`      | `IN (...)`          | `USER.DEPT_ID.in(1, 2, 3)`             |
| `between(v1, v2)` | `BETWEEN v1 AND v2` | `USER.CREATE_TIME.between(start, end)` |
| `isNull()`        | `IS NULL`           | `USER.DELETED.isNull()`                |

### 优势

- ✅ **类型安全**：编译时检查字段名，避免运行时错误
- ✅ **自动完成**：IDE 提供字段自动补全
- ✅ **重构友好**：字段重命名时自动更新
- ✅ **性能优化**：自动分页，无需手动编写 SQL

更多用法请参考 [ARCHITECTURE.md](./ARCHITECTURE.md) 中的 MyBatis-Flex 章节。

## 📡 API 文档

### 统一响应格式

所有API统一使用POST方法，响应格式统一为：

```json
{
  "result": {
    "success": true,
    "errCode": null,
    "errMessage": null
  },
  "data": {
    ...
  }
}
```

### 认证接口

#### 登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "captchaKey": "optional-key",
  "captchaCode": "optional-code"
}
```

**响应示例：**
```json
{
  "result": {
    "success": true,
    "errCode": null,
    "errMessage": null
  },
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "roles": [
        "ROLE_SUPER_ADMIN"
      ],
      "permissions": [
        "system:user:query",
        "system:user:create",
        ...
      ]
    }
  }
}
```

#### 注册
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "Password123",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### 刷新令牌
```http
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### 用户管理接口

#### 创建用户

```http
POST /api/v1/users/create
Content-Type: application/json

{
  "username": "newuser",
  "password": "Password123",
  "nickname": "新用户",
  "deptId": 1,
  "roleIds": [2, 3]
}
```

#### 查询用户列表

```http
POST /api/v1/users/query
Content-Type: application/json

{
  "page": 1,
  "size": 10,
  "username": "admin"
}
```

**响应示例（分页）：**

```json
{
  "result": {
    "success": true
  },
  "data": [
    ...
  ],
  "totalCount": 100,
  "pageSize": 10,
  "pageIndex": 1
}
```

#### 获取用户详情

```http
POST /api/v1/users/get
Content-Type: application/json

{
  "id": 1
}
```

#### 更新用户

```http
POST /api/v1/users/update
Content-Type: application/json

{
  "id": 1,
  "nickname": "新昵称",
  "email": "newemail@example.com"
}
```

#### 删除用户

```http
POST /api/v1/users/delete
Content-Type: application/json

{
  "id": 1
}
```

#### 锁定/解锁用户

```http
POST /api/v1/users/lock
Content-Type: application/json

{
  "id": 1
}
```

### 角色管理接口

#### 获取角色树

```http
POST /api/v1/roles/tree
Content-Type: application/json

{}
```

#### 分配权限给角色

```http
POST /api/v1/roles/assign-permissions
Content-Type: application/json

{
  "roleId": 2,
  "permissionIds": [1, 2, 3, 4]
}
```

### 权限管理接口

#### 获取用户菜单树

```http
POST /api/v1/permissions/user-menu-tree
Content-Type: application/json

{
  "id": 1
}
```

#### 按类型获取权限

```http
POST /api/v1/permissions/by-type
Content-Type: application/json

{
  "permissionType": 1
}
```

权限类型：

- `1` - 菜单权限
- `2` - 按钮权限
- `3` - API权限

完整API文档请访问 Swagger UI：`http://localhost:8080/swagger-ui.html`

## 🔧 高级配置

### 配置检查清单

部署前请确保：

#### 基础环境

- ✅ JDK 17+ 已安装
- ✅ Maven 3.8+ 已安装
- ✅ MySQL 8.0+ 已安装并运行
- ✅ Redis 6.0+ 已安装并运行（可选）

#### 数据库初始化

1. 创建数据库：`CREATE DATABASE loadup_upms;`
2. 执行脚本：`mysql -u root -p loadup_upms < schema.sql`
3. 验证表创建成功（应有12张表）
4. 验证初始数据：默认管理员 `admin/admin123`

### JWT密钥配置 ⚠️ 重要

```yaml
upms:
  security:
    jwt:
      # 生产环境必须修改！至少32个字符
      secret: your-secret-key-at-least-32-characters-long-change-in-production
      expiration: 86400000  # 24小时
```

**生成安全密钥**：

```bash
openssl rand -base64 32
```

### 自定义白名单

在 `application.yml` 中配置不需要认证的路径：

```yaml
upms:
  security:
    whitelist:
      - /public/**
      - /api/v1/public/**
      - /health
```

### 验证码开关

动态控制验证码验证：

```yaml
upms:
  security:
    captcha:
      enabled: true    # 设置为false可关闭验证码
      type: image      # image: 图形验证码, sms: 短信验证码
      expiration: 300  # 有效期（秒）
```

### 登录锁定策略

```yaml
upms:
  security:
    login:
      max-fail-attempts: 5      # 最大失败次数
      lock-duration: 30         # 锁定时长（分钟）
      enable-failure-tracking: true
```

### 数据权限使用

在Service方法上使用 `@DataScope` 注解：

```java
@DataScope(deptAlias = "d")
public List<User> findUsers() {
    // 自动根据用户角色的data_scope字段过滤数据
}
```

### 操作日志记录

在需要记录的方法上添加注解：

```java
@OperationLog(
    type = "CREATE",
    module = "用户管理",
    description = "创建新用户",
    recordResponse = true
)
public User createUser(UserCreateCommand command) {
    // ...
}
```

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 集成测试（使用 Testcontainers）

```bash
mvn verify -P integration-test
```

集成测试会自动启动MySQL和Redis容器。

## 📦 组件依赖

本模块依赖以下 LoadUp 组件：

| 组件 | 用途 |
|------|------|
| `loadup-components-database` | 数据库连接池和事务管理 |
| `loadup-components-cache` | Redis缓存支持 |
| `loadup-components-captcha` | 验证码生成和验证 |
| `loadup-components-dfs` | 分布式文件存储（头像） |
| `loadup-components-gotone` | 短信/邮件通知 |
| `loadup-components-scheduler` | 定时任务（日志清理） |

## 🔐 安全最佳实践

1. **密码策略**
   - 使用BCrypt加密，不可逆
   - 强制密码复杂度要求
   - 定期更新密码提醒

2. **令牌管理**
   - 双Token机制（access + refresh）
   - access token短期有效（24小时）
   - refresh token长期有效（7天）
   - 支持令牌撤销（黑名单机制）

3. **防暴力破解**
   - 登录失败计数
   - 自动账号锁定
   - 验证码验证

4. **审计日志**
   - 完整的操作记录
   - 敏感操作二次验证
   - 日志防篡改

## 📝 TODO

- [ ] 添加多因素认证（MFA）
- [ ] 支持LDAP/AD集成
- [ ] OAuth 2.0 授权服务器
- [ ] 细粒度字段级权限控制
- [ ] 在线用户管理和强制下线
- [ ] 权限缓存预热和刷新机制
- [ ] GraphQL API支持

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

请确保代码符合项目规范，执行 `mvn spotless:apply` 格式化代码。

## 📄 许可证

本项目采用 [Apache License 2.0](../../../LICENSE) 许可证。

## 👥 维护者

- **LoadUp Framework Team** - [GitHub](https://github.com/loadup-cloud)

## 📮 联系方式

- 问题反馈：[GitHub Issues](https://github.com/loadup-cloud/loadup-framework/issues)
- 技术讨论：[Discussions](https://github.com/loadup-cloud/loadup-framework/discussions)

---

**Built with ❤️ by LoadUp Framework Team**
