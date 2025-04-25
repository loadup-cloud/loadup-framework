package com.github.loadup.components.testify.util;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 提供集合之间相互转换的功能
 *
 * 
 *
 */
public class CollectionConvertUtil {

    /**
     * HashSet-->String 转换器
     *
     * @param hashSet
     * @return
     */
    public static String hashSetConvertToString(HashSet<String> hashSet) {

        String resultString = "";
        if (hashSet.isEmpty()) {
            return null;
        }
        for (String s : hashSet) {
            resultString = resultString + s + ",";
        }
        return resultString;

    }

    /**
     * 数组-->List 转换器
     *
     * @param array
     * @return
     */
    public static List<Object> arrayConvertToList(Object[] array) {

        return Arrays.asList(array);

    }
}
