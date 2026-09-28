package io.github.loadup.testify.asserts.operator;

import io.github.loadup.testify.asserts.model.MatchResult;
import java.util.Map;

public interface OperatorMatcher {
    /**
     * @param actual        数据库查询到的实际值
     * @param expectedValue YAML中配置的期望对象 (如 {op: "gt", val: 100})
     * @return 匹配结果描述，如果成功返回 null，失败返回错误原因
     */
    MatchResult match(Object actual, Object expectedValue, Map<String, Object> config);

    boolean support(String op);
}
