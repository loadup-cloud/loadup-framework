# Testify Assert Engine

Testify 测试框架的执行引擎模块。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-assert-engine</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

测试模块按需引入 starter、数据引擎与断言引擎。

## 对外契约

- [`TestifyAssertEngine`](src/main/java/io/github/loadup/testify/asserts/engine/TestifyAssertEngine.java)
- [`OperatorMatcher`](src/main/java/io/github/loadup/testify/asserts/operator/OperatorMatcher.java)
