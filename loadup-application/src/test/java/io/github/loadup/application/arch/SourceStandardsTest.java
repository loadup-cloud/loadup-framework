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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Checks source-retained annotations that bytecode architecture rules cannot observe. */
class SourceStandardsTest {
    private Path root() {
        Path path = Path.of("").toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("AGENTS.md"))) path = path.getParent();
        return Objects.requireNonNull(path, "Repository root is required");
    }

    @Test
    void mapstructUsesSharedSpringConfigurationAndNoLombokIsImported() throws Exception {
        List<String> failures = new ArrayList<>();
        try (var files = Files.walk(root())) {
            for (Path file : files.filter(p -> p.toString().contains("/src/main/java/"))
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList()) {
                String source = Files.readString(file);
                if (Pattern.compile("(?m)^import\\s+lombok\\.").matcher(source).find())
                    failures.add(file + ": Lombok import");
                if (source.contains("import org.mapstruct.Mapper;")
                        && !Pattern.compile(
                                        "@Mapper\\([^)]*config\\s*=\\s*LoadUpMapStructConfig\\.class", Pattern.DOTALL)
                                .matcher(source)
                                .find()) failures.add(file + ": missing shared MapStruct config");
                if (source.contains("Mappers.getMapper(") || source.contains("import org.mapstruct.factory.Mappers;"))
                    failures.add(file + ": unmanaged converter");
            }
        }
        assertTrue(failures.isEmpty(), failures::toString);
    }

    @Test
    void businessInputsUseCommandAndQueryNames() throws Exception {
        List<String> failures = new ArrayList<>();
        try (var files = Files.walk(root().resolve("loadup-modules"))) {
            for (Path file : files.filter(p -> p.toString().contains("/src/main/java/"))
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList()) {
                String source = Files.readString(file);
                String name = file.getFileName().toString();
                if (source.contains("public enum ")) continue;
                if (file.toString().contains("/client/command/") && !name.endsWith("Command.java"))
                    failures.add(file + ": command name");
                if (file.toString().contains("/client/query/") && !name.endsWith("Query.java"))
                    failures.add(file + ": query name");
            }
        }
        assertTrue(failures.isEmpty(), failures::toString);
    }

    @Test
    void localModulesResolveTheirCurrentRootParent() throws Exception {
        List<String> failures = new ArrayList<>();
        try (var files = Files.walk(root())) {
            for (Path pom : files.filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .filter(p ->
                            !p.toString().contains("/target/") && !p.toString().contains("/node_modules/"))
                    .toList()) {
                var document = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(pom.toFile());
                var parents = document.getElementsByTagName("parent");
                if (parents.getLength() == 0) continue;
                var parent = (org.w3c.dom.Element) parents.item(0);
                if (!parent.getElementsByTagName("artifactId")
                        .item(0)
                        .getTextContent()
                        .equals("loadup-parent")) continue;
                var relative = parent.getElementsByTagName("relativePath");
                if (relative.getLength() == 0
                        || !pom.getParent()
                                .resolve(relative.item(0).getTextContent())
                                .normalize()
                                .equals(root().resolve("pom.xml"))) failures.add(pom + ": root parent path");
            }
        }
        assertTrue(failures.isEmpty(), failures::toString);
    }

    @Test
    void containerTestsUseFailsafeNamesEvenOutsideApplicationClasspath() throws Exception {
        List<String> failures = new ArrayList<>();
        try (var files = Files.walk(root())) {
            for (Path file : files.filter(p -> p.toString().contains("/src/test/java/"))
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList()) {
                if (Pattern.compile("(?m)^[ \t]*@EnableTestContainers[(]")
                                .matcher(Files.readString(file))
                                .find()
                        && !file.getFileName().toString().endsWith("IT.java")) failures.add(file.toString());
            }
        }
        assertTrue(failures.isEmpty(), failures::toString);
    }
}
