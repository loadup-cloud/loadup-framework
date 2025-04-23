package com.alipay.test.acts.object.generator.impl;

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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import com.alipay.test.acts.helper.CSVHelper;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.object.generator.ObjectGenerator;
import com.alipay.test.acts.object.manager.ObjectTypeManager;
import com.google.common.reflect.TypeToken;

/**
 * Set类型处理器
 * @author xiaoleicxl
 * @version $Id: SetTypeGenerator.java, v 0.1 2015年12月9日 下午12:47:41 xiaoleicxl Exp $
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SetTypeGenerator implements ObjectGenerator {

    private static final Log LOG = LogFactory.getLog(SetTypeGenerator.class);

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
                    ActsLogUtil.fail(LOG, "复杂类型的Set转换为String出错！");
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
            //return Object.class;
            return Object.class;
        } else {
            return (Class<?>) typeArguments[0];
        }

        return null;
    }

    @Override
    public void setObjectValue(Object collectionObject, Object value, String originalValue,
                               int index) {
        if (collectionObject instanceof Set) {
            ((Set) collectionObject).add(value);
        } else {
            ActsLogUtil.fail(LOG,
                "给对象【" + collectionObject + "】失败，对象是【" + collectionObject.getClass() + "】类型而不是Set");
        }
    }
}
