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

... (truncated for brevity) ...
