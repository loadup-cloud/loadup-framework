/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.datarule.parser;

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

import com.alipay.test.acts.exception.CcilParseException;
import com.alipay.test.acts.exception.RuleParseException;

/**
 * rule解析基类。
 * 
 * @author dasong.jds
 * @version $Id: RuleParser.java, v 0.1 2015年5月25日 下午2:39:45 dasong.jds Exp $
 */
public abstract class RuleParser<E> {

    protected final RuleLexer lexer;

    public RuleParser(String rule) {
        this(new RuleLexer(rule));
        this.lexer.nextToken();
    }

    public RuleParser(RuleLexer lexer) {
        this.lexer = lexer;
    }

    /**
     * 分析生成对应表达式。
     * 
     * @return
     */
    public abstract E parse();

    protected boolean identifierEquals(String text) {
        return lexer.token() == RuleToken.IDENTIFIER && lexer.stringVal().equalsIgnoreCase(text);
    }

    protected void acceptIdentifier(String text) {
        if (identifierEquals(text)) {
            lexer.nextToken();
        } else {
            setErrorEndPos(lexer.pos());
            throw new RuleParseException("syntax error[expect=" + text + ",actual=" + lexer.token()
                                         + "],rule=" + lexer.text);
        }
    }

    protected void accept(RuleToken token) {
        if (lexer.token() == token) {
            lexer.nextToken();
        } else {
            setErrorEndPos(lexer.pos());
            throw new CcilParseException("syntax error[expect=" + token + ",actual="
                                         + lexer.token() + "],rule=" + lexer.text);
        }
    }

    private int errorEndPos = -1;

    protected void setErrorEndPos(int errPos) {
        if (errPos > errorEndPos) {
            errorEndPos = errPos;
        }
    }

    protected String scanFieldName() {

        String field = lexer.stringVal();
        lexer.nextToken();
        while (lexer.token() == RuleToken.DOT) {
            lexer.nextToken();
            field = field + "." + lexer.stringVal();
            lexer.nextToken();
        }

        return field;
    }

}
