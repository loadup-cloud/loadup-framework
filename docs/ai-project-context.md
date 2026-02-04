# LoadUp 项目上下文（供 AI 助手使用）

目的：为 AI 助手提供一个可快速理解 LoadUp 仓库的项目级上下文文档，包含项目结构、快速上手、模块职责、常用代码模板与常用工具类位置，帮助 AI 在生成代码或修改代码时保持一致性。

---

## 项目概览

- 项目名称：LoadUp 企业级开发平台
- 仓库类型：单仓库（Monorepo）Maven 多模块
- 主要目的：作为企业级应用的脚手架和平台，提供可复用的组件、规范化的代码模板与模块化的业务实现，帮助内部开发团队快速交付业务功能
- 目标用户：内部开发团队、平台开发者、自动化代码生成工具（如 GitHub Copilot / Cursor / ChatGPT）

---

## 快速开始指南

1. 环境要求
   - Java 21
   - Maven 3.8+
   - MySQL 8.0
   - Redis（用于缓存/会话）

2. 本地运行（示例）

```bash
git clone <repository>
cd loadup-framework
mvn clean install -DskipTests
# 启动管理后台示例模块（按实际模块路径调整）
cd loadup-application/loadup-app-admin
mvn spring-boot:run
```

说明：替换 `<repository>` 为仓库的 Git 地址。若首次构建遇到依赖问题，先执行 `mvn -U clean install` 以刷新依赖。

---

## 代码组织与模块说明（详细）

仓库核心模块（至少包含）：
- `loadup-dependencies`
- `loadup-commons`
- `loadup-components`
- `loadup-modules`
- `loadup-gateway`
- `loadup-application`

每个模块的职责：

1. loadup-dependencies
   - 负责集中管理依赖版本（dependencyManagement），所有第三方库版本在此声明。
   - 不要随意修改该模块中的版本号（见注意事项）。

2. loadup-commons
   - 基础工具与公共类型
   - 包含：常量类（ErrorCode、Constants）、通用 DTO/Result、工具类（DateUtils、StringUtils、JsonUtils）、统一异常（BusinessException）以及通用配置与基础类
   - 常用位置示例（仓库内实际包名以 `io.github.loadup` 为准）

3. loadup-components
   - 基础可复用组件（中间件、适配器）
   - 常见子模块：认证（JWT / 权限验证）、缓存（Redis 封装）、消息（邮件/短信/通知）、文件存储（DFS/上传）、数据库基础扩展
   - 在开发新组件时，先在 `components` 中验证并提供易用的 API，供 `modules` 引用

4. loadup-modules
   - 业务模块集合，按业务域划分
   - 示例：
     - `system`（或 `upms`）：用户、角色、菜单、部门等系统管理功能
     - `business`：具体业务线模块
   - 业务代码应放在 modules，尽量避免把业务逻辑写到 application 或 components

5. loadup-gateway
   - API 网关层，负责路由转发、认证过滤、限流/熔断等边界功能
   - gateway 可依赖 `commons` 与 `components`，但不得被 `modules` 或 `application` 反向依赖

6. loadup-application
   - 应用启动层，组合模块并提供运行时配置
   - 示例子模块：
     - `admin`：管理后台（UI 后端）
     - `api`：对外开放的 REST API 服务

---

## 代码模式示例（典型模板）

说明：下面模板为 AI 生成代码时的参考样式，包含完整 imports、注解与约定。占位符使用 `{ {Module} }` / `{ {Entity} }` / `{ {dto} }` 等，请由 AI 在生成时替换为实际值。

### Controller 模板（示例）

```java
package io.github.loadup.{ {module} }.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.loadup.commons.dto.Result;
import io.github.loadup.{ {module} }.dto.{ {Entity} }Request;
import io.github.loadup.{ {module} }.dto.{ {Entity} }Response;
import io.github.loadup.{ {module} }.service.{ {Entity} }Service;

/**
 * { {Entity} }Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/{ {module} }/{ {entity} }")
@RequiredArgsConstructor
@Tag(name = "{ {Entity} } API", description = "{ {Entity} } 相关接口")
public class { {Entity} }Controller {

    private final { {Entity} }Service { {entity} }Service;

    @Operation(summary = "创建 { {Entity} }")
    @PostMapping
    public ResponseEntity<Result<{ {Entity} }Response>> create(@Valid @RequestBody { {Entity} }Request req) {
        var resp = { {entity} }Service.create(req);
        return ResponseEntity.ok(Result.success(resp));
    }

    // get/list/update/delete 模板方法略
}
```

### Service 模板（示例）

```java
package io.github.loadup.{ {module} }.service;

import io.github.loadup.{ {module} }.dto.{ {Entity} }Request;
import io.github.loadup.{ {module} }.dto.{ {Entity} }Response;

public interface { {Entity} }Service {
    { {Entity} }Response create({ {Entity} }Request request);
}

package io.github.loadup.{ {module} }.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import io.github.loadup.{ {module} }.mapper.{ {Entity} }Mapper;
import io.github.loadup.{ {module} }.entity.{ {Entity} }Entity;
import io.github.loadup.{ {module} }.dto.{ {Entity} }Request;
import io.github.loadup.{ {module} }.dto.{ {Entity} }Response;
import io.github.loadup.{ {module} }.service.{ {Entity} }Service;
import io.github.loadup.commons.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class { {Entity} }ServiceImpl implements { {Entity} }Service {

    private final { {Entity} }Mapper { {entity} }Mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public { {Entity} }Response create({ {Entity} }Request request) {
        // 验证 -> DTO -> Entity -> 持久化 -> 返回
        { {Entity} }Entity e = new { {Entity} }Entity();
        // set fields
        int inserted = { {entity} }Mapper.insert(e);
        if (inserted != 1) {
            throw new BusinessException("创建失败");
        }
        return new { {Entity} }Response();
    }
}
```

### Mapper 模板（示例）

```java
package io.github.loadup.{ {module} }.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loadup.{ {module} }.entity.{ {Entity} }Entity;

@Mapper
public interface { {Entity} }Mapper extends BaseMapper<{ {Entity} }Entity> {
    // 自定义查询方法示例
}
```

### Entity 模板（示例）

```java
package io.github.loadup.{ {module} }.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_{ {entity} }")
public class { {Entity} }Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    @JsonIgnore
    private String password; // 密码字段必须加 @JsonIgnore
}
```

---

## 常用工具类位置（推荐使用仓库中已有实现）

> 注意：仓库中实际包名前缀为 `io.github.loadup`，下列路径请以该前缀为准。

- 日期处理：io.github.loadup.commons.utils.DateUtils
- 字符串处理：io.github.loadup.commons.utils.StringUtils
- JSON 处理：io.github.loadup.commons.utils.JsonUtils
- 验证工具：io.github.loadup.commons.utils.Validator
- 通用响应/DTO：io.github.loadup.commons.dto.Result
- 统一异常：io.github.loadup.commons.exception.BusinessException
- ID 生成器：io.github.loadup.commons.util.IdGenerator (或类似工具)

---

## 当前开发重点（示例，按优先级高->低）

1. system 模块：用户/权限/角色/菜单 完成认证与鉴权整合（高优先级）
2. 缓存组件：Redisson 封装与注解化缓存支持（中高）
3. 网关：完善鉴权过滤器与限流策略（中）
4. 增强测试覆盖：为关键 Service 编写单元与集成测试（中）
5. 文档与模板：完善 Copilot/AI 指令与代码生成模板（中低）

---

## 注意事项与最佳实践（必须遵守）

- 不要直接修改 `loadup-dependencies` 中的版本号，任何依赖变更需通过团队审查。
- 新的通用功能或工具先放 `loadup-components` 中实现并验证，再由 `loadup-modules` 使用。
- 业务逻辑必须放在 `loadup-modules` 下的具体模块中。
- 代码风格：遵循仓库现有格式化和静态检查规则（Spotless / Checkstyle / PMD / SpotBugs）。
- 安全：敏感字段（如 password）在实体与 DTO 中应加 `@JsonIgnore`（或在 DTO 层不返回）；日志中不得打印完整 Token 或密码。
- SQL 安全：避免字符串拼接的 SQL，使用 MyBatis-Flex 的参数化查询（QueryWrapper）或 Mapper 注解/XML 参数化语句。
- 参数校验：Controller 入参使用 `@Valid` 与 jakarta.validation 注解（@NotNull/@NotBlank/@Size 等）。
- 事务：写操作在 ServiceImpl 中使用 `@Transactional(rollbackFor = Exception.class)`。

---

## 当 AI 需要生成新功能时的工作流程（建议步骤）

1. 在 `loadup-commons` 查找是否已有工具/类型可以复用（Result、BusinessException、DTO、工具类等）。
2. 在 `loadup-components` 查找是否有可复用组件（缓存、鉴权、文件存储等）。
3. 在 `loadup-modules` 中选择合适的子模块创建业务实现（遵循包名与模块依赖规则）。
4. 生成代码时：包含完整 `import`，JavaDoc，参数校验注解，并提供单元测试占位。
5. 提交前运行本地构建与测试，并确保没有违反模块依赖规则或违反质量门。

---

如果你希望，我可以：
- 将本文件翻译并生成英文版本放在 `docs/` 下；
- 根据此上下文为指定实体生成完整的代码（Controller/Service/Mapper/Entity/DTO/测试占位）；
- 生成可复用的模板文件供 CI 或脚本使用（Velocity / Freemarker）。

---

文档结束。
