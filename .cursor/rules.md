# LoadUp 项目 Cursor AI 规则

本文档为 Cursor AI 在 loadup 项目（单仓库 Maven 多模块，基于 Spring Boot 3.4.3）中自动生成或修改代码时的行为规范与检查清单。

简介
- 项目类型：单仓库（monorepo）Maven 多模块项目
- Spring Boot 版本：3.4.3
- 代码包前缀：`io.github.loadup`
- 核心模块（至少包含）：`dependencies`, `commons`, `components`, `modules`, `gateway`, `application`

目标
- 保持统一的代码风格与模块边界
- 避免循环依赖、违反包设计或数据库约定
- 生成的代码应可直接编译、并遵循现有模块的实现模式

---

## 项目结构规则

1. 模块依赖关系（单向）：
   - `commons` → `components` → `modules` → `application`
   - `gateway` 独立（可依赖 `components` 与 `commons`），但不得被 `modules` 或 `application` 依赖为反向依赖
2. 禁止循环依赖（任何模块之间都不得形成 A → B → ... → A 的路径）。在生成或修改代码时，始终检查 `pom.xml` 中的模块依赖和代码中的包引用，确保没有引入反向依赖。
3. 代码包前缀必须为 `io.github.loadup`，子包按照模块名与功能划分（例如：`io.github.loadup.commons.util`，`io.github.loadup.gateway.starter`）。

检查要点：
- 在引入依赖前，先查看根 `pom.xml` 和目标模块的 `pom.xml` 是否允许该依赖。
- 若需要引用其他模块的类，优先考虑放入 `commons`（通用工具/DTO）或 `components`（可复用组件），避免直接跨越多个层级。

---

## 命名规范

- 实体（Entity）：`XxxEntity`（对应数据库表 `t_xxx`，参见数据库规范）
- DTO：`XxxDTO` 或按场景区分 `XxxRequest` / `XxxResponse`
- VO（视图对象）：`XxxVO`
- Service 接口：`XxxService`
- Service 实现：`XxxServiceImpl`
- Mapper 接口（MyBatis Flex）：`XxxMapper`
- Controller：`XxxController`

示例：
- 实体类：`UserEntity` 对应表 `t_user`
- DTO：`UserDTO` 或 `UserCreateRequest` / `UserResponse`

---

## 编码规范

1. Lombok
   - 优先使用 Lombok 注解以减少样板代码：@Data、@Builder、@AllArgsConstructor、@NoArgsConstructor 等。
   - 对于需要不可变对象或特定构造逻辑的类，选择合适的 Lombok 注解组合并补充 JavaDoc。
2. 日志
   - 使用 `@Slf4j` 作为日志注入（非静态 Logger）。
3. 异常处理
   - 统一业务异常类型为 `BusinessException`（位于 `commons` 模块）；所有服务层抛出业务错误时使用该类型。
   - 全局异常处理器为 `GlobalExceptionHandler`（通常位于 `application` 或 `commons` 的 starter 包），必须处理 `BusinessException` 与通用异常，返回统一 `Result<T>`。
4. API 返回规范
   - 控制器统一返回 `Result<T>`（包装成功/失败、错误码、消息与数据）。
5. 参数校验
   - 使用 `@Valid` 与 `jakarta.validation` 注解（如 `@NotNull`, `@NotBlank`, `@Size` 等）；Controller 层入参必须加 `@Valid` 并在 DTO 上声明约束。
6. 并发与性能
   - 对可能的并发数据结构或静态状态，显式说明线程安全性；优先使用线程安全集合或通过无状态服务/局部变量避免共享可变状态。

约束：
- 生成代码必须包含完整 import 语句（不要依赖 IDE 自动补全）。
- 公共（public）方法必须带 JavaDoc，说明输入、返回值和异常。

---

## 数据库规范

1. 持久化框架：MyBatis Flex（项目统一使用）
2. 表名与字段命名：
   - 表名使用小写与下划线：`xxx` 或 `t_xxx`（实体名为 `XxxEntity`，表名建议以 `t_` 前缀明确业务表）
   - 字段名使用 snake_case（例如：`created_at`, `user_name`）
3. 主键：
   - 主键类型为 `String`（非自增），字段名必须为 `id`。
   - 主键由业务或 UUID 等策略生成（生成策略写在公共工具/组件中，优先复用现有工具）。
4. SQL 与映射：
   - Mapper 名称与路径遵循 `io.github.loadup.<module>.mapper` 或 `...mapper` 约定，接口命名 `XxxMapper`。

示例：
- 表：`t_user` 字段：`id varchar(64) primary key, user_name varchar(128), created_at datetime`

---

## 代码生成规则（当被要求创建新功能时）

按以下步骤严格执行：
1. 先在 `commons` 模块查找是否已有可复用工具、DTO、异常类或基础类型（例如：`Result`, `BusinessException`, `DateUtils`, `StringUtils` 等）；优先复用，避免复制粘贴。
2. 若 commons 无合适实现，再检查 `components` 模块是否已有可复用组件（例如缓存、限流、client 封装等）。
3. 在合适的模块中创建代码：
   - 公共工具、基础 DTO、全局异常等放入 `commons`
   - 可复用库/中间件封装放入 `components`
   - 具体业务模块放入 `modules/<business>`
   - 应用启动、配置、全局 `Controller` 或集成在 `application`
   - 网关相关放入 `gateway`（gateway 可依赖 commons/components）
4. 创建代码时遵循现有代码风格与包结构（查看相邻类实现以模仿命名、注解与异常处理习惯）。

生成前检查清单：
- 是否违反模块依赖关系？（检查 pom、package-import）
- 是否已有相似功能／工具可复用？
- 是否包含完整 import 和 JavaDoc？
- 是否包含参数校验与异常处理？

---

## 特别指令（强制执行）

- 生成代码时必须包含完整的 `import` 语句（不要省略）。
- 所有公开方法（public）必须包含 JavaDoc，说明参数、返回值、抛出异常与线程安全约定（若与线程安全相关）。
- 优先考虑线程安全与性能（例如：避免在单例中使用可变全局状态；使用局部不可变变量或线程安全集合）。
- 优先重用已有工具类（示例：`DateUtils`, `StringUtils`, `IdGenerator` 等）；若项目中未存在，先在 `commons` 创建并 add tests/JavaDoc。
- 任何新增的依赖或第三方库，必须先在 `loadup-dependencies` 或根 `pom.xml` 中申明（遵循团队依赖管理策略）。

---

## 质量门（生成/修改代码前后的最小验证）

1. 编译（mvn -q -DskipTests package）应通过（至少本模块编译无错误）。
2. 静态检查（若项目集成 Spotless/Checkstyle/PMD）未引入新的格式或规范错误。
3. 简单单元测试或 smoke 测试覆盖关键路径（如 DTO 序列化、Mapper 基本方法）
4. 检查模块间依赖关系无循环。

---

## 其他提示（供 AI 使用时参考）

- 在生成 Controller/Service/Mapper 时，给出示例请求/响应 DTO 的结构和字段注释。
- 对于复杂逻辑，输出简短的设计说明（1-3 行）说明为何选择该实现并列出替代方案。
- 若需要数据库迁移脚本，遵循仓库内现有 Liquibase/Flyway 迁移文件格式（若存在），并放在对应模块的 `db/migration` 或 `resources/db/migration`。

---

遵守以上规则，Cursor AI 可更可靠地在 loadup 仓库中生成一致、可维护的代码。如需放松或扩展某条规则，请在 PR 中与模块维护者讨论并记录在 `docs/` 或本文件的更新历史部分。
