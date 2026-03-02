# loadup-dependencies（BOM）

`loadup-dependencies` 模块用于集中管理依赖版本（BOM），所有组件与子项目通过该模块进行统一版本管理。

注意事项：
- 不要直接修改 `loadup-dependencies` 中的版本，任何依赖升级应通过团队审查与 PR 流程。
- 若添加第三方库，需要在 `loadup-dependencies/pom.xml` 中声明并经过审批。

查看 `loadup-dependencies/pom.xml` 了解当前版本锁定策略。
