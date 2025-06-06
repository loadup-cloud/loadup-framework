/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.components.testify.datarule.RangeRule;
import com.github.loadup.components.testify.exception.RuleParseException;

/**
 * 范围规则解析器。
 *
 * <p> 支持(0-100),[0-100],(0-100],[0-100),[-],[1-],[-100)等格式。
 * <br> 括号代表不包含，方括号代表包含，无明确边界的可为空。
 *
 *
 *
 */
public class RangeRuleParser extends RuleParser<RangeRule> {

    /**
     * @param lexer
     */
    public RangeRuleParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public RangeRuleParser(String rule) {
        super(rule);
    }

    /**
     * @see RuleParser#parse()
     */
    @Override
    public RangeRule parse() {

        RangeRule rangeRule = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case LPAREN:
            case LBRACKET:
                rangeRule = new RangeRule();
                rangeRule.setContainsMin(tok == RuleToken.LBRACKET);

                lexer.nextToken();
                switch (lexer.token()) {
                    case LITERAL_INT:
                    case LITERAL_FLOAT:
                        rangeRule.setMinValue(lexer.numberString());
                        lexer.nextToken();
                        accept(RuleToken.SUB);
                        break;
                    case SUB:
                        lexer.nextToken();
                        break;
                    default:
                        throw new RuleParseException("ERROR[token="
                                + tok
                                + ",pos="
                                + lexer.pos()
                                + "],expect a number or '-',rule="
                                + lexer.text);
                }

                switch (lexer.token()) {
                    case LITERAL_INT:
                    case LITERAL_FLOAT:
                        rangeRule.setMaxValue(lexer.numberString());
                        lexer.nextToken();
                        break;
                    default:
                }

                switch (lexer.token()) {
                    case RPAREN:
                    case RBRACKET:
                        rangeRule.setContainsMax(lexer.token() == RuleToken.RBRACKET);
                        lexer.nextToken();
                        break;
                    default:
                        throw new RuleParseException("ERROR[token="
                                + tok
                                + ",pos="
                                + lexer.pos()
                                + "],expect ']' or ')',rule="
                                + lexer.text);
                }
                return rangeRule;
            case EOF:
                throw new RuleParseException("EOF");
            default:
                throw new RuleParseException("range rule parse ERROR[token="
                        + tok
                        + ",pos="
                        + lexer.pos()
                        + "],expect pattern like (0-100) or [0-100],rule="
                        + lexer.text);
        }
    }
}
