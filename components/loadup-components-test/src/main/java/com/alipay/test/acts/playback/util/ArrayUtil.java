/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.util;

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

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author qingqin
 * @version $Id: ArrayUtils.java, v 0.1 2019年07月30日 下午10:16 qingqin Exp $
 */
public class ArrayUtil {

    public static boolean isStringArrayEqual(String[] array1, String[] array2) {
        if (array1 == null && array2 == null) {
            return true;
        } else if (array1 == null) {
            return false;
        } else if (array2 == null) {
            return false;
        } else {
            if (array1.length != array2.length) {
                return false;
            } else {
                for (int i = 0; i < array1.length; i++) {
                    if (!StringUtils.equals(StringUtils.trim(array1[i]), StringUtils.trim(array2[i]))) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public static String join(String[] array, String split) {
        if (isEmpty(array)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(32);
        for (String each : array) {
            sb.append(each).append(split);
        }
        return sb.substring(0, sb.lastIndexOf(split));
    }

    public static String[] list2Array(List<String> list) {
        if (isEmpty(list)) {
            return new String[0];
        }
        return list.toArray(new String[0]);
    }

    public static Object[] list2Array(List<Object> list, Object[] array) {
        if (isEmpty(list)) {
            return array;
        }
        return list.toArray(array);
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Collection<?> array) {
        return array == null || array.isEmpty();
    }

}
