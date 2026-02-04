# LoadUp Commons

## 概述

LoadUp Commons 提供了项目中通用的基础工具类、DTO 和 API 定义。

## 模块结构

```
loadup-commons/
├── loadup-commons-api/       # 通用 API 定义
├── loadup-commons-dto/       # 通用 DTO 和响应封装
└── loadup-commons-util/      # 工具类集合
```

## loadup-commons-api

提供通用的 API 接口定义和常量。

**主要内容**:
- 基础 CRUD 接口定义
- 通用常量定义
- 枚举类型

**使用示例**:
```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-api</artifactId>
</dependency>
```

## loadup-commons-dto

提供统一的响应封装和通用 DTO。

**核心类**:
- `Result<T>` - 统一响应封装
- `PageResult<T>` - 分页响应
- `BaseDTO` - DTO 基类

**使用示例**:
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public Result<UserDTO> getUser(@PathVariable String id) {
        UserDTO user = userService.getUser(id);
        return Result.success(user);
    }
    
    @GetMapping("/users")
    public PageResult<UserDTO> listUsers(PageQuery query) {
        Page<UserDTO> page = userService.listUsers(query);
        return PageResult.success(page);
    }
}
```

## loadup-commons-util

提供各种工具类。

**主要工具类**:
- `JwtUtils` - JWT 工具
- `DateUtils` - 日期处理
- `StringUtils` - 字符串工具
- `JsonUtils` - JSON 序列化
- `BeanUtils` - Bean 复制
- `ValidationUtils` - 参数校验

**使用示例**:
```java
// JWT 生成和解析
String token = JwtUtils.generateToken(userId, secret, expiration);
Claims claims = JwtUtils.parseToken(token, secret);

// 日期处理
LocalDateTime now = DateUtils.now();
String formatted = DateUtils.format(now, "yyyy-MM-dd HH:mm:ss");

// JSON 处理
String json = JsonUtils.toJson(object);
UserDTO user = JsonUtils.fromJson(json, UserDTO.class);
```

## 依赖关系

```
loadup-commons-api (无外部依赖)
loadup-commons-dto (依赖 commons-api)
loadup-commons-util (依赖 commons-dto)
```

## 相关文档

- [详细文档](../docs/commons.md)
- [API 文档](../docs/commons/commons-api.md)
- [DTO 文档](../docs/commons/commons-dto.md)
- [工具类文档](../docs/commons/commons-util.md)
