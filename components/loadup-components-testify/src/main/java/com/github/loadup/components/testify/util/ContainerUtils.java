/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.util;

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

import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.model.VirtualList;
import com.github.loadup.components.testify.model.VirtualMap;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 马洪良
 *
 */
public class ContainerUtils {

    /**
     * 处理LIST或者MAP类型
     *
     * @param rawType   原始类型
     * @param keyType   泛型的第一个类型
     * @param ValueType 泛型的第二个类型
     * @param csvRoot   文件的跟目录
     * @param setCalue  防止重复的value值
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void handContainer(
            Class<?> rawType,
            Type keyType,
            Type ValueType,
            final String csvRoot,
            Set<String> setCalue,
            Set<String> sbfWarn,
            Boolean isInterface) {
        if (keyType == null || null == rawType || CSVApisUtil.isWrapClass(rawType)) {
            return;
        }
        // 如果已经存在,不要覆盖,直接返回
        File file = new File(csvRoot);
        if (file.exists()) {
            return;
        }

        if (Collection.class.isAssignableFrom(rawType)) {
            // 最外层需要合并
            handContainerList(rawType, keyType, setCalue, csvRoot, sbfWarn, isInterface);
        } else if (Map.class.isAssignableFrom(rawType)) {
            handContainerMap(rawType, keyType, ValueType, csvRoot, setCalue, sbfWarn, isInterface);
        } else {
            if (rawType.isArray()) {
                rawType = rawType.getComponentType();
            }
            handContainerNoraml(rawType, keyType, csvRoot, setCalue, sbfWarn, isInterface);
        }

        return;
    }

    /**
     * @param rawType
     * @param keyType
     * @param setCalue
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void handContainerNoraml(
            Class<?> rawType,
            Type keyType,
            String csvRoot,
            Set<String> setCalue,
            Set<String> sbfWarn,
            Boolean isInterface) {
        Class<?> keyRaw = CSVApisUtil.getParameRawCls(keyType);
        if (keyType instanceof ParameterizedType) {
            if (Map.class.isAssignableFrom(keyRaw) || Collection.class.isAssignableFrom(keyRaw)) {
                Type innerFirst = CSVApisUtil.HandMutiParameType(keyType, 0);
                Type innerSecond = CSVApisUtil.HandMutiParameType(keyType, 1);
                String cvsChild = null;
                if (Map.class.isAssignableFrom(keyRaw)) {
                    cvsChild = CSVApisUtil.getGenericCsvFileName(keyRaw, CSVApisUtil.getClass(innerSecond, 1), csvRoot);
                } else {
                    cvsChild = CSVApisUtil.getGenericCsvFileName(keyRaw, CSVApisUtil.getClass(innerFirst, 0), csvRoot);
                }
                handContainer(
                        CSVApisUtil.getParameRawCls(keyType),
                        innerFirst,
                        innerSecond,
                        cvsChild,
                        setCalue,
                        sbfWarn,
                        isInterface);

                // 开始写外部的
                CSVApisUtil.doProcess(rawType, keyRaw, setCalue, csvRoot, sbfWarn);
                csvReplaceObj(keyRaw, cvsChild, csvRoot, sbfWarn);
            } else {
                Type innerFirst = CSVApisUtil.HandMutiParameType(keyType, 0);
                String cvsChild =
                        CSVApisUtil.getGenericCsvFileName(keyRaw, CSVApisUtil.getClass(innerFirst, 0), csvRoot);
                handContainerNoraml(keyRaw, innerFirst, cvsChild, setCalue, sbfWarn, isInterface);
                // 开始写外部的
                CSVApisUtil.doProcess(rawType, keyRaw, setCalue, csvRoot, sbfWarn);
                csvReplaceObj(keyRaw, cvsChild, csvRoot, sbfWarn);
            }
        } else {
            // 普通的类型
            if (null != keyRaw && keyRaw.isArray()) {
                keyRaw = keyRaw.getComponentType();
            }
            try {
                witeClassListToCvs(rawType, CSVApisUtil.getClass(keyType, 0), setCalue, sbfWarn, csvRoot);
            } catch (Exception e) {
                sbfWarn.add(rawType.toString());
            }
        }
    }

    /**
     * @param rawType
     * @param keyType
     * @param valueType
     * @param setCalue
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void handContainerMap(
            Class<?> rawType,
            Type keyType,
            Type valueType,
            String csvRoot,
            Set<String> setCalue,
            Set<String> sbfWarn,
            Boolean isInterface) {
        if (!Map.class.isAssignableFrom(rawType)) {
            return;
        }
        // 开始处理Key
        Class<?> keyCls = CSVApisUtil.getClass(keyType, 0);
        String keyPath = CSVApisUtil.getGenericCsvFileName(keyCls, null, csvRoot);
        // 开始写外部的
        CSVApisUtil.doProcess(keyCls, null, setCalue, keyPath, sbfWarn);

        // 开始处理value
        Class<?> valueRaw = CSVApisUtil.getParameRawCls(valueType);
        String valuePath = null;
        if (valueType instanceof ParameterizedType) {
            if (Map.class.isAssignableFrom(valueRaw)) {

                Class<?> clsMap = CSVApisUtil.getClass(CSVApisUtil.HandMutiParameType(valueType, 1), 0);
                valuePath = CSVApisUtil.getGenericCsvFileName(valueRaw, clsMap, csvRoot);
            } else {
                Class<?> clsMap = CSVApisUtil.getClass(CSVApisUtil.HandMutiParameType(valueType, 0), 0);
                valuePath = CSVApisUtil.getGenericCsvFileName(valueRaw, clsMap, csvRoot);
            }

            handContainer(
                    valueRaw,
                    CSVApisUtil.HandMutiParameType(valueType, 0),
                    CSVApisUtil.HandMutiParameType(valueType, 1),
                    valuePath,
                    setCalue,
                    sbfWarn,
                    isInterface);
        } else {
            valuePath = CSVApisUtil.getGenericCsvFileName(CSVApisUtil.getClass(valueType, 0), null, csvRoot);
            CSVApisUtil.doProcess(CSVApisUtil.getClass(valueType, 0), null, setCalue, valuePath, sbfWarn);
        }

        // 开始写LIST需要单独处理
        if (valueType instanceof ParameterizedType) {
            handleMapAndList(
                    rawType,
                    keyCls,
                    (Class) ((ParameterizedType) valueType).getRawType(),
                    isInterface,
                    csvRoot,
                    keyPath,
                    valuePath,
                    setCalue);
        } else {
            handleMapAndList(
                    rawType,
                    keyCls,
                    CSVApisUtil.getClass(valueType, 0),
                    isInterface,
                    csvRoot,
                    keyPath,
                    valuePath,
                    setCalue);
        }
    }

    /**
     * @param rawType
     * @param keyType
     * @param setCalue
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void handContainerList(
            Class<?> rawType,
            Type keyType,
            Set<String> setCalue,
            String csvRoot,
            Set<String> sbfWarn,
            Boolean isInterface) {
        if (!Collection.class.isAssignableFrom(rawType)) {
            return;
        }

        Class<?> keyRaw = CSVApisUtil.getParameRawCls(keyType);
        Class<?> keyRawCls = null;
        String cvsChild = null;

        // 所有带<>的情况
        if (keyType instanceof ParameterizedType) {

            // Map或者其他Collection的情况
            if (Map.class.isAssignableFrom(keyRaw) || Collection.class.isAssignableFrom(keyRaw)) {

                Type innerFirst = CSVApisUtil.HandMutiParameType(keyType, 0);
                Type innerSecond = CSVApisUtil.HandMutiParameType(keyType, 1);
                if (Map.class.isAssignableFrom(keyRaw)) {
                    // Map的情况
                    cvsChild = CSVApisUtil.getGenericCsvFileName(keyRaw, CSVApisUtil.getClass(innerFirst, 1), csvRoot);
                } else {
                    // 其他Collection的情况
                    cvsChild = CSVApisUtil.getGenericCsvFileName(keyRaw, CSVApisUtil.getClass(innerSecond, 0), csvRoot);
                }
                handContainer(
                        CSVApisUtil.getParameRawCls(keyType),
                        innerFirst,
                        innerSecond,
                        cvsChild,
                        setCalue,
                        sbfWarn,
                        isInterface);
                keyRawCls = keyRaw;

            } else {
                // 非Collection的范型的情况
                Type innerFirst = CSVApisUtil.HandMutiParameType(keyType, 0);
                cvsChild = CSVApisUtil.getGenericCsvFileName(keyRaw, CSVApisUtil.getClass(innerFirst, 0), csvRoot);
                handContainerNoraml(keyRaw, innerFirst, cvsChild, setCalue, sbfWarn, isInterface);
            }
        } else {
            keyRawCls = CSVApisUtil.getClass(keyType, 0);
            cvsChild = CSVApisUtil.getGenericCsvFileName(keyRawCls, null, csvRoot);
            CSVApisUtil.doProcess(keyRawCls, null, setCalue, cvsChild, sbfWarn);
        }
        // 开始写LIST需要单独处理
        handleMapAndList(rawType, keyRawCls, null, isInterface, csvRoot, cvsChild, null, setCalue);
    }

    private static void handleMapAndList(
            Class<?> rawType,
            Class<?> childClsOne,
            Class<?> childClsNext,
            Boolean isInterface,
            String csvRoot,
            String csvChildPath,
            String csvSecondPath,
            Set<String> setCalue) {
        List<String[]> outputValues = new ArrayList<String[]>();
        // 组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.CLASS.getCode());
        header.add(CSVColEnum.PROPERTY.getCode());
        header.add(CSVColEnum.TYPE.getCode());
        header.add(CSVColEnum.RULE.getCode());
        header.add(CSVColEnum.FLAG.getCode());
        header.add(CSVColEnum.VALUE.getCode());
        outputValues.add(header.toArray(new String[header.size()]));

        if (Collection.class.isAssignableFrom(rawType)) {
            //       if (isInterface) {
            List<String> value = new ArrayList<String>();
            value.add(VirtualList.class.getName());
            // 属性名
            value.add("virtualList");
            // 属性类型
            if (CSVApisUtil.isWrapClass(childClsOne)) {
                value.add(rawType.getName());
                // 原子数据规则
                value.add("");
                if (StringUtils.equals(childClsOne.getName(), "java.lang.String")) {
                    value.add("Y");
                    value.add("1");
                } else {
                    CSVApisUtil.addSimpleValue("virtualList", childClsOne, value);
                }
            } else {
                value.add(rawType.getName());
                // 原子数据规则
                value.add("");
                // 死循环控制
                if (setCalue.contains(childClsOne.getName())) {
                    value.add("N");
                    value.add("null");
                } else {
                    value.add("Y");
                    value.add(CSVApisUtil.cutCsvName(csvChildPath) + "@1");
                }
            }
            outputValues.add(value.toArray(new String[value.size()]));
            //            } else {
            //                return;
            //            }

        } else if (Map.class.isAssignableFrom(rawType)) {
            for (int i = 0; i < 2; i++) {
                List<String> value = new ArrayList<String>();
                if (i == 0) {
                    value.add(VirtualMap.class.getName());
                    // 属性名
                    value.add("mapKey");
                    // 属性类型
                    if (CSVApisUtil.isWrapClass(childClsOne)) {
                        value.add(childClsOne.getName());
                        // 原子数据规则
                        value.add("");
                        if (StringUtils.equals(childClsOne.getName(), "java.lang.String")) {
                            value.add("Y");
                            value.add("1");
                        } else {
                            CSVApisUtil.addSimpleValue("mapKey", childClsOne, value);
                        }
                    } else {
                        value.add(childClsOne.getName());
                        // 原子数据规则
                        value.add("");
                        // 死循环控制
                        if (setCalue.contains(childClsOne.getName())) {
                            value.add("N");
                            value.add("null");
                        } else {
                            value.add("Y");
                            value.add(CSVApisUtil.cutCsvName(csvChildPath) + "@1");
                        }
                    }
                } else {
                    value.add(null);
                    // 属性名
                    value.add("mapValue");
                    if (CSVApisUtil.isWrapClass(childClsNext)) {
                        value.add(childClsNext.getName());
                        // 原子数据规则
                        value.add("");
                        if (StringUtils.equals(childClsNext.getName(), "java.lang.String")) {
                            value.add("Y");
                            value.add("1");
                        } else {
                            CSVApisUtil.addSimpleValue("mapValue", childClsNext, value);
                        }
                    } else {
                        // 属性类型
                        value.add(childClsNext.getName());

                        // 原子数据规则
                        value.add("");
                        // 死循环控制
                        if (setCalue.contains(childClsNext.getName())) {
                            value.add("N");
                            value.add("null");
                        } else {
                            value.add("Y");
                            value.add(CSVApisUtil.cutCsvName(csvSecondPath) + "@1");
                        }
                    }
                }
                outputValues.add(value.toArray(new String[value.size()]));
            }
        }
        CSVApisUtil.writeToCsv(FileUtil.getTestResourceFileByRootPath(csvRoot), outputValues);
    }

    private static String witeClassListToCvs(
            Class<?> clsParent, Class<?> clsChild, Set<String> setCalue, Set<String> sbfWarn, final String csvRoot)
            throws IOException {
        String cvsChild = null;
        // 先处理内部类
        if (null != clsChild) {
            cvsChild = CSVApisUtil.getGenericCsvFileName(clsChild, null, csvRoot);
            CSVApisUtil.doProcess(clsChild, null, setCalue, cvsChild, sbfWarn);
        }

        // 开始写外部的
        CSVApisUtil.doProcess(clsParent, null, setCalue, csvRoot, sbfWarn);

        csvReplaceObj(clsChild, cvsChild, csvRoot, sbfWarn);

        return csvRoot;
    }

    /**
     * @param clsChild
     * @param cvsChild
     * @param csvParent
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void csvReplaceObj(Class<?> clsChild, String cvsChild, String csvParent, Set<String> sbfWarn) {
        try {
            if (null != clsChild) {
                FileReader fReader = new FileReader(csvParent);
                CSVReader csvReader = new CSVReader(fReader);
                List<String[]> readLine = csvReader.readAll();
                List<String> addLine = new ArrayList<String>(6);
                int i = 0;
                for (String[] readDetail : readLine) {
                    if (StringUtils.equals(readDetail[2], "java.lang.Object")) {
                        addLine.add(readDetail[0]);
                        addLine.add(readDetail[1]);
                        addLine.add(clsChild.getName());
                        addLine.add(readDetail[3]);

                        if (CSVApisUtil.isWrapClass(clsChild)) {
                            if (StringUtils.equals(clsChild.getName(), "java.lang.String")) {
                                addLine.add("Y");
                                addLine.add("1");
                            } else {
                                CSVApisUtil.addSimpleValue("", clsChild, addLine);
                            }
                        } else if (StringUtils.isNotBlank(cvsChild)) {
                            File openFile = new File(cvsChild);
                            if (openFile.exists()) {
                                addLine.add("Y");
                                addLine.add(CSVApisUtil.cutCsvName(cvsChild) + "@1");
                            } else {
                                addLine.add("N");
                                addLine.add("");
                            }
                        } else {
                            addLine.add("N");
                            addLine.add("");
                        }
                        break;
                    }
                    i++;
                }

                readLine.set(i, addLine.toArray(new String[addLine.size()]));

                csvReader.close();
                fReader.close();

                FileWriter fWrite = new FileWriter(csvParent);
                CSVWriter csvWriter = new CSVWriter(fWrite);
                csvWriter.writeAll(readLine);
                csvWriter.close();
                fWrite.close();
            }
        } catch (Exception e) {
            sbfWarn.add("文件中" + CSVApisUtil.cutCsvName(csvParent) + "添加属性文件" + CSVApisUtil.cutCsvName(cvsChild) + "失败");
        }
    }
}
