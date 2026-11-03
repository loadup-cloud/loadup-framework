# LoadUp Components AuthServer API

OAuth2 授权服务器与令牌签发的业务契约与接口模块；实现由独立模块提供。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-authserver-api</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

需要 SAS 协议与 JWT 签发时引入 binder-sas；用户密码校验由业务认证适配提供。
运行时还需在应用侧引入对应 AuthServer binder 与 KMS binder；仅有接口 jar 不会创建后端能力。
签名配置为 signing.active-key（明确 name/version）与 verification-keys（保留公钥引用），不配置或传递私钥值；详见 [接入说明](../README.md)。

## 配置入口

| 前缀                          | 配置类                                                                                                                          |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `loadup.security.auth-server` | [`LoadUpAuthServerProperties`](src/main/java/io/github/loadup/components/authserver/properties/LoadUpAuthServerProperties.java) |

## 对外契约

- [`LoadUpSubject`](src/main/java/io/github/loadup/components/authserver/jwt/LoadUpSubject.java)
