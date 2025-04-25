/**
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.object.processor;

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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.github.loadup.components.testify.cache.TestifyCacheData;
import com.github.loadup.components.testify.context.TestifyCaseContextHolder;
import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.model.VirtualList;
import com.github.loadup.components.testify.model.VirtualMap;
import com.github.loadup.components.testify.object.TestifyObjectUtil;
import com.github.loadup.components.testify.object.enums.UnitFlagEnum;
import com.github.loadup.components.testify.object.manager.ObjectTypeManager;
import com.github.loadup.components.testify.util.BaseDataUtil;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.yaml.cpUnit.property.BaseUnitProperty;
import com.github.loadup.components.testify.yaml.cpUnit.property.ListObjectUnitProperty;
import com.github.loadup.components.testify.yaml.cpUnit.property.MapObjectUnitProperty;
import com.github.loadup.components.testify.yaml.cpUnit.property.ObjectUnitProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ognl.OgnlException;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;
import org.testng.Assert;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * 对象处理器
 */
@Slf4j
@Getter
@Setter
public class ObjectProcessor {


    private final ObjectTypeManager objectTypeManager = new ObjectTypeManager();

    /**
     * 当前对象对应csv文件名
     */
    private final String csvPath;

    /**
     * 当前对象列名
     */
    private final String description;

    /**
     * 当前差异化属性数据
     */
    private final Map<String, BaseUnitProperty> attributeMap;
    /**
     * 当前对象的列名
     */
    public Map<String, Map<String, String>> flags = new LinkedHashMap<String, Map<String, String>>();
    /**
     * 当前对象路径
     */
    private String keyPath;
    /**
     * 当前csv文件类名
     */
    private String className;
    /**
     * 当前属性类名
     */
    private Class<?> classType;
    private ClassLoader classLoader;

    public ObjectProcessor(ClassLoader classLoader, String csvPath, String description) {
        this.csvPath = csvPath;
        this.description = description;
        this.attributeMap = new HashMap<String, BaseUnitProperty>();
        this.classLoader = classLoader;
        loadCSVFile();
        this.keyPath = this.className;
    }

    public ObjectProcessor(ClassLoader classLoader, String csvPath, String description,
                           String encoding) {
        this.csvPath = csvPath;
        this.description = description;
        this.attributeMap = new HashMap<String, BaseUnitProperty>();
        this.classLoader = classLoader;
        loadCSVFile(encoding);
        this.keyPath = this.className;
    }

    public ObjectProcessor(String csvPath, String description) {
        this.csvPath = csvPath;
        this.description = description;
        this.attributeMap = new HashMap<String, BaseUnitProperty>();

        loadCSVFile();
        this.keyPath = this.className;
    }

    public ObjectProcessor(String csvPath, String description, String encoding) {
        this.csvPath = csvPath;
        this.description = description;
        this.attributeMap = new HashMap<String, BaseUnitProperty>();

        loadCSVFile(encoding);
        this.keyPath = this.className;
    }

    /**
     * 基于CP点差异化数据生成对象
     *
     * @param cls
     * @param csvPath
     * @param desc
     * @return
     * @throws Exception
     */
    public Object genObject() throws Exception {

        //3. 锁定对象实际类名
        Class<?> objClass = null;
        String realClassName = this.className;

        //构造一个map用来存放flag
        Map<String, String> flagMap = new HashMap<String, String>();
        if (this.classType == null || realClassName.contains(".")) {
            //3.1 只有第一层对象, 或在csv文件标识为qualifiedName时可以走到这里
            try {
                objClass = classLoader.loadClass(realClassName);
            } catch (ClassNotFoundException e) {
                TestifyLogUtil.error(log, "对象QualifiedName无法加载:" + realClassName, e);
                throw new Exception("对象QualifiedName无法加载:" + realClassName + "建议mvn一下");
            }
        } else if (!this.classType.getSimpleName().equals(realClassName)) {
            //3.2 类名不匹配，用反射在包内查找对应子类
            String packageName = this.classType.getPackage().getName();
            String prefix = packageName.substring(0, packageName.lastIndexOf('.'));
            Reflections reflections = new Reflections(ClasspathHelper.forPackage(prefix),
                    new SubTypesScanner(), new FilterBuilder().includePackage(prefix));
            for (Class<?> subClass : reflections.getSubTypesOf(objClass)) {
                if (StringUtils.equals(subClass.getSimpleName(), realClassName.trim())) {
                    objClass = subClass;
                    break;
                }
            }
        } else {
            objClass = this.classType;
        }

        //4. 构造类对象
        Object objValue = TestifyObjectUtil.genInstance(objClass);

        //5. 逐个填充属性
        for (Entry<String, BaseUnitProperty> entry : this.attributeMap.entrySet()) {
            String fieldName = entry.getKey();
            if (StringUtils.isBlank(fieldName)) {
                //空的就跳过
                continue;
            }
            BaseUnitProperty property = entry.getValue();
            String flagCode = property.getFlagCode();
            Object referedValue = property.getExpectValue();

            //5.2 反射获取field
            Field field = TestifyObjectUtil.getField(objClass, fieldName);
            Class<?> propertyClass = TestifyObjectUtil.getClass(objClass, fieldName);
            flagMap.put(fieldName, flagCode);

            if (field == null || propertyClass == null) {
                TestifyLogUtil.error(log, "字段【" + fieldName + "】不存在类【" + objClass.getSimpleName()
                        + "】及其父类中");
                throw new Exception("请检查模板或者去掉字段" + fieldName + "类为" + objClass.getSimpleName());
            }

            Class<?> fieldType = field.getType();

            //5.3 初始化属性对象
            Object fieldValue = null;

            //5.3 基于差异化数据找到当前属性flag标识
            flagCode = (property != null && property.getFlagCode() != null) ? property
                    .getFlagCode() : flagCode;

            //5.4当flagCode不为N时，需要进行填充数据
            if (UnitFlagEnum.getByCode(flagCode) != UnitFlagEnum.N
                    && !referedValue.toString().equals("null")) {
                if (UnitFlagEnum.getByCode(flagCode) == UnitFlagEnum.CUSTOM) {
                    //5.4.1自定义标识走自定义生成器
                    fieldValue = TestifyCacheData.getCustomGenerator(flagCode).generater(
                            this.keyPath + "." + fieldName, property.getExpectValue(),
                            fieldType.getSimpleName());
                } else if (objectTypeManager.isSimpleType(fieldType)) {
                    //5.4.2简单类型处理，即当前属性为简单类型
                    fieldValue = generateSimpleProperty(fieldName, fieldType, flagCode,
                            referedValue);
                } else {
                    if (objectTypeManager.isCollectionType(fieldType)) {
                        //5.4.3.1 复合类型处理，即当前属性为数组、List或Map类型
                        //获取子对象属性
                        //这里开始特殊逻辑的判断，分别对新框架下的MapConvert
                        //ListConvert 开始处理
                        String csvDir = StringUtils.substringBeforeLast(csvPath, "/");
                        String exceptValue = (String) property.getExpectValue();
                        String desc = StringUtils.substringAfterLast(exceptValue, "@");
                        String convertCsv = null;
                        if (StringUtils.substringBefore(exceptValue, "@").contains(":")) {
                            convertCsv = csvDir
                                    + "/"
                                    + StringUtils.substringAfter(
                                    (StringUtils.substringBefore(exceptValue, "@")), ":");
                        } else {
                            convertCsv = csvDir + "/"
                                    + StringUtils.substringBefore(exceptValue, "@");
                        }

                        if (Map.class.isAssignableFrom(fieldType)
                                && ObjHandUtil.isSubListConvert(convertCsv, VirtualMap.class.getName())) {

                            //VirtualMap的情况单独处理，加载所有key－value
                            Map<Object, Object> map = new HashMap<Object, Object>();
                            List<String> descList = BaseDataUtil.loadDesc(convertCsv);

                            for (String descTemp : descList) {
                                ObjectProcessor processorTemp = new ObjectProcessor(classLoader,
                                        convertCsv, descTemp);
                                Map<Object, Object> descMap = (Map<Object, Object>) processorTemp
                                        .genObject();
                                map.putAll(descMap);
                            }

                            fieldValue = map;

                        } else if (Collection.class.isAssignableFrom(fieldType)
                                && ObjHandUtil.isSubListConvert(convertCsv,
                                VirtualList.class.getName())) {
                            ObjectProcessor processor = new ObjectProcessor(classLoader,
                                    convertCsv, desc);
                            fieldValue = processor.genObject();
                        } else {
                            Class<?> argumentClass = objectTypeManager.getCollectionItemClass(
                                    field.getGenericType(), fieldType);

                            //Array 数组的情况
                            if (fieldType.isArray()) {
                                argumentClass = fieldType.getComponentType();
                            }

                            if (objectTypeManager.isSimpleType(argumentClass)) {
                                //复合子属性为简单对象
                                fieldValue = generateSimpleCollection(property, argumentClass,
                                        fieldType, fieldName, referedValue);
                            } else {
                                //符合子属性为复杂对象
                                fieldValue = generateComplexCollection(property, argumentClass,
                                        fieldType, fieldName, referedValue);
                            }
                        }
                    } else {
                        //5.4.3.2 复杂对象处理
                        fieldValue = generateChildObject(property, fieldType, fieldName,
                                referedValue);
                    }
                }
                property.setActualValue(fieldValue);
                TestifyObjectUtil.setProperty(objValue, fieldName, fieldValue);
            } else if (StringUtils.equals(referedValue == null ? "null" : referedValue.toString(),
                    "null")) {
                property.setActualValue(fieldValue);
                TestifyObjectUtil.setProperty(objValue, fieldName, fieldValue);
            }
        }

        //存放当前对象的flag值
        flags.put(realClassName, flagMap);

        if (StringUtils.equals(objClass.getName(), "com.github.loadup.components.test.model.VirtualMap")) {
            return ObjHandUtil.handMapConvert(objValue);
        } else if (StringUtils.equals(objClass.getName(), "com.github.loadup.components.test.model.VirtualList")) {
            return ObjHandUtil.handListConvert(objValue, csvPath);
        }
        return objValue;
    }

    /**
     * 比较对象
     *
     * @param property
     * @param object
     */
    public void checkObject(Object actual) {
        for (Entry<String, BaseUnitProperty> entry : this.attributeMap.entrySet()) {
            String columnName = entry.getKey();
            BaseUnitProperty property = this.attributeMap.get(columnName);
            String flagCode = property.getFlagCode();
            Object expectValue = property.getExpectValue();

            //同上下文动态Map替换
            Map<String, Object> uniqueMap = TestifyCaseContextHolder.get().getUniqueMap();
            if (uniqueMap.containsKey(this.keyPath + "." + columnName)) {
                expectValue = uniqueMap.get(this.keyPath + "." + columnName);
            } else if (uniqueMap.containsKey(columnName)) {
                expectValue = uniqueMap.get(columnName);
            }

            Object actualField = null;
            //1. 获取到传进来的对象中所对应列属性值
            try {
                actualField = TestifyObjectUtil.getProperty(actual, columnName);
            } catch (OgnlException e) {
                TestifyLogUtil.error(log, String.format("使用ongl获取:%s对象的属性:%s时报错,csv文件路径为:%s",
                        actual, columnName, csvPath), e);
                continue;
            }

            if (actualField == null) {
                if (!StringUtils.isBlank(String.valueOf(expectValue))) {
                    TestifyLogUtil.error(log, this.keyPath + "." + columnName + "同" + this.csvPath
                            + "列" + this.description + "校验失败, 期望值:" + expectValue
                            + ",实际值:" + String.valueOf(actualField) + ",校验标识:"
                            + flagCode);
                }
            } else {
                //差异化对象递归比较
                property.compare(actualField);
            }
        }
    }

    /**
     * 生成子属性对象
     *
     * @param property
     * @param fieldType
     * @param fieldName
     * @param referedValue
     * @return
     */
    protected Object generateChildObject(BaseUnitProperty property, Class<?> fieldType,
                                         String fieldName, Object referedValue) {

        if (StringUtils.isBlank(String.valueOf(referedValue))) {
            return null;
        } else {
            if (property instanceof ObjectUnitProperty) {
                return ((ObjectUnitProperty) property).genObject(classLoader);
            } else {
                return referedValue;
            }
        }

    }

    /**
     * 生成子属性复合对象
     *
     * @param property
     * @param argumentClass
     * @param fieldClass
     * @param fieldName
     * @param referedValue
     * @return
     */
    protected Object generateComplexCollection(BaseUnitProperty property, Class<?> argumentClass,
                                               Class<?> fieldClass, String fieldName,
                                               Object referedValue) {
        Object fieldValue = null;
        if (property.getExpectValue() == null) {
            return null;
        }

        if (property instanceof ListObjectUnitProperty) {
            ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
            listProperty.setTargetCSVPath(FileUtil.getRelativePath(argumentClass.getSimpleName()
                    + ".csv", this.csvPath));
            listProperty.setClassType(argumentClass);
            return listProperty.genObject(classLoader);
        } else if (property instanceof MapObjectUnitProperty) {
            MapObjectUnitProperty mapProperty = (MapObjectUnitProperty) property;
            if (String.valueOf(mapProperty.getBaseValue()).contains(".csv@")) {
                mapProperty.setTargetCSVPath(FileUtil.getRelativePath(argumentClass.getSimpleName()
                        + ".csv", this.csvPath));
            }
            mapProperty.setClassType(argumentClass);
            return mapProperty.genObject(classLoader);

        } else if (!(property.getExpectValue() instanceof String)) {
            TestifyLogUtil.fail(log, "不能在yaml中为简单collection属性对象设定非String的值");
        }

        String value = String.valueOf(referedValue);
        if (StringUtils.isBlank(value)) {
            return null;
        } else if (StringUtils.equals("@element_empty@", value)) {
            return objectTypeManager.getCollectionObject(fieldClass);
        } else {
            String[] valueParts = value.split("@");
//            Assert.assertTrue("复杂对象描述字符串必须包含且只包含一个@", valueParts.length == 2);
            String[] values = valueParts[1].trim().split(";");

            if (fieldClass.isArray()) {
                fieldValue = Array.newInstance(fieldClass.getComponentType(), valueParts.length);
            } else {
                fieldValue = objectTypeManager.getCollectionObject(fieldClass);
            }
            for (int i = 0; i < values.length; i++) {
                Object valuePart = generateChildObject(property, argumentClass, fieldName,
                        valueParts[0].trim() + "@" + values[i].trim());
                objectTypeManager.setCollectionObjectValue(fieldValue, valuePart, values[i], i,
                        fieldClass);
            }
        }

        return fieldValue;
    }

    /**
     * 生成简单集合对象
     *
     * @param property
     * @param argumentClass
     * @param fieldClass
     * @param fieldName
     * @param referedValue
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Object generateSimpleCollection(BaseUnitProperty property, Class<?> argumentClass,
                                              Class<?> fieldClass, String fieldName,
                                              Object referedValue) {
        Object fieldValue = null;
        if (property.getExpectValue() == null) {
            return null;
        }

        if (property instanceof ListObjectUnitProperty) {
            ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
            List<BaseUnitProperty> childPropertyList = listProperty.getObjectList();
            fieldValue = new ArrayList();
            for (BaseUnitProperty childProperty : childPropertyList) {
                ((List) fieldValue).add(childProperty.getExpectValue());
            }
            return fieldValue;
        } else if (!(property.getExpectValue() instanceof String)) {
            TestifyLogUtil.fail(log, "不能在yaml中为简单collection属性对象设定非String的值");
        }

        String value = String.valueOf(referedValue);
        if (StringUtils.isBlank(value)) {
            return null;
        } else if (StringUtils.equals("@element_empty@", value)) {
            return objectTypeManager.getCollectionObject(fieldClass);
        } else if (value.startsWith("{") || value.startsWith("[")) {
            return JSON.parseObject(value, new TypeReference<Map<String, String>>() {
            });
        } else {
            String[] valueParts = value.split(";");
            if (fieldClass.isArray()) {
                fieldValue = Array.newInstance(fieldClass.getComponentType(), valueParts.length);
            } else {
                fieldValue = objectTypeManager.getCollectionObject(fieldClass);
            }
            for (int i = 0; i < valueParts.length; i++) {
                Object valuePart = objectTypeManager.getSimpleObject(argumentClass, valueParts[i],
                        fieldName, argumentClass.getName());
                objectTypeManager.setCollectionObjectValue(fieldValue, valuePart, valueParts[i], i,
                        fieldClass);
            }
        }

        return fieldValue;
    }

    /**
     * 生成简单类型对象
     *
     * @param fieldName
     * @param fieldType
     * @param flagCode
     * @param referedValue
     * @return
     */
    protected Object generateSimpleProperty(String fieldName, Class<?> fieldType, String flagCode,
                                            Object referedValue) {

        Object fieldValue = null;
        if (referedValue == null || StringUtils.isBlank(String.valueOf(referedValue))) {

            return objectTypeManager.getSimpleObject(fieldType, String.valueOf(referedValue),
                    fieldName, fieldType.getName());
        }

        switch (UnitFlagEnum.getByCode(flagCode)) {
            case F:
                return FileUtil.readFile(FileUtil.getRelativePath(String.valueOf(referedValue),
                        this.csvPath));
            case D:
            case C:
            case Y:
                return objectTypeManager.getSimpleObject(fieldType, String.valueOf(referedValue),
                        fieldName, fieldType.getName());
            default:
                Assert.fail(this.keyPath + "." + fieldName + "生成对象时，需要对标识" + flagCode + "进行替换");
                break;
        }
        return fieldValue;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void loadCSVFile() throws TestifyException {
        //1. 加载CSV数据
        List tableList = CSVHelper.readFromCsv(this.csvPath);
        if (tableList == null || tableList.size() == 0)
            TestifyLogUtil.fail(log, this.csvPath + "文件内容为空");
        if (tableList.size() < 2) {
            throw new TestifyException("当前的CSV没有内容或者内容不全,文件名为" + csvPath);
        }
        //2. 锁定列实际序号
        String[] labels = (String[]) tableList.get(0);
        int baseIndex = 0, classNameCol = 0, colNameCol = 0, flagCol = 0, indexCol = -1;
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i].toLowerCase().trim();

            if (StringUtils.equals(label, this.description)) {
                indexCol = i;
            } else {
                CSVColEnum labelEnum = CSVColEnum.getByCode(label);
                if (labelEnum != null) {
                    switch (labelEnum) {
                        case CLASS:
                            classNameCol = i;
                            baseIndex++;
                            break;
                        case PROPERTY:
                            colNameCol = i;
                            baseIndex++;
                            break;
                        case TYPE:
                            baseIndex++;
                            break;
                        case RULE:
                            baseIndex++;
                            break;
                        case FLAG:
                            flagCol = i;
                            baseIndex++;
                            break;
                        default:
                            Assert.fail("csv文件格式有误");
                    }
                }
            }
        }
        boolean noValue = false;
        if (indexCol == -1) {
            if (StringUtils.isNumeric(this.description)) {
                indexCol = baseIndex + Integer.valueOf(this.description) - 1;
            } else {
                noValue = true;
            }
        }

        this.className = ((String[]) tableList.get(1))[classNameCol];
        for (int i = 1; i < tableList.size(); i++) {
            String[] row = (String[]) tableList.get(i);
            String fieldName = row[colNameCol];
            String flagCode = row[flagCol];
            String referedValue = noValue ? null : row[indexCol];

            //5.1 CSV加载值中对引号需要过滤处理
            referedValue = StringUtils.replace(referedValue, "\"\"", "\"");
            BaseUnitProperty property = null;
            if (!this.attributeMap.containsKey(fieldName)) {
                if (!noValue && referedValue.contains(".csv@")) {
                    if (!referedValue.contains(":")) {
                        String[] valueParts = referedValue.split(".csv@");

                        //list<复杂对象>有多列时，获取复杂对象的desc的列号
                        String[] descParts = referedValue.split(";");
                        for (int index = 0; index < descParts.length; index++) {
                            String temp = StringUtils.substringAfter(descParts[index], "@");
                            descParts[index] = temp;
                        }

                        if (descParts.length == 1) {
                            Map<String, Object> attribute = new HashMap<String, Object>();
                            attribute.put("__desc", valueParts[0] + "@" + valueParts[1]);
                            property = new ObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName, this.csvPath,
                                    attribute);
                        } else {

                            //list<Object>的情况,Set<Object>暂不支持
                            property = new ListObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName,
                                    this.csvPath, new ArrayList());
                            ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
                            for (int j = 0; j < descParts.length; j++) {
                                Map<String, Object> attribute = new HashMap<String, Object>();
                                attribute.put("__desc", valueParts[0] + "@" + descParts[j]);
                                ObjectUnitProperty childProperty = new ObjectUnitProperty(
                                        fieldName, this.keyPath + "." + fieldName + "[" + j + "]",
                                        this.csvPath, attribute);
                                listProperty.getObjectList().add(childProperty);
                            }
                        }
                    } else {

                        String[] parts = referedValue.split(";");
                        Map<String, BaseUnitProperty> tmpMap = new LinkedHashMap<String, BaseUnitProperty>();
                        int index = 0;
                        for (String part : parts) {

                            String[] mapParts = part.split("\\:");
                            String[] baseInfo = mapParts[1].split(".csv@");
                            String key = mapParts[0];
                            String baseName = baseInfo[0];
                            String desc = baseInfo[1];
                            Map<String, Object> attribute = new HashMap<String, Object>();
                            attribute.put("__desc", baseName + "@" + desc);
                            BaseUnitProperty mapProperty = new ObjectUnitProperty(fieldName,
                                    this.keyPath + "." + fieldName + "[" + index + "]", this.csvPath,
                                    attribute);
                            tmpMap.put(key, mapProperty);
                            index++;

                        }
                        property = new MapObjectUnitProperty(fieldName, this.keyPath + "."
                                + fieldName, this.csvPath,
                                tmpMap);
                    }
                } else {
                    if (!referedValue.contains(":")) {
                        property = new BaseUnitProperty(fieldName, this.keyPath + "." + fieldName,
                                referedValue);
                    } else {
                        String[] parts = referedValue.split(";");
                        Map<String, BaseUnitProperty> tmpMap = new LinkedHashMap<String, BaseUnitProperty>();
                        int index = 0;
                        for (String part : parts) {
                            String[] mapParts = part.split("\\:");
                            String key = mapParts[0];
                            String value = mapParts[1];
                            BaseUnitProperty mapProperty = new BaseUnitProperty(fieldName,
                                    this.keyPath + "." + fieldName + "[" + index + "]", value);
                            tmpMap.put(key, mapProperty);
                            property = new MapObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName,
                                    this.csvPath, tmpMap);
                        }
                    }
                }

                property.setExpectValue(referedValue);
                property.setFlagCode(flagCode);

            } else {
                property = this.attributeMap.get(fieldName);
                if (property.getFlagCode() == null) {
                    property.setFlagCode(flagCode);
                }
            }
            property.setBaseValue(referedValue);
            property.setBaseFlagCode(flagCode);
            this.attributeMap.put(fieldName, property);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void loadCSVFile(String encoding) throws TestifyException {
        //1. 加载CSV数据
        List tableList = CSVHelper.readFromCsv(this.csvPath, encoding);
        if (tableList == null || tableList.size() == 0)
            TestifyLogUtil.fail(log, this.csvPath + "文件内容为空");
        if (tableList.size() < 2) {
            throw new TestifyException("当前的CSV没有内容或者内容不全,文件名为" + csvPath);
        }
        //2. 锁定列实际序号
        String[] labels = (String[]) tableList.get(0);
        int baseIndex = 0, classNameCol = 0, colNameCol = 0, flagCol = 0, indexCol = -1;
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i].toLowerCase().trim();

            if (StringUtils.equalsIgnoreCase(label, this.description)) {
                indexCol = i;
            } else {
                CSVColEnum labelEnum = CSVColEnum.getByCode(label);
                if (labelEnum != null) {
                    switch (labelEnum) {
                        case CLASS:
                            classNameCol = i;
                            baseIndex++;
                            break;
                        case PROPERTY:
                            colNameCol = i;
                            baseIndex++;
                            break;
                        case TYPE:
                            baseIndex++;
                            break;
                        case RULE:
                            baseIndex++;
                            break;
                        case FLAG:
                            flagCol = i;
                            baseIndex++;
                            break;
                        default:
                            Assert.fail("csv文件格式有误");
                    }
                }
            }
        }
        boolean noValue = false;
        if (indexCol == -1) {
            if (StringUtils.isNumeric(this.description)) {
                indexCol = baseIndex + Integer.valueOf(this.description) - 1;
            } else {
                noValue = true;
            }
        }

        this.className = ((String[]) tableList.get(1))[classNameCol];
        for (int i = 1; i < tableList.size(); i++) {
            String[] row = (String[]) tableList.get(i);
            String fieldName = row[colNameCol];
            String flagCode = row[flagCol];
            String referedValue = noValue ? null : row[indexCol];

            //5.1 CSV加载值中对引号需要过滤处理
            referedValue = StringUtils.replace(referedValue, "\"\"", "\"");
            BaseUnitProperty property = null;
            if (!this.attributeMap.containsKey(fieldName)) {
                if (!noValue && referedValue.contains(".csv@")) {
                    if (!referedValue.contains(":")) {
                        String[] valueParts = referedValue.split(".csv@");

                        //list<复杂对象>有多列时，获取复杂对象的desc的列号
                        String[] descParts = referedValue.split(";");
                        for (int index = 0; index < descParts.length; index++) {
                            String temp = StringUtils.substringAfter(descParts[index], "@");
                            descParts[index] = temp;
                        }

                        if (descParts.length == 1) {
                            Map<String, Object> attribute = new HashMap<String, Object>();
                            attribute.put("__desc", valueParts[0] + "@" + valueParts[1]);
                            property = new ObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName, this.csvPath,
                                    attribute);
                        } else {

                            //list<Object>的情况,Set<Object>暂不支持
                            property = new ListObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName,
                                    this.csvPath, new ArrayList());
                            ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
                            for (int j = 0; j < descParts.length; j++) {
                                Map<String, Object> attribute = new HashMap<String, Object>();
                                attribute.put("__desc", valueParts[0] + "@" + descParts[j]);
                                ObjectUnitProperty childProperty = new ObjectUnitProperty(
                                        fieldName, this.keyPath + "." + fieldName + "[" + j + "]",
                                        this.csvPath, attribute);
                                listProperty.getObjectList().add(childProperty);
                            }
                        }
                    } else {

                        String[] parts = referedValue.split(";");
                        Map<String, BaseUnitProperty> tmpMap = new LinkedHashMap<String, BaseUnitProperty>();
                        int index = 0;
                        for (String part : parts) {

                            String[] mapParts = part.split("\\:");
                            String[] baseInfo = mapParts[1].split(".csv@");
                            String key = mapParts[0];
                            String baseName = baseInfo[0];
                            String desc = baseInfo[1];
                            Map<String, Object> attribute = new HashMap<String, Object>();
                            attribute.put("__desc", baseName + "@" + desc);
                            BaseUnitProperty mapProperty = new ObjectUnitProperty(fieldName,
                                    this.keyPath + "." + fieldName + "[" + index + "]", this.csvPath,
                                    attribute);
                            tmpMap.put(key, mapProperty);
                            index++;

                        }
                        property = new MapObjectUnitProperty(fieldName, this.keyPath + "."
                                + fieldName, this.csvPath,
                                tmpMap);
                    }
                } else {
                    if (!referedValue.contains(":")) {
                        property = new BaseUnitProperty(fieldName, this.keyPath + "." + fieldName,
                                referedValue);
                    } else {
                        String[] parts = referedValue.split(";");
                        Map<String, BaseUnitProperty> tmpMap = new LinkedHashMap<String, BaseUnitProperty>();
                        int index = 0;
                        for (String part : parts) {
                            String[] mapParts = part.split("\\:");
                            String key = mapParts[0];
                            String value = mapParts[1];
                            BaseUnitProperty mapProperty = new BaseUnitProperty(fieldName,
                                    this.keyPath + "." + fieldName + "[" + index + "]", value);
                            tmpMap.put(key, mapProperty);
                            property = new MapObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName,
                                    this.csvPath, tmpMap);
                        }
                    }
                }

                property.setExpectValue(referedValue);
                property.setFlagCode(flagCode);

            } else {
                property = this.attributeMap.get(fieldName);
                if (property.getFlagCode() == null) {
                    property.setFlagCode(flagCode);
                }
            }
            property.setBaseValue(referedValue);
            property.setBaseFlagCode(flagCode);
            this.attributeMap.put(fieldName, property);
        }
    }

}
