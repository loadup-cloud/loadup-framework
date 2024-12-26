package com.github.loadup.components.gateway.plugin.repository.database.dal.ibatis;

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.SecurityDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.SecurityDO;

import java.util.*;

public class IbatisSecurityDAO implements SecurityDAO {

    /**
     * @see SecurityDAO#insert(SecurityDO)
     */
    public String insert(SecurityDO security) {
        if (security == null) {
            throw new IllegalArgumentException("Can't insert a null data object into db.");
        }

        return security.getSecurityStrategyCode();
    }

    /**
     * @see SecurityDAO#load(String, String, String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public SecurityDO load(String clientId, String securityStrategyCode, String operateType, String algoName) {
        Map param = new HashMap();

        param.put("clientId", clientId);
        param.put("securityStrategyCode", securityStrategyCode);
        param.put("operateType", operateType);
        param.put("algoName", algoName);

        return null;

    }

    /**
     * @see SecurityDAO#lock(String, String, String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public SecurityDO lock(String clientId, String securityStrategyCode, String operateType, String algoName) {
        Map param = new HashMap();

        param.put("clientId", clientId);
        param.put("securityStrategyCode", securityStrategyCode);
        param.put("operateType", operateType);
        param.put("algoName", algoName);

        return null;

    }

    /**
     * @see SecurityDAO#loadAll()
     */
    @SuppressWarnings("unchecked")
    public List<SecurityDO> loadAll() {

        return null;

    }

    /**
     * @see SecurityDAO#loadByClientId(String)
     */
    @SuppressWarnings("unchecked")
    public List<SecurityDO> loadByClientId(String clientId) {

        return null;

    }

    /**
     * @see SecurityDAO#update(SecurityDO)
     */
    public int update(SecurityDO security) {
        if (security == null) {
            throw new IllegalArgumentException("Can't update by a null data object.");
        }

        return 0;
    }

    /**
     * @see SecurityDAO#delete(String, String, String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int delete(String clientId, String securityStrategyCode, String operateType, String algoName) {
        Map param = new HashMap();

        param.put("clientId", clientId);
        param.put("securityStrategyCode", securityStrategyCode);
        param.put("operateType", operateType);
        param.put("algoName", algoName);

        return 0;
    }

    /**
     * @see SecurityDAO#deleteByClientId(String)
     */
    public int deleteByClientId(String clientId) {

        return 0;
    }

}	