# LoadUp Components Signature 架构

## 职责与边界

提供数字签名能力的独立模块。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-validation`

## 实现入口

主要入口文件：

- [`SignatureAutoConfiguration`](src/main/java/io/github/loadup/components/signature/config/SignatureAutoConfiguration.java)
- [`DigestService`](src/main/java/io/github/loadup/components/signature/service/DigestService.java)
- [`KeyPairService`](src/main/java/io/github/loadup/components/signature/service/KeyPairService.java)
- [`SignatureService`](src/main/java/io/github/loadup/components/signature/service/SignatureService.java)

## 分层与调用路径

```text
调用方 → SignatureService / DigestService / KeyPairService → JCA 算法
```

## 装配规则

- [`SignatureAutoConfiguration`](src/main/java/io/github/loadup/components/signature/config/SignatureAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnProperty( prefix = "loadup.components.signature", name = "enabled", havingValue = "true", matchIfMissing = true)`

## 扩展契约

- [`KeyPairService`](src/main/java/io/github/loadup/components/signature/service/KeyPairService.java)：由实现方或调用方按接口定义对接。
- [`SignatureService`](src/main/java/io/github/loadup/components/signature/service/SignatureService.java)：由实现方或调用方按接口定义对接。
- [`DigestService`](src/main/java/io/github/loadup/components/signature/service/DigestService.java)：由实现方或调用方按接口定义对接。

## 配置归属

- [`SignatureProperties`](src/main/java/io/github/loadup/components/signature/properties/SignatureProperties.java) 绑定 `loadup.components.signature`。

集成方式与配置示例见 [README.md](./README.md)。
