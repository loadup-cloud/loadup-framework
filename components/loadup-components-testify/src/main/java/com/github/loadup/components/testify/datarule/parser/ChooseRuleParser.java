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

import com.github.loadup.components.testify.datarule.ChooseRule;
import com.github.loadup.components.testify.datarule.RuleObject;
import com.github.loadup.components.testify.exception.RuleParseException;

/**
 * 选择规则分析器。
 *
 *
 *
 */
public class ChooseRuleParser extends RuleParser<ChooseRule> {

    /**
     * @param lexer
     */
    public ChooseRuleParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public ChooseRuleParser(String rule) {
        super(rule);
    }

    /**
     * @see RuleParser#parse()
     */
    @Override
    public ChooseRule parse() {

        ChooseRule chooseRule = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case CHOOSE:
                chooseRule = new ChooseRule();
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
                    throw new RuleParseException("choose rule parse ERROR[token="
                            + tok
                            + ",pos="
                            + lexer.pos()
                            + "],expect pattern like choose(2,aa,bb={b1,b2}),rule="
                            + lexer.text);
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
                    } else if (lexer.token() == RuleToken.ASSEMBLE) {
                        chooseRule.addAssembleRule(new AssembleRuleParser(lexer).parse());
                    } else {
                        throw new RuleParseException("choose rule parse ERROR[token="
                                + tok
                                + ",pos="
                                + lexer.pos()
                                + "],expect pattern like choose(2,aa,bb={b1,b2}),rule="
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
                throw new RuleParseException("choose rule parse ERROR[token="
                        + tok
                        + ",pos="
                        + lexer.pos()
                        + "],expect pattern like choose(2,aa,bb={b1,b2}),rule="
                        + lexer.text);
        }
    }
}
