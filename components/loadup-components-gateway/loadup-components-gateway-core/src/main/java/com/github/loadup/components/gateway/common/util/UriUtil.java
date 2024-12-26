package com.github.loadup.components.gateway.common.util;

import com.github.loadup.components.gateway.core.common.Constant;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class UriUtil {

    /**
     * get uri with path separator replaced by dot.
     * such as:
     * input: http://query/rate.htm
     * return: query.rate.htm
     */
    public static String getUriWithDot(String url) {
        return StringUtils.replace(getURIPath(url), Constant.PATH_SEPARATOR, Constant.PATH_CONJUNCTION);
    }

    /**
     * get uri
     */
    public static String getURIPath(String url) {
        int index = StringUtils.indexOf(url, Constant.URI_SEPARATOR);
        if (index >= 0 && index < (url.length() - 3)) {
            return StringUtils.substring(url, index + 3);
        } else {
            return url;
        }
    }

}