# Coverage 架构

## 职责与边界

`loadup-coverage` 是内部、POM packaging 的报告聚合器，通过根 `component-coverage` profile 按需加入。普通构建和 SDK 消费不引入它；默认跳过部署。它负责组件执行数据完整性、全局合并和报告，逐 jar 阈值由父 POM 的 JaCoCo `check` 负责。

## 原生执行链

| 阶段 | 位置 | 行为 |
|---|---|---|
| initialize / pre-integration-test | 各测试模块 | prepare-agent 写入 unit / IT 独立文件；Surefire/Failsafe 使用 `@{argLine}` 延迟读取 agent |
| post-integration-test | 本模块 | Enforcer `requireFilesSize` 校验所有显式列出的预期 exec 存在且非空 |
| verify | 本模块 | JaCoCo `merge` 合并组件 unit / IT 数据，`report-aggregate` 生成 HTML/XML/CSV |
| 后续显式 goal 调用 | 每个运行时组件 | Enforcer 确认 classes 目录与合并数据存在，再执行 JaCoCo BUNDLE 的 LINE / BRANCH COVEREDRATIO ≥0.90 |

依赖边保证本模块在所有组件及独立测试模块之后执行。compile scope 对应生产代码与执行数据，test scope 对应执行数据但不贡献代码分母。业务模块、commons 不作为组件报告的直接依赖或门禁对象。

全局数据合并只读取 `loadup-components/**/target/jacoco-unit.exec` 与 `jacoco-it.exec`；聚合报告同样只读取这两类文件，避免重复加载模块本地 merged 数据。执行文件检查独立于 `skip.enforcer`，快速质量阶段的依赖检查开关不能关闭它。

## 为什么分两次 Maven 调用

同一 reactor 中，组件的 verify 早于兄弟测试模块完成。提前检查会把尚未执行的兄弟用例当作未覆盖。JaCoCo 原生 `check` 读取当前模块的 classes 和指定 exec，并不接受聚合报告 XML。因此先完成整个 reactor 的测试与聚合，再显式逐模块调用 check；第二次只执行插件 goal，不重新编译、测试或合并。

`component-coverage-check` / `component-coverage-inputs` 的 phase 为 `none`，避免第一步提前运行。默认 `loadup.coverage.skip=true`，只有具备生产源码的组件 jar 显式改为 false，父聚合器、业务模块和纯测试模块跳过。独立继承 Spring Boot 的依赖 BOM 不包含这些 execution，第二步通过 Maven 项目选择器排除它。

## 排除规则与约束

父 POM 明列 MyBatis-Flex 的 `dataobject/table`、生成的 `DOMapper` 与 DelayedTask 的 MapStruct 实现，报告和门禁采用同一排除配置。手写源码全部计入；新增生成器需要维护配置，不能为达到阈值排除手写 DTO、异常或自动配置。

原生方案不再提供 Python 工具的动态源码扫描、逐源码字节码完整性比对、陈旧数据硬失败及自定义 Markdown/JSON 摘要。使用显式 Maven 依赖/文件清单和原生 HTML/XML/CSV；新增模块和测试类别必须同步维护清单。预期测试全部跳过而无 exec 也会失败，这符合完整覆盖率验收不能跳过测试的要求。

文件存在检查不等于测试发现数量检查；测试发现、全套 skip、JUnit 条件及 bytecode mismatch 告警仍需要结合测试报告验收。第一步必须从 clean 开始且完整成功，两步间不能修改源码、重新编译或混用历史 exec；单独运行 check 不代表完整验收。

本次配置修改未执行构建或检查，不能宣称原生门禁已通过。
