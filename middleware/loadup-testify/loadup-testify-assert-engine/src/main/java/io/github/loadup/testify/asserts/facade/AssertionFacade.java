package io.github.loadup.testify.asserts.facade;

/*-
 * #%L
 * Testify Assert Engine
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

import com.fasterxml.jackson.databind.JsonNode;
import io.github.loadup.testify.asserts.engine.TestifyAssertEngine;
import io.github.loadup.testify.core.variable.VariableContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssertionFacade {

    private final Map<String, TestifyAssertEngine> engineMap;

    public AssertionFacade(List<TestifyAssertEngine> engines) {
        // 自动将所有引擎按 supportKey 存入 Map
        this.engineMap = engines.stream()
                .collect(Collectors.toMap(TestifyAssertEngine::supportKey, e -> e, (existing, replacement) -> {
                    // 如果发生冲突，抛出明确的错误信息
                    throw new IllegalStateException(String.format(
                            "Duplicate AssertEngine key: '%s' found in %s and %s",
                            existing.supportKey(),
                            existing.getClass().getSimpleName(),
                            replacement.getClass().getSimpleName()));
                }));
    }

    public List<String> executeAll(JsonNode expectRoot, Object actualRes, Throwable businessError) {
        List<String> reports = new ArrayList<>();
        // 获取当前的变量上下文
        Map<String, Object> context = VariableContext.get();
        // 遍历 YAML 中 expect 下的所有节点
        expectRoot.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode config = entry.getValue();
            TestifyAssertEngine engine = engineMap.get(key);

            if (engine != null) {
                // 根据 key 决定传入哪个实际对象
                Object actual =
                        switch (key) {
                            case "response" -> actualRes;
                            case "exception" -> businessError;
                            case "database" -> null; // DB 引擎通常内部自己去查库，不需要外传实际值
                            default -> null;
                        };

                try {
                    engine.compare(config, actual, context, reports);
                } catch (Exception e) {
                    log.error("Assert Error:", e);
                    reports.add("❌ 引擎 [" + key + "] 执行崩溃: " + e.getMessage());
                }
            }
        });

        return reports;
    }
}
