# LoadUp BOM

本 BOM 默认通过 Deploy 上传 GitHub Packages；中央库发布使用 `mvnd -f loadup-framework-bom/pom.xml clean deploy -Prelease`，
会启用 Central Publishing 与 GPG，签名 POM。BOM 不需要 sources/Javadoc JAR；凭证和版本要求见 [发布说明](../PUBLISHING.md)。

`loadup-framework-bom` 是 framework 的 BOM，管理 commons、components 与框架第三方依赖版本，是纯 POM 制品，不提供运行时代码。

## 引入

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.loadup-cloud</groupId>
      <artifactId>loadup-framework-bom</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

然后按需声明具体 commons 或 components 制品，不需要为受本 BOM 管理的坐标重复写版本。
消费通用业务模块时另外导入 `loadup-modules-bom`；使用 Testify 时另外导入 `loadup-testify-bom`。
各产品 BOM 跟随所属仓库版本；Modules、Testify BOM 导入配套 Framework BOM，Pay BOM 导入 Framework 与 Modules BOM。
已导入上层产品 BOM 的消费方无需重复导入 Framework BOM。版本调整及依赖边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

平台基线为 Spring Boot 4.2.0、Spring Framework 7.1.0、Spring Security 7.2.0 和 Spring Cloud 2026.0.0。
测试栈使用 Spring Boot 4.2.0 BOM 管理的 JUnit 6.1.3，Jupiter 与 Platform 版本保持一致。
架构规则在普通 Jupiter 测试中通过 `com.tngtech.archunit:archunit` 执行，版本由本 BOM 管理。

当前管理 PowerJob 5.1.7、Groovy / Groovy JSON 6.0.0 和 commons-text 1.15.0；Awaitility 使用 Spring Boot BOM 的
4.3.0，消费模块不再单独固定旧版本。Groovy
当前仅提供依赖版本管理，仓库没有直接使用它的运行时代码；采用自定义脚本的消费工程需按 [Groovy 6 变更说明](https://groovy-lang.org/releasenotes/groovy-6.0.html)
验证语法和行为。

嵌入式 Tomcat 的 core、EL、WebSocket 和 Jasper 由 Spring Boot BOM 统一管理，当前版本为 11.0.27。

LoadUp BOM 将 Jackson 3 统一管理为 3.2.4，Jackson 2 跟随 Spring Boot BOM 的 2.21.8。两代共用的 `jackson-annotations`
固定为 2.22。SpringDoc 3.2.1 和 Swagger Core 3.0.0 使用 Jackson 3。

Framework 使用 Flyway 13.13.0 和 Commons Lang 3.22.0。工作区中继承 Spring Boot 父 POM 的项目由父 POM 管理这两项，
当前分别为 12.11.0 和 3.21.0。

`at.yawk.lz4:lz4-java` 统一管理为 1.14.1，覆盖 Kafka 客户端等传递依赖中的旧版本。此项仅管理版本，不主动引入 Redisson 的可选
LZ4 codec 依赖；消费方需要时按坐标声明，无需重复写版本。
