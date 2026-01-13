# LoadUp Commons Util

## 📋 概述

LoadUp Commons Util 提供了企业级的通用工具类和辅助功能，包含 JSON 处理、ID 生成、日期时间处理、验证工具、上下文工具等。

## 🎯 功能特性

- ✅ **JSON 工具** - 基于 Jackson 的类型安全 JSON 处理
- ✅ **ID 生成器** - UUID、随机数等多种 ID 生成方式
- ✅ **日期时间工具** - 日期格式化、解析和时长计算
- ✅ **验证工具** - 邮箱、手机号、身份证等常见验证
- ✅ **密码工具** - 密码生成和加密
- ✅ **上下文工具** - Spring 上下文和运行时环境工具
- ✅ **内部线程** - 高性能线程本地变量支持
- ✅ **对象工具** - 对象操作和转换

## 📦 Maven 依赖

```xml
<dependency>
    <groupId>com.github.loadup.commons</groupId>
    <artifactId>loadup-commons-util</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```

## 🚀 核心工具类

### 1. JsonUtil - JSON 处理工具 ⭐

企业级 JSON 处理工具，基于 Jackson 实现，提供类型安全、完整验证和详细日志记录。

**特性:**
- ✅ 类型安全（无原始类型警告）
- ✅ 完整的参数验证和空值检查
- ✅ 支持泛型、集合类型转换
- ✅ 支持文件和流操作
- ✅ JSON 节点操作
- ✅ 详尽的错误日志

#### 基本序列化

```java
import com.github.loadup.commons.util.JsonUtil;

// 对象转 JSON
User user = new User("张三", 25);
String json = JsonUtil.toJsonString(user);

// 格式化输出
String prettyJson = JsonUtil.toJsonStringPretty(user);
```

#### 基本反序列化

```java
// JSON 转对象
String json = "{\"name\":\"张三\",\"age\":25}";
User user = JsonUtil.parseObject(json, User.class);

// JSON 转 List
String jsonArray = "[{\"name\":\"张三\"},{\"name\":\"李四\"}]";
List<User> users = JsonUtil.parseObject(jsonArray, List.class, User.class);

// JSON 转 Map
String jsonMap = "{\"user1\":{\"name\":\"张三\"},\"user2\":{\"name\":\"李四\"}}";
Map<String, User> userMap = JsonUtil.parseObject(jsonMap, Map.class, String.class, User.class);

// 使用 TypeReference（支持复杂泛型）
List<User> users = JsonUtil.parseObject(jsonArray, new TypeReference<List<User>>() {});
```

#### Map 操作

```java
// JSON 转 Map
String json = "{\"name\":\"张三\",\"age\":25}";
Map<String, Object> map = JsonUtil.toMap(json);
Map<String, String> strMap = JsonUtil.jsonToMap(json);

// Map 转对象
Map<String, Object> map = new HashMap<>();
map.

put("name","张三");
map.

put("age",25);

User user = JsonUtil.mapToObject(map, User.class);
```

#### 文件操作

```java
// 从文件读取
User user = JsonUtil.parseObject(new File("user.json"), User.class);

// 写入文件（自动创建父目录）
JsonUtil.

toFile(new File("output/user.json"),user);
```

#### 流操作

```java
// 从输入流读取
InputStream inputStream = new FileInputStream("user.json");
User user = JsonUtil.parseObject(inputStream, User.class);
```

#### JSON 节点操作

```java
// 创建 JSON 对象/数组
ObjectNode jsonObject = JsonUtil.createEmptyJsonObject();
ArrayNode jsonArray = JsonUtil.createEmptyJsonArray();

// 添加元素到数组
JsonUtil.

addElementToJsonArray(jsonArray, user);

// 获取子节点
String json = "{\"data\":{\"user\":{\"name\":\"张三\"}}}";
JsonNode userNode = JsonUtil.getSubNode(json, "/data/user");

// 解析为 JsonNode 树
JsonNode tree = JsonUtil.toJsonNodeTree(json);
```

**日志配置建议:**

```yaml
# application.yml
logging:
  level:
    com.github.loadup.commons.util.JsonUtil: WARN  # 生产环境
    # com.github.loadup.commons.util.JsonUtil: DEBUG  # 开发环境
```

---

### 2. IdUtils - ID 生成工具

提供多种 ID 生成方式，适用于不同场景。

```java
import com.github.loadup.commons.util.IdUtils;

// 生成 UUID（带分隔符）
String uuid = IdUtils.uuid();
        // 示例: "550e8400-e29b-41d4-a716-446655440000"

        // 生成 UUID（无分隔符）
        String uuid2 = IdUtils.uuid2();
        // 示例: "550e8400e29b41d4a716446655440000"

        // 生成随机 Long
        long randomId = IdUtils.randomLong();

        // 获取随机数生成器（用于自定义场景）
        SecureRandom random = IdUtils.getSecureRandom();
```

**使用场景:**

- `uuid()` - 分布式系统主键、请求追踪 ID
- `uuid2()` - 需要紧凑格式的场景
- `randomLong()` - 数值型 ID、订单号

---

### 3. DateUtils - 日期时间工具

日期时间处理和格式化工具。

```java
import com.github.loadup.commons.util.date.DateUtils;
import java.util.Date;
import java.time.LocalDateTime;

// 日期格式化
String dateStr = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

        // 日期解析
        Date date = DateUtils.parse("2026-01-06 12:30:00", "yyyy-MM-dd HH:mm:ss");

        // 获取当前时间
        Date now = DateUtils.now();
        LocalDateTime localNow = DateUtils.localNow();

        // 日期计算
        Date tomorrow = DateUtils.addDays(new Date(), 1);
        Date nextWeek = DateUtils.addWeeks(new Date(), 1);
        Date nextMonth = DateUtils.addMonths(new Date(), 1);

        // 日期比较
        boolean isBefore = DateUtils.isBefore(date1, date2);
        boolean isAfter = DateUtils.isAfter(date1, date2);

        // 获取日期部分
        int year = DateUtils.getYear(date);
        int month = DateUtils.getMonth(date);
        int day = DateUtils.getDay(date);
```

---

### 4. DurationUtils - 时长工具

时长解析和格式化工具，支持多种时长格式。

```java
import com.github.loadup.commons.util.date.DurationUtils;
import java.time.Duration;

// 解析时长字符串
Duration duration = DurationUtils.parse("2h30m");  // 2小时30分钟
Duration duration2 = DurationUtils.parse("PT2H30M");  // ISO-8601 格式

// 支持的格式:
// - "1d" / "1day" / "1days" - 天
// - "2h" / "2hour" / "2hours" - 小时
// - "30m" / "30min" / "30minute" - 分钟
// - "45s" / "45sec" / "45second" - 秒
// - "1d2h30m" - 组合格式
// - "PT2H30M" - ISO-8601 标准格式

// 格式化输出
String formatted = DurationUtils.format(duration);
```

**使用场景:**

- 配置文件中的超时时间设置
- 任务执行时长计算
- 缓存过期时间设置

---

### 5. ValidateUtils - Bean 验证工具

基于 Jakarta Bean Validation (JSR-380) 的对象验证工具。

```java
import com.github.loadup.commons.util.ValidateUtils;
import jakarta.validation.constraints.*;

// 定义验证对象

public class User {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 18, message = "年龄不能小于18岁")
    private Integer age;
}

// 验证对象（失败抛出异常）
User user = new User();
try{
        ValidateUtils.

validate(user);
}catch(
ValidationException e){
        System.out.

println("验证失败: "+e.getMessage());
        }

// 检查对象是否有效
boolean isValid = ValidateUtils.isValid(user);

// 获取所有验证错误
Set<ConstraintViolation<User>> violations = ValidateUtils.getViolations(user);
violations.

forEach(v ->{
String errorMsg = ValidateUtils.getErrorMessage(v);
    System.out.

println(errorMsg);
});

// 格式化所有错误为单个字符串
String allErrors = ValidateUtils.formatViolations(violations);

// 获取 Validator 实例（高级用法）
Validator validator = ValidateUtils.getValidator();
```

**支持的验证注解:**

- `@NotNull` - 非空
- `@NotBlank` - 非空字符串
- `@NotEmpty` - 非空集合
- `@Size` - 大小范围
- `@Min / @Max` - 数值范围
- `@Email` - 邮箱格式
- `@Pattern` - 正则表达式
- 更多见 Jakarta Validation 规范

---

### 6. PasswordUtils - 密码加密工具

基于 Spring Security 的字符串加密解密工具，使用 AES-256 加密算法。

```java
import com.github.loadup.commons.util.PasswordUtils;

// 生成随机盐值（8位）
String salt = PasswordUtils.getRandomSalt();  // 例如: "12345678"

// 加密字符串
String plainText = "敏感数据123";
String password = "mySecretKey";
String encrypted = PasswordUtils.encrypt(plainText, password, salt);

// 解密字符串（需要相同的密码和盐值）
String decrypted = PasswordUtils.decrypt(encrypted, password, salt);

// 验证盐值是否有效
boolean isValid = PasswordUtils.isValidSalt(salt);
```

**重要说明:**

- 盐值必须为 8 位字符串
- 解密时必须使用与加密时相同的密码和盐值
- 加密结果为 Base64 编码字符串
- 解密失败返回 null

**使用场景:**

- 配置文件中的敏感信息加密
- 数据库字段加密
- API 密钥加密存储

---

### 7. ApplicationContextUtils - Spring 上下文工具

Spring 应用上下文访问工具。

```java
import com.github.loadup.commons.util.context.ApplicationContextUtils;

// 获取 Bean
UserService userService = ApplicationContextUtils.getBean(UserService.class);
        UserService userService2 = ApplicationContextUtils.getBean("userService", UserService.class);

        // 获取应用上下文
        ApplicationContext context = ApplicationContextUtils.getApplicationContext();

        // 获取配置属性
        String appName = ApplicationContextUtils.getProperty("spring.application.name");
        String appName2 = ApplicationContextUtils.getProperty("app.name", "default");

        // 获取环境信息
        Environment environment = ApplicationContextUtils.getEnvironment();

        // 检查 Profile
        boolean isDev = ApplicationContextUtils.isProfileActive("dev");

        // 发布事件
ApplicationContextUtils.

        publishEvent(new CustomEvent(this));
```

---

### 8. RuntimeUtils - 运行时工具

运行时环境和系统信息工具。

```java
import com.github.loadup.commons.util.context.RuntimeUtils;

// 获取 CPU 核心数
int cpuCount = RuntimeUtils.getCpuCount();

        // 获取内存信息
        long totalMemory = RuntimeUtils.getTotalMemory();
        long freeMemory = RuntimeUtils.getFreeMemory();
        long usedMemory = RuntimeUtils.getUsedMemory();

        // 获取系统信息
        String osName = RuntimeUtils.getOsName();
        String osVersion = RuntimeUtils.getOsVersion();
        String javaVersion = RuntimeUtils.getJavaVersion();

        // 获取当前进程 PID
        long pid = RuntimeUtils.getPid();

        // 执行垃圾回收
RuntimeUtils.

        gc();
```

---

### 9. ObjectUtil - 对象工具

对象操作和转换工具，提供 null 安全的方法。

```java
import com.github.loadup.commons.util.ObjectUtil;

// 空值检查
boolean isNull = ObjectUtil.isNull(obj);
        boolean isNotNull = ObjectUtil.isNotNull(obj);

        // 空值返回默认值
        String value = ObjectUtil.defaultIfNull(str, "default");

        // 使用 Supplier 提供默认值（懒加载）
        String value2 = ObjectUtil.defaultIfNull(str, () -> getDefaultValue());

        // 对象比较（null安全）
        boolean equals = ObjectUtil.equals(obj1, obj2);

        // 对象哈希码（null安全）
        int hash = ObjectUtil.hashCode(obj);

        // 对象转字符串（null安全）
        String str = ObjectUtil.toString(obj);
        String str2 = ObjectUtil.toString(obj, "null对象");

        // 要求对象不为null，否则抛出异常
        User user = ObjectUtil.requireNonNull(maybeNullUser);
        User user2 = ObjectUtil.requireNonNull(maybeNullUser, "User cannot be null");

        // 检查是否为空（null或空字符串）
        boolean isEmpty = ObjectUtil.isEmpty(obj);
        boolean isNotEmpty = ObjectUtil.isNotEmpty(obj);

        // 安全类型转换
        String str = ObjectUtil.cast(obj, String.class);  // 失败返回null
```

**特性:**

- 所有方法都是 null 安全的
- 支持 Supplier 懒加载默认值
- 提供类型安全的转换方法

---

### 10. AnnotationUtils - 注解工具

注解处理和扫描工具，提供便捷的注解查找和处理方法。

```java
import com.github.loadup.commons.util.AnnotationUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// 查找类中所有带指定注解的方法
List<Method> methods = AnnotationUtils.findMethods(MyClass.class, MyAnnotation.class);

        // 查找类中所有带指定注解的字段
        List<Field> fields = AnnotationUtils.findFields(MyClass.class, MyAnnotation.class);

        // 检查类是否有指定注解
        boolean hasAnnotation = AnnotationUtils.hasAnnotation(MyClass.class, MyAnnotation.class);

        // 检查方法是否有指定注解
        Method method = MyClass.class.getMethod("myMethod");
        boolean hasMethodAnnotation = AnnotationUtils.hasAnnotation(method, MyAnnotation.class);

        // 检查字段是否有指定注解
        Field field = MyClass.class.getDeclaredField("myField");
        boolean hasFieldAnnotation = AnnotationUtils.hasAnnotation(field, MyAnnotation.class);

        // 获取类上的注解实例
        MyAnnotation annotation = AnnotationUtils.getAnnotation(MyClass.class, MyAnnotation.class);

        // 获取方法上的注解实例
        MyAnnotation methodAnnotation = AnnotationUtils.getAnnotation(method, MyAnnotation.class);

        // 获取字段上的注解实例
        MyAnnotation fieldAnnotation = AnnotationUtils.getAnnotation(field, MyAnnotation.class);

        // 获取类上的所有注解
        Annotation[] annotations = AnnotationUtils.getAnnotations(MyClass.class);
```

**使用场景:**

- 框架开发中的注解扫描
- 自定义注解处理器
- 反射操作简化

---

### 11. InternalThread - 内部线程（高性能）

提供快速访问 InternalThreadLocal 变量的特殊用途线程。

```java
import com.github.loadup.commons.util.internal.InternalThread;
import com.github.loadup.commons.util.internal.InternalThreadLocalMap;

// 创建内部线程
InternalThread thread = new InternalThread(() -> {
    // 线程执行逻辑
    System.out.println("Running in InternalThread");
});

        // 带名称的线程
        InternalThread namedThread = new InternalThread("MyWorker");

        // 带线程组的线程
        ThreadGroup group = new ThreadGroup("Workers");
        InternalThread groupThread = new InternalThread(group, runnable, "Worker-1");

        // 访问线程本地映射
        InternalThreadLocalMap map = thread.threadLocalMap();
thread.

        setThreadLocalMap(new InternalThreadLocalMap());
```

**使用场景:**

- 高性能服务器（如 Netty）
- 需要频繁访问 ThreadLocal 的场景
- 自定义线程池

**注意事项:**

- `threadLocalMap()` 和 `setThreadLocalMap()` 仅供内部使用
- 适用于性能敏感的底层框架

---

## 📝 完整使用示例

### 示例 1: 用户注册流程

```java
import com.github.loadup.commons.util.*;
import com.github.loadup.commons.util.date.DateUtils;

public class UserService {

    public Result registerUser(UserRegisterDTO dto) {
        // 1. Bean 验证
        try {
            ValidateUtils.validate(dto);
        } catch (ValidationException e) {
            return Result.fail("输入验证失败: " + e.getMessage());
        }

        // 2. 生成用户ID
        String userId = IdUtils.uuid2();

        // 3. 加密密码（使用 PasswordUtils）
        String salt = PasswordUtils.getRandomSalt();
        String encryptedPassword = PasswordUtils.encrypt(
                dto.getPassword(),
                "APP_SECRET_KEY",
                salt
        );

        // 4. 创建用户对象
        User user = new User();
        user.setId(userId);
        user.setEmail(dto.getEmail());
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        user.setCreateTime(DateUtils.now());

        // 5. 保存用户
        userRepository.save(user);

        // 6. 记录日志（转 JSON）
        String userJson = JsonUtil.toJson(user);
        log.info("User registered: {}", userJson);

        return Result.success(user);
    }
}
```

### 示例 2: 配置加密存储

```java
import com.github.loadup.commons.util.*;

public class ConfigService {
    
    private static final String SALT = "12345678";  // 生产环境应从配置读取
    
    public void saveEncryptedConfig(String key, String value) {
        // 加密配置值
        String encrypted = PasswordUtils.encrypt(value, "CONFIG_KEY", SALT);
        
        // 保存到数据库
        configRepository.save(key, encrypted);
        
        log.info("Saved encrypted config: {}", key);
    }
    
    public String getDecryptedConfig(String key) {
        // 从数据库读取
        String encrypted = configRepository.get(key);
        
        if (ObjectUtil.isEmpty(encrypted)) {
            return null;
        }
        
        // 解密
        String decrypted = PasswordUtils.decrypt(encrypted, "CONFIG_KEY", SALT);
        return decrypted;
    }
}
```

        String hashedPassword = PasswordUtils.encode(password);

        // 4. 创建用户对象
        User user = new User();
        user.setId(userId);
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(hashedPassword);
        user.setCreateTime(DateUtils.now());

        // 5. 保存用户
        userRepository.save(user);

        // 6. 记录日志（转 JSON）
        String userJson = JsonUtil.toJsonString(user);
        log.info("User registered: {}", userJson);

        return Result.success(user);
    }

}
```

### 示例 2: 配置管理

```java
import com.github.loadup.commons.util.context.ApplicationContextUtils;

public class ConfigService {

    public void loadConfig() {
        // 从 Spring 配置读取
        String apiUrl = ApplicationContextUtils.getProperty("api.base.url");
        int timeout = ApplicationContextUtils.getProperty("api.timeout", 30);

        // 检查环境
        if (ApplicationContextUtils.isProfileActive("prod")) {
            // 生产环境特殊处理
            log.info("Running in production mode");
        }

        // 获取 Bean
        RedisTemplate redis = ApplicationContextUtils.getBean(RedisTemplate.class);
    }
}
```

### 示例 3: 数据导入导出

```java
import com.github.loadup.commons.util.*;

public class DataExportService {

    public void exportUsers(List<User> users, File outputFile) {
        // 转换为 JSON 并写入文件
        JsonUtil.toFile(outputFile, users);
        log.info("Exported {} users to {}", users.size(), outputFile.getPath());
    }

    public List<User> importUsers(File inputFile) {
        // 从文件读取 JSON
        List<User> users = JsonUtil.parseObject(
                inputFile,
                List.class,
                User.class
        );

        // 验证数据
        users.forEach(user -> {
            if (!ValidateUtils.isEmail(user.getEmail())) {
                throw new IllegalArgumentException("Invalid email: " + user.getEmail());
            }
        });

        return users;
    }
}
```

### 示例 4: 性能监控

```java
import com.github.loadup.commons.util.context.RuntimeUtils;

public class MonitorService {

    public SystemInfo getSystemInfo() {
        SystemInfo info = new SystemInfo();
        info.setCpuCount(RuntimeUtils.getCpuCount());
        info.setTotalMemory(RuntimeUtils.getTotalMemory());
        info.setFreeMemory(RuntimeUtils.getFreeMemory());
        info.setUsedMemory(RuntimeUtils.getUsedMemory());
        info.setOsName(RuntimeUtils.getOsName());
        info.setJavaVersion(RuntimeUtils.getJavaVersion());
        info.setPid(RuntimeUtils.getPid());
        return info;
    }

    public void checkMemoryUsage() {
        long usedMemory = RuntimeUtils.getUsedMemory();
        long totalMemory = RuntimeUtils.getTotalMemory();
        double usage = (double) usedMemory / totalMemory;

        if (usage > 0.8) {
            log.warn("High memory usage: {}%", usage * 100);
            RuntimeUtils.gc();  // 触发垃圾回收
        }
    }
}
```

---

## 🔧 高级配置

### JSON 工具配置

JsonUtil 使用预配置的 ObjectMapper，如需自定义:

```java
import com.github.loadup.commons.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

// 获取 ObjectMapper 实例
ObjectMapper mapper = JsonUtil.getObjectMapper();

        // 自定义配置
mapper.

        configure(SerializationFeature.INDENT_OUTPUT, true);
```

### 日志配置

```yaml
# application.yml
logging:
  level:
    # JSON 工具日志
    com.github.loadup.commons.util.JsonUtil: WARN

    # 其他工具类日志
    com.github.loadup.commons.util: INFO
```

### 线程池配置（使用 InternalThread）

```java
import com.github.loadup.commons.util.internal.InternalThread;

ThreadPoolExecutor executor = new ThreadPoolExecutor(
        10, 20, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        (runnable) -> new InternalThread(runnable, "Worker")
);
```

---

## 📊 性能优化建议

### 1. JSON 处理

- ✅ 使用 `parseObject(String, Class)` 而非反射
- ✅ 大文件使用流式处理 `parseObject(InputStream, Class)`
- ✅ 重复序列化考虑缓存结果

### 2. ID 生成

- ✅ 高并发场景使用 `randomLong()` 而非 `uuid()`
- ✅ 分布式系统使用 UUID 避免冲突

### 3. 日期处理

- ✅ 频繁格式化使用 `SimpleDateFormat` 缓存
- ✅ Java 8+ 优先使用 `LocalDateTime`

### 4. 线程本地变量

- ✅ 高性能场景使用 `InternalThread`
- ✅ 避免线程本地变量泄漏

---

## ⚠️ 注意事项

### JsonUtil 迁移指南

如果从旧版本升级，注意以下变更:

| 旧方法                       | 新方法                                | 说明    |
|---------------------------|------------------------------------|-------|
| `toJSONString()`          | `toJsonString()`                   | 方法重命名 |
| `parseObject(Map, Class)` | `mapToObject(Map, Class)`          | 类型安全  |
| `toMap()` 返回 `Map`        | `toMap()` 返回 `Map<String, Object>` | 类型安全  |

### 线程安全

- ✅ 所有工具类方法都是线程安全的
- ⚠️ `InternalThread` 的 `threadLocalMap` 是线程独立的

### 内存管理

- ⚠️ 大量使用 `JsonUtil.toJsonString()` 注意内存占用
- ⚠️ 使用 `InternalThread` 确保清理 ThreadLocal

---

## 🔗 相关链接

- [Jackson 官方文档](https://github.com/FasterXML/jackson)
- [Java Date/Time API](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
- [Spring Framework](https://spring.io/projects/spring-framework)

---

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0)

```
Copyright (C) 2026 LoadUp Cloud

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

详见 [LICENSE](../../LICENSE) 文件。

---

**最后更新**: 2026-01-06  
**版本**: 1.0.0-SNAPSHOT
