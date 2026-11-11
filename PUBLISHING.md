# GitHub Packages 与 Maven Central 发布

Framework、Modules、Testify 及其独立 BOM 默认激活 `github` profile。日常开发和 CI 使用 GitHub Packages；
中央库等正式版本准备好后，再手动启用 `release` profile 发布。

```bash
mvnd clean deploy -DdeployAtEnd=true
mvnd clean deploy -Prelease
```

`github` 通过 Maven Deploy Plugin 上传到 `loadup-cloud/loadup-packages`，支持正式版和 SNAPSHOT，不启用 GPG。
`release` 使用 Central Publishing Plugin 0.12.0 上传到 Sonatype Central Portal，启用 GPG 3.2.8，
并为 JAR 在 package 阶段附加 sources/Javadoc，再在 verify 阶段签名。普通构建不启用 Central/GPG 插件。

三个独立 BOM 不继承仓库 parent，因此各自保留 `release` profile。BOM 只需要 POM 与其签名，不生成 sources/Javadoc JAR。
手动启用 `release` 后，同一 POM 内默认的 `github` profile 停用，Central 插件接管发布。若同一正式版本需要双仓发布，分别执行上述命令。

现有 Framework CI 的 Maven 命令显式固定 `github`，覆盖率任务使用 `-Pgithub,component-coverage`，不会启用 `release`。
这是因为 Maven 激活同一 POM 的其他 profile 时会停用 `activeByDefault` profile；本地组合使用其他 profile 时也应显式带上 `github`。

发布凭证由本机 Maven settings 管理：`github` server 使用具有目标 GitHub Packages 写权限的凭证，
`central` server 使用 Central Portal 生成的用户 token。GPG 使用本机可用的签名私钥及其口令配置，POM 不保存凭证或私钥。

中央库正式版发布前，将本仓库制品与引用的上游 BOM、二方依赖设置为配套的正式版本，并先发布上游制品。
当前源码仍使用 `0.0.1-SNAPSHOT`，日常和 CI 发布使用 GitHub。正式版中央库发布由人手动执行，配置明确设置 `autoPublish=false`，
上传并等待 Portal 校验后，在 Portal 中完成最终 publish。

测试和覆盖率等内部模块通过 `maven.deploy.skip=true` 排除发布。Central 插件的 `skipPublishing` 同样读取该属性，
保证两个目标使用相同的制品范围。

发布配置只做过源码、XML 和 IDE 静态检查；未执行构建、GPG 签名或远程上传。

官方说明：[Central Maven 插件](https://central.sonatype.org/publish/publish-portal-maven/)、
[制品与签名要求](https://central.sonatype.org/publish/requirements/)、
[Maven profile 激活规则](https://maven.apache.org/guides/introduction/introduction-to-profiles.html)。
