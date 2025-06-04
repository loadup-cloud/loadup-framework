/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.api;

/*-
 * #%L
 * loadup-components-gateway-facade
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
