# LoadUp Framework

LoadUp 是面向 Spring Boot 应用的可复用框架与 SDK。项目通过 BOM 统一依赖版本，集成方按需引入技术组件和通用业务模块，在自己的应用中组合所需能力。

[阅读文档](https://loadup-docs.laysan.site/) · [技术组件](loadup-components/README.md) · [业务模块](loadup-modules/README.md) · [路线图](ROADMAP.md)

## 项目概况

- **按需集成**：缓存、认证、文件存储等组件可以独立选择；提供多种后端的能力通过 binder 选择实现。
- **遵循 Spring 生态**：业务接口使用 Spring MVC Controller；缓存等能力优先采用已有的标准接口。
- **通用业务能力**：UPMS 提供用户、权限与账号安全管理；审计中心记录关键操作；数据字典支持动态维护业务选项；文件资源管理复用 DFS 存储；站内通知接入 Gotone 渠道；导入导出任务由 Scheduler 唤醒数据库扫描器并复用文件资源；DelayedTask 保存业务截止时间并提供租约恢复与重试。
- **统一版本管理**：`loadup-dependencies` BOM 管理 LoadUp 模块及相关依赖版本。

LoadUp 是供其他应用消费的 SDK。`loadup-application` 用于本地集成验证，不是生产部署单元。

## 能力与目录

| 目录 | 内容 |
| --- | --- |
| [`loadup-dependencies`](loadup-dependencies/README.md) | 统一依赖版本的 BOM |
| [`loadup-commons`](loadup-commons/README.md) | DTO、工具和日志基础能力 |
| [`loadup-components`](loadup-components/README.md) | Web MVC、认证授权、指标追踪、缓存、数据库、分布式锁、文件存储、通知、调度等技术组件 |
| [`loadup-modules`](loadup-modules/README.md) | 可复用业务模块，包含 UPMS、审计中心、数据字典、文件资源、站内通知、导入导出任务、商户资料及动态合约管理 |
| [`loadup-testify`](loadup-testify/README.md) | 集成测试辅助能力 |
| [`loadup-application`](loadup-application/README.md) | 本地集成验证启动器 |

模块的接入方式、配置项和设计说明见各目录的 README 与 ARCHITECTURE 文档，也可在[文档站](https://loadup-docs.laysan.site/docs/)按能力查找。

## 快速接入

项目当前使用 **Java 25**、**Spring Boot 4.1.1** 和 **Maven**。在消费工程中先导入 BOM：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.loadup-cloud</groupId>
      <artifactId>loadup-dependencies</artifactId>
      <version>0.0.2-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

然后按需添加具体模块。例如，为 Spring MVC 业务接口引入统一响应与错误处理：

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-webmvc</artifactId>
</dependency>
```

其他模块无需重复声明受 BOM 管理的版本。当前版本为 `SNAPSHOT`，接入前需要确保 BOM 和所选模块已安装在本地 Maven 仓库，或已发布到你使用的制品仓库。具体配置请参阅[组件文档](loadup-components/README.md)。

## 架构边界

依赖方向为 `commons → components → modules → 集成方应用`。业务模块按需提供 Spring MVC Web 适配；技术组件的不同后端通过独立 binder 接入。项目不会要求集成方使用 `loadup-application` 作为自己的部署入口。

## 许可证

本项目采用 [Apache License 2.0](LICENSE)。

## 技术组件目录

组件源码按 `security`、`web`、`data`、`integration`、`reliability`、`execution`、`platform` 七类组织；分类目录不改变 Maven 坐标，仍可按需独立引入。完整索引见 [技术组件](loadup-components/README.md)。

错误契约由 [commons-error](loadup-commons/loadup-commons-error/README.md) 统一提供；业务错误枚举位于各模块的 client.error，HTTP 报文由 commons-dto/WebMVC 装配。

## 文档同步

文档站默认位于兄弟目录 `../docs`，其他位置可传入 `--site /path/to/docs`。

`doc-sync-agent.py` 按根 `pom.xml` 的 `<modules>` 逐层发现模块，使用分类后的真实组件路径；不会扫描或创建旧的组件源码目录。每个模块的 README 和 ARCHITECTURE 合并为一页，文档站首页与文档入口页手工维护。

```bash
python3 doc-sync-agent.py          # Preview changes
python3 doc-sync-agent.py --apply  # Synchronize generated Markdown
python3 doc-sync-agent.py --check  # Fail on stale generated pages
python3 doc-sync-agent.py --build  # Synchronize and build Hugo
python3 -m unittest discover -s tools -p test_doc_sync.py
```

脚本只清理带生成标记的过期页面及其空父目录；`--build` 通过 Hugo 清理旧输出。IDEA 的 `.iml` 不属于文档产物，移动模块后需按根 POM 同步 Maven 项目。

## Maven 质量检查

使用 Maven Daemon（`mvnd`）。Spotless 负责 Palantir 格式与 import 清理；Checkstyle 使用 `config/quality/checkstyle.xml` 检查空语句、equals/hashCode、switch fall-through 和 import 等结构规范，不叠加 Sun 格式规则。Checkstyle 引擎支持 Java 25，PMD 排除 `target/generated-sources` 和 `target/generated-test-sources`，生成文件由 APT 管理。

本地统一使用根目录脚本，默认检查全部模块及六项质量门禁：SpotBugs、Spotless、License、Enforcer、PMD 和 Checkstyle。

```bash
./quality-check.sh                                      # Default: all modules and checks
./quality-check.sh --dry-run                            # Preview without running Maven
./quality-check.sh -m :loadup-components-scheduler-api   # Selected module and dependencies
./quality-check.sh -m :loadup-components-http --checks spotbugs,spotless
```

`--all` 可显式指定全量，不能与 `--modules` 同时使用；模块选择支持 Maven 路径、`:artifactId` 和逗号分隔列表。脚本从任意目录调用均定位到项目根目录，使用一次 `mvnd clean verify`，按需追加 PMD/Checkstyle 门禁；插件版本与规则取自根 POM。定向检查包含工作区 BOM 和依赖，避免使用本地安装的旧 BOM。

脚本跳过单测和集成测试，不自动格式化或更新 License 头；检查仍会编译和打包以提供 SpotBugs 所需产物。格式修复请单独执行 `spotless:apply`。日志保存为根目录已忽略的 `quality-check-*.log`，Maven 失败返回原退出码；不会回退到 `mvn`。

报告位于各模块 `target/spotbugsXml.xml`、`target/pmd.xml` 和 `target/checkstyle-result.xml`。SpotBugs 排除项集中在 `config/quality/spotbugs-exclude.xml`，已知误报应限定到具体规则和责任边界，并说明理由；commons 手写代码参与扫描。

## GitHub CI

[Build and Test](.github/workflows/ci.yml) 在 `main`、`develop`、`dev` 的 push / PR 及手动触发时运行。Linux runner 使用 Temurin 25、经 SHA-256 校验的 mvnd 1.0.6 和 Maven 依赖缓存；PR 更新取消同分支的旧运行。缓存恢复后清理 Maven 的 `*.lastUpdated` 失败标记；CI 的 mvnd 命令使用 `-U` 重新检查缺失的 release 及更新的 SNAPSHOT，已下载的正常制品继续复用。

- **变更范围**：轻量任务运行 Python 工具回归并比较 PR 的 merge-base 差异或 push 的 before/after。仅修改模块 README/ARCHITECTURE/AGENTS、根 ROADMAP/LICENSE/NOTICE 及文档同步工具时，跳过后端 Java、Docker、Maven 与发布任务；混合后端改动、未知文件、手动运行或比较历史缺失时运行后端检查。删除/改名也参与判断，资源目录中的普通 Markdown 模板仍属于后端改动。
- **Backend Fast Quality**：与测试主任务并行，通过 validate + 独立门禁执行 Spotless、License、Enforcer 和 Checkstyle，不编译项目。
- **Backend Tests and Analysis**：一次 `-Pcomponent-coverage clean install` 执行单测、真实容器 IT、SpotBugs 和 PMD。`loadup-coverage` 使用 JaCoCo 原生 `merge` / `report-aggregate` 合并组件与兄弟测试模块的数据，Enforcer 拒绝预期执行文件缺失或为空。构建成功后，复用同一批 classes/exec，显式调用原生 `jacoco:check`，要求每个组件 jar 的行、分支覆盖率均 ≥90%，不重复编译或测试。报告和原始执行数据无论成功失败均归档 14 天；完整聚合报告需要测试构建成功。操作说明见 [覆盖率模块](loadup-coverage/README.md)。
- **Tests, Quality and Coverage**：保留原必需检查名称作为轻量汇总门禁。纯前端/文档改动明确返回成功；后端改动必须同时通过快速质量和测试任务，检测失败、检查失败/取消不会放行。
- **依赖安全**：PR 的 Dependency Review 阻止新增 high / critical 漏洞。仓库不再声明或执行 OWASP `dependency-check-maven` 聚合扫描，也不再需要 `NVD_API_KEY` 与 OWASP 数据缓存；如需 NVD 深度扫描，应在独立流水线或上游执行。PR 检查需要启用 GitHub Dependency Graph，私有仓库还需具备 Dependency Review 权限。参见 [GitHub 配置说明](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/manage-your-dependency-security/configure-dependency-review-action)。
- **发布**：默认关闭。设置仓库变量 `PUBLISH_PACKAGES=true`、`PACKAGES_USERNAME` 以及具有目标仓库访问和 `write:packages` 权限的 secret `PACKAGES_TOKEN` 后，仅存在后端改动的 `main` / `develop` push 且上述验证全部通过才发布至既有 `loadup-cloud/loadup-packages`。跨仓库发布凭证仅在发布步骤注入，PR 不接收此凭证；不再复用 OSSRH 凭证。发布重新 clean 编译并跳过已执行的测试，不复用其他 job 的 target。

仓库维护者应将 **Tests, Quality and Coverage** 与 **Dependency Review** 设为 PR 合并必需检查。前端在独立的 `loadup-admin` 仓库执行 Node/pnpm CI；Hugo 站点在并列的 `docs` 仓库。本仓库不配置 npm Dependabot，也不构建前端或文档站。
