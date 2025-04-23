package com.alipay.test.acts.mock;

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

import java.lang.reflect.Field;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.mock.model.CacheRecoverModel;
import com.alipay.test.acts.util.DeepCopyUtils;

/**
 * 用于反射修改缓存中的一些共用字段
 * 
 * @author baishuo.lp
 * @version $Id: CacheMockUtil.java, v 0.1 2015年2月8日 下午10:32:14 baishuo.lp Exp $
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CacheMockUtil {
    private static final Log                     logger     = LogFactory.getLog(AtsMockUtil.class);

    public final static Stack<CacheRecoverModel> cacheStack = new Stack<CacheRecoverModel>();

    /**
     * 反射获取非静态私有成员变量
     * 
     * @param object
     * @param fieldName
     * @param needBackup，若为true则备份，可以用recoverAllCache恢复
     * @return
     */
    public static <T> T getCacheData(Object object, Class fieldClass, String fieldName,
                                     boolean needBackup) {
        if (object == null) {
            ActsLogUtil.fail(logger, "静态变量不能采用此方法");
        }
        T fieldObj = null;
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            fieldObj = (T) field.get(object);
            if (needBackup)
                backupObject(field, object, fieldObj);
            if (!isAccessible) {
                field.setAccessible(false);
            }
        } catch (Exception e) {
            ActsLogUtil.fail(logger, object.getClass().getSimpleName() + "反射生成" + fieldName + "出错",
                e);
        }
        return fieldObj;
    }

    /**
     * 反射获取私有静态成员变量
     * 
     * @param fieldClass
     * @param fieldName
     * @param needBackup，若为true则备份，可以用recoverAllCache恢复
     * @return
     */
    public static <T> T getCacheData(Class fieldClass, String fieldName, boolean needBackup) {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            T fieldObj = (T) field.get(null);
            if (needBackup)
                backupObject(field, null, fieldObj);
            if (!isAccessible) {
                field.setAccessible(false);
            }
            return fieldObj;
        } catch (Exception e) {
            ActsLogUtil.fail(logger, fieldClass.getSimpleName() + "反射生成" + fieldName + "出错", e);
            return null;
        }
    }

    /**
     * 反射修改非静态私有成员变量
     * 
     * @param object
     * @param fieldName
     * @param needBackup，若为true则备份，可以用recoverAllCache恢复
     * @return
     */
    public static <T> T setCacheData(Object object, Class fieldClass, String fieldName,
                                     Object targetValue, boolean needBackup) {
        if (object == null) {
            ActsLogUtil.fail(logger, "静态变量不能采用此方法");
            return null;
        }
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            T fieldObj = (T) field.get(object);
            if (needBackup)
                backupObject(field, object, fieldObj);
            field.set(object, targetValue);
            if (!isAccessible) {
                field.setAccessible(false);
            }
            return fieldObj;
        } catch (Exception e) {
            ActsLogUtil.fail(logger, object.getClass().getSimpleName() + "反射生成" + fieldName + "出错",
                e);
            return null;
        }
    }

    /**
     * 反射修改私有静态成员变量
     * 
     * @param fieldClass
     * @param fieldName
     * @param needBackup，若为true则备份，可以用recoverAllCache恢复
     * @return
     */
    public static <T> T setCacheData(Class fieldClass, String fieldName, Object targetValue,
                                     boolean needBackup) {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            boolean isAccessible = field.isAccessible();
            if (!isAccessible) {
                field.setAccessible(true);
            }
            T fieldObj = (T) field.get(null);
            if (needBackup)
                backupObject(field, null, fieldObj);
            field.set(null, targetValue);
            if (!isAccessible) {
                field.setAccessible(false);
            }
            return fieldObj;
        } catch (Exception e) {
            ActsLogUtil.fail(logger, fieldClass.getSimpleName() + "反射生成" + fieldName + "出错", e);
            return null;
        }
    }

    /**
     * 恢复所有被反射调用并修改的缓存
     * 
     * 通常来说，如果修改的是实际从数据库加载的缓存值，也可以用当前工程刷新缓存方法来复原
     */
    public static void recoverAllCache() {
        for (CacheRecoverModel model : cacheStack) {
            Field field = model.getField();
            Object cacheContainer = model.getCacheContainer();
            Object cacheObj = model.getCacheObject();
            try {
                boolean isAccessible = field.isAccessible();
                if (!isAccessible) {
                    field.setAccessible(true);
                }
                field.set(cacheObj, cacheContainer);
                if (!isAccessible) {
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                ActsLogUtil.fail(logger, "恢复对象" + cacheContainer.getClass().getSimpleName() + "的对象"
                                         + cacheObj.getClass().getSimpleName() + "失败", e);
            }
        }
        cacheStack.clear();
    }

    /**
     * 备份对象
     * 
     * @param object
     * @return
     */
    private static void backupObject(Field field, Object object, Object fieldObj) {
        if (fieldObj == null) {
            cacheStack.add(new CacheRecoverModel(field, object, fieldObj));
        } else {
            cacheStack.add(new CacheRecoverModel(field, object, DeepCopyUtils.deepCopy(fieldObj)));
        }
    }
}
