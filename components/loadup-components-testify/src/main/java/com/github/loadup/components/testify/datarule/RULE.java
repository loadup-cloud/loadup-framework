/**
 
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.datarule;

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


import com.github.loadup.components.testify.datarule.handler.RuleHandler;
import com.github.loadup.components.testify.datarule.handler.RuleHandlerFactory;
import com.github.loadup.components.testify.datarule.parser.RuleObjectParser;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据规则操作统一入口。
 *
 * 
 *
 */
public class RULE {

    /**
     * 规则执行。
     *
     * @param rule
     * @param refHandler
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String execute(String rule, ReferenceHandler refHandler) {

        if (StringUtils.isEmpty(rule)) {
            return null;
        }

        RuleObject ruleObject = new RuleObjectParser(rule).parse();
        RuleHandler<RuleObject> handler = RuleHandlerFactory.getHandler(ruleObject.getClass());

        String value = handler.handle(ruleObject, refHandler);

        return value;
    }

    /**
     * rule中如果存在变量引用，
     */
    public static interface ReferenceHandler {

        /**
         * 获取系统。
         *
         * @return
         */
        String getSystem();

        /**
         * 获取规则。
         *
         * @param name
         * @return
         */
        String getRule(String name);

        /**
         * 获取数据，以便优先使用业务定义数据。
         *
         * @param name
         * @return
         */
        String getData(String name);

        /**
         * 设置数据，以便业务感知生成数据。
         *
         * @param name
         * @param data
         * @return
         */
        void setData(String name, String data);
    }
}
