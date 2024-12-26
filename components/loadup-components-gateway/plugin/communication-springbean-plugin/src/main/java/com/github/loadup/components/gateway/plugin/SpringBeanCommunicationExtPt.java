package com.github.loadup.components.gateway.plugin;

import com.alibaba.cola.extension.Extension;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.commons.util.context.ApplicationContextUtils;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.facade.extpoint.CommunicationProxyExtPt;
import com.github.loadup.components.gateway.facade.model.CommunicationConfiguration;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.helper.ApiDefinition;
import com.github.loadup.components.gateway.plugin.helper.BeanApiHelper;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * SpringBean call implementation of communication plugin extension.
 */
@Extension(bizId = "SPRINGBEAN")
@Component
public class SpringBeanCommunicationExtPt implements CommunicationProxyExtPt {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(SpringBeanCommunicationExtPt.class);

    private BeanApiHelper beanApiHelper = BeanApiHelper.getInstance();

    @Override
    public String sendMessage(CommunicationConfiguration config, String messageContent) {
        Object result;
        if (config == null) {
            LogUtil.error(logger, "BEAN_CALL_ERROR, CommunicationConfiguration is null");
            return "";
        }

        try {
            ApiDefinition apiDefinition = beanApiHelper.parseApiDefinition(config.getUri());

            // invoke spring bean method in current spring context
            Object objService = ApplicationContextUtils.getBean(apiDefinition.getBeanId());
            Method method = beanApiHelper.getServiceMethod(objService, apiDefinition);
            Object request = beanApiHelper.parseParam(method, messageContent);
            result = method.invoke(objService, request);
        } catch (JsonSyntaxException e) {
            LogUtil.error(logger, e,
                    "BEAN_CALL_ERROR_PREX, Failed to send message because of error occurs when trying to get bean with config:"
                            + config.getUri());
            throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL,
                    "Param of input is illegal");
        } catch (Exception e) {
            LogUtil.error(logger, e,
                    "BEAN_CALL_ERROR_PREX, Failed to send message because of error occurs when trying to get bean with config:"
                            + config.getUri());
            throw new RuntimeException("Failed to send message to springbean.");
        }
        return result != null ? JsonUtil.toJSONString(result) : "";
    }

}