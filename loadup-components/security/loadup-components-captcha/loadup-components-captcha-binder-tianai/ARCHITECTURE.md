# LoadUp Captcha Binder Tianai 架构

## 职责与边界

验证码的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-captcha-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `cloud.tianai.captcha:tianai-captcha`
- `tools.jackson.core:jackson-databind`
- `org.slf4j:slf4j-api`
- `org.springframework.boot:spring-boot-starter`

## 实现入口

主要入口文件：

- [`TianaiCaptchaAutoConfiguration`](src/main/java/io/github/loadup/components/captcha/tianai/autoconfigure/TianaiCaptchaAutoConfiguration.java)
- [`TianaiCaptchaProvider`](src/main/java/io/github/loadup/components/captcha/tianai/TianaiCaptchaProvider.java)

## 分层与调用路径

`CaptchaTemplate` 统一生成与校验入口，单个 `CaptchaProvider` 负责算法及存储细节。

```text
业务代码 → API/facade → 当前 binder → 第三方引擎或基础设施
```
binder 实现框架契约，把实现库及其配置隔离在业务 API 之外。

## 装配规则

- [`TianaiCaptchaAutoConfiguration`](src/main/java/io/github/loadup/components/captcha/tianai/autoconfigure/TianaiCaptchaAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnClass(TACBuilder.class)`
  - `@ConditionalOnProperty(prefix = "loadup.captcha", name = "binder-type", havingValue = "tianai", matchIfMissing = true)`

## 配置归属

- [`TianaiCaptchaProperties`](src/main/java/io/github/loadup/components/captcha/tianai/TianaiCaptchaProperties.java) 绑定 `loadup.captcha.binder.tianai`。

## 设计取舍

选择独立 binder，使验证码实现的依赖留在集成应用；替换底层实现时，业务侧仍使用 `CaptchaTemplate` 契约。

集成方式与配置示例见 [README.md](README.md)。

## 校验失败与日志

支持数值百分比、轨迹对象及轨迹 JSON；空 ID、空输入、非法 JSON、未知输入类型或引擎异常返回 `false`。解析异常只记录异常类型，不记录轨迹 JSON、异常消息或异常链，避免解析器泄露请求内容。关闭默认资源时由接入方提供资源，过期配置按类型传入引擎。
