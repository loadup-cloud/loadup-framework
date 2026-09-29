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

- [`TianaiCaptchaAutoConfiguration`](src/main/java/io/github/loadup/components/captcha/tianai/autoconfig/TianaiCaptchaAutoConfiguration.java)
- [`TianaiCaptchaProvider`](src/main/java/io/github/loadup/components/captcha/tianai/TianaiCaptchaProvider.java)
