/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.extpoint;

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

import com.github.loadup.components.extension.api.IExtensionPoint;
import com.github.loadup.components.gateway.facade.model.*;
import java.util.List;

/**
 * <p>
 * repositoryServiceExt.java
 * </p>
 */
public interface RepositoryServiceExtPt extends IExtensionPoint {

    /**
     * persist the interface data
     */
    void saveOrUpdateInterface(InterfaceDto interfaceDto);

    /**
     * Load cert config list.
     */
    List<CertConfigDto> loadCertConfig();

    /**
     * Load cert algorithm config list.
     */
    List<CertAlgorithmConfigDto> loadCertAlgorithmConfig();

    /**
     * Load interface config list.
     */
    List<InterfaceConfigDto> loadInterfaceConfig();

    /**
     * Load message process config list.
     */
    List<MessageProcessConfigDto> loadMessageProcessConfig();

    /**
     * Load communication config list.
     */
    List<CommunicationConfigDto> loadCommunicationConfig();

    /**
     * Load message receiver config list.
     */
    List<MessageReceiverConfigDto> loadMessageReceiverConfig();

    /**
     * Load message sender config list.
     */
    List<MessageSenderConfigDto> loadMessageSenderConfig();

    /**
     * Load inst config list.
     */
    List<InstConfigDto> loadInstConfig();

    /**
     * Load inst interface config list.
     * ¬
     */
    List<InstInterfaceConfigDto> loadInstInterfaceConfig();

    /**
     * <p>
     * add new interface(API or SPI).
     * </p>
     */
    void addInterface(InterfaceDto dto);

    /**
     * <p>
     * update present interface(API or SPI).
     * </p>
     */
    void updateInterface(InterfaceDto dto);

    /**
     * <p>
     * query present interface(API or SPI).
     * </p>
     */
    List<InterfaceDto> queryInterface(
            Integer pageSize,
            Integer page,
            String tntInstId,
            String interfaceId,
            String clientId,
            String type,
            String status,
            String interfaceName);

    /**
     * <p>
     * remove present interface(API or SPI).
     * </p>
     */
    void removeInterface(String interfaceId);

    /**
     * upgrade interface
     */
    void upgradeInterface(InterfaceDto dto);

    /**
     * online interface
     */
    void onlineInterface(String interfaceId);

    /**
     * offline interface
     */
    void offlineInterface(String interfaceId);

    /**
     * add client to repository
     */
    public String addClient(ClientConfigDto clientConfigDto);

    /**
     * authorize interface to a client
     */
    public void authorizeClient(ClientInterfaceConfigDto clientInterfaceConfigDto);

    /**
     * deauthorize interface from a client
     */
    public void deauthorizeClient(ClientInterfaceConfigDto clientInterfaceConfigDto);

    /**
     * update inst config by clientId
     */
    public void updateClient(ClientConfigDto clientConfigUpdateDto);

    /**
     * query inst config by clientId
     */
    public ClientConfigDto queryClient(String clientId);

    /**
     * <p>
     * remove present instConfig
     * </p>
     */
    public int removeClient(String clientId);

    /**
     * add new cert config.
     */
    public SecurityConfigDto addSecurity(SecurityConfigDto dto);

    /**
     * update present cert config
     */
    public void updateSecurity(SecurityConfigDto request);

    /**
     * query cert config by client id
     */
    public List<SecurityConfigDto> querySecurityByClient(String clientId);

    /**
     * remove cert config by certCode
     */
    public int removeSecurity(SecurityConfigDto certCode);
}
