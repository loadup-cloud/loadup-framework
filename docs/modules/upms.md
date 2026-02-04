# LoadUp UPMS - 用户权限管理系统

## 概述

LoadUp UPMS (User Permission Management System) 是基于 **COLA 4.0** 架构的企业级用户权限管理系统，实现 **RBAC3** (角色继承与约束) 权限模型，采用 **MyBatis-Flex** 提供类型安全的数据访问。

## 核心特性

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

## 架构设计

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

详细架构设计请参阅: [ARCHITECTURE.md](../../loadup-modules/loadup-modules-upms/ARCHITECTURE.md)

## 数据库设计

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

## 快速开始

### 1. 依赖引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-upms-starter</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```

### 2. 数据库初始化

执行数据库脚本创建表结构：

```bash
mysql -u root -p loadup < schema.sql
```

### 3. 配置文件

```yaml
upms:
  security:
    jwt:
      secret: your-secret-key-at-least-32-characters
    login:
      max-fail-attempts: 5
      lock-duration: 30
```

## API 接口

### 用户管理

```
POST   /api/upms/user           创建用户
GET    /api/upms/user/{id}      查询用户
PUT    /api/upms/user/{id}      更新用户
DELETE /api/upms/user/{id}      删除用户
GET    /api/upms/user           用户列表（分页）
```

### 角色管理

```
POST   /api/upms/role           创建角色
GET    /api/upms/role/{id}      查询角色
PUT    /api/upms/role/{id}      更新角色
DELETE /api/upms/role/{id}      删除角色
POST   /api/upms/role/{id}/permissions  分配权限
```

### 部门管理

```
POST   /api/upms/dept           创建部门
GET    /api/upms/dept/{id}      查询部门
PUT    /api/upms/dept/{id}      更新部门
DELETE /api/upms/dept/{id}      删除部门
GET    /api/upms/dept/tree      部门树
```

## MyBatis-Flex 使用示例

### 类型安全查询

```java
import static io.github.loadup.modules.upms.infrastructure.persistence.tables.Tables.*;

// 查询用户
QueryWrapper query = QueryWrapper.create()
    .where(UPMS_USER.USERNAME.eq("admin"))
    .and(UPMS_USER.STATUS.eq(1));
User user = userMapper.selectOneByQuery(query);

// 关联查询
QueryWrapper query = QueryWrapper.create()
    .select(UPMS_USER.ALL_COLUMNS)
    .from(UPMS_USER)
    .leftJoin(UPMS_DEPARTMENT).on(UPMS_USER.DEPT_ID.eq(UPMS_DEPARTMENT.ID))
    .where(UPMS_USER.STATUS.eq(1));
```

## 安全机制

### JWT 认证

1. 登录获取 Token
2. 请求携带 Token: `Authorization: Bearer {token}`
3. 后端验证 Token 合法性
4. 提取用户信息和权限

### 权限控制

使用 Spring Security 注解：

```java
@PreAuthorize("hasAuthority('system:user:create')")
public void createUser(UserCreateCmd cmd) { ... }

@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long userId) { ... }
```

## 性能优化

- **缓存策略**: 用户信息、角色权限缓存到 Redis
- **批量查询**: 避免 N+1 查询问题
- **异步日志**: 操作日志异步写入
- **索引优化**: 核心查询字段建立索引

## 相关文档

- [完整 README](../../loadup-modules/loadup-modules-upms/README.md)
- [架构设计](../../loadup-modules/loadup-modules-upms/ARCHITECTURE.md)
- [API 文档](http://localhost:8080/swagger-ui.html)

---

**© 2025 LoadUp Framework. All rights reserved.**
