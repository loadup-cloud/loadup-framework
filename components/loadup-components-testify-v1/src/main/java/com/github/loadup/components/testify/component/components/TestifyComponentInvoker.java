/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.component.components;

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

import java.lang.reflect.Method;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class TestifyComponentInvoker {
    public Object componentObject;
    public Method targetMethod;

    public Object execute(String command) throws Exception {
        Object[] params = genParam(command);
        return targetMethod.invoke(componentObject, params);
    }

    /**
     * 生成入参
     *
     * @param command
     * @return
     */
    public Object[] genParam(String command) {
        Object[] params = null;
        if (StringUtils.contains(command, "?")) {
            params = new Object[targetMethod.getParameterTypes().length];
            String query = StringUtils.substringAfter(command, "?");
            if (StringUtils.isNotBlank(query)) {
                String[] pairs = StringUtils.split(query, "&");

                int index = 0;
                for (String pair : pairs) {
                    if (StringUtils.isBlank(pair)) {
                        continue;
                    }
                    Object value = StringUtils.substringAfter(pair, "=");
                    params[index] = value;
                    index++;
                }
            }
        } else {
            // 不包含?,认为就是没有入参的方法
        }
        return params;
    }
}
