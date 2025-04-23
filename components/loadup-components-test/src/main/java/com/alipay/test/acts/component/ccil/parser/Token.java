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

/**
 * ccil标记。
 * 
 * @author dasong.jds
 * @version $Id: Token.java, v 0.1 2015年5月25日 下午1:46:58 dasong.jds Exp $
 */
public enum Token {

    // ~~~ 关键字
    CCPREPARE("ccprepare"),

    CCCHECK("cccheck"),

    CCCLEAR("ccclear"),

    CCMOCK("ccmock"),

    CCBEAN("ccbean"),

    // ~~~ 控制符
    EOF,

    ERROR,

    IDENTIFIER,

    HINT,

    VARIANT,

    LITERAL_INT,

    LITERAL_FLOAT,

    LITERAL_HEX,

    LITERAL_CHARS,

    LITERAL_NCHARS,

    LITERAL_ALIAS,

    LINE_COMMENT,

    MULTI_LINE_COMMENT,

    // ~~~ 符号
    LPAREN("("),

    RPAREN(")"),

    LBRACE("{"),

    RBRACE("}"),

    LBRACKET("["),

    RBRACKET("]"),

    SEMI(";"),

    COMMA(","),

    DOT("."),

    DOTDOT(".."),

    DOTDOTDOT("..,"),

    EQ("="),

    GT(">"),

    LT("<"),

    BANG("!"),

    TILDE("~"),

    QUES("?"),

    COLON(":"),

    COLONCOLON("::"),

    COLONEQ(":="),

    EQEQ("=="),

    LTEQ("<="),

    LTEQGT("<=>"),

    LTGT("<>"),

    GTEQ(">="),

    BANGEQ("!="),

    BANGGT("!>"),

    BANGLT("!<"),

    AMPAMP("&&"),

    BARBAR("||"),

    PLUS("+"),

    SUB("-"),

    STAR("*"),

    SLASH("/"),

    AMP("&"),

    BAR("|"),

    CARET("^"),

    PERCENT("%"),

    LTLT("<<"),

    GTGT(">>"),

    MONKEYS_AT("@");

    public final String name;

    Token() {
        this(null);
    }

    Token(String name) {
        this.name = name;
    }

}
