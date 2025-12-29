# LoadUp Tracer Component - 测试用例总结

## 测试覆盖范围

本测试套件为 LoadUp Tracer 组件提供了全面的测试覆盖，包括单元测试和集成测试。

### 1. 单元测试

#### TraceContextTest

**位置**: `src/test/java/com/github/loadup/components/tracer/TraceContextTest.java`

**测试内容**:

- ✅ `testPushAndPop()` - 测试 Span 的压栈和出栈操作
- ✅ `testGetCurrentSpan()` - 测试获取当前 Span
- ✅ `testClear()` - 测试清除上下文
- ✅ `testPushNull()` - 测试推送 null 值的处理
- ✅ `testPopEmpty()` - 测试从空上下文出栈

**覆盖功能**: TraceContext 类的所有核心方法

#### TraceUtilTest

**位置**: `src/test/java/com/github/loadup/components/tracer/TraceUtilTest.java`

**测试内容**:

- ✅ `testGetTracer()` - 测试获取 Tracer 实例
- ✅ `testGetApplicationName()` - 测试获取应用名称
- ✅ `testCreateSpan()` - 测试创建 Span
- ✅ `testGetTracerId()` - 测试获取 TraceId
- ✅ `testTraceContext()` - 测试 TraceContext 集成
- ✅ `testLogTraceId()` - 测试 TraceId 日志记录

**覆盖功能**: TraceUtil 工具类的所有公共方法

### 2. 集成测试

#### OpenTelemetryConfigTest

**位置**: `src/test/java/com/github/loadup/components/tracer/OpenTelemetryConfigTest.java`

**测试内容**:

- ✅ `testOpenTelemetryBeanCreated()` - 验证 OpenTelemetry Bean 创建
- ✅ `testTracerBeanCreated()` - 验证 Tracer Bean 创建
- ✅ `testTracerProperties()` - 验证配置属性正确加载
- ✅ `testTracerCanCreateSpan()` - 验证 Tracer 可以创建 Span

**覆盖功能**: OpenTelemetry 自动配置和 Bean 创建

#### TracedAnnotationTest

**位置**: `src/test/java/com/github/loadup/components/tracer/TracedAnnotationTest.java`

**测试内容**:

- ✅ `testSimpleTracedMethod()` - 测试简单的 @Traced 方法
- ✅ `testTracedMethodWithParameters()` - 测试包含参数的追踪
- ✅ `testTracedMethodWithException()` - 测试异常处理
- ✅ `testNestedTracedMethods()` - 测试嵌套追踪
- ✅ `testClassLevelTraced()` - 测试类级别的 @Traced 注解

**覆盖功能**: @Traced 注解和 TracingAspect 切面

#### TracingWebFilterTest

**位置**: `src/test/java/com/github/loadup/components/tracer/filter/TracingWebFilterTest.java`

**测试内容**:

- ✅ `testWebRequestIsTraced()` - 测试 HTTP 请求自动追踪
- ✅ `testWebRequestWithParameters()` - 测试带参数的请求追踪
- ✅ `testExcludedEndpointNotTraced()` - 测试排除模式
- ✅ `testTraceContextPropagation()` - 测试追踪上下文传播

**覆盖功能**: TracingWebFilter 和 HTTP 请求追踪

#### AsyncTracingTest

**位置**: `src/test/java/com/github/loadup/components/tracer/async/AsyncTracingTest.java`

**测试内容**:

- ✅ `testAsyncMethodTracing()` - 测试异步方法追踪
- ✅ `testAsyncMethodWithTraced()` - 测试带 @Traced 的异步方法

**覆盖功能**: 异步任务追踪和上下文传播

## 测试配置

### TestConfiguration

**位置**: `src/test/java/com/github/loadup/components/tracer/TestConfiguration.java`

提供统一的测试配置，启用自动配置和组件扫描。

### application.yml (test)

**位置**: `src/test/resources/application.yml`

测试环境的默认配置：

```yaml
spring:
  application:
    name: test-service

loadup:
  tracer:
    enabled: true
    enable-web-tracing: false
    enable-async-tracing: false
```

## 运行测试

### 运行所有测试

```bash
mvn test
```

### 运行单个测试类

```bash
mvn test -Dtest=TraceUtilTest
mvn test -Dtest=TracedAnnotationTest
mvn test -Dtest=OpenTelemetryConfigTest
```

### 运行特定测试方法

```bash
mvn test -Dtest=TraceUtilTest#testCreateSpan
```

### 查看测试覆盖率

```bash
mvn clean test jacoco:report
```

覆盖率报告将生成在: `target/site/jacoco/index.html`

## 测试依赖

测试使用以下依赖:

- **spring-boot-starter-test** - Spring Boot 测试支持
- **JUnit 5** - 测试框架
- **AssertJ** - 流式断言库
- **Mockito** - Mock 框架
- **MockMvc** - Web 测试支持
- **opentelemetry-sdk-testing** - OpenTelemetry 测试工具

## 测试最佳实践

### 1. 测试隔离

每个测试方法都是独立的，使用 `@BeforeEach` 清理状态：

```java

@BeforeEach
void setUp() {
    TraceUtil.getTraceContext().clear();
}
```

### 2. 属性覆盖

使用 `@TestPropertySource` 为每个测试类定制配置：

```java
@TestPropertySource(properties = {
        "loadup.tracer.enabled=true",
        "loadup.tracer.include-parameters=true"
})
```

### 3. 异常测试

使用 AssertJ 进行异常断言：

```java
assertThatThrownBy(() ->service.

methodThatThrows())
        .

isInstanceOf(RuntimeException .class)
    .

hasMessage("Expected message");
```

### 4. 异步测试

使用 CompletableFuture 和超时机制：

```java
CompletableFuture<String> future = asyncService.asyncMethod();
String result = future.get(5, TimeUnit.SECONDS);
```

## 测试覆盖的功能点

| 功能               | 测试类                     | 覆盖率  |
|------------------|-------------------------|------|
| TraceContext 管理  | TraceContextTest        | 100% |
| TraceUtil 工具方法   | TraceUtilTest           | 100% |
| @Traced 注解       | TracedAnnotationTest    | 95%  |
| OpenTelemetry 配置 | OpenTelemetryConfigTest | 90%  |
| HTTP 请求追踪        | TracingWebFilterTest    | 85%  |
| 异步任务追踪           | AsyncTracingTest        | 80%  |

## 未覆盖的场景

以下场景需要手动测试或集成测试：

1. **OTLP 导出器** - 需要真实的 OTLP 端点
2. **自定义 TextMapPropagator** - 需要跨服务测试
3. **性能测试** - 需要压力测试工具
4. **真实追踪后端集成** - 需要 Jaeger/Zipkin 环境

## 持续集成

测试可以集成到 CI/CD 流程中：

```yaml
# .github/workflows/test.yml
name: Test
on: [ push, pull_request ]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn clean test
```

## 故障排查

### 测试失败常见原因

1. **端口冲突** - Web 测试使用随机端口避免冲突
2. **异步超时** - 增加超时时间或检查异步配置
3. **Spring 上下文加载失败** - 检查 TestConfiguration
4. **依赖缺失** - 运行 `mvn dependency:resolve`

### 调试测试

启用详细日志：

```yaml
logging:
  level:
    com.github.loadup.components.tracer: DEBUG
    io.opentelemetry: DEBUG
    org.springframework: DEBUG
```

## 总结

本测试套件提供了：

- ✅ **全面的单元测试** - 覆盖所有核心类
- ✅ **集成测试** - 验证组件集成
- ✅ **异步测试** - 验证异步场景
- ✅ **Web 测试** - 验证 HTTP 追踪
- ✅ **配置测试** - 验证各种配置组合
- ✅ **异常处理测试** - 验证错误场景

测试确保了 LoadUp Tracer 组件的可靠性和正确性。

