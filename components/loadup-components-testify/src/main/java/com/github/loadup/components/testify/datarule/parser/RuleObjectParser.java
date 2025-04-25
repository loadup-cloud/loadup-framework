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

import com.github.loadup.components.testify.datarule.RuleObject;
import com.github.loadup.components.testify.exception.RuleParseException;

/**
 * 规则对象解析统一入口，适配各种表达式。
 *
 * 
 *
 */
public class RuleObjectParser extends RuleParser<RuleObject> {

    /**
     * @param lexer
     */
    public RuleObjectParser(RuleLexer lexer) {
        super(lexer);
    }

    /**
     * @param rule
     */
    public RuleObjectParser(String rule) {
        super(rule);
    }

    /**
     * @see RuleParser#parse()
     */
    @Override
    public RuleObject parse() {

        RuleObject ruleObject = null;

        final RuleToken tok = lexer.token();

        switch (tok) {
            case ASSEMBLE:
                ruleObject = new AssembleRuleParser(lexer).parse();
                break;
            case CHOOSE:
                ruleObject = new ChooseRuleParser(lexer).parse();
                break;
            case LIST:
                ruleObject = new ListChooseRuleParser(lexer).parse();
                break;
            case IDENTIFIER:
                ruleObject = new CustomRuleParser(lexer).parse();
                break;

            case LBRACE:
                ruleObject = new ArrayRuleParser(lexer).parse();
                break;

            case LPAREN:
            case LBRACKET:
                ruleObject = new RangeRuleParser(lexer).parse();
                break;

            case EOF:
                throw new RuleParseException("EOF");
            default:
                throw new RuleParseException("rule parse ERROR[token=" + tok + ",pos="
                        + lexer.pos() + "],rule=" + lexer.text);
        }
        return ruleObject;
    }

}
