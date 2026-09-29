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
