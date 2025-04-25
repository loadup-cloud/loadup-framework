package com.github.loadup.components.testify.model;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟数据库对象
 *
 * @author tantian.wc
 * 
 */
public class VirtualDataSet extends TestUnit {

    /**
     * 数据库表的列表
     */
    private List<VirtualTable> virtualTables = new ArrayList<VirtualTable>();

    public static VirtualDataSet getInstance() {
        return new VirtualDataSet();
    }

    /**
     * 添加一系列DO，会自动生成VirtualTable
     *
     * @param dataObjects
     * @return
     */
    public VirtualDataSet addDataObjects(List<Object> dataObjects) {
        if (this.virtualTables == null) {
            this.virtualTables = new ArrayList<VirtualTable>();
        }

        for (Object dataObject : dataObjects) {
            VirtualTable virtualTable = new VirtualTable(dataObject);
            this.virtualTables.add(virtualTable);
        }
        return this;

    }

    /**
     * 添加一系列VirtualTable
     *
     * @param virtualTables
     * @return
     */
    public VirtualDataSet addTables(List<VirtualTable> virtualTables) {
        if (this.virtualTables == null) {
            this.virtualTables = new ArrayList<VirtualTable>();
        }

        for (VirtualTable virtualTable : virtualTables) {
            this.virtualTables.add(virtualTable);

        }
        return this;
    }

    /**
     * 添加一个VirtualTable
     *
     * @param virtualTables
     * @return
     */
    public VirtualDataSet addVirtualTable(VirtualTable virtualTable) {
        if (this.virtualTables == null) {
            this.virtualTables = new ArrayList<VirtualTable>();
        }
        this.virtualTables.add(virtualTable);
        return this;
    }

    /**
     * 通过表名获取VirtualTable
     *
     * @param tableName
     * @return
     */
    public VirtualTable getVirtualTableByName(String tableName) {
        if (virtualTables != null && tableName != null) {
            for (VirtualTable virtualTable : virtualTables) {
                if (tableName.equalsIgnoreCase(virtualTable.getTableName())) {
                    return virtualTable;
                }
            }
        }
        return null;
    }

    public List<VirtualTable> getVirtualTables() {
        return virtualTables;
    }

    public void setVirtualTables(List<VirtualTable> virtualTables) {
        this.virtualTables = virtualTables;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualDataSet [virtualTables=" + virtualTables + "]";
    }

}
