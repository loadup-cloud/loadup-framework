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

import java.util.HashMap;
import java.util.Map;

/**
 * 关键字定义。
 * 
 * @author dasong.jds
 * @version $Id: Keywords.java, v 0.1 2015年5月25日 下午2:00:49 dasong.jds Exp $
 */
public class Keywords {

    /** 关键字 */
    private final Map<String, Token> keywords;

    /** 默认唯一实例 */
    public final static Keywords     DEFAULT_KEYWORDS;

    static {
        Map<String, Token> map = new HashMap<String, Token>();

        map.put("ccprepare", Token.CCPREPARE);
        map.put("cccheck", Token.CCCHECK);
        map.put("ccclear", Token.CCCLEAR);
        map.put("ccmock", Token.CCMOCK);
        map.put("ccbean", Token.CCBEAN);

        map.put("CCPREPARE", Token.CCPREPARE);
        map.put("CCCHECK", Token.CCCHECK);
        map.put("CCCLEAR", Token.CCCLEAR);
        map.put("CCMOCK", Token.CCMOCK);
        map.put("CCBEAN", Token.CCBEAN);

        DEFAULT_KEYWORDS = new Keywords(map);
    }

    /**
     * 构造函数。
     * 
     * @param keywords
     */
    private Keywords(Map<String, Token> keywords) {
        this.keywords = keywords;
    }

    /**
     * 判断token是否关键字。
     * 
     * @param token
     * @return
     */
    public boolean containsValue(Token token) {
        return this.keywords.containsValue(token);
    }

    /**
     * 获取关键字。
     * 
     * @param key
     * @return
     */
    public Token getKeyword(String key) {
        key = key.toUpperCase();
        return keywords.get(key);
    }

}
