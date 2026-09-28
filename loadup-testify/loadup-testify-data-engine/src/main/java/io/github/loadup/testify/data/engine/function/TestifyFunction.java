package io.github.loadup.testify.data.engine.function;

/**
 * 自定义函数接口
 */
public interface TestifyFunction {
    /**
     * 绑定的前缀名称，如 "time" 对应 ${time.now()}
     */
    String getPrefix();
}
