# LoadUp Modules

## 概述

LoadUp Modules 包含业务模块，每个模块都遵循 DDD（领域驱动设计）架构。

## 模块列表

| 模块                      | 说明       | 状态    |
|-------------------------|----------|-------|
| **loadup-modules-upms** | 用户权限管理系统 | ✅ 已实现 |

## loadup-modules-upms

用户权限管理系统（User Permission Management System），提供用户、角色、权限、部门等核心功能。

### 架构

基于 DDD 分层架构：

```
loadup-modules-upms/
├── loadup-modules-upms-client/          # 客户端接口（API、DTO）
├── loadup-modules-upms-app/             # 应用层（Service、Command Handler）
├── loadup-modules-upms-domain/          # 领域层（Entity、Domain Service）
├── loadup-modules-upms-infrastructure/  # 基础设施层（Repository、Mapper）
├── loadup-modules-upms-adapter/         # 适配器层（Controller）
└── loadup-modules-upms-test/            # 测试
```

### 核心功能

#### 1. 用户管理

- ✅ 用户注册、登录、注销
- ✅ 用户信息管理（CRUD）
- ✅ 密码加密存储
- ✅ 登录失败锁定

#### 2. 角色管理

- ✅ 角色定义（CRUD）
- ✅ 角色权限分配
- ✅ 角色数据范围字段（过滤策略由业务侧实现）

#### 3. 权限管理

- ✅ 菜单权限
- ✅ 按钮权限
- ✅ API 权限
- ✅ 角色与权限管理

#### 4. 部门管理

- ✅ 部门树形结构
- ✅ 部门层级管理

#### 5. 认证授权

- ✅ JWT Token 认证
- ✅ Spring Security 方法级权限控制 (`@PreAuthorize`)

### 快速开始

#### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-upms-app</artifactId>
</dependency>
```

#### 2. 配置

```yaml
loadup:
  upms:
    security:
      jwt:
        secret: ${JWT_SECRET}  # 使用 openssl rand -base64 48 生成
        expiration: 86400000  # 24 hours
      login:
        max-fail-attempts: 5
        lock-duration: 30  # minutes
```

#### 3. 使用

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 只有 ADMIN 可以删除用户
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return Result.success();
    }
    
}
```

### API 示例

#### 认证 API

```bash
# 登录
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# 响应
{
  "code": "200",
  "status": "SUCCESS",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "userId": "1",
    "username": "admin"
  }
}
```

#### 用户 API

```bash
# 获取用户列表
GET /api/v1/users?page=1&size=10
Authorization: Bearer <token>

# 创建用户
POST /api/v1/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "deptId": "dept-001",
  "roleIds": ["role-001"]
}
```

### 数据库表结构

```sql
-- 用户表
CREATE TABLE t_user (
  id VARCHAR(64) PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50),
  email VARCHAR(100),
  phone VARCHAR(20),
  dept_id VARCHAR(64),
  status TINYINT DEFAULT 1,
  login_fail_count INT DEFAULT 0,
  locked_until DATETIME,
  created_at DATETIME ,
  updated_at DATETIME  ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE t_role (
  id VARCHAR(64) PRIMARY KEY,
  role_code VARCHAR(50) UNIQUE NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  data_scope VARCHAR(20),  -- ALL/DEPT/DEPT_AND_SUB/SELF/CUSTOM
  sort INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at DATETIME ,
  updated_at DATETIME  ON UPDATE CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE t_permission (
  id VARCHAR(64) PRIMARY KEY,
  permission_code VARCHAR(100) UNIQUE NOT NULL,
  permission_name VARCHAR(50) NOT NULL,
  type VARCHAR(20),  -- MENU/BUTTON/API
  parent_id VARCHAR(64),
  sort INT DEFAULT 0,
  created_at DATETIME 
);

-- 部门表
CREATE TABLE t_department (
  id VARCHAR(64) PRIMARY KEY,
  dept_name VARCHAR(50) NOT NULL,
  parent_id VARCHAR(64),
  ancestors VARCHAR(500),
  sort INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at DATETIME ,
  updated_at DATETIME  ON UPDATE CURRENT_TIMESTAMP
);
```

## 相关文档

- [UPMS 详细文档](../docs/modules/upms.md)
- [UPMS 架构说明](loadup-modules-upms/ARCHITECTURE.md)
- 角色 `data_scope` 仅保存策略编码，数据过滤由业务侧通过类型化查询条件实现。

## 扩展开发

### 添加新模块

1. 创建模块目录结构（参考 upms 模块）
2. 实现各层代码
3. 添加到父 `pom.xml`
4. 编写单元测试和集成测试
5. 更新文档

## 贡献

欢迎提交 Issue 和 Pull Request！
