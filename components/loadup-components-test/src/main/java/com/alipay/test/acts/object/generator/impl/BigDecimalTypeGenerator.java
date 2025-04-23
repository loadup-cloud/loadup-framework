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

import java.lang.reflect.Type;
import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.object.generator.ObjectGenerator;

/**
 * BigDecimal类型处理器
 * @author xiaoleicxl
 * @version $Id: BigDecimalTypeGenerator.java, v 0.1 2015年12月9日 下午12:48:14 xiaoleicxl Exp $
 */
public class BigDecimalTypeGenerator implements ObjectGenerator {

    private static final Log LOG = LogFactory.getLog(BigDecimalTypeGenerator.class);

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue) {
        BigDecimal num = null;
        try {
            num = new BigDecimal(referedCSVValue);
        } catch (Exception e) {
            ActsLogUtil.fail(LOG, "money对象转化出错，返回null!");
        }
        return num;
    }

    @Override
    public String generateObjectValue(Object obj, String csvPath, boolean isSimple) {
        return String.valueOf(obj);
    }

    @Override
    public Class<?> getItemClass(Type collectionItemType, Class<?> clz) {
        //简单类型不实现
        return null;
    }

    @Override
    public void setObjectValue(Object collectionObject, Object value, String originalValue,
                               int index) {
        //简单类型不实现
    }
}
