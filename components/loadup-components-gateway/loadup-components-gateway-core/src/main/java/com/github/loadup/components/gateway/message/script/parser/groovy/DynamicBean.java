package com.github.loadup.components.gateway.message.script.parser.groovy;

import java.util.*;

/**
 * 需要动态加载到Spring中的Bean配置信息
 */
public final class DynamicBean {

    /**
     * 存储属性的map
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * 添加属性
     */
    public void put(String key, String value) {
        properties.put(key, value);
    }

    /**
     * 遍历属性
     */
    public Iterator<String> keyIterator() {
        return properties.keySet().iterator();
    }

    /**
     * 返回属性值
     */
    public String get(String key) {
        return properties.get(key);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer retValue = new StringBuffer("DynamicBean[properties=");
        retValue.append(this.properties);
        retValue.append(']');
        return retValue.toString();
    }

}
