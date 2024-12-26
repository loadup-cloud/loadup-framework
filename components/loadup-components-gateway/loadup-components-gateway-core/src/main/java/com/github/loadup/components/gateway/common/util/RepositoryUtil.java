package com.github.loadup.components.gateway.common.util;

import com.alibaba.cola.extension.ExtensionExecutor;
import com.github.loadup.components.gateway.cache.common.SystemParameter;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExtPt;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class RepositoryUtil {
    @Resource
    private ExtensionExecutor extensionExecutor;

    /**
     * get repository service
     */
    public static RepositoryServiceExtPt getRepositoryService() {
        RepositoryServiceExtPt result = ExtensionPointLoader.get(RepositoryServiceExtPt.class,
                SystemParameter.getParameter(Constant.REPOSITORY_EXTPOINT_BIZCODE));
        if (result == null) {
            throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL, "Can not find repositoryService.");
        }
        return result;
    }

    /**
     * get repository type
     */
    public static RepositoryType getRepositoryType() {
        String type = StringUtils.defaultString(SystemParameter.getParameter(Constant.REPOSITORY_EXTPOINT_BIZCODE), "FILE");
        RepositoryType result = RepositoryType.getEnumByCode(type);
        if (result == null) {
            throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL, "Can not find valid repositoryType.");
        }
        return result;
    }
}