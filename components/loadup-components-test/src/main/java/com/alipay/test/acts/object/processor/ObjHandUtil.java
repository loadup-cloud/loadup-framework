/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.object.processor;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.helper.CSVHelper;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.model.VirtualList;
import com.alipay.test.acts.model.VirtualMap;
import com.alipay.test.acts.util.CSVApisUtil;
import com.alipay.test.acts.util.ReflectUtil;

/**
 *
 * @author 马洪良
 * @version $Id: ObjHandUtil.java, v 0.1 2015年11月29日 下午11:58:50 马洪良 Exp $
 */
public final class ObjHandUtil {

    private static final Log logger = LogFactory.getLog(ObjHandUtil.class);

    /**
    *
    * @param convertCsv
    * @param clsName
    * @return
    */
    public static boolean isSubListConvert(String convertCsv, String clsName) {
        if (StringUtils.isBlank(convertCsv) || !StringUtils.contains(convertCsv, ".csv")
            || StringUtils.isBlank(clsName)) {
            return false;
        }
        List<?> tableList = CSVHelper.readFromCsv(convertCsv);
        String className = StringUtils.trim(((String[]) tableList.get(1))[0]);
        if (StringUtils.equals(className, clsName)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param objValue
     * @return
     */
    public static Collection<Object> handListConvert(Object objValue, String csvPath) {
        try {
            VirtualList obj = (VirtualList) objValue;
            List<?> tableList = CSVHelper.readFromCsv(csvPath);
            String typeName = StringUtils.trim(((String[]) tableList.get(1))[2]);

            Class<?> listRet = Class.forName(typeName);
            if (List.class.isAssignableFrom(listRet)) {
                List<Object> retList = new ArrayList<Object>();
                Class<?> typeCls = obj.getVirtualList().getClass();
                if (CSVApisUtil.isWrapClass(typeCls) && null == obj.getVirtualList()) {
                    retList.add(ReflectUtil.valueByCorrectType(null, typeCls, "1"));
                }
                if (CSVApisUtil.isWrapClass(typeCls) && typeCls.getName().equals("java.lang.String")
                    && StringUtils.isBlank((String) obj.getVirtualList())) {
                    retList.add("demo");
                } else {
                    retList.add(obj.getVirtualList());
                }
                return retList;
            } else {
                Set<Object> retSet = new HashSet<Object>();
                Class<?> typeCls = obj.getVirtualList().getClass();
                if (CSVApisUtil.isWrapClass(typeCls) && null == obj.getVirtualList()) {
                    retSet.add(ReflectUtil.valueByCorrectType(null, typeCls, "1"));
                }
                if (CSVApisUtil.isWrapClass(typeCls) && typeCls.getName().equals("java.lang.String")
                    && StringUtils.isBlank((String) obj.getVirtualList())) {
                    retSet.add("demo");
                } else {
                    retSet.add(obj.getVirtualList());
                }
                return retSet;
            }
        } catch (Exception e) {
            logger.error("返回集合对象出错");
        }

        return null;
    }

    /**
     *
     * @param objValue
     * @return
     */
    public static Map<Object, Object> handMapConvert(Object objValue) {
        VirtualMap obj = (VirtualMap) objValue;
        Map<Object, Object> retMap = new HashMap<Object, Object>();
        try {
            retMap.put(obj.getMapKey(), obj.getMapValue());
        } catch (Exception e) {
            ActsLogUtil.error(logger, "暂时只支持Map的key为字符串");
            return null;
        }
        return retMap;
    }

}
