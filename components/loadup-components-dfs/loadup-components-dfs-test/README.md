# LoadUp DFS Test

DFS组件的测试模块，包含所有Provider的单元测试和集成测试。

## 测试覆盖

### 测试类

|           测试类           |        功能         | 用例数 | 通过率  |
|-------------------------|-------------------|-----|------|
| LocalDfsProviderTest    | Local Provider    | 10  | 100% |
| DatabaseDfsProviderTest | Database Provider | 12  | 100% |
| DfsServiceTest          | DFS Service       | 10  | 100% |
| DfsIntegrationTest      | 集成测试              | 10  | 100% |

### 测试场景

- ✅ 文件上传/下载/删除
- ✅ 元数据管理
- ✅ 大文件处理
- ✅ 并发操作
- ✅ 错误处理
- ✅ 多Provider切换
- ✅ 业务分类
- ✅ 访问控制

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试
mvn test -Dtest=LocalDfsProviderTest

# 运行测试并生成覆盖率报告
mvn clean test jacoco:report
```

## 测试配置

测试使用**Testcontainers MySQL**，自动启动MySQL容器，无需手动配置数据库。

### 优势

- ✅ 与生产环境一致（使用真实MySQL）
- ✅ 避免H2兼容性问题
- ✅ 自动管理容器生命周期
- ✅ 每次测试使用干净环境

### 配置文件
```yaml
# application-test.yml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

### 容器配置

- **镜像**: mysql:8.0
- **数据库**: dfs_test
- **用户**: test/test
- **字符集**: utf8mb4

### 前置条件

- ✅ Docker环境运行中
- ✅ 足够的磁盘空间
- ✅ 网络连接正常（首次需拉取镜像）

## 测试结果

```
Tests run: 42
✅ Passed: 42 (100%)
❌ Failed: 0
⚠️  Errors: 0

代码覆盖率: 90%+
```

详见: [主项目文档](../README.md)
