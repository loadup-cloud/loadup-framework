package com.github.loadup.components.gateway.message.script.parser.groovy;

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

import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * groovy脚本内部缓存
 */
public class GroovyInnerCache {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(GroovyInnerCache.class);

    /**
     * 脚本列表
     */
    private static ConcurrentMap<String, GroovyInfo> groovyMap = new ConcurrentHashMap<String, GroovyInfo>();

    /**
     * 把脚本缓存一下
     */
    public static void put2map(boolean clear, List<GroovyInfo> groovyList) {
        // 先清空
        if (clear && !groovyMap.isEmpty()) {
            groovyMap.clear();
        }
        for (GroovyInfo groovyInfo : groovyList) {
            String scriptName = groovyInfo.getClassName();
            if (!groovyMap.containsKey(scriptName)) {
                groovyMap.put(scriptName, groovyInfo);
            } else {
                LogUtil.warn(logger, "duplicate groovy script with same name:" + groovyInfo);
            }
        }
    }

    /**
     * 更新map
     */
    public static void update2map(List<GroovyInfo>[] groovyInfos) {
        List<GroovyInfo> addedGroovyInfos = groovyInfos[0];
        List<GroovyInfo> updatedGroovyInfos = groovyInfos[1];
        List<GroovyInfo> deletedGroovyInfos = groovyInfos[2];
        addMap(addedGroovyInfos);
        addMap(updatedGroovyInfos);
        removeMap(deletedGroovyInfos);
    }

    /**
     * 新增
     */
    private static void addMap(List<GroovyInfo> groovyList) {
        for (GroovyInfo groovyInfo : groovyList) {
            groovyMap.put(groovyInfo.getClassName(), groovyInfo);
        }
    }

    /**
     * 删除
     */
    private static void removeMap(List<GroovyInfo> groovyList) {
        for (GroovyInfo groovyInfo : groovyList) {
            groovyMap.remove(groovyInfo.getClassName());
        }
    }

    /**
     * 根据名称获取脚本信息
     */
    public static GroovyInfo getByName(String scriptName) {
        return groovyMap.get(scriptName);
    }

    /**
     * 获取已经加载的全部脚本类名
     */
    public static Map<String, GroovyInfo> getGroovyInfos() {
        return groovyMap;
    }
}
