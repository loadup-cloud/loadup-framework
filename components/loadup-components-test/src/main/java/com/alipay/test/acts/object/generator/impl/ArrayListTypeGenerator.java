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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.helper.CSVHelper;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.object.generator.ObjectGenerator;
import com.alipay.test.acts.object.manager.ObjectTypeManager;
import com.google.common.reflect.TypeToken;

/**
 * 列表类型处理器
 * @author zhiyuan.lzy
 * @version $Id: ListTypeHandler.java, v 0.1 2014年12月16日 下午2:52:19 zhiyuan.lzy Exp $
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ArrayListTypeGenerator implements ObjectGenerator {

    private static final Log LOG = LogFactory.getLog(ArrayListTypeGenerator.class);

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue) {
        return new ArrayList();
    }

    @Override
    public String generateObjectValue(Object obj, String csvPath, boolean isSimple) {
        String collectionString = "";

        if (((List) obj).size() == 0) {
            collectionString = "@element_empty@";
            return collectionString;
        }

        if (isSimple) {
            for (int i = 0; i < ((List) obj).size(); i++) {
                collectionString = collectionString + String.valueOf(((List) obj).get(i)) + ";";
            }
        } else {

            String reCsvPath = StringUtils.substringAfterLast(csvPath, "/");
            String tempCollectionString = reCsvPath + "@";
            for (int i = 0; i < ((List) obj).size(); i++) {
                try {
                    int index = CSVHelper.insertObjDataAndReturnIndex(((List) obj).get(i), csvPath);
                    collectionString = collectionString + tempCollectionString
                                       + String.valueOf(index) + ";";
                } catch (Exception e) {
                    ActsLogUtil.fail(LOG, "复杂类型的List转换为String出错！");
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

        if (null == genericType) {
            throw new ActsException("解析List的具体类型失败");
        }

        Type[] typeArguments = genericType.getActualTypeArguments();
        if (typeArguments.length > 1) {
            throw new ActsException("解析List的具体类型失败。泛化的类型必须只有一个，实际值【" + typeArguments.length + "】个");
        }

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
            return Object.class;
        } else if (typeArguments[0] instanceof WildcardType) {
            return Object.class;
        } else {
            return (Class<?>) typeArguments[0];
        }
        return null;
    }

    @Override
    public void setObjectValue(Object collectionObject, Object value, String originalValue,
                               int index) {
        if (collectionObject instanceof List) {
            ((List) collectionObject).add(value);
        } else {
            ActsLogUtil
                .fail(LOG, "给对象【" + collectionObject + "】失败，对象是【" + collectionObject.getClass()
                           + "】类型而不是List");
        }
    }
}
