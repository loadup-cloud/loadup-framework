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

import java.util.HashMap;
import java.util.Map;

/**
 * 关键字定义。
 *
 * 
 *
 */
public class RuleKeywords {

    /**
     * 默认唯一实例
     */
    public final static RuleKeywords DEFAULT_KEYWORDS;

    static {
        Map<String, RuleToken> map = new HashMap<String, RuleToken>();
        map.put("choose", RuleToken.CHOOSE);
        map.put("CHOOSE", RuleToken.CHOOSE);

        map.put("list", RuleToken.LIST);
        map.put("LIST", RuleToken.LIST);

        map.put("assemble", RuleToken.ASSEMBLE);
        map.put("ASSEMBLE", RuleToken.ASSEMBLE);

        DEFAULT_KEYWORDS = new RuleKeywords(map);
    }

    /**
     * 关键字
     */
    private final Map<String, RuleToken> keywords;

    /**
     * 构造函数。
     *
     * @param keywords
     */
    private RuleKeywords(Map<String, RuleToken> keywords) {
        this.keywords = keywords;
    }

    /**
     * 判断token是否关键字。
     *
     * @param token
     * @return
     */
    public boolean containsValue(RuleToken token) {
        return this.keywords.containsValue(token);
    }

    /**
     * 获取关键字。
     *
     * @param key
     * @return
     */
    public RuleToken getKeyword(String key) {
        key = key.toUpperCase();
        return keywords.get(key);
    }

}
