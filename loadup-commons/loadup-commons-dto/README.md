# Loadup Common DTO

提供通用响应、分页、数据对象基类和 MapStruct 共享配置。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-dto</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤


## 对外契约

- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)
- [`LoadUpMapStructConfig`](src/main/java/io/github/loadup/commons/mapping/LoadUpMapStructConfig.java)
- [`ResultCode`](src/main/java/io/github/loadup/commons/result/ResultCode.java)
- [`IEnum`](src/main/java/io/github/loadup/commons/enums/IEnum.java)
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)
- [`AssertionCallback`](src/main/java/io/github/loadup/commons/error/AssertionCallback.java)
