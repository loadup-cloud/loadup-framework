package com.github.loadup.components.gateway.plugin.repository.database.dal.ibatis;

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.InstInterfaceMapDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InstInterfaceMapDO;

import java.util.*;

public class IbatisInstInterfaceMapDAO implements InstInterfaceMapDAO {

    /**
     * @see InstInterfaceMapDAO#insert(InstInterfaceMapDO)
     */
    public String insert(InstInterfaceMapDO instInterfaceMap) {
        if (instInterfaceMap == null) {
            throw new IllegalArgumentException("Can't insert a null data object into db.");
        }

        return instInterfaceMap.getClientId();
    }

    /**
     * @see InstInterfaceMapDAO#loadAll()
     */
    @SuppressWarnings("unchecked")
    public List<InstInterfaceMapDO> loadAll() {

        return null;

    }

    /**
     * @see InstInterfaceMapDAO#loadByClientId(String)
     */
    @SuppressWarnings("unchecked")
    public List<InstInterfaceMapDO> loadByClientId(String clientId) {

        return null;

    }

    /**
     * @see InstInterfaceMapDAO#loadByInterfaceId(String)
     */
    @SuppressWarnings("unchecked")
    public List<InstInterfaceMapDO> loadByInterfaceId(String interfaceId) {

        return null;

    }

    /**
     * @see InstInterfaceMapDAO#loadByClientIdAndInterfaceId(String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public InstInterfaceMapDO loadByClientIdAndInterfaceId(String clientId, String interfaceId) {
        Map param = new HashMap();

        param.put("clientId", clientId);
        param.put("interfaceId", interfaceId);

        return null;

    }

    /**
     * @see InstInterfaceMapDAO#delete(String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int delete(String clientId, String interfaceId) {
        Map param = new HashMap();

        param.put("clientId", clientId);
        param.put("interfaceId", interfaceId);

        return 0;
    }

    /**
     * @see InstInterfaceMapDAO#deleteByClientId(String)
     */
    public int deleteByClientId(String clientId) {

        return 0;
    }

    /**
     * @see InstInterfaceMapDAO#deleteByInterfaceId(String)
     */
    public int deleteByInterfaceId(String interfaceId) {

        return 0;
    }

}	