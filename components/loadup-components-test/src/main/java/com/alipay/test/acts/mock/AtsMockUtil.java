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

import com.alipay.test.acts.mock.model.AtsSingleMock;
import com.alipay.test.acts.mock.model.MockCallBack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用于管理AtsSingleMock，由于mock会修改已加载的bean，因此使用任何AtsSingleMock均不支持并发测试
 *
 * @author baishuo.lp
 * @version $Id: AtsMockUtil.java, v 0.1 2015年2月8日 下午10:23:46 baishuo.lp Exp $
 */
public class AtsMockUtil {
    private static final Log logger = LogFactory.getLog(AtsMockUtil.class);

    /**
     * 基于返回值添加mock，若失败则抛出junit Assert异常
     *
     * @param container，容器bundle名+;+bean名
     * @param beanName，需要mock的bean名称
     * @param beanClass，需要mock的bean的类
     * @param methodName，需要mock的方法名
     * @param result,                     可以为返回对象Object，也可以为Throwable, 也可以为MockCallBack对象
     *                                    <p>
     *                                    若为MockCallBack对象，可以参考下述示例，嵌套方法必须与mock方法入参、返回类型一致，并可添加内部实现逻辑
     *                                    若需要校验入参，可以在callback中自行添加校验方式
     *                                    MockCallBack callBack = new MockCallBack() {
     *                                    public String SameMethodName(Object... args) {
     *                                    ...
     *                                    }
     *                                    };
     * @return 返回AtsSingleMock对象，可以用于移除单一mock或简单入参校验
     */
    public static AtsSingleMock addAtsMock(String container, String beanName, Class<?> beanClass,
                                           String methodName, Object result) {

        AtsSingleMock mockObj = new AtsSingleMock(container, beanName, beanClass, methodName);
        if (result instanceof MockCallBack) {
//            Assert.assertTrue("mock添加callback失败", mockObj.addMock((MockCallBack) result));
            return mockObj;
        } else {
            mockObj.addMock(null);
            if (result instanceof Throwable) {
//                Assert.assertTrue("mock添加失败", mockObj.recordMock((Throwable) result));
            } else
//                Assert.assertTrue("mock添加失败", mockObj.recordMock(result));
                return mockObj;
//        }
        }

        /**
         * 校验入参
         *
         * @param mockObj
         * @param expArgs
         */
//    public static void verifyArgs(AtsSingleMock mockObj, List<Object[]> expArgs) {
//        List<Object[]> actArgs = getArgs(mockObj);
//        if (expArgs == null || actArgs == null) {
//            if (expArgs != actArgs) {
//                ActsLogUtil.fail(logger, "调用入参其一为空,期望值" + JsonUtil.toPrettyString(expArgs) + "实际值"
//                                         + JsonUtil.toPrettyString(actArgs));
//            } else
//                return;
//        }
//        if (expArgs.size() != actArgs.size()) {
//            ActsLogUtil.fail(logger, "调用次数不一致，期望个数" + expArgs.size() + "实际个数" + actArgs.size());
//        }
//        for (int i = 0; i < expArgs.size(); i++) {
//            Object[] expArg = expArgs.get(i);
//            Object[] actArg = actArgs.get(i);
//            verifyArgsArray(expArg, actArg);
//        }
//        if (ActsCaseContextHolder.get().getProcessErrorLog().size() > 0) {
//            Assert.fail("mock数据入参校验失败:" + ActsCaseContextHolder.get().getProcessErrorLog());
//        }
//    }

        /**
         * 获取mock入参
         *
         * @param mockObj
         * @return
         */
//    @SuppressWarnings("rawtypes")
//    public static List<Object[]> getArgs(AtsSingleMock mockObj) {
//        String container = CacheMockUtil.getCacheData(mockObj, AtsSingleMock.class, "container",
//            false);
//        String beanName = CacheMockUtil.getCacheData(mockObj, AtsSingleMock.class, "beanName",
//            false);
//        Class beanClass = CacheMockUtil.getCacheData(mockObj, AtsSingleMock.class, "beanClass",
//            false);
//        String methodName = CacheMockUtil.getCacheData(mockObj, AtsSingleMock.class, "methodName",
//            false);
//        Map<String, MockRecoverObject> mockRecovers = CacheMockUtil.getCacheData(MockUtils.class,
//            "mockRecovers", false);
//        Object containerBean = BeanUtil.getBean(container);
//        containerBean = BeanUtil.getTargetBean(containerBean);
//        MockRecoverObject recover = mockRecovers.get(container + "." + beanName);
//        Map<String, MockMethodInterceptor> mockStore = CacheMockUtil.getCacheData(MockProxy.class,
//            "mockStore", false);
//        if (recover == null) {
//            ActsLogUtil.fail(logger, "找不到" + beanName + "的" + methodName + "方法对应mock记录");
//        }
//        MockMethodInterceptor interceptor = mockStore.get(containerBean.toString()
//                                                          + recover.getOrgBean().toString());
//        if (interceptor == null) {
//            ActsLogUtil.fail(logger, "找不到" + beanName + "的" + methodName + "方法对应mock拦截器");
//        }
//        Map<Method, List<Object[]>> invokedArgs = CacheMockUtil.getCacheData(interceptor,
//            MockMethodInterceptor.class, "invokedArgs", false);
//
//        for (Map.Entry<Method, List<Object[]>> entry : invokedArgs.entrySet()) {
//            if (StringUtils.equals(entry.getKey().getName(), methodName)
//                && StringUtils.equals(beanClass.getName(), entry.getKey().getDeclaringClass()
//                    .getName())) {
//                return entry.getValue();
//            }
//        }
//        return null;
//    }

        /**
         * 清理调用入参记录
         *
         * @param mockObj
         */
//    public static void clearRecords(AtsSingleMock mockObj) {
//        List<Object[]> args = getArgs(mockObj);
//        args.clear();
//    }

        /**
         * 释放所有mock
         */
//    public static void releaseAllMock() {
//        Map<String, MockRecoverObject> mockRecovers = CacheMockUtil.getCacheData(MockUtils.class,
//            "mockRecovers", false);
//        try {
//            for (Map.Entry<String, MockRecoverObject> entry : mockRecovers.entrySet()) {
//                String key = entry.getKey();
//                String containerName = key.substring(0, key.lastIndexOf("."));
//                String beanName = key.substring(key.lastIndexOf(".") + 1);
//                Object container = BeanUtil.getBean(containerName);
//                container = BeanUtil.getTargetBean(container);
//                Object target = entry.getValue().getOrgBean();
//                MockProxy.removeAllMock(container, target);
//                BeanUtilsBean.getInstance().setProperty(container, beanName, target);
//            }
//        } catch (Exception e) {
//            ActsLogUtil.error(logger, "mock恢复未知异常", e);
//        } finally {
//            mockRecovers.clear();
//        }
//    }

        //私有方法

        /**
         * 校验对象数组
         *
         * @param expArg
         * @param actArg
         * @return
         */
//    private static void verifyArgsArray(Object[] expArg, Object[] actArg) {
//        if (expArg == null || actArg == null) {
//            ActsLogUtil.error(logger, "调用入参其一为空,期望值" + JsonUtil.toPrettyString(expArg) + "实际值"
//                                      + JsonUtil.toPrettyString(actArg));
//        }
//        if (expArg.length != actArg.length) {
//            ActsLogUtil.error(logger, "参数个数不一致，期望个数" + expArg.length + "实际个数" + actArg.length);
//        }
//        for (int i = 0; i < expArg.length; i++) {
//            ActsObjectUtil.compareObject(expArg[i], actArg[i]);
//        }
//    }
return null;
    }
}
