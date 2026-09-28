package io.github.loadup.commons.util;

/*
@author Lise
 * @since 1.0.0
 */

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ToStringUtils {
    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.JSON_STYLE);
    }

    public static String reflectionToString(Object object) {
        return ToStringBuilder.reflectionToString(object);
    }

    public static String reflectionToString(Object object, ToStringStyle style) {
        return ToStringBuilder.reflectionToString(object, style);
    }
}
