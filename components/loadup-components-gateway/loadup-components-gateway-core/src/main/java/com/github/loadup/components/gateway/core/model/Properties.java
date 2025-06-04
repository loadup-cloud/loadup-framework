/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.model;

/*-
 * #%L
 * loadup-components-gateway-core
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

import com.github.loadup.components.gateway.core.common.Constant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * properties container
 */
public class Properties {

    /**
     * 参数map
     */
    private final Map<String, String> configMap = new HashMap<String, String>();

    /**
     * Constructor.
     */
    public Properties() {}

    /**
     * Constructor for deep clone
     */
    private Properties(Map<String, String> configMap) {
        this.configMap.putAll(configMap);
    }

    /**
     * get property value
     */
    public String getProperty(String propertyName) {
        return configMap.get(propertyName);
    }

    /**
     * set properties, like p1=1;p2=2 ...
     */
    public void setProperties(String properties) {
        configMap.clear();
        if (StringUtils.isEmpty(properties)) {
            return;
        }
        String[] configs = Constant.CONFIG_SEPARATOR.split(properties);
        for (String config : configs) {
            int firstSplitIndex = config.indexOf(Constant.VALUE_SEPARATOR);
            if (-1 == firstSplitIndex) {
                continue;
            }
            String keyString = config.substring(0, firstSplitIndex).trim();
            configMap.put(keyString, config.substring(firstSplitIndex + 1));
        }
    }

    /**
     * set properties, like p1=1;p2=2 ...
     */
    public void setProperties(Map<String, String> properties) {
        configMap.clear();
        if (MapUtils.isEmpty(properties)) {
            return;
        }
        this.configMap.putAll(properties);
    }

    /**
     * set properties with specified regex
     */
    public void setProperties(String properties, String regex) {
        configMap.clear();
        if (StringUtils.isEmpty(properties)) {
            return;
        }
        Pattern pattern = Pattern.compile(regex);
        String[] configs = pattern.split(properties);
        for (String config : configs) {
            if (StringUtils.isEmpty(config)) {
                continue;
            }
            int firstSplitorIndex = config.indexOf(Constant.VALUE_SEPARATOR);
            String keyString = config.substring(0, firstSplitorIndex).trim();
            configMap.put(keyString, config.substring(firstSplitorIndex + 1));
        }
    }

    /**
     * return configMap
     */
    public Map<String, String> getConfigMap() {
        return configMap;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return configMap.toString();
    }

    /**
     * Clone properties.
     */
    public Properties clone() {
        Properties newProperties = new Properties(this.configMap);
        return newProperties;
    }
}
