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

/**
 *
 */
public class TenantUtil {

    /**
     * get current running tenantId
     */
    public static String getCurrentTenantId() {
        //        Tenant tenant = org.springframework.cloud.antfin.api.context.TenantUtil.getTenant();
        //        AssertUtil.isNotNull(tenant, GatewayliteErrorCode.PARAM_ILLEGAL,
        //            "Cannot get current tenant information.");
        //        return tenant.getTenantId();
        return null;
    }

    /**
     * get current running clientId
     */
    public static String getCurrentClientId() {
        //        Tenant tenant = org.springframework.cloud.antfin.api.context.TenantUtil.getTenant();
        //        AssertUtil.isNotNull(tenant, GatewayliteErrorCode.PARAM_ILLEGAL,
        //                "Cannot get current tenant information.");
        //        Map<String, String> properties = tenant.getAttributes();
        //        AssertUtil.isNotNull(properties, GatewayliteErrorCode.PARAM_ILLEGAL,
        //                "Cannot get current clientId.");
        //        String clientId = properties.get(Constant.KEY_HTTP_CLIENT_ID);
        //        AssertUtil.isNotNull(clientId, GatewayliteErrorCode.PARAM_ILLEGAL,
        //                "Cannot get current clientId.");
        //        return clientId;
        return null;
    }

    /**
     * get current running clientId
     */
    public static String getClientIdByTenantId(String tenantId) {
        //        List<Tenant> tenants = org.springframework.cloud.antfin.api.context.TenantUtil
        //            .getAllTenants();
        //        AssertUtil.isNotNull(tenants, GatewayliteErrorCode.PARAM_ILLEGAL,
        //            "Cannot get tenant list.");
        //        Tenant matchedTenant = null;
        //        for (Tenant tenant : tenants) {
        //            if (StringUtils.equals(tenantId, tenant.getTenantId())) {
        //                matchedTenant = tenant;
        //                break;
        //            }
        //        }
        //        AssertUtil.isNotNull(matchedTenant, GatewayliteErrorCode.PARAM_ILLEGAL,
        //                "Cannot get matched tenant.");
        //        Map<String, String> properties = matchedTenant.getAttributes();
        //        AssertUtil.isNotNull(properties, GatewayliteErrorCode.PARAM_ILLEGAL,
        //            "Cannot get clientId.");
        //        String clientId = properties.get(Constant.KEY_HTTP_CLIENT_ID);
        //        AssertUtil.isNotNull(clientId, GatewayliteErrorCode.PARAM_ILLEGAL, "Cannot get clientId.");
        //        return clientId;
        return null;
    }
}
