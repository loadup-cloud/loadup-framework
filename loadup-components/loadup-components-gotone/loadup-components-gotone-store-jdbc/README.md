# Loadup Gotone Store JDBC

多渠道通知的可选 JDBC 持久化模块。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-store-jdbc</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `GotoneStoreJdbcAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

引入 engine 和需要的渠道 binder；渠道可同时存在，存储模块按需引入。

## 对外契约

- [`ServiceChannelDOMapper`](src/main/java/io/github/loadup/components/gotone/store/mapper/ServiceChannelDOMapper.java)
- [`NotificationServiceDOMapper`](src/main/java/io/github/loadup/components/gotone/store/mapper/NotificationServiceDOMapper.java)
- [`NotificationRecordDOMapper`](src/main/java/io/github/loadup/components/gotone/store/mapper/NotificationRecordDOMapper.java)

## 自动装配

- [`GotoneStoreJdbcAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/store/config/GotoneStoreJdbcAutoConfiguration.java)
