# LoadUp Captcha Components API 架构

## 职责与边界

验证码的业务契约与接口模块；实现由独立模块提供。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure-processor`
- `org.springframework.boot:spring-boot-configuration-processor`
- `org.springframework.boot:spring-boot-starter`

## 实现入口

主要入口文件：

- [`CaptchaAutoConfiguration`](src/main/java/io/github/loadup/components/captcha/autoconfig/CaptchaAutoConfiguration.java)
- [`CaptchaProvider`](src/main/java/io/github/loadup/components/captcha/CaptchaProvider.java)
- [`CaptchaTemplate`](src/main/java/io/github/loadup/components/captcha/CaptchaTemplate.java)
- [`DefaultCaptchaTemplate`](src/main/java/io/github/loadup/components/captcha/DefaultCaptchaTemplate.java)
