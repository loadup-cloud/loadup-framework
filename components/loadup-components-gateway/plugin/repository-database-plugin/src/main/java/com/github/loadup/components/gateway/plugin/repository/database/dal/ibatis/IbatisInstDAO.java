package com.github.loadup.components.gateway.plugin.repository.database.dal.ibatis;

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.InstDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InstDO;

import java.util.List;

public class IbatisInstDAO implements InstDAO {

    /**
     * @see InstDAO#insert(InstDO)
     */
    public String insert(InstDO inst) {
        if (inst == null) {
            throw new IllegalArgumentException("Can't insert a null data object into db.");
        }

        //getSqlMapClientTemplate().insert("MS-INST-INSERT", inst);

        return inst.getClientId();
    }

    /**
     * @see InstDAO#update(InstDO)
     */
    public int update(InstDO inst) {
        if (inst == null) {
            throw new IllegalArgumentException("Can't update by a null data object.");
        }

        return 0;
    }

    /**
     * @see InstDAO#loadAll()
     */
    @SuppressWarnings("unchecked")
    public List<InstDO> loadAll() {

        return null;

    }

    /**
     * @see InstDAO#load(String)
     */
    public InstDO load(String clientId) {

        return null;

    }

    /**
     * @see InstDAO#lock(String)
     */
    public InstDO lock(String clientId) {

        return null;

    }

    /**
     * @see InstDAO#delete(String)
     */
    public int delete(String clientId) {

        return 0;
    }

}	