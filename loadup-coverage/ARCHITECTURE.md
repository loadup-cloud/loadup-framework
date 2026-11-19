# Coverage 架构

## 职责与边界

`loadup-coverage` 是内部、POM packaging 的报告聚合器，通过根 `component-coverage` profile 按需加入。普通构建和 SDK
消费不引入它；默认跳过部署。父 POM 校验组件执行数据完整性和逐 jar 覆盖率阈值，本模块负责全局合并和报告。

## 原生执行链

| 阶段                              | 位置           | 行为                                                                                               |
|-----------------------------------|----------------|----------------------------------------------------------------------------------------------------|
| initialize / pre-integration-test | 各测试模块     | prepare-agent 写入 unit / IT 独立文件；Surefire/Failsafe 使用 `@{argLine}` 延迟读取 agent          |
| verify                            | 本次构建的组件 | Ant 按测试源码命名发现预期套件，校验对应 exec 存在且非空                                          |
| verify                            | 本模块         | JaCoCo `merge` 合并组件 unit / IT 数据，`report-aggregate` 生成 HTML/XML/CSV                       |
| 后续显式 goal 调用                | 每个运行时组件 | Enforcer 确认 classes 目录与合并数据存在，再执行 JaCoCo BUNDLE 的 LINE / BRANCH COVEREDRATIO ≥0.90 |

依赖边保证本模块在所有组件及独立测试模块之后执行。compile scope 对应生产代码与执行数据，test scope
对应执行数据但不贡献代码分母。业务模块、commons 不作为组件报告的直接依赖或门禁对象。

全局数据合并只读取 `loadup-components/**/target/jacoco-unit.exec` 与 `jacoco-it.exec`；聚合报告同样只读取这两类文件，避免重复加载模块本地
merged 数据。执行文件检查独立于 `enforcer.skip`，快速质量阶段的依赖检查开关不能关闭它。

## 为什么分两次 Maven 调用

同一 reactor 中，组件的 verify 早于兄弟测试模块完成。提前检查会把尚未执行的兄弟用例当作未覆盖。JaCoCo 原生 `check` 读取当前模块的
classes 和指定 exec，并不接受聚合报告 XML。因此先完成整个 reactor 的测试与聚合，再显式逐模块调用 check；第二次只执行插件
goal，不重新编译、测试或合并。

`component-coverage-check` / `component-coverage-inputs` 的 phase 为 `none`，避免第一步提前运行。默认
`loadup.coverage.skip=true`，只有具备生产源码的组件 jar 显式改为 false，父聚合器、业务模块和纯测试模块跳过。独立继承 Spring
Boot 的依赖 BOM 不包含这些 execution，第二步通过 Maven 项目选择器排除它。

## 排除规则与约束

父 POM 明列 MyBatis-Flex 的 `dataobject/table`、生成的 `DOMapper` 与 Task 的 MapStruct
实现，报告和门禁采用同一排除配置。手写源码全部计入；新增生成器需要维护配置，不能为达到阈值排除手写 DTO、异常或自动配置。

启用 `component-coverage` 后，父 POM 只在本次构建的 `loadup-components` 非 POM 模块检查
`project.build.testSourceDirectory`。命名规则与父 POM 的 Surefire/Failsafe 共用：`*Test.java`
（排除 `*IntegrationTest.java`）对应 unit，`*IT.java` 和 `*IntegrationTest.java` 对应 IT。
只有发现相应源码时才要求该类别的 exec；发现依据不是测试报告，因此整类测试被跳过且没有生成 exec 时也会失败。
这是按命名检查，不解析 JUnit 注解；只用作夹具且没有可运行套件的源码应避免这些后缀。

文件存在检查不等于测试发现数量检查；测试发现、全套 skip、JUnit 条件及 bytecode mismatch 告警仍需要结合测试报告验收。第一步必须从
clean 开始且完整成功，两步间不能修改源码、重新编译或混用历史 exec；单独运行 check 不代表完整验收。

待验收：当前版本的真实覆盖率产物、模块聚合清单及原生门禁结果。
