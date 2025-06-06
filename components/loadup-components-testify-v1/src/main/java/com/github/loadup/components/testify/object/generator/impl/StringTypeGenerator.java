/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.object.generator.impl;

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

import com.github.loadup.components.testify.constant.TestifyYamlConstants;
import com.github.loadup.components.testify.object.generator.ObjectGenerator;
import java.io.File;
import java.lang.reflect.Type;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

/**
 * 字符串类型处理器
 *
 *
 *
 */
public class StringTypeGenerator implements ObjectGenerator {

    private static final String lineSeparator = File.separator;

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue) {
        Assert.assertEquals(clz, String.class);
        if (StringUtils.equals("''", referedCSVValue)) {
            return "";
        }
        if (referedCSVValue.contains(TestifyYamlConstants.LINESEPARATOR)) {
            referedCSVValue = referedCSVValue.replace(TestifyYamlConstants.LINESEPARATOR, lineSeparator);
        }

        return referedCSVValue.trim();
    }

    @Override
    public String generateObjectValue(Object obj, String csvPath, boolean isSimple) {
        if (StringUtils.isEmpty(((String) obj))) {
            return "''";
        }

        return String.valueOf(obj);
    }

    @Override
    public Class<?> getItemClass(Type collectionItemType, Class<?> clz) {
        // 简单类型不实现
        return null;
    }

    @Override
    public void setObjectValue(Object collectionObject, Object value, String originalValue, int index) {
        // 简单类型不实现
    }
}
