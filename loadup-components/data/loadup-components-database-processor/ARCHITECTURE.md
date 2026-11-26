# 数据库注解处理器架构

## 编译期职责

`LoadUpMyBatisFlexProcessor` 继承官方 MyBatis-Flex 处理器，在上游初始化之前将随包发布的 UTF-8 生成策略写入本次编译的 `CLASS_OUTPUT`。随后确认上游可定位 Maven 或 Gradle 工程根目录。策略准备失败时输出编译错误并保留原始原因。注解识别、生成逻辑、支持类型和源版本继续由上游实现负责。

```mermaid
flowchart LR
    Java["带 @Table 的业务 DO"] --> Compiler["Java 编译器"]
    Compiler --> LoadUp["LoadUpMyBatisFlexProcessor"]
    LoadUp --> Policy["写入本次 CLASS_OUTPUT 的生成策略"]
    Policy --> Flex["官方 Flex 处理器"]
    Flex --> Output["生成 Mapper、TableDef 和 Tables"]
```

## 装配与隔离

编译器显式选择 LoadUp 入口，因为上游传递依赖也注册了 SPI。MapStruct 与 Spring Boot 元数据处理器各用独立入口。处理器模块编译自身时禁用注解处理，依赖位于编译期，运行时 Database 工件独立发布。

主源码和测试源码各自在独立输出目录准备策略，`stopBubbling=true` 使模块与祖先目录的本地文件无法覆盖该策略。Framework 内的实体模块通过 provided/optional 依赖保证 reactor 顺序。独立仓库通过 `annotationProcessorPaths` 解析已发布版本，版本由 Framework BOM 对齐。

JDK 编译回归测试覆盖生成源码可编译、固定包名和类型名、主测试输出隔离以及策略准备失败。升级上游处理器时验证这些契约。

[编译器配置](README.md) · [运行时 Database](../loadup-components-database/ARCHITECTURE.md)
