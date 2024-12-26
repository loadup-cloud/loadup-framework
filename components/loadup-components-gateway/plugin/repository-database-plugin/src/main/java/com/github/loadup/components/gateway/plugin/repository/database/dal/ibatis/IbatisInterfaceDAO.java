package com.github.loadup.components.gateway.plugin.repository.database.dal.ibatis;

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.InterfaceDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InterfaceDO;

import java.util.*;

public class IbatisInterfaceDAO implements InterfaceDAO {

    /**
     * @see InterfaceDAO#insert(InterfaceDO)
     */
    public String insert(InterfaceDO interfaceDO) {
        if (interfaceDO == null) {
            throw new IllegalArgumentException("Can't insert a null data object into db.");
        }

        return interfaceDO.getInterfaceId();
    }

    /**
     * @see InterfaceDAO#update(InterfaceDO)
     */
    public int update(InterfaceDO interfaceDO) {
        if (interfaceDO == null) {
            throw new IllegalArgumentException("Can't update by a null data object.");
        }

        return 0;
    }

    /**
     * @see InterfaceDAO#loadByInterfaceId(String)
     */
    public InterfaceDO loadByInterfaceId(String interfaceId) {

        return null;

    }

    /**
     * @see InterfaceDAO#lockByInterfaceId(String)
     */
    public InterfaceDO lockByInterfaceId(String interfaceId) {

        return null;

    }

    /**
     * @see InterfaceDAO#lock(String, String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<InterfaceDO> lock(String type, String url, String tenantId) {
        Map param = new HashMap();

        param.put("type", type);
        param.put("url", url);
        param.put("tenantId", tenantId);

        return null;

    }

    /**
     * @see InterfaceDAO#loadAll()
     */
    @SuppressWarnings("unchecked")
    public List<InterfaceDO> loadAll() {

        return null;

    }

    /**
     * @see InterfaceDAO#delete(String)
     */
    public int delete(String interfaceId) {

        return 0;
    }

    /**
     * @see InterfaceDAO#loadByPage(String, String, String, String, String, String, int, int)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<InterfaceDO> loadByPage(String tenantId, String interfaceId, String clientId, String type, String status,
                                        String interfaceName, int offset, int rowcount) {
        Map param = new HashMap();

        param.put("tenantId", tenantId);
        param.put("interfaceId", interfaceId);
        param.put("clientId", clientId);
        param.put("type", type);
        param.put("status", status);
        param.put("interfaceName", interfaceName);
        param.put("offset", offset);
        param.put("rowcount", rowcount);

        return null;

    }

}	