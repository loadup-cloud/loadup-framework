/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.helper;

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

import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.db.TestifyDBUtil;
import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.db.model.DBConnection;
import com.github.loadup.components.testify.db.offlineService.AbstractDBService;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.TestifyObjectUtil;
import com.github.loadup.components.testify.object.manager.ObjectTypeManager;
import com.github.loadup.components.testify.util.FileUtil;
import com.google.common.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import ognl.OgnlException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * CSV辅助工具
 */
@Slf4j
public class CSVHelper {

    private static final int STATIC = 0x00000008;
    private static final int FINAL = 0x00000010;
    private static final String MAP_CONTENT_TEMPLATE = "a:1"; // 多个元素的情况："a:1;b:2"
    private static final String LIST_CONTENT_TEMPLATE = "1"; // 多个元素的情况："1;2;3"
    private static final String COMPLEX_LIST_CONTENT_TEMPLATE = "FILE@1"; // 多个元素的情况："FILE@1;FILE@2"
    private static final String COMPLEX_MAP_CONTENT_TEMPLATE = "a:FILE@1"; // 多个元素的情况："a:FILE@1;b:FILE@2"
    private static final String COMPLEX_TYPE_CONTENT_TEMPLATE = "FILE@1";
    private static final String FILE_WORDS = "FILE";

    private static final ObjectTypeManager objectTypeManager = new ObjectTypeManager();

    /**
     * 基于表名和dbConfigKey生成空数据csv模板
     *
     * @param tableFullName
     * @param dbConfigKey
     */
    public static void genDBCSVFile(String tableFullName, String dbConfigKey) {
        if (StringUtils.isBlank(tableFullName)) {
            throw new TestifyException("表名为空，无法生成CSV文件" + tableFullName);
        }

        String suffix = tableFullName.substring(tableFullName.lastIndexOf("_") + 1);
        String tableName = tableFullName;
        if (StringUtils.isNumeric(suffix)) {
            tableName = tableFullName.substring(0, tableFullName.lastIndexOf("_"));
        }
        dbConfigKey = TestifyDBUtil.getDBConfigKey(tableName, dbConfigKey);
        DBConnection conn = new DBConnection(dbConfigKey);

        genDBCSVFile(
                tableFullName,
                TestifyPathConstants.DB_DATA_PATH + tableName + ".csv",
                conn.getUrl(),
                conn.getUserName(),
                conn.getPassword(),
                conn.getSchema());
    }

    /**
     * 生成数据库CSV空模板
     *
     * @param tableFullName 数据表全名，包含分库分表位
     * @param csvPath
     * @param dbUrl         数据库链接，OB的链接，Oracle的链接或Mysql的链接
     * @param userName      Oracle或Mysql的用户名
     * @param password      Oracle或Mysql的密码
     */
    public static void genDBCSVFile(
            String tableFullName, String csvPath, String dbUrl, String userName, String password, String schema) {
        if (StringUtils.isBlank(csvPath)) {
            throw new TestifyException("路径为空，无法生成CSV文件" + csvPath);
        }

        File file = FileUtil.getTestResourceFile(csvPath);
        if (file.exists()) {
            throw new TestifyException("文件【" + csvPath + "】已经存在，请尝试输入新文件路径");
        }
        List<String[]> outputValues = new ArrayList<String[]>();
        // 组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.COLUMN.getCode());
        header.add(CSVColEnum.COMMENT.getCode());
        header.add(CSVColEnum.TYPE.getCode());
        header.add(CSVColEnum.FLAG.getCode());
        header.add(CSVColEnum.VALUE.getCode());
        outputValues.add(header.toArray(new String[header.size()]));
        List<Map<String, Object>> map = null;
        try {
            AbstractDBService service = AbstractDBService.getService(dbUrl, userName, password, schema);
            if (null == service) {
                throw new TestifyException("数据库服务不能为空");
            }
            map = service.getTableInfo(tableFullName);

            // 兼容多表的情况
            if (map.size() == 0) {
                map = service.getTableInfo(tableFullName + "_000");
            }

            // 兼容多表的情况，部分系统表后缀两位，如主站acctrans
            if (map.size() == 0) {
                map = service.getTableInfo(tableFullName + "_00");
            }
        } catch (Exception e) {
            TestifyLogUtil.error(log, "数据库操作失败", e);
            return;
        }
        for (Map<String, Object> childMap : map) {
            List<String> value = new ArrayList<String>();
            String columnName = childMap.get("field").toString();
            boolean primaryFlag = Integer.valueOf(childMap.get("key").toString()) > 0 ? true : false;
            String comment = childMap.get("comment").toString();
            String columnType = childMap.get("type").toString();
            String flag;
            if (primaryFlag) flag = "C";
            else if (columnType.equals("TIMESTAMP")) {
                flag = "D";
            } else flag = "Y";

            value.add(columnName);
            value.add(comment);
            value.add(columnType);
            value.add(flag);
            if (flag.equals("D")) value.add("today");
            else if (columnName.equals("currency")) value.add("156");
            outputValues.add(value.toArray(new String[value.size()]));
        }
        writeToCsv(file, outputValues);
    }

    /**
     * 生成对象模板csv文件
     *
     * @param objClass
     * @param rootCsvPath
     */
    public static void genObjCSVFile(
            Class<?> objClass,
            String rootCsvPath,
            Class<?> variableType,
            final String clazzType,
            Map<String, Object> map) {

        if (null == rootCsvPath) {
            throw new TestifyException("路径为空，无法生成CSV文件");
        }
        if (null == objClass) {
            throw new TestifyException("类名为空，无法生成CSV文件");
        }

        File file = FileUtil.getTestResourceFileByRootPath(rootCsvPath);
        if (file.exists() || null != map.get(objClass.getName())) {
            TestifyLogUtil.warn(log, "文件【" + rootCsvPath + "】已经存在，直接跳过");
            return;
        }

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

        map.put(objClass.getName(), objClass);
        // 支持类继承
        List<Field> fields = new ArrayList<Field>();
        try {
            fields = TestifyObjectUtil.getAllFields(objClass);
        } catch (Throwable e) {
            TestifyLogUtil.warn(log, "载入类Field失败:" + objClass.getName() + ",异常原因:" + e);
            throw new TestifyException("载入类Field失败:" + objClass.getName(), e);
        }

        int i = 1;

        for (Field field : fields) {
            // 如果字段是static或者final，不生成在CSV文件里
            if ((field.getModifiers() & STATIC) > 0 || (field.getModifiers() & FINAL) > 0) {
                continue;
            }
            List<String> value = new ArrayList<String>();
            if (1 == i) {
                // 如果是第一个生成字段，则内容需要包含class名
                value.add(objClass.getName());
            } else {
                value.add("");
            }
            value.add(field.getName());
            value.add(field.getType().getName());

            value.add(""); // 原子数据规则

            if (objectTypeManager.isSimpleType(field.getType())) {
                /*包括Integer,Float,Double,Long,Short,Byte,Boolean,Character*/
                addSimpleValue(objClass, field, value);
            } else if (objectTypeManager.isCollectionType(field.getType())) {
                // 如果是当前的Filed集合对象,包括List,Map,ARRAY
                try {
                    addCollectionValue(rootCsvPath, map, field, value);
                } catch (Throwable exp) {
                    // 解析失败设置成空，flag为N.
                    value.add("N");
                    value.add("null");
                    outputValues.add(value.toArray(new String[value.size()]));
                    i++;
                    exp.printStackTrace();
                    continue;
                }

            } else if ((field.getGenericType() instanceof TypeVariable) && (variableType != null)) {
                // 如果当前的Filed范型,明确的指导当前的是例如LIST<T>
                try {
                    addParameterizedValue(rootCsvPath, variableType, clazzType, map, field, value);
                } catch (Throwable exp) {
                    // 解析失败设置成空，flag为N.
                    value.add("N");
                    value.add("null");
                    outputValues.add(value.toArray(new String[value.size()]));
                    i++;
                    exp.printStackTrace();
                    continue;
                }
            } else {
                // 复杂对象
                // 如果argumentClass与被解析类同类型，为了避免读模版时死循环，设置成空
                if (null != map.get(field.getType().getName())) {
                    value.add("N");
                    value.add("");
                } else {
                    String subCsvPath = getCsvFileName(field.getType(), rootCsvPath);
                    try {
                        genObjCSVFile(field.getType(), subCsvPath, null, null, map);
                    } catch (Throwable exp) {
                        // 解析失败设置成空，flag为N.
                        value.add("N");
                        value.add("null");
                        outputValues.add(value.toArray(new String[value.size()]));
                        i++;
                        exp.printStackTrace();
                        continue;
                    }
                    value.add("Y");
                    value.add(COMPLEX_TYPE_CONTENT_TEMPLATE.replace(
                            FILE_WORDS, field.getType().getSimpleName() + ".csv"));
                }
            }
            outputValues.add(value.toArray(new String[value.size()]));
            i++;
        }
        // 复杂对象没有field的情况
        if (fields.size() == 0) {
            List<String> value = new ArrayList<String>();
            value.add(objClass.getName());
            value.add("");
            value.add("");
            value.add("");
            value.add("");
            value.add("");
        }
        writeToCsv(file, outputValues);
    }

    /**
     * @param rootCsvPath
     * @param variableType
     * @param clazzType
     * @param map
     * @param field
     * @param value
     */
    private static void addParameterizedValue(
            String rootCsvPath,
            Class<?> variableType,
            final String clazzType,
            Map<String, Object> map,
            Field field,
            List<String> value) {
        if (objectTypeManager.isSimpleType(variableType)) {
            // 范型为简单类型
            addGSimpleValue(variableType, field, value);
        } else if (objectTypeManager.isCollectionType(variableType)) {
            // 范型是集合对象,包括List,Map,ARRAY
            Class<?> argumentClass =
                    objectTypeManager.getCollectionItemClass(variableType.getComponentType(), variableType);

            // 如果参数是简单类型
            if (objectTypeManager.isSimpleType(argumentClass)) {
                if (StringUtils.equals(Map.class.getName(), variableType.getName())
                        || StringUtils.equals(
                                java.util.HashMap.class.getName(),
                                field.getType().getName())) {
                    value.add("M");
                    value.add(MAP_CONTENT_TEMPLATE);
                } else {
                    value.add("Y");
                    value.add(LIST_CONTENT_TEMPLATE);
                }
            } else {
                // 如果argumentClass与被解析类同类型，为了避免读模版时死循环，设置成空
                // 暂存当前对象,避免死循环生成对象
                if (null != map.get(argumentClass.getName())) {
                    value.add("N");
                    value.add("");
                } else {
                    // 如果是复杂对象集合,那么继续递归.
                    String subCsvPath = getCsvFileName(argumentClass, rootCsvPath);
                    genObjCSVFile(argumentClass, subCsvPath, null, null, map);
                    value.add("Y");
                    value.add(
                            COMPLEX_LIST_CONTENT_TEMPLATE.replace(FILE_WORDS, argumentClass.getSimpleName() + ".csv"));
                }
            }
        } else {
            // 范型是复杂对象
            // 如果argumentClass与被解析类同类型，为了避免读模版时死循环，设置成空
            // 暂存当前对象,避免死循环生成对象
            if (null != map.get(variableType.getName())) {
                value.add("Y");
                value.add("");
            } else {
                String subCsvPath = getCsvFileName(variableType, rootCsvPath);
                genObjCSVFile(variableType, subCsvPath, null, null, map);
                String clsName = StringUtils.isBlank(clazzType) ? variableType.getName() : clazzType;
                value.set(2, clsName);
                value.add("Y");
                value.add(COMPLEX_TYPE_CONTENT_TEMPLATE.replace(FILE_WORDS, variableType.getSimpleName() + ".csv"));
            }
        }
    }

    /**
     * 当前的FILED对象时一个简单对象
     *
     * @param variableType
     * @param field
     * @param value
     */
    private static void addGSimpleValue(Class<?> variableType, Field field, List<String> value) {
        if (StringUtils.equals(java.util.Date.class.getName(), variableType.getName())) {
            value.add("D");
            value.add("today");
        } else {
            value.add("Y");
            value.add("");
        }
    }

    /**
     * 当前的FILED对象时一个COLLECTION对象
     *
     * @param rootCsvPath
     * @param map
     * @param field
     * @param value
     */
    private static void addCollectionValue(
            String rootCsvPath, Map<String, Object> map, Field field, List<String> value) {
        Class<?> argumentClass = objectTypeManager.getCollectionItemClass(field.getGenericType(), field.getType());
        // 如果参数是简单类型
        if (objectTypeManager.isSimpleType(argumentClass)) {
            if (StringUtils.equals(Map.class.getName(), field.getType().getName())
                    || StringUtils.equals(
                            java.util.HashMap.class.getName(), field.getType().getName())) {
                value.add("M");
                value.add(MAP_CONTENT_TEMPLATE);
            } else {
                value.add("Y");
                value.add(LIST_CONTENT_TEMPLATE);
            }
        } else {
            // 如果argumentClass与被解析类同类型，为了避免读模版时死循环，设置成空
            // 暂存当前对象,避免死循环生成对象
            if (null != map.get(argumentClass.getName())) {
                value.add("N");
                value.add("");
            } else {
                // 如果是复杂对象集合,那么继续递归.
                String subCsvPath = getCsvFileName(argumentClass, rootCsvPath);
                genObjCSVFile(argumentClass, subCsvPath, null, null, map);

                // 判断是List<Object>还是<String,Object>
                if (StringUtils.equals(Map.class.getName(), field.getType().getName())
                        || StringUtils.equals(
                                java.util.HashMap.class.getName(),
                                field.getType().getName())) {
                    value.add("M");
                    value.add(COMPLEX_MAP_CONTENT_TEMPLATE.replace(FILE_WORDS, argumentClass.getSimpleName() + ".csv"));
                } else if (StringUtils.equals(Map.class.getName(), argumentClass.getName())
                        || StringUtils.equals(java.util.HashMap.class.getName(), argumentClass.getName())) {
                    // 这儿有个坑，只支持LIST<MAP<STRING,STRING>>
                    value.set(2, "java.util.List<java.util.Map>");
                    value.add("M");
                    value.add("key:value;");
                } else {
                    Type genType = field.getGenericType();
                    TypeToken<?> superType = TypeToken.of(genType);
                    value.set(2, superType.resolveType(genType).getType().toString());
                    value.add("Y");
                    value.add(
                            COMPLEX_LIST_CONTENT_TEMPLATE.replace(FILE_WORDS, argumentClass.getSimpleName() + ".csv"));
                }
            }
        }
    }

    /**
     * @param objClass
     * @param field
     * @param value
     */
    private static void addSimpleValue(Class<?> objClass, Field field, List<String> value) {
        if (StringUtils.equals(java.util.Date.class.getName(), field.getType().getName())) {
            value.add("D");
            value.add("today");
        } else if (StringUtils.equals(
                java.util.Currency.class.getName(), field.getType().getName())) {

            value.add("Y");
            value.add("CNY");

        } else if (StringUtils.equals(objClass.getSimpleName(), "MultiCurrencyMoney")
                && StringUtils.equals(field.getName(), "currencyValue")) {

            // 对MultiCurrencyMoney中的currencyValue赋值默认156，否则加载模版和写yaml有问题
            value.add("Y");
            value.add("156");

        } else if (StringUtils.equals(field.getType().getName(), "int")
                || StringUtils.equals(field.getType().getName(), "java.lang.Integer")
                || StringUtils.equals(field.getType().getName(), "long")
                || StringUtils.equals(field.getType().getName(), "java.lang.Long")
                || StringUtils.equals(field.getType().getName(), "short")) {
            value.add("Y");
            value.add("0");
        } else if (StringUtils.equals(field.getType().getName(), "float")
                || StringUtils.equals(field.getType().getName(), "double")) {
            value.add("Y");
            value.add("0.0");
        } else if (StringUtils.equals(field.getType().getName(), "boolean")) {
            value.add("Y");
            value.add("false");
        } else if (StringUtils.equals(field.getType().getName(), "char")) {
            value.add("Y");
            value.add("A");
        } else {
            value.add("Y");
            value.add("");
        }
    }

    /**
     * 基于动态数据生成对象csv文件
     *
     * @param actual
     * @param csvPath
     */
    public static void genObjCSVFileWithData(Object actual, String csvPath) {
        if (StringUtils.isBlank(csvPath) || actual == null) {
            throw new TestifyException("路径为空，无法生成CSV文件");
        }
        try {
            insertObjDataAndReturnIndex(actual, csvPath);
            log.warn("CSV文件生成完毕");
        } catch (Throwable e) {
            throw new TestifyException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int insertObjDataAndReturnIndex(Object actual, String csvPath) {

        List<String[]> outputValues = new ArrayList<String[]>();

        File file = FileUtil.getTestResourceFileByRootPath(csvPath);
        if (file.exists()) {
            outputValues = readFromCsv(file);
        } else {
            List<String> header = new ArrayList<String>();
            header.add(CSVColEnum.CLASS.getCode());
            header.add(CSVColEnum.PROPERTY.getCode());
            header.add(CSVColEnum.TYPE.getCode());
            header.add(CSVColEnum.RULE.getCode());
            header.add(CSVColEnum.FLAG.getCode());
            outputValues.add(header.toArray(new String[header.size()]));

            // Field[] fields = actual.getClass().getDeclaredFields();
            // 增加父类支持
            List<Field> fields = TestifyObjectUtil.getAllFields(actual.getClass());

            int i = 1;

            // 填充非value部分
            for (Field field : fields) {

                // 如果字段是static或者final，不生成在CSV文件里
                if ((field.getModifiers() & STATIC) > 0 || (field.getModifiers() & FINAL) > 0) {
                    continue;
                }
                List<String> value = new ArrayList<String>();
                if (1 == i) {
                    // 如果是第一个生成字段，则内容需要包含class名
                    value.add(actual.getClass().getName());
                } else {
                    value.add("");
                }
                value.add(field.getName());
                value.add(field.getType().getName());
                value.add("");
                value.add("Y");
                outputValues.add(value.toArray(new String[value.size()]));
                i++;
            }
        }

        // 填充value部分
        int index = outputValues.get(0).length - 4;
        int row = outputValues.size();

        for (int i = 0; i < row; i++) {

            List<String> value = new ArrayList<String>();
            for (int j = 0; j < outputValues.get(i).length; j++) {
                value.add(outputValues.get(i)[j]);
            }
            if (0 == i) {
                value.add("value" + String.valueOf(index));
            } else {
                Object actualField = null;
                try {
                    actualField = TestifyObjectUtil.getProperty(actual, value.get(1));
                } catch (OgnlException e) {

                    TestifyLogUtil.warn(log, "获取对象" + actual + "属性" + value.get(1) + "失败:" + e);
                    throw new RuntimeException(e);
                }
                if (actualField == null) {
                    value.add("null");
                } else {
                    // 增加父类支持
                    Field field = TestifyObjectUtil.getField(actual.getClass(), value.get(1));
                    Class<?> clz = field.getType();

                    if (objectTypeManager.isSimpleType(clz)) {

                        // 简单类型
                        String simpleValue = objectTypeManager.getSimpleObjValue(clz, actualField, value.get(1));
                        value.add(simpleValue);
                    } else if (objectTypeManager.isCollectionType(clz)) {

                        Class<?> argumentClass;

                        if (clz.isArray()) {
                            // Array 数组的情况
                            argumentClass = clz.getComponentType();
                        } else {
                            // 集合类型
                            argumentClass = objectTypeManager.getCollectionItemClass(field.getGenericType(), clz);
                        }
                        if (objectTypeManager.isSimpleType(argumentClass)) {
                            String collectionStr =
                                    objectTypeManager.getCollectionObjectString(clz, actualField, true, null);
                            value.add(collectionStr);
                        } else {

                            String subCsvPath = getCsvFileName(argumentClass, csvPath);
                            String collectionStr =
                                    objectTypeManager.getCollectionObjectString(clz, actualField, false, subCsvPath);
                            value.add(collectionStr);
                        }
                    } else {

                        // 复杂类型
                        String subCsvPath = getCsvFileName(clz, csvPath);
                        int subIndex = insertObjDataAndReturnIndex(actualField, subCsvPath);
                        value.add(clz.getSimpleName() + ".csv@" + String.valueOf(subIndex));
                    }
                }
            }
            outputValues.set(i, value.toArray(new String[value.size()]));
        }
        writeToCsv(file, outputValues);
        return index;
    }

    /**
     * 获取csv文件名
     *
     * @param objClass
     * @param csvPath
     * @return
     */
    public static String getCsvFileName(Class<?> objClass, String csvPath) {

        String[] paths = csvPath.split("/");
        ArrayUtils.reverse(paths);

        String className = objClass.getSimpleName() + ".csv";

        if (!StringUtils.equals(className, paths[0])) {
            csvPath = StringUtils.replace(csvPath, paths[0], className);
        }

        return csvPath;
    }

    /**
     * 写入csv文件
     *
     * @param file
     * @param outputValues
     */
    public static void writeToCsv(File file, List<String[]> outputValues) {

        // 初始化写入文件
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {

            TestifyLogUtil.warn(log, "初始化写入文件【" + file.getName() + "】失败" + e);
            throw new RuntimeException(e);
        }
        // 将生成内容写入CSV文件
        try {
            OutputStreamWriter osw = null;
            osw = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(osw);
            csvWriter.writeAll(outputValues);
            csvWriter.close();
            TestifyLogUtil.warn(log, file.getName() + "生成完毕");
        } catch (Exception e) {
            TestifyLogUtil.warn(log, "通过文件流输出数据失败:" + file.getName() + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 基于路径读取csv文件
     *
     * @param csvPath
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List readFromCsv(String csvPath) {
        File file = new File(csvPath);
        return readFromCsv(file);
    }

    /**
     * 基于路径读取csv文件
     *
     * @param csvPath
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List readFromCsv(String csvPath, String encoding) {
        File file = new File(csvPath);
        return readFromCsv(file, encoding);
    }

    /**
     * 基于文件读取csv文件
     *
     * @param file
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static List readFromCsv(File file) {
        if (null == file) {
            throw new TestifyException("文件不能为空");
        }
        if (!file.exists()) {
            throw new TestifyException(file.getAbsolutePath() + "文件不存在");
        }
        // 初始化读入文件
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "读入文件【" + file.getName() + "】初始化失败", e);
        }

        // 读取文件内容
        List tableList = null;
        try {
            InputStreamReader isr = new InputStreamReader(inputStream);
            CSVReader csvReader = new CSVReader(isr);
            tableList = csvReader.readAll();
            csvReader.close();
            isr.close();
            inputStream.close();
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "通过CSV文件流读入数据失败", e);
        }
        return tableList;
    }

    /**
     * 基于文件读取csv文件,带编码方式
     *
     * @param file
     * @param encode 编码方式，如:UTF-8
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static List readFromCsv(File file, String encode) {
        if (null == file) {
            throw new TestifyException("文件不能为空");
        }
        if (!file.exists()) {
            throw new TestifyException(file.getAbsolutePath() + "文件不存在");
        }
        // 初始化读入文件
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "读入文件【" + file.getName() + "】初始化失败", e);
        }

        // 读取文件内容
        List tableList = null;
        try {
            InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName(encode));
            CSVReader csvReader = new CSVReader(isr);
            tableList = csvReader.readAll();
            csvReader.close();
            isr.close();
            inputStream.close();
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "通过CSV文件流读入数据失败", e);
        }
        return tableList;
    }
}
