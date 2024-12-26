package com.github.loadup.components.gateway.common.util;

import com.github.loadup.components.gateway.core.common.enums.PropertyName;
import com.github.loadup.components.gateway.core.model.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class PropertiesUtil {

    /**
     * get property
     */
    public static String getProperty(Properties property, PropertyName name) {
        String value = property.getProperty(name.getName());
        if (StringUtils.isBlank(value)) {
            value = name.getDefaultValue();
        }
        return value;
    }

    /**
     * get property
     */
    public static String getProperty(Properties property, String name) {
        String value = property.getProperty(name);
        return value;
    }
}
