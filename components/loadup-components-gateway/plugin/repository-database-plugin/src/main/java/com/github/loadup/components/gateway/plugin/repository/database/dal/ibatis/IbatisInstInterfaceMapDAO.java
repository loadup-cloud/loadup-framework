/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.InstInterfaceMapDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InstInterfaceMapDO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
