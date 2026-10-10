# LoadUp Merchant Client

公开报文与 MerchantLookup。接入细节见 [商户 README](../README.md)。

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-merchant-client</artifactId>
</dependency>
```

版本由 LoadUp BOM 管理；test 仅限 test scope，不作为生产依赖。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 职责 | 公开报文与 MerchantLookup |
| 当前实现 | 不可变 record 提供写入、分页、状态、详情和非敏感 Profile 查询契约。Masked 注解仅描述 WebMVC 输出；MerchantLookup 使用显式租户/id 查询，不暴露私有联系信息。 |
| 验证状态 | 源码提供，尚未编译/运行 |

## 接入约束

使用可信租户作用域，编码创建后不变；合约 merchantId 为返回 id。地址/登记号/联系方式输出脱敏，修改时 null 保留、空串清空，不写回掩码。Merchant app 与 contract 适配需同时引入；自定义 Provider 可替换默认适配。配置、权限、示例和限制见主 README。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。
