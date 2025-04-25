package com.github.loadup.components.testify.utils.compare;

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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * 比较两个对象的工具类
 *
 * @author rong.zhang
 */
public class CompareTwoObject {

    private static final ThreadLocal<Boolean> FORWARD = new ThreadLocal<Boolean>();

    /**
     * 主方法，用于比较两个对象的是否一致，针对不一致的地方输出具体哪个属性不一致。
     * 针对list或array类型， .0或.1表示具体第几个元素不一致
     * 针对map类型， .key表示具体某个key不一样
     * 其他类型则是.表示某个类中的某个属性
     * <p>
     * 注意：由于是通过json来进行比较，采用javabean的方法进行转化，因此会针对所有的get方法进行验证。
     *
     * @param src
     * @param target
     * @return
     */
    public static CompareResult compare(Object src, Object target, Map<String, String> flag) {

        String srcJson = JSON.toJSONString(src);

        String targetJson = JSON.toJSONString(target
        );

        if (srcJson.equals(targetJson)) {
            return new CompareResult(true);
        }

        JSONObject srcObj = JSONObject.parseObject(srcJson);
        JSONObject targetObj = JSONObject.parseObject(targetJson);

        CompareResult cr = new CompareResult(false);

        FORWARD.set(true);
        compareJson(null, null, srcObj, targetObj, cr);

        FORWARD.set(false);
        compareJson(null, null, targetObj, srcObj, cr);

        FORWARD.set(null);

        System.out.println("srcJson=" + srcJson);
        System.out.println("targetJson=" + targetJson);
        System.out.println("filter key=" + flag.toString());

        return filterCompareResult(cr, flag);
    }

    private static void compareJson(String parent, String key, Object srcObj, Object targetObj,
                                    CompareResult cr) {
        if (srcObj instanceof JSONObject) {
            compareJsonObj(parent, (JSONObject) srcObj, (JSONObject) targetObj, cr);
        } else if (srcObj instanceof JSONArray) {
            compareJsonArray(parent, (JSONArray) srcObj, (JSONArray) targetObj, cr);
        } else {
            compareJsonString(parent, srcObj, targetObj, cr);
        }
    }

    private static void compareJsonObj(String parent, JSONObject srcObj, JSONObject targetObj,
                                       CompareResult cr) {

        if (targetObj == null) {
            compareJsonString(parent, srcObj.toString(), null, cr);
            return;
        }
        Set<String> keys = srcObj.keySet();
        for (String key1 : keys) {
            compareJson(genKeyPath(parent, key1), key1, srcObj.get(key1), targetObj == null ? null
                    : targetObj.get(key1), cr);
        }
    }

    private static void compareJsonString(String parent, Object srcObj, Object targetObj,
                                          CompareResult cr) {
        if (!srcObj.equals(targetObj)) {
            if (FORWARD.get() == null || FORWARD.get()) {
                cr.addCompareDetail(parent, srcObj, targetObj);
            } else {
                cr.addCompareDetail(parent, targetObj, srcObj);
            }
        }
    }

    private static void compareJsonArray(String parent, JSONArray json1, JSONArray json2,
                                         CompareResult cr) {
        Iterator<Object> i1 = json1.iterator();
        Iterator<Object> i2 = null;
        if (json2 != null) {
            i2 = json2.iterator();
        } else {
            compareJsonString(parent, json1.toString(), null, cr);
            return;
        }
        int index = 0;
        while (i1.hasNext()) {
            Object key1 = i1.next();
            Object key2;
            String thisParent = genKeyPath(parent, "" + index++);
            if (i2 != null && i2.hasNext() && ((key2 = i2.next()) != null)) {
                compareJson(thisParent, "" + index, key1, key2, cr);
            } else {
                compareJsonString(thisParent, key1.toString(), null, cr);
            }
        }
    }

    private static String genKeyPath(String parent, String key) {
        if (StringUtils.isBlank(parent)) {
            return key;
        }
        return parent + "." + key;

    }

    private static CompareResult filterCompareResult(CompareResult cr, Map<String, String> flag) {
        if (flag.isEmpty()) {
            return cr;
        }
        Set<CompareDetail> cds = cr.getCompareDetails();

        Iterator<CompareDetail> it = cds.iterator();
        while (it.hasNext()) {
            CompareDetail cd = it.next();
            if (flag.get(cd.getKey()) != null && flag.get(cd.getKey()).equals("N")) {
                it.remove();
            }
        }

        if (cds.isEmpty()) {
            CompareResult crfilter = new CompareResult(true);
            return crfilter;
        }

        cr.setCompareDetails(cds);
        return cr;
    }
}
