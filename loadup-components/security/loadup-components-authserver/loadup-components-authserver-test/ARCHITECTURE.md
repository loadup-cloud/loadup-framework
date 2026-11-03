# AuthServer Test 设计

测试依赖 authserver-api、SAS binder、kms-local。配置测试使用 ApplicationContextRunner；协议测试使用最小 Servlet 应用和 MockMvc，不接数据库、远程 KMS 或生产凭证。

TestKeyStores 在临时目录生成真实 PKCS12，配置用例与协议用例共享该夹具。SAS 始终按明确 alias/version 装配 KMS 签名器；JWKS 和 decoder 使用公钥，不能读取私钥。协议测试的密钥位置通过 DynamicPropertySource 提供，配置文件只保存测试身份和密码环境属性名。

回归用例明确验证 Spring JwsHeader 的 alg 对象被转为 Nimbus 所需的 JSON 字符串，且生成的实际 JWT 可验签；KMS 失败用例同时核对 sign 调用次数，避免异常在到达 KMS 前发生却被误判为 provider 失败覆盖。

单元测试覆盖身份/轮换/错误边界；协议集成覆盖 token endpoint、授权重定向和公钥发布。静态检查与测试运行分别记录，不能用 Pod 就绪或 MockMvc 之外的部署检查代替本模块测试执行。
