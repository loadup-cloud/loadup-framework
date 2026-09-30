# Loadup Components Extension

提供基于 AspectJ 的扩展机制。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-extension</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `ExtensionAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤


## 对外契约

- [`ExtensionProvider`](src/main/java/io/github/loadup/components/extension/spi/ExtensionProvider.java)
- [`BizIdentity`](src/main/java/io/github/loadup/components/extension/api/BizIdentity.java)
- [`IExtensionPoint`](src/main/java/io/github/loadup/components/extension/api/IExtensionPoint.java)

## 自动装配

- [`ExtensionAutoConfiguration`](src/main/java/io/github/loadup/components/extension/config/ExtensionAutoConfiguration.java)
