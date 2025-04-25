/**
 
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.dal.convertor;

/*-
 * #%L
 * loadup-components-test
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

import com.github.loadup.components.testify.dal.dataobject.OrgDbDO;
import com.github.loadup.components.testify.dal.table.OrgDb;

/**
 * 
 *
 */
public class OrgDbConvertor {

    /**
     * 表对象转换为DO对象
     *
     * @param orgDb
     * @return
     */
    public static OrgDbDO convert2DO(OrgDb orgDb) {

        if (orgDb == null) {
            return null;
        }

        OrgDbDO orgDbDO = new OrgDbDO();

        orgDbDO.setGmtCreate(orgDb.getGmt_create());
        orgDbDO.setGmtModify(orgDb.getGmt_modify());
        orgDbDO.setMemo(orgDb.getMemo());
        orgDbDO.setSourceData(orgDb.getSource_data());
        orgDbDO.setSourceId(orgDb.getSource_id());
        orgDbDO.setSourceRule(orgDb.getSource_rule());
        orgDbDO.setSystem(orgDb.getSystem());

        return orgDbDO;
    }

}
