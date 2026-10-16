# LoadUp BOM

`loadup-dependencies` 统一管理 LoadUp 发布坐标和第三方依赖版本，是纯 POM 模块，不提供运行时代码。

## 引入

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.loadup-cloud</groupId>
      <artifactId>loadup-dependencies</artifactId>
      <version>0.0.2-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

然后在消费工程中按需声明具体组件或业务模块，不需要为受 BOM 管理的坐标重复写版本。版本调整及依赖边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

测试栈使用 Spring Boot 4.1.1 BOM 管理的 JUnit 6.0.3，Jupiter 与 Platform 版本保持一致。
架构规则在普通 Jupiter 测试中通过 `com.tngtech.archunit:archunit` 执行，版本由本 BOM 管理。

当前管理 JobRunr 9.0.0、Groovy / Groovy JSON 6.0.0 和 commons-text 1.15.0；Awaitility 使用 Spring Boot BOM 的 4.3.0，消费模块不再单独固定旧版本。Groovy 当前仅提供依赖版本管理，仓库没有直接使用它的运行时代码；采用自定义脚本的消费工程需按 [Groovy 6 变更说明](https://groovy-lang.org/releasenotes/groovy-6.0.html)验证语法和行为。
