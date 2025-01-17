package com.github.loadup.commons.core;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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
 * @author Laysan
 * @since 1.0.0
 */
public interface StringPool {

    /**
     * 字符串常量：制表符 {@code "\t"}
     */
    String TAB = "	";

    /**
     * 字符串常量：点 {@code "."}
     */
    String DOT = ".";

    /**
     * 字符串常量：双点 {@code ".."} <br>
     * 用途：作为指向上级文件夹的路径，如：{@code "../path"}
     */
    String DOUBLE_DOT = "..";

    /**
     * 字符串常量：斜杠 {@code "/"}
     */
    String SLASH = "/";

    /**
     * 字符串常量：反斜杠 {@code "\\"}
     */
    String BACKSLASH = "\\";

    /**
     * 字符串常量：回车符 {@code "\r"} <br>
     * 解释：该字符常用于表示 Linux 系统和 MacOS 系统下的文本换行
     */
    String CR = "\r";

    /**
     * 字符串常量：换行符 {@code "\n"}
     */
    String LF = "\n";

    /**
     * 字符串常量：Windows 换行 {@code "\r\n"} <br>
     * 解释：该字符串常用于表示 Windows 系统下的文本换行
     */
    String CRLF = "\r\n";

    /**
     * 字符串常量：下划线 {@code "_"}
     */
    String UNDERLINE = "_";

    /**
     * 字符串常量：减号（连接符） {@code "-"}
     */
    String DASHED = "-";

    /**
     * 字符串常量：逗号 {@code ","}
     */
    String COMMA = ",";

    /**
     * 字符串常量：花括号（左） <code>"{"</code>
     */
    String DELIM_START = "{";

    /**
     * 字符串常量：花括号（右） <code>"}"</code>
     */
    String DELIM_END = "}";

    /**
     * 字符串常量：中括号（左） {@code "["}
     */
    String BRACKET_START = "[";

    /**
     * 字符串常量：中括号（右） {@code "]"}
     */
    String BRACKET_END = "]";

    /**
     * 字符串常量：冒号 {@code ":"}
     */
    String COLON = ":";

    /**
     * 字符串常量：艾特 {@code "@"}
     */
    String AT = "@";

    /**
     * 字符串常量：HTML 不间断空格转义 {@code "&nbsp;" -> " "}
     */
    String HTML_NBSP = "&nbsp;";
    ;

    /**
     * 字符串常量：HTML And 符转义 {@code "&amp;" -> "&"}
     */
    String HTML_AMP = "&amp;";

    /**
     * 字符串常量：HTML 双引号转义 {@code "&quot;" -> "\""}
     */
    String HTML_QUOTE = "&quot;";

    /**
     * 字符串常量：HTML 单引号转义 {@code "&apos" -> "'"}
     */
    String HTML_APOS = "&apos;";

    /**
     * 字符串常量：HTML 小于号转义 {@code "&lt;" -> "<"}
     */
    String HTML_LT = "&lt;";

    /**
     * 字符串常量：HTML 大于号转义 {@code "&gt;" -> ">"}
     */
    String HTML_GT = "&gt;";

    /**
     * 字符串常量：空 JSON {@code "{}"}
     */
    String EMPTY_JSON = "{}";
}
