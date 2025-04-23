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

import com.alipay.test.acts.component.ccil.CcilInvoke;
import com.alipay.test.acts.component.enums.RepositoryType;
import com.alipay.test.acts.exception.CcilParseException;

/**
 * 组件调用表达式解析器。
 * 
 * @author dasong.jds
 * @version $Id: CcilInvokeParser.java, v 0.1 2015年5月25日 下午3:33:56 dasong.jds Exp $
 */
public class CcilInvokeParser extends CcilParser<CcilInvoke> {

    /**
     * @param lexer
     */
    public CcilInvokeParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * @param ccil
     */
    public CcilInvokeParser(String ccil) {
        super(ccil);
    }

    /** 
     * @see com.ipay.itest.common.ccil.parser.CcilParser#parse()
     */
    @Override
    public CcilInvoke parse() {

        CcilInvoke ccilInvoke = null;

        final Token tok = lexer.token();

        switch (tok) {
            case CCPREPARE:
            case CCCHECK:
            case CCCLEAR:
            case CCMOCK:
                ccilInvoke = new CcilInvoke();
                ccilInvoke.setRepositoryType(RepositoryType.getRepositoryType(tok.name));

                lexer.nextToken();
                accept(Token.COLON);

                ccilInvoke.setComponentId(lexer.stringVal());
                lexer.nextToken();
                switch (lexer.token()) {
                    case QUES:
                        ccilInvoke.setParams(new CcilInvokeParamParser(lexer).parse());
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
                    "Inovke parse ERROR[token="
                            + tok
                            + ",pos="
                            + lexer.pos()
                            + "],expect invoke expr like ccprepare:xxx?xxx=xxx.There are four types:ccprepare,cccheck,ccclear and ccmock,ccil="
                            + lexer.text);
        }

        return ccilInvoke;
    }

}
