package com.github.loadup.components.gateway.common.util;

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