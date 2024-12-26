package com.github.loadup.components.gateway.core.model;

import com.github.loadup.components.gateway.core.common.Constant;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
    public Properties() {
    }

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
