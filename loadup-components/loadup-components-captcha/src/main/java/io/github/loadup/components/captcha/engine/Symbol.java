package io.github.loadup.components.captcha.engine;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

/**
 * 标识符
 *
 * @author L.cm
 */
public enum Symbol {

    /**
     * 标识符
     */
    NUM("n", false),

    /**
     * 加法
     */
    ADD("+", false),

    /**
     * 减发
     */
    SUB("-", false),

    /**
     * 乘法
     */
    MUL("x", true),

    /**
     * 除法
     */
    DIV("÷", true);

    /**
     * 算数符号
     */
    private final String value;

    /**
     * 是否优先计算
     */
    private final boolean priority;

    public static Symbol of(String c) {
        Symbol[] values = Symbol.values();
        for (Symbol value : values) {
            if (value.value.equals(c)) {
                return value;
            }
        }
        throw new IllegalArgumentException("不支持的标识符，仅仅支持(+、-、×、÷)");
    }

    Symbol(String value, boolean priority) {
        this.value = value;
        this.priority = priority;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isPriority() {
        return this.priority;
    }
}
