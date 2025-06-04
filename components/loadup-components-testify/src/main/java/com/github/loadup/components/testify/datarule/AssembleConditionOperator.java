/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.datarule;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

/**
 * 条件操作符。
 *
 *
 *
 */
public enum AssembleConditionOperator {
    EQUAL("=="),

    NOT_EQUAL("!="),

    GREATER(">"),

    GREATER_EQUAL(">="),

    LESS("<"),

    LESS_EQUAL("<="),

    AND("&&"),

    OR("||"),

    LEFT_PAREN("("),

    RIGHT_PAREN(")");

    private final String symbol;

    /**
     * @param symbol
     */
    private AssembleConditionOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 根据符号获取操作。
     *
     * @param symbol
     * @return
     */
    public static AssembleConditionOperator getBySymbol(String symbol) {
        for (AssembleConditionOperator operator : values()) {
            if (operator.symbol.equals(symbol)) {
                return operator;
            }
        }

        return null;
    }
}
