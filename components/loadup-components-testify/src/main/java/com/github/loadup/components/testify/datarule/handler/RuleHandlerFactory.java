/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.datarule.handler;

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
import com.github.loadup.components.testify.datarule.*;
import com.github.loadup.components.testify.exception.RuleParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则处理工厂。
 *
 * 
 *
 */
public class RuleHandlerFactory {

    /**
     * 处理器配置。
     */
    @SuppressWarnings("rawtypes")
    private final static Map<Class, RuleHandler> handlers = new HashMap<Class, RuleHandler>();

    /**
     * 批处理器配置。
     */
    @SuppressWarnings("rawtypes")
    private final static Map<Class, BatchRuleHandler> batchHandlers = new HashMap<Class, BatchRuleHandler>();

    static {
        handlers.put(RangeRule.class, new RangeRuleHandler());
        handlers.put(ArrayRule.class, new ArrayRuleHandler());
        handlers.put(CustomRule.class, new CustomRuleHandler());
        handlers.put(ChooseRule.class, new ChooseRuleHandler());
        handlers.put(AssembleRule.class, new AssembleRuleHandler());
        handlers.put(ListChooseRule.class, new ListChooseRuleHandler());

        batchHandlers.put(RangeRule.class, new RangeRuleHandler());
        batchHandlers.put(ArrayRule.class, new ArrayRuleHandler());
        batchHandlers.put(ListChooseRule.class, new ListChooseRuleHandler());
    }

    /**
     * 根据规则类型获取对应处理。
     *
     * @param rule
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public static RuleHandler getHandler(Class ruleType) {
        RuleHandler handler = handlers.get(ruleType);

        if (handler == null) {
            throw new RuleParseException("not support rule:" + ruleType);
        }
        return handler;
    }

    /**
     * 根据规则类型获取批量处理器。
     *
     * @param ruleType
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static BatchRuleHandler getBatchHandler(Class ruleType) {
        BatchRuleHandler batchHandler = batchHandlers.get(ruleType);
        if (batchHandler == null) {
            throw new RuleParseException("not support batch rule:" + ruleType);
        }
        return batchHandler;
    }

}
