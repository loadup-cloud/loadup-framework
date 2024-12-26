package com.github.loadup.components.gateway.plugin.repository.file.util;

import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * FileRepositoryUtil.java
 * </p>
 */
public class FileRepositoryUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryUtil.class);

    /**
     * get columns from row
     */
    public static String[] getColumns(String line) {
        if (StringUtils.isBlank(line) || StringUtils.isBlank(line.trim())) {
            //log sys warn and ignore
            LogUtil.warn(logger, "reading empty line, ignore");
            return null;
        }

        return StringUtils.splitPreserveAllTokens(line, Constant.COMMA_SEPARATOR);
    }

}