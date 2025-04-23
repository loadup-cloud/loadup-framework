/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.object;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.object.processor.ObjectProcessor;
import com.alipay.test.acts.util.FileUtil;
import com.alipay.test.acts.util.JsonUtil;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * 对象操作工具类
 * 
 * @author baishuo.lp
 * @version $Id: ObjectUtil.java, v 0.1 2015年8月19日 下午12:08:51 baishuo.lp Exp $
 */
public class ActsObjectUtil {

    private static final Log logger = LogFactory.getLog(ActsObjectUtil.class);

    /**
     * 两个对象直接比较，支持预期目标为文本模式
     * 默认在mock校验入参中使用
     * 
     * @param exp
     * @param act
     */
    public static void compareObject(Object exp, Object act) {
        if (String.valueOf(exp).contains("csv@")) {
            String[] valueParts = String.valueOf(exp).split("@", 2);
            String csvPath = FileUtil.getRelativePath(valueParts[0], null);
            String cols = valueParts[1];
            if (act instanceof List) {
                String[] descArray = cols.split(";");
                for (String desc : descArray) {
                    ObjectProcessor processor = new ObjectProcessor(csvPath, desc);
                    processor.checkObject(act);
                }
            } else {
                ObjectProcessor processor = new ObjectProcessor(csvPath, cols);
                processor.checkObject(act);
            }
        } else {
            String expString = JsonUtil.toPrettyString(exp);
            String actString = JsonUtil.toPrettyString(act);
            if (!StringUtils.equals(expString, actString)) {
                ActsLogUtil.error(logger, "期望值:\n" + expString + "\n,实际值:\n" + actString);
            }
        }
    }

    /**
     * 简单比较简单对象
     * 
     * @param exp
     * @param act
     */
    public static boolean easyCompare(Object exp, Object act) {
        String expString = JsonUtil.toPrettyString(exp);
        String actString = JsonUtil.toPrettyString(act);
        return StringUtils.equals(expString, actString);
    }

    /*
     * 设置对象属性
     */
    public static void setProperty(Object object, String ognlExpression, Object value) {
        try {
//            OgnlContext ognlContext = new OgnlContext();
//            ognlContext.setMemberAccess(new DefaultMemberAccess(true));
//            Object ognlExprObj = Ognl.parseExpression(ognlExpression);
//            Ognl.setValue(ognlExprObj, ognlContext, object, value);
        } catch (Exception e) {
            ActsLogUtil.fail(logger, "使用Ongl设置变量失败 : " + ognlExpression, e);
        }
    }

    /**
     * 使用Ongl获取对象中属性值
     * @param object 
     * @param ognlExpression 
     * @return
     * @throws OgnlException 
     */
    public static Object getProperty(Object object, String ognlExpression) throws OgnlException {
//        OgnlContext ognlContext = new OgnlContext();
//        ognlContext.setMemberAccess(new DefaultMemberAccess(true));
//        Object ognlExprObj = Ognl.parseExpression(ognlExpression);
//        return Ognl.getValue(ognlExprObj, ognlContext, object);
return null;
    }

    /**
     * 获取class的全部字段（包括父类）
     * 
     * @param objClass    -目标对象的class
     * 
     * @return 字段List
     */
    public static List<Field> getAllFields(Class<?> objClass) {

        List<Field> lis = new ArrayList<Field>();

        Class<?> cls = objClass;

        while (null != cls && !cls.equals(Object.class)) {

            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                lis.add(field);
            }
            cls = cls.getSuperclass();
        }

        return lis;
    }

    /**
     * 获取class指定字段名的字段（父类的字段一样可以获取）
     * 
     * @param objClass    -目标对象的class
     * @param fieldName    -指定字段名
     * 
     * @return 对应字段名的字段，若对应不上，返回null
     */
    public static Field getField(Class<?> objClass, String fieldName) {

        Class<?> cls = objClass;

        while (!cls.equals(Object.class)) {

            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {

                if (StringUtils.equals(field.getName(), fieldName)) {

                    return field;
                }
            }
            cls = cls.getSuperclass();
        }

        return null;
    }

    /**
     * 获取class指定字段名所在的实际class（可能返回父类class）
     * 
     * @param objClass    -目标对象的class
     * @param fieldName    -指定字段名
     * 
     * @return 指定字段名所在的实际class，若对应不上，返回null
     */
    public static Class<?> getClass(Class<?> objClass, String fieldName) {

        Class<?> cls = objClass;

        while (!cls.equals(Object.class)) {

            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {

                if (StringUtils.equals(field.getName(), fieldName)) {

                    return cls;
                }
            }
            cls = cls.getSuperclass();
        }

        return null;
    }

    /** initiate an instance of the objClass
     * int/char/double/float/byte/long/short = 0，boolean = false
     * object/enum = null
     * */
    @SuppressWarnings("unchecked")
    public static Object genInstance(Class<?> objClass) throws Exception {
        Object objValue = null;

        Constructor constructors[] = objClass.getConstructors();
        for (Constructor constructor : constructors) {
            Class[] types = constructor.getParameterTypes();
            Object[] params = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                if (types[i].isPrimitive()) {
                    if (types[i] == boolean.class) {
                        params[i] = false;
                    } else {
                        params[i] = 0;
                    }
                } else if (types[i].isAssignableFrom(List.class)) {
                    params[i] = new ArrayList<Object>();
                } else if (types[i].isAssignableFrom(Map.class)) {
                    params[i] = new HashMap<Object, Object>();
                }
            }
            try {
                objValue = constructor.newInstance(params);
                if (objValue != null)
                    break;
            } catch (InstantiationException e) {
                logger.info("InstantiationException", e);
            } catch (IllegalAccessException e) {
                logger.info("IllegalAccessException", e);
            } catch (IllegalArgumentException e) {
                logger.info("IllegalArgumentException", e);
            } catch (InvocationTargetException e) {
                logger.info("InvocationTargetException", e);
            }
        }
        if (objValue == null) {
            ActsLogUtil.error(logger, "对类【" + objClass.getSimpleName()
                                      + "】创建对象失败，请尝试使用实现类名或实现类Qualified Name填入csv");
            throw new Exception("创建对象失败，请尝试手动创建csv或者给对象添加默认为空的构造函数");
        }

        return objValue;
    }
}
