/*-
 * #%L
 * Testify Spring Boot Starter
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
package io.github.loadup.testify.starter.cases;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

/**
 * Loads batch test cases from YAML or JSON files. Three layouts are supported:
 *
 * <ul>
 *   <li>{@code cases: [...]} — recommended for a batch of cases in a single file
 *   <li>{@code [...]} — root-level array of case objects
 *   <li>single case object — a file containing exactly one case
 * </ul>
 *
 * <p>Each case object may define {@code name}, {@code variables}, {@code input} and {@code expect}.
 */
public final class CaseFiles {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private CaseFiles() {}

    public static List<TestifyCase> loadYaml(String classpathPath) {
        try (InputStream in = new ClassPathResource(classpathPath).getInputStream()) {
            return parse(YAML_MAPPER.readTree(in));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load testify cases from " + classpathPath, e);
        }
    }

    public static List<TestifyCase> loadJson(String classpathPath) {
        try (InputStream in = new ClassPathResource(classpathPath).getInputStream()) {
            return parse(JSON_MAPPER.readTree(in));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load testify cases from " + classpathPath, e);
        }
    }

    private static List<TestifyCase> parse(JsonNode root) {
        List<TestifyCase> cases = new ArrayList<>();
        if (root == null || root.isNull()) {
            return cases;
        }
        if (root.isArray()) {
            root.forEach(node -> cases.add(toCase(node)));
        } else if (root.has("cases") && root.get("cases").isArray()) {
            root.get("cases").forEach(node -> cases.add(toCase(node)));
        } else {
            cases.add(toCase(root));
        }
        return cases;
    }

    private static TestifyCase toCase(JsonNode node) {
        String name = node.path("name").asText();
        if (name.isBlank()) {
            name = node.path("id").asText();
        }
        if (name.isBlank()) {
            name = "case";
        }
        Map<String, Object> variables = node.has("variables")
                ? YAML_MAPPER.convertValue(node.get("variables"), new TypeReference<LinkedHashMap<String, Object>>() {})
                : Map.of();
        JsonNode input = node.has("input") ? node.get("input") : node.get("params");
        JsonNode expect = node.has("expect") ? node.get("expect") : node.get("expected");
        return new TestifyCase(name, variables, input, expect);
    }
}
