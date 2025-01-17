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

import com.github.loadup.components.gateway.plugin.repository.database.dal.daointerface.InterfaceDAO;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InterfaceDO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
