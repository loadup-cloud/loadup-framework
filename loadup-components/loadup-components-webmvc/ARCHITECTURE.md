# LoadUp Web MVC 架构

## 职责与边界

提供Spring MVC 响应与错误处理能力的独立模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-commons-util`
- `loadup-commons-tracer`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-webmvc`

## 实现入口

主要入口文件：

- [`LoadUpWebMvcAutoConfiguration`](src/main/java/io/github/loadup/components/webmvc/LoadUpWebMvcAutoConfiguration.java)
- [`ApiErrorController`](src/main/java/io/github/loadup/components/webmvc/ApiErrorController.java)
