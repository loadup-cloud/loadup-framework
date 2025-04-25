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

import com.github.loadup.components.testify.datarule.ListChooseRule;
import com.github.loadup.components.testify.datarule.RuleObject;
import com.github.loadup.components.testify.exception.RuleParseException;

/**
 * List选择规则分析器,解析list(2,a,b)。
 * 生成[1],[1,2],[3,2]这样的列表
 *
 * 
 *
 */
public class ListChooseRuleParser extends RuleParser<ListChooseRule> {

    /**
     * @param lexer
     */
    public ListChooseRuleParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public ListChooseRuleParser(String rule) {
        super(rule);
    }

    /**
     * @see RuleParser#parse()
     */
    @Override
    public ListChooseRule parse() {

        ListChooseRule chooseRule = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case LIST:
                chooseRule = new ListChooseRule();
                lexer.nextToken();
                accept(RuleToken.LPAREN);

                int sign = 1;
                if (lexer.token() == RuleToken.SUB) {
                    sign = -1;
                    lexer.nextToken();
                }

                if (lexer.token() == RuleToken.LITERAL_INT) {
                    chooseRule.setRowCount(sign * lexer.integerValue().intValue());
                    lexer.nextToken();
                } else {
                    throw new RuleParseException(
                            "list rule parse ERROR[token=" + tok + ",pos=" + lexer.pos()
                                    + "],expect pattern like list(2,aa,bb={b1,b2}),rule=" + lexer.text);
                }

                accept(RuleToken.COMMA);

                for (; ; ) {
                    if (lexer.token() == RuleToken.IDENTIFIER) {
                        String fieldName = scanFieldName();
                        RuleObject fieldRule = null;
                        if (lexer.token() == RuleToken.EQ) {
                            lexer.nextToken();
                            fieldRule = new RuleObjectParser(lexer).parse();
                        }
                        chooseRule.addField(fieldName, fieldRule);
                    } else {
                        throw new RuleParseException(
                                "list rule parse ERROR[token=" + tok + ",pos=" + lexer.pos()
                                        + "],expect pattern like list(2,aa,bb={b1,b2}),rule="
                                        + lexer.text);
                    }
                    if (lexer.token() == RuleToken.COMMA) {
                        lexer.nextToken();
                    } else {
                        break;
                    }
                }

                accept(RuleToken.RPAREN);

                return chooseRule;

            case EOF:
                throw new RuleParseException("EOF");
            default:
                throw new RuleParseException("list rule parse ERROR[token=" + tok + ",pos="
                        + lexer.pos()
                        + "],expect pattern like list(2,aa,bb={b1,b2}),rule="
                        + lexer.text);
        }

    }
}
