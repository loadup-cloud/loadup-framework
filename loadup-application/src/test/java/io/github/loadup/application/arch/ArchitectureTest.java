/*
 * #%L
 * Loadup Launcher
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.application.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * <ul>
 *   <li>HTTP controllers belong to Web or authentication adapters</li>
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

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework..")
                .because("Follow the documented dependency direction")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void domainShouldNotDependOnMyBatisFlex() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("com.mybatisflex..")
                .because("Follow the documented dependency direction")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("Production code must not depend on Lombok")
    void productionShouldNotDependOnLombok() {
        noClasses().should().dependOnClassesThat().resideInAPackage("lombok..").check(ALL_CLASSES);
    }

    @Test
    void controllersUseFacadesInsteadOfApplicationServices() {
        noClasses()
                .that()
                .haveSimpleNameEndingWith("Controller")
                .and()
                .resideInAPackage("..modules..web..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..app.service..", "..infrastructure..", "..domain..")
                .check(MODULE_CLASSES);
    }

    @Test
    void publicFacadeNamesAreConsistent() {
        classes()
                .that()
                .resideInAPackage("..client.facade..")
                .should()
                .beInterfaces()
                .andShould()
                .haveSimpleNameEndingWith("Facade")
                .check(MODULE_CLASSES);
    }

    @Test
    void repositoriesUseTheDatabaseComponent() {
        noClasses()
                .that()
                .resideInAPackage("..modules..infrastructure.repository..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework.jdbc.core..")
                .check(MODULE_CLASSES);
    }

    // ── HTTP adapter ownership ──────────────────────────────────────────────

    @Test
    @DisplayName("Controllers belong to Web or authentication adapters")
    void controllersBelongToAdapters() {
        noClasses()
                .that()
                .resideOutsideOfPackages("..web..", "..authserver..", "io.github.loadup.components.webmvc..")
                .should()
                .beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .orShould()
                .beAnnotatedWith("org.springframework.stereotype.Controller")
                .because("business layers must not own HTTP adapters")
                .check(ALL_CLASSES);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void noFieldAutowired() {
        noFields()
                .should()
                .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .because("dependencies use explicit constructor injection")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
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
                .because("Follow the documented dependency direction")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void noModuleCircularDependencies() {
        slices().matching("io.github.loadup.modules.(*)..")
                .should()
                .beFreeOfCycles()
                .because("Follow the documented dependency direction")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void noCommonsCircularDependencies() {
        JavaClasses commons = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.loadup.commons");

        slices().assignedFrom(new SliceAssignment() {
                    public SliceIdentifier getIdentifierOf(JavaClass type) {
                        if (!type.getName().startsWith("io.github.loadup.commons.")) return SliceIdentifier.ignore();
                        String location =
                                type.getSource().orElseThrow().getUri().toString();
                        var matcher = java.util.regex.Pattern.compile("/(loadup-commons-[^/]+)/")
                                .matcher(location);
                        if (!matcher.find())
                            throw new IllegalStateException("Cannot identify commons artifact: " + location);
                        return SliceIdentifier.of(matcher.group(1));
                    }

                    public String getDescription() {
                        return "commons Maven artifacts";
                    }
                })
                .should()
                .beFreeOfCycles()
                .check(commons);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void serviceClassNamingConvention() {
        classes()
                .that()
                .resideInAPackage("..app.service..")
                .and()
                .areAnnotatedWith("org.springframework.stereotype.Service")
                .should()
                .haveSimpleNameEndingWith("Service")
                .because("Follow the documented dependency direction")
                .check(MODULE_CLASSES);
    }

    @Test
    @DisplayName("Enforce documented layer and naming conventions")
    void gatewayImplNamingConvention() {
        classes()
                .that()
                .resideInAPackage("..infrastructure.repository..")
                .and()
                .areAnnotatedWith("org.springframework.stereotype.Repository")
                .should()
                .haveSimpleNameEndingWith("GatewayImpl")
                .because("Follow the documented dependency direction")
                .check(MODULE_CLASSES);
    }

    // ── Test class naming conventions ─────────────────────────────────────────

    @Test
    @DisplayName("Test classes annotated with @EnableTestContainers must be named *IT")
    void integrationTestNamingConvention() {
        JavaClasses testClasses = new ClassFileImporter()
                .withImportOption(new ImportOption() {
                    @Override
                    public boolean includes(Location location) {
                        return location.contains("/test-classes/");
                    }
                })
                .importPackages("io.github.loadup");

        classes()
                .that()
                .areAnnotatedWith("io.github.loadup.components.testcontainers.annotation.EnableTestContainers")
                .should()
                .haveSimpleNameEndingWith("IT")
                .because("Test classes needing database containers must be named *IT.java so Failsafe runs them")
                .allowEmptyShould(true)
                .check(testClasses);
    }

    @Test
    @DisplayName("Do not print to System.out / System.err; use LogUtil")
    void noSystemOutPrintln() {
        noClasses()
                .that()
                .resideOutsideOfPackage("..test..")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("java.io.PrintStream")
                .because("Framework logs must use LogUtil")
                .check(ALL_CLASSES);
    }

    @Test
    @DisplayName("Only LogUtil may create SLF4J loggers")
    void loggerFactoryIsOwnedByLogUtil() {
        noClasses()
                .that()
                .doNotHaveFullyQualifiedName("io.github.loadup.commons.log.LogUtil")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("org.slf4j.LoggerFactory")
                .because("Framework logging must use the shared LogUtil facade")
                .check(ALL_CLASSES);
    }
}
