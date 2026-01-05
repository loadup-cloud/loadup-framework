# TestContainers Configuration Example

## 1. testcontainers.properties (在 src/test/resources 目录下)

```properties
# TestContainers 配置
testcontainers.reuse.enable=true
testcontainers.mysql.version=mysql:8.0
testcontainers.mysql.database=testdb
testcontainers.mysql.username=test
testcontainers.mysql.password=test
```

## 2. application-test.yml (在 src/test/resources 目录下)

```yaml
spring:
  datasource:
    # 这些配置会被 TestContainers 自动覆盖
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect

# 日志配置
logging:
  level:
    root: INFO
    org.testcontainers: INFO
    com.github.loadup: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## 3. logback-test.xml (在 src/test/resources 目录下)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="org.testcontainers" level="INFO"/>
    <logger name="com.github.dockerjava" level="WARN"/>
    <logger name="com.github.loadup" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

## 4. 环境变量配置

### 4.1 Docker 环境配置

```bash
# 启用容器复用（加速测试）
export TESTCONTAINERS_REUSE_ENABLE=true

# 设置 Docker 主机（如果使用远程 Docker）
export DOCKER_HOST=tcp://localhost:2375

# 设置 Docker TLS 验证（如果需要）
export DOCKER_TLS_VERIFY=1
export DOCKER_CERT_PATH=/path/to/certs
```

### 4.2 测试配置环境变量

```bash
# MySQL 版本
export TESTCONTAINERS_MYSQL_VERSION=mysql:8.0.33

# 数据库名称
export TESTCONTAINERS_MYSQL_DATABASE=mydb

# 用户名和密码
export TESTCONTAINERS_MYSQL_USERNAME=testuser
export TESTCONTAINERS_MYSQL_PASSWORD=testpass
```

## 5. IDE 配置

### 5.1 IntelliJ IDEA

#### Run Configuration:

1. 打开 Run/Debug Configurations
2. 选择 JUnit 测试配置
3. 添加 VM options:

```
-Dtestcontainers.mysql.version=mysql:8.0.33
-Dtestcontainers.reuse.enable=true
```

#### Environment Variables:

```
TESTCONTAINERS_REUSE_ENABLE=true
DOCKER_HOST=unix:///var/run/docker.sock
```

### 5.2 VS Code

#### .vscode/settings.json:

```json
{
    "java.test.config": {
        "vmArgs": [
            "-Dtestcontainers.mysql.version=mysql:8.0.33",
            "-Dtestcontainers.reuse.enable=true"
        ],
        "env": {
            "TESTCONTAINERS_REUSE_ENABLE": "true"
        }
    }
}
```

## 6. CI/CD 配置示例

### 6.1 GitHub Actions

```yaml
name: Test with MySQL TestContainers

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      # 不需要额外服务，TestContainers 会自动管理
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          
      - name: Run tests
        run: mvn test -pl components/loadup-components-testcontainers
        env:
          TESTCONTAINERS_MYSQL_VERSION: mysql:8.0
```

### 6.2 GitLab CI

```yaml
test:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  
  services:
    - docker:dind
  
  variables:
    DOCKER_HOST: tcp://docker:2375
    DOCKER_TLS_CERTDIR: ""
    TESTCONTAINERS_MYSQL_VERSION: mysql:8.0
  
  script:
    - mvn test -pl components/loadup-components-testcontainers
  
  cache:
    paths:
      - .m2/repository
```

### 6.3 Jenkins

```groovy
pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                script {
                    docker.image('maven:3.9-eclipse-temurin-17').inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                        sh '''
                            export TESTCONTAINERS_MYSQL_VERSION=mysql:8.0
                            mvn test -pl components/loadup-components-testcontainers
                        '''
                    }
                }
            }
        }
    }
}
```

## 7. Docker Compose for Local Development

虽然 TestContainers 会自动管理容器，但如果你想要一个持久的开发数据库：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: loadup-mysql-dev
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: testdb
      MYSQL_USER: test
      MYSQL_PASSWORD: test
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 8. 性能优化配置

### 8.1 启用容器复用

在 `~/.testcontainers.properties` 中：

```properties
testcontainers.reuse.enable=true
```

### 8.2 使用本地 Docker 镜像

```bash
# 提前拉取镜像
docker pull mysql:8.0

# 或者构建自定义镜像
docker build -t my-mysql:test -f Dockerfile.mysql .
```

### 8.3 并行测试配置

在 `pom.xml` 中：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
        <reuseForks>true</reuseForks>
    </configuration>
</plugin>
```

## 9. 故障排除配置

### 9.1 启用详细日志

```properties
# 在 src/test/resources/testcontainers.properties
checks.disable=false
ryuk.container.timeout=60
```

### 9.2 调试模式

```java
// 在测试类中添加
static {
    System.setProperty("testcontainers.checks.disable", "false");
}
```

### 9.3 网络配置

如果遇到网络问题：

```properties
# ~/.testcontainers.properties
docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy
```

