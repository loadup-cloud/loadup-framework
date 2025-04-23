/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.ccil.handler;

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

import com.alipay.test.acts.component.InvokeContext;
import com.alipay.test.acts.component.ccil.CcilExpr;
import com.alipay.test.acts.component.ccil.CcilInvokeParam;
import com.alipay.test.acts.component.ccil.CcilVObject;
import com.alipay.test.acts.component.ccil.CcilVPrimitive;
import com.alipay.test.acts.component.ccil.CcilVariantRef;
import com.alipay.test.acts.component.data.OgnlValue;
import com.alipay.test.acts.component.data.TextValue;

/**
 * 调用参数处理器。
 * 
 * @author dasong.jds
 * @version $Id: CcilInvokeParamHandler.java, v 0.1 2015年7月21日 下午10:28:54 dasong.jds Exp $
 */
class CcilInvokeParamHandler {

    /**
     * 处理表达式参数信息。
     * 
     * @param invokeContext
     * @param invoke
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void handleInvokeParams(InvokeContext invokeContext, List<CcilInvokeParam> invokeParams) {

        if (invokeParams == null) {
            return;
        }

        // 表达式部分，可能会覆盖传入部分。
        InvokeParamVistor[] visitors = new InvokeParamVistor[] {
                new PrimitiveParamVisitor(invokeContext), new RefrenceParamVisitor(invokeContext),
                new ObjectParamVisitor(invokeContext) };

        for (CcilInvokeParam param : invokeParams) {
            for (InvokeParamVistor vistor : visitors) {
                if (vistor.accept(param)) {
                    vistor.visitParamName(param.getName());
                    vistor.visitValueExp(param.getValue());
                }
            }
        }

    }

    /**
     * 调用参数访问器。
     */
    static abstract class InvokeParamVistor<T extends CcilExpr> {

        protected final InvokeContext invokeContext;

        protected String              paramName;

        public InvokeParamVistor(InvokeContext invokeContext) {
            super();
            this.invokeContext = invokeContext;
        }

        public abstract boolean accept(CcilInvokeParam paramExp);

        public void visitParamName(String paramName) {
            this.paramName = paramName;
        }

        public abstract void visitValueExp(T exp);

    }

    /**
     * 原始参数处理。
     */
    static class PrimitiveParamVisitor extends InvokeParamVistor<CcilVPrimitive> {

        public PrimitiveParamVisitor(InvokeContext invokeContext) {
            super(invokeContext);
        }

        @Override
        public boolean accept(CcilInvokeParam paramExp) {
            return paramExp.getValue() instanceof CcilVPrimitive;
        }

        @Override
        public void visitParamName(String paramName) {
            this.paramName = paramName;
        }

        @Override
        public void visitValueExp(CcilVPrimitive exp) {
            this.invokeContext.setVariable(this.paramName, exp.getValue());
        }

    }

    /**
     * 引用参数处理。
     */
    static class RefrenceParamVisitor extends InvokeParamVistor<CcilVariantRef> {

        public RefrenceParamVisitor(InvokeContext invokeContext) {
            super(invokeContext);
        }

        @Override
        public boolean accept(CcilInvokeParam paramExp) {
            return paramExp.getValue() instanceof CcilVariantRef;
        }

        @Override
        public void visitValueExp(CcilVariantRef exp) {

            invokeContext.setVariable(paramName, new OgnlValue(exp.getOgnl()));
        }
    }

    static class ObjectParamVisitor extends InvokeParamVistor<CcilVObject> {

        public ObjectParamVisitor(InvokeContext invokeContext) {
            super(invokeContext);
        }

        @Override
        public boolean accept(CcilInvokeParam paramExp) {
            return paramExp.getValue() instanceof CcilVObject;
        }

        @Override
        public void visitValueExp(CcilVObject exp) {
            this.invokeContext.setVariable(this.paramName, new TextValue(exp.getValue()));
        }
    }

}
