# LoadUp — GitHub Copilot 指令（中文）

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
- MyBatis-Plus: 3.5.4.1 (持久层)
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
  - `io.github.loadup.{{module}}.controller`
  - `io.github.loadup.{{module}}.service`
  - `io.github.loadup.{{module}}.service.impl`
  - `io.github.loadup.{{module}}.mapper`
  - `io.github.loadup.{{module}}.entity` 或 `domain` / `do`
  - `io.github.loadup.{{module}}.dto`
- 强制措施建议：在 CI 中使用 Maven Enforcer / ArchUnit 检查模块反向依赖和包边界。

---

4. 命名与代码风格
- 实体（Entity/DO）命名：`XxxEntity` 或 `XxxDO`（表名使用 `t_xxx`）。
- DTO：`XxxDTO` / `XxxRequest` / `XxxResponse`。
- VO：`XxxVO`（视图层专用）。
- Service 接口：`XxxService`；实现类：`XxxServiceImpl`（位于 `service.impl` 包）。
- Mapper：`XxxMapper`（继承 MyBatis-Flex `BaseMapper<XxxEntity>`）。
- Controller：`XxxController`。
- Lombok：优先使用 `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`，但对外 API 的 DTO 需明确定义字段约束与 JavaDoc。
- 日志：统一使用 `@Slf4j`（lombok.extern.slf4j.Slf4j）。

---

5. 代码生成要求（详尽模板与约束）
说明：生成代码必须包含完整的 import 列表、JavaDoc 注释、参数校验、异常处理与基本单元测试示例。

5.1 Controller 模板要求（必备注解）
- 注解：`@RestController`, `@RequestMapping`, `@Slf4j`, `@RequiredArgsConstructor`
- 返回类型：统一使用 `Result<T>`（项目内通用响应封装），或 `ResponseEntity<Result<T>>`。
- 参数校验：入参 DTO 使用 `@Valid`，方法参数和路径参数使用 `@PathVariable', `@RequestParam`, `@RequestBody` 对应注解。
- OpenAPI：使用 `@Operation` / `@Tag` 为每个接口添加说明。

完整 import 列表（示例）:
- import org.springframework.web.bind.annotation.RestController;
- import org.springframework.web.bind.annotation.RequestMapping;
- import org.springframework.web.bind.annotation.GetMapping;
- import org.springframework.web.bind.annotation.PostMapping;
- import org.springframework.web.bind.annotation.PutMapping;
- import org.springframework.web.bind.annotation.DeleteMapping;
- import org.springframework.web.bind.annotation.PathVariable;
- import org.springframework.web.bind.annotation.RequestParam;
- import org.springframework.web.bind.annotation.RequestBody;
- import org.springframework.http.ResponseEntity;
- import jakarta.validation.Valid;
- import lombok.RequiredArgsConstructor;
- import lombok.extern.slf4j.Slf4j;
- import io.swagger.v3.oas.annotations.Operation;
- import io.swagger.v3.oas.annotations.tags.Tag;
- import io.github.loadup.commons.dto.Result; // 根据项目实际路径
- import io.github.loadup.{{module}}.dto.XxxRequest;
- import io.github.loadup.{{module}}.dto.XxxResponse;
- import io.github.loadup.{{module}}.service.XxxService;

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
- import io.github.loadup.{{module}}.mapper.XxxMapper;
- import io.github.loadup.{{module}}.entity.XxxEntity;
- import io.github.loadup.{{module}}.dto.XxxDTO;
- import io.github.loadup.commons.exception.BusinessException;

Service 生成约定：
- Service 接口放在 `service` 包，接口方法尽量保持幂等和语义清晰。
- ServiceImpl 使用构造器注入 Mapper/仓储。
- 公共方法提供 JavaDoc，说明并发/线程安全假定。

5.3 实体类（Entity）模板要求
- 实体必须实现 `java.io.Serializable`。
- 使用 Lombok 注解（`@Data', `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`）。
- 使用 MyBatis-Plus 注解映射表与 id（`@TableName', `@TableId`）。

完整 import 列表（示例）:
- import java.io.Serializable;
- import com.baomidou.mybatisplus.annotation.TableName;
- import com.baomidou.mybatisplus.annotation.TableId;
- import com.baomidou.mybatisplus.annotation.IdType;
- import lombok.Data;
- import lombok.Builder;
- import lombok.NoArgsConstructor;
- import lombok.AllArgsConstructor;
- import com.fasterxml.jackson.annotation.JsonIgnore; // 如需隐藏字段

实体约定：
- 表名使用 `t_xxx`（snake_case），字段使用 snake_case。主键字段名为 `id`，类型为 `String`（非自增），由 `IdGenerator` 或 UUID/Snowflake 生成。
- 敏感字段（如 password）标注 `@JsonIgnore`，且在日志或 toString 中避免打印。

5.4 Mapper（MyBatis-Plus）模板要求
- 继承 `com.baomidou.mybatisplus.core.mapper.BaseMapper<XxxEntity>`。
- 标注 `@Mapper` 或在 MyBatis 扫描配置中包含该包。

完整 import 列表（示例）:
- import org.apache.ibatis.annotations.Mapper;
- import com.baomidou.mybatisplus.core.mapper.BaseMapper;
- import io.github.loadup.{{module}}.entity.XxxEntity;

Mapper 生成约定：
- 提供符合业务语义的自定义方法时，使用参数化查询并写好 SQL 注释。
- 避免在 Mapper 中编写动态字符串拼接的 SQL，优先使用 MyBatis-Plus 提供的 Wrapper 或注解参数化查询。

---

6. 安全规范（强制）
- 密码字段：实体/DTO 中密码字段必须标注 `@JsonIgnore`，并尽量在 DTO 层不返回密码字段。
- 敏感信息脱敏：对输出（日志/接口响应）中包含的敏感信息（手机号、身份证号、邮箱等）进行脱敏处理（公用工具 `commons` 中应提供脱敏方法）。
- 输入校验：所有外部接口入参必须进行校验（`@Valid` + `@NotNull/@NotBlank/@Size` 等）。
- SQL 安全：严禁字符串拼接形成 SQL；所有 SQL 使用参数化查询（MyBatis-Plus wrapper/API 或注解方式）。
- JWT：Token 验证应在过滤器/拦截器层进行，控制器方法只聚焦业务逻辑并声明必要权限注解（如 `@PreAuthorize`）。
- 日志安全：禁止将完整 Token、密码或敏感字段写入日志；对异常栈中的敏感信息做脱敏。
- 依赖安全：CI 中集成 SCA（如 OWASP Dependency-Check），发现高危依赖必须阻断合并。

---

7. 性能建议（高优先级）
- 缓存：对读多写少的数据使用 Redis（Redisson）或本地缓存（Caffeine），并定义清晰的失效与一致性策略。
- 批量操作：对大量写入使用批量接口或 MyBatis-Flex 的 `insertBatch` / `updateBatch` 等方法，注意分批大小与事务边界。
- N+1 问题：在数据模型设计或查询中避免 N+1 查询，使用 join 或批量获取策略。
- 分页查询：对大数据量使用游标/分页策略（限制单页大小），并在 SQL 添加合理索引。
- 连接池与资源：调优 HikariCP、设置合理的最大连接数，避免过度并发导致数据库连接耗尽。

---

8. 测试要求（代码生成时必须提供测试占位／示例）
- 单元测试：对 Service 的公共方法编写单元测试，使用 JUnit5 + Mockito 模拟依赖并使用 AssertJ 进行断言。
- 集成测试：必要时写基于 Testcontainers 的集成测试，验证 Mapper 与真实 MySQL 行为。
- 测试风格：测试方法命名清晰（given_when_then），每个测试保证独立可重复。
- 示例 imports（单元测试）:
  - import org.junit.jupiter.api.Test;
  - import org.mockito.InjectMocks;
  - import org.mockito.Mock;
  - import org.mockito.junit.jupiter.MockitoExtension;
  - import static org.mockito.Mockito.*;
  - import static org.assertj.core.api.Assertions.*;

---

9. 质量门（CI 检查清单）
- 构建通过：`mvn -T 1C -DskipTests=false clean package`。
- 测试通过：单元+集成测试绿色。
- 静态检查：Spotless / Checkstyle / PMD / SpotBugs 通过。
- 代码覆盖率：达到项目设定阈值（建议 ≥ 70%）。
- 依赖扫描：无高危漏洞。
- 模块依赖校验：无循环依赖，遵循模块依赖规则。

---

10. PR / 合并前清单（开发者或自动化校验）
- 本地构建并运行全部测试。
- 运行格式化与静态检查工具并修复警告。
- 更新/添加必要的 migration SQL（若涉及 DB 变更），并在 PR 描述中说明回滚策略。
- 在 PR 描述中列出：变更摘要、测试说明、性能影响、是否有兼容性风险、审查者。
- 若涉及安全、性能或数据库变更，需至少一名相应领域负责人审批。

---

11. 代码模板示例（可直接被 Copilot 展开，包含完整 import）

说明：以下为可复制粘贴模板片段，使用时用实际包名替换 {{module}} 和 {{Entity}} 等占位符。模板遵循本文件中所有必须的注解与约定。

11.1 Controller 示例（模板）

package io.github.loadup.{{module}}.controller;

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
import io.github.loadup.commons.dto.Result;
import io.github.loadup.{{module}}.dto.{{Entity}}Request;
import io.github.loadup.{{module}}.dto.{{Entity}}Response;
import io.github.loadup.{{module}}.service.{{Entity}}Service;

/**
 * {{Entity}}Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/{{module}}/{{entity}}")
@RequiredArgsConstructor
@Tag(name = "{{Entity}} API", description = "{{Entity}} 相关接口")
public class {{Entity}}Controller {

    private final {{Entity}}Service {{entity}}Service;

    /**
     * 创建 {{Entity}}
     */
    @Operation(summary = "创建 {{Entity}}")
    @PostMapping
    public ResponseEntity<Result<{{Entity}}Response>> create(@Valid @RequestBody {{Entity}}Request request) {
        {{Entity}}Response resp = {{entity}}Service.create(request);
        return ResponseEntity.ok(Result.success(resp));
    }

    // ... 其他接口（get/list/update/delete）
}


11.2 Service + ServiceImpl 示例（模板）

package io.github.loadup.{{module}}.service;

import io.github.loadup.{{module}}.dto.{{Entity}}Request;
import io.github.loadup.{{module}}.dto.{{Entity}}Response;

/**
 * {{Entity}}Service 接口
 */
public interface {{Entity}}Service {

    /**
     * 创建 {{Entity}}
     * @param request 请求 DTO
     * @return 响应 DTO
     */
    {{Entity}}Response create({{Entity}}Request request);
}


package io.github.loadup.{{module}}.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import io.github.loadup.{{module}}.mapper.{{Entity}}Mapper;
import io.github.loadup.{{module}}.entity.{{Entity}}Entity;
import io.github.loadup.{{module}}.dto.{{Entity}}Request;
import io.github.loadup.{{module}}.dto.{{Entity}}Response;
import io.github.loadup.{{module}}.service.{{Entity}}Service;
import io.github.loadup.commons.exception.BusinessException;

/**
 * {{Entity}}ServiceImpl
 */
@Service
@RequiredArgsConstructor
public class {{Entity}}ServiceImpl implements {{Entity}}Service {

    private final {{Entity}}Mapper {{entity}}Mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public {{Entity}}Response create({{Entity}}Request request) {
        // TODO: 先校验入参 -> 转换 -> 持久化 -> 返回 DTO
        {{Entity}}Entity entity = new {{Entity}}Entity();
        // ... set fields
        int inserted = {{entity}}Mapper.insert(entity);
        if (inserted != 1) {
            throw new BusinessException("创建失败");
        }
        return new {{Entity}}Response();
    }
}


11.3 Entity 示例（模板）

package io.github.loadup.{{module}}.entity;

import java.io.Serializable;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {{Entity}} 实体映射 t_{{entity}}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("t_{{entity}}")
public class {{Entity}}Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)  // ID 由审计功能自动生成
    private String id;

    private String name;

    @JsonIgnore
    private String password; // 密码字段必须加 @JsonIgnore

    // ... 其他字段，字段名使用 snake_case 映射
}    // ... 其他字段，字段名使用 snake_case 映射
}


11.4 Mapper 示例（模板）

package io.github.loadup.{{module}}.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.core.BaseMapper;
import io.github.loadup.{{module}}.entity.{{Entity}}Entity;

@Mapper
public interface {{Entity}}Mapper extends BaseMapper<{{Entity}}Entity> {
    // 如果需要自定义 SQL，请使用注解参数或 XML（参数化），避免字符串拼接
}


11.5 单元测试 示例（ServiceTest 模板）

package io.github.loadup.{{module}}.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class {{Entity}}ServiceTest {

    @Mock
    private io.github.loadup.{{module}}.mapper.{{Entity}}Mapper {{entity}}Mapper;

    @InjectMocks
    private io.github.loadup.{{module}}.service.impl.{{Entity}}ServiceImpl {{entity}}Service;

    @Test
    public void create_shouldSucceed_whenValidRequest() {
        // arrange
        io.github.loadup.{{module}}.dto.{{Entity}}Request req = new io.github.loadup.{{module}}.dto.{{Entity}}Request();
        // ... set fields
        when({{entity}}Mapper.insert(any())).thenReturn(1);

        // act
        var resp = {{entity}}Service.create(req);

        // assert
        assertThat(resp).isNotNull();
        // ... 更多断言
    }
}

---

12. 额外说明与最佳实践（供 Copilot 使用时参考）
- 优先复用 `loadup-commons` 中的 `Result`, `BusinessException`, `IdGenerator`, `DateUtils`, `StringUtils` 等公用工具。
- 对于需要分页的接口，使用统一分页 DTO（page/size/sort），返回标准化分页响应。
- 如果生成 DB 变更脚本，请一并生成回滚脚本，并在 PR 中说明数据迁移步骤。
- 对于可能引起大量数据扫描的接口，提示开发者评审索引与查询计划（EXPLAIN）。

---

13. 可选后续工作（我可以帮你做）
- 将此文件同步到 `docs/` 下并生成英文版。
- 为常见 CRUD 场景生成可复用代码模板（Groovy/Velocity/FreeMarker 模板）。
- 在 CI 中添加 ArchUnit/Maven Enforcer 的示例配置并提交 PR。

---

遵循本指令有助于让 Copilot 生成符合 LoadUp 代码风格、可维护与安全的代码。对于模板中的占位符（`<module>`, `<Entity>` 等），请用实际模块及实体名替换；如需我直接为某个业务实体生成代码与测试，请提供实体字段与目标模块。
