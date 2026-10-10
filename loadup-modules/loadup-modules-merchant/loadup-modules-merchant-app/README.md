# LoadUp Merchant App

资料管理编排。接入细节见 [商户 README](../README.md)。

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-merchant-app</artifactId>
</dependency>
```

版本由 LoadUp BOM 管理；test 仅限 test scope，不作为生产依赖。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 职责 | 资料管理编排 |
| 当前实现 | MerchantService 事务编排创建/更新/查询/启停；MerchantQueryService 提供可信基本 Profile。共享 Clock、事务管理器与 DataSource；显式 Import 装配，不与 REGISTER_BEAN 条件混用扫描。 |
| 验证状态 | 源码提供，尚未编译/运行 |

## 接入约束

使用可信租户作用域，编码创建后不变；合约 merchantId 为返回 id。地址/登记号/联系方式输出脱敏，修改时 null 保留、空串清空，不写回掩码。Merchant app 与 contract 适配需同时引入；自定义 Provider 可替换默认适配。配置、权限、示例和限制见主 README。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。
