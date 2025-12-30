package com.github.loadup.components.captcha.engine;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 标识符
 *
 * @author L.cm
 */
@Getter
@RequiredArgsConstructor
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

}
