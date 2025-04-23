package com.alipay.test.acts.api;

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

import java.util.List;

import com.alipay.test.acts.mock.AtsMockUtil;
import com.alipay.test.acts.mock.CacheMockUtil;
import com.alipay.test.acts.mock.model.AtsSingleMock;

/**
 * Mock入口APIs
 * 
 * @author baishuo.lp
 * @version $Id: MockApis.java, v 0.1 2015年3月7日 下午12:41:49 baishuo.lp Exp $
 */
@SuppressWarnings("rawtypes")
public class MockApis {

    /**
     * 基于返回值添加mock，若失败则抛出junit Assert异常
     * 
     * 
     * @param container，容器bundle名+;+bean名
     * @param beanName，需要mock的bean名称
     * @param beanClass，需要mock的bean的类
     * @param methodName，需要mock的方法名
     * @param result, 可以为返回对象Object，也可以为Throwable, 也可以为MockCallBack对象
     * 
     * 若为MockCallBack对象，可以参考下述示例，嵌套方法必须与mock方法入参、返回类型一致，并可添加内部实现逻辑
     * 若需要校验入参，可以在callback中自行添加校验方式
     * MockCallBack callBack = new MockCallBack() {
                public String SameMethodName(Object... args) {
                    ...
                }
            };
     * 
     * @return 返回AtsSingleMock对象，可以用于移除单一mock或简单入参校验
     */
    public static AtsSingleMock addAtsMock(String container, String beanName, Class<?> beanClass,
                                           String methodName, Object result) {
        return AtsMockUtil.addAtsMock(container, beanName, beanClass, methodName, result);
    }

    /**
     * 校验mock调用入参
     * 
     * @param mockObj, mock对象
     * @param expArgs，待校验入参，入参校验单个对象支持xxxxx.csv@1格式来用csv校验，支持相对路径
     */
    public static void verifyAtsMockArgs(AtsSingleMock mockObj, List<Object[]> expArgs) {
//        AtsMockUtil.verifyArgs(mockObj, expArgs);
    }

    /**
     * 获取mock调用入参对象序列
     * 
     * @param mockObj, mock对象
     */
    public static List<Object[]> getAtsMockArgs(AtsSingleMock mockObj) {
        return null;//AtsMockUtil.getArgs(mockObj);
    }

    /**
     * 清理mock调用记录
     * 
     * @param mockObj, mock对象
     */
    public static void clearAtsMockRecords(AtsSingleMock mockObj) {
//        AtsMockUtil.clearRecords(mockObj);
    }

    /**
     * 释放所有mock
     */
    public static void releaseAtsMock() {
//        AtsMockUtil.releaseAllMock();
    }

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
        return CacheMockUtil.getCacheData(object, fieldClass, fieldName, needBackup);
    }

    /**
     * 反射获取私有静态成员变量，不做备份，不能恢复
     * 
     * @param fieldClass
     * @param fieldName
     * @param needBackup，若为true则备份，可以用recoverAllCache恢复
     * @return
     */

    public static <T> T getCacheData(Class fieldClass, String fieldName, boolean needBackup) {
        return CacheMockUtil.getCacheData(fieldClass, fieldName, needBackup);
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
        return CacheMockUtil.setCacheData(object, fieldClass, fieldName, targetValue, needBackup);
    }

    /**
     * 反射修改私有静态成员变量，不做备份，不能恢复
     * 
     * @param fieldClass
     * @param fieldName
     * @param needBackup，若为true则备份，可以用recoverAllCache恢复
     * @return
     */
    public static <T> T setCacheData(Class fieldClass, String fieldName, Object targetValue,
                                     boolean needBackup) {
        return CacheMockUtil.setCacheData(fieldClass, fieldName, targetValue, needBackup);
    }

    /**
     * 恢复所有被反射调用并修改的缓存
     * 
     * 通常来说，如果修改的是实际从数据库加载的缓存值，也可以用当前工程刷新缓存方法来复原
     */
    public static void recoverAllCache() {
        CacheMockUtil.recoverAllCache();
    }
}
