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

import java.util.ArrayList;
import java.util.List;

import com.alipay.test.acts.component.InvokeContext;
import com.alipay.test.acts.component.ccil.CcilExpr;
import com.alipay.test.acts.component.ccil.CcilStatement;
import com.alipay.test.acts.component.ccil.parser.CcilExprParser;

/**
 * ccil执行器。
 * 
 * @author dasong.jds
 * @version $Id: CcilExpr.java, v 0.1 2015年5月27日 上午11:40:11 dasong.jds Exp $
 */
public class CcilExecutor {

    /** 处理器 */
    private static List<CcilExprHandler> handlers = new ArrayList<CcilExprHandler>();

    static {
        handlers.add(new CcilInvokeHandler());
        handlers.add(new CcilAssignHandler());
        handlers.add(new CcilBeanCallHandler());
    }

    /**
     * 执行ccil。
     * 
     * @param invokeContext
     * @param ccil
     * @return
     */
    public Object execute(InvokeContext invokeContext, String ccil) {

        Object result = null;

        CcilExpr expr = new CcilExprParser(ccil).parse();

        if (expr instanceof CcilStatement) {
            for (CcilExpr e : ((CcilStatement) expr).getCcilExprs()) {
                result = handleExpr(invokeContext, e);
            }
        } else {
            result = handleExpr(invokeContext, expr);
        }

        return result;
    }

    /**
     * 原子表达式处理。
     * 
     * @param invokeContext
     * @param expr
     * @return
     */
    public Object handleExpr(InvokeContext invokeContext, CcilExpr expr) {

        for (CcilExprHandler handler : handlers) {
            if (handler.accept(expr)) {
                return handler.handle(invokeContext, expr);
            }
        }
        return null;
    }
}
