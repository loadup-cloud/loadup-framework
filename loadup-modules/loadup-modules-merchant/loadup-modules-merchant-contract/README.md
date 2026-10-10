# LoadUp Merchant Contract

可选合约事实适配。接入细节见 [商户 README](../README.md)。

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-merchant-contract</artifactId>
</dependency>
```

版本由 LoadUp BOM 管理；test 仅限 test scope，不作为生产依赖。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 职责 | 可选合约事实适配 |
| 当前实现 | 只依赖 merchant-client、contract-client 与 Boot 装配，将 MerchantLookup 查询结果转换为 MerchantFactsProvider。检查 tenant/id，仅输出基本事实，已存在 Provider 时退让。 |
| 验证状态 | 源码提供，尚未编译/运行 |

## 接入约束

使用可信租户作用域，编码创建后不变；合约 merchantId 为返回 id。地址/登记号/联系方式输出脱敏，修改时 null 保留、空串清空，不写回掩码。Merchant app 与 contract 适配需同时引入；自定义 Provider 可替换默认适配。配置、权限、示例和限制见主 README。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。
