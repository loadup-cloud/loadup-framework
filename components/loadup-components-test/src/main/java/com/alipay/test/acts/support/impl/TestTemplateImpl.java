/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.support.impl;

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


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.testng.TestException;

import com.alipay.test.acts.component.CCInvoker;
import com.alipay.test.acts.component.CCRepositoryFactory;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.manage.TestLogger;
import com.alipay.test.acts.manage.TestProfiler;
import com.alipay.test.acts.manage.TestRecorder;
import com.alipay.test.acts.support.SofaRuntimeReference;
import com.alipay.test.acts.support.TestAction;
import com.alipay.test.acts.support.TestCallback;
import com.alipay.test.acts.support.TestCase;
import com.alipay.test.acts.support.TestCaseFactory;
import com.alipay.test.acts.support.TestCaseHolder;
import com.alipay.test.acts.support.TestContext;
import com.alipay.test.acts.support.TestTemplate;
import com.alipay.test.acts.support.enums.ActionType;

/**
 * 测试模版实现类。
 * <p>
 * 具备执行过程记录能力。
 * 
 * @author dasong.jds
 * @version $Id: TestTemplateImpl.java, v 0.1 2015年5月10日 下午10:36:22 dasong.jds Exp $
 */
public class TestTemplateImpl implements TestTemplate, ApplicationContextAware {

    /** 用例库工厂 */
    private CCRepositoryFactory ccRepositoryFactory;

    // ~~~ 公共方法
    /** 
     * @see com.ipay.itest.common.support.TestTemplate#execute(com.ipay.itest.common.support.TestCallback)
     */
    @Override
    public <R, T> void execute(TestCallback<R, T> testCallback) {

        TestContext ctx = new TestContext();

        CCInvoker ccInvoker = new CCInvoker(ccRepositoryFactory, ctx);
        ctx.setCcInvoker(ccInvoker);

        TestProfiler.start("[+]");
        R request = null;
        T result = null;
        boolean success = false;
        try {
            TestLogger.getLogger().info("[test begin]");

            TestCase tc = caseDescribe(testCallback);
            TestCaseHolder.set(tc);

            if (!tc.getOption().isRun()) {
                TestLogger.getLogger().info("testCase not run for option[isRun=false]");
                return;
            }

            request = prepare(ctx, testCallback);

            result = execute(ctx, request, testCallback);

            check(ctx, request, result, testCallback);

        } catch (ActsException e) {
            TestLogger.getLogger().error(e, "test error[context=", ctx, "]");
            throw e;
        } finally {
            try {
                clear(ctx, request, result, testCallback);
                success = true;
            } catch (TestException e) {
                TestLogger.getLogger().error(e, "clear error[context=", ctx, "]");
                throw e;
            } finally {
                TestLogger.getLogger().info("[test end]");
                TestProfiler.release();

                TestProfiler.profile(success);
                TestRecorder.record();

                TestCaseHolder.clear();
            }
        }
    }

    /** 
     * @see com.ipay.itest.common.support.TestTemplate#executeByCcil(String, String, String, String, String)
     */
    @Override
    public void executeByCcil(final String caseExpr, final String ccilPrepare,
                              final String ccilExecute, final String ccilCheck,
                              final String ccilClear) {

        execute(new TestCallback<Object, Object>() {

            @Override
            public String caseDescribe() {
                return caseExpr;
            }

            @Override
            public Object prepare(TestContext ctx) {
                return ctx.getCcInvoker().invoke(ccilPrepare);
            }

            @Override
            public Object execute(TestContext ctx, Object request) {
                ctx.getCcInvoker().invoke(ccilExecute);
                return null;
            }

            @Override
            public void check(TestContext ctx, Object request, Object result) {
                ctx.getCcInvoker().invoke(ccilCheck);
            }

            @Override
            public void clear(TestContext ctx, Object request, Object result) {
                ctx.getCcInvoker().invoke(ccilClear);
            }
        });

    }

    // ~~~ 私有方法
    /**
     * 用例描述。
     * 
     * @param testCallback
     */
    private <R, T> TestCase caseDescribe(TestCallback<R, T> testCallback) {
        TestProfiler.enter("[case describe]");
        try {
            TestLogger.getLogger().info("[case describe]");
            String caseDesc = testCallback.caseDescribe();
            if (StringUtils.isEmpty(caseDesc)) {
                throw new ActsException("case describe error,case is null");
            }
            TestCase tc = TestCaseFactory.create(caseDesc);
            printCaseDesc(tc);
            return tc;
        } finally {
            TestProfiler.release();
        }
    }

    /**
     * 用例准备。
     * 
     * @param ctx
     * @param ccInvoker
     * @param testCallback
     * @return
     */
    private <R, T> R prepare(TestContext ctx, TestCallback<R, T> testCallback) {
        R request;
        TestProfiler.enter("[prepare phase]");
        try {
            TestLogger.getLogger().info("[prepare phase]");
            request = testCallback.prepare(ctx);
            doPrepareAction(ctx);
            TestLogger.getLogger().info("test prepare[request=", request, "]");
        } finally {
            TestProfiler.release();
        }
        return request;
    }

    /**
     * 用例执行。
     * 
     * @param ctx
     * @param request
     * @param testCallback
     * @return
     */
    private <R, T> T execute(TestContext ctx, R request, TestCallback<R, T> testCallback) {
        T result;
        TestProfiler.enter("[execute phase]");
        try {
            TestLogger.getLogger().info("[execute phase]");
            result = testCallback.execute(ctx, request);
            doExecuteAction(ctx);
            TestLogger.getLogger().info("test execute[result=", result, "]");
        } finally {
            TestProfiler.release();
        }
        return result;
    }

    /**
     * 用例检查。
     * 
     * @param ctx
     * @param request
     * @param result
     * @param testCallback
     */
    private <R, T> void check(TestContext ctx, R request, T result, TestCallback<R, T> testCallback) {
        TestProfiler.enter("[check phase]");
        try {
            TestLogger.getLogger().info("[check phase]");
            testCallback.check(ctx, request, result);
            doCheckAction(ctx);
        } finally {
            TestProfiler.release();
        }
    }

    /**
     * 用例清理。
     * 
     * @param ctx
     * @param request
     * @param result
     * @param testCallback
     */
    private <R, T> void clear(TestContext ctx, R request, T result, TestCallback<R, T> testCallback) {
        TestProfiler.enter("[clear phase]");
        try {
            TestLogger.getLogger().info("[clear phase]");
            testCallback.clear(ctx, request, result);
            doClearAction(ctx);
        } finally {
            TestProfiler.release();
        }
    }

    /**
     * 执行准备操作。
     * 
     * @param ctx
     */
    private void doPrepareAction(TestContext ctx) {
        List<TestAction> actions = ctx.getActionManager().getActions(ActionType.PREPARE);
        for (TestAction action : actions) {
            action.doAction(ctx);
        }
    }

    /**
     * 执行执行动作。
     * 
     * @param ctx
     */
    private void doExecuteAction(TestContext ctx) {
        List<TestAction> actions = ctx.getActionManager().getActions(ActionType.EXECUTE);
        for (TestAction action : actions) {
            action.doAction(ctx);
        }
    }

    /**
     * 执行检查操作。
     * 
     * @param ctx
     */
    private void doCheckAction(TestContext ctx) {
        List<TestAction> actions = ctx.getActionManager().getActions(ActionType.CHECK);
        for (TestAction action : actions) {
            action.doAction(ctx);
        }
    }

    /**
     * 执行清理操作。
     * 
     * @param ctx
     */
    private void doClearAction(TestContext ctx) {
        List<TestAction> actions = ctx.getActionManager().getActions(ActionType.CLEAR);
        for (TestAction action : actions) {
            action.doAction(ctx);
        }
    }

    /**
     * 输出测试用例描述。
     * 
     * @param testCase
     */
    private void printCaseDesc(TestCase testCase) {

        TestLogger.getLogger().info("[case id]", testCase.getCaseId());
        TestProfiler.item("[case id]", testCase.getCaseId());

        TestLogger.getLogger().info("[case option]", testCase.getOption());
        TestProfiler.item("[case option]", testCase.getOption());

        if (testCase.getCaseDescription() != null) {
            TestLogger.getLogger().info("[case description]");
            TestProfiler.enter("[case description]");
            try {
                String s = testCase.getCaseDescription();
                TestLogger.getLogger().info(s);
                TestProfiler.item(s);
            } finally {
                TestProfiler.release();
            }
        }
    }

    // ~~~ 容器方法

    /**
     * Setter method for property <tt>ccRepositoryFactory</tt>.
     * 
     * @param ccRepositoryFactory value to be assigned to property ccRepositoryFactory
     */
    public void setCcRepositoryFactory(CCRepositoryFactory ccRepositoryFactory) {
        this.ccRepositoryFactory = ccRepositoryFactory;
    }

    /** 
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SofaRuntimeReference.setApplicationContext(applicationContext);
    }

}
