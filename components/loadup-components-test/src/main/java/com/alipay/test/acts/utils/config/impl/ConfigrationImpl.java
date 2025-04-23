/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.utils.config.impl;

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

import java.util.Map;
import java.util.HashMap;

import com.alipay.test.acts.utils.config.Configration;

/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: ConfigrationImpl.java, v 0.1 2015年10月20日 上午11:53:41 tianzhu.wtzh Exp $
 */
public class ConfigrationImpl implements Configration {

    /**
     * 配置Map
     */
    private Map<String, String> atsConfigMap = new HashMap<String, String>();

    public ConfigrationImpl() {
    }

    public Map<String, String> getConfig() {
        //对map进行clone，保证map的安全性
        Map<String, String> map = new HashMap<String, String>();
        map.putAll(this.atsConfigMap);
        return map;
    }

    public void setConfig(Map<String, String> map) {
        this.atsConfigMap.putAll(map);
    }

    public String getPropertyValue(String key) {
        return this.atsConfigMap.get(key);
    }

    /**
     * 设置配置属性
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        this.atsConfigMap.put(key, value);
    }

    public String getPropertyValue(String key, String defaultValue) {
        return this.atsConfigMap.get(key) == null ? defaultValue : this.atsConfigMap.get(key);
    }
}
