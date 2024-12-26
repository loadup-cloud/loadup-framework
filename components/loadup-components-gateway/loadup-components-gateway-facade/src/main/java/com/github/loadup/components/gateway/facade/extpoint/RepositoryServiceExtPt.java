package com.github.loadup.components.gateway.facade.extpoint;

import com.alibaba.cola.extension.ExtensionPointI;
import com.github.loadup.components.gateway.facade.model.*;

import java.util.List;

/**
 * <p>
 * repositoryServiceExt.java
 * </p>
 */
public interface RepositoryServiceExtPt extends ExtensionPointI {

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
    List<InterfaceDto> queryInterface(Integer pageSize, Integer page, String tntInstId,
                                      String interfaceId, String clientId, String type,
                                      String status, String interfaceName);

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