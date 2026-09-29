# LoadUp ConfigCenter Binder Nacos 架构

## 职责与边界

配置中心的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-configcenter-api`
- `loadup-commons-util`

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.alibaba.nacos:nacos-client`
- `org.yaml:snakeyaml`
- `org.springframework.boot:spring-boot-starter`
- `com.github.spotbugs:spotbugs-annotations`

## 实现入口

主要入口文件：

- [`NacosConfigCenterAutoConfiguration`](src/main/java/io/github/loadup/components/configcenter/nacos/autoconfig/NacosConfigCenterAutoConfiguration.java)
- [`NacosConfigCenterProvider`](src/main/java/io/github/loadup/components/configcenter/nacos/NacosConfigCenterProvider.java)
