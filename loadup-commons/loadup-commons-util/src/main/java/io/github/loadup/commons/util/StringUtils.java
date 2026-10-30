/*
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util;

import io.github.loadup.commons.json.ToStringAsJson;
import java.text.MessageFormat;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Lise
 * @since 1.0.0
 */
public class StringUtils {

    /**
     * The empty String {@code ""}.
     */
    public static final String EMPTY = "";

    private static final char BACKSLASH = '\\';

    private StringUtils() {}

    /**
     * Returns true if the given CharSequence is null, empty, or consists only of whitespace.
     */
    public static boolean isBlank(CharSequence cs) {
        return cs == null || cs.toString().isBlank();
    }

    /**
     * Returns true if the given CharSequence is non-null, non-empty, and not all whitespace.
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Returns true if the given CharSequence is null or has length zero.
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Returns true if the given CharSequence is non-null and has a length greater than zero.
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static String format(String strPattern, Object... argArray) {
        return formatWith(strPattern, "{}", argArray);
    }

    /**
     *
     * @since 5.7.14
     */
    private static String formatWith(String strPattern, String placeHolder, Object... argArray) {
        if (isBlank(strPattern) || isBlank(placeHolder) || ArrayUtils.isEmpty(argArray)) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();
        final int placeHolderLength = placeHolder.length();

        final StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;
        int delimIndex;
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(placeHolder, handledPosition);
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    return strPattern;
                }

                sbuf.append(strPattern, handledPosition, strPatternLength);
                return sbuf.toString();
            }

            if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == BACKSLASH) {
                if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == BACKSLASH) {

                    sbuf.append(strPattern, handledPosition, delimIndex - 1);
                    sbuf.append(argArray[argIndex]);
                    handledPosition = delimIndex + placeHolderLength;
                } else {

                    argIndex--;
                    sbuf.append(strPattern, handledPosition, delimIndex - 1);
                    sbuf.append(placeHolder.charAt(0));
                    handledPosition = delimIndex + 1;
                }
            } else {
                sbuf.append(strPattern, handledPosition, delimIndex);
                sbuf.append(argArray[argIndex]);
                handledPosition = delimIndex + placeHolderLength;
            }
        }

        sbuf.append(strPattern, handledPosition, strPatternLength);

        return sbuf.toString();
    }

    /**
     * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
     *
     * @since 5.7.10
     */
    public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template.toString();
        }

        String template2 = template.toString();
        String value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object rawValue = entry.getValue();
            if (rawValue == null && ignoreNull) {
                continue;
            }
            value = String.valueOf(rawValue);
            template2 = template2.replace("{" + entry.getKey() + "}", value);
        }
        return template2;
    }

    /**
     * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
     */
    public static String format(CharSequence template, Map<?, ?> map) {
        return format(template, map, true);
    }

    public static String formatWithIndex(CharSequence pattern, Object... arguments) {
        return MessageFormat.format(pattern.toString(), arguments);
    }

    /**
     * to string ,default json style
     */
    public static String toString(Object obj) {
        return ToStringAsJson.reflectionToString(obj);
    }
}
