package io.github.loadup.testify.asserts.engine;

import java.util.Map;
import tools.jackson.databind.JsonNode;

public interface TestifyAssertEngine {
    /**
     * 执行断言逻辑
     *
     * @param expectNode 对应的 YAML 子节点内容
     * @param actual     实际值（由编排器传入）
     * @param context    testcontext
     */
    void compare(JsonNode expectNode, Object actual, Map<String, Object> context);
}
