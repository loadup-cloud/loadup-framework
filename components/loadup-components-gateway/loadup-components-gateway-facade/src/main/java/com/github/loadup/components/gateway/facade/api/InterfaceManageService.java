package com.github.loadup.components.gateway.facade.api;

import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;

/**
 *
 */
public interface InterfaceManageService {

    /**
     * <p>
     * add new interface(API or SPI).
     * </p>
     */
    InterfaceConfigAddResponse add(InterfaceConfigAddRequest request);

    /**
     * <p>
     * update present interface(API or SPI).
     * </p>
     */
    InterfaceConfigUpdateResponse update(InterfaceConfigUpdateRequest request);

    /**
     * 升级的请求版本号应比已有版本号都大。
     * upgrade present interface
     */
    InterfaceConfigUpgradeResponse upgrade(InterfaceConfigUpgradeRequest request);

    /**
     * <p>
     * query present interface(API or SPI).
     * </p>
     */
    InterfaceConfigQueryResponse query(InterfaceConfigQueryRequest request);

    /**
     * <p>
     * remove present interface(API or SPI).
     * </p>
     */
    InterfaceConfigRemoveResponse remove(InterfaceConfigRemoveRequest request);

    /**
     * 接口只会有一个版本生效。
     */
    InterfaceConfigOnlineResponse online(InterfaceConfigOnlineRequest request);

    /**
     * make interface from valid to invalid
     */
    InterfaceConfigOfflineResponse offline(InterfaceConfigOfflineRequest request);
}