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

        // getSqlMapClientTemplate().insert("MS-INST-INSERT", inst);

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
