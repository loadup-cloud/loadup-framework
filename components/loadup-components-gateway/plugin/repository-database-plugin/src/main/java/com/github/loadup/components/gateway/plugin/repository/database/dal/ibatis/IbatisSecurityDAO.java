package com.github.loadup.components.gateway.plugin.repository.database.dal.ibatis;

/*-
 * #%L
 * repository-database-plugin
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

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.SecurityDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.SecurityDO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
