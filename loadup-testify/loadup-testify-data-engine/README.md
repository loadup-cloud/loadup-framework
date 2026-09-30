# Testify Data Engine

Testify 测试框架的执行引擎模块。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-data-engine</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

测试模块按需引入 starter、数据引擎与断言引擎。

## 对外契约

- [`TestifyFunction`](src/main/java/io/github/loadup/testify/data/engine/function/TestifyFunction.java)
