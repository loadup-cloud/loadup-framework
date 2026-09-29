# Loadup Components Extension 架构

## 职责与边界

提供基于 AspectJ 的扩展机制。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-aspectj`

## 实现入口

主要入口文件：

- [`ExtensionAutoConfiguration`](src/main/java/io/github/loadup/components/extension/config/ExtensionAutoConfiguration.java)
- [`ExtensionProvider`](src/main/java/io/github/loadup/components/extension/spi/ExtensionProvider.java)
