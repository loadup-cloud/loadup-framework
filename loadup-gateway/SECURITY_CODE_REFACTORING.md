# SecurityCode 字段重构总结

## 概述

将 `securityCode` 从 properties 中提取出来，作为路由配置的一个独立关键字段，分别在 CSV 文件和数据库表中作为独立列存储。

## 修改内容

### 1. CSV 文件格式变更

**之前的格式**:
```csv
path,method,target,requestTemplate,responseTemplate,enabled,properties
/api/test,GET,http://...,req.groovy,resp.groovy,true,timeout=30000;securityCode=OFF
```

**新的格式**:
```csv
path,method,target,securityCode,requestTemplate,responseTemplate,enabled,properties
/api/test,GET,http://...,OFF,req.groovy,resp.groovy,true,timeout=30000;retryCount=3
```

**关键变化**:
- ✅ `securityCode` 从 properties 中提取出来
- ✅ 作为独立列放在 `target` 后面（索引 3）
- ✅ properties 中不再包含 securityCode

### 2. 文件清单

#### 修改的文件

```
✅ routes.csv
   - 更新 header 添加 securityCode 列
   - 调整所有数据行

✅ FileRouteEntity.java
   - 添加 securityCode 字段

✅ FileRepositoryPlugin.java
   - 更新 parseRouteFromCsvLine() - 解析第4列为 securityCode
   - 更新 convertToRouteConfig() - 使用 entity.getSecurityCode()
   - 更新 createRoutesFile() - header 包含 securityCode

✅ RouteEntity.java (database)
   - 添加 securityCode 字段

✅ DatabaseRepositoryPlugin.java
   - 更新 convertToRouteConfig() - 使用 entity.getSecurityCode()

✅ RouteConfig.java
   - 更新 builderFrom() - 复制 securityCode 字段
```

#### 新增的文件

```
✅ V2__add_security_code_column.sql
   - 数据库迁移脚本
```

### 3. 数据库变更

**新增列**:
```sql
ALTER TABLE gateway_routes 
ADD COLUMN security_code VARCHAR(32) NULL 
COMMENT 'Security strategy code (OFF/default/signature/internal)' 
AFTER target;
```

**更新现有数据**:
```sql
UPDATE gateway_routes 
SET security_code = 'default' 
WHERE security_code IS NULL;
```

**迁移文件位置**:
```
loadup-gateway/plugins/repository-database-plugin/
  src/main/resources/db/migration/
    V2__add_security_code_column.sql
```

## CSV 字段顺序（新）

| 索引 | 字段名 | 说明 | 示例 |
|------|--------|------|------|
| 0 | path | 路径 | `/api/test` |
| 1 | method | HTTP 方法 | `GET` |
| 2 | target | 目标地址 | `http://...` 或 `bean://...` |
| 3 | **securityCode** | **认证策略** | `OFF` / `default` / `signature` / `internal` |
| 4 | requestTemplate | 请求模板 | `req.groovy` |
| 5 | responseTemplate | 响应模板 | `resp.groovy` |
| 6 | enabled | 是否启用 | `true` / `false` |
| 7 | properties | 其他属性 | `timeout=30000;retryCount=3` |

## securityCode 取值

| 值 | 说明 | 使用场景 |
|---|------|----------|
| `OFF` | 跳过认证 | 公开接口（登录、注册） |
| `default` | JWT 认证 | 用户接口 |
| `signature` | 签名验签 | Open API、第三方集成 |
| `internal` | 内部调用验证 | 服务间调用 |
| 自定义 | 自定义策略 | 扩展认证方式 |

## 示例配置

### CSV 示例

```csv
path,method,target,securityCode,requestTemplate,responseTemplate,enabled,properties
/api/v1/auth/login,POST,bean://authenticationController:login,OFF,,,true,timeout=30000
/api/v1/users/profile,GET,bean://userController:getProfile,default,,,true,timeout=30000
/open-api/orders,POST,http://localhost:8080/orders,signature,,,true,timeout=60000
/api/internal/cache/clear,POST,bean://cacheService:clear,internal,,,true,
```

### 数据库示例

```sql
INSERT INTO gateway_routes (route_id, path, method, target, security_code, enabled, properties)
VALUES 
  ('route-1', '/api/v1/auth/login', 'POST', 'bean://authenticationController:login', 'OFF', true, 'timeout=30000'),
  ('route-2', '/api/v1/users/profile', 'GET', 'bean://userController:getProfile', 'default', true, 'timeout=30000'),
  ('route-3', '/open-api/orders', 'POST', 'http://localhost:8080/orders', 'signature', true, 'timeout=60000'),
  ('route-4', '/api/internal/cache/clear', 'POST', 'bean://cacheService:clear', 'internal', true, '');
```

## 迁移指南

### 对于 FILE 存储

1. **备份现有 CSV**:
   ```bash
   cp routes.csv routes.csv.backup
   ```

2. **更新 CSV header**:
   ```csv
   path,method,target,securityCode,requestTemplate,responseTemplate,enabled,properties
   ```

3. **调整数据行**:
   - 在 target 后添加 securityCode 列
   - 从 properties 中移除 securityCode

4. **重启应用**验证

### 对于 DATABASE 存储

1. **运行迁移脚本**:
   ```sql
   source V2__add_security_code_column.sql
   ```

2. **迁移数据**（如果 properties 中有 securityCode）:
   ```sql
   -- 从 properties 中提取 securityCode 并更新
   UPDATE gateway_routes
   SET security_code = SUBSTRING_INDEX(SUBSTRING_INDEX(properties, 'securityCode=', -1), ';', 1)
   WHERE properties LIKE '%securityCode=%';
   
   -- 清理 properties 中的 securityCode
   UPDATE gateway_routes
   SET properties = REPLACE(
       REPLACE(properties, CONCAT(';securityCode=', security_code), ''),
       CONCAT('securityCode=', security_code, ';'), ''
   )
   WHERE properties LIKE '%securityCode=%';
   ```

3. **验证数据**:
   ```sql
   SELECT route_id, path, security_code, properties 
   FROM gateway_routes 
   LIMIT 10;
   ```

## 兼容性说明

### 向后兼容

- ✅ **CSV**: 旧格式的 CSV（没有 securityCode 列）会导致解析错误，需要手动迁移
- ✅ **数据库**: 通过迁移脚本平滑升级，现有数据设置默认值 `default`

### 升级步骤

1. 更新代码（已完成）
2. 对于 FILE 存储：手动更新 CSV 文件
3. 对于 DATABASE 存储：运行迁移脚本
4. 重启应用
5. 验证路由加载和认证功能

## 验证清单

- [ ] CSV 文件格式正确（包含 securityCode 列）
- [ ] 数据库迁移脚本已执行（如果使用 DATABASE 存储）
- [ ] 应用能正常启动
- [ ] 路由加载成功（检查日志）
- [ ] 认证功能正常（测试不同 securityCode）
- [ ] SecurityAction 正确读取 routeConfig.getSecurityCode()

## 相关文档

- [GATEWAY_AUTH_DELIVERY.md](../../GATEWAY_AUTH_DELIVERY.md) - Gateway 认证实施总交付
- [loadup-gateway-core/SECURITY.md](../../loadup-gateway/loadup-gateway-core/SECURITY.md) - 认证策略详细文档

## 总结

**核心变化**:
- ✅ `securityCode` 从 properties 提升为独立字段
- ✅ CSV 格式更新（第4列）
- ✅ 数据库表添加 `security_code` 列
- ✅ 代码逻辑同步更新

**优势**:
- 🎯 配置更清晰直观
- 🎯 便于查询和过滤（数据库）
- 🎯 避免 properties 解析开销
- 🎯 强化 securityCode 作为关键配置的地位

---

**重构完成！** 🎉
