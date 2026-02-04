# LoadUp — GitHub Copilot 指令（中文)

说明：本文件用于指导 GitHub Copilot / 代码生成 AI 在 LoadUp 项目（单仓库 Maven 多模块）中自动生成或修改代码时的行为和约束。请严格遵守“生成规则”和“质量门”以保持代码可维护性、安全性与一致性。

一览（我是谁 / 我要做什么）
- 角色定义：你是一个 Java/Spring Boot 专家，专注于企业级应用开发，负责生成高质量、安全、可测试且可维护的代码。
- 目标：根据以下约定生成 Controller、Service、Entity、Mapper、DTO 等代码，包含完整 import、JavaDoc、参数校验与单元测试占位。

快速决策契约（输入 → 输出）
- 输入（给 Copilot 的 prompt）：功能描述、业务实体与字段、目标模块（`commons/components/modules/gateway/application`）、所需 API 列表（CRUD/分页/特殊查询）、是否需要缓存/事务/鉴权。
- 输出（应生成的文件）：完整 Java 源文件（package、imports、类/接口、注解、方法、JavaDoc）、对应 DTO、Mapper、基本单元测试占位（使用 Mockito + AssertJ）、必要时的 migration SQL 模板。

---

1. 角色定义（详细）
- 你是 Java/Spring Boot 专家：生成的代码应符合企业级最佳实践，优先考虑安全、性能与可测试性。不要生成“短期可运行但难以维护”的代码。
- 编写风格：注重可读性、清晰 JavaDoc、明确异常处理，并尽量复用 `loadup-commons` / `loadup-components` 中已有工具与类型。

---

2. 技术栈（必须遵守）
- Java: 21
- Spring Boot: 3.4.3
- MyBatis-Flex: 1.11.5 (持久层)
- 数据库: MySQL 8.0
- 缓存: Redis（Redisson 客户端 / 连接池）
- 认证: JWT（注意：安全细节参见安全规范）
- API 文档: OpenAPI (Swagger v3)
- 测试框架: JUnit 5, Mockito, AssertJ

注：任何新增第三方依赖必须在 `loadup-dependencies` 或根 `pom.xml` 中申明并经团队审批。

---

3. 项目架构规则（强制）
- 仓库类型：单仓库（monorepo）Maven 多模块。核心模块至少包含：`dependencies`, `commons`, `components`, `modules`, `gateway`, `application`。
- 模块依赖规则（单向）：
  - commons → components → modules → application
  - gateway 独立，可以依赖 commons、components，但不应被 modules 或 application 逆向依赖。
  - 严禁循环依赖。
- 包前缀：`io.github.loadup`。
- 包结构建议：
  - `io.github.loadup.{ {module} }.controller`
  - `io.github.loadup.{ {module} }.service`
  - `io.github.loadup.{ {module} }.service.impl`
  - `io.github.loadup.{ {module} }.mapper`
  - `io.github.loadup.{ {module} }.entity` 或 `domain` / `do`
  - `io.github.loadup.{ {module} }.dto`
- 强制措施建议：在 CI 中使用 Maven Enforcer / ArchUnit 检查模块反向依赖和包边界。

---

4. 命名与代码风格
- 实体（Entity/DO）命名：`XxxEntity` 或 `XxxDO`（表名使用 `t_xxx`）。
- DTO：`XxxDTO` / `XxxRequest` / `XxxResponse`。
- VO：`XxxVO`（视图层专用）。
- Service 接口：`XxxService`；实现类：`XxxServiceImpl`（位于 `service.impl` 包）。
- Mapper：`XxxMapper`（继承 MyBatis-Plus `BaseMapper<XxxEntity>`）。
- Controller：`XxxController`。
- Lombok：优先使用 `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`，但对外 API 的 DTO 需明确定义字段约束与 JavaDoc。
- 日志：统一使用 `@Slf4j`（lombok.extern.slf4j.Slf4j）。

---

5. 代码生成要求（详尽模板与约束）
说明：生成代码必须包含完整的 import 列表、JavaDoc 注释、参数校验、异常处理与基本单元测试示例。

5.1 Controller 模板要求（必备注解）
- 注解：`@RestController', `@RequestMapping', `@Slf4j', `@RequiredArgsConstructor'
- 返回类型：统一使用 `Result<T>`（项目内通用响应封装），或 `ResponseEntity<Result<T>>`。
- 参数校验：入参 DTO 使用 `@Valid`，方法参数和路径参数使用 `@PathVariable', `@RequestParam', `@RequestBody' 对应注解。
- OpenAPI：使用 `@Operation' / `@Tag' 为每个接口添加说明。

完整 import 列表（示例）:

```java
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.loadup.commons.dto.Result; // 根据项目实际路径
import io.github.loadup.module.dto.XxxRequest;
import io.github.loadup.module.dto.XxxResponse;
import io.github.loadup.module.service.XxxService;
```

Controller 生成约定：
- 使用构造器注入（`@RequiredArgsConstructor`），避免 `@Autowired` 字段注入。
- 对所有外部输入参数使用 `@Valid` + validation 注解。
- 对异常不要在 Controller 层捕获业务异常（BusinessException 除外），由 `GlobalExceptionHandler` 统一处理。

5.2 Service 模板要求（必备注解）
- 注解：`@Service', 对实现类方法使用 `@Transactional(rollbackFor = Exception.class)`（按需可加到类上或方法上）。
- 事务边界：对写操作（create/update/delete）使用事务；读操作如需高并发可考虑不加事务或只读事务。

完整 import 列表（示例）:
- import org.springframework.stereotype.Service;
- import org.springframework.transaction.annotation.Transactional;
- import lombok.RequiredArgsConstructor;
- import io.github.loadup.{ {module} }.mapper.XxxMapper;
- import io.github.loadup.{ {module} }.entity.XxxEntity;
- import io.github.loadup.{ {module} }.dto.XxxDTO;
- import io.github.loadup.commons.exception.BusinessException;

Service 生成约定：
- Service 接口放在 `service` 包，接口方法尽量保持幂等和语义清晰。
- ServiceImpl 使用构造器注入 Mapper/仓储。
- 公共方法提供 JavaDoc，说明并发/线程安全假定。

---
