# ConfigCenter Binder XXL-Conf

使用 `loadup-components-configcenter-binder-xxl-conf` 和 `loadup.configcenter.binder-type: xxl-conf`。在
`loadup.configcenter.binder.xxl-conf` 配置 `address`（含部署的 context path）、`access-token`、`app-name` 和 `env`。

| 能力             | 支持                                                                      |
|------------------|---------------------------------------------------------------------------|
| 查值、列出键     | `queryData`、`queryKey`                                                   |
| 注解或程序化监听 | `monitor` 长轮询收到信号后批量重查订阅键                                  |
| 程序化写入和删除 | 返回 `false`。XXL-Conf 2.2.1 通过管理台写入和删除，修改仍会触发客户端监听 |

协议以 [XXL-Conf 2.2.1 的 OpenAPI 路由](https://github.com/xuxueli/xxl-conf/blob/2.2.1/xxl-conf-admin/src/main/java/com/xxl/conf/admin/business/openapi/OpenApiController.java)
为准。
