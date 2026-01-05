# TestContainers 扩展支持：Redis 和 S3

## 概述

`loadup-components-testcontainers` 模块现在支持三种容器类型：

- 🗄️ **MySQL** - 关系型数据库
- 🔴 **Redis** - 缓存和消息队列
- ☁️ **LocalStack (S3)** - AWS S3 对象存储模拟

---

## Redis 容器支持

### 快速开始

#### 方式 1: 继承抽象基类（推荐）

```java

@SpringBootTest
class RedisCacheTest extends AbstractRedisContainerTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testRedisOperations() {
        redisTemplate.opsForValue().set("key", "value");
        String value = redisTemplate.opsForValue().get("key");
        assertEquals("value", value);
    }
}
```

#### 方式 2: 使用初始化器

```java

@SpringBootTest
@ContextConfiguration(initializers = RedisContainerInitializer.class)
class RedisCacheTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testRedisOperations() {
        // 测试代码
    }
}
```

#### 方式 3: 直接使用共享容器

```java

@SpringBootTest
@TestPropertySource(properties = {
        "spring.redis.host=" + SharedRedisContainer.HOST,
        "spring.redis.port=" + SharedRedisContainer.PORT
})
class RedisCacheTest {
    // 测试代码
}
```

### Redis 配置选项

#### 系统属性配置

```bash
# 更改 Redis 版本
-Dtestcontainers.redis.version=redis:7-alpine
```

#### Maven 配置

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <testcontainers.redis.version>redis:7-alpine</testcontainers.redis.version>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

### Redis API 参考

#### SharedRedisContainer

| 方法/常量           | 返回类型                  | 说明          |
|-----------------|-----------------------|-------------|
| `getInstance()` | `GenericContainer<?>` | 获取容器实例      |
| `getHost()`     | `String`              | 获取 Redis 主机 |
| `getPort()`     | `Integer`             | 获取 Redis 端口 |
| `getUrl()`      | `String`              | 获取连接 URL    |
| `HOST`          | `String`              | 主机常量        |
| `PORT`          | `Integer`             | 端口常量        |
| `URL`           | `String`              | URL 常量      |

### Redis 使用示例

#### 示例 1: Spring Data Redis

```java

@SpringBootTest
class RedisRepositoryTest extends AbstractRedisContainerTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testCacheOperations() {
        // String 操作
        redisTemplate.opsForValue().set("user:1", "John Doe");
        String user = (String) redisTemplate.opsForValue().get("user:1");
        assertThat(user).isEqualTo("John Doe");

        // Hash 操作
        redisTemplate.opsForHash().put("user:profile:1", "name", "John");
        redisTemplate.opsForHash().put("user:profile:1", "age", "30");
        Object name = redisTemplate.opsForHash().get("user:profile:1", "name");
        assertThat(name).isEqualTo("John");

        // List 操作
        redisTemplate.opsForList().rightPush("tasks", "task1");
        redisTemplate.opsForList().rightPush("tasks", "task2");
        Long size = redisTemplate.opsForList().size("tasks");
        assertThat(size).isEqualTo(2);
    }
}
```

#### 示例 2: 缓存注解测试

```java

@SpringBootTest
@EnableCaching
class CacheAnnotationTest extends AbstractRedisContainerTest {
    @Autowired
    private UserService userService;

    @Test
    void testCacheAnnotations() {
        // 首次调用 - 执行方法
        User user1 = userService.getUserById(1L);

        // 第二次调用 - 从缓存获取
        User user2 = userService.getUserById(1L);

        assertThat(user1).isSameAs(user2);
    }
}
```

---

## LocalStack (S3) 容器支持

### 快速开始

#### 方式 1: 继承抽象基类（推荐）

```java

@SpringBootTest
class S3StorageTest extends AbstractLocalStackContainerTest {
    @Autowired
    private S3Client s3Client;

    @Test
    void testS3Operations() {
        // 创建 bucket
        s3Client.createBucket(builder -> builder.bucket("test-bucket"));

        // 上传文件
        s3Client.putObject(request -> request
                        .bucket("test-bucket")
                        .key("test.txt"),
                RequestBody.fromString("Hello S3"));

        // 下载文件
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                request -> request.bucket("test-bucket").key("test.txt"));

        assertThat(response.asUtf8String()).isEqualTo("Hello S3");
    }
}
```

#### 方式 2: 使用初始化器

```java

@SpringBootTest
@ContextConfiguration(initializers = LocalStackContainerInitializer.class)
class S3StorageTest {
    @Autowired
    private S3Client s3Client;

    @Test
    void testS3Upload() {
        // 测试代码
    }
}
```

#### 方式 3: 直接使用共享容器

```java

@SpringBootTest
@TestPropertySource(properties = {
        "aws.s3.endpoint=" + SharedLocalStackContainer.S3_ENDPOINT,
        "aws.access-key-id=" + SharedLocalStackContainer.ACCESS_KEY,
        "aws.secret-access-key=" + SharedLocalStackContainer.SECRET_KEY
})
class S3StorageTest {
    // 测试代码
}
```

### LocalStack 配置选项

#### 系统属性配置

```bash
# 更改 LocalStack 版本
-Dtestcontainers.localstack.version=localstack/localstack:3.0
```

#### S3 Client 配置示例

```java

@Configuration
@TestConfiguration
public class S3TestConfig {
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(SharedLocalStackContainer.getS3Endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                SharedLocalStackContainer.getAccessKey(),
                                SharedLocalStackContainer.getSecretKey()
                        )
                ))
                .region(Region.of(SharedLocalStackContainer.getRegion()))
                .build();
    }
}
```

### LocalStack API 参考

#### SharedLocalStackContainer

| 方法/常量             | 返回类型                  | 说明           |
|-------------------|-----------------------|--------------|
| `getInstance()`   | `LocalStackContainer` | 获取容器实例       |
| `getS3Endpoint()` | `String`              | 获取 S3 端点 URL |
| `getAccessKey()`  | `String`              | 获取访问密钥       |
| `getSecretKey()`  | `String`              | 获取秘密密钥       |
| `getRegion()`     | `String`              | 获取区域         |
| `S3_ENDPOINT`     | `String`              | S3 端点常量      |
| `ACCESS_KEY`      | `String`              | 访问密钥常量       |
| `SECRET_KEY`      | `String`              | 秘密密钥常量       |
| `REGION`          | `String`              | 区域常量         |

### S3 使用示例

#### 示例 1: 文件上传下载

```java

@SpringBootTest
class S3FileOperationsTest extends AbstractLocalStackContainerTest {
    private              S3Client s3Client;
    private static final String   BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(getS3Endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(getAccessKey(), getSecretKey())
                ))
                .region(Region.of(getRegion()))
                .build();

        // 创建测试 bucket
        s3Client.createBucket(b -> b.bucket(BUCKET_NAME));
    }

    @Test
    void testUploadAndDownload() throws IOException {
        // 上传文件
        String content = "Test file content";
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key("test/file.txt")
                        .build(),
                RequestBody.fromString(content)
        );

        // 下载文件
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key("test/file.txt")
                        .build()
        );

        assertThat(response.asUtf8String()).isEqualTo(content);
    }
}
```

#### 示例 2: DFS 组件集成测试

```java

@SpringBootTest
class DfsS3ProviderTest extends AbstractLocalStackContainerTest {
    @Autowired
    private DfsService dfsService;

    @Test
    void testS3FileStorage() {
        // 上传文件
        FileUploadRequest request = FileUploadRequest.builder()
                .filename("test.txt")
                .inputStream(new ByteArrayInputStream("content".getBytes()))
                .contentType("text/plain")
                .build();

        FileMetadata metadata = dfsService.upload(request, "s3");

        // 验证文件存在
        boolean exists = dfsService.exists(metadata.getFileId(), "s3");
        assertThat(exists).isTrue();

        // 下载文件
        FileDownloadResponse response = dfsService.download(metadata.getFileId(), "s3");
        assertThat(response).isNotNull();
    }
}
```

---

## 组合使用多种容器

### 同时使用 MySQL 和 Redis

```java

@SpringBootTest
@ContextConfiguration(initializers = {
        MySQLContainerInitializer.class,
        RedisContainerInitializer.class
})
class MultiContainerTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testDatabaseAndCache() {
        // 测试数据库
        // ...

        // 测试缓存
        // ...
    }
}
```

### 同时使用 MySQL、Redis 和 S3

```java

@SpringBootTest
@ContextConfiguration(initializers = {
        MySQLContainerInitializer.class,
        RedisContainerInitializer.class,
        LocalStackContainerInitializer.class
})
class FullStackTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private S3Client s3Client;

    @Test
    void testFullStack() {
        // 完整的端到端测试
    }
}
```

---

## 性能对比

### 容器启动时间

| 容器类型       | 首次启动 | 后续启动（共享） | 性能提升       |
|------------|------|----------|------------|
| MySQL      | ~8秒  | ~1秒      | **87% ⬆️** |
| Redis      | ~3秒  | <1秒      | **90% ⬆️** |
| LocalStack | ~15秒 | ~1秒      | **93% ⬆️** |

### 资源消耗

| 场景               | 传统方式  | 共享容器 | 节省         |
|------------------|-------|------|------------|
| 10个测试类（每个用MySQL） | 10个容器 | 1个容器 | **90% ⬇️** |
| 10个测试类（每个用Redis） | 10个容器 | 1个容器 | **90% ⬇️** |
| 混合使用             | 20个容器 | 3个容器 | **85% ⬇️** |

---

## 故障排除

### Redis 连接问题

**问题**: 无法连接到 Redis
**解决**:

```java
// 检查容器状态
GenericContainer<?> redis = SharedRedisContainer.getInstance();
System.out.

println("Redis Running: "+redis.isRunning());
        System.out.

println("Redis Host: "+SharedRedisContainer.getHost());
        System.out.

println("Redis Port: "+SharedRedisContainer.getPort());
```

### LocalStack S3 问题

**问题**: S3 操作失败
**解决**:

```java
// 验证端点配置
System.out.println("S3 Endpoint: "+SharedLocalStackContainer.getS3Endpoint());

// 确保使用正确的 Region
S3Client s3Client = S3Client.builder()
        .region(Region.of(SharedLocalStackContainer.getRegion()))
        .forcePathStyle(true) // LocalStack 需要
        .build();
```

---

## 总结

### 支持的容器

| 容器         | 用途        | 默认版本           | 端口 |
|------------|-----------|----------------|----|
| MySQL      | 关系型数据库    | mysql:8.0      | 随机 |
| Redis      | 缓存/消息队列   | redis:7-alpine | 随机 |
| LocalStack | AWS S3 模拟 | localstack:3.0 | 随机 |

### 核心优势

1. **统一管理** - 一个模块提供所有容器支持
2. **性能卓越** - 共享容器大幅减少启动时间
3. **易于使用** - 继承基类即可，零配置
4. **灵活扩展** - 易于添加新的容器类型

---

**版本**: 1.0.0-SNAPSHOT  
**最后更新**: 2026-01-05

