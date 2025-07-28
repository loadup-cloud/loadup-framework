/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.common.util;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.extension.exector.ExtensionExecutor;
import com.github.loadup.components.gateway.cache.common.DefaultGatewayConfigs;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExt;
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
    public static RepositoryServiceExt getRepositoryService() {
        RepositoryServiceExt result = ExtensionPointLoader.get(
                RepositoryServiceExt.class, DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE));
        if (result == null) {
            throw new CommonException(GatewayErrorCode.PARAM_ILLEGAL, "Can not find repositoryService.");
        }
        return result;
    }

    /**
     * get repository type
     */
    public static RepositoryType getRepositoryType() {
        String type =
                StringUtils.defaultString(DefaultGatewayConfigs.get(Constant.REPOSITORY_EXTPOINT_BIZCODE), "FILE");
        RepositoryType result = RepositoryType.getByCode(type);
        if (result == null) {
            throw new CommonException(GatewayErrorCode.PARAM_ILLEGAL, "Can not find valid repositoryType.");
        }
        return result;
    }
}
