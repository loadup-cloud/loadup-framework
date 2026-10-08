# Loadup Commons

通用基础能力的聚合模块，不承载业务流程。

## 子模块

- [`loadup-commons-dto`](loadup-commons-dto/README.md)
- [`loadup-commons-util`](loadup-commons-util/README.md)
- [`loadup-commons-log`](loadup-commons-log/README.md)

此 POM 用于 Maven 聚合；在消费工程中选择需要的具体 jar 坐标。

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

此目录是 Maven 聚合模块，不作为业务运行依赖。按用途选择子模块：

- [`loadup-commons-dto`](loadup-commons-dto/README.md)：运行实现。
- [`loadup-commons-log`](loadup-commons-log/README.md)：运行实现。
- [`loadup-commons-util`](loadup-commons-util/README.md)：运行实现。

## 展示脱敏

- [`loadup-commons-masking`](loadup-commons-masking/README.md)：纯 Java 展示规则与 `@Masked` 输出元数据，可供 WebMVC、显式日志与导出使用。

## 执行链上下文

[`loadup-commons-context`](loadup-commons-context/README.md) 提供 JDK 25 ScopedValue 类型化只读 `ContextHolder`、不可变 ExecutionContext 与可选 ServiceTemplate。租户数据复用该存储，登录身份和 Trace 仍采用各自标准上下文。
