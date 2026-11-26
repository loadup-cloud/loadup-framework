# 平台基础组件架构

## 职责与依赖

Runtime 承接 Commons 的 Spring 装配。ConfigCenter 通过 SPI 绑定外部配置服务，Observability 复用宿主的 Micrometer。Testcontainers 通过测试入口提供基础设施，生命周期属于测试运行。

宿主直接依赖所需的组件工件。目录对应 Maven reactor 的聚合层，运行时依赖由各组件 POM 声明。

## 组件设计

- [配置中心](loadup-components-configcenter/ARCHITECTURE.md)
- [运行时装配](loadup-components-runtime/ARCHITECTURE.md)
- [应用观测](loadup-components-observability/ARCHITECTURE.md)
- [业务扩展](loadup-components-extension/ARCHITECTURE.md)
- [测试容器](loadup-components-testcontainers/ARCHITECTURE.md)

[接入说明](README.md)
