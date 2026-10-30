# LoadUp Common Error

提供独立于 Spring、Jackson、DTO 和 ORM 的错误契约，生产代码仅依赖 JDK。

## 接入

通过 LoadUp BOM 管理版本，按需引入：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-error</artifactId>
</dependency>
```

| 能力            | 契约                                                        |
|-----------------|-------------------------------------------------------------|
| ResultCode      | 由枚举实现 getCode/getStatus/getMessage                     |
| CommonException | 接收 ResultCode 或 ResultCode + Throwable；对外文案取自枚举 |
| 通用错误        | CommonResultCodeEnum 定义平台级校验、认证与系统失败         |
| 错误校验        | requireFailure 检查枚举、码格式、失败状态与文案长度         |
| 框架依赖        | 无 Spring/JSON/数据库运行依赖，无配置项                     |

```java
throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
throw new CommonException(CommonResultCodeEnum.SYS_ERROR, cause);
```

业务模块在 `client.error.XxxResultCodeEnum` 声明错误，码为 `<MODULE>_<REASON>`，固定英文文案。码只包含大写字母、数字、下划线，长度不超过
64；文案不超过 256。S 表示成功，F 表示失败，U 表示结果未知；成功码不能构造失败响应或异常。

业务代码不直接构造 RuntimeException/IllegalArgumentException/IllegalStateException 来表达错误。异常仍为 unchecked
CommonException，便于事务回滚；不需要每层捕获再包装。系统故障保留 cause，文案不会取自 cause.getMessage ()。

原有 Java 包名保持 `commons.result.ResultCode`、`commons.enums`、`commons.error.CommonException`，源码归属此模块。响应
envelope 与 JSON 映射仍由 commons-dto/WebMVC 提供。

## 验证

`mvnd clean test -pl :loadup-commons-error -am -Dtest=CommonExceptionTest -Dsurefire.failIfNoSpecifiedTests=false`
