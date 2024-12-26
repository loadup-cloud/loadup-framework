package com.github.loadup.components.gateway.core.prototype.util;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ConfigUtils.java
 */
public class SupergwGatewayConfigurationUtils {

    /**
     * Gets get str before charset.
     */
    public static String getStrBeforeCharset(String uriString, String charset) {
        int splitIndex = StringUtils.indexOf(uriString, charset);
        if (splitIndex > 0) {
            uriString = StringUtils.substring(uriString, 0, splitIndex);

        }
        return uriString;
    }

}