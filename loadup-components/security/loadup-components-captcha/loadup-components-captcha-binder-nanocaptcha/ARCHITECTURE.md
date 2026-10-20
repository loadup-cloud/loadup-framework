# LoadUp Captcha Binder Nanocaptcha 架构

## 职责与边界

验证码的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-captcha-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `net.logicsquad:nanocaptcha`
- `org.slf4j:slf4j-api`
- `org.springframework.boot:spring-boot-starter`

## 实现入口

主要入口文件：

- [`NanocaptchaAutoConfiguration`](src/main/java/io/github/loadup/components/captcha/nanocaptcha/autoconfigure/NanocaptchaAutoConfiguration.java)
- [`NanocaptchaProvider`](src/main/java/io/github/loadup/components/captcha/nanocaptcha/NanocaptchaProvider.java)

## 分层与调用路径

`CaptchaTemplate` 统一生成与校验入口，单个 `CaptchaProvider` 负责算法及存储细节。

```text
业务代码 → API/facade → 当前 binder → 第三方引擎或基础设施
```
binder 实现框架契约，把实现库及其配置隔离在业务 API 之外。

## 装配规则

- [`NanocaptchaAutoConfiguration`](src/main/java/io/github/loadup/components/captcha/nanocaptcha/autoconfigure/NanocaptchaAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnClass(ImageCaptcha.class)`
  - `@ConditionalOnProperty(prefix = "loadup.captcha", name = "binder-type", havingValue = "nanocaptcha")`

## 配置归属

- [`NanocaptchaProperties`](src/main/java/io/github/loadup/components/captcha/nanocaptcha/NanocaptchaProperties.java) 绑定 `loadup.captcha.binder.nanocaptcha`。

## 设计取舍

选择独立 binder，使验证码实现的依赖留在集成应用；替换底层实现时，业务侧仍使用 `CaptchaTemplate` 契约。

集成方式与配置示例见 [README.md](README.md)。

## 校验与存储边界

空 ID、非字符串输入及不存在或过期的验证码返回 `false`。字符串比较忽略大小写及两端空白；第一次有效类型的校验尝试即消费答案，错误答案也不可重试。非字符串输入不消费答案。

答案保存在当前 JVM，达到 5000 条时清理过期条目；这不是硬容量上限，也不适合多实例共享校验。接入方需限制生成频率，并保证生成和校验落到同一实例。测试通过 SDK 构造边界提供固定答案，真实 PNG 渲染覆盖数字和拉丁字符集，不反射读取答案。

## NanoCaptcha 3.0 兼容性

使用 `ImageCaptcha.Factory.Builder.build().create()` 生成验证码，背景生产器位于 `image.background` 包。3.0 已移除中文生成器，`content` 仅支持 `numbers` 和 `latin`；中文或其他未知字符集在生成时抛出 `IllegalArgumentException`，不再静默回退为数字。校验继续由 binder 比较答案，保留忽略大小写、两端空白及单次消费语义。
