# Loadup Common DTO 架构

## 职责与边界

提供通用响应、分页、数据对象基类和 MapStruct 共享配置。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.fasterxml.jackson.core:jackson-annotations`
- `org.mapstruct:mapstruct`
- `com.mybatis-flex:mybatis-flex-annotation`
- `io.swagger.core.v3:swagger-annotations-jakarta`

## 实现入口

主要源码入口：

- [`BaseDO`](src/main/java/io/github/loadup/commons/dataobject/BaseDO.java)
- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)
- [`BaseEntity`](src/main/java/io/github/loadup/commons/domain/BaseEntity.java)

## 分层与调用路径

```text
业务模型 → Result/PageDTO/BaseDO 与对象映射约定 → 上层组件
```

## 扩展契约

- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)：由实现方或调用方按接口定义对接。
- [`LoadUpMapStructConfig`](src/main/java/io/github/loadup/commons/mapping/LoadUpMapStructConfig.java)：由实现方或调用方按接口定义对接。
- [`ResultCode`](src/main/java/io/github/loadup/commons/result/ResultCode.java)：由实现方或调用方按接口定义对接。
- [`IEnum`](src/main/java/io/github/loadup/commons/enums/IEnum.java)：由实现方或调用方按接口定义对接。
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)：由实现方或调用方按接口定义对接。
- [`AssertionCallback`](src/main/java/io/github/loadup/commons/error/AssertionCallback.java)：由实现方或调用方按接口定义对接。

集成方式与配置示例见 [README.md](README.md)。
