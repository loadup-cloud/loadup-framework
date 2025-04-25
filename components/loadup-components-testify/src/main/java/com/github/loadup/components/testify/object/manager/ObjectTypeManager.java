package com.github.loadup.components.testify.object.manager;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.generator.ObjectGenerator;
import com.github.loadup.components.testify.object.generator.impl.ArrayListTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.ArrayTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.BigDecimalTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.CurrencyTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.DateTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.EnumTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.ListTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.MapTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.SetTypeGenerator;
import com.github.loadup.components.testify.object.generator.impl.StringTypeGenerator;
import com.github.loadup.components.testify.util.CSVApisUtil;

/**
 * 对象类型总控器类
 *
 * @author guangming.zhong
 *
 */
@Slf4j
public class ObjectTypeManager {


    private final Map<String, ObjectGenerator> objectHandlers = new HashMap<String, ObjectGenerator>();

    private final Set<String> simpleHandlerSet = new HashSet<String>();

    private final List<String> simpleClassType = new ArrayList<String>();

    public ObjectTypeManager() {

        objectHandlers.put("enum", new EnumTypeGenerator());
        objectHandlers.put(String.class.getName(), new StringTypeGenerator());
        objectHandlers.put(java.util.Date.class.getName(), new DateTypeGenerator());
        objectHandlers.put(List.class.getName(), new ListTypeGenerator());
        objectHandlers.put(ArrayList.class.getName(), new ArrayListTypeGenerator());
        objectHandlers.put(Map.class.getName(), new MapTypeGenerator());
        objectHandlers.put(HashMap.class.getName(), new MapTypeGenerator());
        objectHandlers.put(Set.class.getName(), new SetTypeGenerator());
        objectHandlers.put(HashSet.class.getName(), new SetTypeGenerator());
        objectHandlers.put("ARRAY", new ArrayTypeGenerator());
        objectHandlers.put(java.util.Currency.class.getName(), new CurrencyTypeGenerator());
        objectHandlers.put(java.math.BigDecimal.class.getName(), new BigDecimalTypeGenerator());

        for (Entry<String, ObjectGenerator> entry : objectHandlers.entrySet()) {
            if (entry.getValue().isSimpleType()) {
                simpleHandlerSet.add(entry.getKey());
            }
        }

        simpleClassType.add("java.lang.Integer");
        simpleClassType.add("java.lang.Float");
        simpleClassType.add("java.lang.Double");
        simpleClassType.add("java.lang.Long");
        simpleClassType.add("java.lang.Short");
        simpleClassType.add("java.lang.Byte");
        simpleClassType.add("java.lang.Boolean");
        simpleClassType.add("java.lang.Character");
        simpleClassType.add("java.util.Properties");
    }

    /*
     * 对象是否为基本类型
     */
    public boolean isSimpleType(Class<?> clz) {
        boolean result = false;

        String simpleKey = null;
        if (clz.isEnum() || clz.getName().toLowerCase().contains("enum")) {
            simpleKey = "enum";
        } else {
            simpleKey = clz.getName();
        }

        if (clz.isPrimitive() || simpleHandlerSet.contains(simpleKey)
                || simpleClassType.contains(simpleKey)) {
            result = true;
        }
        return result;
    }

    /*
     * 获取简单类型的对象
     */
    public Object getSimpleObject(Class<?> clz, String value, String fieldName, String className) {
        ObjectGenerator handler = null;

        if (clz.isPrimitive() || simpleClassType.contains(className)) {
            return getPrimitiveObject(value, className);
        }

        if (clz.isEnum()) {
            handler = objectHandlers.get("enum");
        } else {
            handler = objectHandlers.get(clz.getName());
        }

        Object result = null;
        if (handler != null) {
            result = handler.generateFieldObject(clz, fieldName, value);
        }
        return result;
    }

    /*
     * 获取简单对象的String值
     */
    public String getSimpleObjValue(Class<?> clz, Object obj, String fieldName) {
        ObjectGenerator handler = null;

        if (clz.isPrimitive() || simpleClassType.contains(clz.getName())) {
            return String.valueOf(obj);
        }

        if (clz.isEnum()) {
            handler = objectHandlers.get("enum");
        } else {
            handler = objectHandlers.get(clz.getName());
        }

        String result = null;
        if (handler != null) {
            result = handler.generateObjectValue(obj, null, true);
        }
        return result;
    }

    /*
     * 返回java内置基本类型的对象
     */
    protected Object getPrimitiveObject(String value, String primitiveType) {

        Object result = null;

        if (StringUtils.equals("int", primitiveType)
                || StringUtils.equals("java.lang.Integer", primitiveType)) {
            result = Integer.parseInt(value);
        } else if (StringUtils.equals("float", primitiveType)
                || StringUtils.equals("java.lang.Float", primitiveType)) {
            result = Float.parseFloat(value);
        } else if (StringUtils.equals("double", primitiveType)
                || StringUtils.equals("java.lang.Double", primitiveType)) {
            result = Double.parseDouble(value);
        } else if (StringUtils.equals("long", primitiveType)
                || StringUtils.equals("java.lang.Long", primitiveType)) {
            result = Long.parseLong(value);
        } else if (StringUtils.equals("short", primitiveType)
                || StringUtils.equals("java.lang.Short", primitiveType)) {
            result = Short.parseShort(value);
        } else if (StringUtils.equals("byte", primitiveType)
                || StringUtils.equals("java.lang.Byte", primitiveType)) {
            result = Byte.parseByte(value);
        } else if (StringUtils.equals("boolean", primitiveType)
                || StringUtils.equals("java.lang.Boolean", primitiveType)) {
            result = Boolean.parseBoolean(value);
        } else if (StringUtils.equals("char", primitiveType)
                || StringUtils.equals("java.lang.Character", primitiveType)) {
            result = value.charAt(0);
        } else {
            TestifyLogUtil.error(log, "基本类型解析失败，返回null值");
        }

        return result;
    }

    /*
     * 对象是否为集合类型
     */
    public boolean isCollectionType(Class<?> clz) {
        boolean result = false;

        String collectionKey = null;
        if (clz.isArray()) {
            collectionKey = "ARRAY";
        } else {
            collectionKey = clz.getName();
        }

        if (!simpleHandlerSet.contains(collectionKey) && objectHandlers.containsKey(collectionKey)) {
            result = true;
        }
        return result;
    }

    /*
     * 获取集合对象组件的class
     */
    public Class<?> getCollectionItemClass(Type genericType, Class<?> clz) {
        //当前cls类型
        ObjectGenerator generator = getObjectGenerator(clz);

        Class<?> result = null;
        // result = generator.getItemClass(genericType, clz);
        if (generator != null) {
            if (Map.class.isAssignableFrom(clz)) {
                result = CSVApisUtil.getClass(genericType, 1);

            } else {
                result = CSVApisUtil.getClass(genericType, 0);

            }
        }
        return result;
    }

    /*
     * 获取集合对象
     */
    public Object getCollectionObject(Class<?> clz) {
        ObjectGenerator generator = getObjectGenerator(clz);

        Object result = null;
        if (generator != null) {
            result = generator.generateFieldObject(clz, null, null);
        }
        return result;
    }

    /*
     * 设置集合组件的值
     */
    public void setCollectionObjectValue(Object collectionObject, Object value,
                                         String originalValue, int index, Class<?> clz) {
        ObjectGenerator generator = getObjectGenerator(clz);

        if (generator != null) {
            generator.setObjectValue(collectionObject, value, originalValue, index);
        }
    }

    /*
     * 获取集合转化的string
     */
    public String getCollectionObjectString(Class<?> clz, Object collectionObject,
                                            boolean isSimple, String csvPath) {

        ObjectGenerator generator = getObjectGenerator(clz);

        String result = null;
        if (generator != null) {
            result = generator.generateObjectValue(collectionObject, csvPath, isSimple);
        }
        return result;
    }

    /*
     * 获取集合处理器
     */
    private ObjectGenerator getObjectGenerator(Class<?> clz) {
        ObjectGenerator handler = null;
        if (clz.isArray()) {
            handler = objectHandlers.get("ARRAY");
        } else {
            handler = objectHandlers.get(clz.getName());
        }
        return handler;
    }
}
