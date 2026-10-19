# LoadUp Coverage

内部覆盖率构建模块，使用 JaCoCo Maven 插件生成跨组件报告，并配合父 POM 按每个运行时 jar 检查行、分支覆盖率均 ≥90%。无需 Python 覆盖率工具或 JaCoCo CLI；不提供运行时 API，不向制品仓库部署。

## 手工执行

需要 Java 25、Maven Daemon 和 Docker。从仓库根目录依次执行，第一步失败时先修复，不执行第二步：

```bash
mvnd -B -ntp -T 1 -Pcomponent-coverage clean install \
  -DskipTests=false -Dskip.unit.tests=false -Dskip.integration.tests=false \
  -Dskip.spotless=true -Dskip.spotbugs=true -Dtestcontainers.reuse.enable=false
mvnd -B -ntp -T 1 -Pcomponent-coverage -pl '!loadup-dependencies' --fail-at-end \
  enforcer:enforce@component-coverage-inputs \
  jacoco:check@component-coverage-check
```

第一步执行一次完整构建并安装 reactor 依赖；第二步仅调用命名插件 execution，复用同批 classes 与合并 exec，不运行生命周期，不需要也不能添加 `clean`。排除 `loadup-dependencies` 是因为该 BOM 直接继承 Spring Boot，不继承根 POM 的命名 execution。

上述命令跳过格式和 SpotBugs，供覆盖率验收使用；CI 首步还执行 SpotBugs/PMD，快速质量任务检查格式、License 和依赖。不能通过跳过测试、降低阈值或删除执行文件来作为完整验收。

## 报告

| 文件 | 内容 |
|---|---|
| `target/site/jacoco-aggregate/index.html` | 总览与逐 jar、包、类的可读报告 |
| `target/site/jacoco-aggregate/jacoco.xml` | 原生 XML，供后续平台消费 |
| `target/site/jacoco-aggregate/jacoco.csv` | 原生 CSV |
| `target/jacoco-components.exec` | 所有组件的单测及 IT 合并数据 |

路径相对本模块。覆盖率违规写入第二步 Maven 日志并返回失败；`--fail-at-end` 继续处理不受失败依赖阻断的项目。总平均值不作为验收标准，没有分支的 jar 不产生分支阈值违规。

CI 归档上述报告、原始 unit / IT exec 及 Surefire/Failsafe 报告。完整聚合报告要求构建成功；构建中途失败时可能只有已经完成模块的报告。

## 新增模块或测试

1. 有生产源码的组件 jar 在自己的 POM 添加 `loadup.coverage.skip=false`；纯测试模块不添加。
2. 在本模块 POM 添加组件依赖，生产 jar 使用默认 compile scope，纯测试模块使用 test scope。版本由框架 BOM 管理。
3. 在 `component-execution-data` 中维护预期文件：有 Surefire 用例列 `jacoco-unit.exec`，有 Failsafe 用例列 `jacoco-it.exec`；两类都有则各列一条。移除测试类别时同步删除条目。
4. 新增生成器时只按实际生成类型维护父 POM 排除项，不排除手写 DTO、配置、异常或业务适配器。

本模块不是所有业务模块的覆盖率门禁；`loadup-modules` / commons 可运行测试，但不计入组件报告分母。组件范围及设计限制见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 本次状态

2026-10-19 完成配置迁移，按照用户要求未运行 Maven、测试、格式化、静态检查或文档同步。所有通过结论等待手工验证。
