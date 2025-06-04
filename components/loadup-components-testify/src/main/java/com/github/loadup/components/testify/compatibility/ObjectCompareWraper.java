/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.compatibility;

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

import com.github.loadup.components.testify.utils.ObjectUtil;
import com.github.loadup.components.testify.utils.check.ObjectCompareUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 */
public class ObjectCompareWraper extends ObjectCompareUtil {
    private static StringBuilder reportStr = new StringBuilder();

    public static boolean compareMessageWithFlag(
            Object oldObject, Object newObject, Map<String, Map<String, String>> flags, Map<String, Object> paramMap) {

        varParaMapHolder.set(paramMap);
        varFlagMapHolder.set(flags);
        return compareByFields(oldObject, newObject);
    }

    public static boolean compareByFields(Object oldObject, Object newObject) {
        // 预期结果变量动态替换
        if (newObject instanceof String) {
            if (((String) newObject).startsWith("$")) {
                String key = ((String) newObject).replace("$", "");
                if (varParaMapHolder.get() != null && varParaMapHolder.get().get(key) != null) {
                    newObject = varParaMapHolder.get().get(key);
                }
            }
        }
        boolean isSame = true;

        if (oldObject == null) {
            return newObject == null;
        }
        if (newObject == null) {
            return oldObject == null;
        }
        String objName = oldObject.getClass().getName();
        Class<?> objType = oldObject.getClass();
        // 可直接比较类型，调用比较方法
        if (isComparable(objType)) {
            return compare(oldObject, newObject);
        } else if (objType.isArray()) {
            Object[] oldObjectArray = (Object[]) oldObject;
            Object[] newObjectArray = (Object[]) newObject;
            if (oldObjectArray.length != newObjectArray.length) {
                reportStr.append("\n" + "数组长度不匹配" + objName);
                return false;
            }
            for (int i = 0; i < oldObjectArray.length; i++) {
                if (!compareByFields(oldObjectArray[i], newObjectArray[i])) {
                    reportStr.append("\n" + "数组元素校验失败,index=" + i);
                    return false;
                }
            }
            return true;
        } else if (oldObject instanceof Map) {
            Map<Object, Object> oldObjectMap = (Map<Object, Object>) oldObject;
            Map<Object, Object> newObjectMap = (Map<Object, Object>) newObject;
            if (oldObjectMap.size() != newObjectMap.size()) {
                reportStr.append("\n" + "hashMap size不一致" + objName);
                return false;
            }
            for (Entry<Object, Object> entry : oldObjectMap.entrySet()) {
                Object oldObjectVal = entry.getValue();
                Object newObjectVal = newObjectMap.get(entry.getKey());
                if (!(compareByFields(oldObjectVal, newObjectVal))) {
                    reportStr.append("\n" + "hashmap元素校验失败，key=" + entry.getKey());
                    return false;
                }
            }
        } else if (oldObject instanceof List) {
            List<?> oldObjectList = (List<?>) oldObject;
            List<?> newObjectList = (List<?>) newObject;
            if (oldObjectList.size() != newObjectList.size()) {
                reportStr.append("\n" + "列表长度不一致" + objName);
                return false;
            }
            for (Object objTarget : oldObjectList) {
                boolean isFound = false;
                for (Object objExpect : newObjectList) {
                    if (compareByFields(objTarget, objExpect)) {
                        isFound = true;
                        newObjectList.remove(objExpect);
                        break;
                    }
                }
                if (!isFound) {
                    reportStr.append("\n" + "目标元素未在期望列表中找到" + ObjectUtil.toJson(objTarget));
                    return false;
                }
            }
        } else {
            Map<String, String> objFlag = null;
            if (!(varFlagMapHolder.get() == null || varFlagMapHolder.get().isEmpty())) {
                objFlag = varFlagMapHolder.get().get(objName);
            }
            List<Field> fields = new ArrayList<Field>();

            for (Class<?> c = objType; c != null; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !fields.contains(field)) {
                        fields.add(field);
                    }
                }
            }
            for (Field field : fields) {
                String fieldName = field.getName();
                field.getType().toString();
                try {
                    field.setAccessible(true);
                    Object oldField = field.get(oldObject);
                    Object newField = field.get(newObject);
                    if (objFlag != null && objFlag.get(fieldName) != null) {
                        if (objFlag.get(fieldName).equals("N")) {
                            continue;
                        } else if (objFlag.get(fieldName).equals("R")) {
                            Pattern pattern = Pattern.compile((String) newField);
                            Matcher matcher = pattern.matcher((String) oldField);
                            isSame = matcher.matches();
                            continue;
                        }
                    }
                    if (!compareByFields(oldField, newField)) {
                        reportStr.append("\n"
                                + fieldName
                                + "消息成员校验失败,老的值"
                                + ObjectUtil.toJson(oldField)
                                + "新的值"
                                + ObjectUtil.toJson(newField));

                        isSame = false;
                    }

                } catch (IllegalArgumentException e) {
                    return false;
                } catch (IllegalAccessException e) {
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return isSame;
    }

    public static boolean compare(Object oldObject, Object newObject) {
        if (oldObject.getClass().isPrimitive()) {
            if (!(oldObject == newObject)) {
                // LOGGER.warn("比较失败");
                return false;
            }

        } else if (StringUtils.equals(oldObject.getClass().getName(), "java.math.BigDecimal")) {
            BigDecimal bitTarge = (BigDecimal) oldObject;
            BigDecimal bitExpect = (BigDecimal) newObject;
            if (bitTarge.intValue() != bitExpect.intValue()) {
                return false;
            }
        } else if (oldObject.getClass() == String.class) {
            if (!oldObject.equals(newObject)) {
                return false;
            }
        } else {
            if (!oldObject.equals(newObject)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     *
     * @return property value of reportStr
     */
    public static String getReportResult() {
        return reportStr.toString();
    }
}
