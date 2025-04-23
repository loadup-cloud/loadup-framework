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

import java.util.ArrayList;
import java.util.List;

import com.alipay.test.acts.component.ccil.CcilInvokeParam;
import com.alipay.test.acts.component.ccil.CcilVObject;
import com.alipay.test.acts.component.ccil.CcilVPrimitive;
import com.alipay.test.acts.component.ccil.CcilVariantRef;

/**
 * 调用参数解析。
 * 
 * @author dasong.jds
 * @version $Id: CcilInvokeParamParser.java, v 0.1 2015年7月21日 下午7:09:33 dasong.jds Exp $
 */
public class CcilInvokeParamParser extends CcilParser<List<CcilInvokeParam>> {

    /**
     * @param lexer
     */
    public CcilInvokeParamParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * @param ccil
     */
    public CcilInvokeParamParser(String ccil) {
        super(ccil);
    }

    /** 
     * @see com.ipay.itest.common.ccil.parser.CcilParser#parse()
     */
    @Override
    public List<CcilInvokeParam> parse() {
        accept(Token.QUES);
        List<CcilInvokeParam> params = new ArrayList<CcilInvokeParam>();
        for (;;) {
            CcilInvokeParam param = parseParam();
            params.add(param);

            Token toc = lexer.token();
            if (Token.AMP == toc) {
                lexer.nextToken();
            } else {
                break;
            }
        }
        return params;

    }

    private CcilInvokeParam parseParam() {
        CcilInvokeParam param = new CcilInvokeParam();

        param.setName(lexer.stringVal());

        lexer.nextToken();
        accept(Token.EQ);
        Token toc = lexer.token();
        switch (toc) {
            case VARIANT:
                String val = lexer.stringVal();
                param.setValue(new CcilVariantRef(val));
                lexer.nextToken();
                break;
            case LITERAL_INT:
            case LITERAL_FLOAT:
                param.setValue(new CcilVPrimitive(lexer.numberString()));
                lexer.nextToken();
                break;
            case LBRACE:
                param.setValue(parseObject());
                lexer.nextToken();
                break;
            case IDENTIFIER:
            case LITERAL_HEX:
            case LITERAL_CHARS:
            case LITERAL_NCHARS:
            case LITERAL_ALIAS:
                param.setValue(new CcilVPrimitive(lexer.stringVal()));
                lexer.nextToken();
                break;
            default:
                param.setValue(null);
        }
        return param;
    }

    private CcilVObject parseObject() {
        CcilVObject obj = new CcilVObject();
        lexer.scanObject();
        obj.setValue(lexer.stringVal());
        return obj;
    }

}
