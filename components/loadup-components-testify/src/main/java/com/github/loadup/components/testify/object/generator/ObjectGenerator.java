package com.github.loadup.components.testify.object.generator;

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

import java.lang.reflect.Type;

/**
 * 对象类型处理器接口
 *
 * 
 *
 */
public interface ObjectGenerator {

    /**
     * 判断是否是简单类型（同collectionType区分）
     *
     * @return
     */
    boolean isSimpleType();

    /**
     * 基于类对象指定Field，参考CSV提供数值，生成待比较对象
     *
     * @param clsLoader
     * @param clz
     * @param fieldName
     * @param referedCSVValue，csv文件数值用来辅助提取（可选）
     * @return
     */
    Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue);

    /**
     * 基于输入对象，生成CSV文件中待落地数据
     *
     * @param obj
     * @return
     */
    String generateObjectValue(Object obj, String csvPath, boolean isSimple);

    /**
     * 解析复杂对象具体类型
     *
     * @param collectionItemType
     * @param clz
     * @return
     */
    Class<?> getItemClass(Type collectionItemType, Class<?> clz);

    /**
     * 设定复杂对象序列值
     *
     * @param collectionObject
     * @param value
     * @param originalValue
     * @param index
     */
    void setObjectValue(Object collectionObject, Object value, String originalValue, int index);
}
