/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml.cpUnit;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.constant.ActsPathConstants;
import com.alipay.test.acts.constant.ActsSpecialMapConstants;
import com.alipay.test.acts.context.ActsCaseContextHolder;
import com.alipay.test.acts.db.enums.CSVColEnum;
import com.alipay.test.acts.helper.CSVHelper;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.object.enums.UnitFlagEnum;
import com.alipay.test.acts.yaml.cpUnit.property.BaseUnitProperty;
import com.alipay.test.acts.yaml.enums.CPUnitTypeEnum;
import org.testng.Assert;

/**
 * 数据库CheckPoint单位
 * 
 * @author baishuo.lp
 * @version $Id: DataBaseCPUnit.java, v 0.1 2015年8月12日 上午11:23:52 baishuo.lp Exp $
 */
public class DataBaseCPUnit extends BaseCPUnit {

    private static final Log                    logger        = LogFactory
                                                                  .getLog(DataBaseCPUnit.class);

    private final Map<String, BaseUnitProperty> modifyMap     = new LinkedHashMap<String, BaseUnitProperty>();

    private List<String>                        conditionKeys = new ArrayList<String>();

    public DataBaseCPUnit(String unitName, Map<String, Object> rawData) {
        this.unitName = unitName;
        this.unitType = CPUnitTypeEnum.DATABASE;
        this.description = "" + rawData.get("__desc");
        this.targetCSVPath = ActsPathConstants.DB_DATA_PATH + this.unitName + ".csv";
        rawData.remove("__desc");

        for (Entry<String, Object> entry : rawData.entrySet()) {
            String keyName = entry.getKey();
            if (keyName.startsWith("$")) {
                this.specialMap.put(keyName, entry.getValue());
            } else {
                Object value = entry.getValue();
                String flagCode = null;
                if (keyName.endsWith("]")) {
                    flagCode = keyName.substring(keyName.indexOf('[') + 1, keyName.length() - 1);
                    keyName = keyName.substring(0, keyName.indexOf('['));
                }
                BaseUnitProperty property = new BaseUnitProperty(keyName, this.unitName + "."
                                                                          + keyName, value);
                if (flagCode != null) {
                    property.setFlagCode(flagCode);
                }
                this.modifyMap.put(keyName, property);
            }
        }
        this.loadCSVFile();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object dump() {
        Map dumpMap = new LinkedHashMap();
        dumpMap.put("__desc", this.description);
        for (Entry<String, BaseUnitProperty> entry : this.modifyMap.entrySet()) {
            dumpMap.putAll((Map) entry.getValue().dump());
        }
        for (Entry<String, Object> entry : this.specialMap.entrySet()) {
            dumpMap.put(entry.getKey(), entry.getValue());
        }
        return dumpMap;
    }

    /**
     * 刷入uniqueMap数据
     */
    public void loadUniqueMap() {
        Map<String, Object> uniqueMap = ActsCaseContextHolder.get().getUniqueMap();
        for (Entry<String, BaseUnitProperty> entry : this.modifyMap.entrySet()) {
            BaseUnitProperty property = entry.getValue();
            String columnName = property.getKeyName();
            Object value = null;
            if (uniqueMap.containsKey(this.unitName + "-" + this.description + "-" + columnName)) {
                value = uniqueMap.get(this.unitName + "-" + this.description + "-" + columnName);
            } else if (uniqueMap.containsKey(this.unitName + "-" + columnName)) {
                value = uniqueMap.get(this.unitName + "-" + columnName);
            } else if (uniqueMap.containsKey(columnName)) {
                value = uniqueMap.get(columnName);
            } else if (this.specialMap.containsKey("$" + columnName)
                       && !ActsSpecialMapConstants.specialConstantSet.contains("$" + columnName)) {
                String specialKey = (String) this.specialMap.get("$" + columnName);
                if (uniqueMap.containsKey(specialKey)) {
                    value = uniqueMap.get(specialKey);
                }
            } else {
                property.setUnique(false);
                value = property.getExpectValue();
            }
            property.setOldExpectValue(property.getExpectValue());
            property.setExpectValue(value);
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadCSVFile() {
        List tableList = CSVHelper.readFromCsv(this.targetCSVPath);
        if (tableList == null || tableList.size() == 0)
            ActsLogUtil.fail(logger, this.targetCSVPath + "文件内容为空");
        String[] labels = (String[]) tableList.get(0);
        int baseIndex = 0, colNameCol = 0, commentCol = 0, typeCol = 0, flagCol = 0, indexCol = -1;
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i].toLowerCase().trim();
            if (StringUtils.equals(label, this.description)) {
                indexCol = i;
            } else {
                CSVColEnum labelEnum = CSVColEnum.getByCode(label);
                if (labelEnum != null) {
                    switch (CSVColEnum.getByCode(label)) {
                        case COLUMN:
                            colNameCol = i;
                            baseIndex++;
                            break;
                        case COMMENT:
                            commentCol = i;
                            baseIndex++;
                            break;
                        case FLAG:
                            flagCol = i;
                            baseIndex++;
                            break;
                        case TYPE:
                            typeCol = i;
                            baseIndex++;
                            break;
                        default:
                            Assert.fail("csv文件格式有误");
                    }
                }
            }
        }
        if (indexCol == -1) {
            Assert.assertTrue(StringUtils.isNumeric(this.description),this.unitName + "若无法匹配列名，则desc必须为数字，当前为" + this.description
                );
            indexCol = baseIndex + Integer.valueOf(this.description) - 1;
        }

        for (int i = 1; i < tableList.size(); i++) {
            String[] row = (String[]) tableList.get(i);
            String columnName = row[colNameCol].trim();
            String type = row[typeCol].trim();
            String comment = row[commentCol].trim();
            String flagCode = row[flagCol].trim();
            String value = row[indexCol].trim();

            BaseUnitProperty property;
            if (this.modifyMap.containsKey(columnName)) {
                property = this.modifyMap.get(columnName);
                if (this.modifyMap.get(columnName).getFlagCode() == null) {
                    property.setFlagCode(flagCode);
                }
            } else {
                property = new BaseUnitProperty(columnName, this.unitName + "." + columnName, value);
                property.setExpectValue(value);
                property.setFlagCode(flagCode);
            }
            if (UnitFlagEnum.getByCode(property.getFlagCode()) == UnitFlagEnum.C) {
                this.conditionKeys.add(columnName);
            }
            property.setBaseValue(value);
            property.setBaseFlagCode(flagCode);
            property.setDbColumnComment(comment);
            property.setDbColumnType(type);
            this.modifyMap.put(columnName, property);
        }
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "DataBaseCPUnit [unitType=" + unitType + ", modifyMap=" + modifyMap + ", unitName="
               + unitName + ", description=" + description + ", targetCSVPath=" + targetCSVPath
               + ", specialMap=" + specialMap + "]";
    }

    /**
     * Getter method for property <tt>modifyMap</tt>.
     * 
     * @return property value of modifyMap
     */
    public Map<String, BaseUnitProperty> getModifyMap() {
        return modifyMap;
    }

    /**
     * Getter method for property <tt>conditionKeys</tt>.
     * 
     * @return property value of conditionKeys
     */
    public List<String> getConditionKeys() {
        return conditionKeys;
    }

    /**
     * Setter method for property <tt>conditionKeys</tt>.
     * 
     * @param conditionKeys value to be assigned to property conditionKeys
     */
    public void setConditionKeys(List<String> conditionKeys) {
        this.conditionKeys = conditionKeys;
    }

}
