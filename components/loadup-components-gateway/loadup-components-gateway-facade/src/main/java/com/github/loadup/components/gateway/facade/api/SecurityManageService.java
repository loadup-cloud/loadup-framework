package com.github.loadup.components.gateway.facade.api;

import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;

/**
 *
 */
public interface SecurityManageService {
    /**
     * add new cert config.
     */
    CertConfigAddResponse add(CertConfigAddRequest request);

    /**
     * update present cert config
     */
    CertConfigUpdateResponse update(CertConfigUpdateRequest request);

    /**
     * query cert config by client id
     */
    CertConfigQueryResponse query(CertConfigQueryRequest clientId);

    /**
     * remove cert config by certCode
     */
    CertConfigRemoveResponse remove(CertConfigRemoveRequest certCode);
}