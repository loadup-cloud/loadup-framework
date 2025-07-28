/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin;

/*-
 * #%L
 * communication-springbean-plugin
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

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.commons.util.context.ApplicationContextUtils;
import com.github.loadup.components.gateway.facade.extpoint.CommunicationProxyExtPt;
import com.github.loadup.components.gateway.facade.model.CommunicationConfiguration;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.helper.ApiDefinition;
import com.github.loadup.components.gateway.plugin.helper.BeanApiHelper;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SpringBean call implementation of communication plugin extension.
 */
@Extension(bizCode = "SPRINGBEAN")
@Component
public class SpringBeanCommunicationExtPt implements CommunicationProxyExtPt {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(SpringBeanCommunicationExtPt.class);

    private BeanApiHelper beanApiHelper = BeanApiHelper.getInstance();

    @Override
    public String sendMessage(CommunicationConfiguration config, String messageContent) {
        Object result;
        if (config == null) {
            LogUtil.error(log, "BEAN_CALL_ERROR, CommunicationConfiguration is null");
            return "";
        }

        try {
            ApiDefinition apiDefinition = beanApiHelper.parseApiDefinition(config.getUri());

            // invoke spring bean method in current spring context
            Object objService = ApplicationContextUtils.getBean(apiDefinition.beanId());
            Method method = beanApiHelper.getServiceMethod(objService, apiDefinition);
            Object request = beanApiHelper.parseParam(method, messageContent);
            result = method.invoke(objService, request);
        } catch (Exception e) {
            LogUtil.error(
                    log,
                    e,
                    "BEAN_CALL_ERROR_PREX, Failed to send message because of error occurs when trying to get bean with config:"
                            + config.getUri());
            throw new RuntimeException("Failed to send message to springbean.");
        }
        return result != null ? JsonUtil.toJSONString(result) : "";
    }
}
