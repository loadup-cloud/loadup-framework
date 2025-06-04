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

import com.github.loadup.components.testify.exception.GenericsException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.generator.ObjectGenerator;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Map类型处理器
 */
@Slf4j
public class MapTypeGenerator implements ObjectGenerator {

    private static final String SEPERATOR = ":";

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue) {
        return new HashMap();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String generateObjectValue(Object obj, String csvPath, boolean isSimple) {
        String collectionString = "";

        if (((Map) obj).size() == 0) {
            collectionString = "@element_empty@";
            return collectionString;
        }

        if (isSimple) {
            for (Object o : ((Map) obj).keySet()) {
                collectionString =
                        collectionString + String.valueOf(o) + ":" + String.valueOf(((Map) obj).get(o)) + ";";
            }
        } else {
            String reCsvPath = StringUtils.substringAfterLast(csvPath, "/");
            String tempCollectionString = reCsvPath + "@";
            for (Object o : ((Map) obj).keySet()) {
                try {
                    int index = CSVHelper.insertObjDataAndReturnIndex(((Map) obj).get(o), csvPath);
                    collectionString = collectionString + String.valueOf(o) + ":" + tempCollectionString
                            + String.valueOf(index) + ";";
                } catch (Exception e) {
                    TestifyLogUtil.fail(log, "复杂类型的Map转换为String出错！", e);
                }
            }
        }
        collectionString = collectionString.substring(0, collectionString.length() - 1);
        return collectionString;
    }

    @Override
    public Class<?> getItemClass(Type collectionItemType, Class<?> clz) {

        ParameterizedType genericType = null;
        if (collectionItemType instanceof ParameterizedType) {
            genericType = (ParameterizedType) collectionItemType;
        }

        if (genericType == null) {
            TestifyLogUtil.fail(log, "解析Map的具体类型失败。");
        }

        Type[] typeArguments = genericType.getActualTypeArguments();
        if (typeArguments.length != 2) {
            TestifyLogUtil.fail(log, "解析Map的具体类型失败。泛化的类型必须只有2个，实际值【" + typeArguments.length + "】个");
        }
        try {
            return (Class<?>) typeArguments[1];
        } catch (Exception e) {
            throw new GenericsException("Map中含有暂时不支持的T类型");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setObjectValue(Object collectionObject, Object value, String originalValue, int index) {
        String[] valueParts = originalValue.split(SEPERATOR, 2);
        if (valueParts.length == 0) {
            TestifyLogUtil.fail(log, "解析Map值失败，格式不合法");
        }

        if (collectionObject instanceof Map) {
            ((Map) collectionObject).put(valueParts[0], valueParts[1]);
        } else {
            TestifyLogUtil.fail(
                    log, "给对象【" + collectionObject + "】失败，对象是【" + collectionObject.getClass() + "】类型而不是Map");
        }
    }
}
