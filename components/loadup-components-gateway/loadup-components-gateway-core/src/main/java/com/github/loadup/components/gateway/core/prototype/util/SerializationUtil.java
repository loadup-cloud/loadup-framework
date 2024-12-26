package com.github.loadup.components.gateway.core.prototype.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class SerializationUtil {
    /**
     * No need to convert data format
     */
    public static final String DATE_NO_CONVERT = "NoConvert";

    public static String serializeWithDateFormat(Object object, String dateFormat) {
        if (DATE_NO_CONVERT.equals(dateFormat)) {
            return JSON.toJSONString(object);
        } else if (StringUtils.isNotBlank(dateFormat)) {
            return JSONObject.toJSONString(object);
        }
        return JSON.toJSONString(object);
    }
}