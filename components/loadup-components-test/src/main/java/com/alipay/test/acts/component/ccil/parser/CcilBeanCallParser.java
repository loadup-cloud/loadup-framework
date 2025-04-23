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

import com.alipay.test.acts.component.ccil.CcilBeanCall;
import com.alipay.test.acts.exception.CcilParseException;

/**
 * bean调用分析器。
 * 
 * @author dasong.jds
 * @version $Id: CcilBeanCallParser.java, v 0.1 2015年7月21日 下午6:10:14 dasong.jds Exp $
 */
public class CcilBeanCallParser extends CcilParser<CcilBeanCall> {

    /**
     * @param ccil
     */
    public CcilBeanCallParser(String ccil) {
        super(ccil);
    }

    /**
     * @param lexer
     */
    public CcilBeanCallParser(Lexer lexer) {
        super(lexer);
    }

    /** 
     * @see com.ipay.itest.common.ccil.parser.CcilParser#parse()
     */
    @Override
    public CcilBeanCall parse() {

        CcilBeanCall beanCallExpr = null;

        final Token tok = lexer.token();

        switch (tok) {
            case CCBEAN:
                beanCallExpr = new CcilBeanCall();
                lexer.nextToken();
                accept(Token.COLON);

                beanCallExpr.setBeanName(lexer.stringVal());
                lexer.nextToken();
                accept(Token.DOT);
                beanCallExpr.setMethodName(lexer.stringVal());

                lexer.nextToken();
                if (Token.COLON == lexer.token) {
                    lexer.nextToken();
                    beanCallExpr.setBundle(lexer.stringVal());
                    lexer.nextToken();
                }

                switch (lexer.token()) {
                    case QUES:
                        beanCallExpr.setParams(new CcilInvokeParamParser(lexer).parse());
                        break;
                    case SEMI:
                    case EOF:
                        break;
                    default:
                        throw new CcilParseException("ERROR[token=" + tok + ",pos=" + lexer.pos()
                                                     + "],expect '?',';' or end,ccil=" + lexer.text);
                }
                break;
            case EOF:
                throw new CcilParseException("EOF");
            default:
                throw new CcilParseException(
                    "Inovke parse ERROR[token=" + tok + ",pos=" + lexer.pos()
                            + "],expect ccbean expr like ccbean:xxxx.xxxx:xxx?xxx=xxx,ccil="
                            + lexer.text);
        }

        return beanCallExpr;
    }

}
