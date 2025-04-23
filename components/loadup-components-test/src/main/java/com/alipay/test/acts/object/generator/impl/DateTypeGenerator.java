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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.object.generator.ObjectGenerator;
import com.alipay.test.acts.util.DateUtil;

public class DateTypeGenerator implements ObjectGenerator {

    private static String    dateFormat = "yyyy-MM-dd hh:mm:ss";
    private static String    today      = "today";

    private static final Log LOG        = LogFactory.getLog(DateTypeGenerator.class);

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public Object generateFieldObject(Class<?> clz, String fieldName, String referedCSVValue) {
//        Assert.assertEquals("传入属性类必须为Date", clz, Date.class);
        Date retDate = null;
        if (!StringUtils.isBlank(referedCSVValue) && !referedCSVValue.equals("null")) {
            if (referedCSVValue.equalsIgnoreCase("today")) {
                retDate = new Date();
            } else if (referedCSVValue.startsWith("today")) {
                String diffString;
                if (referedCSVValue.contains("+")) {
                    diffString = referedCSVValue.substring(referedCSVValue.lastIndexOf("+") + 1)
                        .trim();
                } else {
                    diffString = referedCSVValue
                        .substring(referedCSVValue.lastIndexOf("today") + 5).trim();
                }
                int diff = 0;
                try {
                    diff = Integer.valueOf(diffString);
                } catch (NumberFormatException e) {
                    ActsLogUtil.fail(LOG, "时间格式后缀解析错误" + referedCSVValue);
                }
                retDate = DateUtil.addDays(new Date(), diff);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                try {
                    retDate = sdf.parse(referedCSVValue.trim());
                } catch (Exception e) {
                    ActsLogUtil.fail(LOG, "解析GMT字段【" + fieldName + "】失败。日期值【" + referedCSVValue
                                          + "】", e);
                }
            }
        }
        return retDate;
    }

    @Override
    public String generateObjectValue(Object obj, String csvPath, boolean isSimple) {
        String str = "null";
        if (obj != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            try {
                str = sdf.format(obj);
            } catch (Exception e) {
                ActsLogUtil.error(LOG, "解析日期字段失败。日期值【" + obj + "】", e);
            }
        }

        if (StringUtils.equalsIgnoreCase(str, DateUtil.formatDateString(new Date(), dateFormat))) {
            return today;
        } else {
            return str;
        }
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
