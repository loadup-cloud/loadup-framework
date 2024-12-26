package com.github.loadup.components.gateway.common.util;

import com.github.loadup.components.gateway.core.common.Constant;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.github.loadup.components.gateway.core.common.Constant.PATH_CONJUNCTION;
import static com.github.loadup.components.gateway.core.common.Constant.PATH_SEPARATOR;

/**
 *
 */
public class InterfaceConfigUtil {

    /**
     *
     */
    public static String generateInterfaceId(String url, String tenantId, String version, String type,
                                             Map<String, String> communicationProperties) {

        if (MapUtils.isNotEmpty(communicationProperties)) {
            String customInterfaceId = communicationProperties.get(Constant.INTERFACE_ID);
            if (StringUtils.isNotBlank(customInterfaceId)) {
                return customInterfaceId;
            }
        }

        String subInterfaceId = StringUtils.replace(UriUtil.getURIPath(url), PATH_SEPARATOR, PATH_CONJUNCTION);

        return StringUtils.defaultIfBlank(tenantId, "platform") + PATH_CONJUNCTION + type
                + PATH_CONJUNCTION + subInterfaceId + PATH_CONJUNCTION + version;
    }
}