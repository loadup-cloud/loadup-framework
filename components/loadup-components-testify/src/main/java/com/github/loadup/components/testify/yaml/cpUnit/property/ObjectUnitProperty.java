/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.yaml.cpUnit.property;

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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import ognl.OgnlException;

import org.apache.commons.lang3.ClassUtils;

import org.testng.Assert;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.github.loadup.components.testify.cache.TestifyCacheData;
import com.github.loadup.components.testify.context.TestifyCaseContextHolder;
import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.exception.ModleFileException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.model.VirtualList;
import com.github.loadup.components.testify.model.VirtualMap;
import com.github.loadup.components.testify.object.TestifyObjectUtil;
import com.github.loadup.components.testify.object.enums.UnitFlagEnum;
import com.github.loadup.components.testify.object.generator.CustomGenerator;
import com.github.loadup.components.testify.object.manager.ObjectTypeManager;
import com.github.loadup.components.testify.object.processor.ObjHandUtil;
import com.github.loadup.components.testify.object.processor.ObjectProcessor;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.yaml.cpUnit.ObjectCPUnit;

/**
 * 对象基础属性
 *
 * 
 *
 */
@Slf4j
public class ObjectUnitProperty extends BaseUnitProperty {
    private final ObjectTypeManager objectTypeManager = new ObjectTypeManager();
    private String targetCSVPath;
    private Class<?> classType;
    /**
     * 对象描述，同时用作序列标识
     */
    private String description;
    /**
     * 对象属性列表
     */
    private Map<String, BaseUnitProperty> attributeMap = new LinkedHashMap<String, BaseUnitProperty>();
    /**
     * 当前csv文件类名
     */
    private String className;
    private ClassLoader classLoader;

    @SuppressWarnings("unchecked")
    public ObjectUnitProperty(String keyName, String keyPath, String parentCSVPath,
                              Map<String, Object> attribute) {
        super(keyName, keyPath, null);
        this.description = ("" + attribute.get("__desc")).trim();
        if (this.description.contains("@")) {
            String[] valueParts = this.description.split("@");
            this.keyName = valueParts[0].trim();
            this.description = valueParts[1].trim();
        }
        this.targetCSVPath = generateTargetCSVPath(this.keyName, parentCSVPath);
        attribute.remove("__desc");
        for (Entry<String, Object> entry : attribute.entrySet()) {
            String childKeyName = entry.getKey();
            Object value = entry.getValue();
            String flagCode = null;
            if (childKeyName.endsWith("]")) {
                flagCode = childKeyName.substring(childKeyName.indexOf('[') + 1,
                        childKeyName.length() - 1);
                childKeyName = childKeyName.substring(0, childKeyName.indexOf('['));
            }
            String childKeyPath = this.keyPath + "." + childKeyName;
            BaseUnitProperty property = null;
            if (value instanceof Map) {
                Map<String, Object> mapValue = (Map<String, Object>) value;
                if (mapValue.get("__desc") != null) {
                    //按复杂类处理
                    property = new ObjectUnitProperty(childKeyName, childKeyPath,
                            this.targetCSVPath, mapValue);
                } else {
                    //按普通Map对象处理
                    property = new BaseUnitProperty(childKeyName, childKeyPath, value);
                }
            } else if (value instanceof List) {
                List<Object> listValue = (List<Object>) value;
                property = new ListObjectUnitProperty(childKeyName, childKeyPath, parentCSVPath,
                        listValue);
            } else if (value instanceof VirtualList) {
                List<Object> listValue = (List<Object>) value;
                property = new ListObjectUnitProperty(childKeyName, childKeyPath, parentCSVPath,
                        listValue);
            } else if (value instanceof VirtualMap) {
                //TODO  Key 为对象如何处理呢？
                Map<String, Object> mapValue = (Map<String, Object>) value;
                if (mapValue.get("__desc") != null) {
                    //按复杂类处理
                    property = new ObjectUnitProperty(childKeyName, childKeyPath,
                            this.targetCSVPath, mapValue);
                }
            } else {
                property = new BaseUnitProperty(childKeyName, childKeyPath, value);
            }

            if (flagCode != null) {
                property.setFlagCode(flagCode);
            }
            this.attributeMap.put(childKeyName, property);
        }

        this.loadCSVFile();

    }

    /**
     * 用于将ObjectCPUnit转换为ObjectUnitProperty
     *
     * @param unit
     */
    public ObjectUnitProperty(ObjectCPUnit unit) {
        super(unit.getUnitName(), unit.getUnitName(), null);
        this.description = unit.getDescription();
        this.targetCSVPath = unit.getTargetCSVPath();
        this.attributeMap = unit.getAttributeMap();
        this.loadCSVFile();
    }

    /**
     * 获取csv文件相对路径
     *
     * @param csvFileName
     * @return
     */
    private static String generateTargetCSVPath(String propertyName, String parentCSVPath) {
        return FileUtil.getRelativePath(propertyName + ".csv", parentCSVPath);
    }

    /**
     * 生成普通对象
     *
     * @param classLoader
     * @param property
     * @return
     */
    public Object genObject(ClassLoader classLoader) {
        //3. 锁定对象实际类名
        Class<?> objClass = null;
        String realClassName = this.className;
        this.classLoader = classLoader;
        if (this.classType == null || realClassName.contains(".")) {
            //3.1 只有第一层对象, 或在csv文件标识为qualifiedName时可以走到这里
            try {
                objClass = classLoader.loadClass(realClassName);
            } catch (ClassNotFoundException e) {
                TestifyLogUtil.error(log, "对象QualifiedName无法加载:" + realClassName, e);
            }
        } else if (!this.classType.getSimpleName().equals(realClassName)) {
            //3.2 类名不匹配，用反射在包内查找对应子类
            String packageName = ClassUtils.getPackageName(className);
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
        Object objValue = null;
        try {
            objValue = TestifyObjectUtil.genInstance(objClass);
        } catch (Exception e) {
            TestifyLogUtil.error(log, "对" + this.keyPath + "对应类【" + objClass.getSimpleName()
                    + "】创建对象失败，请尝试使用实现类名或实现类Qualified Name填入csv", e);
            return objValue;
        }

        //5. 逐个填充属性
        for (Entry<String, BaseUnitProperty> entry : this.attributeMap.entrySet()) {
            String fieldName = entry.getKey();
            BaseUnitProperty property = entry.getValue();
            String flagCode = property.getFlagCode();
            Object referedValue = property.getExpectValue();

            Object oldReferedValue = referedValue;
            //referedValue = replaceUniqueValue(property, referedValue, fieldName);

            //5.2 反射获取field
            Field field = TestifyObjectUtil.getField(objClass, fieldName);
            Class<?> propertyClass = TestifyObjectUtil.getClass(objClass, fieldName);
            Class<?> fieldType = field.getType();

            if (field == null || propertyClass == null) {
                TestifyLogUtil.error(log, "字段【" + fieldName + "】不存在类【" + objClass.getSimpleName()
                        + "】及其父类中");
                return null;
            }

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
                        Class<?> argumentClass = objectTypeManager.getCollectionItemClass(
                                field.getGenericType(), fieldType);

                        //Array 数组的情况
                        if (fieldType.isArray()) {
                            argumentClass = fieldType.getComponentType();
                        }


                        String csvDir = StringUtils.substringBeforeLast(targetCSVPath, "/");
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
                            ObjectProcessor processor = new ObjectProcessor(classLoader,
                                    convertCsv, desc);
                            try {
                                fieldValue = processor.genObject();
                            } catch (Exception e) {
                                log.error("处理mapConvert存在异常", e);
                            }
                        } else if (Collection.class.isAssignableFrom(fieldType)
                                && ObjHandUtil.isSubListConvert(convertCsv,
                                VirtualList.class.getName())) {
                            ObjectProcessor processor = new ObjectProcessor(classLoader,
                                    convertCsv, desc);
                            try {
                                fieldValue = processor.genObject();
                            } catch (Exception e) {
                                log.error("处理listConvert存在异常", e);
                            }
                        } else if (objectTypeManager.isSimpleType(argumentClass)) {
                            //复合子属性为简单对象
                            fieldValue = generateSimpleCollection(property, argumentClass,
                                    fieldType, fieldName, referedValue);
                        } else {
                            //符合子属性为复杂对象
                            fieldValue = generateComplexCollection(property, argumentClass,
                                    fieldType, fieldName, referedValue);
                        }
                    } else {
                        //5.4.3.2 复杂对象处理
                        if (property instanceof ObjectUnitProperty) {
                            fieldValue = generateChildObject((ObjectUnitProperty) property,
                                    fieldType, fieldName, referedValue);
                        } else {
                            String value = String.valueOf(referedValue);
                            if (!StringUtils.isBlank(value)) {
                                if (referedValue.equals("null")) {
                                    fieldValue = null;
                                } else if (StringUtils.equals(objClass.getSimpleName(),
                                        "VirtualList")
                                        || StringUtils.equals(objClass.getSimpleName(),
                                        "VirtualMap")) {
                                    fieldValue = referedValue;
                                } else {
                                    TestifyLogUtil.fail(log, "不允许复杂对象参考数值为普通非空字符串");
                                }

                            }
                        }
                    }
                }
                TestifyObjectUtil.setProperty(objValue, fieldName, fieldValue);
                if (property.isUnique()) {
                    property.setExpectValue(oldReferedValue);
                }
            } else if (StringUtils.equals(referedValue == null ? "null" : referedValue.toString(),
                    "null")) {
                property.setActualValue(fieldValue);
                TestifyObjectUtil.setProperty(objValue, fieldName, fieldValue);
            }
        }

        if (StringUtils.equals(objClass.getName(), "com.github.loadup.components.test.model.VirtualMap")) {
            return ObjHandUtil.handMapConvert(objValue);
        } else if (StringUtils.equals(objClass.getName(), "com.github.loadup.components.test.model.VirtualList")) {
            return ObjHandUtil.handListConvert(objValue, targetCSVPath);
        }
        return objValue;

    }

    /**
     * 对象比较工具类，回调方法
     *
     * @param object
     * @param csvPath
     * @param desc
     */
    @Override
    public void compare(Object object) {
        for (Entry<String, BaseUnitProperty> entry : this.attributeMap.entrySet()) {
            String columnName = entry.getKey();
            BaseUnitProperty property = this.attributeMap.get(columnName);
            String flagCode = property.getFlagCode();
            Object expectValue = property.getExpectValue();

            //同上下文动态Map替换
            expectValue = replaceUniqueValue(property, expectValue, columnName);

            Object actualField = null;
            //1. 获取到传进来的对象中所对应列属性值
            try {
                actualField = TestifyObjectUtil.getProperty(object, columnName);
            } catch (OgnlException e) {
                TestifyLogUtil.error(log, String.format("使用ongl获取:%s对象的属性:%s时报错,csv文件路径为:%s",
                        object, columnName, this.targetCSVPath), e);
                continue;
            }

            if (actualField == null) {
                if (!StringUtils.isBlank(String.valueOf(expectValue))) {
                    TestifyLogUtil.error(
                            log,
                            this.keyPath + "." + columnName + "同" + this.targetCSVPath + "列"
                                    + this.description + "校验失败, 期望值:" + expectValue + ",实际值:"
                                    + String.valueOf(actualField) + ",校验标识:" + flagCode);
                    property.setActualValue(actualField);
                    property.setCompareSuccess(false);
                }
            } else {
                //差异化对象递归比较
                property.compare(actualField);
                if (!property.isCompareSuccess()) {
                    if (property.getClass() == BaseUnitProperty.class) {
                        TestifyLogUtil.error(log,
                                this.keyPath + "." + columnName + "同" + this.targetCSVPath + "列"
                                        + this.description + "校验失败, 期望值:" + expectValue + ",实际值:"
                                        + String.valueOf(actualField) + ",校验标识:" + flagCode);
                        //生成baseline（新的expectedvalue）
                        if (!property.isUnique()) {
                            if (UnitFlagEnum.getByCode(flagCode) == UnitFlagEnum.CUSTOM) {
                                CustomGenerator generator = TestifyCacheData
                                        .getCustomGenerator(this.flagCode);
                                property.setActualValue(generator.generater(this.targetCSVPath,
                                        actualField, actualField.getClass().getSimpleName()));
                            } else {
                                property.setActualValue(generateSimpleProperty(flagCode,
                                        actualField.getClass(), flagCode, actualField));
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object dump(String fieldName) {
        Map dumpMap = new LinkedHashMap();
        Map objectMap = new LinkedHashMap();
        objectMap.put("__desc", this.keyName + "@" + this.description);
        for (Entry<String, BaseUnitProperty> entry : this.attributeMap.entrySet()) {
            BaseUnitProperty property = entry.getValue();
            if (property instanceof ListObjectUnitProperty) {
                ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
                objectMap.putAll((Map) listProperty.dump(entry.getKey()));
            } else if (property instanceof ObjectUnitProperty) {
                objectMap.putAll((Map) ((ObjectUnitProperty) property).dump(entry.getKey()));
            } else {
                Map childMap = (Map) property.dump();
                objectMap.putAll(childMap);
            }
        }
        if (objectMap.size() > 1) {
            dumpMap.put(fieldName, objectMap);
        }
        return dumpMap;
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
    protected Object generateChildObject(ObjectUnitProperty property, Class<?> fieldType,
                                         String fieldName, Object referedValue) {
        if (property instanceof ObjectUnitProperty) {
            property.setClassType(fieldType);
            return property.genObject(classLoader);
        } else {
            if (StringUtils.isBlank(String.valueOf(referedValue))) {
                return null;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object generateComplexCollection(BaseUnitProperty property, Class<?> argumentClass,
                                               Class<?> fieldClass, String fieldName,
                                               Object referedValue) {
        Object fieldValue = null;
        if (property.getExpectValue() == null) {
            return null;
        }

        if (property instanceof ListObjectUnitProperty) {
            ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
            listProperty.setClassType(argumentClass);
            return listProperty.genObject(classLoader);
        } else if (property instanceof MapObjectUnitProperty) {
            MapObjectUnitProperty mapProperty = (MapObjectUnitProperty) property;
            mapProperty.setClassType(argumentClass);
            return mapProperty.genObject(classLoader);

        } else if (property instanceof ObjectUnitProperty) {
            //这里为了兼容前端显示，会对单一list列表生成ObjectUnitProperty而非ListObjectUnitProperty，故需要做转化
            ((ObjectUnitProperty) property).setClassType(argumentClass);
            List list = new ArrayList();
            list.add(((ObjectUnitProperty) property).genObject(classLoader));
            return list;
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
                Object valuePart = generateChildObject(null, argumentClass, fieldName,
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
            return fieldValue;
        }

        if (StringUtils.contains(String.valueOf(referedValue), "BigDecimal.csv")) {
            referedValue = "0.01";
        }

        switch (UnitFlagEnum.getByCode(flagCode)) {
            case F:
                return FileUtil.readFile(FileUtil.getRelativePath(String.valueOf(referedValue),
                        this.targetCSVPath));
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

    /**
     * 更新替换字段
     *
     * @param originValue
     * @param columnName
     * @return
     */
    private Object replaceUniqueValue(BaseUnitProperty property, Object originValue,
                                      String columnName) {
        //同上下文动态Map替换
        Map<String, Object> uniqueMap = TestifyCaseContextHolder.get().getUniqueMap();
        property.setOldExpectValue(originValue);
        if (uniqueMap.containsKey(this.keyPath + "." + columnName)) {
            property.setUnique(true);
            originValue = uniqueMap.get(this.keyPath + "." + columnName);
        } else if (uniqueMap.containsKey(columnName)) {
            property.setUnique(true);
            originValue = uniqueMap.get(columnName);
        } else {
            property.setUnique(false);
        }
        property.setExpectValue(originValue);
        return originValue;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void loadCSVFile() {

        try {

            //1. 加载CSV数据
            List tableList = CSVHelper.readFromCsv(this.targetCSVPath);
            if (tableList == null || tableList.size() == 0)
                TestifyLogUtil.fail(log, this.targetCSVPath + "文件内容为空");
            if (tableList.size() < 2) {
                throw new TestifyException("当前的CSV文件内容不全,文件名为" + targetCSVPath);
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
            if (indexCol == -1) {
//                Assert.assertTrue(this.targetCSVPath + "若无法匹配列名，则desc必须为数字，当前为" + this.description,
//                    StringUtils.isNumeric(this.description));
                indexCol = baseIndex + Integer.valueOf(this.description) - 1;
            }

            this.className = ((String[]) tableList.get(1))[classNameCol];
            for (int i = 1; i < tableList.size(); i++) {
                String[] row = (String[]) tableList.get(i);
                String fieldName = row[colNameCol];
                String flagCode = row[flagCol];
                String referedValue = row[indexCol];

                //5.1 CSV加载值中对引号需要过滤处理
                referedValue = StringUtils.replace(referedValue, "\"\"", "\"");
                BaseUnitProperty property = null;
                if (!this.attributeMap.containsKey(fieldName)) {
                    if (referedValue.contains(".csv@")) {
                        if (!referedValue.contains(":")) {
                            //没有：就是list否则是复杂map
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
                                        + fieldName,
                                        this.targetCSVPath, attribute);
                            } else {
                                property = new ListObjectUnitProperty(fieldName, this.keyPath + "."
                                        + fieldName,
                                        this.targetCSVPath, new ArrayList());
                                ListObjectUnitProperty listProperty = (ListObjectUnitProperty) property;
                                for (int j = 0; j < descParts.length; j++) {
                                    Map<String, Object> attribute = new HashMap<String, Object>();
                                    attribute.put("__desc", valueParts[0] + "@" + descParts[j]);
                                    ObjectUnitProperty childProperty = new ObjectUnitProperty(
                                            fieldName, this.keyPath + "." + fieldName + "[" + j + "]",
                                            this.targetCSVPath, attribute);
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
                                        this.keyPath + "." + fieldName + "[" + index + "]",
                                        this.targetCSVPath, attribute);
                                tmpMap.put(key, mapProperty);
                                index++;
                            }
                            property = new MapObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName,
                                    this.targetCSVPath, tmpMap);
                        }
                    } else {
                        if (!referedValue.contains(":")) {
                            property = new BaseUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName, referedValue);
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
                                index++;
                            }
                            property = new MapObjectUnitProperty(fieldName, this.keyPath + "."
                                    + fieldName,
                                    this.targetCSVPath, tmpMap);
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

        } catch (RuntimeException e) {
            // throw for hints
            throw new ModleFileException(this.targetCSVPath, e);
        }

    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ObjectUnitProperty [targetCSVPath=" + targetCSVPath + ", classType=" + classType
                + ", description=" + description + ", attributeMap=" + attributeMap + ", keyName="
                + keyName + ", flagCode=" + flagCode + ", keyPath=" + keyPath + "]";
    }

    /**
     *
     *
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     *
     * @param description value to be assigned to property description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     *
     * @return property value of attributeMap
     */
    public Map<String, BaseUnitProperty> getAttributeMap() {
        return attributeMap;
    }

    /**
     *
     *
     * @return property value of targetCSVPath
     */
    public String getTargetCSVPath() {
        return targetCSVPath;
    }

    /**
     * 
     *
     * @param targetCSVPath value to be assigned to property targetCSVPath
     */
    public void setTargetCSVPath(String targetCSVPath) {
        this.targetCSVPath = targetCSVPath;
    }

    /**
     *
     *
     * @return property value of classType
     */
    public Class<?> getClassType() {
        return classType;
    }

    /**
     * 
     *
     * @param classType value to be assigned to property classType
     */
    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }
}
