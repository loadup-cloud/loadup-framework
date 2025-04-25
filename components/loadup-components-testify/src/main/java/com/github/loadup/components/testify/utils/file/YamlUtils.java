/**
 
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.github.loadup.components.testify.utils.file;

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
import org.yaml.snakeyaml.Yaml;

/**
 * 
 * 
 */
public class YamlUtils {
    /**
     * 解析yaml字符串转成对象
     *
     * @param yaml
     * @return
     */
    public static <T> T parse(String yaml, Class<T> type) {
        if (StringUtils.isBlank(yaml)) {
            return null;
        }
        Yaml yamlObject = new Yaml();
        return yamlObject.loadAs(yaml, type);
    }

    /**
     * 把java对象转换成yaml字符串
     *
     * @param data
     * @return
     */
    public static String dump(Object data) {
        Yaml yamlObject = new Yaml();
        return yamlObject.dump(data);
    }

}
