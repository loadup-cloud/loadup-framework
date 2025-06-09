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

import com.github.loadup.components.testify.datarule.AssembleCondition;
import com.github.loadup.components.testify.datarule.AssembleConditionItem;
import com.github.loadup.components.testify.datarule.AssembleConditionOperator;
import com.github.loadup.components.testify.datarule.AssembleItem;
import com.github.loadup.components.testify.datarule.AssembleRule;
import com.github.loadup.components.testify.datarule.AssembleValue;
import com.github.loadup.components.testify.datarule.RuleObject;
import com.github.loadup.components.testify.exception.RuleParseException;

/**
 * 组装规则解析器。
 *
 *
 *
 */
public class AssembleRuleParser extends RuleParser<AssembleRule> {

    /**
     * @param lexer
     */
    public AssembleRuleParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public AssembleRuleParser(String rule) {
        super(rule);
    }

    /**
     * @see RuleParser#parse()
     */
    @Override
    public AssembleRule parse() {

        AssembleRule assembleRule = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case ASSEMBLE:
                assembleRule = new AssembleRule();
                lexer.nextToken();
                accept(RuleToken.LPAREN);

                for (; ; ) {
                    AssembleItem assembleItem = new AssembleItem();
                    assembleRule.addItem(assembleItem);

                    if (lexer.token() == RuleToken.IDENTIFIER || lexer.token() == RuleToken.LPAREN) {
                        AssembleCondition condition = parseCondition();
                        assembleItem.setCondition(condition);
                    }
                    if (lexer.token() == RuleToken.LBRACE) {
                        lexer.nextToken();
                        parseValues(assembleItem);
                        accept(RuleToken.RBRACE);

                        if (lexer.token() == RuleToken.COMMA) {
                            lexer.nextToken();
                            continue;
                        }

                        if (lexer.token() == RuleToken.RPAREN) {
                            break;
                        }
                    } else {
                        throw new RuleParseException(getParseException(tok, null));
                    }
                }

                accept(RuleToken.RPAREN);

                return assembleRule;

            case EOF:
                throw new RuleParseException("EOF");
            default:
                throw new RuleParseException(getParseException(tok, null));
        }
    }

    /**
     * 组装条件分析。
     *
     * @return
     */
    private AssembleCondition parseCondition() {
        AssembleCondition condition = new AssembleCondition();

        int paren_count = 0;

        for (; ; ) {
            if (lexer.token() == RuleToken.LPAREN) {
                condition.add(AssembleConditionOperator.LEFT_PAREN);
                paren_count++;
                lexer.nextToken();
            } else if (lexer.token == RuleToken.IDENTIFIER) {
                AssembleConditionItem conditionItem = parseConditionItem();
                condition.add(conditionItem);

                while (lexer.token() == RuleToken.RPAREN) {
                    condition.add(AssembleConditionOperator.RIGHT_PAREN);
                    paren_count--;
                    lexer.nextToken();
                }

                if (lexer.token() == RuleToken.AMPAMP) {
                    condition.add(AssembleConditionOperator.AND);
                    lexer.nextToken();
                } else if (lexer.token() == RuleToken.BARBAR) {
                    condition.add(AssembleConditionOperator.OR);
                    lexer.nextToken();
                }
            } else if (lexer.token() == RuleToken.LBRACE) {
                break;
            } else {
                throw new RuleParseException(
                        getParseException(lexer.token(), "expect compare expr like aa==aa1&&bb==23||cc=cc1"));
            }
        }
        if (paren_count != 0) {
            throw new RuleParseException(getParseException(lexer.token(), "parenthesis not in pair"));
        }
        return condition;
    }

    /**
     * 分析条件项。
     *
     * @param condition
     */
    private AssembleConditionItem parseConditionItem() {
        AssembleConditionItem item = new AssembleConditionItem();

        item.setField(scanFieldName());

        AssembleConditionOperator operator = AssembleConditionOperator.getBySymbol(lexer.token().name);
        if (operator == null) {
            throw new RuleParseException(
                    getParseException(lexer.token(), "illegal compare symbol, expect ==,>,>=,<=,<,!="));
        }
        item.setOperator(operator);

        lexer.nextToken();
        switch (lexer.token()) {
            case IDENTIFIER:
            case LITERAL_HEX:
            case LITERAL_CHARS:
            case LITERAL_NCHARS:
            case LITERAL_ALIAS:
                String value = lexer.stringVal();
                item.setValue(value);
                lexer.nextToken();
                break;
            case LITERAL_INT:
            case LITERAL_FLOAT:
                String number = lexer.numberString();
                item.setValue(number);
                lexer.nextToken();
                break;
            default:
                throw new RuleParseException(
                        getParseException(lexer.token(), "compare value must be a string or number"));
        }

        return item;
    }

    /**
     * 分析组装值。
     *
     * @param assembleItem
     */
    private void parseValues(AssembleItem assembleItem) {
        for (; ; ) {
            if (lexer.token() == RuleToken.IDENTIFIER) {
                AssembleValue value = new AssembleValue();
                assembleItem.addValue(value);

                value.setField(scanFieldName());

                accept(RuleToken.EQ);

                RuleObject rule = new RuleObjectParser(lexer).parse();
                value.setRule(rule);

                if (lexer.token() == RuleToken.COMMA) {
                    lexer.nextToken();
                }
            } else if (lexer.token() == RuleToken.RBRACE) {
                break;
            } else {
                throw new RuleParseException(
                        getParseException(lexer.token(), "expect rule like 'aa={aa1,aa2},bb=[100-200]'"));
            }
        }
    }

    /**
     * 抛解析异常。
     *
     * @param tok
     * @return
     */
    private String getParseException(final RuleToken tok, String msg) {
        if (msg == null) {
            msg = "expect pattern like assemble(a=a1&&b=b1{c={c1,c2},d=[100-500]},{e={e1,e2}})";
        }
        return "assemble rule parse ERROR[token=" + tok + ",pos=" + lexer.pos() + "]," + msg + ",rule=" + lexer.text;
    }
}
