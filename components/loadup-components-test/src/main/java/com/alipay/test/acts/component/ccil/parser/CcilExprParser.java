/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.ccil.parser;

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

import com.alipay.test.acts.component.ccil.CcilAssign;
import com.alipay.test.acts.component.ccil.CcilBeanCall;
import com.alipay.test.acts.component.ccil.CcilExpr;
import com.alipay.test.acts.component.ccil.CcilInvoke;
import com.alipay.test.acts.component.ccil.CcilStatement;
import com.alipay.test.acts.exception.CcilParseException;

/**
 * ccil 表达式解析器。
 * 
 * @author dasong.jds
 * @version $Id: CcilParser.java, v 0.1 2015年5月24日 上午12:14:45 dasong.jds Exp $
 */
public class CcilExprParser extends CcilParser<CcilExpr> {

    public CcilExprParser(String ccil) {
        super(ccil);
    }

    public CcilExprParser(Lexer lexer) {
        super(lexer);
    }

    /** 
     * @see com.ipay.itest.common.ccil.parser.CcilParser#parse()
     */
    @Override
    public CcilExpr parse() {

        CcilExpr ccilExpr = null;

        for (;;) {
            final Token tok = lexer.token();

            switch (tok) {
                case CCPREPARE:
                case CCCHECK:
                case CCCLEAR:
                case CCMOCK:
                    CcilInvoke invoke = parseInvoke();
                    ccilExpr = recordExpr(ccilExpr, invoke);
                    break;
                case CCBEAN:
                    CcilBeanCall beanCall = parseBeanCall();
                    ccilExpr = recordExpr(ccilExpr, beanCall);
                    break;
                case IDENTIFIER:
                    CcilAssign assignExpr = new CcilAssign();
                    assignExpr.setName(lexer.stringVal());
                    lexer.nextToken();
                    accept(Token.EQ);
                    if (Token.CCBEAN == lexer.token) {
                        assignExpr.setBeanCall(parseBeanCall());
                    } else {
                        assignExpr.setCcilInvoke(parseInvoke());
                    }
                    ccilExpr = recordExpr(ccilExpr, assignExpr);
                    break;
                case SEMI:
                    if (ccilExpr != null && !(ccilExpr instanceof CcilStatement)) {
                        ccilExpr = recordExpr(ccilExpr, new CcilStatement());
                    }
                    lexer.nextToken();
                    break;
                case EOF:
                    return ccilExpr;
                default:
                    throw new CcilParseException("ERROR[token=" + tok + ",pos=" + lexer.pos()
                                                 + "],expect ccinvoke or ccbean expr,ccil="
                                                 + lexer.text);
            }

        }

    }

    /**
     * 根据上次处理结果登记。
     * 
     * @param last
     * @param current
     * @return
     */
    private CcilExpr recordExpr(CcilExpr last, CcilExpr current) {
        if (last instanceof CcilStatement) {
            ((CcilStatement) last).addCcilExpr(current);
            return last;
        } else if (current instanceof CcilStatement) {
            ((CcilStatement) current).addCcilExpr(last);
            return current;
        } else {
            return current;
        }
    }

    /**
     * 解析组件调用表达式。
     * 
     * @return
     */
    private CcilInvoke parseInvoke() {
        return (new CcilInvokeParser(lexer)).parse();
    }

    /**
     * 解析bean调用。
     * @return
     */
    private CcilBeanCall parseBeanCall() {
        return new CcilBeanCallParser(lexer).parse();
    }

}
