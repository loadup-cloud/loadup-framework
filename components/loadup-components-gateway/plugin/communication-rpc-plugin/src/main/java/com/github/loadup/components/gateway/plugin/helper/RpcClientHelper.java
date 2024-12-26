package com.github.loadup.components.gateway.plugin.helper;

import com.github.loadup.components.gateway.facade.spi.OpenApi;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.common.Constant.PATH_CONJUNCTION;

/**
 *
 */
@Component("gatewayRpcClientHelper")
public class RpcClientHelper {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(RpcClientHelper.class);

    /**
     * environment
     */
    @Resource
    private Environment environment;

    /**
     * get open api instance
     *
     * @throws Exception
     */
    private OpenApi getOpenApiInstance(String uniqueId, int timeout) throws Exception {
        return null;
    }

    /**
     * get open api
     */
    public OpenApi getOpenApi(String interfaceId, int timeout) {
        OpenApi openApi = null;
        try {
            String openApiServiceName = StringUtils.split(interfaceId, PATH_CONJUNCTION)[1];
            openApi = getOpenApiInstance(openApiServiceName, timeout);
        } catch (Exception e) {
            LogUtil.error(logger, "create open api service instance error, interfaceId=",
                    interfaceId, e);
        }
        return openApi;
    }
}