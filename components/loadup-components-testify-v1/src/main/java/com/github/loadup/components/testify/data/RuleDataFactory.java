/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data;

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

import com.github.loadup.components.testify.data.impl.UnifiedRuleDataStore;
import com.github.loadup.components.testify.datarule.RULE;
import com.github.loadup.components.testify.datarule.RULE.ReferenceHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则数据工厂。
 *
 *
 *
 */
public class RuleDataFactory {

    /**
     * 数据仓库
     */
    private RuleDataStore dataStore = new UnifiedRuleDataStore();

    /**
     * 根据规则获取数据。
     *
     * @param callback
     * @param rule
     * @return
     */
    public String getDataByOriginRule(String system, RuleDataCallback callback, String rule) {

        ReferenceHandler refHandler = createMappedHandler(callback, system);

        return RULE.execute(rule, refHandler);
    }

    /**
     * 根据规则获取数据。
     *
     * @param callback
     * @param rule
     * @return
     */
    public String getDataByRule(String system, RuleDataCallback callback, String rule) {

        ReferenceHandler refHandler = createHandler(callback, system);

        return RULE.execute(rule, refHandler);
    }

    /**
     * 根据配置内容获取对应数据。
     *
     * @param callback
     * @param names
     * @return
     */
    public Map<String, String> getDataByOriginName(String system, RuleDataCallback callback, String... names) {

        ReferenceHandler refHandler = createMappedHandler(callback, system);

        Map<String, String> datas = new HashMap<String, String>(names.length);

        for (String name : names) {
            String rule = dataStore.getMappedRule(system, name);
            String value = RULE.execute(rule, refHandler);

            datas.put(name, value);
        }

        return datas;
    }

    /**
     * 根据配置内容获取对应数据。
     *
     * @param callback
     * @param names
     * @return
     */
    public Map<String, String> getDataByName(String system, RuleDataCallback callback, String... names) {

        ReferenceHandler refHandler = createHandler(callback, system);

        Map<String, String> datas = new HashMap<String, String>(names.length);

        for (String name : names) {
            String rule = dataStore.getRule(system, name);
            String value = RULE.execute(rule, refHandler);

            datas.put(name, value);
        }

        return datas;
    }

    // ~~~ 私有实现

    /**
     * 创建处理器。
     *
     * @param callback
     * @param system
     * @return
     */
    private ReferenceHandler createHandler(final RuleDataCallback callback, final String system) {
        return new ReferenceHandler() {

            @Override
            public String getSystem() {
                return system;
            }

            @Override
            public String getRule(String name) {
                return dataStore.getRule(system, name);
            }

            @Override
            public String getData(String name) {
                if (callback != null) {
                    return callback.getData(name);
                }
                return null;
            }

            @Override
            public void setData(String name, String data) {
                if (callback != null) {
                    callback.setData(name, data);
                }
            }
        };
    }

    /**
     * 创建映射handler。
     *
     * @param callback
     * @param system
     * @return
     */
    private ReferenceHandler createMappedHandler(final RuleDataCallback callback, final String system) {
        return new ReferenceHandler() {

            @Override
            public String getSystem() {
                return system;
            }

            @Override
            public String getRule(String name) {
                return dataStore.getMappedRule(system, name);
            }

            @Override
            public String getData(String name) {
                if (callback != null) {
                    return callback.getData(name);
                }
                return null;
            }

            @Override
            public void setData(String name, String data) {
                if (callback != null) {
                    callback.setData(name, data);
                }
            }
        };
    }

    // ~~~ 容器方法

    /**
     *
     *
     * @param dataStore value to be assigned to property dataStore
     */
    public void setDataStore(RuleDataStore dataStore) {
        this.dataStore = dataStore;
    }
}
