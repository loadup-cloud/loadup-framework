/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component;

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

import java.util.Map;



import com.alipay.test.acts.component.ccil.handler.CcilExecutor;
import com.alipay.test.acts.component.enums.RepositoryType;
import com.alipay.test.acts.manage.TestLogger;
import com.alipay.test.acts.manage.TestProfiler;
import com.alipay.test.acts.support.TestContext;
import org.apache.commons.lang3.StringUtils;

/**
 * 组件调用器。
 * <p>
 * 支持用例组件调用语言ccil调用。
 * 
 * @author dasong.jds
 * @version $Id: CCInvoker.java, v 0.1 2015年5月22日 下午9:24:08 dasong.jds Exp $
 */
public class CCInvoker {

    /** 用例库工厂 */
    private final CCRepositoryFactory repositoryFactory;

    /** 调用上下文 */
    private final InvokeContext       invokeContext;

    /**
     * 构造invoker对象。
     * 
     * @param repositoryFactory     仓储
     * @param testContext           测试上下文
     */
    public CCInvoker(CCRepositoryFactory repositoryFactory, TestContext testContext) {
        super();
        this.repositoryFactory = repositoryFactory;
        this.invokeContext = new InvokeContext(testContext, this);
    }

    /**
     * 暂时支持的格式：<br>
     * ccprepare:cc-id?param1=value1& param2='v21 v22' & param3=${attribute}
     * <br>或<br>
     * result1=ccprepare:cc-id?param1=value1 & param2=value2
     * <p>
     * 第一种表示执行cc-id用例准备组件，并传入param1、param2、param3三个参数，参数可引用上下文中的属性<br>
     * 第二种表示准备组件执行后将结果放入测试上下文，并以result1作为key。
     * <p>
     * 多条语句以分号分割。
     * 
     * @param ccil              执行语句
     * @param params            执行参数，本次调用有效
     * @return                  执行结果
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(String ccil, Map<String, Object> params) {

        if (StringUtils.isBlank(ccil)) {
            return null;
        }
        TestProfiler.enter("[ccil]", ccil);
        invokeContext.push();

        try {
            handleParams(params);

            Object result = new CcilExecutor().execute(invokeContext, ccil);

            TestLogger.getLogger().info(buildInvokeLog(ccil, params, result));

            return (T) result;
        } finally {
            invokeContext.pop();
            TestProfiler.release();
        }
    }

    /**
     * 组件调用。
     * 
     * @param ctx       测试上下文
     * @param ccil      调用语句
     * @return          调用结果
     * 
     * @see #invoke(TestContext, String, Map)
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(String ccil) {
        Object result = this.invoke(ccil, null);

        return (T) result;
    }

    /**
     * 获取组件。
     * 
     * @param type          组件类型
     * @param componentId   组件ID
     * @return              组件
     */
    public CCComponent getComponent(RepositoryType type, String componentId) {

        return repositoryFactory.getRepository(type).getComponent(componentId);
    }

    /**
     * 获取范围内变量。
     * 
     * @return
     */
    public Map<String, Object> getAvailableVariables() {

        return invokeContext.getAvailableVariables();
    }

    /**
     * 获取范围内变量。
     * 
     * @return
     */
    public Object getAvailableVariable(String name) {

        return invokeContext.getAvailableVariable(name);
    }

    /**
     * 处理参数信息。
     * <p>
     * 传入参数压入当前scope。
     * 
     * @param params            
     */
    private void handleParams(Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                invokeContext.setVariable(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 构建调用信息。
     * 
     * @param ccil    
     * @param result     
     */
    private String buildInvokeLog(String ccil, Map<String, Object> params, Object result) {
        StringBuilder msg = new StringBuilder("cc invoke[ccil=");
        msg.append(ccil);
        msg.append(",params=");
        msg.append(params);
        msg.append(",result=");
        msg.append(result);
        msg.append("]");
        return msg.toString();
    }

}
