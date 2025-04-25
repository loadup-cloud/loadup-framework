/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.datarule.parser;

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

import com.github.loadup.components.testify.datarule.ArrayRule;
import com.github.loadup.components.testify.datarule.Reference;
import com.github.loadup.components.testify.exception.RuleParseException;

/**
 * 序列规则分析器。
 *
 * 
 *
 */
public class ArrayRuleParser extends RuleParser<ArrayRule> {

    /**
     * @param lexer
     */
    public ArrayRuleParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public ArrayRuleParser(String rule) {
        super(rule);
    }

    /**
     * @see RuleParser#parse()
     */
    @Override
    public ArrayRule parse() {

        ArrayRule arrayRule = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case LBRACE:
                arrayRule = new ArrayRule();
                lexer.nextToken();

                if (lexer.token() != RuleToken.RPAREN) {
                    parseItems(arrayRule);
                }

                accept(RuleToken.RBRACE);
                return arrayRule;

            case EOF:
                throw new RuleParseException("EOF");
            default:
                throw new RuleParseException("array rule parse ERROR[token=" + tok + ",pos="
                        + lexer.pos()
                        + "],expect pattern like {aaa,bbb,ccc},rule="
                        + lexer.text);
        }
    }

    /**
     * 分析参数部分。
     *
     * @param arrayRule
     */
    private void parseItems(ArrayRule arrayRule) {
        for (; ; ) {
            Object item = parseItem();
            arrayRule.addItem(item);

            RuleToken toc = lexer.token();
            if (RuleToken.COMMA == toc) {
                lexer.nextToken();
            } else {
                break;
            }
        }
    }

    /**
     * 单项解析。
     *
     * @return
     */
    private Object parseItem() {
        RuleToken toc = lexer.token();
        switch (toc) {
            case VARIANT:
                String var = lexer.stringVal();
                lexer.nextToken();
                return new Reference(var);
            case LITERAL_INT:
            case LITERAL_FLOAT:
                String number = lexer.numberString();
                lexer.nextToken();
                return number;
            case LBRACE:
                lexer.scanObject();
                String object = lexer.stringVal();
                lexer.nextToken();
                return object;
            case IDENTIFIER:
            case LITERAL_HEX:
            case LITERAL_CHARS:
            case LITERAL_NCHARS:
            case LITERAL_ALIAS:
                String value = lexer.stringVal();
                lexer.nextToken();
                return value;
            default:
                return null;
        }
    }

}
