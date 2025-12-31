# LoadUp Cache 测试文档

## 测试模块结构

```
loadup-components-cache-test/
├── src/test/java/com/github/loadup/components/cache/
│   ├── CacheTestApplication.java              # 测试应用启动类
│   ├── common/                                 # 通用测试基类和工具
│   │   ├── BaseCacheTest.java                 # 测试基类
│   │   └── model/                             # 测试实体类
│   │       ├── User.java                      # 用户实体
│   │       └── Product.java                   # 商品实体
│   ├── caffeine/                              # Caffeine 缓存测试
│   │   ├── CaffeineBasicOperationsTest.java   # 基础操作测试
│   │   ├── CaffeineExpirationTest.java        # 过期策略测试
│   │   ├── CaffeineConcurrencyTest.java       # 并发测试
│   │   └── AntiAvalancheTest.java             # 防雪崩测试
│   ├── redis/                                 # Redis 缓存测试（待实现）
│   └── integration/                           # 集成测试（待实现）
└── src/test/resources/
    └── application-test.properties            # 测试配置
```

## 测试用例说明

### 1. 基础操作测试 (CaffeineBasicOperationsTest)

测试缓存的基本 CRUD 操作：

- ✅ 基本的 set 和 get 操作
- ✅ 获取不存在的 key
- ✅ delete 操作
- ✅ deleteAll 操作
- ✅ 覆盖已存在的 key
- ✅ 不同类型对象的缓存
- ✅ String 类型值的缓存
- ✅ 批量操作
- ✅ 空值处理

**运行方式：**

```bash
mvn test -Dtest=CaffeineBasicOperationsTest
```

### 2. 过期策略测试 (CaffeineExpirationTest)

测试各种过期策略：

- ✅ 写入后过期 (expire-after-write)
- ✅ 访问后过期 (expire-after-access)
- ✅ 最大容量淘汰策略
- ✅ 每个 cache 的独立配置
- ✅ 随机过期偏移
- ✅ 缓存刷新

**运行方式：**

```bash
mvn test -Dtest=CaffeineExpirationTest
```

**注意事项：**

- 测试包含等待操作，执行时间较长
- 使用 Awaitility 库进行异步验证

### 3. 并发测试 (CaffeineConcurrencyTest)

测试高并发场景下的缓存行为：

- ✅ 并发写入
- ✅ 并发读取
- ✅ 并发读写混合
- ✅ 并发删除
- ✅ 高并发场景下的一致性
- ✅ 缓存性能压测

**运行方式：**

```bash
mvn test -Dtest=CaffeineConcurrencyTest
```

**性能指标：**

- 支持至少 1000 ops/sec
- 多线程并发无数据丢失
- 多线程并发无数据损坏

### 4. 防雪崩测试 (AntiAvalancheTest)

测试防缓存雪崩和击穿策略：

- ✅ 随机过期时间防止缓存雪崩
- ✅ 热点数据长过期时间防止击穿
- ✅ 高优先级数据不易被淘汰
- ✅ 批量数据的分散过期
- ✅ 模拟缓存雪崩场景

**运行方式：**

```bash
mvn test -Dtest=AntiAvalancheTest
```

**策略验证：**

- 随机偏移使过期时间分散
- 热点数据使用更长的过期时间
- 高优先级数据优先保留

## 运行所有测试

### 运行所有 Caffeine 测试

```bash
mvn test -Dtest=com.github.loadup.components.cache.caffeine.*Test
```

### 运行所有测试

```bash
mvn test
```

### 运行测试并生成报告

```bash
mvn clean test jacoco:report
```

## 测试配置

### 默认测试配置 (application-test.properties)

```properties
loadup.cache.type=caffeine
loadup.cache.caffeine.initial-capacity=100
loadup.cache.caffeine.maximum-size=1000
loadup.cache.caffeine.expire-after-write-seconds=300
loadup.cache.caffeine.expire-after-access-seconds=60
```

### 自定义测试配置

在测试类上使用 `@TestPropertySource` 注解：

```java
@TestPropertySource(properties = {
        "loadup.cache.type=caffeine",
        "loadup.cache.caffeine.expire-after-write-seconds=2"
})
public class MyCustomTest extends BaseCacheTest {
    // test methods
}
```

## 测试实体类

### User

```java
User user = User.createTestUser("1");
// 包含: id, name, email, age, createTime, updateTime
```

### Product

```java
Product product = Product.createTestProduct("1");
// 包含: id, name, description, price, stock, category
```

## 编写新测试

### 1. 继承基类

```java
public class MyTest extends BaseCacheTest {
    // cacheBinding 已自动注入
    // TEST_CACHE_NAME, TEST_KEY, TEST_VALUE 已定义
}
```

### 2. 使用断言

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Test
void myTest() {
    // Given
    User user = User.createTestUser("1");

    // When
    cacheBinding.set(TEST_CACHE_NAME, "key", user);
    User result = cacheBinding.get(TEST_CACHE_NAME, "key", User.class);

    // Then
    assertThat(result).isNotNull();
    assertEquals(user.getId(), result.getId());
}
```

### 3. 测试异步行为

```java
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

@Test
void asyncTest() {
    // Wait for condition
    await().atMost(5, TimeUnit.SECONDS)
           .until(() -> cacheBinding.get(TEST_CACHE_NAME, "key", User.class) == null);
}
```

## 待实现测试

### Redis 缓存测试

- [ ] Redis 基础操作测试
- [ ] Redis 过期策略测试
- [ ] Redis 并发测试
- [ ] Redis 连接池测试
- [ ] Redis 序列化测试

### 集成测试

- [ ] Caffeine 与 Redis 切换测试
- [ ] 配置热更新测试
- [ ] 多 cache name 混合使用测试
- [ ] 缓存统计和监控测试

### 性能测试

- [ ] 大数据量测试（百万级）
- [ ] 长时间运行稳定性测试
- [ ] 内存泄漏测试
- [ ] 极限并发测试（1000+ 线程）

## 测试最佳实践

1. **测试隔离**：每个测试方法独立，使用 @BeforeEach 和 @AfterEach 清理
2. **命名规范**：使用 @DisplayName 提供中文描述
3. **Given-When-Then**：使用 BDD 风格组织测试代码
4. **断言明确**：使用清晰的断言消息
5. **性能测试**：记录并输出关键性能指标
6. **并发测试**：使用 CountDownLatch 和 ExecutorService
7. **异步验证**：使用 Awaitility 进行异步条件等待

## 故障排查

### 测试超时

- 增加 @Test(timeout = xxx) 超时时间
- 检查是否有死锁或无限等待
- 使用 @Timeout 注解设置全局超时

### 并发测试失败

- 增加线程池等待时间
- 检查共享资源的同步
- 使用 volatile 或 AtomicXXX

### 过期测试不稳定

- 增加等待缓冲时间
- 使用 Awaitility 而不是 Thread.sleep
- 考虑时钟漂移影响

## 持续集成

在 CI/CD 流水线中运行：

```yaml
# GitHub Actions 示例
- name: Run Tests
  run: mvn clean test

- name: Upload Test Results
  uses: actions/upload-artifact@v2
  with:
    name: test-results
    path: target/surefire-reports/
```

## 测试覆盖率

目标覆盖率：

- 代码行覆盖率：> 80%
- 分支覆盖率：> 70%
- 核心类覆盖率：> 90%

查看覆盖率报告：

```bash
mvn jacoco:report
open target/site/jacoco/index.html
```
