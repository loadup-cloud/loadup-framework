package com.github.loadup.components.gateway.facade.api;

import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;

/**
 *
 */
public interface ClientManageService {
    /**
     * add new client
     */
    ClientConfigAddResponse add(ClientConfigAddRequest request);

    /**
     * authorize interface to an client
     */
    ClientConfigAuthorizeResponse authorize(ClientConfigAuthorizeRequest request);

    /**
     * deauthorize interface to an client
     */
    ClientConfigDeauthorizeResponse deauthorize(ClientConfigDeauthorizeRequest request);

    /**
     * update client config by clientId
     */
    ClientConfigUpdateResponse update(ClientConfigUpdateRequest request);

    /**
     * query client config by clientId
     */
    ClientConfigQueryResponse query(ClientConfigQueryRequest request);

    /**
     * <p>
     * remove present clientConfig
     * </p>
     */
    ClientConfigRemoveResponse remove(ClientConfigRemoveRequest request);
}