package io.github.loadup.testify.starter.loader;

/*-
 * #%L
 * Testify Spring Boot Starter
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.loadup.testify.core.model.TestContext;
import java.io.InputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/** YamlLoader 仅负责将 YAML 物理文件读取为 TestContext 对象。 不再持有 VariableEngine，不再进行变量解析。 */
@Slf4j
public class YamlLoader {

    private static final ObjectMapper yamlMapper;

    static {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.registerModule(new JavaTimeModule());
        // 允许 YAML 中存在 TestContext 未定义的字段
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SneakyThrows
    public TestContext load(String path) {
        log.info("Loading raw YAML test case from classpath: {}", path);

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Test case file not found in classpath: " + path);
            }

            // 直接将 YAML 文件反序列化为 TestContext Record
            // 此时里面的 JsonNode 仍然保留着原始的占位符文本，如 "${faker.name()}"
            TestContext context = yamlMapper.readValue(is, TestContext.class);
            // 如果需要从路径推断测试名称，可以在这里进行简单的元数据补充
            String testName = path.substring(path.lastIndexOf("/") + 1)
                    .replace(".yaml", "")
                    .replace(".yml", "");
            context.setTestName(testName);
            context.setYamlPath(path);

            // 返回最原始的 Context 对象，变量解析交给后续的 runTest 环节
            return context;
        }
    }
}
