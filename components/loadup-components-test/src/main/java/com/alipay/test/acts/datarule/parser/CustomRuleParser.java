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

import com.alipay.test.acts.datarule.CustomRule;
import com.alipay.test.acts.datarule.Reference;
import com.alipay.test.acts.exception.RuleParseException;

/**
 * 自定义规则解析器。
 * 
 * @author dasong.jds
 * @version $Id: CustomRuleParser.java, v 0.1 2015年9月24日 下午1:39:26 dasong.jds Exp $
 */
public class CustomRuleParser extends RuleParser<CustomRule> {

    /**
     * @param lexer
     */
    public CustomRuleParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public CustomRuleParser(String rule) {
        super(rule);
    }

    /** 
     * @see RuleParser#parse()
     */
    @Override
    public CustomRule parse() {

        CustomRule customRule = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case IDENTIFIER:
                customRule = new CustomRule();

                customRule.setMethodName(lexer.stringVal());
                lexer.nextToken();

                accept(RuleToken.LPAREN);

                if (lexer.token() != RuleToken.RPAREN) {
                    parseParams(customRule);
                }

                accept(RuleToken.RPAREN);

                return customRule;

            case EOF:
                throw new RuleParseException("EOF");
            default:
                throw new RuleParseException("custom rule parse ERROR[token=" + tok + ",pos="
                                             + lexer.pos()
                                             + "],expect pattern like xxxx(22,sss,${xxx}),rule="
                                             + lexer.text);
        }
    }

    /**
     * 分析参数部分。
     * 
     * @param customRule
     */
    private void parseParams(CustomRule customRule) {
        for (;;) {
            Object param = parseParam();
            customRule.addParam(param);

            RuleToken toc = lexer.token();
            if (RuleToken.COMMA == toc) {
                lexer.nextToken();
            } else {
                break;
            }
        }
    }

    /**
     * 单个参数解析。
     * 
     * @return
     */
    private Object parseParam() {
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
