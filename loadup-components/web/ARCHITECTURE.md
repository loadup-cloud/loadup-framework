# Web 组件架构

## 职责与依赖

WebMVC 在应用入口绑定部署上下文，并按 API 路径选择响应处理规则。Controller 将应用契约转换为 HTTP 报文。SpringDoc 根据 Controller 契约生成 OpenAPI，文档端点访问权限由宿主安全链控制。

宿主直接依赖所需的组件工件。目录对应 Maven reactor 的聚合层，运行时依赖由各组件 POM 声明。

## 组件设计

- [WebMVC](loadup-components-webmvc/ARCHITECTURE.md)
- [接口文档](loadup-components-springdoc/ARCHITECTURE.md)

[接入说明](README.md)
