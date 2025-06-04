/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.repository.common;

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

import com.github.loadup.components.gateway.common.util.UriUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.model.communication.ProtocolType;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface relevant configuration builder
 */
public abstract class AbstractInterfaceConfigBuilder<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInterfaceConfigBuilder.class);

    private static final String URI_STRING = "uri";

    private static final String SECURITY_STRATEGY_CODE = "security_strategy_code";

    private static final String HEADER_ASSEMBLE = "integration_service_request_header_assemble";

    private static final String BODY_ASSEMBLE = "integration_service_request_assemble";

    private static final String RESPONSE_PARSE = "integration_service_response_parser";

    /**
     * generate biz key
     * <p>
     * format should follow the template which is defined in SPI_CONF
     * 0 integration_uri,
     * 1 security_strategy_code,
     * 2 integration_service_request_header_assemble,
     * 3 integration_service_request_assemble,
     * 4 integration_service_response_parser,
     * 5 communication_properties
     */
    public String generateBizKey(String url) {
        /**
         * integration_uri follow the pattern of
         * http://domaneName/path
         * https://domainName/path
         * TR://domainName:12000/class/method
         * SPRINGSPRINGBEAN://class/method
         *
         * http://wf.rate.query would be splited into [http, wf.rate.query]
         * http://wf.com/rate/query.htm would be splited into [http, wf.com, rate, query.htm]
         */
        // domainName.path || class.method
        return StringUtils.replace(UriUtil.getURIPath(url), Constant.PATH_SEPARATOR, Constant.PATH_CONJUNCTION);
    }

    /**
     * get URl path with url columns[0]
     */
    protected String getURIPathWithUrl(String uri) {
        /**
         * integration_uri follow the pattern of
         * http://domaneName/path
         * https://domainName/path
         * TR://domainName:12000/class/method
         * SPRINGSPRINGBEAN://class/method
         *
         * http://wf.rate.query would be splited into [http, wf.rate.query]
         * http://wf.com/rate/query.htm would be splited into [http, wf.com, rate, query.htm]
         */
        int index = StringUtils.indexOf(uri, Constant.URI_SEPARATOR);
        if (index >= 0 && index < (uri.length() - 3)) {
            return StringUtils.substring(uri, index + 3);
        } else {
            return uri;
        }
    }

    /**
     * get URL path from uri string
     */
    protected String getURLPath(String... columns) {
        if (StringUtils.contains(columns[0], Constant.URI_SEPARATOR)) {
            return columns[0];
        } else {
            return ProtocolType.HTTP.getCode().concat(Constant.URI_SEPARATOR).concat(columns[0]);
        }
    }

    /**
     * get protocol and set it as HTTP as default if can't parse protocol from string
     */
    protected String getProtocol(String url) {
        /**
         * integration_uri follow the pattern of
         * http://domaneName/path
         * https://domainName/path
         * TR://domainName:12000/class/method
         * SPRINGSPRINGBEAN://class/method
         *
         * http://wf.rate.query would be splited into [http, wf.rate.query]
         * http://wf.com/rate/query.htm would be splited into [http, wf.com, rate, query.htm]
         */
        int index = StringUtils.indexOf(url, Constant.URI_SEPARATOR);
        if (index >= 0 && index < (url.length() - 3)) {
            return StringUtils.substring(url, 0, index);
        } else {
            return ProtocolType.HTTP.getCode();
        }
    }

    /**
     * get domain from URI string
     */
    protected String getDomain(String url) {
        /**
         * integration_uri follow the pattern of
         * http://domaneName/path
         * https://domainName/path
         * TR://domainName:12000/class/method
         * SPRINGSPRINGBEAN://class/method
         *
         * http://wf.rate.query would be splited into [http, wf.rate.query]
         * http://wf.com/rate/query.htm would be splited into [http, wf.com, rate, query.htm]
         */
        String uriString = UriUtil.getURIPath(url);
        String[] domainPath = StringUtils.split(uriString, Constant.PATH_SEPARATOR, 2);
        return domainPath[0];
    }

    /**
     * validate mandatory fields
     * <p>
     * format should follow the template which is defined in SPI_CONF
     * 0 integration_uri,
     * 1 security_strategy_code,
     */
    protected boolean validate(String url, String securityStrategyCode) {
        List<String> invalidFieldList = new ArrayList<String>(2);
        if (StringUtils.isBlank(url)) {
            invalidFieldList.add(URI_STRING);
        }

        if (StringUtils.isBlank(securityStrategyCode)) {
            invalidFieldList.add(SECURITY_STRATEGY_CODE);
        }

        if (CollectionUtils.isNotEmpty(invalidFieldList)) {
            LogUtil.error(
                    logger,
                    "There are some invalid fields=",
                    StringUtils.join(invalidFieldList.iterator(), Constant.COMMA_SEPARATOR));
            return false;
        } else {
            return true;
        }
    }
}
