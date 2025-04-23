package com.alipay.test.acts.mock.processor;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.test.acts.mock.enums.MockStatus;
import com.alipay.test.acts.mock.model.MockCallBack;
import com.alipay.test.acts.mock.model.MockRecoverObject;
import com.alipay.test.acts.util.BeanUtil;
import com.alipay.test.acts.util.BeanUtilsBean;

/**
 * MockUtil
 * 
 * @author midang
 * @version $Id: MockUtil.java,v 0.1 2010-11-16 下午07:51:35 midang Exp $
 */
public class MockUtils {

    private static final Logger                   logger       = LoggerFactory
                                                                   .getLogger(MockUtils.class);

    private static Map<String, MockRecoverObject> mockRecovers = new HashMap<String, MockRecoverObject>();

    private static Map<String, List<String>>      errorMsgs    = new HashMap<String, List<String>>();

    /**
     * 添加mock
     * 
     * @param container 容器名
     * @param beanName  期望mock的bean名
     * @param beanClass 期望mock的bean的class,如果beanName继承了一个接口,这里可以写上接口的class
     * @param methodNames 期望mock的方法名，至少一个，否则返回失败
     * @return
     */
    public static synchronized boolean addMock(String container, String beanName,
                                               Class<?> beanClass, String... methodNames) {

        if (StringUtils.isBlank(container) || StringUtils.isBlank(beanName) || beanClass == null
            || methodNames == null || methodNames.length < 1) {
            return false;
        }

        for (String methodName : methodNames) {
            if (!addMock(container, beanName, beanClass, methodName, null))
                return false;
        }

        return true;
    }

    /**
     * mock指定方法
     * 
     * @param container
     * @param beanName
     * @param beanClass
     * @param methodName
     * @param callback
     * @return
     */
    public static boolean addMock(String container, String beanName, Class<?> beanClass,
                                  String methodName, MockCallBack callback) {
        if (StringUtils.isBlank(container) || StringUtils.isBlank(beanName) || beanClass == null
            || StringUtils.isBlank(methodName)) {
            return false;
        }

        // 容器bean
        Object containerBean = BeanUtil.getBean(container);
        containerBean = BeanUtil.getTargetBean(containerBean);
        MockRecoverObject recover = mockRecovers.get(genKey(container, beanName));
        // 1.已经替换了对应容器的field，只需增加方法级mock
        if (recover != null) {
            MockProxy.addMock(containerBean, recover.getOrgBean(), methodName, callback);
            return true;
        }
        // 2.空白，需替换对应容器的field
        try {
            // 目标原始bean
            Object orgBean = new BeanUtilsBean().getProperty(containerBean, beanName);
            // 根据目标bean生成的proxy
            Object proxy = MockProxy.getProxy(containerBean, orgBean, beanClass);
            // 将目标原始bean替换为proxy
            new BeanUtilsBean().setProperty(containerBean, beanName, proxy);
            MockProxy.addMock(containerBean, orgBean, methodName, callback);
            // 保存原始bean和proxy，恢复时需要用到
            recover = new MockRecoverObject(orgBean, proxy);
            mockRecovers.put(genKey(container, beanName), recover);
        } catch (Exception ex) {
            logger.error("", ex);
            return false;
        }

        return true;
    }

    /**
     * 录制返回值
     * 
     * @param container 容器名
     * @param beanName  期望mock的bean名
     * @param methodName 期望mock的方法名
     * @param returnObject 返回值
     * @return
     */
    public static boolean recordMock(String container, String beanName, String methodName,
                                     Object returnObject) {

        // 容器bean
        Object containerBean = BeanUtil.getBean(container);
        containerBean = BeanUtil.getTargetBean(containerBean);
        MockRecoverObject recover = mockRecovers.get(genKey(container, beanName));
        // 未mock
        if (recover == null) {
            return false;
        }
        MockProxy.recordMock(containerBean, recover.getOrgBean(), methodName, returnObject, null);
        return true;
    }

    /**
     * 录制异常
     * 
     * @param container 容器名
     * @param beanName  期望mock的bean名
     * @param methodName 期望mock的方法名
     * @param e 异常
     * @return
     */
    public static boolean recordMock(String container, String beanName, String methodName,
                                     Throwable e) {
        // 容器bean
        Object containerBean = BeanUtil.getBean(container);
        containerBean = BeanUtil.getTargetBean(containerBean);
        MockRecoverObject recover = mockRecovers.get(genKey(container, beanName));
        // 未mock
        if (recover == null) {
            return false;
        }
        MockProxy.recordMock(containerBean, recover.getOrgBean(), methodName, null, e);
        return true;
    }

    /**
     * 删除mock，恢复原始功能
     * 
     * @param container 容器名
     * @param beanName  期望删除mock的bean名
     * @param methodNames 期望删除mock的方法名，若不指定方法名，则删除该bean下全部方法的mock
     * @return
     */
    public static synchronized boolean removeMock(String container, String beanName,
                                                  String... methodNames) {
        if (StringUtils.isBlank(container) || StringUtils.isBlank(beanName)) {
            return false;
        }
        // 容器bean
        Object containerBean = BeanUtil.getBean(container);
        containerBean = BeanUtil.getTargetBean(containerBean);
        MockRecoverObject recover = mockRecovers.get(genKey(container, beanName));
        // 未mock
        if (recover == null) {
            return true;
        }
        // 若移除全部方法，则将原始bean还原
        if (methodNames == null || methodNames.length < 1) {
            MockProxy.removeAllMock(containerBean, recover.getOrgBean());
            try {
                new BeanUtilsBean().setProperty(containerBean, beanName, recover.getOrgBean());
                mockRecovers.remove(genKey(container, beanName));
            } catch (Exception e) {
                logger.error("", e);
                return false;
            }
            return true;
        }
        for (String methodName : methodNames) {
            if (MockStatus.CAN_REMOVE == MockProxy.removeMock(containerBean, recover.getOrgBean(),
                methodName)) {
                // 若已移除全部方法，则将原始bean还原
                try {
                    new BeanUtilsBean().setProperty(containerBean, beanName, recover.getOrgBean());
                    mockRecovers.remove(genKey(container, beanName));
                } catch (Exception e) {
                    logger.error("", e);
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    public static synchronized void removeAllMock() {
        try {
            for (Map.Entry<String, MockRecoverObject> entry : mockRecovers.entrySet()) {
                String[] beans = entry.getKey().split("\\.");
                if (beans == null || beans.length != 2) {
                    continue;
                }
                Object container = BeanUtil.getBean(beans[0]);
                container = BeanUtil.getTargetBean(container);
                Object target = entry.getValue().getOrgBean();
                new BeanUtilsBean().setProperty(container, beans[1], target);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            mockRecovers.clear();
        }
    }

    /**
     * 检查mock入参
     * 
     * @param container
     * @param beanName
     * @param methodName
     * @param expArgs
     * @return
     */
    public static boolean argsCheck(String container, String beanName, String methodName,
                                    Object... expArgs) {

        List<String> errs = errorMsgs.get(container + beanName + methodName);
        if (errs != null) {
            errorMsgs.remove(container + beanName + methodName);
            errs.clear();
        }
        return argsCheck(container, beanName, methodName, expArgs, 0);
    }

    /**
     * 检查mock入参,接受参数列表，但必须按序
     * 
     * @param container
     * @param beanName
     * @param methodName
     * @param expArgs 接受参数列表，但必须按序
     * @return
     */
    public static boolean argsCheck(String container, String beanName, String methodName,
                                    List<Object[]> expArgs) {

        List<String> errs = errorMsgs.get(container + beanName + methodName);
        if (errs != null) {
            errorMsgs.remove(container + beanName + methodName);
            errs.clear();
        }
        errs = new ArrayList<String>();
        errorMsgs.put(container + beanName + methodName, errs);

        if (expArgs == null) {
            String errMsg = container + beanName + methodName + "  期望参数为null.";
            errs.add(errMsg);
            logger.error(errMsg);
            return false;
        }
        int err = 0;
        for (int i = 0; i < expArgs.size(); i++) {
            if (!argsCheck(container, beanName, methodName, expArgs.get(i), 0)) {
                err++;
            }
        }
        return err == 0;
    }

    private static boolean argsCheck(String container, String beanName, String methodName,
                                     Object[] expArgs, int actIndex) {

        List<String> errs = errorMsgs.get(container + beanName + methodName);
        if (errs == null) {
            errs = new ArrayList<String>();
            errorMsgs.put(container + beanName + methodName, errs);
        }

        Object containerBean = BeanUtil.getBean(container);
        containerBean = BeanUtil.getTargetBean(containerBean);
        MockRecoverObject recover = mockRecovers.get(genKey(container, beanName));
        if (recover == null) {
            String err = container + beanName + methodName + " 未mock.";
            errs.add(err);
            logger.error(err);
            return false;
        }
        Object[] actArgs = MockProxy.getInovkeArg(containerBean, recover.getOrgBean(), methodName,
            actIndex);
        if (actArgs == null) {
            String err = container + beanName + methodName + " 入参不存在，mock未调用？";
            errs.add(err);
            logger.error(err);
            return false;
        }
        boolean ret = argsCheck(expArgs, actArgs);
        if (!ret) {
            String err = container + beanName + methodName + " mock入参检查错误："
                         + genErrorMsg(expArgs, actArgs) + "\n";
            logger.error(err);
            errs.add(err);
        }
        return ret;
    }

    /**
     * 获取mock入参检验失败的错误消息
     * 
     * @param container
     * @param beanName
     * @param methodName
     * @return
     */
    public static String getArgsCheckErrMsg(String container, String beanName, String methodName) {
        List<String> errs = errorMsgs.get(container + beanName + methodName);
        if (errs == null)
            return null;
        String ret = "";
        for (String err : errs) {
            ret = ret.concat(err).concat("\n");
        }
        return ret;
    }

    private static String genErrorMsg(Object[] expArgs, Object[] actArgs) {
        String expArg = "";
        if (expArgs == null) {
            expArg = expArg.concat("null");
        } else {
            for (Object arg : expArgs) {
                if (StringUtils.isNotEmpty(expArg))
                    expArg = expArg.concat(",");
                expArg = expArg.concat(String.valueOf(arg));
            }
        }

        String actArg = "";
        if (actArgs == null) {
            actArg = actArg.concat("null");
        } else {
            for (Object arg : actArgs) {
                if (StringUtils.isNotEmpty(actArg))
                    actArg = actArg.concat(",");
                actArg = actArg.concat(String.valueOf(arg));
            }
        }

        return "期望值 " + expArg + "; 实际值 " + actArg;

    }

    private static boolean argsCheck(Object[] expArgs, Object[] actArgs) {

        if (expArgs == null || actArgs == null) {
            return expArgs == null && actArgs == null;
        }

        if (expArgs.length != actArgs.length) {
            return false;
        }
        int err = 0;
        for (int i = 0; i < expArgs.length; i++) {
            if (!StringUtils.equals(String.valueOf(expArgs[i]), String.valueOf(actArgs[i]))) {
                err++;
            }
        }
        return err == 0;
    }

    private static String genKey(String container, String targetBean) {
        return container + "." + targetBean;
    }
}
