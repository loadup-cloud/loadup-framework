/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils.check;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.components.testify.util.VelocityUtil;
import com.github.loadup.components.testify.utils.DetailCollectUtils;
import com.github.loadup.components.testify.utils.ObjectUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectCompareUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectCompareUtil.class);
    private static final String[] comparableTypes = {
        "int",
        "float",
        "double",
        "long",
        "short",
        "byte",
        "boolean",
        "char",
        "java.lang.Integer",
        "java.lang.Float",
        "java.lang.Double",
        "java.lang.Long",
        "java.lang.Short",
        "java.lang.Byte",
        "java.lang.Boolean",
        "java.lang.Character",
        "java.lang.String",
        "java.math.BigDecimal",
        "java.util.Date"
    };
    // 预期结果变量替换
    public static ThreadLocal<Map<String, Object>> varParaMapHolder = new ThreadLocal<Map<String, Object>>();

    // 字段校验方法Y：普通校验，N：不校验，R：正则校验<className,<fieldName,flag>>
    public static ThreadLocal<Map<String, Map<String, String>>> varFlagMapHolder =
            new ThreadLocal<Map<String, Map<String, String>>>();

    // 自定义flag方法
    public static ThreadLocal<Map<String, Method>> flagMethodsHolder = new ThreadLocal<Map<String, Method>>();

    // 自定义flag方法所在类实例
    public static ThreadLocal<Object> flagMethodObjHolder = new ThreadLocal<Object>();
    private static ThreadLocal<StringBuffer> reportStrHolder = ThreadLocal.withInitial(() -> new StringBuffer());

    public static void compare(
            Object actual, Object expect, Map<String, Map<String, String>> flags, Map<String, Object> paramMap) {

        varParaMapHolder.set(paramMap);
        varFlagMapHolder.set(flags);
        reportStrHolder.set(new StringBuffer());
        DetailCollectUtils.appendAndLog(
                "\nexpect:" + ObjectUtil.toJson(expect) + "\nactual:" + ObjectUtil.toJson(actual), LOGGER);

        if (!compareByFields(actual, expect)) {
            throw new AssertionError(
                    "[CheckFailed]比对失败，详情如下：" + reportStrHolder.get().toString());
        }
    }

    public static boolean matchObj(
            Object actual, Object expect, Map<String, Map<String, String>> flags, Map<String, Object> paramMap) {

        varParaMapHolder.set(paramMap);
        varFlagMapHolder.set(flags);
        return compareByFields(actual, expect);
    }

    public static boolean compareByFields(Object actual, Object expect) {

        /* 1.1 特殊场景：处理为空的场景 */
        if (actual == null && expect == null) {
            return true;
        }
        if ((actual == null && expect != null) || (expect == null && actual != null)) {
            appendCmpReport("比对值为空，实际值：" + actual + ", 预期值：" + expect);
            return false;
        }

        /* 1.2 特殊场景：如果对象类型是Throwable/StackTraceElement[],,打条日志,直接认为成功 */
        if (actual.getClass().equals(Throwable.class) || actual.getClass().equals(StackTraceElement[].class)) {
            appendCmpReport("合理情况,跳过当前类型校验" + actual.getClass().getName());
            return true;
        }

        /* 2.1 前置准备：预期结果变量动态替换 */
        if (expect instanceof String) {
            // 统一使用velocity做变量替换
            String expectStr = (String) expect;
            if (expectStr.indexOf("$") != -1) { // 说明有需要替换的变量
                String parsedValue = VelocityUtil.evaluateString(varParaMapHolder.get(), expectStr);
                expect = parsedValue;
            }
        }

        /* 2.2 前置准备：变量初始化 */
        String objName = actual.getClass().getName();
        Class<?> objType = actual.getClass();

        /* 3.1 正式校验：根据对象类型开启正式比对 */
        if (isComparable(objType)) {
            return compare(actual, expect);

        } else if (actual instanceof byte[]) {
            String sExp = Arrays.toString((byte[]) expect);
            String sAct = Arrays.toString((byte[]) actual);

            if (!sExp.equals(sAct)) {
                appendCmpReport("byte[]比对不一致" + objName);
                return false;
            }

        } else if (objType.isArray()) {
            Object[] actualArray = (Object[]) actual;
            Object[] expectArray = (Object[]) expect;
            if (actualArray.length != expectArray.length) {
                appendCmpReport("数组长度不匹配" + objName);
                return false;
            }
            for (int i = 0; i < actualArray.length; i++) {
                if (!compareByFields(actualArray[i], expectArray[i])) {
                    appendCmpReport("数组元素校验失败,index=" + i);
                    return false;
                }
            }
            return true;

        } else if (actual instanceof Map) {

            Map<Object, Object> actualMap = (Map) actual;
            Map<Object, Object> expectMap = (Map) expect;

            if (actualMap.size() != expectMap.size()) {
                appendCmpReport("hashMap size不一致" + objName);
                return false;
            }

            // 双向严格一致校验、支持乱序
            // 以期望Map为准校验实际值
            for (Entry<Object, Object> entry : expectMap.entrySet()) {
                Object expectVal = entry.getValue();
                Object actualVal = actualMap.get(entry.getKey());
                if (!(compareByFields(actualVal, expectVal))) {
                    appendCmpReport("hashmap元素校验失败，key=" + entry.getKey());
                    return false;
                }
            }

            // 以实际Map为准校验期望值
            for (Entry<Object, Object> entry : actualMap.entrySet()) {
                Object actualVal = entry.getValue();
                Object expectVal = expectMap.get(entry.getKey());
                if (!(compareByFields(actualVal, expectVal))) {
                    appendCmpReport("hashmap元素校验失败，key=" + entry.getKey());
                    return false;
                }
            }

        } else if (actual instanceof List) {
            List actualList = (List) actual;
            List expectListCpy = Lists.newArrayList((List) expect);
            if (actualList.size() != expectListCpy.size()) {
                appendCmpReport("列表长度不一致" + objName);
                return false;
            }

            // 以实际List为准校验实际值
            for (Object objActual : actualList) {
                boolean isFound = false;
                // 记录报告起始位置
                int reportStart = reportStrHolder.get().length();
                for (Object objExpect : expectListCpy) {
                    if (compareByFields(objActual, objExpect)) {
                        isFound = true;
                        // 消除列表错位比对造成的噪音报告
                        reportStrHolder
                                .get()
                                .delete(reportStart, reportStrHolder.get().length());
                        // 防止多个相同实际元素对应到一个预期元素
                        expectListCpy.remove(objExpect);
                        break;
                    }
                }
                // 实际结果元素循环找一遍没找到
                if (!isFound) {
                    appendCmpReport("目标元素未在期望列表中找到" + ObjectUtil.toJson(objActual));
                    return false;
                }
            }

        } else {
            /* 3.2 正式校验：复杂对象类型比对 */
            return complexObjCompare(objType, objName, actual, expect);
        }

        return true;
    }

    public static boolean complexObjCompare(Class<?> objType, String objName, Object actual, Object expect) {
        boolean isSame = true;
        Map objFlag = varFlagMapHolder.get() != null ? varFlagMapHolder.get().get(objName) : null;

        // 提取复杂对象内部的fields
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = objType; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !fields.contains(field)) {
                    fields.add(field);
                }
            }
        }

        // 内部Fields逐一比对
        StringBuilder flagSuggest =
                new StringBuilder("If you want to exclude the field , please add the content to yaml's flag tag:" + "\n"
                        + objName + ": \n\t");
        String addFlag = "";
        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldCoord = objName + "#" + fieldName; // 字段坐标，用于标注比对位置

            // 如果对象类型是Throwable/StackTraceElement[],,打条日志,直接认为成功
            if (StringUtils.equals(fieldName, "suppressedExceptions")
                    || field.getType().equals(Throwable.class)) {
                appendCmpReport("Skip Exceptions " + actual.getClass().getName());
                continue;
            }

            if (StringUtils.equals(fieldName, "stackTrace") && field.getType().equals(StackTraceElement[].class)) {
                appendCmpReport("Skip Exceptions:" + actual.getClass().getName() + ",Field:" + fieldName);
                continue;
            }

            try {
                field.setAccessible(true);
                Object objActual = field.get(actual);
                Object objExpect = field.get(expect);

                // 未设置flag的字段直接比对
                if (objFlag == null
                        || objFlag.get(fieldName) == null
                        || objFlag.get(fieldName).equals("Y")) {

                    if (!compareByFields(objActual, objExpect)) {
                        appendCmpReport("校验失败的对象属性: " + fieldCoord);
                        isSame = false;
                        if (StringUtils.isEmpty(addFlag)) {
                            addFlag = addFlag + fieldName + ": N";
                        } else {
                            addFlag = addFlag + "\n\t" + fieldName + ": N";
                        }
                    }

                    continue;
                }

                // 处理设置了特定flag的情况
                String flag = (String) objFlag.get(fieldName);

                if (flag.equals("N")) {
                    // 不校验该属性字段
                } else if (flag.equals("R")) {
                    // 使用正则匹配该属性字段
                    Pattern pattern = Pattern.compile((String) objExpect);
                    Matcher matcher = pattern.matcher((String) objActual);
                    boolean matchRes = matcher.matches();
                    if (!matchRes) {
                        isSame = false;
                    }

                } else if (flag.startsWith("D")) {
                    // Date时间校验
                    // 1. 处理空值情况
                    if (null == objActual && null == objExpect) {
                        continue;
                    }
                    if ((null == objActual && null != objExpect) || (null != objActual && null == objExpect)) {
                        appendCmpReport(fieldCoord + "时间校验失败,\n实际值:" + objActual + "\n期望值:" + objExpect);
                        isSame = false;
                        continue;
                    }
                    // 2. 检查是否是合法时间类型
                    if (!isTemporalType(objActual) || !isTemporalType(objExpect)) {
                        appendCmpReport(fieldCoord + "打了D标签但不是支持的时间类型！");
                        continue;
                    }

                    long actualMillis = toEpochMilli(objActual);
                    long expectMillis = toEpochMilli(objExpect);

                    // 存在两种情况，一种直接是D，一种是D200情况分别处理，D就直接按照yaml中存储的时间比对，D200和当前时间比对
                    if (flag.equals("D")) {
                        if (actualMillis != expectMillis) {
                            appendCmpReport(fieldCoord + "时间校验失败,\n实际值:" + objActual + "\n期望值:" + objExpect);
                            isSame = false;
                        }
                    } else {
                        // D200：允许200毫秒误差
                        long tolerance = parseTolerance(flag);
                        if (Math.abs(actualMillis - expectMillis) > tolerance) {
                            isSame = false;
                            appendCmpReport(fieldCoord + "时间校验失败,\n实际值:" + objActual + "\n期望值:" + objExpect + "\n误差值:"
                                    + tolerance / 1000);
                        }
                    }

                } else if (flag.equalsIgnoreCase("ME")) {
                    // 针对map类型的特殊比对
                    Map<Object, Object> tarMap = (Map) objActual;
                    Map<Object, Object> expMap = (Map) objExpect;

                    if (expMap == null) {
                        continue;
                    }

                    // 以期望Map为准校验实际值，此处的风险在于期望值写少了，校验的内容也变少。仅支持属性为Map的情况
                    for (Entry<Object, Object> entry : expMap.entrySet()) {
                        Object expectVal = entry.getValue();
                        Object actualVal = tarMap.get(entry.getKey());
                        if (!(compareByFields(actualVal, expectVal))) {
                            isSame = false;
                            appendCmpReport("hashmap元素校验失败，key=" + entry.getKey());
                        }
                    }

                } else if (flag.startsWith("MK@") && flag.length() > 3) {
                    Map<Object, Object> tarMap = (Map) objActual;
                    Map<Object, Object> expMap = (Map) objExpect;
                    // 过滤非校验KEY
                    // 格式：类#Map字段#MK@Key1^Key2^...
                    // 例子：SysInvokeServiceResult#responseMap#MK@h5ConfirmUrl^confirmUrl
                    for (String key : flag.split("@")[1].split("\\^")) {
                        expMap.remove(key);
                        tarMap.remove(key);
                    }

                    if (!compareByFields(tarMap, expMap)) {
                        appendCmpReport("校验失败的对象属性: " + fieldCoord);
                        isSame = false;
                    }

                } else if (flag.equalsIgnoreCase("J")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode actualJson = objectMapper.readTree(objectMapper.writeValueAsBytes(objActual));
                    JsonNode expectJson = objectMapper.readTree(objectMapper.writeValueAsBytes(objExpect));

                    if (!expectJson.equals(actualJson)) {
                        isSame = false;
                        appendCmpReport("JSON对象全量校验失败: "
                                + fieldCoord
                                + ", \n实际值: "
                                + actualJson.toPrettyString()
                                + "\n期望值: "
                                + expectJson.toPrettyString());
                    }
                } else if (flag.equalsIgnoreCase("JE")) {
                    ObjectMapper objectMapper = new ObjectMapper();

                    try {
                        // 序列化并解析为 JsonNode
                        JsonNode actualJson = objectMapper.readTree(objectMapper.writeValueAsBytes(objActual));
                        JsonNode expectJson = objectMapper.readTree(objectMapper.writeValueAsBytes(objExpect));

                        boolean fieldMatch = true;

                        // 仅比较期望值中包含的字段
                        Iterator<Map.Entry<String, JsonNode>> jsonFields = expectJson.fields();
                        while (jsonFields.hasNext()) {
                            Map.Entry<String, JsonNode> entry = jsonFields.next();
                            String key = entry.getKey();
                            JsonNode expectVal = entry.getValue();

                            if (!actualJson.has(key)) {
                                appendCmpReport("JSON对象字段缺失: " + fieldCoord + ", key=" + key);
                                fieldMatch = false;
                            } else {
                                JsonNode actualVal = actualJson.get(key);
                                if (!expectVal.equals(actualVal)) {
                                    appendCmpReport("JSON对象字段值不一致: " + fieldCoord + ", key=" + key + "\n实际值: "
                                            + actualVal + "\n期望值: "
                                            + expectVal);
                                    fieldMatch = false;
                                }
                            }
                        }

                        if (!fieldMatch) {
                            isSame = false;
                        }

                    } catch (JsonProcessingException e) {
                        isSame = false;
                        appendCmpReport("JSON序列化失败(flag=JE): " + fieldCoord + ", 错误信息: " + e.getMessage());
                    }
                } else {
                    // 搜索并尝试自定义flag方法
                    if (flagMethodsHolder.get() != null
                            && flagMethodObjHolder.get() != null
                            && flagMethodsHolder.get().containsKey(flag)) {
                        Method flagMethod = flagMethodsHolder.get().get(flag);
                        String cmpMsg = (String) flagMethod.invoke(flagMethodObjHolder.get(), objExpect, objActual);
                        if (StringUtils.isNotEmpty(cmpMsg)) {
                            isSame = false;
                            appendCmpReport("自定义Flag(" + flag + ")规则校验失败：" + fieldCoord + "\n" + cmpMsg);
                        }
                    } else {
                        isSame = false;
                        appendCmpReport("未找到自定义Flag(" + flag + ")规则，请添加该Flag对应的比对方法后再试：" + fieldCoord + "\n");
                    }
                }

            } catch (Exception e) {
                String msg = fieldCoord + " comparison failed! \n";
                msg += e.getClass().getName() + ": " + e.getMessage();

                appendCmpReport(msg);
                LOGGER.error(msg);
                return false;
            }
        }

        if (!StringUtils.isEmpty(addFlag)) {
            appendCmpReport(flagSuggest.append(addFlag + "\n").toString());
        }

        return isSame;
    }

    private static long parseTolerance(String flag) {
        if ("D".equals(flag)) {
            return 0; // 完全匹配
        }

        String suffix = flag.substring(1); // 去除 D 字符
        char unitChar = suffix.charAt(suffix.length() - 1);
        String valueStr = suffix.substring(0, suffix.length() - 1);

        Duration duration;

        try {
            long value = Long.parseLong(valueStr);
            switch (unitChar) {
                case 'S': // 秒
                    duration = Duration.ofSeconds(value);
                    break;
                case 'M': // 分钟
                    duration = Duration.ofMinutes(value);
                    break;
                case 'H': // 小时
                    duration = Duration.ofHours(value);
                    break;
                case 'm': // 毫秒（注意这里是小写 m）
                    duration = Duration.ofMillis(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported time unit: " + unitChar);
            }
        } catch (NumberFormatException e) {
            // 如果没有单位，默认是毫秒
            duration = Duration.ofMillis(Long.parseLong(suffix));
        }

        return duration.toMillis();
    }

    private static boolean isTemporalType(Object obj) {
        return obj instanceof Date
                || obj instanceof LocalDate
                || obj instanceof LocalDateTime
                || obj instanceof Instant;
    }

    private static long toEpochMilli(Object temporal) {
        if (temporal instanceof Date) {
            return ((Date) temporal).getTime();
        } else if (temporal instanceof LocalDate) {
            return ((LocalDate) temporal)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } else if (temporal instanceof LocalDateTime) {
            return ((LocalDateTime) temporal)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } else if (temporal instanceof Instant) {
            return ((Instant) temporal).toEpochMilli();
        } else if (temporal instanceof String) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime objExpect = LocalDateTime.parse((String) temporal, formatter);
                return objExpect.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (Exception e) {
                // 忽略或记录错误
            }
        }
        throw new IllegalArgumentException("Unsupported temporal type: " + temporal.getClass());
    }

    public static boolean isComparable(Class<?> objType) {
        for (String comparableType : comparableTypes) {
            if (comparableType.equals(objType.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean compare(Object actual, Object expect) {
        if (actual.getClass().isPrimitive()) {
            if (!(actual == expect)) {
                // LOGGER.warn("比较失败");
                return false;
            }

        } else if (StringUtils.equals(actual.getClass().getName(), "java.math.BigDecimal")) {
            BigDecimal bitTarge = (BigDecimal) actual;
            BigDecimal bitExpect = (BigDecimal) expect;
            if (0 != bitTarge.compareTo(bitExpect)) {
                return false;
            }
        } else {
            if (!actual.equals(expect)) {
                appendCmpReport("实际值: " + actual + "\n期望值: " + expect);
                return false;
            }
        }
        return true;
    }

    public static void addParam(String key, String value) {
        varParaMapHolder.get().put(key, value);
    }

    /**
     * set flag methods content
     *
     * @param flagMethods
     * @param obj
     */
    public static void setFlagMethodsHolder(Map<String, Method> flagMethods, Object obj) {
        flagMethodsHolder.set(flagMethods);
        flagMethodObjHolder.set(obj);
    }

    /**
     *
     *
     * @return property value of reportStr
     */
    public static StringBuffer getReportStr() {
        return reportStrHolder.get();
    }

    public static void appendCmpReport(String msg) {
        // get last line content
        String lstLine = "";
        int i = reportStrHolder.get().lastIndexOf("\n");
        if (i != -1) {
            lstLine = reportStrHolder.get().substring(i);
        }

        if (lstLine.contains("校验失败的对象属性") && !msg.contains("校验失败的对象属性")) {
            msg = "\n" + msg;
        }

        reportStrHolder.get().append("\n" + msg);
    }
}
