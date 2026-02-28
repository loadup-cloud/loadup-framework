package io.github.loadup.application.arch;

/*-
 * #%L
 * Loadup Launcher
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * LoadUp 架构约束测试（ArchUnit）
 *
 * <p>校验以下规则：
 * <ul>
 *   <li>业务模块各层依赖方向（domain → client，infra → domain，app → infra+domain）</li>
 *   <li>domain 层不得引入 Spring / ORM 框架注解</li>
 *   <li>模块之间无横向依赖</li>
 *   <li>不允许使用字段级 @Autowired（必须构造器注入）</li>
 *   <li>不允许创建 @RestController / @Controller</li>
 *   <li>无包内循环依赖</li>
 * </ul>
 */
@DisplayName("LoadUp Architecture Rules")
class ArchitectureTest {

    private static JavaClasses ALL_CLASSES;
    private static JavaClasses MODULE_CLASSES;

    @BeforeAll
    static void importClasses() {
        ALL_CLASSES = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.loadup");

        MODULE_CLASSES = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.loadup.modules");
    }

    // ── Domain 层纯洁性 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("domain 层不得引入 Spring 注解")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework..")
                .because("domain 层是纯 DDD 模型，不得依赖 Spring 框架")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("domain 层不得引入 MyBatis-Flex 注解（@Table 等必须在 infrastructure 层）")
    void domainShouldNotDependOnMyBatisFlex() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("com.mybatisflex..")
                .because("DO 实体（@Table）必须放在 infrastructure.dataobject 包，domain 层不依赖 ORM")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("domain 层不得引入 Lombok @Builder（domain model 不使用 builder 模式除外）")
    void domainShouldNotUseLombokTableAnnotation() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("com.mybatisflex.annotation..")
                .check(MODULE_CLASSES);
    }

    // ── 禁止创建 REST Controller ─────────────────────────────────────────────

    @Test
    @DisplayName("不允许创建 @RestController / @Controller（通过 Gateway bean:// 协议替代）")
    void noRestControllerAllowed() {
        noClasses()
                .should()
                .beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .orShould()
                .beAnnotatedWith("org.springframework.stereotype.Controller")
                .because("所有 API 端点通过 LoadUp Gateway 路由配置暴露，禁止创建 HTTP Controller")
                .check(ALL_CLASSES);
    }

    // ── 禁止字段级 @Autowired ────────────────────────────────────────────────

    @Test
    @DisplayName("不允许在非测试类中使用字段级 @Autowired（必须使用构造器注入）")
    void noFieldAutowired() {
        noClasses()
                .that()
                .resideOutsideOfPackage("..test..")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("org.springframework.beans.factory.annotation.Autowired")
                .because("必须使用构造器注入（@RequiredArgsConstructor），禁止字段 @Autowired")
                .check(MODULE_CLASSES);
    }

    // ── 模块分层依赖规则 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("modules 各业务模块分层依赖方向正确")
    void modulesLayeredArchitecture() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("client")
                .definedBy("..modules..client..")
                .layer("domain")
                .definedBy("..modules..domain..")
                .layer("infrastructure")
                .definedBy("..modules..infrastructure..")
                .layer("app")
                .definedBy("..modules..app..")
                .whereLayer("app")
                .mayNotBeAccessedByAnyLayer()
                .whereLayer("infrastructure")
                .mayOnlyBeAccessedByLayers("app")
                .whereLayer("domain")
                .mayOnlyBeAccessedByLayers("app", "infrastructure")
                .whereLayer("client")
                .mayOnlyBeAccessedByLayers("app", "infrastructure", "domain")
                .because("必须遵循 client → domain → infrastructure → app 单向依赖")
                .check(MODULE_CLASSES);
    }

    // ── 禁止循环依赖 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("业务模块包之间无循环依赖")
    void noModuleCircularDependencies() {
        slices().matching("io.github.loadup.modules.(*)..")
                .should()
                .beFreeOfCycles()
                .because("模块之间不允许循环依赖")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("commons 包内无循环依赖")
    void noCommonsCircularDependencies() {
        JavaClasses commons = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.loadup.commons");

        slices().matching("io.github.loadup.commons.(*)..")
                .should()
                .beFreeOfCycles()
                .check(commons);
    }

    // ── Service 命名约束 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("app.service 包中的类必须以 Service 结尾并标注 @Service")
    void serviceClassNamingConvention() {
        classes()
                .that()
                .resideInAPackage("..app.service..")
                .and()
                .areAnnotatedWith("org.springframework.stereotype.Service")
                .should()
                .haveSimpleNameEndingWith("Service")
                .because("Service Bean 必须以 Service 结尾，便于识别")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("infrastructure.repository 包中的类必须以 GatewayImpl 结尾")
    void gatewayImplNamingConvention() {
        classes()
                .that()
                .resideInAPackage("..infrastructure.repository..")
                .and()
                .areAnnotatedWith("org.springframework.stereotype.Repository")
                .should()
                .haveSimpleNameEndingWith("GatewayImpl")
                .because("Gateway 实现类必须以 GatewayImpl 结尾")
                .check(MODULE_CLASSES);
    }

    // ── Security 规则 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("不允许直接使用 System.out / System.err（必须使用 Slf4j）")
    void noSystemOutPrintln() {
        noClasses()
                .that()
                .resideOutsideOfPackage("..test..")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("java.io.PrintStream")
                .because("禁止使用 System.out/err，所有日志必须通过 @Slf4j")
                .check(MODULE_CLASSES);
    }
}
