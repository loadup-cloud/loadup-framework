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

import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.generator.ObjectGenerator;
import com.github.loadup.components.testify.object.manager.ObjectTypeManager;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Set类型处理器
 */
@Slf4j
public class SetTypeGenerator implements ObjectGenerator {

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue) {
        return new HashSet();
    }

    @Override
    public String generateObjectValue(Object obj, String csvPath, boolean isSimple) {
        String collectionString = "";

        if (((Set) obj).size() == 0) {
            collectionString = "@element_empty@";
            return collectionString;
        }

        if (isSimple) {
            for (Object o : (Set) obj) {
                collectionString = collectionString + String.valueOf((o)) + ";";
            }
        } else {
            collectionString = csvPath + "@";
            for (Object o : (Set) obj) {

                try {
                    int index = CSVHelper.insertObjDataAndReturnIndex(o, csvPath);
                    collectionString = collectionString + String.valueOf(index) + ";";
                } catch (Exception e) {
                    TestifyLogUtil.fail(log, "复杂类型的Set转换为String出错！");
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

        //        Assert.assertNotNull("解析Set的具体类型失败。", genericType);

        Type[] typeArguments = genericType.getActualTypeArguments();

        //        Assert.assertEquals("解析Set的具体类型失败。泛化的类型必须只有一个，实际值【" + typeArguments.length + "】个",
        //            typeArguments.length, 1);

        if (typeArguments[0] instanceof ParameterizedType) {
            ObjectTypeManager subMng = new ObjectTypeManager();
            TypeToken<?> genericTypeToken = TypeToken.of(typeArguments[0]);
            Class innCls = genericTypeToken.getRawType();
            if (subMng.isSimpleType(innCls)) {
                return genericTypeToken.getRawType();
            } else if (subMng.isCollectionType(innCls)) {
                return innCls;
            }
        } else if (typeArguments[0] instanceof TypeVariable) {
            //  return Object.class; 这里暂时返回空
            return Object.class;
        } else if (typeArguments[0] instanceof WildcardType) {
            // return Object.class;
            return Object.class;
        } else {
            return (Class<?>) typeArguments[0];
        }

        return null;
    }

    @Override
    public void setObjectValue(Object collectionObject, Object value, String originalValue, int index) {
        if (collectionObject instanceof Set) {
            ((Set) collectionObject).add(value);
        } else {
            TestifyLogUtil.fail(
                    log, "给对象【" + collectionObject + "】失败，对象是【" + collectionObject.getClass() + "】类型而不是Set");
        }
    }
}
