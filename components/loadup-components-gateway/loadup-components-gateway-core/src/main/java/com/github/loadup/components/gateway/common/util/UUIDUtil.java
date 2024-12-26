package com.github.loadup.components.gateway.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 *
 */
public class UUIDUtil {

    /**
     * generate uuid
     */
    public static String getUUID() {
        return StringUtils.replaceAll(UUID.randomUUID().toString(), "-", "");
    }
}