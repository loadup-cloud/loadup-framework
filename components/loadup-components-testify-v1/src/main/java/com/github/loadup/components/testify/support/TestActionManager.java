/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.support;

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

import com.github.loadup.components.testify.support.enums.ActionType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作管理器。
 * <p>
 * 可注册不同阶段的测试动作。
 *
 *
 *
 */
public class TestActionManager {

    /**
     * 操作列表
     */
    private final Map<ActionType, List<TestAction>> testActions = new HashMap<ActionType, List<TestAction>>();

    /**
     * 注册action。
     *
     * @param action
     */
    public void registerAction(ActionType actionType, TestAction action) {

        getActions(actionType).add(action);
    }

    /**
     * 获取操作列表。
     *
     * @param actionType
     * @return
     */
    public List<TestAction> getActions(ActionType actionType) {
        List<TestAction> actions = testActions.get(actionType);
        if (actions == null) {
            actions = new ArrayList<TestAction>();
            testActions.put(actionType, actions);
        }
        return actions;
    }
}
