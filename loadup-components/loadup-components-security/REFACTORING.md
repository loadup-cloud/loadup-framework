# LoadUp Security 组件重构总结

## 重构背景

在单体应用架构下，采用 Gateway 负责认证、Security 组件负责授权的方案。

## 组件定位变更

### 之前（❌ 已废弃）
- 包含完整的 Spring Security Filter Chain
- 在 DispatcherServlet 之前进行认证
- 包含 JWT Filter、异常处理器等

### 现在（✅ 推荐）
**仅提供方法级权限控制**
- 启用 `@PreAuthorize` / `@PostAuthorize` 注解
- 提供 `LoadUpUser` 用户模型
- 提供 `SecurityHelper` 工具类
- **不包含认证逻辑**（由 Gateway 完成）

## 文件结构对比

### 删除的文件
```
❌ config/LoadUpSecurityProperties.java      # 配置属性（白名单）
❌ filter/InnerAuthenticationFilter.java     # 认证过滤器
❌ handler/RestAccessDeniedHandler.java      # 403 处理器
❌ handler/RestAuthenticationEntryPoint.java # 401 处理器
```

### 保留并重构的文件
```
✅ config/SecurityAutoConfiguration.java     # 简化为只启用方法注解
✅ core/LoadUpUser.java                      # 简化为独立模型
✅ util/SecurityHelper.java                  # 保持不变
```

### 新增文件
```
✅ README.md                                 # 组件文档
✅ example/UserServiceExample.java           # 使用示例
```

## 依赖变更

### 移除的依赖
```xml
❌ spring-boot-starter-security
❌ spring-boot-starter-web
❌ loadup-commons-dto
❌ loadup-commons-util
❌ jakarta.servlet-api
```

### 保留的依赖
```xml
✅ spring-security-core
✅ spring-security-config
✅ spring-context
✅ lombok
```

## 工作流程

```
Client Request
    ↓
Gateway (认证)
    ├─ JWT 验证
    ├─ 签名验签
    └─ 内部调用识别
    ↓
填充 SecurityContext
    ↓
业务方法
    ├─ @PreAuthorize 检查 ← (Security 组件)
    └─ 业务逻辑执行
```

## 使用示例

### 1. 在 UPMS 模块中引入
```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-security</artifactId>
</dependency>
```

### 2. 在 Service 中使用注解
```java
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        // 只有 ADMIN 可以删除
    }
    
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public void updateUser(String userId, UserDTO dto) {
        // ADMIN 或本人可以修改
    }
}
```

### 3. 获取当前用户
```java
String userId = SecurityHelper.getCurUserId();
String userName = SecurityHelper.getCurUserName();
LoadUpUser user = SecurityHelper.getCurUser();
```

## Gateway 需要做的事

在将请求转发到业务 Bean 之前，填充 SecurityContext：

```java
// 在 Gateway 的 ActionDispatcher 中
LoadUpUser user = LoadUpUser.builder()
    .userId("123")
    .username("admin")
    .roles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
    .build();

Authentication auth = new UsernamePasswordAuthenticationToken(
    user, null, user.getAuthorities()
);
SecurityContextHolder.getContext().setAuthentication(auth);

// 然后转发到业务 Bean
```

## 优势

1. ✅ **职责清晰**：Security 组件只负责授权，不负责认证
2. ✅ **轻量化**：移除了不必要的依赖和代码
3. ✅ **灵活性**：Gateway 可以支持多种认证方式
4. ✅ **标准化**：使用 Spring Security 标准注解
5. ✅ **可测试**：Mock SecurityContext 即可测试权限逻辑

## 注意事项

1. **SecurityContext 必须在业务方法前填充**
   - 由 Gateway 在转发前完成
   
2. **角色命名规范**
   - 数据库存储：`ADMIN` / `USER`
   - 注解使用：`hasRole('ADMIN')` (Spring Security 会自动加 `ROLE_` 前缀)
   - 或直接使用：`hasAuthority('ROLE_ADMIN')`

3. **线程安全**
   - `SecurityContextHolder` 使用 `ThreadLocal`
   - JDK 21 虚拟线程环境下也是安全的
   - 异步操作需要手动传递（使用 `DelegatingSecurityContextExecutor`）

## 下一步工作

1. ✅ 完成 Security 组件重构
2. 🔄 在 Gateway 中实现认证逻辑（方案 A）
3. 🔄 在 Gateway 的 Bean Proxy 中填充 SecurityContext
4. 🔄 在 UPMS Controller/Service 中添加权限注解

## 相关文档

- [README.md](./README.md) - 组件使用文档
- [UserServiceExample.java](./src/main/java/io/github/loadup/components/security/example/UserServiceExample.java) - 使用示例
